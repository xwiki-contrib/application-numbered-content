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
package org.xwiki.contrib.numbered.content.toc.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListBLock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.internal.macro.toc.TocBlockFilter;
import org.xwiki.rendering.internal.macro.toc.TreeParameters;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.syntax.Syntax;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Generates a ToC Tree of {@link Block} from input input parameters.
 *
 * @version $Id: 895750ff95339e6ee4316d1e267a188c14e5acb8 $
 * @since 9.6RC1
 */
public class TocTreeBuilder
{
    private static final String START_PARAMETER = "start";

    private final TocBlockFilter tocBlockFilter;

    private final Map<Block, Map<HeaderBlock, String>> cache = new WeakHashMap<>();

    /**
     * @param tocBlockFilter the filter to use to generate TOC anchors
     */
    public TocTreeBuilder(TocBlockFilter tocBlockFilter)
    {
        this.tocBlockFilter = tocBlockFilter;
    }

    /**
     * Build a Table of content from the parameters.
     *
     * @param parameters the input parameters to generate the tree of {@link Block}s
     * @param headingsNumbered if {@code true} the numbers of the headings will be displayed
     * @param headersSupplier TODO
     * @return the list of {@link Block}s representing the TOC
     */
    public List<Block> build(TreeParameters parameters, boolean headingsNumbered,
        Supplier<List<HeaderBlock>> headersSupplier)
    {
        // TODO: edit to keep build a cache, set non-duplicated IDs, insert numbers in prefix of the headings and
        // normalize the names.

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
        List<HeaderBlock> headers;
        if (this.cache.containsKey(parameters.rootBlock)) {
            headers = new ArrayList<>(this.cache.get(parameters.rootBlock).keySet());
        } else {
            headers = headersSupplier.get();
            if (headingsNumbered) {
                buildCache(parameters, headers);
            }
        }

        // Construct table of content from sections list
        Block tocBlock = generateTree(headers, parameters.start, parameters.depth, parameters.documentReference,
            parameters.isNumbered, headingsNumbered, parameters.rootBlock);
        if (tocBlock != null) {
            result = singletonList(tocBlock);
        } else {
            result = emptyList();
        }

        return result;
    }

    private void buildCache(TreeParameters parameters, List<HeaderBlock> headers)
    {
        // If the root block is a section, remove its header block for the list of header blocks
        // TODO: see if still relevant.
        if (parameters.rootBlock instanceof SectionBlock) {
            Block block = parameters.rootBlock.getChildren().get(0);

            if (block instanceof HeaderBlock) {
                headers.remove(block);
            }
        }

        Map<HeaderBlock, String> rootBlockCache = new HashMap<>();
        this.cache.put(parameters.rootBlock, rootBlockCache);

        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        for (HeaderBlock header : headers) {
            cacheHeader(rootBlockCache, stack, header);
        }
    }

    private void cacheHeader(Map<HeaderBlock, String> rootBlockCache, Deque<Integer> stack, HeaderBlock header)
    {
        int level = header.getLevel().getAsInt();
        Integer start = null;
        if (header.getParameter(START_PARAMETER) != null) {
            start = Integer.parseInt(header.getParameter(START_PARAMETER));
        }
        if (level == stack.size()) {
            cacheHeaderSameLevel(rootBlockCache, stack, header, start);
        } else if (level > stack.size()) {
            cacheHeaderLevelIncreses(rootBlockCache, stack, header, level, start);
        } else {
            cacheHeaderLevelDecreases(rootBlockCache, stack, header, level, start);
        }
    }

    private void cacheHeaderLevelDecreases(Map<HeaderBlock, String> rootBlockCache, Deque<Integer> stack,
        HeaderBlock header, int level, Integer start)
    {
        while (stack.size() > level) {
            stack.pop();
        }
        Integer pop = stack.pop();
        if (start != null) {
            stack.push(start);
        } else {
            stack.push(pop + 1);
        }
        rootBlockCache.put(header, serialize(stack));
    }

    private void cacheHeaderLevelIncreses(Map<HeaderBlock, String> rootBlockCache, Deque<Integer> stack,
        HeaderBlock header, int level, Integer start)
    {
        for (int i = stack.size(); i < level; i++) {
            if (i != level - 1) {
                stack.push(0);
            } else {
                if (start != null) {
                    stack.push(start);
                } else {
                    stack.push(1);
                }
            }
        }
        rootBlockCache.put(header, serialize(stack));
    }

