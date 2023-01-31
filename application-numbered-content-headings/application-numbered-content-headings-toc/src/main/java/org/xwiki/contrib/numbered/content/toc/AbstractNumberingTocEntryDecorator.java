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
package org.xwiki.contrib.numbered.content.toc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.xwiki.context.Execution;
import org.xwiki.contrib.numbered.content.headings.internal.HeadingNumberingCalculator;
import org.xwiki.contrib.numbered.content.headings.internal.HeadingsNumberingExecutionContextInitializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.WordBlock;

import static org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService.SKIP_PARAMETER;
import static org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService.START_PARAMETER;

/**
 * Abstract implementation of {@link TocEntryDecorator}, leaving to a concrete implementation to task of resolving if
 * the entry must be prefixed with a number (e.g., according to some configuration, or based on the parent macro of a
 * header).
 *
 * @version $Id$
 * @since 1.8
 */
public abstract class AbstractNumberingTocEntryDecorator implements TocEntryDecorator
{
    @Inject
    private Execution execution;

    @Override
    public List<Block> decorate(HeaderBlock headerBlock, List<Block> blocks, Block rootBlock,
        TocEntriesResolver tocEntriesResolver)
    {
        boolean isNumbered = isNumbered();
        if (isNumbered) {
            Map<HeaderBlock, String> headingBlockStringMap = getHeadingsMap(rootBlock, tocEntriesResolver);
            String rawContent = headingBlockStringMap.get(headerBlock);
            if (rawContent != null) {
                blocks.addAll(0, List.of(new WordBlock(rawContent), new SpaceBlock()));
            }
        }

        return cleanupEntryLabel(blocks);
    }

    private Map<HeaderBlock, String> getHeadingsMap(Block rootBlock, TocEntriesResolver tocEntriesResolver)
    {
        Map<HeaderBlock, String> result = new HashMap<>();

        HeadingNumberingCalculator helper =
            (HeadingNumberingCalculator) this.execution.getContext()
                .getProperty(HeadingsNumberingExecutionContextInitializer.PROPERTY_KEY);

        for (HeaderBlock heading : tocEntriesResolver.getHeaderBlocks(rootBlock)) {
            if (heading.getParameter(SKIP_PARAMETER) == null) {
                Integer start = null;
                if (heading.getParameter(START_PARAMETER) != null) {
                    start = Integer.parseInt(heading.getParameter(START_PARAMETER));
                }

                result.put(heading, helper.addHeading(heading, start));
            }
        }

        return result;
    }

    protected abstract boolean isNumbered();

    private List<Block> cleanupEntryLabel(List<Block> blocks)
    {
        // Remove all the trailing spaces and special symbols. For instance "Hello World !" becomes "Hello World"
        while (!blocks.isEmpty()) {
            Block block = blocks.get(blocks.size() - 1);
            if (block instanceof SpecialSymbolBlock || block instanceof SpaceBlock) {
                blocks.remove(blocks.size() - 1);
            } else {
                break;
            }
        }

        return blocks;
    }
}
