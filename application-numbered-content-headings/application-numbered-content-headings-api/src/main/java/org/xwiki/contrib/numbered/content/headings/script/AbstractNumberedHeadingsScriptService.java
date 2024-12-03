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
package org.xwiki.contrib.numbered.content.headings.script;

import javax.inject.Inject;

import org.xwiki.contrib.numbered.content.headings.NumberedHeadingsConfiguration;
import org.xwiki.script.service.ScriptService;

/**
 * Numbered Headings Script Service. Provides operations related to numbered headings, such as knowing if the current
 * page should be numbered. The class is currently abstract to have two implementations. One with the deprecated name *
 * {@code numberedheadings}, and one with the new {@code numbered.headings} name. This abstract class can be removed and
 * the implementation moved to {@link NumberedHeadingsScriptService} if {@link DeprecatedNumberedHeadingsScriptService}
 * is removed from the code base.
 *
 * @version $Id$
 * @since 1.10.3
 */
public abstract class AbstractNumberedHeadingsScriptService implements ScriptService
{
    @Inject
    private NumberedHeadingsConfiguration defaultNumberedHeadingsConfiguration;

    /**
     * Checks if the current document has numbered headings activated.
     *
     * @return {@code true} if the current document has numbered headings activated, {@code false} otherwise.
     * @throws Exception in case of error when checking if the current document has numbered headings activated.
     */
    // TODO: change the thrown exception.
    public boolean isNumberedHeadingsEnabled() throws Exception
    {
        return this.defaultNumberedHeadingsConfiguration.isNumberedHeadingsEnabled();
    }

    /**
     * Checks if the parent of the current document has numbered headings activated.
     *
     * @return {@code true} if the current document has numbered headings activated, {@code false} otherwise.
     * @throws Exception in case of error when checking if the current document has numbered headings activated.
     */
    public boolean isNumberedHeadingsEnabledOnParent() throws Exception
    {
        return this.defaultNumberedHeadingsConfiguration.isNumberedHeadingsEnabledOnParent();
    }
}