    private void cacheHeaderSameLevel(Map<HeaderBlock, String> rootBlockCache, Deque<Integer> stack, HeaderBlock header,
        Integer start)
    {
        Integer pop = stack.pop();
        if (start != null) {
            stack.push(start);
        } else {
            stack.push(pop + 1);
        }
        rootBlockCache.put(header, serialize(stack));
    }

    private String serialize(Deque<Integer> stack)
    {
        List<String> ret = new ArrayList<>();
        Iterator<Integer> integerIterator = stack.descendingIterator();
        while (integerIterator.hasNext()) {
            ret.add(integerIterator.next().toString());
        }
        return StringUtils.join(ret, ".");
    }

    /**
     * Convert headers into list block tree.
     *
     * @param headers the headers to convert.
     * @param start the "start" parameter value.
     * @param depth the "depth" parameter value.
     * @param numbered when {@code true} the {@link BulletedListBlock} use to display the ToC items are replaced by
     *     {@link NumberedListBlock}
     * @param headingsNumbered when {@code true} the numbers added on prefix of the headings are also displayed in
     *     the ToC before the headings content
     * @param rootBlock the root block of the current numbered headings
     * @return the root block of generated block tree or null if no header was matching the specified parameters
     */
    private Block generateTree(List<HeaderBlock> headers, int start, int depth, String documentReference,
        boolean numbered, boolean headingsNumbered, Block rootBlock)
    {
        Block tocBlock = null;

        int currentLevel = start - 1;
        Block currentBlock = null;
        for (HeaderBlock headerBlock : headers) {
            int headerLevel = headerBlock.getLevel().getAsInt();

            if (headerLevel >= start && headerLevel <= depth) {
                // Move to next header in toc tree

                if (currentLevel < headerLevel) {
                    while (currentLevel < headerLevel) {
                        if (currentBlock instanceof ListBLock) {
                            currentBlock = addItemBlock(currentBlock, null, documentReference, headingsNumbered,
                                rootBlock);
                        }

                        currentBlock = createChildListBlock(currentBlock, numbered);
                        ++currentLevel;
                    }
                } else {
                    while (currentLevel > headerLevel) {
                        currentBlock = currentBlock.getParent().getParent();
                        --currentLevel;
                    }
                    currentBlock = currentBlock.getParent();
                }

                currentBlock = addItemBlock(currentBlock, headerBlock, documentReference, headingsNumbered, rootBlock);
            }
        }

        if (currentBlock != null) {
            tocBlock = currentBlock.getRoot();
        }

        return tocBlock;
    }

    /**
     * Add a {@link ListItemBlock} in the current toc tree block and return the new {@link ListItemBlock}.
     *
     * @param currentBlock the current block in the toc tree.
     * @param headerBlock the {@link HeaderBlock} to use to generate toc anchor label.
     * @param rootBlock the root block of the currently numbered headings
     * @return the new {@link ListItemBlock}.
     */
    private Block addItemBlock(Block currentBlock, HeaderBlock headerBlock, String documentReference,
        boolean headingsNumbered, Block rootBlock)
    {
        ListItemBlock itemBlock = headerBlock == null
            ? createEmptyTocEntry()
            : createTocEntry(headerBlock, documentReference, headingsNumbered, rootBlock);

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
     * @param headerBlock the {@link HeaderBlock}.
     * @param rootBlock the root block of the currently numbered headings
     * @return the new list item block.
     */
    private ListItemBlock createTocEntry(HeaderBlock headerBlock, String documentReference, boolean headingsNumbered,
        Block rootBlock)
    {
        // Create the link to target the header anchor
        DocumentResourceReference reference = new DocumentResourceReference(documentReference);
        reference.setAnchor(headerBlock.getId());

        List<Block> childrenBlocks = this.tocBlockFilter.generateLabel(headerBlock);
        ArrayList<Block> blocks = new ArrayList<>();
        if (headingsNumbered) {
            Map<HeaderBlock, String> headerBlockStringMap = this.cache.get(rootBlock);
            blocks.add(new RawBlock(headerBlockStringMap.get(headerBlock), Syntax.XHTML_1_0));
            blocks.add(new SpaceBlock());
        }
        blocks.addAll(childrenBlocks);
        LinkBlock linkBlock = new LinkBlock(blocks, reference, false);

        return new ListItemBlock(singletonList(linkBlock));
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
