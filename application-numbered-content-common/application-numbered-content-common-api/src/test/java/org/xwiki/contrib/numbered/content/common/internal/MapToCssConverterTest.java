/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.numbered.content.common.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link MapToCssConverter}.
 *
 * @version $Id$
 */
@ComponentTest
class MapToCssConverterTest
{
    @InjectMockComponents
    private MapToCssConverter converter;

    @Test
    void convert()
    {
        assertEquals("", this.converter.convert(Map.of()));
    }

    @Test
    void convertSingleSelectorNoRule()
    {
        assertEquals("a {}\n", this.converter.convert(Map.of("", Map.of("a", Map.of()))));
    }

    @Test
    void convertSingleSelectorNoRuleWithMediaQuery()
    {
        assertEquals("selector{\na {}\n}\n", this.converter.convert(Map.of("selector", Map.of("a", Map.of()))));
    }

    @Test
    void convertSingleSelectorSingleRule()
    {
        assertEquals("a {counter-reset: c1;\n"
            + "}\n", this.converter.convert(Map.of("", Map.of("a", Map.of("counter-reset", "c1")))));
    }

    @Test
    void convertSingleSelectorTwoRules()
    {
        Map<String, String> rules = new LinkedHashMap<>();
        rules.put("counter-reset", "c1");
        rules.put("color", "blue");
        assertEquals("a {counter-reset: c1;\n"
            + "color: blue;\n"
            + "}\n", this.converter.convert(Map.of("", Map.of("a", rules))));
    }

    @Test
    void convertTwoSelectorsTwoRules()
    {
        Map<String, String> rulesA = new LinkedHashMap<>();
        rulesA.put("counter-reset", "c1");
        rulesA.put("color", "blue");
        Map<String, String> rulesB = new LinkedHashMap<>();
        rulesB.put("counter-reset", "c1");
        rulesB.put("color", "blue");
        Map<String, Map<String, String>> selectors = new LinkedHashMap<>();
        selectors.put("a", rulesA);
        selectors.put("b", rulesB);
        assertEquals("a {counter-reset: c1;\n"
            + "color: blue;\n"
            + "}\n"
            + "b {counter-reset: c1;\n"
            + "color: blue;\n"
            + "}\n", this.converter.convert(Map.of("", selectors)));
    }
}
