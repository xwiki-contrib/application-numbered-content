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

import org.xwiki.component.annotation.Role;
import org.xwiki.component.manager.ComponentLookupException;

/**
 * Provide the operations to build a {@link TocTreeBuilder}, possibly based on an hint used to select the headings
 * resolver.
 * TODO: remove this class once the minimal parent-platform of this module includes XRENDERING-704.
 *
 * @version $Id$
 * @since 1.8
 */
@Role
public interface TocTreeBuilderFactory
{
    /**
     * @return a toc tree builder using the default {@link TocEntriesResolver}
     * @throws ComponentLookupException in case of error when accessing the {@link TocEntriesResolver} or
     *     {@link TocEntryDecorator} instances
     */
    default TocTreeBuilder build() throws ComponentLookupException
    {
        return build(null);
    }

    /**
     * @return a toc tree builder using the a {@link TocEntriesResolver} with hint {@code resolverHint}
     * @param resolverHint the hint to use to resolve the {@link TocEntriesResolver}
     * @throws ComponentLookupException in case of error when accessing the {@link TocEntriesResolver} or
     *     {@link TocEntryDecorator} instances
     */
    TocTreeBuilder build(String resolverHint) throws ComponentLookupException;
}
