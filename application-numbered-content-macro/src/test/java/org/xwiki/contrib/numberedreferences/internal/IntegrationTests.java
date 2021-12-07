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
package org.xwiki.contrib.numberedreferences.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.mockito.stubbing.Answer;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheManager;
import org.xwiki.localization.LocalizationContext;
import org.xwiki.localization.LocalizationManager;
import org.xwiki.localization.Translation;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.test.integration.TestDataParser;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link TestDataParser}.
 *
 * @version $Id$
 * @since 1.0
 */
@AllComponents
public class IntegrationTests implements RenderingTests
{
    @Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        CacheManager cacheManager = componentManager.registerMockComponent(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(cacheManager.createNewCache(any())).thenReturn(cache);
        Map<String, DefaultHeadersNumberingCacheManager.CachedValue> mapCache = new HashMap<>();
        doAnswer(invocation -> {
            mapCache.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(cache).set(any(), any());
        when(cache.get(any())).thenAnswer(invocation -> mapCache.get(invocation.getArgument(0)));

        Locale defaultLocale = Locale.getDefault();
        LocalizationContext localizationContext = componentManager.registerMockComponent(LocalizationContext.class);
        when(localizationContext.getCurrentLocale()).thenReturn(defaultLocale);
        LocalizationManager localizationManager = componentManager.registerMockComponent(LocalizationManager.class);

        // Mock the translation by retuning the passed key. Optionally with the list of arguments between brackets separated by commas.
        // For instance "my.translation.key [A, B]".
        when(localizationManager.getTranslation(any(), eq(defaultLocale))).thenAnswer(invocation -> {
            Translation translation = mock(Translation.class);
            String translationKey = invocation.getArgument(0);
            when(translation.render(any())).thenAnswer((Answer<Block>) invocationRender -> {
                List<Block> blocks = new ArrayList<>();
                blocks.add(new WordBlock(translationKey));
                Object[] parameters = invocationRender.getArguments();
                if (parameters.length > 0) {
                    blocks.add(new SpaceBlock());
                    blocks.add(new SpecialSymbolBlock('['));
                    for (int i = 0; i < parameters.length; i++) {
                        blocks.add(new WordBlock(String.valueOf(parameters[i])));
                        if (i < parameters.length - 1) {
                            blocks.add(new SpecialSymbolBlock(','));
                        }
                    }
                    blocks.add(new SpecialSymbolBlock(']'));
                }
                return new CompositeBlock(blocks);
            });
            return translation;
        });
    }
}
