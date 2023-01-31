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
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.numbered.content.headings.FiguresNumberingService;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.IdBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;

import static org.xwiki.contrib.numbered.content.headings.macro.FigureType.FIGURE;
import static org.xwiki.rendering.internal.macro.figure.FigureTypeRecognizerMacro.DATA_XWIKI_RENDERING_FIGURE_TYPE;

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
    private static final String COUNTERS_KEY = "counters";

    private static final String FIGURES_KEY = "figures";

    @Inject
    private Execution execution;

    @Override
    public List<FigureBlock> getFiguresList(Block rootBlock)
    {
        return rootBlock.getBlocks(new ClassBlockMatcher(FigureBlock.class), Block.Axes.DESCENDANT);
    }

    @Override
    public Map<FigureBlock, String> getFiguresMap(Block rootBlock)
    {
        ExecutionContext context = this.execution.getContext();
        Map<String, Object> figureNumbering =
            (Map<String, Object>) context.getProperty(FigureNumberingExecutionContextInitializer.PROPERTY_KEY);
        Map<String, Long> counters = initAndGet(figureNumbering, COUNTERS_KEY);
        Map<String, Long> figuresMap = initAndGet(figureNumbering, FIGURES_KEY);

        Map<FigureBlock, String> result = new HashMap<>();
        for (FigureBlock figure : getFiguresList(rootBlock)) {
            String id = getId(figure);
            if (id != null && !figuresMap.containsKey(id)) {
                String type = Objects.toString(figure.getParameter(DATA_XWIKI_RENDERING_FIGURE_TYPE), FIGURE.getName());
                Long counter = counters.getOrDefault(type, 1L);
                result.put(figure, String.valueOf(counter));
                figuresMap.put(id, counter);
                counters.put(type, counter + 1);
            } else {
                result.put(figure, String.valueOf(figuresMap.get(id)));
            }
        }

        return result;
    }

    private String getId(FigureBlock figureBlock)
    {
        String idParameter = "id";
        if (figureBlock.getParameter(idParameter) != null) {
            return figureBlock.getParameter(idParameter);
        }
        IdBlock idBlock = figureBlock.getFirstBlock(IdBlock.class::isInstance, Block.Axes.DESCENDANT);
        if (idBlock != null) {
            return idBlock.getName();
        }
        ImageBlock firstBlock = figureBlock.getFirstBlock(ImageBlock.class::isInstance, Block.Axes.DESCENDANT);
        if (firstBlock != null) {
            return firstBlock.getId();
        }

        return null;
    }

    private static <K, V> Map<K, V> initAndGet(Map<String, Object> figureNumbering, String countersKey)
    {
        figureNumbering.putIfAbsent(countersKey, new HashMap<>());
        return (Map<K, V>) figureNumbering.get(countersKey);
    }
}
