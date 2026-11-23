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
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.event.MethodExprActionListener;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.FacesEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The Tab component represents one tab in a tab set. Tabs must be children of a
 * TabSet, or of another Tab.
 *
 * <p>
 * Tab extends {@link Hyperlink}. Clicking on a tab therefore submits the
 * current page.
 */
@Component(type = "com.sun.webui.jsf.Tab",
        family = "com.sun.webui.jsf.Tab",
        displayName = "Tab",
        tagName = "tab",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_tab",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_tab_props")
        //CHECKSTYLE:ON
public final class Tab extends Hyperlink implements NamingContainer {

    /**
     * The id of this tab's currently selected Tab child or null if one is not
     * selected.
     */
    @Property(isHidden = true)
    private String selectedChildId;

    /**
     * Create a new instance of Tab.
     */
    public Tab() {
        super();
        setRendererType("com.sun.webui.jsf.Tab");
    }

    /**
     * Create a new instance of Tab with the text property set to the value
     * specified.
     * @param text tab text
     */
    public Tab(final String text) {
        this();
        setText(text);
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Tab";
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     * @return String
     */
    @Property(isHidden = true, isAttribute = true)
    @Override
    public String getOnDblClick() {
        return super.getOnDblClick();
    }

    @Property(isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    @Property(shortDescription = "The display label for this tab")
    @Override
    public Object getText() {
        return super.getText();
    }

    /**
     * Returns the id of this tab's currently selected Tab child, or null if one
     * is not selected.
     * @return String
     */
    public String getSelectedChildId() {
        if (this.selectedChildId != null) {
            return this.selectedChildId;
        }
        ValueExpression vb = getValueExpression("selectedChildId");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Set the id of this tab's currently selected Tab child to the value
     * specified.
     * @param newSelectedChildId selectedChildId
     */
    public void setSelectedChildId(final String newSelectedChildId) {
        this.selectedChildId = newSelectedChildId;
    }

    /**
     * Returns the number of children of this tab that are themselves tabs.
     * @return int
     */
    public int getTabChildCount() {
        if (this.getChildCount() == 0) {
            return 0;
        }
        int childTabCount = 0;
        for (UIComponent child : this.getChildren()) {
            if (child instanceof Tab) {
                childTabCount++;
            }
        }
        return childTabCount;
    }

    /**
     * Returns a list of all children of this tab that are themselves tabs.
     * @return {@code List<Tab>}
     */
    public List<Tab> getTabChildren() {
        List<Tab> tabChildren = new ArrayList<Tab>();
        for (UIComponent child : this.getChildren()) {
            if (child instanceof Tab) {
                tabChildren.add((Tab) child);
            }
        }
        return tabChildren;
    }

    /**
     * Customized implementation that allows child components to decode possible
     * submitted input only if the component is part of the currently selected
     * tab. Some input components cannot distinguish between a null submitted
     * value that is the result of the user unselecting the value (e.g. in the
     * case of a checkbox or list box) from the case that is the result of the
     * component being hidden in an unselected tab.
     */
    @Override
    public void processDecodes(final FacesContext context) {
        if (!this.isRendered()) {
            return;
        }
        TabSet tabSet = Tab.getTabSet(this);
        if (tabSet == null) {
            return;
        }
        if (this.getId() != null && this.getId().equals(tabSet.getSelected())) {
            // If this tab was the selected tab in the submitted page, invoke
            // process
            // decodes on all children components
            for (UIComponent child : this.getChildren()) {
                child.processDecodes(context);
            }
        } else {
            // Otherwise, invoke process decodes only on any tab children, since
            // one of them, or one of their descendants, may be the tab that was
            // selected on the submitted page
            for (Tab tabChild : this.getTabChildren()) {
                tabChild.processDecodes(context);
            }
        }
        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }
    }

    /**
     * Customized implementation that, in addition to invoking all other action
     * listeners for this tab, invokes the action listener method bound by the
     * action listener expression on this tab's parent tabSet, if there is one.
     */
    @Override
    public void broadcast(final FacesEvent event)
            throws AbortProcessingException {

        super.broadcast(event);
        if (event instanceof ActionEvent) {
            TabSet tabSet = Tab.getTabSet(this);
            if (tabSet != null
                    && tabSet.getActionListenerExpression() != null) {
                ActionListener listener =
                        new MethodExprActionListener(tabSet
                                .getActionListenerExpression());
                listener.processAction((ActionEvent) event);
            }
        }
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.selectedChildId = (String) values[1];
    }

    @Override
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[2];
        values[0] = super.saveState(context);
        values[1] = this.selectedChildId;
        return values;
    }

    /**
     * Utility method that returns the tabSet instance that contains the tab
     * specified.
     * @param tab tab
     * @return TabSet
     */
    public static TabSet getTabSet(final Tab tab) {
        TabSet tabSet = null;
        UIComponent parent = tab.getParent();
        while (tabSet == null && parent != null) {
            if (parent instanceof TabSet) {
                tabSet = (TabSet) parent;
            } else {
                parent = parent.getParent();
            }
        }
        return tabSet;
    }
}
