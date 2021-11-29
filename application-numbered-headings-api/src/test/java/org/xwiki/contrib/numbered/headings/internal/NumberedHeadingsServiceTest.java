package org.xwiki.contrib.numbered.headings.internal;/*
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static com.xpn.xwiki.XWikiContext.EXECUTIONCONTEXT_KEY;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.numbered.headings.internal.NumberedHeadingsClassDocumentInitializer.REFERENCE;
import static org.xwiki.contrib.numbered.headings.internal.NumberedHeadingsClassDocumentInitializer.STATUS_ACTIVATED;
import static org.xwiki.contrib.numbered.headings.internal.NumberedHeadingsClassDocumentInitializer.STATUS_DEACTIVATED;
import static org.xwiki.contrib.numbered.headings.internal.NumberedHeadingsClassDocumentInitializer.STATUS_INHERITS;
import static org.xwiki.contrib.numbered.headings.internal.NumberedHeadingsClassDocumentInitializer.STATUS_PROPERTY;

/**
 * Test of {@link NumberedHeadingsService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class NumberedHeadingsServiceTest
{
    public static final DocumentReference DOCUMENT_REFERENCE_PAGE =
        new DocumentReference("xwiki", asList("Space0", "Space1"), "Page");

    public static final DocumentReference DOCUMENT_REFERENCE_SPACE_1 =
        new DocumentReference("xwiki", "Space0", "Space1");

    @InjectMockComponents
    private NumberedHeadingsService numberedHeadingsService;

    @MockComponent
    private DocumentAccessBridge documentAccessBridge;

    @MockComponent
    private Execution execution;

    @Mock
    private XWikiDocument xWikiDocumentPage;

    @Mock
    private XWikiDocument xWikiDocumentSpace1;

    @Mock
    private ExecutionContext context;

    @Mock
    private XWikiContext xWikiContext;

    @BeforeEach
    void setUp() throws Exception
    {
        when(this.xWikiDocumentPage.getParentReference()).thenReturn(DOCUMENT_REFERENCE_SPACE_1);
        when(this.documentAccessBridge.getDocumentInstance(DOCUMENT_REFERENCE_PAGE)).thenReturn(this.xWikiDocumentPage);
        when(this.documentAccessBridge.getDocumentInstance(DOCUMENT_REFERENCE_SPACE_1))
            .thenReturn(this.xWikiDocumentSpace1);
        when(this.execution.getContext()).thenReturn(this.context);
        when(this.context.getProperty(EXECUTIONCONTEXT_KEY)).thenReturn(this.xWikiContext);
        when(this.xWikiContext.getDoc()).thenReturn(this.xWikiDocumentPage);
        when(this.xWikiDocumentPage.getDocumentReference()).thenReturn(DOCUMENT_REFERENCE_PAGE);
    }

    @Test
    void isNumberedHeadingsEnabled() throws Exception
    {
        BaseObject baseObject = mock(BaseObject.class);
        when(this.xWikiDocumentPage.getXObject(REFERENCE)).thenReturn(null);
        when(this.xWikiDocumentSpace1.getXObject(REFERENCE)).thenReturn(baseObject);
        when(baseObject.getStringValue(STATUS_PROPERTY)).thenReturn(STATUS_ACTIVATED);
        assertTrue(this.numberedHeadingsService.isNumberedHeadingsEnabled());
    }

    @Test
    void isNumberedHeadingsEnabledXWikiContextMissing() throws Exception
    {
        when(this.context.getProperty(EXECUTIONCONTEXT_KEY)).thenReturn(null);
        assertFalse(this.numberedHeadingsService.isNumberedHeadingsEnabled());
    }

    @Test
    void isNumberedBridgeException() throws Exception
    {
        NullPointerException nullPointerException = new NullPointerException();
        when(this.documentAccessBridge.getDocumentInstance(DOCUMENT_REFERENCE_PAGE))
            .thenThrow(nullPointerException);
        Exception exception =
            assertThrows(Exception.class, () -> this.numberedHeadingsService.isNumberedHeadingsEnabled());
        assertSame(nullPointerException, exception);
    }

    @ParameterizedTest
    @CsvSource({ STATUS_ACTIVATED + ",true", STATUS_DEACTIVATED + ",false", STATUS_INHERITS + ",false" })
    void isNumberedHeadingsEnabledOnParent(String status, boolean expected) throws Exception
    {
        BaseObject baseObject = mock(BaseObject.class);
        when(this.xWikiDocumentPage.getXObject(REFERENCE)).thenReturn(null);
        when(this.xWikiDocumentSpace1.getXObject(REFERENCE)).thenReturn(baseObject);
        when(baseObject.getStringValue(STATUS_PROPERTY)).thenReturn(status);
        assertEquals(expected, this.numberedHeadingsService.isNumberedHeadingsEnabled());
    }

    @Test
    void isNumberedHeadingsEnabledNoXObject() throws Exception
    {
        when(this.xWikiDocumentPage.getXObject(REFERENCE)).thenReturn(null);
        when(this.xWikiDocumentSpace1.getXObject(REFERENCE)).thenReturn(null);
        assertFalse(this.numberedHeadingsService.isNumberedHeadingsEnabled());
    }

    @Test
    void isNumberedHeadingsEnabledDocIsNull() throws Exception
    {
        when(this.xWikiContext.getDoc()).thenReturn(null);
        assertFalse(this.numberedHeadingsService.isNumberedHeadingsEnabled());
    }
}