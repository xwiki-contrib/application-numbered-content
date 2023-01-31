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
package org.xwiki.contrib.numbered.content.headings.pdf.internal.macro;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.contrib.numbered.content.toc.TocEntriesResolver;
import org.xwiki.contrib.numbered.content.toc.TocEntryDecorator;
import org.xwiki.contrib.numbered.content.toc.TocTreeBuilder;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.internal.macro.toc.TocBlockFilter;
import org.xwiki.rendering.internal.macro.toc.TreeParameters;
import org.xwiki.rendering.listener.HeaderLevel;

import static org.xwiki.export.pdf.internal.job.DocumentRenderer.PARAMETER_DOCUMENT_REFERENCE;

/**
 * Extends {@link TocTreeBuilder} in order to distinguish between headings that correspond to document titles and
 * headings that are part of the document content. This is useful when multiple pages are exported to PDF and the PDF
 * export has been configured to output also the document titles (default behavior).
 * <p>
 * Note that this class is a copy of {@code org.xwiki.export.pdf.internal.macro.PDFTocTreeBuilder}.
 * TODO: remove this class once the minimal parent-platform of this module includes XRENDERING-704.
 *
 * @version $Id: bcc8c163d88e183ddf0dc53fa4d9c534293dbe0f $
 * @since 1.8
 */
public class PDFTocTreeBuilder extends TocTreeBuilder
{
    /**
     * Initialize a PDF table of content tree builder.
     *
     * @param tocBlockFilter the filter to use to generate the toc anchors
     * @param tocEntriesResolver the resolver to use to find the entries in a given {@link Block}
     * @param decorators the decorators that will be called on each toc entry, allowing to add additional
     *     information on the toc entries
     */

    public PDFTocTreeBuilder(TocBlockFilter tocBlockFilter, TocEntriesResolver tocEntriesResolver,
        List<TocEntryDecorator> decorators)
    {
        super(tocBlockFilter, tocEntriesResolver, decorators);
    }

    @Override
    protected ListItemBlock createTocEntry(HeaderBlock headerBlock, TreeParameters parameters)
    {
        ListItemBlock listItem = super.createTocEntry(headerBlock, parameters);
        if (HeaderLevel.LEVEL1.equals(headerBlock.getLevel())
            && !StringUtils.isEmpty(headerBlock.getParameter(PARAMETER_DOCUMENT_REFERENCE)))
        {
            // The given header block corresponds to a document title, so it marks the beginning of a new document, when
            // multiple documents are exported to PDF. We want to be able to style the corresponding TOC entry
            // differently.
            listItem.setParameter(PARAMETER_DOCUMENT_REFERENCE, headerBlock.getParameter(PARAMETER_DOCUMENT_REFERENCE));
        }
        return listItem;
    }
}

