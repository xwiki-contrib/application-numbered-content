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
package org.xwiki.contrib.numbered.content.toc.internal;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.numbered.content.toc.TocMacroParameters;
import org.xwiki.contrib.numbered.content.toc.TocTreeBuilder;
import org.xwiki.contrib.numbered.content.toc.TocTreeBuilderFactory;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.macro.toc.TreeParameters;
import org.xwiki.rendering.internal.macro.toc.TreeParametersBuilder;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.rendering.wiki.WikiModelException;
import org.xwiki.skinx.SkinExtension;

import static org.xwiki.rendering.macro.toc.TocMacroParameters.Scope.PAGE;

/**
 * Replace the {@link org.xwiki.rendering.internal.macro.toc.TocMacro} with a new implementation where numbered headings
 * numbers are displayed in the ToC.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("toc")
@Singleton
public class TocMacro extends AbstractMacro<TocMacroParameters>
{
    private static final String DESCRIPTION = "Generates a Table Of Contents.";

    @Inject
    private Provider<WikiModel> wikiModelProvider;

    private TocTreeBuilder tocTreeBuilder;

    @Inject
    private TocTreeBuilderFactory tocTreeBuilderFactory;

    @Inject
    @Named("ssrx")
    private SkinExtension ssfx;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public TocMacro()
    {
        super("Table Of Contents", DESCRIPTION, TocMacroParameters.class);

        // Make sure this macro is executed as one of the last macros to be executed since
        // other macros can generate headers which need to be taken into account by the TOC
        // macro.
        setPriority(2000);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_NAVIGATION));
    }

    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();
        try {
            this.tocTreeBuilder = this.tocTreeBuilderFactory.build();
        } catch (ComponentLookupException e) {
            throw new InitializationException(String.format("Failed to initialize [%s]", TocTreeBuilder.class), e);
        }
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(TocMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        this.ssfx.use("toc.css");
        Block rootBlock = getRootBlockBlock(parameters);

        TreeParametersBuilder builder = new TreeParametersBuilder();
        TreeParameters treeParameters = builder.build(rootBlock, parameters, context);
        return this.tocTreeBuilder.build(treeParameters);
    }

    private Block getRootBlockBlock(TocMacroParameters parameters) throws MacroExecutionException
    {
        Block rootBlock = null;
        WikiModel wikiModel = this.wikiModelProvider.get();
        if (parameters.getReference() != null) {
            if (wikiModel != null) {
                // Remote TOC always has a PAGE scope since a LOCAL scope would have no meaning
                parameters.setScope(PAGE);
                // Get the referenced source's XDOM
                rootBlock = this.getXDOM(new DocumentResourceReference(parameters.getReference()), wikiModel);
            } else {
                throw new MacroExecutionException(
                    "The \"reference\" parameter can only be used when a WikiModel implementation is available");
            }
        }
        return rootBlock;
    }

    private XDOM getXDOM(DocumentResourceReference reference, WikiModel wikiModel) throws MacroExecutionException
    {
        try {
            return wikiModel.getXDOM(reference);
        } catch (WikiModelException e) {
            throw new MacroExecutionException(String.format("Failed to get XDOM for [%s]", reference), e);
        }
    }
}
