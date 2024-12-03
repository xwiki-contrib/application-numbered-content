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

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.numbered.content.common.CSSCountersProvider;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test of {@link CounterInitializationCSSMerge}.
 *
 * @version $Id$
 */
@ComponentTest
class CounterInitializationCSSMergeTest
{
    @InjectMockComponents
    private CounterInitializationCSSMerge merger;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @BeforeComponent
    void beforeComponent() throws Exception
    {
        this.componentManager.registerComponent(ComponentManager.class, "context", this.componentManager);
    }

    @Test
    void noProvider()
    {
        assertEquals(Map.of(), merger.merge(Locale.ENGLISH));
    }

    @Test
    void oneProvider() throws Exception
    {
        CSSCountersProvider p1 =
            componentManager.registerMockComponent(CSSCountersProvider.class, "p1");

        when(p1.selectors(Locale.ENGLISH)).thenReturn(Set.of("c1"));

        Map<String, Map<String, Map<String, String>>> selectorA = Map.of(
            "", Map.of("#xwikicontent, body[contenteditable=\"true\"]", Map.of("counter-reset", "c1"))
        );
        assertEquals(selectorA, merger.merge(Locale.ENGLISH));
    }

    @Test
    void twoProviders() throws Exception
    {
        CSSCountersProvider p1 =
            componentManager.registerMockComponent(CSSCountersProvider.class, "p1");
        CSSCountersProvider p2 =
            componentManager.registerMockComponent(CSSCountersProvider.class, "p2");

        when(p1.selectors(Locale.ENGLISH)).thenReturn(Set.of("c1"));
        when(p2.selectors(Locale.ENGLISH)).thenReturn(Set.of("c2"));

        Map<String, Map<String, Map<String, String>>> map = Map.of(
            "", Map.of("#xwikicontent, body[contenteditable=\"true\"]", Map.of("counter-reset", "c1 c2"))
        );
        assertEquals(map, merger.merge(Locale.ENGLISH));
    }

    @Test
    void twoProvidersDifferentCSSSelectors() throws Exception
    {
        CSSCountersProvider p1 =
            componentManager.registerMockComponent(CSSCountersProvider.class, "p1");
        CSSCountersProvider p2 =
            componentManager.registerMockComponent(CSSCountersProvider.class, "p2");

        when(p1.selectors(Locale.ENGLISH)).thenReturn(Set.of("c1"));
        when(p2.selectors(Locale.ENGLISH)).thenReturn(Set.of("c2"));

        Map<String, Map<String, Map<String, String>>> map = Map.of(
            "", Map.of("#xwikicontent, body[contenteditable=\"true\"]", Map.of("counter-reset", "c1 c2"))
        );
        assertEquals(map, merger.merge(Locale.ENGLISH));
    }
}
