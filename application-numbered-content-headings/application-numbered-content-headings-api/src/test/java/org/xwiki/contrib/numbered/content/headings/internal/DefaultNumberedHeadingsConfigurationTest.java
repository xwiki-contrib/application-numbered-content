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
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static com.xpn.xwiki.XWikiContext.EXECUTIONCONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link DefaultNumberedHeadingsConfiguration}.
 *
 * @version $Id$
 * @since 1.2
 */
@ComponentTest
class DefaultNumberedHeadingsConfigurationTest
{
    @InjectMockComponents
    private DefaultNumberedHeadingsConfiguration numberedHeadingsConfiguration;

    @MockComponent
    private DocumentAccessBridge documentAccessBridge;

    @MockComponent
    private Execution execution;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private XWikiContext xWikiContext;

    @Mock
    private XWikiDocument doc;

    @BeforeEach
    void setUp()
    {
        when(this.execution.getContext()).thenReturn(this.executionContext);
        when(this.executionContext.getProperty(EXECUTIONCONTEXT_KEY)).thenReturn(this.xWikiContext);
        when(this.xWikiContext.getDoc()).thenReturn(this.doc);
    }

    @Test
    void isNumberedHeadingsEnabled() throws Exception
    {
        BaseObject baseObject = mock(BaseObject.class);
        when(this.doc.getXObject(NumberedHeadingsClassDocumentInitializer.REFERENCE)).thenReturn(baseObject);
        when(baseObject.getStringValue(NumberedHeadingsClassDocumentInitializer.STATUS_PROPERTY))
            .thenReturn("deactivated");
        boolean numberedHeadingsEnabled = this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled();
        assertFalse(numberedHeadingsEnabled);
        verify(this.doc, never()).getDocumentReference();
    }

    
}