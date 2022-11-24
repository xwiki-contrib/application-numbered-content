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

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextInitializer;

/**
 * Initializes a {@link HeadingNumberingCalculator} to store the stack of heading and their corresponding numbers for
 * the whole rendering lifecycle.
 *
 * @version $Id$
 * @since 1.6.1
 */
@Component
@Named(HeadingsNumberingExecutionContextInitializer.PROPERTY_KEY)
@Singleton
public class HeadingsNumberingExecutionContextInitializer implements ExecutionContextInitializer
{
    /**
     * Figure numbering context property key.
     */
    public static final String PROPERTY_KEY = "headingsNumbering";

    @Override
    public void initialize(ExecutionContext context)
    {
        if (!context.hasProperty(PROPERTY_KEY)) {
            context.newProperty(PROPERTY_KEY)
                .inherited()
                .initial(new HeadingNumberingCalculator())
                .declare();
        }
    }
}
