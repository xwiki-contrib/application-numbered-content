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
package org.xwiki.contrib.numbered.headings.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

import static java.util.Arrays.asList;

/**
 * Create or update the {@code NumberedHeadings.Code.NumberedHeadingsClass} document with all required information.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("NumberedHeadings.Code.NumberedHeadingsClass")
@Singleton
public class NumberedHeadingsClassDocumentInitializer extends AbstractMandatoryClassInitializer
{
    /**
     * The local reference of the editor binding class.
     */
    public static final LocalDocumentReference REFERENCE =
        new LocalDocumentReference(asList("NumberedHeadings", "Code"), "NumberedHeadingsClass");

    /**
     * The field name of the activated property.
     */
    public static final String ACTIVATED_PROPERTY = "activated";

    /**
     * Default constructor.
     */
    public NumberedHeadingsClassDocumentInitializer()
    {
        super(REFERENCE);
    }

    @Override
    protected void createClass(BaseClass xclass)
    {
        // TODO: localization
        xclass.addBooleanField(ACTIVATED_PROPERTY, "Activated", "checkbox", true);
    }
}
