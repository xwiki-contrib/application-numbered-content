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
package org.xwiki.contrib.numbered.content.headings.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.headings.FiguresNumberingService;
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
    @Override
    public List<FigureBlock> getFiguresList(Block rootBlock)
    {
        return rootBlock.getBlocks(new ClassBlockMatcher(FigureBlock.class), Block.Axes.DESCENDANT);
    }

    @Override
    public Map<FigureBlock, String> getFiguresMap(Block rootBlock)
    {
        Map<FigureBlock, String> result = new HashMap<>();

        int cptr = 1;
        for (FigureBlock figure : getFiguresList(rootBlock)) {
            result.put(figure, String.valueOf(cptr));
            cptr++;
        }

        return result;
    }
}
