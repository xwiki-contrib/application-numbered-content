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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListBLock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.internal.macro.toc.TocBlockFilter;
import org.xwiki.rendering.internal.macro.toc.TreeParameters;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.stability.Unstable;

/**
 * Generates a ToC Tree of {@link Block} from input parameters.
 *
 * @version $Id: 895750ff95339e6ee4316d1e267a188c14e5acb8 $
 * @since 1.0
 */
@Unstable
public class TocTreeBuilder
{
    private TocBlockFilter tocBlockFilter;

    private TocEntriesResolver tocEntriesResolver;

    private List<TocEntryDecorator> decorators;

    /**
     * Initialize a table of content tree builder.
     *
     * @param tocBlockFilter the filter to use to generate the toc anchors
     * @param tocEntriesResolver the resolver to use to find the entries in a given {@link Block}
     * @param decorators the decorators that will be called on each toc entry, allowing to add additional
     *     information on the toc entries
     */
    public TocTreeBuilder(TocBlockFilter tocBlockFilter, TocEntriesResolver tocEntriesResolver,
        List<TocEntryDecorator> decorators)
    {
        this.tocBlockFilter = tocBlockFilter;
        this.tocEntriesResolver = Objects.requireNonNullElseGet(tocEntriesResolver,
            () -> rootBlock -> rootBlock.getBlocks(new ClassBlockMatcher(HeaderBlock.class),
                Block.Axes.DESCENDANT));
        this.decorators = decorators;
    }

    /**
     * @param parameters the input parameters to generate the tree of {@link Block}s.
     * @return the list of {@link Block}s representing the TOC
     */
    public List<Block> build(TreeParameters parameters)
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

        // Get the list of sections in the scope
        List<HeaderBlock> headers = this.tocEntriesResolver.getHeaderBlocks(parameters.rootBlock);

        // If the root block is a section, remove its header block for the list of header blocks
        if (parameters.rootBlock instanceof SectionBlock) {
            Block block = parameters.rootBlock.getChildren().get(0);

            if (block instanceof HeaderBlock) {
                headers.remove(block);
            }
        }

        // Construct table of content from sections list
        Block tocBlock = generateTree(headers, parameters);
        if (tocBlock != null) {
            result = Arrays.asList(tocBlock);
        } else {
            result = Collections.emptyList();
        }

        return result;
    }

    /**
     * Convert headers into list block tree.
     *
     * @param headers the headers to convert.
     * @param parameters the tree parameters
     * @return the root block of generated block tree or null if no header was matching the specified parameters
     */
    private Block generateTree(List<HeaderBlock> headers, TreeParameters parameters)
    {
        Block tocBlock = null;

        int start = parameters.start;
        int depth = parameters.depth;
        boolean numbered = parameters.isNumbered;

        int currentLevel = start - 1;
        Block currentBlock = null;
        for (HeaderBlock headerBlock : headers) {
            int headerLevel = headerBlock.getLevel().getAsInt();

            if (headerLevel >= start && headerLevel <= depth) {
                // Move to next header in toc tree

                if (currentLevel < headerLevel) {
                    while (currentLevel < headerLevel) {
                        if (currentBlock instanceof ListBLock) {
                            currentBlock = addItemBlock(currentBlock, null, parameters);
                        }

                        currentBlock = createChildListBlock(numbered, currentBlock);
                        ++currentLevel;
                    }
                } else {
                    while (currentLevel > headerLevel) {
                        currentBlock = currentBlock.getParent().getParent();
                        --currentLevel;
                    }
                    currentBlock = currentBlock.getParent();
                }

                currentBlock = addItemBlock(currentBlock, headerBlock, parameters);
            }
        }

        if (currentBlock != null) {
            tocBlock = currentBlock.getRoot();

            // Add CSS class to ease styling.
            tocBlock.setParameter("class", "wikitoc");
        }

        return tocBlock;
    }

    /**
     * Add a {@link ListItemBlock} in the current toc tree block and return the new {@link ListItemBlock}.
     *
     * @param currentBlock the current block in the toc tree.
     * @param headerBlock the {@link HeaderBlock} to use to generate toc anchor label.
     * @param parameters the tree parameters
     * @return the new {@link ListItemBlock}.
     */
    private Block addItemBlock(Block currentBlock, HeaderBlock headerBlock, TreeParameters parameters)
    {
        ListItemBlock itemBlock = headerBlock == null ? createEmptyTocEntry() : createTocEntry(headerBlock, parameters);

        currentBlock.addChild(itemBlock);

        return itemBlock;
    }

    /**
     * @return a new empty list item.
     * @since 1.8RC2
     */
    private ListItemBlock createEmptyTocEntry()
    {
        return new ListItemBlock(Collections.emptyList());
    }

    /**
     * Create a new toc list item based on section title.
     *
     * @param headerBlock the {@link HeaderBlock}.
     * @param parameters the tree parameters
     * @return the new list item block.
     */
    protected ListItemBlock createTocEntry(HeaderBlock headerBlock, TreeParameters parameters)
    {
        // Create the link to target the header anchor
        DocumentResourceReference reference = new DocumentResourceReference(parameters.documentReference);
        String idParameter = headerBlock.getParameter("id");
        if (idParameter != null) {
            reference.setAnchor(idParameter);
        } else {
            reference.setAnchor(headerBlock.getId());
        }

        List<Block> blocks = this.tocBlockFilter.generateLabel(headerBlock);

        for (TocEntryDecorator decorator : this.decorators) {
            blocks = decorator.decorate(headerBlock, blocks, parameters.rootBlock, this.tocEntriesResolver);
        }
        LinkBlock linkBlock = new LinkBlock(blocks, reference, false);

        return new ListItemBlock(Collections.singletonList(linkBlock));
    }

    /**
     * Create a new ListBlock and add it in the provided parent block.
     *
     * @param numbered indicate if the list has to be numbered or with bullets
     * @param parentBlock the block where to add the new list block.
     * @return the new list block.
     */
    private ListBLock createChildListBlock(boolean numbered, Block parentBlock)
    {
        ListBLock childListBlock =
            numbered ? new NumberedListBlock(Collections.emptyList()) : new BulletedListBlock(Collections.emptyList());

        if (parentBlock != null) {
            parentBlock.addChild(childListBlock);
        }

        return childListBlock;
    }
}
