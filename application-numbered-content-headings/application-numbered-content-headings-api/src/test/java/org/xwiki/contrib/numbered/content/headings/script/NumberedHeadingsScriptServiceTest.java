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

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.xwiki.contrib.numbered.content.headings.NumberedHeadingsConfiguration;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Test of {@link NumberedHeadingsScriptService}.
 *
 * @version $Id$
 * @since 1.8.2
 */
@ComponentTest
class NumberedHeadingsScriptServiceTest
{
    @InjectMockComponents
    private NumberedHeadingsScriptService numberedHeadingsScriptService;

    @MockComponent
    private NumberedHeadingsConfiguration defaultNumberedHeadingsConfiguration;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    @Mock
    private XWikiContext context;

    @Mock
    private XWikiRequest request;

    @BeforeEach
    void setUp()
    {
        when(this.contextProvider.get()).thenReturn(this.context);
        when(this.context.getRequest()).thenReturn(this.request);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void isNumberedHeadingsEnabled(boolean configuration) throws Exception
    {
        when(this.defaultNumberedHeadingsConfiguration.isNumberedHeadingsEnabled()).thenReturn(configuration);
        assertEquals(configuration, this.numberedHeadingsScriptService.isNumberedHeadingsEnabled());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void isNumberedHeadingsEnabledWithParam(boolean parameter) throws Exception
    {
        when(this.request.getParameter("enableNumberedHeadings")).thenReturn(Boolean.toString(parameter));
        assertEquals(parameter, this.numberedHeadingsScriptService.isNumberedHeadingsEnabled());
        verifyNoInteractions(this.defaultNumberedHeadingsConfiguration);
    }
}
