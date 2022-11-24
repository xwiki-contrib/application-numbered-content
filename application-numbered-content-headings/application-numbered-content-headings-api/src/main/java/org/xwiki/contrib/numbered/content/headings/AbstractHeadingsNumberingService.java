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
package org.xwiki.contrib.numbered.content.headings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.xwiki.context.Execution;
import org.xwiki.contrib.numbered.content.headings.internal.HeadingNumberingCalculator;
import org.xwiki.contrib.numbered.content.headings.internal.HeadingsNumberingExecutionContextInitializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.stability.Unstable;

/**
 * Provides a default behaviour for the {@link HeadingsNumberingService} components.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
public abstract class AbstractHeadingsNumberingService implements HeadingsNumberingService
{
    @Inject
    private Execution execution;

    @Override
    public List<HeaderBlock> getHeadingsList(Block rootBlock)
    {
        return getHeadingsBlocks(rootBlock);
    }

    @Override
    public Map<HeaderBlock, String> getHeadingsMap(Block rootBlock)
    {
        Map<HeaderBlock, String> result = new HashMap<>();

        HeadingNumberingCalculator helper =
            (HeadingNumberingCalculator) this.execution.getContext()
                .getProperty(HeadingsNumberingExecutionContextInitializer.PROPERTY_KEY);

        for (HeaderBlock heading : getHeadingsList(rootBlock)) {
            if (heading.getParameter(SKIP_PARAMETER) == null) {
                Integer start = null;
                if (heading.getParameter(START_PARAMETER) != null) {
                    start = Integer.parseInt(heading.getParameter(START_PARAMETER));
                }
                
                result.put(heading, helper.addHeading(heading, start));
            }
        }

        return result;
    }

    /**
     * Return the header blocks that needs to be numbered.
     *
     * @param rootBlock the root block to number
     * @return the list of header blocks to number
     */
    public abstract List<HeaderBlock> getHeadingsBlocks(Block rootBlock);
}
