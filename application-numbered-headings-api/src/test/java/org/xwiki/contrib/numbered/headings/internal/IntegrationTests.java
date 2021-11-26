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

import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.localization.Translation;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.test.integration.TestDataParser;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link TestDataParser}.
 *
 * @version $Id$
 * @since 1.0
 */
@AllComponents
public class IntegrationTests implements RenderingTests
{
    @RenderingTests.Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        ContextualLocalizationManager contextualLocalizationManager =
            componentManager.registerMockComponent(ContextualLocalizationManager.class);

        Translation translation1 = mock(Translation.class);

        when(translation1.render(any(Integer.class))).thenAnswer(invocation -> {
            int number = invocation.getArgument(0);
            return new CompositeBlock(asList(new WordBlock("Figure"), new SpaceBlock(),
                new WordBlock(String.valueOf(number)), new SpecialSymbolBlock(':')));
        });

        when(contextualLocalizationManager.getTranslation("transformation.numberedReferences.figurePrefix"))
            .thenReturn(translation1);

        Translation translation2 = mock(Translation.class);
        when(translation2.render(any(Integer.class))).thenAnswer(invocation -> {
            int number = invocation.getArgument(0);
            return new CompositeBlock(asList(new WordBlock("Table"), new SpaceBlock(),
                new WordBlock(String.valueOf(number)), new SpecialSymbolBlock(':')));
        });
        when(contextualLocalizationManager.getTranslation("transformation.numberedReferences.tablePrefix")).thenReturn(
            translation2);

        when(componentManager.<NumberedHeadingsService>registerMockComponent(NumberedHeadingsService.class)
            .isNumberedHeadingsEnabled()).thenReturn(true);
    }
}
