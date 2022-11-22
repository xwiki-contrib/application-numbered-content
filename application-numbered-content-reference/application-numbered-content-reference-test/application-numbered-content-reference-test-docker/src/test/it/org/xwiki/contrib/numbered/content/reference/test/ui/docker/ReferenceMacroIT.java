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
package org.xwiki.contrib.numbered.content.reference.test.ui.docker;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.ViewPage;
import org.xwiki.test.ui.po.editor.ClassEditPage;
import org.xwiki.test.ui.po.editor.ObjectEditPage;
import org.xwiki.test.ui.po.editor.ObjectEditPane;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests of the reference macro.
 *
 * @version $Id$
 * @since 1.6.1
 */
@UITest
class ReferenceMacroIT
{
    @BeforeEach
    void setUp(TestUtils setup)
    {
        setup.loginAsSuperAdmin();
    }

    @Test
    void referenceFigureOnContextMacro(TestUtils setup, TestReference testReference)
    {
        setup.deletePage(testReference);
        setup.createPage(testReference, "= {{id name=\"test\"/}} H1A =\n"
            + "\n"
            + "{{context document=\"Main.WebHome\" transformation=\"current\"}}\n"
            + "Section {{reference section=\"test\"/}} is very important.\n"
            + "Section {{reference section=\"test2\"/}} is very important.\n"
            + "{{/context}}\n"
            + "\n"
            + "= {{id name=\"test2\"/}} H1B =\n"
            + "\n"
            + "{{context document=\"Main.WebHome\" transformation=\"current\"}}\n"
            + "Section {{reference section=\"test\"/}} is very important.\n"
            + "Section {{reference section=\"test2\"/}} is very important.\n"
            + "{{/context}}");
        List<WebElement> headers = setup.getDriver().findElements(By.cssSelector("#xwikicontent h1"));
        assertEquals(2, headers.size());
        assertEquals("HH1A", headers.get(0).getAttribute("id"));
        assertEquals("HH1B", headers.get(1).getAttribute("id"));
        List<WebElement> links = setup.getDriver().findElements(By.cssSelector("#xwikicontent .wikilink a"));
        assertEquals(4, links.size());
        String url = setup.getURL(testReference.getLastSpaceReference());
        assertEquals(url + "/#test", links.get(0).getAttribute("href"));
        assertEquals(url + "/#test2", links.get(1).getAttribute("href"));
        assertEquals(url + "/#test", links.get(2).getAttribute("href"));
        assertEquals(url + "/#test2", links.get(3).getAttribute("href"));
        assertEquals("1", links.get(0).getText());
        assertEquals("2", links.get(1).getText());
        assertEquals("1", links.get(2).getText());
        assertEquals("2", links.get(3).getText());
    }

    @Test
    void referenceFigureOnXObjectProperty(TestUtils setup, TestReference testReference)
    {
        DocumentReference xClassDocumentReference =
            new DocumentReference("XClass", testReference.getLastSpaceReference());
        DocumentReference xObjectDocumentReference =
            new DocumentReference("XObject", testReference.getLastSpaceReference());
        setup.deletePage(xClassDocumentReference);
        setup.deletePage(xObjectDocumentReference);
        // Create an XClass.
        ViewPage xClassPage = setup.createPage(xClassDocumentReference, "");
        ClassEditPage classEditPage = xClassPage.editClass();
        classEditPage.addProperty("propertyA", "TextArea");
        classEditPage.addProperty("propertyB", "TextArea");
        classEditPage.clickSaveAndView();
        // Create an XObject.
        ViewPage xObjectPage = setup.createPage(xObjectDocumentReference, String.format("%s\n" +
            "\n" +
            "{{velocity}} \n" +
            "$doc.display('propertyA')\n" +
            "$doc.display('propertyB')\n"
            + "{{/velocity}}\n"
            + "\n"
            + "%s", generateFigureSnippet(3), generateFigureSnippet(4)));
        ObjectEditPage objectEditPage = xObjectPage.editObjects();
        ObjectEditPane objectEditPane =
            objectEditPage.addObject(setup.serializeReference(xClassDocumentReference.getLocalDocumentReference()));
        objectEditPane.setPropertyValue("propertyA", generateFigureSnippet(1));
        objectEditPane.setPropertyValue("propertyB", generateFigureSnippet(2));
        objectEditPage.clickSaveAndView();

        // Check the rendering of the reference links.
        List<WebElement> links = setup.getDriver().findElements(By.cssSelector("#xwikicontent .wikilink a"));
        assertEquals(4, links.size());
        String url = setup.getURL(xObjectDocumentReference);
        assertEquals(url + "#T3", links.get(0).getAttribute("href"));
        assertEquals(url + "#T1", links.get(1).getAttribute("href"));
        assertEquals(url + "#T2", links.get(2).getAttribute("href"));
        assertEquals(url + "#T4", links.get(3).getAttribute("href"));
        assertEquals("3", links.get(0).getText());
        assertEquals("1", links.get(1).getText());
        assertEquals("2", links.get(2).getText());
        assertEquals("4", links.get(3).getText());
    }

