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
package org.xwiki.contrib.numbered.content.figures.latex.internal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.figure.FigureType;
import org.xwiki.contrib.figure.internal.FigureTypesConfiguration;
import org.xwiki.contrib.numbered.content.figures.NumberedFiguresException;
import org.xwiki.contrib.numbered.content.figures.internal.NumberedFiguresConfiguration;

import static org.xwiki.contrib.figure.FigureType.DEFAULT_FIGURE_TYPES;

/**
 * Compute the counters for the figure types.
 *
 * @version $Id$
 * @since 1.9
 */
@Component(roles = NumberedCounterService.class)
@Singleton
public class NumberedCounterService
{
    @Inject
    private FigureTypesConfiguration figureTypesConfiguration;

    @Inject
    private NumberedFiguresConfiguration numberedFiguresConfiguration;

    /**
     * @return the map of figure types and their corresponding counters
     * @throws NumberedFiguresException in case of issue when loading the configuration
     */
    public Map<FigureType, String> computeCounters() throws NumberedFiguresException
    {
        Map<String, Set<FigureType>> counters = this.numberedFiguresConfiguration.getFigureCounters();
        Map<String, FigureType> countersCache = new HashMap<>();

        return this.figureTypesConfiguration.getFigureTypes()
            .stream()
            // This sort is actually important to make sure that the figure are always evaluated in the same order.
            // Therefore, the same type is always use as the counter, which makes things easier to understand and debug.
            .sorted(Comparator.comparing(FigureType::getId))
            .filter(it -> !DEFAULT_FIGURE_TYPES.contains(it.getId()))
            .collect(Collectors.toMap(it -> it, type -> getComputeCounter(counters, countersCache, type)));
    }

    private static String getComputeCounter(Map<String, Set<FigureType>> counters,
        Map<String, FigureType> countersCache, FigureType type)
    {
        String counter = type.getId();
        for (Map.Entry<String, Set<FigureType>> stringSetEntry : counters.entrySet()) {
            Set<FigureType> counterTypes = stringSetEntry.getValue();
            String counterId = stringSetEntry.getKey();
            if (counterTypes.contains(type)) {
                if (!countersCache.containsKey(counterId)) {
                    countersCache.put(counterId, type);
                }
                counter = countersCache.get(counterId).getId();
                break;
            }
        }
        return counter;
    }
}
