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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.headings.script.NumberedFigureDisplayData;
import org.xwiki.localization.LocalizationManager;

/**
 * Numbered Figures Display Data Manager.
 *
 * @version $Id$
 * @since 1.8.5
 */
@Component(roles = NumberedFiguresDisplayDataManager.class)
@Singleton
public class NumberedFiguresDisplayDataManager
{
    private static final List<String> TRANSLATION_KEYS = List.of(
        "numbered.figures.numbered.figureCaption.label",
        "numbered.figures.numbered.tableCaption.label"
    );

    private static final String FIGURE = "figure";

    private static final String FIGURE_CAPTION = "figureCaption";

    private static final String PLACEHOLDER_TRANSLATION_KEY = "ckeditor.plugin.macro.placeholder";

    @Inject
    private LocalizationManager localizationManager;

    /**
     * Get the data needed to display the numbered figures.
     *
     * @param locale the locale to use
     * @return the data needed to display the numbered figures
     */
    public NumberedFigureDisplayData getFigureDisplayData(Locale locale)
    {
        Map<String, String> translations = TRANSLATION_KEYS.stream()
            .collect(Collectors.toUnmodifiableMap(Function.identity(),
                key -> this.localizationManager.getTranslationPlain(key, locale)));


        String figurePlaceholder = this.localizationManager.getTranslationPlain(PLACEHOLDER_TRANSLATION_KEY, locale,
            FIGURE);
        String figureCaptionPlaceholder = this.localizationManager.getTranslationPlain(PLACEHOLDER_TRANSLATION_KEY,
            locale, FIGURE_CAPTION);

        return new NumberedFigureDisplayData(translations, figurePlaceholder, figureCaptionPlaceholder);
    }
}
