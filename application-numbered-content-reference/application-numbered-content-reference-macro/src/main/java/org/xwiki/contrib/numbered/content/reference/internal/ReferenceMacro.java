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
package org.xwiki.contrib.numbered.content.reference.internal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.headings.FiguresNumberingService;
import org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService;
import org.xwiki.contrib.numbered.content.reference.ReferenceMacroParameters;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.IdBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

/**
 * Create a link to a section id, displaying the section number as the link label.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("reference")
@Singleton
public class ReferenceMacro extends AbstractMacro<ReferenceMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION =
        "Create a link to a section id, displaying the section number as the link label.";

    /**
     * The key of the id parameter.
     *
     * @since 1.3
     */
    private static final String ID_PARAMETER_KEY = "id";

    @Inject
    private List<HeadingsNumberingService> headingsNumberingServices;

    @Inject
    private List<FiguresNumberingService> figuresNumberingServices;

    @Inject
    private ContextualLocalizationManager l10n;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public ReferenceMacro()
    {
        super("Reference", DESCRIPTION, ReferenceMacroParameters.class);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_NAVIGATION));
        // One more than the figure type recognizer macro to make sure that all the figure types are identified
        // before computing the references.
        setPriority(3001);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(ReferenceMacroParameters parameters, String content, MacroTransformationContext context)
    {
        DocumentResourceReference resourceReference = new DocumentResourceReference("");
        String id = parameters.getId();
        resourceReference.setAnchor(id);

        // Ask the numbering services to compute the numbers until on of the computes a block with the requested id.
        Block rootBlock = ((Block) context.getXDOM()).getRoot();

        // Chain the search, first in the headers, then in the figures.
        String number = headingNumber(id, rootBlock);
        if (number == null) {
            number = figuresNumber(id, rootBlock);
        }

        Block referenceContent;
        if (number != null) {
            referenceContent = new LinkBlock(singletonList(new WordBlock(number)), resourceReference, false);
        } else {
            String errorMessage =
                this.l10n.getTranslationPlain("numbered.content.reference.macro.error.referenceNotFound",
                    id);
            referenceContent = new MacroBlock("error", emptyMap(), errorMessage, true);
        }
        return singletonList(referenceContent);
    }

    private String headingNumber(String id, Block rootBlock)
    {
        String number = null;
        for (HeadingsNumberingService headingsNumberingService : this.headingsNumberingServices) {

            String headingBlockNumber =
                headingsNumberingService.getHeadingsMap(rootBlock).entrySet().stream()
                    .filter(it -> {
                        HeaderBlock headingBlock = it.getKey();
                        return Objects.equals(headingBlock.getId(), id)
                            || Objects.equals(headingBlock.getParameter(ID_PARAMETER_KEY), id)
                            || headingBlock.<IdBlock>getBlocks(new ClassBlockMatcher(IdBlock.class),
                            Block.Axes.DESCENDANT).stream().anyMatch(idb -> Objects.equals(idb.getName(), id));
                    }).findFirst().map(Map.Entry::getValue)
                    .orElse(null);
            if (headingBlockNumber != null) {
                number = headingBlockNumber;
                break;
            }
        }
        return number;
    }

    private String figuresNumber(String id, Block rootBlock)
    {
        String number = null;
        for (FiguresNumberingService figureNumberingService : this.figuresNumberingServices) {

            String figureBlockNumber =
                figureNumberingService.getFiguresMap(rootBlock).entrySet().stream()
                    .filter(it -> hasId(it.getKey(), id)).findFirst().map(Map.Entry::getValue)
                    .orElse(null);
            if (figureBlockNumber != null) {
                number = figureBlockNumber;
                break;
            }
        }
        return number;
    }

    private boolean hasId(FigureBlock figureBlock, String id)
    {
        return figureBlock.getFirstBlock(block -> {
            boolean result = false;

            String idParameter = block.getParameter(ID_PARAMETER_KEY);
            if (idParameter != null) {
                // Match the id parameter of any block.
                result = Objects.equals(idParameter, id);
            } else if (block instanceof IdBlock) {
                // Match the id of the id block.
                IdBlock idBlock = (IdBlock) block;
                result = Objects.equals(idBlock.getName(), id);
            } else if (block instanceof ImageBlock) {
                // Match the id of the image block.
                ImageBlock imageBlock = (ImageBlock) block;
                result = Objects.equals(imageBlock.getId(), id);
            }

            return result;
        }, Block.Axes.DESCENDANT_OR_SELF) != null;
    }
}
