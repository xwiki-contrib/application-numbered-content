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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link DefaultNumberingCacheManager}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class DefaultNumberingCacheManagerTest
{
    @InjectMockComponents
    private DefaultNumberingCacheManager defaultNumberingCacheManager;

    @MockComponent
    private CacheManager cacheManager;

    @Mock
    private Cache<Map<HeaderBlock, String>> cache;

    private final Block block = new SpaceBlock();

    @BeforeEach
    void setUp() throws Exception
    {
        when(this.cacheManager.<Map<HeaderBlock, String>>createNewCache(any())).thenReturn(this.cache);
        this.defaultNumberingCacheManager.initialize();
    }

    @Test
    void get()
    {
        Map<HeaderBlock, String> expectedMap = Collections.emptyMap();
        when(this.cache.get(blockHashCode())).thenReturn(expectedMap);
        Map<HeaderBlock, String> actualMap = this.defaultNumberingCacheManager.get(this.block);
        assertSame(expectedMap, actualMap);
    }

    @Test
    void containsKey()
    {
        when(this.cache.get(blockHashCode())).thenReturn(null);
        assertFalse(this.defaultNumberingCacheManager.containsKey(this.block));
    }

    @Test
    void getHeaders()
    {
        Map<HeaderBlock, String> t = new HashMap<>();
        HeaderBlock h1 = new HeaderBlock(singletonList(new SpaceBlock()), HeaderLevel.LEVEL1);
        HeaderBlock h2 = new HeaderBlock(singletonList(new SpaceBlock()), HeaderLevel.LEVEL2);
        t.put(h1, "1");
        t.put(h2, "2");
        when(this.cache.get(blockHashCode())).thenReturn(t);
        assertEquals(asList(h1, h2), this.defaultNumberingCacheManager.getHeaders(this.block));
    }

    @Test
    void put()
    {
        Map<HeaderBlock, String> map = Collections.emptyMap();
        this.defaultNumberingCacheManager.put(this.block, map);
        verify(this.cache).set(blockHashCode(), map);
    }

    private String blockHashCode()
    {
        return String.valueOf(this.block.hashCode());
    }
}