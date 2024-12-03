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
package org.xwiki.contrib.numbered.content.common.internal;

import java.util.Map;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Helper to take a CSS datastructure and convert it to a valid CSS string.
 *
 * @version $Id$
 * @since 1.10.3
 */
@Component(roles = MapToCssConverter.class)
@Singleton
public class MapToCssConverter
{
    /**
     * The first level of the map is the empty string, or a string that will be used as a media query.
     * The inner map represents CSS selectors, themselves containing maps of key/values CSS rules.
     *
     * @param map the provided map
     * @return converts the provided map to a set of css selector and their key/value rules
     */
    public String convert(Map<String, Map<String, Map<String, String>>> map)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Map<String, Map<String, String>>> stringMapEntry : map.entrySet()) {
            String key = stringMapEntry.getKey();
            Map<String, Map<String, String>> map2 = stringMapEntry.getValue();
            boolean isEmpty = key.isEmpty();
            if (!isEmpty) {
                stringBuilder.append(key);
                stringBuilder.append("{\n");
            }
            extracted(map2, stringBuilder);
            if (!isEmpty) {
                stringBuilder.append('}');
                stringBuilder.append('\n');
            }
        }

        return stringBuilder.toString();
    }

    private void extracted(Map<String, Map<String, String>> map, StringBuilder stringBuilder)
    {
        for (Map.Entry<String, Map<String, String>> selector : map.entrySet()) {
            stringBuilder.append(selector.getKey());
            stringBuilder.append(" {");
            convert(stringBuilder, selector.getValue());
            stringBuilder.append('}');
            stringBuilder.append('\n');
        }
    }

    private void convert(StringBuilder stringBuilder, Map<String, String> value)
    {
        for (Map.Entry<String, String> rule : value.entrySet()) {
            stringBuilder.append(rule.getKey());
            stringBuilder.append(": ");
            stringBuilder.append(rule.getValue());
            stringBuilder.append(";\n");
        }
    }
}
