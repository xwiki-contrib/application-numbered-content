package org.xwiki.contrib.numbered.content.headings.internal;/*
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService.SKIP_PARAMETER;
import static org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService.START_PARAMETER;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL1;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL2;

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
    private HeadingsNumberingService headersNumberingService;

    @MockComponent
    private Execution execution;

    @Mock
    private ExecutionContext context;

    @BeforeEach
    void setUp()
    {
        when(this.execution.getContext()).thenReturn(this.context);
        when(this.context.getProperty(HeadingsNumberingExecutionContextInitializer.PROPERTY_KEY))
            .thenReturn(new HeadingNumberingCalculator());
    }

    @Test
    void getHeaderBlocks()
    {
        List<Block> blocks = asList(
            new HeaderBlock(emptyList(), HeaderLevel.LEVEL1),
            new HeaderBlock(emptyList(), HeaderLevel.LEVEL2)
        );
        List<HeaderBlock> headerBlocks = this.headersNumberingService.getHeadingsBlocks(new GroupBlock(blocks));
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
        List<HeaderBlock> headerBlocks = this.headersNumberingService.getHeadingsBlocks(new GroupBlock(blocks));
        assertEquals(singletonList(h1), headerBlocks);
    }

    @Test
    void getHeaders()
    {
        HeaderBlock h2 = new HeaderBlock(emptyList(), LEVEL2, "H01");
        HeaderBlock h1 = new HeaderBlock(emptyList(), LEVEL1, "H1");
        XDOM rootBlock = new XDOM(asList(h2, h1));

        Map<HeaderBlock, String> expectedMap = new HashMap<>();
        expectedMap.put(h2, "0.1");
        expectedMap.put(h1, "1");
        assertEquals(expectedMap, this.headersNumberingService.getHeadingsMap(rootBlock));

        List<HeaderBlock> obtainedHeaders = this.headersNumberingService.getHeadingsList(rootBlock);
        assertEquals(asList(h2, h1), obtainedHeaders);
    }

    @Test
    void getHeadersWithStartParameter()
    {
        HeaderBlock h10 = new HeaderBlock(emptyList(), LEVEL1, singletonMap(START_PARAMETER, "10"), "H10");
        HeaderBlock h11 = new HeaderBlock(emptyList(), LEVEL1, "H11");
        XDOM rootBlock = new XDOM(asList(h10, h11));

        List<HeaderBlock> obtainedHeaders = this.headersNumberingService.getHeadingsList(rootBlock);
        assertEquals(asList(h10, h11), obtainedHeaders);

        Map<HeaderBlock, String> expectedMap = new HashMap<>();
        expectedMap.put(h10, "10");
        expectedMap.put(h11, "11");
        assertEquals(expectedMap, this.headersNumberingService.getHeadingsMap(rootBlock));
    }

    @Test
    void getHeadersWithSkipParameter()
    {
        HeaderBlock hskip = new HeaderBlock(emptyList(), LEVEL1, singletonMap(SKIP_PARAMETER, "true"));
        HeaderBlock h1 = new HeaderBlock(emptyList(), LEVEL1, "H1");

        XDOM rootBlock = new XDOM(asList(hskip, h1));

        List<HeaderBlock> obtainedHeaders = this.headersNumberingService.getHeadingsList(rootBlock);
        assertEquals(asList(hskip, h1), obtainedHeaders);

        assertEquals(singletonMap(h1, "1"), this.headersNumberingService.getHeadingsMap(rootBlock));
    }

    @Test
    void getHeadersWithHeaderInBlock()
    {
        HeaderBlock h1 = new HeaderBlock(emptyList(), LEVEL1, "H1");
        h1.setParent(new GroupBlock());
        HeaderBlock h2 = new HeaderBlock(emptyList(), LEVEL1, "H2");
        XDOM rootBlock = new XDOM(asList(h1, h2));

        List<HeaderBlock> obtainedHeaders = this.headersNumberingService.getHeadingsList(rootBlock);
        assertEquals(asList(h1, h2), obtainedHeaders);

        Map<HeaderBlock, String> expectedMap = new HashMap<>();
        expectedMap.put(h1, "1");
        expectedMap.put(h2, "2");
        assertEquals(expectedMap, this.headersNumberingService.getHeadingsMap(rootBlock));
    }

    @Test
    void getMap()
    {
        HeaderBlock h1 = new HeaderBlock(emptyList(), LEVEL1, "H1");
        HeaderBlock h2 = new HeaderBlock(emptyList(), LEVEL2, "H11");
        XDOM rootBlock = new XDOM(asList(h1, h2));

        Map<HeaderBlock, String> expected = new HashMap<>();
        expected.put(h1, "1");
        expected.put(h2, "1.1");
        assertEquals(expected, this.headersNumberingService.getHeadingsMap(rootBlock));

        assertEquals(asList(h1, h2), this.headersNumberingService.getHeadingsList(rootBlock));
    }
}