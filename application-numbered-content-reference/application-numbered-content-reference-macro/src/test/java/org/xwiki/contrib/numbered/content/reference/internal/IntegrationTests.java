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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.xwiki.contrib.numbered.content.headings.internal.DefaultFiguresNumberingService;
import org.xwiki.contrib.numbered.content.headings.internal.HeadingsNumberingService;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.rendering.test.integration.TestDataParser;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link TestDataParser}.
 *
 * @version $Id$
 * @since 1.0
 */
@AllComponents(excludes = { HeadingsNumberingService.class, DefaultFiguresNumberingService.class })
public class IntegrationTests implements RenderingTests
{
    @Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        ContextualLocalizationManager contextualLocalizationManager =
            componentManager.registerMockComponent(ContextualLocalizationManager.class);

        // Mock the translation by retuning the passed key. Optionally with the list of arguments between brackets
        // separated by commas. For instance "my.translation.key [A, B]".
        when(contextualLocalizationManager.getTranslationPlain(any(), any())).thenAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            String key = arguments[0].toString();
            List<String> values = Arrays.stream(arguments).skip(1).map(Object::toString).collect(Collectors.toList());

            String parameters = "";
            if (!values.isEmpty()) {
                parameters = " [" + String.join(", ", values) + "]";
            }

            return key + parameters;
        });
    }
}
