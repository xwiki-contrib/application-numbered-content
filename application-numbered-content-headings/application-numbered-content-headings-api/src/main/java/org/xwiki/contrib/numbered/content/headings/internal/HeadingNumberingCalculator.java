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
package org.xwiki.contrib.numbered.content.headings.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.IdBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;

/**
 * Helper class to calculate the number of a heading based on previously added headings.
 * <p>
 * This should be used as a temporary helper as the class is neither designed to be re-usable nor thread-safe. For this
 * reason, this is also not a component.
 *
 * @version $Id$
 * @since 1.2
 */
public class HeadingNumberingCalculator
{
    private final Deque<Integer> stack;

    private final Map<String, String> ids = new HashMap<>();

    /**
     * Initialize the heading numbering calculator.
     */
    public HeadingNumberingCalculator()
    {
        this.stack = new ArrayDeque<>();
        this.stack.addLast(0);
    }

    /**
     * Add a heading to the numbering.
     *
     * @param headerBlock the header block to add
     * @param start the start number for the heading of this level, null for continuous numbering
     * @return the computed numbering the of the added header, or {@code null} if the header does not have an id
     */
    public String addHeading(HeaderBlock headerBlock, Integer start)
    {
        int level = headerBlock.getLevel().getAsInt();
        String id = getId(headerBlock);
        if (id == null) {
            return null;
        }

        if (this.ids.containsKey(id)) {
            return this.ids.get(id);
        }

        // Pad with zeros to reach the current level.
        while (this.stack.size() < level) {
            this.stack.addLast(0);
        }

        // Remove numbers of higher levels.
        while (this.stack.size() > level) {
            this.stack.removeLast();
        }

        // Increment or replace the last number.
        Integer lastNumber = this.stack.removeLast();

        this.stack.addLast(Objects.requireNonNullElseGet(start, () -> lastNumber + 1));

        String computedNumber = printStack();
        this.ids.put(id, computedNumber);
        return computedNumber;
    }

    /**
     * @return The number of the last-added heading as string.
     */
    @Override
    public String toString()
    {
        return printStack();
    }

    private String getId(HeaderBlock headerBlock)
    {
        String id = headerBlock.getId();
        if (id != null) {
            return id;
        }

        String idParameter = headerBlock.getParameter("id");
        if (idParameter != null) {
            return idParameter;
        }

        IdBlock idBlock = headerBlock.getFirstBlock(new ClassBlockMatcher(IdBlock.class), Block.Axes.DESCENDANT);
        if (idBlock != null) {
            return idBlock.getName();
        }
        return null;
    }

    private String printStack()
    {
        return this.stack.stream().map(Object::toString).collect(Collectors.joining("."));
    }
}
