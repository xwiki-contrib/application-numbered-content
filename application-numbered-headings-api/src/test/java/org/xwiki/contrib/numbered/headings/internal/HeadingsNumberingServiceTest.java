package org.xwiki.contrib.numbered.headings.internal;/*
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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link HeadingsNumberingService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class HeadingsNumberingServiceTest
{
    @InjectMockComponents
    private HeadingsNumberingService headingsNumberingService;

    @Test
    void getHeaderBlocks()
    {
        List<Block> blocks = asList(
            new HeaderBlock(emptyList(), HeaderLevel.LEVEL1),
            new HeaderBlock(emptyList(), HeaderLevel.LEVEL2)
        );
        List<HeaderBlock> headerBlocks = this.headingsNumberingService.getHeaderBlocks(new GroupBlock(blocks));
        assertEquals(blocks, headerBlocks);
    }

    @Test
    void getHeaderBlocksExcluded()
    {
        HeaderBlock h1 = new HeaderBlock(emptyList(), HeaderLevel.LEVEL1);
        List<Block> blocks = asList(
            h1,
            new GroupBlock(singletonList(new HeaderBlock(emptyList(), HeaderLevel.LEVEL2)),
                singletonMap("class", "numbered-content-root"))
        );
        List<HeaderBlock> headerBlocks = this.headingsNumberingService.getHeaderBlocks(new GroupBlock(blocks));
        assertEquals(singletonList(h1), headerBlocks);
    }
}