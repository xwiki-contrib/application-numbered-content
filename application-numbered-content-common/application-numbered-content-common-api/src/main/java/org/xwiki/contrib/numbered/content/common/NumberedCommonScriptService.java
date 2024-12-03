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
package org.xwiki.contrib.numbered.content.common;

import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.common.internal.CounterInitializationCSSMerge;
import org.xwiki.contrib.numbered.content.common.internal.MapToCssConverter;
import org.xwiki.script.service.ScriptService;
import org.xwiki.skinx.SkinExtension;
import org.xwiki.stability.Unstable;

/**
 * Provides the CSS and cache key for the common numbering counters initialization.
 *
 * @version $Id$
 * @since 1.10.3
 */
@Component
@Named(NumberedScriptService.ROLE_HINT + ".common")
@Singleton
@Unstable
public class NumberedCommonScriptService implements ScriptService
{
    @Inject
    private CounterInitializationCSSMerge counterInitializationCSSMerge;

    @Inject
    private MapToCssConverter converter;

    @Inject
    @Named("ssx")
    private SkinExtension ssx;

    /**
     * @param locale the current locale
     * @return the merged css from the {@link CSSCountersProvider}s
     */
    public String css(Locale locale)
    {
        return getMergedCSS(locale);
    }

    /**
     * @param locale the current locale
     * @return the computed hash of the {@link #css(Locale)}
     */
    public String hash(Locale locale)
    {
        return DigestUtils.md5Hex(getMergedCSS(locale)).toLowerCase();
    }

    public void insertCSS(Locale locale) {
        /*
        #set($ssxHrefCommon = $commondoc.getURL('ssx', $escapetool.url({
    'hash': $services.numbered.common.hash($locale)
  })))
         */

        this.ssx.use("NumberedCommon.Code.NumberedCommon", Map.of("hash", hash(locale)));
    }

    private String getMergedCSS(Locale locale)
    {
        return this.converter.convert(this.counterInitializationCSSMerge.merge(locale));
    }
}
