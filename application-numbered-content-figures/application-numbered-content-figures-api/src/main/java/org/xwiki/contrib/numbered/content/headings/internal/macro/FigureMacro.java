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
package org.xwiki.contrib.numbered.content.headings.internal.macro;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Tag content as an illustration and with an optional caption. Override the figure macro from xwiki standard to match
 * the behavior of XWiki Platform 16.4RC1+. Once the parent version of the module is movided to 14.6RC1+, this class
 * must be removed.
 *
 * @version $Id: 6c6efe3799234cf92fa87eb16ff9471d83073be2 $
 * @since 1.4
 */
@Component
@Named("figure")
@Singleton
public class FigureMacro extends AbstractNoParameterMacro
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Tag content as an illustration and with an optional caption.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "Illustration(s) and caption";

    @Inject
    private MacroContentParser contentParser;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public FigureMacro()
    {
        super("Figure", DESCRIPTION,
            new DefaultContentDescriptor(CONTENT_DESCRIPTION, false, Block.LIST_BLOCK_TYPE));
        setDefaultCategory(DEFAULT_CATEGORY_DEVELOPMENT);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(Object unusedParameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        XDOM xdom = this.contentParser.parse(content, context, false, false);
        // Mark the macro content as being content that has not been transformed (so that it can be edited inline).
        List<Block> contentBlock = List.of(new MetaDataBlock(xdom.getChildren(), getNonGeneratedContentMetaData()));

        return List.of(new CompositeBlock(List.of(
            new MacroBlock("figureTypeRecognizer", Map.of(), false),
            new FigureBlock(contentBlock))
        ));
    }
}

