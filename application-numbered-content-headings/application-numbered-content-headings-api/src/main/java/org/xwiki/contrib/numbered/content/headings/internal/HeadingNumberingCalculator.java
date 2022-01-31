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
import java.util.stream.Collectors;

/**
 * Helper class to calculate the number of a heading based on previously added headings.
 *
 * @version $Id$
 * @since 1.2
 */
public class HeadingNumberingCalculator
{
    private final Deque<Integer> stack;

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
     * @param level The level of the heading.
     * @param start The start number for the heading of this level, null for continuous numbering.
     */
    public void addHeading(int level, Integer start)
    {
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

        if (start != null) {
            this.stack.addLast(start);
        } else {
            this.stack.addLast(lastNumber + 1);
        }
    }

    /**
     * @return The number of the last-added heading as string.
     */
    @Override
    public String toString()
    {
        return this.stack.stream().map(Object::toString).collect(Collectors.joining("."));
    }
}
