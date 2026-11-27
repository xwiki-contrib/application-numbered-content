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

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.xwiki.ckeditor.test.po.CKEditor;
import org.xwiki.test.ui.XWikiWebDriver;
import org.xwiki.test.ui.po.editor.WYSIWYGEditPage;

/**
 * The WYSIWYG edit mode of a page with numbered headings.
 *
 * @version $Id$
 * @since 1.11.1
 */
public class NumberedHeadingsEditPage extends WYSIWYGEditPage
{
    private static final String EDITOR_NAME = "content";

    private static final By HEADING_ANCESTORS =
        By.xpath("ancestor-or-self::*[self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6]");

    /**
     * Waits for the editor and for the numbered headings entries of its context menu to be ready.
     */
    public NumberedHeadingsEditPage()
    {
        new CKEditor(EDITOR_NAME).waitToLoad();
        XWikiWebDriver driver = getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(driver.getTimeout()))
            .until(d -> (Boolean) driver.executeScript(
                "return CKEDITOR.instances[arguments[0]].getMenuItem('headerConfig') != null;", EDITOR_NAME));
    }

    /**
     * Opens the context menu on the innermost element of the edited content whose text is the given text.
     *
     * @param elementText the text of the element
     * @return the displayed context menu
     */
    public CKEditorContextMenu openContextMenu(String elementText)
    {
        return new CKEditorContextMenu(EDITOR_NAME).open(elementText);
    }

    /**
     * Places the caret in the heading with the given text, presses Tab and waits for the heading level to change.
     *
     * @param headingText the text of the heading
     */
    public void indentHeading(String headingText)
    {
        pressKeysInHeading(headingText, Keys.TAB);
    }

    /**
     * Places the caret in the heading with the given text, presses Shift+Tab and waits for the heading level to
     * change.
     *
     * @param headingText the text of the heading
     */
    public void outdentHeading(String headingText)
    {
        pressKeysInHeading(headingText, Keys.chord(Keys.SHIFT, Keys.TAB));
    }

    /**
     * @param text the text of an element of the edited content
     * @return the level of the heading holding the innermost element with the given text, or 0 if this element is not
     *     in a heading
     */
    public int getHeadingLevel(String text)
    {
        return inEditedContent(() -> {
            List<WebElement> headings = findElement(text).findElements(HEADING_ANCESTORS);
            return headings.isEmpty() ? 0
                : Integer.parseInt(headings.get(headings.size() - 1).getTagName().substring(1));
        });
    }

    private void pressKeysInHeading(String headingText, CharSequence keys)
    {
        int level = getHeadingLevel(headingText);
        inEditedContent(() -> {
            findElement(headingText).click();
            getDriver().switchTo().activeElement().sendKeys(keys);
            return null;
        });
        XWikiWebDriver driver = getDriver();
        new WebDriverWait(driver, Duration.ofSeconds(driver.getTimeout()))
            .until(d -> getHeadingLevel(headingText) != level);
    }

    private WebElement findElement(String text)
    {
        return getDriver().findElement(By.xpath(
            String.format("//body//*[normalize-space(.)='%1$s'][not(.//*[normalize-space(.)='%1$s'])]", text)));
    }

    private <T> T inEditedContent(Supplier<T> action)
    {
        XWikiWebDriver driver = getDriver();
        driver.switchTo().frame((WebElement) driver.executeScript(
            "return CKEDITOR.instances[arguments[0]].ui.contentsElement.find('iframe').getItem(0).$;", EDITOR_NAME));
        try {
            return action.get();
        } finally {
            driver.switchTo().defaultContent();
        }
    }
}
