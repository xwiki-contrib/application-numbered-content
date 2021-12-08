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
package org.xwiki.contrib.numbered.content.reference.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.FiguresNumberingService;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;

/**
 * Numbering Service used for the integration tests, selects all the figures in the document.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named("integrationtestfigures")
public class IntegrationTestFiguresNumberingService implements FiguresNumberingService
{
    @Override
    public List<FigureBlock> getFiguresList(Block rootBlock)
    {
        return rootBlock.getBlocks(new ClassBlockMatcher(FigureBlock.class), Block.Axes.DESCENDANT);
    }

    @Override
    public Map<FigureBlock, String> getFiguresMap(Block rootBlock)
    {
        Map<FigureBlock, String> ret = new HashMap<>();
        int i = 1;
        for (FigureBlock block : rootBlock.<FigureBlock>getBlocks(new ClassBlockMatcher(FigureBlock.class),
            Block.Axes.DESCENDANT)) {
            ret.put(block, String.valueOf(i));
            i++;
        }
        return ret;
    }
}
