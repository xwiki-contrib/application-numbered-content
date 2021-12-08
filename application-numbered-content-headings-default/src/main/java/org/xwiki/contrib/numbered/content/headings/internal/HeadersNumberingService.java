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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.headings.AbstractHeadersNumberingService;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;

import static org.xwiki.rendering.block.Block.Axes.DESCENDANT;

/**
 * Compute the numbers for the headers and save the result in a cache.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("headings")
@Singleton
public class HeadersNumberingService extends AbstractHeadersNumberingService
{
    private final ClassBlockMatcher classBlockMatcher = new ClassBlockMatcher(HeaderBlock.class);

    @Override
    public List<HeaderBlock> getHeaderBlocks(Block rootBlock)
    {
        List<HeaderBlock> list = new ArrayList<>();
        for (HeaderBlock block : rootBlock.<HeaderBlock>getBlocks(this.classBlockMatcher, DESCENDANT)) {
            if (!isExcluded(block)) {
                list.add(block);
            }
        }
        return list;
    }

    private boolean isExcluded(HeaderBlock h)
    {
        Block parent = h.getParent();
        while (parent != null) {
            String classes = parent.getParameter("class");
            if (classes != null && classes.contains(NUMBERED_CONTENT_ROOT_CLASS)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
}
