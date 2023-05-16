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

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.figure.FigureType;
import org.xwiki.contrib.figure.latex.internal.DefaultTypedFigureTool;
import org.xwiki.contrib.latex.internal.DefaultFigureTool;
import org.xwiki.contrib.latex.internal.FigureTool;
import org.xwiki.contrib.numbered.content.figures.NumberedFiguresException;
import org.xwiki.rendering.block.Block;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Specialization of {@link FigureTool} for the specificities of numbered and types figures. Overrides
 * {@link DefaultTypedFigureTool} and {@link DefaultFigureTool}.
 *
 * @version $Id$
 * @since 1.9
 */
@Component
@Singleton
public class NumberedTypedFigureTool extends DefaultTypedFigureTool
{
    @Inject
    private NumberedCounterService numberedCounterService;

    @Inject
    private Logger logger;

    @Override
    public String getFigureEnvironment(Block figureBlock)
    {
        Optional<FigureType> optionalType = this.typedFigureTool.getType(figureBlock);
        String figureEnvironment;
        if (optionalType.isPresent()) {
            try {
                FigureType type = optionalType.get();
                if (type.isInline()) {
                    figureEnvironment = type.getId();
                } else {
                    Map<FigureType, String> computedCounters = this.numberedCounterService.computeCounters();
                    figureEnvironment = computedCounters.get(type);
                    if (figureEnvironment == null) {
                        figureEnvironment = type.getId();
                    }
                }
            } catch (NumberedFiguresException e) {
                this.logger.warn("Failed to get the configured figure environment. "
                    + "Falling back to the default figure type identification. "
                    + "Cause: [{}]", getRootCauseMessage(e));
                figureEnvironment = super.getFigureEnvironment(figureBlock);
            }
        } else {
            // In case of unknown figure type, we fall back to the default figure type identification.
            figureEnvironment = super.getFigureEnvironment(figureBlock);
        }
        return figureEnvironment;
    }
}
