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
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.contrib.numbered.content.headings.NumberedHeadingsConfiguration;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test of {@link NumberingTocEntryDecorator}.
 *
 * @version $Id$
 * @since 1.8
 */
@ComponentTest
class NumberingTocEntryDecoratorTest
{
    @InjectMockComponents
    private NumberingTocEntryDecorator decorator;

    @MockComponent
    private NumberedHeadingsConfiguration numberedHeadingsConfiguration;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @Test
    void isNumbered() throws Exception
    {
        when(this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled()).thenReturn(true);
        assertTrue(this.decorator.isNumbered(null));
    }

    @Test
    void isNumberedFalse() throws Exception
    {
        when(this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled()).thenReturn(false);
        assertFalse(this.decorator.isNumbered(null));
    }

    @Test
    void isNumberedError() throws Exception
    {
        when(this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled()).thenThrow(Exception.class);
        assertFalse(this.decorator.isNumbered(null));
        assertEquals("Cannot check if numbered headings are enabled. Cause: [Exception: ]", this.logCapture.getMessage(0));
        assertEquals(Level.WARN, this.logCapture.getLogEvent(0).getLevel());
    }
}
