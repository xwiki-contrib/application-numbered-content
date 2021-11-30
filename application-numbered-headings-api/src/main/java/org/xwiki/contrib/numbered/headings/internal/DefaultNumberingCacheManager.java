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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.EntryEvictionConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.numbered.headings.NumberingCacheManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;

/**
 * The default implementation of {@link NumberingCacheManager}, based on a {@link CacheManager} with an {@link
 * LRUEvictionConfiguration} eviction configuration.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultNumberingCacheManager implements NumberingCacheManager, Initializable
{
    @Inject
    private CacheManager cacheManager;

    private Cache<Map<HeaderBlock, String>> cache;

    @Override
    public void initialize() throws InitializationException
    {
        try {

            CacheConfiguration cacheConfiguration = new CacheConfiguration();
            cacheConfiguration.setConfigurationId("numbered.content.numbering.cache");
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
    public boolean containsKey(Block block)
    {
        return this.cache.get(computeKey(block)) != null;
    }

    @Override
    public List<HeaderBlock> getHeaders(Block block)
    {
        return new ArrayList<>(get(block).keySet());
    }

    @Override
    public void put(Block block, Map<HeaderBlock, String> value)
    {
        this.cache.set(computeKey(block), value);
    }

    @Override
    public Map<HeaderBlock, String> get(Block block)
    {
        return this.cache.get(computeKey(block));
    }

    private String computeKey(Block block)
    {
        return String.valueOf(block.hashCode());
    }
}
