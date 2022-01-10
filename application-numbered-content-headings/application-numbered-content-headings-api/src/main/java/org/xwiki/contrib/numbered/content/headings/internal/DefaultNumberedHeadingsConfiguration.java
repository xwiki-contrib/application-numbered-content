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

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.contrib.numbered.content.headings.NumberedHeadingsConfiguration;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static com.xpn.xwiki.XWikiContext.EXECUTIONCONTEXT_KEY;

/**
 * Provides services related to the numbered headings. For instance, to know if a given document has the numbered
 * headings activated.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultNumberedHeadingsConfiguration implements NumberedHeadingsConfiguration
{
    @Inject
    private DocumentAccessBridge documentAccessBridge;

    @Inject
    private Execution execution;

    @Override
    public boolean isNumberedHeadingsEnabled() throws Exception
    {
        XWikiDocument doc = getDocFromContext();
        if (doc == null) {
            return false;
        }
        if (isNumbered(doc).orElse(false)) {
            return true;
        }
        return isNumbered(doc.getDocumentReference().getParent());
    }

    @Override
    public boolean isNumberedHeadingsEnabledOnParent() throws Exception
    {
        XWikiDocument doc = getDocFromContext();
        if (doc == null) {
            return false;
        }

        return isNumbered(doc.getDocumentReference().getParent());
    }

    private XWikiDocument getDocFromContext()
    {
        XWikiContext property = (XWikiContext) this.execution.getContext().getProperty(EXECUTIONCONTEXT_KEY);
        if (property == null) {
            return null;
        }
        return property.getDoc();
    }

    /**
     * Checks if a document has numbered headings activated by looking at the presence of an XObject of type {@link
     * NumberedHeadingsClassDocumentInitializer#STATUS_PROPERTY}.
     *
     * @param documentReference the document reference to check
     * @return {@code true} if the numbered headings are activated in the document, {@code false} otherwise
     * @throws Exception in case of error when access the document instance though the document bridge
     */
    private boolean isNumbered(EntityReference documentReference) throws Exception
    {
        if (documentReference != null) {
            EntityReference currentReference = documentReference;
            do {
                XWikiDocument actualDoc =
                    (XWikiDocument) this.documentAccessBridge.getDocumentInstance(currentReference);
                if (actualDoc != null) {
                    Optional<Boolean> isNumbered = isNumbered(actualDoc);
                    if (isNumbered.isPresent()) {
                        return isNumbered.get();
                    }
                }
                currentReference = currentReference.getParent();
            } while (currentReference != null);
        }
        return false;
    }

    private Optional<Boolean> isNumbered(XWikiDocument actualDoc)
    {
        Optional<Boolean> isNumbered = Optional.empty();
        BaseObject xObject = actualDoc.getXObject(NumberedHeadingsClassDocumentInitializer.REFERENCE);
        // We stop as soon as we find an object.
        if (xObject != null) {
            String activatePropertyValue =
                xObject.getStringValue(NumberedHeadingsClassDocumentInitializer.STATUS_PROPERTY);
            // If the value is inherits, we continue looking up the hierarchy, otherwise we use the configured 
            // activation setting.
            if (!Objects.equals(activatePropertyValue, "inherits")) {
                isNumbered = Optional.of(Objects.equals(activatePropertyValue, "activated"));
            }
        }
        return isNumbered;
    }
}
