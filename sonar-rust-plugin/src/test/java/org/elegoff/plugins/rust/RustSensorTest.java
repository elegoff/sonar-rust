/**
 *
 * Sonar Rust Plugin (Community)
 * Copyright (C) 2020 Eric Le Goff
 * http://github.com/elegoff/sonar-rust
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.elegoff.plugins.rust;


import org.elegoff.plugins.rust.RustSensor;
import org.elegoff.plugins.rust.clippy.ClippySensor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.LogTester;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elegoff.plugins.rust.clippy.ClippySensorTest.assertNoErrorWarnDebugLogs;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RustSensorTest {
    private final File baseDir = new File("src/test/resources/org/elegoff/plugins/rust/sensor").getAbsoluteFile();

    private SensorContextTester context;

    private ActiveRules activeRules;
    private static Path workDir;



    @Rule
    public LogTester logTester = new LogTester();



    @Test
    public void sensor_descriptor() {
        activeRules = new ActiveRulesBuilder().build();
        DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
        sensor().describe(descriptor);

        assertThat(descriptor.name()).isEqualTo("Rust Sensor");
        assertThat(descriptor.languages()).containsOnly("rust");
        assertThat(descriptor.type()).isEqualTo(InputFile.Type.MAIN);
    }



    private RustSensor sensor() {
        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        FileLinesContext fileLinesContext = mock(FileLinesContext.class);
        when(fileLinesContextFactory.createFor(Mockito.any(InputFile.class))).thenReturn(fileLinesContext);
        CheckFactory checkFactory = new CheckFactory(activeRules);
        return new RustSensor(fileLinesContextFactory, checkFactory, new NoSonarFilter());


    }
}