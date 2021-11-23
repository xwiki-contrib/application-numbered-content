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
package org.xwiki.contrib.numbered.headings.internal;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link NumberedHeadingsTransformation}, in addition to {@link IntegrationTests}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
@AllComponents(excludes = NumberedHeadingsService.class)
class NumberedHeadingsTransformationTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    private NumberedHeadingsService numberedHeadingsService;

    private Transformation numberedHeadingsTransformation;

    private BlockRenderer renderedEvent10;

    private Parser parserXWiki21;

    private BlockRenderer rendererXWiki21;

    @BeforeEach
    void setUp() throws Exception
    {
        this.numberedHeadingsTransformation =
            this.componentManager.getInstance(Transformation.class, "numberedheadings");
        this.renderedEvent10 = this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        this.rendererXWiki21 = this.componentManager.getInstance(BlockRenderer.class, Syntax.XWIKI_2_1.toIdString());
        this.parserXWiki21 = this.componentManager.getInstance(Parser.class, "xwiki/2.1");
        this.componentManager.registerMockComponent(ContextualLocalizationManager.class);
        when(this.numberedHeadingsService.isCurrentDocumentNumbered()).thenReturn(true);
    }

    @Test
    void transformIgnoresProtectedContentWithCodeMacro() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [code] []\n"
            + "beginSection\n"
            + "beginHeader [1, null]\n"
            + "onWord [heading]\n"
            + "endHeader [1, null]\n"
            + "endSection\n"
            + "endMacroMarkerStandalone [code] []\n"
            + "endDocument";

        List<Block> sectionBlock = singletonList(new SectionBlock(
            singletonList(new HeaderBlock(singletonList(new WordBlock("heading")), HeaderLevel.LEVEL1))));
        XDOM xdom = new XDOM(singletonList((Block) new MacroMarkerBlock("code", emptyMap(), sectionBlock, false)));
        this.numberedHeadingsTransformation.transform(xdom, new TransformationContext());

        WikiPrinter printer = new DefaultWikiPrinter();

        this.renderedEvent10.render(xdom, printer);

        assertEquals(expected, printer.toString());
    }

    @Test
    void transformIgnoresProtectedContentWithProtectedParameter() throws Exception
    {
        String expected = "= (% class=\"wikigeneratedheadingnumber\" %)1 (%%)heading A =\n\n"
            + "(% data-xwiki-rendering-protected=\"true\" %)\n"
            + "== heading B ==\n\n"
            + "(% data-xwiki-rendering-protected=\"true\" %)\n"
            + "(((\n"
            + "== heading C ==\n"
            + ")))\n\n"
            + "= (% class=\"wikigeneratedheadingnumber\" %)2 (%%)heading E =";

        String content = "= heading A =\n\n"
            + "(% data-xwiki-rendering-protected = 'true' %)\n"
            + "== heading B ==\n\n"
            + "(% data-xwiki-rendering-protected = 'true' %)(((\n"
            + "== heading C ==\n\n"
            + ")))\n\n"
            + "= heading E =\n";

        XDOM xdom = this.parserXWiki21.parse(new StringReader(content));

        this.numberedHeadingsTransformation.transform(xdom, new TransformationContext());

        WikiPrinter printer = new DefaultWikiPrinter();
        this.rendererXWiki21.render(xdom, printer);

        assertEquals(expected, printer.toString());
    }
}
