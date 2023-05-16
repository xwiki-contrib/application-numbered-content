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
package org.xwiki.contrib.numbered.content.headings.internal.macro;

import org.xwiki.contrib.figure.internal.FigureTypesConfiguration;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.skinx.SkinExtension;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.figure.FigureStyle.BLOCK;

/**
 * @version $Id$
 * @since 1.6
 */
@AllComponents
public class IntegrationTests implements RenderingTests
{
    @Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        FigureTypesConfiguration figureTypesConfiguration =
            componentManager.registerMockComponent(FigureTypesConfiguration.class);
        when(figureTypesConfiguration.getFigureStyle(any())).thenReturn(BLOCK);
        componentManager.registerMockComponent(SkinExtension.class, "ssx");
    }
}
