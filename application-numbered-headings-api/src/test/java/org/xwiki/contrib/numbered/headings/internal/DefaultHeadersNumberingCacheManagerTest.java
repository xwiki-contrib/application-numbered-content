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
package org.xwiki.contrib.numbered.headings.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.numbered.headings.internal.DefaultHeadersNumberingCacheManager.CachedValue;

/**
 * Test of {@link DefaultHeadersNumberingCacheManager}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class DefaultHeadersNumberingCacheManagerTest
{
    @InjectMockComponents
    private DefaultHeadersNumberingCacheManager defaultNumberingCacheManager;

    @MockComponent
    private CacheManager cacheManager;

    @Mock
    private Cache<CachedValue> cache;

    private final Block block = new SpaceBlock();

    @BeforeEach
    void setUp() throws Exception
    {
        when(this.cacheManager.<CachedValue>createNewCache(any())).thenReturn(this.cache);
        this.defaultNumberingCacheManager.initialize();
    }

    @Test
    void get()
    {
        Map<HeaderBlock, String> expectedMap = emptyMap();
        when(this.cache.get(blockHashCode())).thenReturn(new CachedValue(emptyMap(), emptyList()));
        Optional<Map<HeaderBlock, String>> actualMap = this.defaultNumberingCacheManager.get(this.block);
        assertEquals(Optional.of(expectedMap), actualMap);
    }

    @Test
    void getHeaders()
    {
        Map<HeaderBlock, String> t = new HashMap<>();
        HeaderBlock h1 = new HeaderBlock(singletonList(new SpaceBlock()), HeaderLevel.LEVEL1);
        HeaderBlock h2 = new HeaderBlock(singletonList(new SpaceBlock()), HeaderLevel.LEVEL2);
        t.put(h1, "1");
        t.put(h2, "2");
        when(this.cache.get(blockHashCode())).thenReturn(new CachedValue(t, asList(h1, h2)));
        Optional<List<HeaderBlock>> headers = this.defaultNumberingCacheManager.getHeaders(this.block);
        assertTrue(headers.isPresent());
        assertThat(headers.get(), containsInAnyOrder(h1, h2));
    }

    @Test
    void put()
    {
        this.defaultNumberingCacheManager.put(this.block, emptyMap(), emptyList());
        verify(this.cache).set(blockHashCode(), new CachedValue(emptyMap(), emptyList()));
    }

    private String blockHashCode()
    {
        return String.valueOf(this.block.hashCode());
    }
}