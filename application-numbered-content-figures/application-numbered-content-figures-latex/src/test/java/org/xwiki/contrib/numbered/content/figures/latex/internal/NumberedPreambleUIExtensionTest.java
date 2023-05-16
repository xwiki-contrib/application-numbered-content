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
package org.xwiki.contrib.numbered.content.figures.latex.internal;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.figure.FigureType;
import org.xwiki.contrib.figure.internal.FigureLabelService;
import org.xwiki.contrib.figure.internal.FigureTypesConfiguration;
import org.xwiki.contrib.latex.internal.LaTeXTool;
import org.xwiki.contrib.numbered.content.figures.internal.NumberedFiguresConfiguration;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.figure.FigureStyle.BLOCK;
import static org.xwiki.contrib.figure.FigureStyle.INLINE;
import static org.xwiki.contrib.figure.FigureType.AUTOMATIC;
import static org.xwiki.contrib.figure.FigureType.FIGURE;
import static org.xwiki.contrib.figure.FigureType.TABLE;

/**
 * Test of {@link NumberedPreambleUIExtension}.
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList(NumberedCounterService.class)
class NumberedPreambleUIExtensionTest
{
    @InjectMockComponents
    private NumberedPreambleUIExtension uix;

    @MockComponent
    private FigureTypesConfiguration figureTypesConfiguration;

    @MockComponent
    private NumberedFiguresConfiguration numberedFiguresConfiguration;

    @MockComponent
    private LaTeXTool latexTool;

    @MockComponent
    private FigureLabelService figureLabelService;

    @BeforeEach
    void setUp()
    {
        when(this.latexTool.escape(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(this.figureLabelService.getLabel(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void executeNoConfig()
    {
        assertEquals(new RawBlock("", null), this.uix.execute());
    }

    @Test
    void executeWithMinimalConfig()
    {
        when(this.figureTypesConfiguration.getFigureTypes()).thenReturn(Set.of(
            AUTOMATIC,
            FIGURE,
            TABLE
        ));
        assertEquals(new RawBlock("", null), this.uix.execute());
    }

    @Test
    void executeWithoutCounters()
    {
        when(this.figureTypesConfiguration.getFigureTypes()).thenReturn(Set.of(
            AUTOMATIC,
            FIGURE,
            TABLE,
            new FigureType("inlineA", INLINE),
            new FigureType("blockA", BLOCK)
        ));
        assertEquals(new RawBlock("\\newtheorem{inlineA}{inlineA}\n"
            + "\\DeclareFloatingEnvironment{blockA}", null), this.uix.execute());
    }

    @Test
    void executeWithCounters() throws Exception
    {
        FigureType inlineA1 = new FigureType("inlineA1", INLINE);
        FigureType inlineA2 = new FigureType("inlineA2", INLINE);
        FigureType blockA1 = new FigureType("blockA1", BLOCK);
        FigureType blockA2 = new FigureType("blockA2", BLOCK);
        when(this.figureTypesConfiguration.getFigureTypes()).thenReturn(Set.of(
            AUTOMATIC,
            FIGURE,
            TABLE,
            inlineA1,
            inlineA2,
            new FigureType("inlineB", INLINE),
            blockA1,
            blockA2,
            new FigureType("blockAC", BLOCK)
        ));

        when(this.numberedFiguresConfiguration.getFigureCounters()).thenReturn(Map.of(
            "inlineCptr", Set.of(inlineA1, inlineA2),
            "blockCptr", Set.of(blockA1, blockA2)
        ));
        assertEquals("\\newtheorem{inlineA1}{inlineA1}\n"
            + "\\newtheorem{inlineB}{inlineB}\n"
            + "\\newtheorem{inlineA2}[inlineA1]{inlineA2}\n"
            + "\\DeclareFloatingEnvironment{blockA1}\n"
            + "\\DeclareFloatingEnvironment{blockAC}", ((RawBlock) this.uix.execute()).getRawContent());
    }
}
