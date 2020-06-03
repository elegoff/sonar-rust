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
package org.elegoff.plugins.rust.language;

import org.elegoff.plugins.rust.settings.RustLanguageSettings;
import org.junit.Test;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;

public class RustLanguageTest {
    @Test
    public void test() {
        RustLanguage language = new RustLanguage(new ConfigurationBridge(new MapSettings()));
        assertThat(language.getKey()).isEqualTo("rust");
        assertThat(language.getName()).isEqualTo("Rust");
        assertThat(language.getFileSuffixes()).hasSize(1).contains(".rs");
    }

    @Test
    public void custom_file_suffixes() {
        MapSettings settings = new MapSettings();
        settings.setProperty(RustLanguageSettings.FILE_SUFFIXES_KEY, "rs");

        RustLanguage language = new RustLanguage(new ConfigurationBridge(settings));
        assertThat(language.getFileSuffixes()).hasSize(1).contains("rs");
    }
}