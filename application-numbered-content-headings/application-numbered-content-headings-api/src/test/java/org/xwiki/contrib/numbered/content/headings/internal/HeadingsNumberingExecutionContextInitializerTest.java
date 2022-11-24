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
package org.xwiki.contrib.numbered.content.headings.internal;

import org.junit.jupiter.api.Test;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.xwiki.contrib.numbered.content.headings.internal.HeadingsNumberingExecutionContextInitializer.PROPERTY_KEY;

/**
 * Test of {@link HeadingsNumberingExecutionContextInitializer}.
 *
 * @version $Id$
 * @since 1.7
 */
@ComponentTest
class HeadingsNumberingExecutionContextInitializerTest
{
    @InjectMockComponents
    private HeadingsNumberingExecutionContextInitializer initializer;

    @Test
    void initialize()
    {
        ExecutionContext context = new ExecutionContext();
        this.initializer.initialize(context);
        assertEquals(HeadingNumberingCalculator.class, context.getProperty(PROPERTY_KEY).getClass());
    }

    @Test
    void initializeAlreadyInitialized()
    {
        ExecutionContext context = new ExecutionContext();
        HeadingNumberingCalculator calculator = new HeadingNumberingCalculator();
        context.setProperty(PROPERTY_KEY, calculator);
        this.initializer.initialize(context);
        assertSame(calculator, context.getProperty(PROPERTY_KEY));
    }
}
