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

import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService;
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
import static org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService.SKIP_PARAMETER;
import static org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService.START_PARAMETER;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link TestDataParser}.
 * <p>
 * Run integration tests of the ToC Macro when the numbering is activated.
 *
 * @version $Id$
 * @since 1.0
 */
@AllComponents
@RenderingTests.Scope(value = "toc_numbered/")
public class NumberedIntegrationTests implements RenderingTests
{
    @Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        componentManager.registerComponent(ComponentManager.class, "context",
            componentManager.getInstance(ComponentManager.class));

        NumberedHeadingsConfiguration numberedHeadingService =
            componentManager.registerMockComponent(NumberedHeadingsConfiguration.class);
        when(numberedHeadingService.isNumberedHeadingsEnabled()).thenReturn(true);
        HeadingsNumberingService headingsNumberingService =
            componentManager.registerMockComponent(HeadingsNumberingService.class, "headings");
        when(headingsNumberingService.getHeadingsMap(any())).thenAnswer(invocation -> {
            List<HeaderBlock> blocks = invocation.<Block>getArgument(0)
                .getBlocks(new ClassBlockMatcher(HeaderBlock.class), Block.Axes.DESCENDANT);
            Map<HeaderBlock, String> ret = new HashMap<>();
            int i = 0;
            for (HeaderBlock block : blocks) {
                if (block.getParameter(SKIP_PARAMETER) == null) {
                    String startParam = block.getParameter(START_PARAMETER);
                    if (startParam != null) {
                        i = Integer.parseInt(startParam);
                    }
                    ret.put(block, String.valueOf(i));
                    i++;
                } else {
                    ret.put(block, null);
                }
            }
            return ret;
        });
        when(headingsNumberingService.getHeadingsList(any())).thenAnswer(invocation -> invocation.<Block>getArgument(0)
            .getBlocks(new ClassBlockMatcher(HeaderBlock.class), Block.Axes.DESCENDANT));
    }
}
