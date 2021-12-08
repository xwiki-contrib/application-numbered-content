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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.EntryEvictionConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;

/**
 * TODO: document me.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultFiguresNumberingCacheManager implements FiguresNumberingCacheManager, Initializable
{
    @Inject
    private CacheManager cacheManager;

    private Cache<CachedValue> cache;

    /**
     * Internal cache object used to store the resolved numbers and the list of blocks found in the numbered context.
     */
    public static final class CachedValue
    {
        private final Map<FigureBlock, String> numbers;

        private final List<FigureBlock> orderedBlocks;

        /**
         * Default constructor.
         *
         * @param numbers the list of numbered headers
         * @param orderedBlocks the list of headers found in the numbered context
         */
        CachedValue(Map<FigureBlock, String> numbers,
            List<FigureBlock> orderedBlocks)
        {
            this.numbers = numbers;
            this.orderedBlocks = orderedBlocks;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CachedValue that = (CachedValue) o;

            return new EqualsBuilder().append(this.numbers, that.numbers)
                .append(this.orderedBlocks, that.orderedBlocks).isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(17, 37).append(this.numbers).append(this.orderedBlocks).toHashCode();
        }
    }

    @Override
    public void initialize() throws InitializationException
    {
        try {
            CacheConfiguration cacheConfiguration = new CacheConfiguration();
            cacheConfiguration.setConfigurationId("numbered.content.figures.cache");
            // Configure cache eviction policy
            LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
            lru.setMaxEntries(1000);
            lru.setMaxIdle(60);
            lru.setLifespan(60);
            cacheConfiguration.put(EntryEvictionConfiguration.CONFIGURATIONID, lru);
            this.cache = this.cacheManager.createNewCache(cacheConfiguration);
        } catch (CacheException e) {
            throw new InitializationException("Failed to create the cache.", e);
        }
    }

    @Override
    public Optional<List<FigureBlock>> getFigures(Block block)
    {
        CachedValue cachedValue = this.cache.get(computeKey(block));
        return Optional.ofNullable(cachedValue).map(it -> it.orderedBlocks);
    }

    @Override
    public void put(Block block, Map<FigureBlock, String> values, List<FigureBlock> headers)
    {
        this.cache.set(computeKey(block), new CachedValue(values, headers));
    }

    @Override
    public Optional<Map<FigureBlock, String>> get(Block block)
    {
        return Optional.ofNullable(this.cache.get(computeKey(block))).map(it -> it.numbers);
    }

    private String computeKey(Block block)
    {
        return String.valueOf(block.hashCode());
    }
}
