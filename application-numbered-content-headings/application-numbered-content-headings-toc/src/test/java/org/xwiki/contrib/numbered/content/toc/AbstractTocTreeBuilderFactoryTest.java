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
package org.xwiki.contrib.numbered.content.toc;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link AbstractTocTreeBuilderFactory}.
 *
 * @version $Id$
 * @since 1.8
 */
@ComponentTest
class AbstractTocTreeBuilderFactoryTest
{
    @InjectMockComponents
    private TestAbstractTocTreeBuilderFactory factory;

    @MockComponent
    private ComponentManager componentManager;

    @Test
    void getTocEntriesResolverNull() throws Exception
    {
        assertNull(this.factory.getTocEntriesResolver(null));
    }

    @Test
    void getTocEntriesResolverNullKnownComponent() throws Exception
    {
        when(this.componentManager.hasComponent(TocEntriesResolver.class)).thenReturn(true);
        this.factory.getTocEntriesResolver(null);
        verify(this.componentManager).getInstance(TocEntriesResolver.class);
    }

    @Test
    void getTocEntriesResolverWithHint() throws Exception
    {
        when(this.componentManager.hasComponent(TocEntriesResolver.class)).thenReturn(true);
        this.factory.getTocEntriesResolver("testHint");
        verify(this.componentManager).getInstance(TocEntriesResolver.class, "testHint");
    }
}
