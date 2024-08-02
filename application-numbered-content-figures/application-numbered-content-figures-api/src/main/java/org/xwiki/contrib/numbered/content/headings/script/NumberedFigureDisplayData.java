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
package org.xwiki.contrib.numbered.content.headings.script;

import java.util.Map;
import java.util.Set;

import org.xwiki.contrib.figure.FigureType;
import org.xwiki.stability.Unstable;

/**
 * The data that is needed to display the numbered figures.
 *
 * @version $Id$
 * @since 1.10.2
 */
@Unstable
public class NumberedFigureDisplayData
{
    private final Map<String, String> translations;

    private final String figurePlaceholder;

    private final String figureCaptionPlaceholder;

    private final Map<String, String> figureTypeLabels;

    private final Map<String, Set<FigureType>> figureCounters;

    /**
     * Constructor.
     *
     * @param translations the translations that are needed to display the numbered figures
     * @param figurePlaceholder the placeholder in CKEditor for the figure macro
     * @param figureCaptionPlaceholder the placeholder in CKEditor for the figure caption macro
     * @param figureTypeLabels the labels for the configured figure types and the default "figure" type
     * @param figureCounters the configured counters with the figure types they should be used for
     */
    public NumberedFigureDisplayData(Map<String, String> translations, String figurePlaceholder,
        String figureCaptionPlaceholder,
        Map<String, String> figureTypeLabels, Map<String, Set<FigureType>> figureCounters)
    {
        this.translations = translations;
        this.figurePlaceholder = figurePlaceholder;
        this.figureCaptionPlaceholder = figureCaptionPlaceholder;
        this.figureTypeLabels = figureTypeLabels;
        this.figureCounters = figureCounters;
    }

    /**
     * @param key the key of the translation
     * @return the translation for the given key
     */
    public String getTranslation(String key)
    {
        return this.translations.get(key);
    }

    /**
     * @return the placeholder in CKEditor for the figure macro
     */
    public String getFigurePlaceholder()
    {
        return this.figurePlaceholder;
    }

    /**
     * @return the placeholder in CKEditor for the figure caption macro
     */
    public String getFigureCaptionPlaceholder()
    {
        return figureCaptionPlaceholder;
    }

    /**
     * @param figureType the figure type
     * @return the label for the given figure type
     */
    public String getFigureTypeLabel(String figureType)
    {
        return this.figureTypeLabels.get(figureType);
    }

    /**
     * @return the configured counters with the figure types they should be used for
     */
    public Map<String, Set<FigureType>> getFigureCounters()
    {
        return figureCounters;
    }
}
