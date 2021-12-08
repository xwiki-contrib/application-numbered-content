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
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL1;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL2;

/**
 * Test of {@link HeadersNumberingService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class HeadersNumberingServiceTest
{
    @InjectMockComponents
    private HeadersNumberingService headersNumberingService;

    @MockComponent
    protected HeadersNumberingCacheManager cacheManager;

    @Test
    void getHeaderBlocks()
    {
        List<Block> blocks = asList(
            new HeaderBlock(emptyList(), HeaderLevel.LEVEL1),
            new HeaderBlock(emptyList(), HeaderLevel.LEVEL2)
        );
        List<HeaderBlock> headerBlocks = this.headersNumberingService.getHeaderBlocks(new GroupBlock(blocks));
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
        List<HeaderBlock> headerBlocks = this.headersNumberingService.getHeaderBlocks(new GroupBlock(blocks));
        assertEquals(singletonList(h1), headerBlocks);
    }

    /**
     * getHeaders getMap
     */
    @Test
    void getHeadersCached()
    {
        List<HeaderBlock> cachedValue = emptyList();
        XDOM rootBlock = new XDOM(emptyList());
        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.of(cachedValue));
        List<HeaderBlock> headers = this.headersNumberingService.getHeadersList(rootBlock);
        assertSame(cachedValue, headers);
    }

    @Test
    void getHeadersNotCached()
    {

        HeaderBlock h2 = new HeaderBlock(emptyList(), LEVEL2);
        HeaderBlock h1 = new HeaderBlock(emptyList(), LEVEL1);
        XDOM rootBlock = new XDOM(asList(h2, h1));

        Map<HeaderBlock, String> expectedMap = new HashMap<>();
        expectedMap.put(h2, "0.1");
        expectedMap.put(h1, "1");

        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.empty());

        List<HeaderBlock> obtainedHeaders = this.headersNumberingService.getHeadersList(rootBlock);

        ArgumentCaptor<Map<HeaderBlock, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<List<HeaderBlock>> headersCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.cacheManager).put(any(), mapCaptor.capture(), headersCaptor.capture());

        assertEquals(expectedMap, mapCaptor.getValue());
        List<HeaderBlock> value = headersCaptor.getValue();
        assertEquals(asList(h2, h1), value);
        assertEquals(asList(h2, h1), obtainedHeaders);
    }

    @Test
    void getHeadersNotCachedWithStartParameter()
    {
        HeaderBlock h10 = new HeaderBlock(emptyList(), LEVEL1, singletonMap("start", "10"));
        HeaderBlock h11 = new HeaderBlock(emptyList(), LEVEL1);
        XDOM rootBlock = new XDOM(asList(h10, h11));
        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.empty());

        List<HeaderBlock> obtainedHeaders = this.headersNumberingService.getHeadersList(rootBlock);

        ArgumentCaptor<Map<HeaderBlock, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.cacheManager).put(any(), mapCaptor.capture(), any());

        Map<HeaderBlock, String> expectedMap = new HashMap<>();
        expectedMap.put(h10, "10");
        expectedMap.put(h11, "11");
        assertEquals(expectedMap, mapCaptor.getValue());
        assertEquals(asList(h10, h11), obtainedHeaders);
    }

    @Test
    void getHeadersNotCachedWithSkipParameter()
    {
        HeaderBlock hskip = new HeaderBlock(emptyList(), LEVEL1, singletonMap("skip", "true"));
        HeaderBlock h1 = new HeaderBlock(emptyList(), LEVEL1);

        XDOM rootBlock = new XDOM(asList(hskip, h1));
        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.empty());

        List<HeaderBlock> obtainedHeaders = this.headersNumberingService.getHeadersList(rootBlock);

        ArgumentCaptor<Map<HeaderBlock, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.cacheManager).put(any(), mapCaptor.capture(), any());

        assertEquals(singletonMap(h1, "1"), mapCaptor.getValue());
        assertEquals(asList(hskip, h1), obtainedHeaders);
    }

    @Test
    void getHeadersNotCachedWithHeaderInBlock()
    {
        HeaderBlock h1 = new HeaderBlock(emptyList(), LEVEL1);
        h1.setParent(new GroupBlock());
        HeaderBlock h2 = new HeaderBlock(emptyList(), LEVEL1);
        XDOM rootBlock = new XDOM(asList(h1, h2));
        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.empty());

        List<HeaderBlock> obtainedHeaders = this.headersNumberingService.getHeadersList(rootBlock);

        ArgumentCaptor<Map<HeaderBlock, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.cacheManager).put(any(), mapCaptor.capture(), any());

        Map<HeaderBlock, String> expectedMap = new HashMap<>();
        expectedMap.put(h1, "1");
        expectedMap.put(h2, "2");
        assertEquals(expectedMap, mapCaptor.getValue());
        assertEquals(asList(h1, h2), obtainedHeaders);
    }

    @Test
    void getMapNotCached()
    {
        HeaderBlock h1 = new HeaderBlock(emptyList(), LEVEL1);
        HeaderBlock h2 = new HeaderBlock(emptyList(), LEVEL2);
        XDOM rootBlock = new XDOM(asList(h1, h2));

        Map<HeaderBlock, String> expected = new HashMap<>();
        expected.put(h1, "1");
        expected.put(h2, "1.1");

        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.empty());
        when(this.cacheManager.get(rootBlock)).thenReturn(Optional.empty(), Optional.of(expected));

        Map<HeaderBlock, String> obtainedMap = this.headersNumberingService.getHeadersMap(rootBlock);

        ArgumentCaptor<Map<HeaderBlock, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<List<HeaderBlock>> headersCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.cacheManager).put(any(), mapCaptor.capture(), headersCaptor.capture());

        Map<HeaderBlock, String> actualMap = mapCaptor.getValue();
        assertEquals(expected, actualMap);
        List<HeaderBlock> value = headersCaptor.getValue();
        assertEquals(asList(h1, h2), value);
        assertSame(expected, obtainedMap);
    }
}