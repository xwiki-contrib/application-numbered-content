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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.numbered.content.figures.internal.DefaultFiguresNumberingService;
import org.xwiki.contrib.numbered.content.figures.internal.NumberedFiguresConfiguration;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.IdBlock;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.figure.internal.FigureTypeRecognizerMacro.DATA_XWIKI_RENDERING_FIGURE_TYPE;
import static org.xwiki.contrib.numbered.content.figures.internal.FigureNumberingExecutionContextInitializer.PROPERTY_KEY;

/**
 * Test of {@link DefaultFiguresNumberingService}.
 *
 * @version $Id$
 * @since 1.6
 */
@ComponentTest
class DefaultFiguresNumberingServiceTest
{
    private static final String COUNTERS = "counters";

    @InjectMockComponents
    private DefaultFiguresNumberingService figuresNumberingService;

    @MockComponent
    private Execution execution;

    @MockComponent
    private NumberedFiguresConfiguration numberedFiguresConfiguration;

    @Mock
    private ExecutionContext context;

    @BeforeEach
    void setUp() throws Exception
    {
        when(this.execution.getContext()).thenReturn(this.context);
        when(this.context.getProperty(PROPERTY_KEY)).thenReturn(new HashMap<>());
        when(this.numberedFiguresConfiguration.getCounter(anyString()))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void getFiguresList()
    {
        assertEquals(List.of(), this.figuresNumberingService.getFiguresList(new GroupBlock()));
        FigureBlock figureBlock = new FigureBlock(List.of());
        assertEquals(List.of(figureBlock),
            this.figuresNumberingService.getFiguresList(new GroupBlock(List.of(figureBlock))));
    }

    @Test
    void getFiguresMap() throws Exception
    {
        Map<Object, Object> counters = new HashMap<>();
        when(this.context.getProperty(PROPERTY_KEY)).thenReturn(counters);
        FigureBlock figure0 = new FigureBlock(List.of(), Map.of(
            "id", "f0",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "figure"
        ));
        FigureBlock figureImage0 = new FigureBlock(List.of(), Map.of(
            "id", "f1",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "table"
        ));
        FigureBlock figure1 = new FigureBlock(List.of(), Map.of(
            "id", "f2",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "figure"
        ));
        Map<FigureBlock, String> figuresMap = this.figuresNumberingService.getFiguresMap(new GroupBlock(List.of(
            figure0,
            figureImage0,
            figure1
        )));
        Map<FigureBlock, String> expectedResults = Map.of(figure0, "1", figureImage0, "1", figure1, "2");
        assertEquals(expectedResults, figuresMap);
        assertEquals(Map.of(
            COUNTERS, Map.of("figure", 3L, "table", 2L),
            "results", expectedResults
        ), counters);
    }

    @Test
    void getFiguresMapSharedCounter() throws Exception
    {
        Map<Object, Object> counters = new HashMap<>();
        when(this.context.getProperty(PROPERTY_KEY)).thenReturn(counters);
        when(this.numberedFiguresConfiguration.getCounter("figurebis")).thenReturn("figure");

        FigureBlock figure0 = new FigureBlock(List.of(), Map.of(
            "id", "f0",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "figure"
        ));
        FigureBlock figureImage0 = new FigureBlock(List.of(), Map.of(
            "id", "f1",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "table"
        ));
        FigureBlock figure1 = new FigureBlock(List.of(), Map.of(
            "id", "f2",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "figurebis"
        ));
        Map<FigureBlock, String> figuresMap = this.figuresNumberingService.getFiguresMap(new GroupBlock(List.of(
            figure0,
            figureImage0,
            figure1
        )));
        Map<FigureBlock, String> expectedResults = Map.of(figure0, "1", figureImage0, "1", figure1, "2");
        assertEquals(expectedResults, figuresMap);
        assertEquals(Map.of(
            COUNTERS, Map.of("figure", 3L, "table", 2L),
            "results", expectedResults
        ), counters);
    }

    @Test
    void getFiguresMapMissingType() throws Exception
    {
        Map<Object, Object> counters = new HashMap<>();
        when(this.context.getProperty(PROPERTY_KEY)).thenReturn(counters);
        FigureBlock figure0 = new FigureBlock(List.of(), Map.of(
            "id", "f0",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "figure"
        ));
        FigureBlock figureImage0 = new FigureBlock(List.of(), Map.of(
            "id", "f1",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "table"
        ));
        FigureBlock figure1MissingType = new FigureBlock(List.of(new IdBlock("f2")));
        Map<FigureBlock, String> figuresMap = this.figuresNumberingService.getFiguresMap(new GroupBlock(List.of(
            figure0,
            figureImage0,
            figure1MissingType
        )));
        Map<FigureBlock, String> expectedResults = Map.of(figure0, "1", figureImage0, "1", figure1MissingType, "2");
        assertEquals(expectedResults, figuresMap);
        assertEquals(Map.of(
                COUNTERS, Map.of("figure", 3L, "table", 2L),
                "results", expectedResults),
            counters);
    }

    @Test
    void getFiguresMapWithExistingCounter() throws Exception
    {
        Map<Object, Object> counters = new HashMap<>();
        counters.put("figure", 10L);
        counters.put("table", 15L);
        Map<String, Map<Object, Object>> figureNumbering = new HashMap<>();
        figureNumbering.put(COUNTERS, counters);
        when(this.context.getProperty(PROPERTY_KEY)).thenReturn(figureNumbering);
        FigureBlock figure0 = new FigureBlock(List.of(), Map.of(
            "id", "f0",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "figure"
        ));
        FigureBlock figureImage0 = new FigureBlock(List.of(), Map.of(
            "id", "f1",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "table"
        ));
        FigureBlock figure1 = new FigureBlock(List.of(), Map.of(
            "id", "f2",
            DATA_XWIKI_RENDERING_FIGURE_TYPE, "figure"
        ));
        Map<FigureBlock, String> figuresMap = this.figuresNumberingService.getFiguresMap(new GroupBlock(List.of(
            figure0,
            figureImage0,
            figure1
        )));
        assertEquals(Map.of(
            figure0, "10",
            figureImage0, "15",
            figure1, "11"
        ), figuresMap);
        assertEquals(Map.of("figure", 12L, "table", 16L), counters);
    }
}
