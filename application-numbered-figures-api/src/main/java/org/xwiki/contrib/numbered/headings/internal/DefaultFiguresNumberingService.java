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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numberedreferences.FiguresNumberingService;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;

/**
 * Compute the numbers for the figures and save the result in a cache.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultFiguresNumberingService implements FiguresNumberingService
{
    @Inject
    protected FiguresNumberingCacheManager cacheManager;

    @Override
    public List<FigureBlock> getFiguresList(Block rootBlock)
    {
        Optional<List<FigureBlock>> figuresOpt = this.cacheManager.getFigures(rootBlock);
        List<FigureBlock> headers;
        if (figuresOpt.isPresent()) {
            headers = figuresOpt.get();
        } else {
            headers = getFigureBlocks(rootBlock);
            buildCache(rootBlock, headers);
        }
        return headers;
    }

    @Override
    public Map<FigureBlock, String> getFiguresMap(Block rootBlock)
    {
        Optional<Map<FigureBlock, String>> figuresOpt = this.cacheManager.get(rootBlock);
        Map<FigureBlock, String> figures;
        if (figuresOpt.isPresent()) {
            figures = figuresOpt.get();
        } else {
            buildCache(rootBlock, getFigureBlocks(rootBlock));
            figures = this.cacheManager.get(rootBlock).get();
        }
        return figures;
    }

    /**
     * Return the figure blocks that needs to be numbered.
     *
     * @param rootBlock the root block to number
     * @return the list of figure blocks to number
     */
    private List<FigureBlock> getFigureBlocks(Block rootBlock)
    {
        return rootBlock.getBlocks(new ClassBlockMatcher(FigureBlock.class), Block.Axes.DESCENDANT);
    }

    private void buildCache(Block rootBlock, List<FigureBlock> figures)
    {
        Map<FigureBlock, String> rootBlockCache = new HashMap<>();
        this.cacheManager.put(rootBlock, rootBlockCache, figures);

        int cptr = 1;
        for (FigureBlock figure : figures) {
            rootBlockCache.put(figure, String.valueOf(cptr));
            cptr++;
        }
    }
}
