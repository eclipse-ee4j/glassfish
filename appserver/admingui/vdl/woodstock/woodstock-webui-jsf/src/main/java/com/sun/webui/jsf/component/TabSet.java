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

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.ValueChangeEvent;

import java.util.Stack;

/**
 * A set of one or more tabs. A TabSet is a naming container which should
 * contain only {@link Tab} components. The currently selected tab is specified
 * via the {@code selected} property, which may be bound to a model value.
 * Action listeners may be registered individually with a Tab; or, an action
 * listener may be registered with the containing tabSet, in which case it is
 * notified of all tab selection actions.
 *
 * <p>
 * TabSet implements {@link jakarta.faces.component.EditableValueHolder}, but it
 * diverges significantly from the behavior of a typical editable value holder.
 * Note that the {@code selected} property is an alias for the {@code value}
 * property.
 *
 * <ul>
 * <li><b>Converters and Validators</b>. Converters and validators are not used
 * by the tab set component. The value of a tab set is the ID of the currently
 * selected tab, set as a result of the tab selection action. The id is always a
 * String, and need not be converted. Also, since the tab always sets a correct
 * id, the id need not be validated.</li>
 * <li><b>Validation and tab navigation</b>. Tabs may contain input components.
 * By default, when a new tab is selected, the tab set checks whether all input
 * components inside the current tab are valid. If one or more is not valid,
 * then the tab set marks itself as invalid. As a result, the selected property
 * will not be updated, and the tab set will output the previously selected tab
 * as selected. In other words, by default, users cannot move from one tab to
 * another until all they have provided valid values for all the tab's input
 * components.</li>
 * <li><b>Updating of bound value models</b>. If a tab set is valid, and the
 * value of the selected property is changed, a model bound to the value will be
 * updated.</li>
 * <li><b>Immediate tab sets</b>. If a tab set is made immediate, tab navigation
 * will be allowed to occur even if there are invalid input components inside
 * the current tab. However, in this case, the model value will <b>not</b>
 * be updated. This generally works best if the tabs themselves are also made
 * immediate, and if some other command component is used to actually submit the
 * page for value processing.</li>
 * </ul>
 */
@Component(type = "com.sun.webui.jsf.TabSet",
        family = "com.sun.webui.jsf.TabSet",
        displayName = "Tab Set", tagName = "tabSet",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_tab_set",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_tab_set_props")
        //CHECKSTYLE:ON
public final class TabSet extends WebuiInput implements NamingContainer {

    /**
     * Set the method expression that identifies a method that handles the
     * action event fired when one of this tab set's tabs is used to submit the
     * page. The signature of the bound method must correspond to {@link
     * jakarta.faces.event.ActionListenerExpression#processAction}. The class that
     * defines the method must implement the {@code java.io.Serializable}
     * interface or {@code jakarta.faces.component.StateHolder} interface.
     */
    @Property(name = "actionListenerExpression",
            displayName = "Action Listener Expression",
            category = "Advanced")
    @Property.Method(signature
            = "void processAction(jakarta.faces.event.ActionEvent)")
    private MethodExpression actionListenerExpression;

    /**
     * Returns true if the tabs in this tab set should remember which of their
     * tab children was last selected. This enables the user to choose other
     * tabs in the set, and have the child tab selection in the original tab be
     * retained when the user returns to the original tab.
     */
    @Property(name = "lastSelectedChildSaved",
            displayName = "Last Selected Child Saved",
            isHidden = true)
    private boolean lastSelectedChildSaved = true;

    /**
     * lastSelectedChildSaved set.
     */
    private boolean lastSelectedChildSavedSet = true;

    /**
     * Returns true if the tabs should render in a visually lighter style, with
     * reduced shading and bolding. This attribute can only be used with mini
     * tabs, so you must also set the mini attribute to true to render
     * lightweight tabs.
     */
    @Property(name = "lite",
            displayName = "Lightweight Tab Set",
            category = "Appearance")
    private boolean lite = false;

    /**
     * lite set flag.
     */
    private boolean liteSet = false;

    /**
     * Set this attribute to true in a first level tab set, to create tabs that
     * have the smaller "mini" tab style. Note that mini tab sets will not
     * display properly if more than one level of tabs are specified.
     */
    @Property(name = "mini", displayName = "Mini", category = "Appearance")
    private boolean mini = false;

