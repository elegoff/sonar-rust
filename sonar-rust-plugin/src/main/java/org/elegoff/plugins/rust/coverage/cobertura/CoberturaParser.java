package org.elegoff.plugins.rust.coverage.cobertura;

import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMInputCursor;
import org.elegoff.plugins.rust.language.RustLanguage;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.utils.MessageException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import static java.util.Locale.ENGLISH;
import static org.sonar.api.utils.ParsingUtils.parseNumber;

public class CoberturaParser {

    private static final Logger LOG = Loggers.get(CoberturaParser.class);

    private int unresolvedFilenameCount;

    public void parseReport(File xmlFile, SensorContext context, final Map<InputFile, NewCoverage> coverageData) throws XMLStreamException {
        LOG.info("Parsing report '{}'", xmlFile);
        unresolvedFilenameCount = 0;

        StaxParser parser = new StaxParser(rootCursor -> {
            File defaultBaseDirectory = context.fileSystem().baseDir();
            List<File> baseDirectories = Collections.singletonList(defaultBaseDirectory);
            try {
                rootCursor.advance();
            } catch (com.ctc.wstx.exc.WstxEOFException eofExc) {
                LOG.debug("Unexpected end of file is encountered", eofExc);
                throw new EmptyReportException();
            }
            SMInputCursor cursor = rootCursor.childElementCursor();
            while (cursor.getNext() != null) {
                if ("sources".equals(cursor.getLocalName())) {
                    baseDirectories = extractBaseDirectories(cursor, defaultBaseDirectory);
                } else if ("packages".equals(cursor.getLocalName())) {
                    collectFileMeasures(cursor.descendantElementCursor("class"), context, coverageData, baseDirectories);
                }
            }
        });
        parser.parse(xmlFile);
        if (unresolvedFilenameCount > 1) {
            LOG.error("Cannot resolve {} file paths, ignoring coverage measures for those files", unresolvedFilenameCount);
        }
    }

    private static List<File> extractBaseDirectories(SMInputCursor sources, File defaultBaseDirectory) throws XMLStreamException {
        List<File> baseDirectories = new ArrayList<>();
        SMInputCursor source = sources.childElementCursor("source");
        while (source.getNext() != null) {
            String path = FilenameUtils.normalize(source.collectDescendantText());
            if (!StringUtils.isBlank(path)) {
                File baseDirectory = new File(path);
                if (baseDirectory.isDirectory()) {
                    baseDirectories.add(baseDirectory);
                } else {
                    LOG.warn("Invalid directory path in 'source' element: {}", path);
                }
            }
        }
        if (baseDirectories.isEmpty()) {
            return Collections.singletonList(defaultBaseDirectory);
        }
        return baseDirectories;
    }

    private void collectFileMeasures(SMInputCursor classCursor, SensorContext context, Map<InputFile, NewCoverage> coverageData, List<File> baseDirectories)
            throws XMLStreamException {
        while (classCursor.getNext() != null) {
            String filename = FilenameUtils.normalize(classCursor.getAttrValue("filename"));
            InputFile inputFile = resolve(context, baseDirectories, filename);
            if (inputFile != null) {
                NewCoverage coverage = coverageData.computeIfAbsent(inputFile, f -> context.newCoverage().onFile(f));
                collectFileData(classCursor, coverage);
            } else {
                classCursor.advance();
            }
        }
    }

    @Nullable
    private InputFile resolve(SensorContext context, List<File> baseDirectories, String filename) {
        String absolutePath;
        File file = new File(filename);
        if (file.isAbsolute()) {
            if (!file.exists()) {
                logUnresolvedFile("Cannot resolve the file path '{}' of the coverage report, the file does not exist in all <source>.", filename);
            }
            absolutePath = file.getAbsolutePath();
        } else {
            List<File> fileList = baseDirectories.stream()
                    .map(base -> new File(base, filename))
                    .filter(File::exists)
                    .collect(Collectors.toList());
            if (fileList.isEmpty()) {
                logUnresolvedFile("Cannot resolve the file path '{}' of the coverage report, the file does not exist in all <source>.", filename);
                return null;
            }
            if (fileList.size() > 1) {
                logUnresolvedFile("Cannot resolve the file path '{}' of the coverage report, ambiguity, the file exists in several <source>.", filename);
                return null;
            }
            absolutePath = fileList.get(0).getAbsolutePath();
        }
        return context.fileSystem().inputFile(context.fileSystem().predicates().hasAbsolutePath(absolutePath));
    }

    private void logUnresolvedFile(String message, String filename) {
        unresolvedFilenameCount++;
        if (unresolvedFilenameCount == 1) {
            LOG.error(message, filename);
        }
    }

    private static void collectFileData(SMInputCursor classCursor, NewCoverage coverage) throws XMLStreamException {
        SMInputCursor line = classCursor.childElementCursor("lines").advance().childElementCursor("line");
        while (line.getNext() != null) {
            int lineId = Integer.parseInt(line.getAttrValue("number"));
            coverage.lineHits(lineId, Integer.parseInt(line.getAttrValue("hits")));

            String isBranch = line.getAttrValue("branch");
            String text = line.getAttrValue("condition-coverage");
            if (StringUtils.equals(isBranch, "true") && StringUtils.isNotBlank(text)) {
                String[] conditions = StringUtils.split(StringUtils.substringBetween(text, "(", ")"), "/");
                coverage.conditions(lineId, Integer.parseInt(conditions[1]), Integer.parseInt(conditions[0]));
            }
        }
    }
}