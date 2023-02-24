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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.headings.NumberedHeadingsConfiguration;
import org.xwiki.rendering.block.HeaderBlock;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Appends a numbering before each the table of content entry if the current page is numbered.
 *
 * @version $Id$
 * @since 1.8
 */
@Component
@Singleton
@Named(NumberingTocEntryDecorator.HINT)
public class NumberingTocEntryDecorator extends AbstractNumberingTocEntryDecorator
{
    /**
     * The hint for this component.
     */
    public static final String HINT = "numbering";

    @Inject
    private NumberedHeadingsConfiguration numberedHeadingsConfiguration;

    @Inject
    private Logger logger;

    @Override
    protected boolean isNumbered(HeaderBlock headerBlock)
    {
        boolean isNumbered;
        try {
            isNumbered = this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled();
        } catch (Exception e) {
            isNumbered = false;
            this.logger.warn("Cannot check if numbered headings are enabled. Cause: [{}]", getRootCauseMessage(e));
        }
        return isNumbered;
    }
}
