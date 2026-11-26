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
}
