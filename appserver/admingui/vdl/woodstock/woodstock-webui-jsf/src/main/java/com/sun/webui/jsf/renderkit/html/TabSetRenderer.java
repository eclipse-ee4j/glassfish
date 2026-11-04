/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.Tab;
import com.sun.webui.jsf.component.TabSet;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.MethodExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.List;

/**
 * Renders a TabSet component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.TabSet"))
public final class TabSetRenderer extends AbstractRenderer {

    /**
     * Skip anchor name.
     */
    private static final String SKIP_ANCHOR_NAME = "tabSetSkipAnchor";

    /**
     * Selected tab anchor name.
     */
    private static final String SELECTED_TAB_ANCHOR_NAME = "selectedTabAnchor";

    /**
     * Empty string.
     */
    private static final String EMPTY_STR = "";

    /**
     * Space character.
     */
    private static final String SPACE = " ";

    /**
     * Default constructor.
     */
    public TabSetRenderer() {
        super();
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // render any kids of the selected tab component now
        TabSet tabSet = (TabSet) component;
        String selectedTabId = tabSet.getSelected();

        Theme theme = ThemeUtilities.getTheme(context);
        String lite = theme.getStyleClass(ThemeStyles.TABGROUPBOX);

        if (selectedTabId == null) {
            if (tabSet.isMini() && tabSet.isLite()) {
                writer.startElement("div", tabSet);
                writer.writeAttribute("class", lite, null);
                writer.endElement("div");
            }
            writer.endElement("div");
            return;
        }

        Tab selectedTab = tabSet.findChildTab(selectedTabId);
        if (selectedTab == null) {
            if (tabSet.isMini() && tabSet.isLite()) {
                writer.startElement("div", tabSet);
                writer.writeAttribute("class", lite, null);
                writer.endElement("div");
            }
            writer.endElement("div");
            return;
        }

        if (tabSet.isMini() && tabSet.isLite()) {
            writer.startElement("div", tabSet);
            writer.writeAttribute("class", lite, null);
        }

        while (selectedTab.getTabChildCount() > 0) {
            selectedTabId = selectedTab.getSelectedChildId();
            if (selectedTabId == null) {
                selectedTabId = ((Tab) selectedTab.getChildren().get(0))
                        .getId();
            }
            selectedTab = (Tab) selectedTab.findComponent(selectedTabId);
        }

        int numKids = selectedTab.getChildCount();
        if (numKids > 0) {
            // render the contentHeader facet if specified
            UIComponent facet = tabSet.getFacet("contentHeader");
            if (facet != null) {
                RenderingUtilities.renderComponent(facet, context);
            }

            // render the children of the selected Tab component
            List kids = selectedTab.getChildren();
            for (int i = 0; i < numKids; i++) {
                UIComponent kid = (UIComponent) kids.get(i);
                RenderingUtilities.renderComponent(kid, context);
            }

            facet = tabSet.getFacet("contentFooter");
            if (facet != null) {
                RenderingUtilities.renderComponent(facet, context);
            }
        }
        if (tabSet.isMini() && tabSet.isLite()) {
            writer.endElement("div");
        }
        writer.endElement("div");
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        TabSet tabSet = (TabSet) component;
        ResponseWriter writer = context.getResponseWriter();
        Theme theme = ThemeUtilities.getTheme(context);

        List level1 = tabSet.getChildren();

        if (level1.size() < 1) {
            // no tab children and hence no tabset
            if (LogUtil.infoEnabled()) {
                LogUtil.info(TabSetRenderer.class, "WEBUI0005",
                        new String[]{tabSet.getId()});
            }
            return;
        }

        // open the initial div containing the entire tab set
        startTabSetDiv(context, writer, tabSet, theme);

        // render the a11y skip link
        renderSkipLink(context, tabSet, theme);

        // render the first level of tabs and get the 2nd level if any
        List level2Tabs
                = renderLevel(context, tabSet, writer, 1, tabSet.getChildren());

        // if there are any level 2 tabs render those now
        if (level2Tabs != null) {
            List level3Tabs
                    = renderLevel(context, tabSet, writer, 2, level2Tabs);

            // if there are any level 3 tabs render those now
            if (level3Tabs != null) {
                renderLevel(context, tabSet, writer, 3, level3Tabs);
            }
        }

        // output the bookmark for the SkipHyperlink
        RenderingUtilities.renderAnchor(SKIP_ANCHOR_NAME, tabSet, context);
    }

    /**
     * Helper function called by encodeChildren to open the TabSet div.
     * @param context faces context
     * @param writer writer to use
     * @param tabSet tabSet component
     * @param theme the current theme
     * @throws IOException if an IO error occurs
     */
    private void startTabSetDiv(final FacesContext context,
            final ResponseWriter writer, final TabSet tabSet,
            final Theme theme) throws IOException {

        String style = tabSet.getStyle();
        String styleClass = tabSet.getStyleClass();

        String lite = theme.getStyleClass(ThemeStyles.TABGROUP);

        if (tabSet.isMini() && tabSet.isLite()) {
            if (styleClass != null) {
                styleClass = styleClass.concat(SPACE).concat(lite);
            } else {
                styleClass = lite;
            }
        }

        if (!tabSet.isVisible()) {
            String hiddenStyle = theme.getStyleClass(ThemeStyles.HIDDEN);
            if (styleClass == null) {
                styleClass = hiddenStyle;
            } else {
                styleClass = styleClass.concat(SPACE).concat(hiddenStyle);
            }
        }

        writer.startElement("div", tabSet);
        writer.writeAttribute("id", tabSet.getClientId(context), "id");

        if (style != null) {
            writer.writeAttribute("style", style, null);
        }
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, null);
        }
    }

    /**
     * Get the styles.
     * @param tabSet tabSet component
     * @param theme the current theme
     * @param level tab set level
     * @return String[]
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private String[] getStyles(final TabSet tabSet, final Theme theme,
            final  int level) {

        // get the various level specific tab styles we'll need
        String divStyle = EMPTY_STR;
        String tableStyle = EMPTY_STR;
        String linkStyle = EMPTY_STR;
        String selectedTdStyle = EMPTY_STR;
        String selectedTextStyle = EMPTY_STR;

        switch (level) {
            case 1: // get the level 1 tab styles
                if (tabSet.isMini()) {
                    divStyle = theme.getStyleClass(ThemeStyles.MINI_TAB_DIV);
                    tableStyle
                            = theme.getStyleClass(ThemeStyles.MINI_TAB_TABLE);
                    linkStyle = theme.getStyleClass(ThemeStyles.MINI_TAB_LINK);
                    selectedTdStyle = theme.getStyleClass(
                            ThemeStyles.MINI_TAB_TABLE_SELECTED_TD);
                    selectedTextStyle
                            = theme.getStyleClass(
                                    ThemeStyles.MINI_TAB_SELECTED_TEXT);
                } else {
                    divStyle = theme.getStyleClass(ThemeStyles.TAB1_DIV);
                    tableStyle
                            = theme.getStyleClass(ThemeStyles.TAB1_TABLE_NEW);
                    linkStyle = theme.getStyleClass(ThemeStyles.TAB1_LINK);
                    selectedTdStyle
                            = theme.getStyleClass(
                                    ThemeStyles.TAB1_TABLE_SELECTED_TD);
                    selectedTextStyle
                            = theme.getStyleClass(
                                    ThemeStyles.TAB1_SELECTED_TEXT_NEW);
                }
                break;
            case 2: // get the level 2 tab styles
                divStyle = theme.getStyleClass(ThemeStyles.TAB2_DIV);
                tableStyle = theme.getStyleClass(ThemeStyles.TAB2_TABLE_NEW);
                linkStyle = theme.getStyleClass(ThemeStyles.TAB2_LINK);
                selectedTdStyle
                        = theme.getStyleClass(
                                ThemeStyles.TAB2_TABLE_SELECTED_TD);
                selectedTextStyle
                        = theme.getStyleClass(ThemeStyles.TAB2_SELECTED_TEXT);
                break;
            case 3: // get the level 3 tab styles
                divStyle = theme.getStyleClass(ThemeStyles.TAB3_DIV);
                tableStyle = theme.getStyleClass(ThemeStyles.TAB3_TABLE_NEW);
                linkStyle = theme.getStyleClass(ThemeStyles.TAB3_LINK);
                selectedTdStyle
                        = theme.getStyleClass(
                                ThemeStyles.TAB3_TABLE_SELECTED_TD);
                selectedTextStyle
                        = theme.getStyleClass(ThemeStyles.TAB3_SELECTED_TEXT);
                break;
            default:
                break;
        }

        String[] styles = new String[]{
            divStyle, tableStyle, linkStyle, selectedTdStyle, selectedTextStyle
        };
        return styles;
    }

    /**
     * Helper function called by encodeChildren to write out the a11y skip link.
     * @param context faces context
     * @param tabSet tabSet component
     * @param theme the current theme
     * @throws IOException if an IO error occurs
     */
    private void renderSkipLink(final FacesContext context, final TabSet tabSet,
            final Theme theme) throws IOException {

        // need the label of currently selected tab for skip alt text
        Tab selectedTab = tabSet.findChildTab(tabSet.getSelected());

        String[] args = new String[]{EMPTY_STR};
        if (selectedTab != null) {
            Object obj = selectedTab.getText();
            if (obj != null) {
                args[0] = ConversionUtilities.convertValueToString(
                        selectedTab, obj);
            }
        }

        // render the skip link for a11y
        String toolTip = theme.getMessage("tab.skipTagAltText", args);
        String styleClass = theme.getStyleClass(ThemeStyles.SKIP_MEDIUM_GREY1);
        RenderingUtilities.renderSkipLink(SKIP_ANCHOR_NAME, styleClass, null,
                toolTip, null, tabSet, context);
    }

    /**
     * Set the layout level.
     * @param writer writer to use
     * @param tabSet tabSet component
     * @param styles CSS styles
     * @throws IOException if an IO error occurs
     */
    private void layoutLevel(final ResponseWriter writer, final TabSet tabSet,
            final String[] styles) throws IOException {

        writer.startElement("div", tabSet);
        writer.writeAttribute("class", styles[0], null);
        writer.startElement("table", tabSet);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("cellspacing", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("class", styles[1], null);
        writer.writeAttribute("title", EMPTY_STR, null);
        writer.startElement("tr", tabSet);
    }

    /**
     * This method renders each of the Tab components in the given level.
     *
     * @param context The current FacesContext
     * @param tabSet The current TabSet component
     * @param writer The current ResponseWriter
     * @param level The level (1, 2 or 3) of the Tab set to be rendered
     * @param currentLevelTabs A List containing the Tab objects for the current
     * level
     * @return List
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected List renderLevel(final FacesContext context, final TabSet tabSet,
            final ResponseWriter writer, final int level,
            final List currentLevelTabs) throws IOException {

        int numTabs = currentLevelTabs.size();
        if (numTabs == 0) {
            // no tabs in given level
            return null;
        }

        Theme theme = ThemeUtilities.getTheme(context);
        String[] styles = getStyles(tabSet, theme, level);
        String hidden = theme.getStyleClass(ThemeStyles.HIDDEN);
        String selectedTabId = tabSet.getSelected();
        Tab currentLevelSelection = null;

        // need to ensure at least one tab in this level is selected
        boolean levelHasSelection = false;
        for (int i = 0; i < numTabs; i++) {
            try {
                currentLevelSelection = (Tab) currentLevelTabs.get(i);
            } catch (ClassCastException cce) {
                // not a Tab instance
                continue;
            }
            if (isSelected(currentLevelSelection, selectedTabId)) {
                // sTab is either selected or part of selection
                levelHasSelection = true;
                break;
            }
        }

        if (!levelHasSelection) {
            try {
                selectedTabId = ((Tab) currentLevelTabs.get(0)).getId();
                tabSet.setSelected(selectedTabId);
            } catch (ClassCastException cce) {
                // gave it a shot but failed... no tab will be selected
            }
        }

        if (currentLevelSelection != null
                && (currentLevelSelection.getTabChildCount() > 0)) {
            // selected tab in this level has children - must adjust table style
            switch (level) {
                case 1:
                    styles[1]
                            = theme.getStyleClass(ThemeStyles.TAB1_TABLE2_NEW);
                    break;
                case 2:
                    styles[1]
                            = theme.getStyleClass(ThemeStyles.TAB2_TABLE3_NEW);
                    break;
                default:
                    break;
            }
        }

        // open the div, table and tr element for this level of tabs
        layoutLevel(writer, tabSet, styles);

        // get the developer specified binding for action listener
        MethodExpression actionListenerExpression =
                tabSet.getActionListenerExpression();

        // need a variable to save next level of tabs if we have one
        List nextLevelToRender = null;

        // render each tab in this level
        for (int i = 0; i < numTabs; i++) {
            Tab tab;
            try {
                tab = (Tab) currentLevelTabs.get(i);
            } catch (ClassCastException cce) {
                // expected if a child of current Tab is not another Tab
                continue;
            }

            if (!tab.isRendered()) {
                continue;
            }

            // each tab goes in its own table cell
            writer.startElement("td", tabSet);

            String newSelectedClass = styles[3];
            String newNonSelectedClass = null;

            if (!tab.isVisible()) {
                newSelectedClass
                        = newSelectedClass.concat(SPACE).concat(hidden);
                newNonSelectedClass = hidden;
            }

            if (selectedTabId != null && isSelected(tab, selectedTabId)) {
                // this tab or one of it's children is selected
                nextLevelToRender = renderSelectedTab(context, writer, theme,
                        tabSet, tab, styles, newSelectedClass);
            } else {
                // not part of current selection
                tab.setStyleClass(styles[2]);

                RenderingUtilities.renderComponent(tab, context);
            }

            writer.endElement("td");
        }

        writer.endElement("tr");
        writer.endElement("table");
        writer.endElement("div");

        return nextLevelToRender;
    }

    /**
     * Render the selected tab.
     * @param context faces context
     * @param writer writer to use
     * @param theme the current theme
     * @param tabSet tabSet component
     * @param tab tab component
     * @param styles CSS styles
     * @param selectedClass selected CSS class
     * @return List
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private List renderSelectedTab(final FacesContext context,
            final ResponseWriter writer, final Theme theme, final TabSet tabSet,
            final Tab tab, final String[] styles, final String selectedClass)
            throws IOException {

        UIComponent parent = tab.getParent();
        if (parent != null && parent instanceof Tab) {
            if (tabSet.isLastSelectedChildSaved()) {
                // ensure that the parent tab knows this one is selected
                ((Tab) parent).setSelectedChildId(tab.getId());
            } else {
                // forget last selected child
                ((Tab) parent).setSelectedChildId(null);
            }
        }

        String label = EMPTY_STR;
        Object obj = tab.getText();
        if (obj != null) {
            label = ConversionUtilities.convertValueToString(tab, obj);
        }

        String selectionDivClass = styles[4];
        if (label.length() < 6) {
            // short label, apply TabPad style class to div enclosing selection
            String padClass = theme.getStyleClass(ThemeStyles.TAB_PADDING);
            selectionDivClass
                    = selectionDivClass.concat(SPACE).concat(padClass);
        }

        writer.writeAttribute("class", selectedClass, null);
        writer.startElement("div", tab);
        writer.writeAttribute("class", selectionDivClass, null);
        String titleString = theme.getMessage(
                "tabSet.selectedTab", new Object[]{label});
        writer.writeAttribute("title", titleString, null);

        // Write a named anchor for the selected tab, so that focus will return
        // to the current tab after it is selected. In that way tab focus will
        // next shift to the first input component that is a child of the
        // selected tab.
        writer.startElement("a", tab);
        writer.writeAttribute("id", tab.getClientId(context), "id");
        writer.writeAttribute("name", SELECTED_TAB_ANCHOR_NAME, "name");
        writer.endElement("a");

        // just write the label of the selected tab
        writer.write(label);
        writer.endElement("div");

        // return any children of the selected tab to render as next level
        if (tab.getTabChildCount() == 0) {
            return null;
        }
        return tab.getTabChildren();
    }

    /**
     * Utility method that determines if the given Tab component or any one of
     * its descendants is the selected tab.
     *
     * @param tab The Tab component to check for selection
     * @param selectedTabId The id of the currently selected Tab
     * @return {@code boolean}
     */
    protected boolean isSelected(final Tab tab, final String selectedTabId) {
        if (selectedTabId == null) {
            return false;
        }
        if (selectedTabId.equals(tab.getId())) {
            return true;
        }
        if (tab.getTabChildCount() == 0) {
            return false;
        }
        for (Tab child : tab.getTabChildren()) {
            if (isSelected(child, selectedTabId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        // TabSet does not encode or decode its input value. The input
        // value is determined by the selected and/or current child
        // tab component.
    }
}
