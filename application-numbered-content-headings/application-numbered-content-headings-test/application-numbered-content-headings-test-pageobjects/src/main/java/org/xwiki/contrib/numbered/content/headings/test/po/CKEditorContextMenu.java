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
package org.xwiki.contrib.numbered.content.headings.test.po;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.xwiki.test.ui.XWikiWebDriver;
import org.xwiki.test.ui.po.BaseElement;

/**
 * The context menu of a CKEditor instance, opened with the keyboard at the caret position of the editing area,
 * optionally after placing the caret in a given element.
 *
 * @version $Id$
 * @since 1.11.1
 */
public class CKEditorContextMenu extends BaseElement
{
    private static final By PANEL_FRAME = By.cssSelector(".cke_menu_panel iframe.cke_panel_frame");

    private static final By ENTRY = By.cssSelector("a.cke_menubutton");

    private final String editorName;

    private WebElement panelFrame;

    /**
     * @param editorName the name of the CKEditor instance whose context menu is used
     */
    public CKEditorContextMenu(String editorName)
    {
        this.editorName = editorName;
    }

    /**
     * Places the caret in the innermost element of the editing area whose text is the given text, opens the context
     * menu with the keyboard (Shift+F10) and waits for it to be displayed.
     *
     * @param elementText the text of the element
     * @return this context menu
     */
    public CKEditorContextMenu open(String elementText)
    {
        return open(() -> getDriver().findElement(By.xpath(
            String.format("//body//*[normalize-space(.)='%1$s'][not(.//*[normalize-space(.)='%1$s'])]", elementText)))
            .click());
    }

    /**
     * Opens the context menu with the keyboard (Shift+F10) at the current caret position of the editing area, and
     * waits for it to be displayed.
     *
     * @return this context menu
     */
    public CKEditorContextMenu open()
    {
        return open(() -> { });
    }

    private CKEditorContextMenu open(Runnable placeCaret)
    {
        XWikiWebDriver driver = getDriver();
        WebElement editingFrame = (WebElement) driver.executeScript(
            "return CKEDITOR.instances[arguments[0]].ui.contentsElement.find('iframe').getItem(0).$;",
            this.editorName);
        driver.switchTo().frame(editingFrame);
        try {
            placeCaret.run();
            driver.switchTo().activeElement().sendKeys(Keys.chord(Keys.SHIFT, Keys.F10));
        } finally {
            driver.switchTo().defaultContent();
        }
        this.panelFrame = wait(driver).until(d -> d.findElements(PANEL_FRAME).stream()
            .filter(WebElement::isDisplayed).findFirst().orElse(null));
        return this;
    }

    /**
     * @return the labels of the entries displayed in the context menu
     */
    public List<String> getEntries()
    {
        return inPanel(() -> getDriver().findElementsWithoutWaiting(ENTRY).stream()
            .filter(WebElement::isDisplayed)
            .map(entry -> entry.getAttribute("title"))
            .collect(Collectors.toList()));
    }

    /**
     * Clicks the entry with the given label and waits for the context menu to be hidden.
     *
     * @param label the label of the entry to click
     */
    public void clickEntry(String label)
    {
        inPanel(() -> {
            getDriver().findElementsWithoutWaiting(ENTRY).stream()
                .filter(entry -> label.equals(entry.getAttribute("title")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No context menu entry with label [" + label + "]"))
                .click();
            return null;
        });
        wait(getDriver()).until(ExpectedConditions.invisibilityOf(this.panelFrame));
    }

    private <T> T inPanel(Supplier<T> action)
    {
        getDriver().switchTo().frame(this.panelFrame);
        try {
            return action.get();
        } finally {
            getDriver().switchTo().defaultContent();
        }
    }

    private static WebDriverWait wait(XWikiWebDriver driver)
    {
        return new WebDriverWait(driver, Duration.ofSeconds(driver.getTimeout()));
    }
}
