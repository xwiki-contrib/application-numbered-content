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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.figure.FigureTypeRecognizer;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * This macro is expected to be put as the previous sibling of a {@link FigureBlock}. When in this position, it will
 * insert a {@code data-xwiki-rendering-figure-type} property with its type in the {@link FigureBlock}. Override the
 * figureTypeRecognizer macro from xwiki standard to match * the behavior of XWiki Platform 16.4RC1+. * Once the parent
 * version of the module is movided to 14.6RC1+, this class must be removed.
 *
 * @version $Id: 32ee6c564337989596f674b0815ce875aed7c3f0 $
 * @since 1.4
 */
@Component
@Named("figureTypeRecognizer")
@Singleton
public class FigureTypeRecognizerMacro extends AbstractNoParameterMacro
{
    /**
     * Parameter name for the figure types.
     * @since 1.6
     */
    public static final String DATA_XWIKI_RENDERING_FIGURE_TYPE = "data-xwiki-rendering-figure-type";

    @Inject
    private FigureTypeRecognizer figureTypeRecognizer;

    /**
     * Default constructor.
     */
    public FigureTypeRecognizerMacro()
    {
        super("Figure Type Recognizer", "Internal macro used to recognize the type of a figure.");
        setDefaultCategory(DEFAULT_CATEGORY_INTERNAL);
        // High priority to make sure this macro is executed after all the content of the figure macro is executed.
        setPriority(3000);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(Object parameters, String content, MacroTransformationContext context)
    {
        Block nextSibling = context.getCurrentMacroBlock().getNextSibling();
        if (nextSibling instanceof FigureBlock && nextSibling.getParameter(DATA_XWIKI_RENDERING_FIGURE_TYPE) == null) {
            String type = this.figureTypeRecognizer.isTable((FigureBlock) nextSibling) ? "table" : "figure";
            nextSibling.setParameter(DATA_XWIKI_RENDERING_FIGURE_TYPE, type);
        }
        return List.of();
    }
}

