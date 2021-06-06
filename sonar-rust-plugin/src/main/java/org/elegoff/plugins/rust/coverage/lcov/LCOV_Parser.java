package org.elegoff.plugins.rust.coverage.lcov;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elegoff.plugins.rust.coverage.lcov.LCOV_Fields.*;

public class LCOV_Parser {

    private final Map<InputFile, NewCoverage> fileCoverage;
    private final SensorContext sensorContext;
    private final Set<String> unknownPaths = new LinkedHashSet<>();
    private final FileChooser fc;
    private int pbCount = 0;

    private static final Logger LOG = Loggers.get(LCOV_Parser.class);

    private LCOV_Parser(List<String> lines, SensorContext sensorContext, FileChooser fc) {
        this.sensorContext = sensorContext;
        this.fc = fc;
        this.fileCoverage = parse(lines);
    }

    static LCOV_Parser build(SensorContext context, List<File> files, FileChooser fileChooser) {
        final List<String> lines;
        lines = new LinkedList<>();
        for (int i = 0, filesSize = files.size(); i < filesSize; i++) {
            File file = files.get(i);
            try (Stream<String> fileLines = Files.lines(file.toPath())) {
                lines.addAll(fileLines.collect(Collectors.toList()));
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not read content from file: " + file, e);
            }
        }
        LCOV_Parser parser = new LCOV_Parser(lines, context, fileChooser);
        return parser;
    }

    Map<InputFile, NewCoverage> getFileCoverage() {
        return fileCoverage;
    }

    List<String> unknownPaths() {
        return new ArrayList<>(unknownPaths);
    }

    int pbCount() {
        return pbCount;
    }

    private Map<InputFile, NewCoverage> parse(List<String> lines) {
        final Map<InputFile, FileContent> files = new HashMap<>();
        FileContent fileContent = null;
        int reportLineNum = 0;

        for (String line : lines) {
            reportLineNum++;
            if (!line.startsWith(String.valueOf(SF))) {
                if (fileContent != null) {
                    if (line.startsWith(String.valueOf(DA))) {
                        parseLineCoverage(fileContent, reportLineNum, line);

                    } else if (line.startsWith(String.valueOf(BRDA))) {
                        parseBranchCoverage(fileContent, reportLineNum, line);
                    }
                }
            } else {
                fileContent = files.computeIfAbsent(inputFileForSourceFile(line),
                        inputFile -> inputFile == null ? null : new FileContent(inputFile));

            }

        }

        Map<InputFile, NewCoverage> coveredFiles = new HashMap<>();

        files.entrySet().forEach(e -> {
            NewCoverage newCoverage = sensorContext.newCoverage().onFile(e.getKey());
            e.getValue().save(newCoverage);
            coveredFiles.put(e.getKey(), newCoverage);
        });
        return coveredFiles;
    }

    private void parseBranchCoverage(FileContent fileContent, int linum, String line) {
        try {
            String[] tokens = line.substring(String.valueOf(BRDA).length() + 1).trim().split(",");
            String lineNumber = tokens[0];
            String branchNumber = tokens[1] + tokens[2];
            String taken = tokens[3];

            fileContent.newBranch(Integer.valueOf(lineNumber), branchNumber, "-".equals(taken) ? 0 : Integer.valueOf(taken));
        } catch (Exception e) {
            logMismatch(String.valueOf(BRDA), linum, e);
        }
    }

    private void parseLineCoverage(FileContent fileContent, int linum, String line) {
        try {
            String execution = line.substring(String.valueOf(DA).length() + 1);
            String executionCount = execution.substring(execution.indexOf(',') + 1);
            String lineNumber = execution.substring(0, execution.indexOf(','));

            fileContent.newLine(Integer.valueOf(lineNumber), Integer.valueOf(executionCount));
        } catch (Exception e) {
            logMismatch(String.valueOf(DA), linum, e);
        }
    }

    private void logMismatch(String dataType, int linum, Exception e) {
        LOG.debug(String.format("Error while parsing LCOV report: can't save %s data for line %s of coverage report file (%s).", dataType, linum, e.toString()));
        pbCount++;
    }

    @CheckForNull
    private InputFile inputFileForSourceFile(String line) {
        String filePath;
        filePath = line.substring(String.valueOf(SF).length() + 1);
        InputFile inputFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().hasPath(filePath));

        if (inputFile == null) {
            inputFile = fc.getInputFile(filePath);
        }

        if (inputFile == null) {
            unknownPaths.add(filePath);
        }

        return inputFile;
    }

    private static class FileContent {
        private Map<Integer, Map<String, Integer>> branches = new HashMap<>();
        private Map<Integer, Integer> hits = new HashMap<>();
        private final int linesInFile;
        private final String filename;
        private static final String WRONG_LINE_MSG = "Line number %s doesn't exist in file %s";

        FileContent(InputFile inputFile) {
            linesInFile = inputFile.lines();
            filename = inputFile.filename();
        }

        private void validateLine(Integer linum) {
            if (linum >= 1 && linum <= linesInFile) {
                return;
            }
            throw new IllegalArgumentException(String.format(WRONG_LINE_MSG, linum, filename));
        }

        void newLine(Integer lineNumber, Integer executionCount) {
            validateLine(lineNumber);
            hits.merge(lineNumber, executionCount, Integer::sum);
        }

        void newBranch(Integer lineNumber, String branchNumber, Integer taken) {
            validateLine(lineNumber);
            Map<String, Integer> branchesForLine = branches.computeIfAbsent(lineNumber, l -> new HashMap<>());
            branchesForLine.merge(branchNumber, taken, Integer::sum);
        }

        void save(NewCoverage newCoverage) {
            for (Map.Entry<Integer, Integer> e : hits.entrySet()) {
                newCoverage.lineHits(e.getKey(), e.getValue());
            }
            for (Map.Entry<Integer, Map<String, Integer>> e : branches.entrySet()) {
                int line = e.getKey();
                int conditions = e.getValue().size();
                int covered = 0;
                for (Integer taken : e.getValue().values()) {
                    if (taken > 0) {
                        covered++;
                    }
                }

                newCoverage.conditions(line, conditions, covered);
                newCoverage.lineHits(line, hits.getOrDefault(line, 0) + covered);
            }
        }


    }

}
