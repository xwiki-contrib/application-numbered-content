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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.CaseUtils;
import org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListBLock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.internal.macro.toc.TocBlockFilter;
import org.xwiki.rendering.internal.macro.toc.TreeParameters;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.stability.Unstable;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Generates a ToC Tree of {@link Block} from input parameters.
 *
 * @version $Id: 895750ff95339e6ee4316d1e267a188c14e5acb8 $
 * @since 1.0
 */
@Unstable
public class TocTreeBuilder
{
    private static final String ID_PARAMETER_NAME = "id";

    private final TocBlockFilter tocBlockFilter;

    private final HeadingsNumberingService headingsNumberingService;

    /**
     * @param tocBlockFilter the filter to use to generate TOC anchors
     * @param headingsNumberingService the numbering service to use to generate TOC anchors
     */
    public TocTreeBuilder(TocBlockFilter tocBlockFilter, HeadingsNumberingService headingsNumberingService)
    {
        this.tocBlockFilter = tocBlockFilter;
        this.headingsNumberingService = headingsNumberingService;
    }

    /**
     * Build a Table of content from the parameters.
     *
     * @param parameters the input parameters to generate the tree of {@link Block}s
     * @param headingsNumbered if {@code true} the numbers of the headings will be displayed
     * @return the list of {@link Block}s representing the TOC
     */
    public List<Block> build(TreeParameters parameters, boolean headingsNumbered)
    {
        List<Block> result;

        // Example:
        // 1 Section1
        // 1 Section2
        // 1.1 Section3
        // 1 Section4
        // 1.1.1 Section5

        // Generates:
        // ListBlock
        // |_ ListItemBlock (TextBlock: Section1)
        // |_ ListItemBlock (TextBlock: Section2)
        // ...|_ ListBlock
        // ......|_ ListItemBlock (TextBlock: Section3)
        // |_ ListItemBlock (TextBlock: Section4)
        // ...|_ ListBlock
        // ......|_ ListBlock
        // .........|_ ListItemBlock (TextBlock: Section5)

        // Get the list of headers at the root level.
        List<HeaderBlock> headings = this.headingsNumberingService.getHeadingsList(parameters.rootBlock);

        // Construct table of content from sections list
        Block tocBlock = generateTree(headings, parameters.start, parameters.depth, parameters.documentReference,
            parameters.isNumbered, headingsNumbered, parameters.rootBlock);
        if (tocBlock != null) {
            result = singletonList(tocBlock);
        } else {
            result = emptyList();
        }

        return result;
    }

    /**
     * Convert headers into list block tree.
     *
     * @param headings the headers to convert.
     * @param start the "start" parameter value.
     * @param depth the "depth" parameter value.
     * @param numbered when {@code true} the {@link BulletedListBlock} use to display the ToC items are replaced by
     *     {@link NumberedListBlock}
     * @param headingsNumbered when {@code true} the numbers added on prefix of the headings are also displayed in
     *     the ToC before the headings content
     * @param rootBlock the root block of the current numbered headings
     * @return the root block of generated block tree or null if no header was matching the specified parameters
     */
    private Block generateTree(List<HeaderBlock> headings, int start, int depth, String documentReference,
        boolean numbered, boolean headingsNumbered, Block rootBlock)
    {
        Block tocBlock = null;

        int currentLevel = start - 1;
        Block currentBlock = null;
        for (HeaderBlock headingBlock : headings) {
            int headingLevel = headingBlock.getLevel().getAsInt();

            if (headingLevel >= start && headingLevel <= depth) {
                // Move to next header in toc tree

                if (currentLevel < headingLevel) {
                    while (currentLevel < headingLevel) {
                        if (currentBlock instanceof ListBLock) {
                            currentBlock = addItemBlock(currentBlock, null, documentReference, headingsNumbered,
                                rootBlock);
                        }

                        currentBlock = createChildListBlock(currentBlock, numbered);
                        ++currentLevel;
                    }
                } else {
                    while (currentLevel > headingLevel) {
                        currentBlock = currentBlock.getParent().getParent();
                        --currentLevel;
                    }
                    currentBlock = currentBlock.getParent();
                }

                currentBlock = addItemBlock(currentBlock, headingBlock, documentReference, headingsNumbered, rootBlock);
            }
        }

        if (currentBlock != null) {
            tocBlock = currentBlock.getRoot();
        }

        /** Add CSS class to ease styling. */
        tocBlock.setParameter("class", "wikitoc");

        return tocBlock;
    }

