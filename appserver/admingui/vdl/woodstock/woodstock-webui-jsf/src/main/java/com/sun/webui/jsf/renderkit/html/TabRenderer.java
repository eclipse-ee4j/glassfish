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

 /*
 * TabRenderer.java
 */
package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.Tab;
import com.sun.webui.jsf.component.TabSet;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.List;

/**
 * Renders a Tab component.
 *
 * A Tab is a Hyperlink that, when clicked, also updates the lastSelectedChild
 * value of any parent Tab instance as well as the selected value of the
 * enclosing TabSet component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Tab"))
public final class TabRenderer extends HyperlinkRenderer {

    /**
     * Default constructor.
     */
    public TabRenderer() {
        super();
    }

    @Override
    protected void renderChildren(final FacesContext context,
            final UIComponent component) throws IOException {

    }

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        super.renderStart(context, component, writer);

        // ensure that this tab's parent is either a Tab or TabSet
        UIComponent parent = component.getParent();

        if (!(parent instanceof Tab || parent instanceof TabSet)) {
            if (LogUtil.infoEnabled()) {
                LogUtil.info(TabRenderer.class, "WEBUI0006",
                        new String[]{component.getId()});
            }
        }
    }

    @Override
    protected void finishRenderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Tab tab = (Tab) component;

        // Set up local variables we will need
        int tabIndex = tab.getTabIndex();
        if (tabIndex >= 0) {
            writer.writeAttribute("tabIndex", Integer.toString(tabIndex),
                    null);
        }
        super.finishRenderAttributes(context, component, writer);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    protected String getStyles(final FacesContext context,
            final UIComponent component) {

        Tab link = (Tab) component;

        StringBuilder sb = new StringBuilder();

        Theme theme = ThemeUtilities.getTheme(context);
        if (link.isDisabled()) {
            sb.append(" ");
            sb.append(theme.getStyleClass(ThemeStyles.LINK_DISABLED));
        }

        Object value = link.getText();
        if (value != null) {
            String text = ConversionUtilities.convertValueToString(link, value);
            if (text.length() <= 6) {
                sb.append(" ");
                sb.append(theme.getStyleClass(ThemeStyles.TAB_PADDING));
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        super.decode(context, component);
        String paramId = super.getSubmittedParameterId(context, component);
        String submittedValue
                = (String) context.getExternalContext()
                        .getRequestParameterMap().get(paramId);
        if (submittedValue != null) {
            // If this is the tab that submitted the page (i.e. the tab that
            // the user
            // clicked on to submit the page), update the submitted value of the
            // ancestor tabSet. Note that even though this tab was clicked, if
            // this tab has children, the selected tab will be one of its
            // descendants.
            // Also refresh the selectedChildId property of all parent tabs
            // of the selected tab.
            Tab selectedTab = (Tab) component;
            while (selectedTab.getTabChildCount() > 0) {
                String previousSelectedTabId = selectedTab.getSelectedChildId();
                List<Tab> childrenTabs = selectedTab.getTabChildren();
                selectedTab = childrenTabs.get(0);
                for (Tab childTab : childrenTabs) {
                    if (childTab.getId() != null
                            && childTab.getId().equals(previousSelectedTabId)) {
                        selectedTab = childTab;
                    }
                }
            }
            TabSet tabSet = Tab.getTabSet(selectedTab);
            tabSet.setSubmittedValue(selectedTab.getId());
        }
    }
}
