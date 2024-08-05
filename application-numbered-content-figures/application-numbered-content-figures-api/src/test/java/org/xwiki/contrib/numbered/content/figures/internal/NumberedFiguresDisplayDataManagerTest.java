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
package org.xwiki.contrib.numbered.content.figures.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.contrib.figure.FigureStyle;
import org.xwiki.contrib.figure.FigureType;
import org.xwiki.contrib.numbered.content.figures.NumberedFiguresException;
import org.xwiki.contrib.numbered.content.headings.script.NumberedFigureDisplayData;
import org.xwiki.localization.LocalizationManager;
import org.xwiki.localization.Translation;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ComponentTest
class NumberedFiguresDisplayDataManagerTest
{
    @InjectMockComponents
    private NumberedFiguresDisplayDataManager displayDataManager;

    @MockComponent
    private LocalizationManager localizationManager;

    @MockComponent
    private NumberedFiguresConfiguration numberedFiguresConfiguration;

    private final Locale locale = Locale.FRENCH;

    @BeforeEach
    void setUp()
    {
        when(this.localizationManager.getTranslationPlain(any(), eq(this.locale), any()))
            .then(invocationOnMock -> {
                String result = invocationOnMock.getArgument(0);
                if (invocationOnMock.getArguments().length > 2) {
                    return result + " " + invocationOnMock.getArgument(2);
                }
                return result;
            });
    }

    @Test
    void noFigureTypes() throws NumberedFiguresException
    {
        when(this.numberedFiguresConfiguration.getFigureCounters()).thenReturn(Map.of());
        when(this.localizationManager.getTranslation("org.xwiki.rendering.macro.figure.FigureType.figure", this.locale))
            .thenReturn(mock(Translation.class));
        NumberedFigureDisplayData figureDisplayData = this.displayDataManager.getFigureDisplayData(this.locale);
        for (String key : List.of(
            "numbered.figures.caption.block.separator",
            "numbered.figures.caption.inline.start",
            "numbered.figures.caption.inline.end"
        )) {
            assertEquals(key, figureDisplayData.getTranslation(key));
        }

        assertEquals("ckeditor.plugin.macro.placeholder figure", figureDisplayData.getFigurePlaceholder());
        assertEquals("ckeditor.plugin.macro.placeholder figureCaption", figureDisplayData.getFigureCaptionPlaceholder());
        assertTrue(figureDisplayData.getFigureCounters().isEmpty());
        assertEquals("org.xwiki.rendering.macro.figure.FigureType.figure", figureDisplayData.getFigureTypeLabel("figure"));
    }

    @Test
    void someFigureTypes() throws NumberedFiguresException
    {
        Map<String, Set<FigureType>> figureCounters = Map.of(
            "figure", Set.of(FigureType.FIGURE),
            "math", Set.of(new FigureType("proof", FigureStyle.INLINE), new FigureType("lemma", FigureStyle.INLINE))
        );
        when(this.localizationManager.getTranslation("org.xwiki.rendering.macro.figure.FigureType.LEMMA", this.locale))
            .thenReturn(mock(Translation.class));
        when(this.numberedFiguresConfiguration.getFigureCounters()).thenReturn(figureCounters);

        NumberedFigureDisplayData figureDisplayData = this.displayDataManager.getFigureDisplayData(this.locale);
        assertEquals(figureCounters, figureDisplayData.getFigureCounters());
        // Verify that the fallback to both the uppercase translation key and the plain label works.
        assertEquals("Proof", figureDisplayData.getFigureTypeLabel("proof"));
        assertEquals("org.xwiki.rendering.macro.figure.FigureType.LEMMA", figureDisplayData.getFigureTypeLabel("lemma"));
        assertEquals("Figure", figureDisplayData.getFigureTypeLabel("figure"));
    }
}