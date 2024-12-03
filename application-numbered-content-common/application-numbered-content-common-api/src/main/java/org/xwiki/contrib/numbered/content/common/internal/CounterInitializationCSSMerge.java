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
package org.xwiki.contrib.numbered.content.common.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.numbered.content.common.CSSCountersProvider;

/**
 * Merge the values from the {@link CSSCountersProvider}s.
 *
 * @version $Id$
 * @since 1.10.3
 */
@Component(roles = CounterInitializationCSSMerge.class)
@Singleton
public class CounterInitializationCSSMerge
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Logger logger;

    /**
     * Merge the values from the {@link CSSCountersProvider}s.
     *
     * @param locale the current locale
     * @return merge the values from the {@link CSSCountersProvider}s
     */
    public Map<String, Map<String, Map<String, String>>> merge(Locale locale)
    {
        Map<String, Map<String, Map<String, String>>> map = new HashMap<>();
        Map<String, Map<String, String>> baseSelectors = mergeSelectors(p -> p.selectors(locale));
        if (!baseSelectors.isEmpty()) {
            map.put("", baseSelectors);
        }
        Map<String, Map<String, String>> mediaQuerySelectors = mergeSelectors(p -> p.selectorsCounterSet(locale));
        if (!mediaQuerySelectors.isEmpty()) {
            map.put("@supports (counter-set:nh1)", mediaQuerySelectors);
        }
        return map;
    }

    private Map<String, Map<String, String>> mergeSelectors(
        Function<CSSCountersProvider, Set<String>> selectorLambda)
    {
        try {
            String counters = componentManagerProvider.get()
                .<CSSCountersProvider>getInstanceList(CSSCountersProvider.class)
                .stream()
                .map(selectorLambda)
                .flatMap(Collection::stream)
                .collect(Collectors.joining(" "));

            if (counters.isEmpty()) {
                return Map.of();
            }
            return Map.of(
                "#xwikicontent, body[contenteditable=\"true\"]", Map.of("counter-reset", counters)
            );
        } catch (ComponentLookupException e) {
            this.logger.error("Failed to lookup [{}] components", CSSCountersProvider.class, e);
            return Map.of();
        }
    }
}
