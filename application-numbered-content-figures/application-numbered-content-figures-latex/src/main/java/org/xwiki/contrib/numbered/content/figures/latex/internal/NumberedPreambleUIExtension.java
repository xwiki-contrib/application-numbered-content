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
package org.xwiki.contrib.numbered.content.figures.latex.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.figure.FigureType;
import org.xwiki.contrib.figure.internal.FigureLabelService;
import org.xwiki.contrib.latex.internal.LaTeXBlockRenderer;
import org.xwiki.contrib.latex.internal.LaTeXTool;
import org.xwiki.contrib.numbered.content.figures.NumberedFiguresException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;
import org.xwiki.rendering.util.ErrorBlockGenerator;
import org.xwiki.uiextension.UIExtension;

/**
 * Generate the preamble for the numbered figure during latex export.
 *
 * @version $Id$
 * @since 1.9
 */
@Component
@Singleton
@Named("org.xwiki.contrib.figure.latex.Preamble.after")
public class NumberedPreambleUIExtension implements UIExtension, Initializable
{
    @Inject
    private FigureLabelService figureLabelService;

    @Inject
    private LaTeXTool latexTool;

    @Inject
    private ErrorBlockGenerator errorBlockGenerator;

    @Inject
    private NumberedCounterService numberedCounterService;

    private Syntax latexSyntax;

    @Inject
    private SyntaxRegistry syntaxRegistry;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.latexSyntax = this.syntaxRegistry.resolveSyntax(LaTeXBlockRenderer.ROLEHINT);
        } catch (ParseException e) {
            throw new InitializationException(
                String.format("Unable to find the [%s] syntax on the registry.", LaTeXBlockRenderer.ROLEHINT), e);
        }
    }

    @Override
    public String getId()
    {
        return "org.xwiki.contrib.figure.latex.Preamble.after";
    }

    @Override
    public String getExtensionPointId()
    {
        return "org.xwiki.contrib.latex.Preamble.after";
    }

    @Override
    public Map<String, String> getParameters()
    {
        return Map.of("order", "1000");
    }

    @Override
    public Block execute()
    {
        Map<FigureType, String> computedCounters;
        try {
            computedCounters = this.numberedCounterService.computeCounters();
        } catch (NumberedFiguresException e) {
            return new GroupBlock(this.errorBlockGenerator.generateErrorBlocks(false,
                "numbered.content.figures.latex.configuration.error",
                "Failed to access the figure counters configuration. Cause: [{}]", null, e));
        }

        String newLine = System.lineSeparator();
        String collect =
            computedCounters.entrySet()
                .stream()
                .map(entry -> computeTypeDeclaration(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                // Sort by value to always have the standalone theorem before the theorems sharing a counter, and the 
                // floating environments last
                // Then, sort by alphabetic order to make the result deterministic
                .sorted(Entry.<String, Integer>comparingByValue().thenComparing(Entry::getKey))
                .map(Entry::getKey)
                .collect(Collectors.joining(newLine));

        return new RawBlock(collect, this.latexSyntax);
    }

    private Entry<String, Integer> computeTypeDeclaration(FigureType type, String counter)
    {
        Entry<String, Integer> typeDeclaration;
        if (type.isInline()) {
            typeDeclaration = handleInline(type.getId(), counter);
        } else {
            typeDeclaration = handleBlock(type.getId(), counter);
        }
        return typeDeclaration;
    }

    private Entry<String, Integer> handleInline(String typeId, String counter)
    {
        Entry<String, Integer> typeDeclaration;
        if (!Objects.equals(typeId, counter)) {
            typeDeclaration = Map.entry(String.format("\\newtheorem{%s}[%s]{%s}", this.latexTool.escape(typeId),
                this.latexTool.escape(counter),
                this.latexTool.escape(this.figureLabelService.getLabel(typeId))), 1);
        } else {
            typeDeclaration = Map.entry(
                String.format("\\newtheorem{%s}{%s}", this.latexTool.escape(typeId),
                    this.latexTool.escape(this.figureLabelService.getLabel(typeId))), 0);
        }
        return typeDeclaration;
    }

    private static Entry<String, Integer> handleBlock(String typeId, String counter)
    {
        if (Objects.equals(typeId, counter)) {
            return Map.entry(String.format("\\DeclareFloatingEnvironment{%s}", typeId), 2);
        } else {
            return null;
        }
    }
}
