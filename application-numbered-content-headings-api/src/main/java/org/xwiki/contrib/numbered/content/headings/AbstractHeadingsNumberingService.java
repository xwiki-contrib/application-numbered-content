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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.contrib.numbered.content.headings.internal.HeadingsNumberingCacheManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.stability.Unstable;

/**
 * Provides a default behaviour for the {@link HeadingsNumberingService} components where the numbered contents are
 * saved in a {@link HeadingsNumberingCacheManager}.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
public abstract class AbstractHeadingsNumberingService implements HeadingsNumberingService
{
    @Inject
    private HeadingsNumberingCacheManager cacheManager;

    @Override
    public List<HeaderBlock> getHeadingsList(Block rootBlock)
    {
        Optional<List<HeaderBlock>> headingsOpt = this.cacheManager.getHeadings(rootBlock);
        List<HeaderBlock> headings;
        if (headingsOpt.isPresent()) {
            headings = headingsOpt.get();
        } else {
            headings = getHeadingsBlocks(rootBlock);
            buildCache(rootBlock, headings);
        }
        return headings;
    }

    @Override
    public Map<HeaderBlock, String> getHeadingsMap(Block rootBlock)
    {
        Optional<Map<HeaderBlock, String>> headingsOpts = this.cacheManager.get(rootBlock);
        Map<HeaderBlock, String> headings;
        if (headingsOpts.isPresent()) {
            headings = headingsOpts.get();
        } else {
            buildCache(rootBlock, getHeadingsBlocks(rootBlock));
            headings = this.cacheManager.get(rootBlock).get();
        }
        return headings;
    }

    /**
     * Return the header blocks that needs to be numbered.
     *
     * @param rootBlock the root block to number
     * @return the list of header blocks to number
     */
    public abstract List<HeaderBlock> getHeadingsBlocks(Block rootBlock);

    private void buildCache(Block rootBlock, List<HeaderBlock> headings)
    {
        Map<HeaderBlock, String> rootBlockCache = new HashMap<>();
        this.cacheManager.put(rootBlock, rootBlockCache, headings);

        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        for (HeaderBlock heading : headings) {
            if (heading.getParameter(SKIP_PARAMETER) == null) {
                cacheHeading(rootBlockCache, stack, heading);
            }
        }
    }

    private void cacheHeading(Map<HeaderBlock, String> rootBlockCache, Deque<Integer> stack, HeaderBlock heading)
    {
        int level = heading.getLevel().getAsInt();
        Integer start = null;
        if (heading.getParameter(START_PARAMETER) != null) {
            start = Integer.parseInt(heading.getParameter(START_PARAMETER));
        }
        if (level == stack.size()) {
            cacheHeadingSameLevel(rootBlockCache, stack, heading, start);
        } else if (level > stack.size()) {
            cacheHeadingLevelIncreases(rootBlockCache, stack, heading, level, start);
        } else {
            cacheHeadingLevelDecreases(rootBlockCache, stack, heading, level, start);
        }
    }

    private void cacheHeadingLevelDecreases(Map<HeaderBlock, String> rootBlockCache, Deque<Integer> stack,
        HeaderBlock heading, int level, Integer start)
    {
        while (stack.size() > level) {
            stack.pop();
        }
        Integer pop = stack.pop();
        if (start != null) {
            stack.push(start);
        } else {
            stack.push(pop + 1);
        }
        rootBlockCache.put(heading, serialize(stack));
    }

    private void cacheHeadingLevelIncreases(Map<HeaderBlock, String> rootBlockCache, Deque<Integer> stack,
        HeaderBlock heading, int level, Integer start)
    {
        for (int i = stack.size(); i < level; i++) {
            if (i != level - 1) {
                stack.push(0);
            } else {
                if (start != null) {
                    stack.push(start);
                } else {
                    stack.push(1);
                }
            }
        }
        rootBlockCache.put(heading, serialize(stack));
    }

    private void cacheHeadingSameLevel(Map<HeaderBlock, String> rootBlockCache, Deque<Integer> stack,
        HeaderBlock heading, Integer start)
    {
        Integer pop = stack.pop();
        if (start != null) {
            stack.push(start);
        } else {
            stack.push(pop + 1);
        }
        rootBlockCache.put(heading, serialize(stack));
    }

    private String serialize(Deque<Integer> stack)
    {
        List<String> ret = new ArrayList<>();
        Iterator<Integer> integerIterator = stack.descendingIterator();
        while (integerIterator.hasNext()) {
            ret.add(integerIterator.next().toString());
        }
        return StringUtils.join(ret, ".");
    }
}
