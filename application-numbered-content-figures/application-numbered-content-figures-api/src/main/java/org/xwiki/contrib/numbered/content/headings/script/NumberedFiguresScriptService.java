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

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.headings.internal.NumberedFiguresConfiguration;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * Numbered Figures Script Service. Provides operations related to numbered headings, such as knowing if the current
 * page should be numbered.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("numberedfigures")
@Singleton
@Unstable
public class NumberedFiguresScriptService implements ScriptService
{
    @Inject
    private NumberedFiguresConfiguration numberedFiguresConfiguration;

    @Inject
    private Provider<XWikiContext> contextProvider;

    /**
     * Checks if the current document has numbered headings activated.
     *
     * @return {@code true} if the current document has numbered headings activated, {@code false} otherwise.
     * @throws Exception in case of error when checking if the current document has numbered headings activated.
     */
    // TODO: change the thrown exception. 
    public boolean isNumberedFiguresEnabled() throws Exception
    {
        XWikiRequest request = this.contextProvider.get().getRequest();
        boolean isNumberedFiguresEnabled;
        String enableNumberedFiguresParam = request.getParameter("enableNumberedFigures");
        // Bypass the configuration if enableNumberedFigures has the value "true" in the request.
        if (enableNumberedFiguresParam != null) {
            isNumberedFiguresEnabled = Objects.equals(enableNumberedFiguresParam, "true");
        } else {
            isNumberedFiguresEnabled = this.numberedFiguresConfiguration.isNumberedFiguresEnabled();
        }
        return isNumberedFiguresEnabled;
    }

    /**
     * Checks if the parent of the current document has numbered headings activated.
     *
     * @return {@code true} if the current document has numbered headings activated, {@code false} otherwise.
     * @throws Exception in case of error when checking if the current document has numbered headings activated.
     */

    public boolean isNumberedFiguresEnabledOnParent() throws Exception
    {
        return this.numberedFiguresConfiguration.isNumberedFiguresEnabledOnParent();
    }
}
