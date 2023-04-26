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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.contrib.figure.FigureType;
import org.xwiki.contrib.figure.internal.DefaultFigureTypesConfiguration;
import org.xwiki.contrib.figure.internal.FigureTypesConfigurationSource;
import org.xwiki.contrib.numbered.content.figures.internal.NumberedFiguresClassDocumentInitializer;
import org.xwiki.contrib.numbered.content.figures.internal.NumberedFiguresConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.Converter;
import org.xwiki.properties.internal.converter.ListConverter;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.doc.DefaultDocumentAccessBridge;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.test.MockitoOldcore;
import com.xpn.xwiki.test.junit5.mockito.InjectMockitoOldcore;
import com.xpn.xwiki.test.junit5.mockito.OldcoreTest;
import com.xpn.xwiki.test.reference.ReferenceComponentList;
import com.xpn.xwiki.web.XWikiRequest;

import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.figure.FigureType.FIGURE;
import static org.xwiki.contrib.figure.FigureType.TABLE;

/**
 * Test of {@link NumberedFiguresConfiguration}.
 *
 * @version $Id$
 * @since 1.8.3
 */
@OldcoreTest
@ReferenceComponentList
@ComponentList({
    DefaultFigureTypesConfiguration.class,
    ListConverter.class,
    DefaultDocumentAccessBridge.class
})
class NumberedFiguresConfigurationTest
{
    @InjectMockComponents
    private NumberedFiguresConfiguration numberedFiguresConfiguration;

    @InjectMockitoOldcore
    private MockitoOldcore oldcore;

    @MockComponent
    @Named(FigureTypesConfigurationSource.HINT)
    private ConfigurationSource objectConfigurationSource;

    @MockComponent
    private ConverterManager converterManager;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.DEBUG);

    private XWikiDocument document;

    private Converter<List<?>> converter;

    @BeforeComponent
    public void configure() throws Exception
    {
        this.converter = mock(Converter.class);
        when(this.converterManager.<List<?>>getConverter(List.class)).thenReturn(this.converter);
        when(this.converter.convert(any(), any())).thenAnswer(
            invocation -> Arrays.stream(invocation.<String>getArgument(1).split(",")).collect(Collectors.toList()));
    }

    @BeforeEach
    void setUp() throws Exception
    {
        this.document = this.oldcore.getSpyXWiki()
            .getDocument(new DocumentReference("xwiki", "Space", "Page"), this.oldcore.getXWikiContext());
        this.oldcore.getXWikiContext().setDoc(this.document);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void isNumberedFiguresEnabled(boolean configuration) throws Exception
    {
        BaseObject xObject = this.document.newXObject(NumberedFiguresClassDocumentInitializer.REFERENCE,
            this.oldcore.getXWikiContext());
        xObject.setStringValue(NumberedFiguresClassDocumentInitializer.STATUS_PROPERTY,
            configuration ? "activated" : "");

        assertEquals(configuration, this.numberedFiguresConfiguration.isNumberedFiguresEnabled());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void isNumberedFiguresEnabledWithParam(boolean parameter) throws Exception
    {
        XWikiRequest request = mock(XWikiRequest.class);
        this.oldcore.getXWikiContext().setRequest(request);
        when(request.getParameter("enableNumberedFigures")).thenReturn(Boolean.toString(parameter));
        assertEquals(parameter, this.numberedFiguresConfiguration.isNumberedFiguresEnabled());
    }

    @Test
    void getFigureCountersNoOverride()
    {
        Map<String, Set<FigureType>> figureCounters = this.numberedFiguresConfiguration.getFigureCounters();
        assertEquals(Map.of("figure", Set.of(FIGURE), "table", Set.of(TABLE)), figureCounters);
        assertEquals("Mandatory type [figure] added.", this.logCapture.getMessage(0));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(0).getLevel());
        assertEquals("Mandatory type [table] added.", this.logCapture.getMessage(1));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(1).getLevel());
    }

    @Test
    void getFigureCountersIgnoredUnknown()
    {
        Properties countersProperties = new Properties();
        countersProperties.put("math", "lemma,proof");
        this.oldcore.getConfigurationSource().setProperty("numbered.figures.counters", countersProperties);
        Map<String, Set<FigureType>> figureCounters = this.numberedFiguresConfiguration.getFigureCounters();
        assertEquals(Map.of("figure", Set.of(FIGURE), "table", Set.of(TABLE)), figureCounters);
        assertEquals("Mandatory type [figure] added.", this.logCapture.getMessage(0));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(0).getLevel());
        assertEquals("Mandatory type [table] added.", this.logCapture.getMessage(1));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(1).getLevel());
    }

    @Test
    void getFigureCountersWithCounter()
    {
        Properties countersProperties = new Properties();
        countersProperties.put("math", "lemma,proof");
        this.oldcore.getConfigurationSource().setProperty("numbered.figures.counters", countersProperties);
        this.oldcore.getConfigurationSource().setProperty("figure.types", List.of("lemma", "proof"));
        Map<String, Set<FigureType>> figureCounters = this.numberedFiguresConfiguration.getFigureCounters();
        assertEquals(Map.of(
            "figure", Set.of(FIGURE),
            "table", Set.of(TABLE),
            "math", Set.of(new FigureType("lemma"), new FigureType("proof"))
        ), figureCounters);
        assertEquals("Mandatory type [figure] added.", this.logCapture.getMessage(0));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(0).getLevel());
        assertEquals("Mandatory type [table] added.", this.logCapture.getMessage(1));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(1).getLevel());
    }

    @Test
    void getFigureCountersWithDuplicatedCounter()
    {
        Properties countersProperties = new Properties();
        countersProperties.put("math", "lemma,proof");
        countersProperties.put("proof", "proof");
        this.oldcore.getConfigurationSource().setProperty("numbered.figures.counters", countersProperties);
        this.oldcore.getConfigurationSource().setProperty("figure.types", List.of("lemma", "proof"));
        Map<String, Set<FigureType>> figureCounters = this.numberedFiguresConfiguration.getFigureCounters();
        assertEquals(Map.of(
            "figure", Set.of(FIGURE),
            "table", Set.of(TABLE),
            "math", Set.of(new FigureType("lemma"), new FigureType("proof"))
        ), figureCounters);

        assertEquals("Mandatory type [figure] added.", this.logCapture.getMessage(0));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(0).getLevel());
        assertEquals("Mandatory type [table] added.", this.logCapture.getMessage(1));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(1).getLevel());
        assertEquals("Type [proof] appears in more than one counter [[math, proof]]. Taking the first one.",
            this.logCapture.getMessage(2));
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(2).getLevel());
    }
}
