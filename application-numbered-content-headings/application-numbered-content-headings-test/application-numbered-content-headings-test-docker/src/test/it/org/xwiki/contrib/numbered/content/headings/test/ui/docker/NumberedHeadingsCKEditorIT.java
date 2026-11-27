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
package org.xwiki.contrib.numbered.content.headings.test.ui.docker;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.xwiki.contrib.numbered.content.headings.test.po.CKEditorContextMenu;
import org.xwiki.contrib.numbered.content.headings.test.po.NumberedHeadingsEditPage;
import org.xwiki.contrib.numbered.content.headings.test.po.NumberedHeadingsViewPage;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the numbered headings entries of the CKEditor context menu.
 *
 * @version $Id$
 * @since 1.11.1
 */
@UITest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NumberedHeadingsCKEditorIT
{
    private static final String HEADER_CONFIG = "Header Config";

    private static final String REMOVE_NUMBERING = "Remove numbering";

    private static final String RESTORE_NUMBERING = "Restore numbering";

    private static final String SKIPPED_HEADING = "Second";

    private static final String THIRD_HEADING = "Third";

    private static final String PARAGRAPH = "Some paragraph.";

    @BeforeAll
    static void beforeAll(TestUtils setup) throws Exception
    {
        setup.loginAsSuperAdmin();
        // Before XWiki 17.4, the 'org.xwiki.platform.html.head' UIXP, used by the numbered headings configuration, is
        // rendered from the 'meta' wiki preference, which xwiki-platform-distribution-ui-base provides. That module
        // brings too many transitive dependencies, so set the preference here instead.
        setup.setWikiPreference("meta",
            "#foreach($uix in $services.uix.getExtensions('org.xwiki.platform.html.head', {'sortByParameter': 'order'}))\n"
                + "  $services.rendering.render($uix.execute(), 'xhtml/1.0')\n"
                + "#end");
    }

    @Test
    @Order(1)
    void contextMenuEntries(TestUtils setup, TestReference testReference)
    {
        DocumentReference page = getPage(testReference);
        setup.deletePage(page);
        setup.createPage(page, "= First =\n\n" + PARAGRAPH + "\n\n= " + SKIPPED_HEADING + " =\n\n= Third =");
        setup.addObject(page, "NumberedHeadings.Code.NumberedHeadingsClass", "status", "activated");

        NumberedHeadingsEditPage editPage = edit(setup, page);
        List<String> entries = editPage.openContextMenu(SKIPPED_HEADING).getEntries();
        assertTrue(entries.contains(HEADER_CONFIG), entries.toString());
        assertTrue(entries.contains(REMOVE_NUMBERING), entries.toString());
        assertFalse(entries.contains(RESTORE_NUMBERING), entries.toString());

        entries = editPage.openContextMenu(PARAGRAPH).getEntries();
        assertFalse(entries.contains(HEADER_CONFIG), entries.toString());
        assertFalse(entries.contains(REMOVE_NUMBERING), entries.toString());
        assertFalse(entries.contains(RESTORE_NUMBERING), entries.toString());
        editPage.clickCancel();
    }

    @Test
    @Order(2)
    void skipNumbering(TestUtils setup, TestReference testReference)
    {
        DocumentReference page = getPage(testReference);
        NumberedHeadingsEditPage editPage = edit(setup, page);
        editPage.openContextMenu(SKIPPED_HEADING).clickEntry(REMOVE_NUMBERING);
        editPage.clickSaveAndView();

        NumberedHeadingsViewPage viewPage = new NumberedHeadingsViewPage();
        assertEquals(List.of("First", "Third"), viewPage.getNumberedHeadings());

        editPage = edit(setup, page);
        CKEditorContextMenu contextMenu = editPage.openContextMenu(SKIPPED_HEADING);
        List<String> entries = contextMenu.getEntries();
        assertTrue(entries.contains(RESTORE_NUMBERING), entries.toString());
        assertFalse(entries.contains(REMOVE_NUMBERING), entries.toString());
        contextMenu.clickEntry(RESTORE_NUMBERING);
        editPage.clickSaveAndView();

        viewPage = new NumberedHeadingsViewPage();
        assertEquals(List.of("First", SKIPPED_HEADING, "Third"), viewPage.getNumberedHeadings());
    }

    @Test
    @Order(3)
    void headingIndentation(TestUtils setup, TestReference testReference)
    {
        NumberedHeadingsEditPage editPage = edit(setup, getPage(testReference));
        assertEquals(1, editPage.getHeadingLevel(THIRD_HEADING));
        editPage.indentHeading(THIRD_HEADING);
        assertEquals(2, editPage.getHeadingLevel(THIRD_HEADING));
        editPage.outdentHeading(THIRD_HEADING);
        assertEquals(1, editPage.getHeadingLevel(THIRD_HEADING));
        editPage.clickCancel();
    }

    @Test
    @Order(4)
    void toggleSkipNumberingWithoutMovingTheCaret(TestUtils setup, TestReference testReference)
    {
        NumberedHeadingsEditPage editPage = edit(setup, getPage(testReference));
        editPage.openContextMenu(SKIPPED_HEADING).clickEntry(REMOVE_NUMBERING);

        CKEditorContextMenu contextMenu = editPage.openContextMenu();
        List<String> entries = contextMenu.getEntries();
        assertTrue(entries.contains(RESTORE_NUMBERING), entries.toString());
        assertFalse(entries.contains(REMOVE_NUMBERING), entries.toString());
        contextMenu.clickEntry(RESTORE_NUMBERING);

        entries = editPage.openContextMenu().getEntries();
        assertTrue(entries.contains(REMOVE_NUMBERING), entries.toString());
        assertFalse(entries.contains(RESTORE_NUMBERING), entries.toString());
        editPage.clickCancel();
    }

    private DocumentReference getPage(TestReference testReference)
    {
        // Shared by all the test methods, the test reference being specific to each method.
        return new DocumentReference("WebHome", new SpaceReference(testReference.getLastSpaceReference().getParent()));
    }

    private NumberedHeadingsEditPage edit(TestUtils setup, DocumentReference reference)
    {
        setup.gotoPage(reference, "edit", "editor=wysiwyg");
        return new NumberedHeadingsEditPage();
    }
}