    @Test
    void referenceHeadingOnXObjectProperty(TestUtils setup, TestReference testReference)
    {
        DocumentReference xClassDocumentReference =
            new DocumentReference("XClass", testReference.getLastSpaceReference());
        DocumentReference xObjectDocumentReference =
            new DocumentReference("XObject", testReference.getLastSpaceReference());
        setup.deletePage(xClassDocumentReference);
        setup.deletePage(xObjectDocumentReference);
        // Create an XClass.
        ViewPage xClassPage = setup.createPage(xClassDocumentReference, "");
        ClassEditPage classEditPage = xClassPage.editClass();
        classEditPage.addProperty("propertyA", "TextArea");
        classEditPage.addProperty("propertyB", "TextArea");
        classEditPage.clickSaveAndView();
        // Create an XObject.
        ViewPage xObjectPage = setup.createPage(xObjectDocumentReference, String.format("%s\n" +
            "\n" +
            "{{velocity}} \n" +
            "$doc.display('propertyA')\n" +
            "$doc.display('propertyB')\n"
            + "{{/velocity}}\n"
            + "\n"
            + "%s", generateHeaderSnippet(3), generateHeaderSnippet(4)));
        ObjectEditPage objectEditPage = xObjectPage.editObjects();
        ObjectEditPane objectEditPane =
            objectEditPage.addObject(setup.serializeReference(xClassDocumentReference.getLocalDocumentReference()));
        objectEditPane.setPropertyValue("propertyA", generateHeaderSnippet(1));
        objectEditPane.setPropertyValue("propertyB", generateHeaderSnippet(2));
        objectEditPage.clickSaveAndView();

        // Check the rendering of the reference links.
        List<WebElement> links = setup.getDriver().findElements(By.cssSelector("#xwikicontent .wikilink a"));
        assertEquals(4, links.size());
        String url = setup.getURL(xObjectDocumentReference);
        assertEquals(url + "#H3", links.get(0).getAttribute("href"));
        assertEquals(url + "#H1", links.get(1).getAttribute("href"));
        assertEquals(url + "#H2", links.get(2).getAttribute("href"));
        assertEquals(url + "#H4", links.get(3).getAttribute("href"));
        assertEquals("3", links.get(0).getText());
        assertEquals("1", links.get(1).getText());
        assertEquals("2", links.get(2).getText());
        assertEquals("4", links.get(3).getText());
    }

    private static String generateFigureSnippet(int i)
    {
        return String.format("Below in table {{reference figure=\"T%1$d\"/}}\n" +
            "\n" +
            "{{figure}}\n" +
            "{{figureCaption}}\n" +
            "{{id name=\"T%1$d\"/}}Caption of Table %1$d\n"
            + "{{/figureCaption}}\n"
            + "\n"
            + "|Cell of table %1$d\n"
            + "{{/figure}}", i);
    }

    private static String generateHeaderSnippet(int i)
    {
        return String.format("Below in header {{reference figure=\"H%1$d\"/}}\n" +
            "\n" +
            "(%% id='H%1$d' %%)\n" +
            "= Header %1$d =", i);
    }
}
