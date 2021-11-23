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
package org.xwiki.contrib.numberedreferences.internal;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.FigureCaptionBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for {@link NumberedFiguresTransformation}, in addition to {@link IntegrationTests}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
@AllComponents
class NumberedFiguresTransformationTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @BeforeEach
    void setUp() throws Exception
    {
        this.componentManager.registerMockComponent(ContextualLocalizationManager.class);
    }

    @Test
    void transformIgnoresProtectedContent() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [code] [] []\n"
            + "beginMacroMarkerStandalone [figure] [] []\n"
            + "beginFigure\n"
            + "beginMacroMarkerStandalone [figureCaption] [] []\n"
            + "beginFigureCaption\n"
            + "onWord [caption]\n"
            + "endFigureCaption\n"
            + "endMacroMarkerStandalone [figureCaption] [] []\n"
            + "endFigure\n"
            + "endMacroMarkerStandalone [figure] [] []\n"
            + "endMacroMarkerStandalone [code] [] []\n"
            + "endDocument";

        List<Block> figureBlocks = blocks(createMacroMarkerBlock("figure",
            blocks(new FigureBlock(blocks(createMacroMarkerBlock("figureCaption",
                blocks(new FigureCaptionBlock(blocks(new WordBlock("caption"))))))))));
        XDOM xdom = new XDOM(blocks(createMacroMarkerBlock("code", figureBlocks)));
        this.componentManager.<Transformation>getInstance(Transformation.class, "numberedfigures")
            .transform(xdom, new TransformationContext());

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        renderer.render(xdom, printer);

        assertEquals(expected, printer.toString());
    }

    private List<Block> blocks(Block... blocks)
    {
        return Arrays.asList(blocks);
    }

    private MacroMarkerBlock createMacroMarkerBlock(String macroId, List<Block> blocks)
    {
        return new MacroMarkerBlock(macroId, emptyMap(), "", blocks, false);
    }
}
