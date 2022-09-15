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

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.xwiki.contrib.numbered.content.headings.internal.macro.FigureTypeRecognizerMacro.DATA_XWIKI_RENDERING_FIGURE_TYPE;

/**
 * Test of {@link DefaultFiguresNumberingService}.
 *
 * @version $Id$
 * @since 1.6
 */
@ComponentTest
class DefaultFiguresNumberingServiceTest
{
    @InjectMockComponents
    private DefaultFiguresNumberingService figuresNumberingService;

    @Test
    void getFiguresList()
    {
        assertEquals(List.of(), this.figuresNumberingService.getFiguresList(new GroupBlock()));
        FigureBlock figureBlock = new FigureBlock(List.of());
        assertEquals(List.of(figureBlock),
            this.figuresNumberingService.getFiguresList(new GroupBlock(List.of(figureBlock))));
    }

    @Test
    void getFiguresMap()
    {
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
            figure0, "1",
            figureImage0, "1",
            figure1, "2"
        ), figuresMap);
    }
}
