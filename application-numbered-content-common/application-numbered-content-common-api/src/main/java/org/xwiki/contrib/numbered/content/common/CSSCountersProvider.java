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
package org.xwiki.contrib.numbered.content.common;

import java.util.Locale;
import java.util.Set;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Allow content numbering extension to provide the ids of counters they need to see initialized on the root elements of
 * the content without having several CSS rules conflicting with each other. Otherwise, as per CSS rules, one of the
 * rules would arbitrarily override the others, leading to bad numbering for nested content
 *
 * @version $Id$
 * @since 1.10.3
 */
@Role
@Unstable
public interface CSSCountersProvider
{
    /**
     * @param locale the current locale
     * @return a set of selectors, and their rules
     */
    Set<String> selectors(Locale locale);

    /**
     * Similar to {@link #selectors(Locale)} but with for a {@code @supports (counter-set:nh1) {}} media query. By
     * default, reuse the results of {@link #selectors(Locale)}.
     *
     * @param locale the current locale
     * @return the selectors wrapped in a media query
     */
    default Set<String> selectorsCounterSet(Locale locale)
    {
        return selectors(locale);
    }
}
