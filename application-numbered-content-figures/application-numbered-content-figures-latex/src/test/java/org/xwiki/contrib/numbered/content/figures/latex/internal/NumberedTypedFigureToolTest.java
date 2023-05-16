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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.contrib.figure.FigureType;
import org.xwiki.contrib.figure.internal.DefaultTypedFigureTool;
import org.xwiki.contrib.figure.internal.FigureTypesConfiguration;
import org.xwiki.contrib.numbered.content.figures.internal.NumberedFiguresConfiguration;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.figure.FigureStyle.BLOCK;
import static org.xwiki.contrib.figure.FigureStyle.INLINE;
import static org.xwiki.contrib.figure.FigureType.AUTOMATIC;
import static org.xwiki.contrib.figure.FigureType.FIGURE;
import static org.xwiki.contrib.figure.FigureType.TABLE;
import static org.xwiki.contrib.figure.internal.FigureTypeRecognizerMacro.DATA_XWIKI_RENDERING_FIGURE_TYPE;

/**
 * Test of {@link NumberedTypedFigureTool}.
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList({
    DefaultTypedFigureTool.class,
    NumberedCounterService.class
})
class NumberedTypedFigureToolTest
{
    @InjectMockComponents
    private NumberedTypedFigureTool numberedTypedFigureTool;

    @MockComponent
    private FigureTypesConfiguration figureTypesConfiguration;

    @MockComponent
    private NumberedFiguresConfiguration numberedFiguresConfiguration;

    @ParameterizedTest
    @CsvSource({
        "figure,figure",
        "table,table",
        "unknown,figure",
    })
    void getFigureEnvironmentNoConfig(String typeId, String expected)
    {
        assertEquals(expected, this.numberedTypedFigureTool.getFigureEnvironment(
            new FigureBlock(List.of(), Map.of(DATA_XWIKI_RENDERING_FIGURE_TYPE, typeId))));
    }

    @ParameterizedTest
    @CsvSource({
        "figure,figure",
        "table,table",
        "inlineA,inlineA",
        "blockA,blockA"
    })
    void getFigureEnvironmentWithoutCounters(String typeId, String expected)
    {
        FigureType inlineA = new FigureType("inlineA", INLINE);
        FigureType blockA = new FigureType("blockA", BLOCK);

        when(this.figureTypesConfiguration.getFigureType("inlineA")).thenReturn(Optional.of(inlineA));
        when(this.figureTypesConfiguration.getFigureType("blockA")).thenReturn(Optional.of(blockA));
        assertEquals(expected, this.numberedTypedFigureTool.getFigureEnvironment(
            new FigureBlock(List.of(), Map.of(DATA_XWIKI_RENDERING_FIGURE_TYPE, typeId))));
    }

    @ParameterizedTest
    @CsvSource({
        "figure,figure",
        "table,table",
        "inlineA1,inlineA1",
        "inlineA2,inlineA2",
        "blockA1,blockA1",
        "blockA2,blockA1",
        "inlineB,inlineB",
        "blockAC,blockAC"
    })
    void getFigureEnvironmentWithCounters(String typeId, String expected) throws Exception
    {
        FigureType inlineA1 = new FigureType("inlineA1", INLINE);
        FigureType inlineA2 = new FigureType("inlineA2", INLINE);
        FigureType blockA1 = new FigureType("blockA1", BLOCK);
        FigureType blockA2 = new FigureType("blockA2", BLOCK);
        FigureType inlineB = new FigureType("inlineB", INLINE);
        FigureType blockAC = new FigureType("blockAC", BLOCK);

        when(this.figureTypesConfiguration.getFigureTypes()).thenReturn(Set.of(
            AUTOMATIC,
            FIGURE,
            TABLE,
            inlineA1,
            inlineA2,
            inlineB,
            blockA1,
            blockA2,
            blockAC
        ));

        when(this.figureTypesConfiguration.getFigureType("inlineA1")).thenReturn(Optional.of(inlineA1));
        when(this.figureTypesConfiguration.getFigureType("inlineA2")).thenReturn(Optional.of(inlineA2));
        when(this.figureTypesConfiguration.getFigureType("blockA1")).thenReturn(Optional.of(blockA1));
        when(this.figureTypesConfiguration.getFigureType("blockA2")).thenReturn(Optional.of(blockA2));
        when(this.figureTypesConfiguration.getFigureType("inlineB")).thenReturn(Optional.of(inlineB));
        when(this.figureTypesConfiguration.getFigureType("blockAC")).thenReturn(Optional.of(blockAC));

        when(this.numberedFiguresConfiguration.getFigureCounters()).thenReturn(Map.of(
            "inlineCptr", Set.of(inlineA1, inlineA2),
            "blockCptr", Set.of(blockA1, blockA2)
        ));
        assertEquals(expected, this.numberedTypedFigureTool.getFigureEnvironment(
            new FigureBlock(List.of(), Map.of(DATA_XWIKI_RENDERING_FIGURE_TYPE, typeId))));
    }
}