    /**
     * mini set flag.
     */
    private boolean miniSet = false;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
            //CHECKSTYLE:ON
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StyleClassPropertyEditor")
            //CHECKSTYLE:ON
    private String styleClass = null;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible",
            displayName = "Visible",
            category = "Behavior")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Create a new TabSet.
     */
    public TabSet() {
        super();
        setRendererType("com.sun.webui.jsf.TabSet");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.TabSet";
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("selected")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("selected")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    @Property(isHidden = true, isAttribute = false)
    @Override
    public Converter getConverter() {
        return super.getConverter();
    }

    @Property(isHidden = true, isAttribute = false)
    @Override
    public boolean isRequired() {
        return super.isRequired();
    }

    @Property(isHidden = true, isAttribute = false)
    @Override
    public MethodExpression getValidatorExpression() {
        return super.getValidatorExpression();
    }

    @Property(isHidden = true, isAttribute = true)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Get the method expression that identifies a method that handles the
     * action event fired when one of this tab set's tabs is used to submit the
     * page.
     * @return MethodExpression
     */
    public MethodExpression getActionListenerExpression() {
        return this.actionListenerExpression;
    }

    /**
     * Set the method expression that identifies a method that handles the
     * action event fired when one of this tab set's tabs is used to submit the
     * page. The signature of the bound method must correspond to {@link
     * jakarta.faces.event.MethodExpressionActionListener#processAction}. The
     * class that defines the method must implement the
     * {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.
     * @param newActionListenerExpression actionListenerExpression
     */
    public void setActionListenerExpression(
            final MethodExpression newActionListenerExpression) {

        this.actionListenerExpression = newActionListenerExpression;
    }

    /**
     * Returns true if the tabs in this tab set should remember which of their
     * tab children was last selected. This enables the user to choose other
     * tabs in the set, and have the child tab selection in the original tab be
     * retained when the user returns to the original tab.
     * @return {@code boolean}
     */
    public boolean isLastSelectedChildSaved() {
        if (this.lastSelectedChildSavedSet) {
            return this.lastSelectedChildSaved;
        }
        ValueExpression vb = getValueExpression("lastSelectedChildSaved");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Set to true if the tabs in this tab set should remember which of their
     * tab children was last selected. This enables the user to choose other
     * tabs in the set, and have the child tab selection in the original tab be
     * retained when the user returns to the original tab.
     * @param newLastSelectedChildSaved lastSelectedChildSaved
     */
    public void setLastSelectedChildSaved(
            final boolean newLastSelectedChildSaved) {

        this.lastSelectedChildSaved = newLastSelectedChildSaved;
        this.lastSelectedChildSavedSet = true;
    }

    /**
     * Returns true if the tabs should render in a visually lighter style, with
     * reduced shading and bolding. This attribute can only be used with mini
     * tabs, so you must also set the mini attribute to true to render
     * lightweight tabs.
     * @return {@code boolean}
     */
    public boolean isLite() {
        if (this.liteSet) {
            return this.lite;
        }
        ValueExpression vb = getValueExpression("lite");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * Set to true to render the tabs in a visually lighter style, with reduced
     * shading and bolding. This attribute can only be used with mini tabs, so
     * you must also set the mini attribute to true to render lightweight tabs.
     * @param newLite lite
     */
    public void setLite(final boolean newLite) {
        this.lite = newLite;
        this.liteSet = true;
    }

    /**
     * Set this attribute to true in a first level tab set, to create tabs that
     * have the smaller "mini" tab style. Note that mini tab sets will not
     * display properly if more than one level of tabs are specified.
     * @return {@code boolean}
     */
    public boolean isMini() {
        if (this.miniSet) {
            return this.mini;
        }
        ValueExpression vb = getValueExpression("mini");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * Set this attribute to true in a first level tab set, to create tabs that
     * have the smaller "mini" tab style. Note that mini tab sets will not
     * display properly if more than one level of tabs are specified.
     * @param newMini mini
     */
    public void setMini(final boolean newMini) {
        this.mini = newMini;
        this.miniSet = true;
    }

    /**
     * The id of the selected tab.
     * @return String
     */
    @Property(name = "selected",
            displayName = "Selected",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.TabIdsEditor")
            //CHECKSTYLE:ON
    public String getSelected() {
        return (String) getValue();
    }

    /**
     * The id of the selected tab.
     *
     * @see #getSelected()
     * @param selected selected
     */
    public void setSelected(final String selected) {
        setValue((Object) selected);
    }

    /**
     * Returns this tab set's tab descendant with id equal to the value of the
     * selected property. If the value of the selected property is null, returns
     * the first tab child. If there are no tab children, returns null.
     * @return Tab
     */
    public Tab getSelectedTab() {
        if (this.getChildCount() == 0) {
            return null;
        }
        Stack<Tab> tabStack = new Stack<Tab>();
        for (UIComponent child : this.getChildren()) {
            if (Tab.class.isAssignableFrom(child.getClass())) {
                tabStack.push((Tab) child);
            }
        }
        String id = this.getSelected();
        if (id == null) {
            if (tabStack.isEmpty()) {
                return null;
            } else {
                return tabStack.get(0);
            }
        }
        Tab selectedTab = null;
        while (selectedTab == null && !tabStack.isEmpty()) {
            Tab tab = tabStack.pop();
            if (id.equals(tab.getId())) {
                selectedTab = tab;
            } else if (tab.getTabChildCount() > 0) {
                tabStack.addAll(tab.getTabChildren());
            }
        }
        return selectedTab;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueExpression vb = getValueExpression("style");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyle()
     * @param newStyle style
     */
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueExpression vb = getValueExpression("styleClass");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyleClass()
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     * @return {@code boolean}
     */
    public boolean isVisible() {
        if (this.visibleSet) {
            return this.visible;
        }
        ValueExpression vb = getValueExpression("visible");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @see #isVisible()
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * Marks this tab set as valid only if all input component children of the
     * currently selected tab are valid.
     */
    @Override
    public void validate(final FacesContext context) {
        if (!this.isRendered()) {
            return;
        }
        this.setValid(true);
        if (!this.isImmediate()) {
            Tab selectedTab = this.getSelectedTab();
            if (selectedTab != null && selectedTab.getChildCount() > 0) {
                Stack<UIComponent> componentStack = new Stack<UIComponent>();
                componentStack.addAll(selectedTab.getChildren());
                while (this.isValid() && !componentStack.isEmpty()) {
                    UIComponent component = componentStack.pop();
                    if (component instanceof EditableValueHolder
                            && !((EditableValueHolder) component).isValid()) {
                        this.setValid(false);
                    }
                    if (component.getChildCount() > 0) {
                        componentStack.addAll(component.getChildren());
                    }
                }
            }
        }
        if (this.isValid()) {
            Object submittedValue = getSubmittedValue();
            // If a child tab was used to submit the page, then the tab will
            // have set
            // a non-null submitted value. Otherwise, if the submitted value
            // is null,
            // it means a command elsewhere on the page sumitted the tab set.
            // In this case, leave the tab set's current value unchanged.
            if (submittedValue != null) {
                Object previousValue = getValue();
                setValue(submittedValue);
                setSubmittedValue(null);
                if (compareValues(previousValue, submittedValue)) {
                    queueEvent(new ValueChangeEvent(this, previousValue,
                            submittedValue));
                }
            }
        }
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.actionListenerExpression = (jakarta.el.MethodExpression) values[1];
        this.lastSelectedChildSaved = ((Boolean) values[2]);
        this.lastSelectedChildSavedSet = ((Boolean) values[3]);
        this.lite = ((Boolean) values[4]);
        this.liteSet = ((Boolean) values[5]);
        this.mini = ((Boolean) values[6]);
        this.miniSet = ((Boolean) values[7]);
        this.style = (String) values[8];
        this.styleClass = (String) values[9];
        this.visible = ((Boolean) values[10]);
        this.visibleSet = ((Boolean) values[11]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[12];
        values[0] = super.saveState(context);
        values[1] = this.actionListenerExpression;
        if (this.lastSelectedChildSaved) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.lastSelectedChildSavedSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.lite) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.liteSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.mini) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        if (this.miniSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        values[8] = this.style;
        values[9] = this.styleClass;
        if (this.visible) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        return values;
    }

    /**
     * Returns the tab with the id specified that is a child of this tabSet. If
     * no such descendant tab exists, returns null. If this tabSet contains more
     * than one tab with the same id, the tab returned will be the first
     * encountered in document order.
     * @param tabId tabId
     * @return Tab
     */
    public Tab findChildTab(final String tabId) {
        if (tabId == null) {
            return null;
        }
        for (UIComponent child : this.getChildren()) {
            Tab tab = TabSet.findChildTab((Tab) child, tabId);
            if (tab != null) {
                return tab;
            }
        }
        return null;
    }

    /**
     * Returns the tab with the id specified that is a child of the tab
     * specified. If no such descendant tab exists, returns null. If the tab
     * specified contains more than one tab with the same id, the tab returned
     * will be the first encountered in document order.
     * @param tab tab
     * @param tabId tabId
     * @return Tab
     */
    public static Tab findChildTab(final Tab tab, final String tabId) {
        if (tab == null || tabId == null) {
            return null;
        }
        if (tabId.equals(tab.getId())) {
            return tab;
        }
        if (tab.getTabChildCount() == 0) {
            return null;
        }
        for (Tab child : tab.getTabChildren()) {
            Tab foundTab = TabSet.findChildTab(child, tabId);
            if (foundTab != null) {
                return foundTab;
            }
        }
        return null;
    }
}
