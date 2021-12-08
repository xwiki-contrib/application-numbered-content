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

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.numbered.content.headings.internal.NumberedHeadingsClassDocumentInitializer;
import org.xwiki.stability.Unstable;

/**
 * Numbered headings configuration API. Provide an operation to know if headings numbering is activated in the current
 * document.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
@Unstable
public interface NumberedHeadingsConfiguration
{
    /**
     * Check if the current document has numbered headings activated either by looking at the presence of an XObject of
     * type {@link NumberedHeadingsClassDocumentInitializer#STATUS_PROPERTY}.
     *
     * @return @return {@code true} if the numbered headings are activated, {@code false} otherwise
     * @throws Exception in case of error when access the document instance though the document bridge
     */
    boolean isNumberedHeadingsEnabled() throws Exception;

    /**
     * Check if the parent of the current document has numbered headings activated either by looking at the presence of
     * an XObject of type {@link NumberedHeadingsClassDocumentInitializer#STATUS_PROPERTY}. When the current document
     * does not have the parent, {@code false} is returned
     *
     * @return @return {@code true} if the current document has a parent, and numbered headings are activated on the
     *     parent, {@code false} otherwise
     * @throws Exception in case of error when access the document instance though the document bridge
     */
    boolean isNumberedHeadingsEnabledOnParent() throws Exception;
}
