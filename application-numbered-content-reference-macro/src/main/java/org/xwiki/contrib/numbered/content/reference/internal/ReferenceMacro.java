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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.FiguresNumberingService;
import org.xwiki.contrib.numbered.content.HeaderNumberingService;
import org.xwiki.contrib.numbered.content.reference.ReferenceMacroParameters;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.IdBlock;
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

    @Inject
    private List<HeaderNumberingService> headerNumberingServices;

    @Inject
    private List<FiguresNumberingService> figuresNumberingServices;

    @Inject
    private ContextualLocalizationManager l10n;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public ReferenceMacro()
    {
        super("Id", DESCRIPTION, ReferenceMacroParameters.class);
        setDefaultCategory(DEFAULT_CATEGORY_NAVIGATION);
        setPriority(3000);
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
        String number = headerNumber(id, rootBlock);
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

    private String headerNumber(String id, Block rootBlock)
    {
        String number = null;
        for (HeaderNumberingService headerNumberingService : this.headerNumberingServices) {

            String headerBlockNumber =
                headerNumberingService.getHeadersMap(rootBlock).entrySet().stream()
                    .filter(it -> it.getKey().getId().equals(id)).findFirst().map(Map.Entry::getValue)
                    .orElse(null);
            if (headerBlockNumber != null) {
                number = headerBlockNumber;
                break;
            }
        }
        return number;
    }

    private String figuresNumber(String id, Block rootBlock)
    {
        String number = null;
        for (FiguresNumberingService figureNumberingService : this.figuresNumberingServices) {

            String figureBlockNumbre =
                figureNumberingService.getFiguresMap(rootBlock).entrySet().stream()
                    .filter(it -> hasId(it.getKey(), id)).findFirst().map(Map.Entry::getValue)
                    .orElse(null);
            if (figureBlockNumbre != null) {
                number = figureBlockNumbre;
                break;
            }
        }
        return number;
    }

    private boolean hasId(FigureBlock figureBlock, String id)
    {
        return figureBlock.<IdBlock>getBlocks(new ClassBlockMatcher(IdBlock.class), Block.Axes.DESCENDANT).stream()
            .anyMatch(it -> Objects.equals(it.getName(), id));
    }
}
