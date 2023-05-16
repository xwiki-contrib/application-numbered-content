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
package org.xwiki.contrib.numbered.content.figures.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.contrib.figure.FigureStyle;
import org.xwiki.contrib.figure.FigureType;
import org.xwiki.contrib.figure.internal.FigureTypesConfiguration;
import org.xwiki.contrib.numbered.content.figures.NumberedFiguresException;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.Converter;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static com.xpn.xwiki.XWikiContext.EXECUTIONCONTEXT_KEY;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.contrib.figure.FigureType.AUTOMATIC;
import static org.xwiki.contrib.numbered.content.figures.internal.NumberedFiguresConfigurationClassDocumentInitializer.COUNTER;
import static org.xwiki.contrib.numbered.content.figures.internal.NumberedFiguresConfigurationClassDocumentInitializer.TYPES;

/**
 * Provides services related to the numbered headings. For instance, to know if a given document has the numbered
 * headings activated.
 *
 * @version $Id$
 * @since 1.0
 */
@Component(roles = NumberedFiguresConfiguration.class)
@Singleton
public class NumberedFiguresConfiguration implements Initializable
{
    @Inject
    private DocumentAccessBridge documentAccessBridge;

    @Inject
    private Execution execution;

    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configurationSource;

    @Inject
    private FigureTypesConfiguration figureTypesConfiguration;

    @Inject
    private ConverterManager converterManager;

    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    private Converter<List<?>> listConverter;

    @Override
    public void initialize()
    {
        this.listConverter = this.converterManager.getConverter(List.class);
    }

    /**
     * Check if the current document has numbered headings activated either by looking at the presence of an XObject of
     * type {@link NumberedFiguresClassDocumentInitializer#STATUS_PROPERTY}.
     *
     * @return @return {@code true} if the numbered headings are activated, {@code false} otherwise
     * @throws Exception in case of error when access the document instance though the document bridge
     * @see #isNumbered(EntityReference)
     */
    public boolean isNumberedFiguresEnabled() throws Exception
    {
        boolean isNumberedFiguresEnabled;
        String enableNumberedFiguresParam = getEnableNumberedFiguresRequestParameter();
        // Bypass the configuration if enableNumberedFigures has the value "true" in the request.
        if (enableNumberedFiguresParam != null) {
            isNumberedFiguresEnabled = Objects.equals(enableNumberedFiguresParam, "true");
        } else {
            isNumberedFiguresEnabled = internalIsNumberedFiguresEnabled();
        }
        return isNumberedFiguresEnabled;
    }

    /**
     * @return a map of counters and their associated types (e.g.,
     *     {@code Map.of("figure", Set.of("figure"), "math", Set.of("proof", "lemma"))})
     * @since 1.9
     */
    @Unstable
    public Map<String, Set<FigureType>> getFigureCounters() throws NumberedFiguresException
    {
        Map<String, Set<String>> configurationMap = getCountersConfigurationMap();
        Map<String, Set<FigureType>> counters = new HashMap<>();
        for (FigureType figureType : this.figureTypesConfiguration.getFigureTypes()) {
            if (isAutomatic(figureType)) {
                continue;
            }
            counters.merge(computeCounter(configurationMap, figureType.getId()), Set.of(figureType), SetUtils::union);
        }

        return counters;
    }

    /**
     * Resolve the counter for a given figure type.
     *
     * @param type a figure type (e.g., {@code "proof"})
     * @return the resolved counter (e.g., "math")
     * @since 1.9
     */
    @Unstable
    public String getCounter(String type) throws NumberedFiguresException
    {
        return computeCounter(getCountersConfigurationMap(), type);
    }

    /**
     * Restrictions:
     * <ul>
     *     <li>the "figure" and "table" types cannot be part of a custom counter.</li>
     *     <li>a custom counter cannot be shared between figure of different styles
     *      (e.g., between "inline" and "block" figures)</li>
     * </ul>
     *
     * @return the configuration map of the counters (e.g.,
     *     {@code Map.of("math", Set.of("proof, "lemma"), "blocks", Set.of("graph", "plot"))})
     */
    private Map<String, Set<String>> getCountersConfigurationMap() throws NumberedFiguresException
    {
        Map<String, Set<String>> configurationMap = getCountersConfigurationMapFromXObjects();

        if (configurationMap.isEmpty()) {
            configurationMap = getCountersConfigurationMapFromProperties();
        }

        String table = FigureType.TABLE.getId();
        String figure = FigureType.FIGURE.getId();

        checkNoReserverCounterId(configurationMap, figure);
        checkNoReserverCounterId(configurationMap, table);

        for (Map.Entry<String, Set<String>> entry : configurationMap.entrySet()) {

            String counterId = entry.getKey();
            checkNoReseverdTypeInFiguresList(entry, counterId, figure);

            checkNoReseverdTypeInFiguresList(entry, counterId, table);

            checkUniformStylesInFiguresList(entry, counterId);
        }

        return configurationMap;
    }

    private void checkUniformStylesInFiguresList(Map.Entry<String, Set<String>> entry, String counterId)
        throws NumberedFiguresException
    {

        boolean hasInlineStyles = false;
        boolean hasBlockStyles = false;

        for (String filteredValue : entry.getValue()) {
            FigureStyle figureStyle = this.figureTypesConfiguration.getFigureStyle(filteredValue);
            if (figureStyle == FigureStyle.BLOCK) {
                hasBlockStyles = true;
            } else if (figureStyle == FigureStyle.INLINE) {
                hasInlineStyles = true;
            }

            // As soon as we find both block and inline styles in the same counter, we fail the configuration check.
            if (hasInlineStyles && hasBlockStyles) {
                throw new NumberedFiguresException(String.format(
                    "Figure counters miss-configured counter [%s] shares counters of different styles [%s]", counterId,
                    StringUtils.join(entry.getValue(), ", ")));
            }
        }
    }

