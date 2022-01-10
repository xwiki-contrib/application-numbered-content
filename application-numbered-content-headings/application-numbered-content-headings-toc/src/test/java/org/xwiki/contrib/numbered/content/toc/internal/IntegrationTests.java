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
package org.xwiki.contrib.numbered.content.toc.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.contrib.numbered.content.HeaderNumberingService;
import org.xwiki.contrib.numbered.content.headings.NumberedHeadingsConfiguration;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.test.integration.TestDataParser;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link TestDataParser}.
 * <p>
 * Run integration tests of the ToC Macro when the numbering is deactivated.
 *
 * @version $Id$
 * @since 1.0
 */
@AllComponents
@RenderingTests.Scope(value = "toc/")
public class IntegrationTests implements RenderingTests
{
    @Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        NumberedHeadingsConfiguration numberedHeadingService =
            componentManager.registerMockComponent(NumberedHeadingsConfiguration.class);
        when(numberedHeadingService.isNumberedHeadingsEnabled()).thenReturn(false);
        HeaderNumberingService headerNumberingService =
            componentManager.registerMockComponent(HeaderNumberingService.class, "headings");
        when(headerNumberingService.getHeadersMap(any())).thenAnswer(invocation -> {
            List<HeaderBlock> blocks = invocation.<Block>getArgument(0)
                .getBlocks(new ClassBlockMatcher(HeaderBlock.class), Block.Axes.DESCENDANT);
            Map<HeaderBlock, String> ret = new HashMap<>();
            int i = 0;
            for (HeaderBlock block : blocks) {
                ret.put(block, String.valueOf(i));
                i++;
            }
            return ret;
        });
        when(headerNumberingService.getHeadersList(any())).thenAnswer(invocation -> invocation.<Block>getArgument(0)
            .getBlocks(new ClassBlockMatcher(HeaderBlock.class), Block.Axes.DESCENDANT));
    }
}
