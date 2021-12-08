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
package org.xwiki.contrib.numbered.content;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.stability.Unstable;

/**
 * Provide the operations to compute the numbering of figures.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
@Unstable
public interface FiguresNumberingService
{
    /**
     * Return a list of figures found in a root block.
     *
     * @param rootBlock the root block to analyze
     * @return the list of figures found in the root block
     */
    List<FigureBlock> getFiguresList(Block rootBlock);

    /**
     * Return a map of the figures found in a root block associated with their computed numbering.
     *
     * @param rootBlock the root block to analyze
     * @return the computed map
     */
    Map<FigureBlock, String> getFiguresMap(Block rootBlock);
}
