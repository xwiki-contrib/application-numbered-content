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

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;

/**
 * Resolve the entries to use for a table of content for a given root block.
 * TODO: remove this class once the minimal parent-platform of this module includes XRENDERING-704.
 *
 * @version $Id$
 * @since 1.8
 */
@Role
public interface TocEntriesResolver
{
    /**
     * @param rootBlock the root block in which the table of content entries are resolved
     * @return the list of headers to use for the table of content
     */
    List<HeaderBlock> getHeaderBlocks(Block rootBlock);
}
