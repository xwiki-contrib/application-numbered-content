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

import java.util.Locale;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.common.CSSCountersProvider;

/**
 * Provider for the CSS counters used for headings numbering.
 *
 * @version $Id$
 * @since 1.10.3
 */
@Component
@Singleton
@Named("headings")
public class HeadingsCSSCountersProvider implements CSSCountersProvider
{
    @Override
    public Set<String> selectors(Locale locale)
    {
        return Set.of();
    }

    @Override
    public Set<String> selectorsCounterSet(Locale locale)
    {
        return Set.of("nh1", "nh2", "nh3", "nh4", "nh5", "nh6");
    }
}
