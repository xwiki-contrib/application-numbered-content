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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;

/**
 * Cache manager for the numbered headings. The keys are the root block containing the numbered content, the keys are
 * maps of numbered contents and their corresponding values.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
public interface HeadingsNumberingCacheManager
{
    /**
     * Return the list of headers that are cached for a given block.
     *
     * @param block the block containing the headers
     * @return the list of cached headers
     */
    Optional<List<HeaderBlock>> getHeadings(Block block);

    /**
     * Associate the computed value to the given block.
     *
     * @param block the block containing the headers to number
     * @param values the computed values for the headers of the block
     * @param headings the list of headers in their order of appearance
     */
    void put(Block block, Map<HeaderBlock, String> values, List<HeaderBlock> headings);

    /**
     * Return the cached map of headers and their computed numbers.
     *
     * @param block the block containing the headers to number
     * @return the cached map of headers and their computed numbers
     */
    Optional<Map<HeaderBlock, String>> get(Block block);
}
