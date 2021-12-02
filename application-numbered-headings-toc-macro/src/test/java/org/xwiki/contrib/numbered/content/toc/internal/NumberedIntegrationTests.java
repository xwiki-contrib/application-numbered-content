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
package org.xwiki.contrib.numbered.content.toc.internal;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheManager;
import org.xwiki.contrib.numbered.headings.internal.NumberedHeadingsService;
import org.xwiki.rendering.test.integration.TestDataParser;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.numberedreferences.internal.DefaultNumberingCacheManager.CachedValue;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link TestDataParser}.
 * <p>
 * Run integration tests of the ToC Macro when the numbering is activated.
 *
 * @version $Id$
 * @since 1.0
 */
@AllComponents
@RenderingTests.Scope(value = "toc_numbered/")
public class NumberedIntegrationTests implements RenderingTests
{
    @Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        NumberedHeadingsService numberedHeadingService =
            componentManager.registerMockComponent(NumberedHeadingsService.class);
        when(numberedHeadingService.isNumberedHeadingsEnabled()).thenReturn(true);
        CacheManager cacheManager = componentManager.registerMockComponent(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(cacheManager.createNewCache(any())).thenReturn(cache);
        Map<String, CachedValue> mapCache = new HashMap<>();
        doAnswer(invocation -> {
            mapCache.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(cache).set(any(), any());
        when(cache.get(any())).thenAnswer(invocation -> {
            return mapCache.get(invocation.getArgument(0));
        });
    }
}
