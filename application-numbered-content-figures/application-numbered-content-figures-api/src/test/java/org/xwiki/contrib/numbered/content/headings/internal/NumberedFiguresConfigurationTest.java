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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.test.MockitoOldcore;
import com.xpn.xwiki.test.junit5.mockito.InjectMockitoOldcore;
import com.xpn.xwiki.test.junit5.mockito.OldcoreTest;
import com.xpn.xwiki.web.XWikiRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test of {@link NumberedFiguresConfiguration}.
 *
 * @version $Id$
 * @since x.y.z
 */
@OldcoreTest
@AllComponents
class NumberedFiguresConfigurationTest
{
    @InjectMockComponents
    private NumberedFiguresConfiguration numberedFiguresConfiguration;

    @InjectMockitoOldcore
    private MockitoOldcore oldcore;

    private XWikiDocument document;

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
}