    /**
     * Add a {@link ListItemBlock} in the current toc tree block and return the new {@link ListItemBlock}.
     *
     * @param currentBlock the current block in the toc tree.
     * @param headingBlock the {@link HeaderBlock} to use to generate toc anchor label.
     * @param rootBlock the root block of the currently numbered headings
     * @return the new {@link ListItemBlock}.
     */
    private Block addItemBlock(Block currentBlock, HeaderBlock headingBlock, String documentReference,
        boolean headingsNumbered, Block rootBlock)
    {
        ListItemBlock itemBlock = headingBlock == null
            ? createEmptyTocEntry()
            : createTocEntry(headingBlock, documentReference, headingsNumbered, rootBlock);

        currentBlock.addChild(itemBlock);

        return itemBlock;
    }

    /**
     * @return a new empty list item.
     * @since 1.8RC2
     */
    private ListItemBlock createEmptyTocEntry()
    {
        return new ListItemBlock(emptyList());
    }

    /**
     * Create a new toc list item based on section title.
     *
     * @param headingBlock the {@link HeaderBlock}.
     * @param rootBlock the root block of the currently numbered headings
     * @return the new list item block.
     */
    private ListItemBlock createTocEntry(HeaderBlock headingBlock, String documentReference, boolean headingsNumbered,
        Block rootBlock)
    {
        // Create the link to target the header anchor
        DocumentResourceReference reference = new DocumentResourceReference(documentReference);
        String id;
        if (headingBlock.getParameter(ID_PARAMETER_NAME) != null) {
            id = headingBlock.getParameter(ID_PARAMETER_NAME);
        } else {
            id = headingBlock.getId();
        }
        reference.setAnchor(id);

        List<Block> blocks = new ArrayList<>();
        if (headingsNumbered) {
            Map<HeaderBlock, String> headingBlockStringMap = this.headingsNumberingService.getHeadingsMap(rootBlock);
            String rawContent = headingBlockStringMap.get(headingBlock);
            if (rawContent != null) {
                blocks.add(new WordBlock(rawContent));
                blocks.add(new SpaceBlock());
            }
        }
        blocks.addAll(cleanupEntryLabel(headingBlock));
        LinkBlock linkBlock = new LinkBlock(blocks, reference, false);

        return new ListItemBlock(singletonList(linkBlock));
    }

    private List<Block> cleanupEntryLabel(HeaderBlock headingBlock)
    {
        List<Block> blocks = this.tocBlockFilter.generateLabel(headingBlock);

        // Remove all the trailing spaces and special symbols. For instance "Hello World !" becomes "Hello World"
        while (!blocks.isEmpty()) {
            Block block = blocks.get(blocks.size() - 1);
            if (block instanceof SpecialSymbolBlock || block instanceof SpaceBlock) {
                blocks.remove(blocks.size() - 1);
            } else {
                break;
            }
        }

        // Normalize the title by replacing each word with a normalized form where the first letter is upper case and 
        // the rest lower case. 
        return blocks.stream().map(block -> {
            Block newBlock;
            if (block instanceof WordBlock) {
                WordBlock wordBlock = (WordBlock) block;
                newBlock = new WordBlock(CaseUtils.toCamelCase(wordBlock.getWord(), true));
            } else {
                newBlock = block;
            }
            return newBlock;
        }).collect(Collectors.toList());
    }

    /**
     * Create a new ListBlock and add it in the provided parent block.
     *
     * @param parentBlock the block where to add the new list block.
     * @return the new list block.
     */
    private ListBLock createChildListBlock(Block parentBlock, boolean numbered)
    {
        ListBLock childListBlock =
            numbered ? new NumberedListBlock(Collections.emptyList()) : new BulletedListBlock(Collections.emptyList());

        if (parentBlock != null) {
            parentBlock.addChild(childListBlock);
        }

        return childListBlock;
    }
}
