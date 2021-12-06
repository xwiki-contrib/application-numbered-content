package org.xwiki.contrib.numberedreferences;/*
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
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL1;
import static org.xwiki.rendering.listener.HeaderLevel.LEVEL2;

/**
 * Test of {@link AbstractNumberingService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class AbstractNumberingServiceTest
{
    // Proxy to the AbstractNumberingService.
    @InjectMockComponents
    private TestNumberingService numberingService;

    @MockComponent
    protected NumberingCacheManager cacheManager;

    /**
     * getHeaders getMap
     */
    @Test
    void getHeadersCached()
    {
        List<HeaderBlock> cachedValue = emptyList();
        XDOM rootBlock = new XDOM(emptyList());
        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.of(cachedValue));
        List<HeaderBlock> headers = this.numberingService.getHeaders(rootBlock);
        assertSame(cachedValue, headers);
    }

    @Test
    void getHeadersNotCached()
    {
        XDOM rootBlock = new XDOM(emptyList());

        List<HeaderBlock> expectedHeaders = asList(
            new HeaderBlock(emptyList(), LEVEL2),
            new HeaderBlock(emptyList(), LEVEL1)
        );

        Map<HeaderBlock, String> expectedMap = new HashMap<>();
        expectedMap.put(expectedHeaders.get(0), "0.1");
        expectedMap.put(expectedHeaders.get(1), "1");

        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.empty());

        this.numberingService.setHeaders(expectedHeaders);
        List<HeaderBlock> obtainedHeaders = this.numberingService.getHeaders(rootBlock);

        ArgumentCaptor<Map<HeaderBlock, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<List<HeaderBlock>> headersCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.cacheManager).put(any(), mapCaptor.capture(), headersCaptor.capture());

        assertEquals(expectedMap, mapCaptor.getValue());
        List<HeaderBlock> value = headersCaptor.getValue();
        assertEquals(expectedHeaders, value);
        assertSame(expectedHeaders, obtainedHeaders);
    }

    @Test
    void getHeadersNotCachedWithStartParameter()
    {
        XDOM rootBlock = new XDOM(emptyList());
        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.empty());

        List<HeaderBlock> expectedHeaders = asList(
            new HeaderBlock(emptyList(), LEVEL1,singletonMap("start", "10")),
            new HeaderBlock(emptyList(), LEVEL1)
        );
        this.numberingService.setHeaders(expectedHeaders);
        List<HeaderBlock> obtainedHeaders = this.numberingService.getHeaders(rootBlock);

        ArgumentCaptor<Map<HeaderBlock, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.cacheManager).put(any(), mapCaptor.capture(), any());

        Map<HeaderBlock, String> expectedMap = new HashMap<>();
        expectedMap.put(expectedHeaders.get(0), "10");
        expectedMap.put(expectedHeaders.get(1), "11");
        assertEquals(expectedMap, mapCaptor.getValue());
        assertSame(expectedHeaders, obtainedHeaders);
    }

    @Test
    void getHeadersNotCachedWithHeaderInBLock()
    {
        XDOM rootBlock = new XDOM(emptyList());
        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.empty());

        HeaderBlock headerBlock = new HeaderBlock(emptyList(), LEVEL1);
        headerBlock.setParent(new GroupBlock());
        List<HeaderBlock> expectedHeaders = asList(
            headerBlock,
            new HeaderBlock(emptyList(), LEVEL1)
        );
        this.numberingService.setHeaders(expectedHeaders);
        List<HeaderBlock> obtainedHeaders = this.numberingService.getHeaders(rootBlock);

        ArgumentCaptor<Map<HeaderBlock, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(this.cacheManager).put(any(), mapCaptor.capture(), any());

        Map<HeaderBlock, String> expectedMap = new HashMap<>();
        expectedMap.put(expectedHeaders.get(0), "1");
        expectedMap.put(expectedHeaders.get(1), "2");
        assertEquals(expectedMap, mapCaptor.getValue());
        assertSame(expectedHeaders, obtainedHeaders);
    }

    @Test
    void getMapNotCached()
    {
        XDOM rootBlock = new XDOM(emptyList());

        List<HeaderBlock> expectedHeaders = asList(
            new HeaderBlock(emptyList(), LEVEL1),
            new HeaderBlock(emptyList(), LEVEL2)
        );

        Map<HeaderBlock, String> expected = new HashMap<>();
        expected.put(expectedHeaders.get(0), "1");
        expected.put(expectedHeaders.get(1), "1.1");

        when(this.cacheManager.getHeaders(rootBlock)).thenReturn(Optional.empty());
        when(this.cacheManager.get(rootBlock)).thenReturn(Optional.empty(), Optional.of(expected));

        this.numberingService.setHeaders(expectedHeaders);
        Map<HeaderBlock, String> obtainedMap = this.numberingService.getMap(rootBlock);

        ArgumentCaptor<Map<HeaderBlock, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<List<HeaderBlock>> headersCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.cacheManager).put(any(), mapCaptor.capture(), headersCaptor.capture());

        Map<HeaderBlock, String> actualMap = mapCaptor.getValue();
        assertEquals(expected, actualMap);
        List<HeaderBlock> value = headersCaptor.getValue();
        assertEquals(expectedHeaders, value);
        assertSame(expected, obtainedMap);
    }
}