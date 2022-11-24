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
package org.xwiki.contrib.numbered.content.headings.internal;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.IdBlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL1;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL2;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL3;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL4;

/**
 * Unit test for {@link HeadingNumberingCalculator}.
 *
 * @version $Id$
 * @since 1.2
 */
class HeadingNumberingCalculatorTest
{
    HeadingNumberingCalculator calculator;

    @BeforeEach
    void initCalculator()
    {
        this.calculator = new HeadingNumberingCalculator();
    }

    @Test
    void consecutiveNumbers()
    {
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL1, Map.of("id", "H1")), null);
        assertEquals("1", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(new IdBlock("H2")), LEVEL1), null);
        assertEquals("2", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL2, "H21"), null);
        assertEquals("2.1", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL3, "H211"), null);
        assertEquals("2.1.1", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL1, "H3"), null);
        assertEquals("3", this.calculator.toString());
    }

    @Test
    void startNumber()
    {
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL1, "H42"), 42);
        assertEquals("42", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL1, "H43"), null);
        assertEquals("43", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL2, "H4313"), 13);
        assertEquals("43.13", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL1, "H10"), 10);
        assertEquals("10", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL2, "H10100"), 100);
        assertEquals("10.100", this.calculator.toString());
    }

    @Test
    void skipLevel()
    {
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL2, "H02"), 2);
        assertEquals("0.2", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL4, "H0201"), null);
        assertEquals("0.2.0.1", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL3, "H021"), null);
        assertEquals("0.2.1", this.calculator.toString());
        this.calculator.addHeading(new HeaderBlock(List.of(), LEVEL1, "H1"), null);
        assertEquals("1", this.calculator.toString());
    }
}