    private static void checkNoReserverCounterId(Map<String, Set<String>> configurationMap, String table)
        throws NumberedFiguresException
    {
        if (configurationMap.containsKey(table)) {
            throw new NumberedFiguresException(
                "Figure counters miss-configured, the counter id [" + table + "] is reserved");
        }
    }

    private static void checkNoReseverdTypeInFiguresList(Map.Entry<String, Set<String>> entry, String counterId,
        String figure)
        throws NumberedFiguresException
    {
        if (entry.getValue().stream().anyMatch(it -> Objects.equals(it, figure))) {
            throw new NumberedFiguresException(
                "Figure counters miss-configured, counter [" + counterId + "] contains a figure of type [" + figure
                    + "].");
        }
    }

    private Map<String, Set<String>> getCountersConfigurationMapFromXObjects()
    {
        LocalDocumentReference reference =
            new LocalDocumentReference(asList("NumberedFigures", "Code"), "NumberedFiguresCounterConfiguration");
        Map<String, Set<String>> map = new HashMap<>();
        try {
            XWikiContext xWikiContext = this.xWikiContextProvider.get();
            if (xWikiContext != null) {
                XWikiDocument document = xWikiContext.getWiki().getDocument(reference, xWikiContext);
                List<BaseObject> xObjects =
                    document.getXObjects(NumberedFiguresConfigurationClassDocumentInitializer.REFERENCE);
                xObjects.forEach(xObject -> map.put(xObject.getStringValue(COUNTER),
                    ((List<?>) xObject.getListValue(TYPES)).stream().map(String::valueOf).collect(Collectors.toSet())));
            }
        } catch (XWikiException e) {
            this.logger.warn("Failed to load document [{}]. Cause: [{}]", reference, getRootCauseMessage(e));
        }
        return map;
    }

    private Map<String, Set<String>> getCountersConfigurationMapFromProperties()
    {
        Map<String, Set<String>> configurationMap = new HashMap<>();
        Properties properties = this.configurationSource.getProperty("numbered.figures.counters", Properties.class);

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            configurationMap.put((String) entry.getKey(),
                new HashSet<>(this.listConverter.convert(List.class, entry.getValue())));
        }
        return configurationMap;
    }

    private boolean internalIsNumberedFiguresEnabled() throws Exception
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

    /**
     * Check if the parent of the current document has numbered headings activated either by looking at the presence of
     * an XObject of type {@link NumberedFiguresClassDocumentInitializer#STATUS_PROPERTY}. When the current document
     * does not have the parent, {@code false} is returned
     *
     * @return @return {@code true} if the current document has a parent, and numbered headings are activated on the
     *     parent, {@code false} otherwise
     * @throws Exception in case of error when access the document instance though the document bridge
     * @see #isNumbered(EntityReference)
     */
    public boolean isNumberedFiguresEnabledOnParent() throws Exception
    {
        XWikiDocument doc = getDocFromContext();
        if (doc == null) {
            return false;
        }

        return isNumbered(doc.getDocumentReference().getParent());
    }

    private XWikiDocument getDocFromContext()
    {
        XWikiContext property = getXWikiContext();
        if (property == null) {
            return null;
        }
        return property.getDoc();
    }

    /**
     * Checks if a document has numbered headings activated by looking at the presence of an XObject of type
     * {@link NumberedFiguresClassDocumentInitializer#STATUS_PROPERTY}.
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
                Optional<Boolean> isNumbered = isNumbered(actualDoc);
                if (isNumbered.isPresent()) {
                    return isNumbered.get();
                }
                currentReference = currentReference.getParent();
            } while (currentReference != null);
        }
        return false;
    }

    private Optional<Boolean> isNumbered(XWikiDocument actualDoc)
    {
        BaseObject xObject = actualDoc.getXObject(NumberedFiguresClassDocumentInitializer.REFERENCE);
        // We stop as soon as we find an object.
        Optional<Boolean> isNumbered = Optional.empty();
        if (xObject != null) {
            String activatePropertyValue =
                xObject.getStringValue(NumberedFiguresClassDocumentInitializer.STATUS_PROPERTY);
            // If the value is inherits, we continue looking up the hierarchy, otherwise we use the configured 
            // activation setting.
            if (!Objects.equals(activatePropertyValue, "")) {
                isNumbered = Optional.of(Objects.equals(activatePropertyValue, "activated"));
            }
        }
        return isNumbered;
    }

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty(EXECUTIONCONTEXT_KEY);
    }

    private String getEnableNumberedFiguresRequestParameter()
    {
        String enableNumberedFiguresParam;
        XWikiContext xWikiContext = getXWikiContext();
        if (xWikiContext != null && xWikiContext.getRequest() != null) {
            enableNumberedFiguresParam = xWikiContext.getRequest().getParameter("enableNumberedFigures");
        } else {
            enableNumberedFiguresParam = null;
        }
        return enableNumberedFiguresParam;
    }

    private static boolean isAutomatic(FigureType figureType)
    {
        return Objects.equals(figureType.getId(), AUTOMATIC.getId());
    }

    private String computeCounter(Map<String, Set<String>> configurationMap, String figureType)
    {
        List<String> collect = configurationMap
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().contains(figureType))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        String computedCounter;
        if (collect.size() > 1) {
            this.logger.debug("Type [{}] appears in more than one counter [{}]. Taking the first one.", figureType,
                collect);
            computedCounter = collect.get(0);
        } else if (collect.size() == 1) {
            computedCounter = collect.get(0);
        } else {
            // If no counter is configured for a given
            computedCounter = figureType;
        }
        return computedCounter;
    }
}
