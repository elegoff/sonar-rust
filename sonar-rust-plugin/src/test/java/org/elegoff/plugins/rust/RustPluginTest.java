/**
 * Sonar Rust Plugin (Community)
 * Copyright (C) 2021 Eric Le Goff
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

import junit.framework.TestCase;
import org.elegoff.plugins.rust.clippy.ClippyRulesDefinition;
import org.junit.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarRuntime;
import org.sonar.api.utils.Version;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RustPluginTest extends TestCase {
    @Test
    public void testGetExtensions() {
        Version v79 = Version.create(7, 9);
        SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(v79, SonarQubeSide.SERVER, SonarEdition.DEVELOPER);
        assertThat(extensions(runtime)).hasSize(10);
        assertThat(extensions(runtime)).contains(ClippyRulesDefinition.class);
        assertThat(extensions(SonarRuntimeImpl.forSonarLint(v79))).hasSize(10);
    }

    private static List extensions(SonarRuntime runtime) {
        Plugin.Context context = new Plugin.Context(runtime);
        new RustPlugin().define(context);
        return context.getExtensions();
    }
}
