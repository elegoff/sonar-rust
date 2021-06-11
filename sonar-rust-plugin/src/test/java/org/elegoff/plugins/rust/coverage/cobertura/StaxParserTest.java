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
package org.elegoff.plugins.rust.coverage.cobertura;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.stream.XMLStreamException;

import java.io.File;

public class StaxParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void should_fail_parsing_ig_file_does_not_exist() throws Exception {
        thrown.expect(XMLStreamException.class);
        StaxParser parser = new StaxParser(rootCursor -> {});
        parser.parse(new File("fake.xml"));
    }

    @Test
    public void test_XML_with_DTD() throws XMLStreamException {
        StaxParser parser = new StaxParser(getTestHandler());
        parser.parse(getClass().getClassLoader().getResourceAsStream("org/elegoff/plugins/rust/cobertura/dtd-test.xml"));
    }

    @Test
    public void test_XML_with_XSD() throws XMLStreamException {
        StaxParser parser = new StaxParser(getTestHandler());
        parser.parse(getClass().getClassLoader().getResourceAsStream("org/elegoff/plugins/rust/cobertura/xsd-test.xml"));
    }

    @Test
    public void test_XML_with_XSD_and_ampersand() throws XMLStreamException {
        StaxParser parser = new StaxParser(getTestHandler());
        parser.parse(getClass().getClassLoader().getResourceAsStream("org/elegoff/plugins/rust/cobertura/xsd-test-with-entity.xml"));
    }

    private static StaxParser.XmlStreamHandler getTestHandler() {
        return new StaxParser.XmlStreamHandler() {
            public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
                rootCursor.advance();
                while (rootCursor.getNext() != null) {
                    // do nothing intentionally
                }
            }
        };
    }

}