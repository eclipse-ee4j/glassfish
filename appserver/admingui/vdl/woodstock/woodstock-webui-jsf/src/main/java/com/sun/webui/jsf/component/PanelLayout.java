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

import com.sun.faces.annotation.Attribute;
import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The PanelLayout component is used to display a group of related components.
 */
@Component(type = "com.sun.webui.jsf.PanelLayout",
        family = "com.sun.webui.jsf.PanelLayout",
        displayName = "Layout Panel",
        instanceName = "layoutPanel",
        tagName = "panelLayout",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_layout_panel",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_panel_layout_props")
        //CHECKSTYLE:ON
public final class PanelLayout extends UIComponentBase
        implements NamingContainer {

    /**
     * Keyword that indicates flow layout.
     */
    public static final String FLOW_LAYOUT = "flow";

    /**
     * Keyword that indicates grid layout or absolute positioning.
     */
    public static final String GRID_LAYOUT = "grid";

    /**
     * Use absolute grid positioning, or flow layout.
     */
    @Property(name = "panelLayout",
            displayName = "Panel Layout",
            attribute = @Attribute(
                    name = "panelLayout",
                    isBindable = false,
                    isRequired = false))
    private String panelLayout = null;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
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
     * Creates a new instance of PanelLayout.
     */
    public PanelLayout() {
        super();
        setRendererType("com.sun.webui.jsf.PanelLayout");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.PanelLayout";
    }

    /**
     * Set the layout mode for this panel, to either "grid" or "flow". The
     * default value is "grid". Setting this property to an unrecognized value
     * will cause it to reset to the default value.
     * @param newPanelLayout panelLayout
     */
    public void setPanelLayout(final String newPanelLayout) {
        if (FLOW_LAYOUT.equals(newPanelLayout)) {
            doSetPanelLayout(FLOW_LAYOUT);
        } else {
            doSetPanelLayout(GRID_LAYOUT);
        }
    }

    /**
     * Get the panel layout.
     * @return String
     */
    public String getPanelLayout() {
        String layout = doGetPanelLayout();
        if (layout == null) {
            return GRID_LAYOUT;
        }
        return layout;
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    /**
     * Use the rendered attribute to indicate whether the HTML code for the
     * component should be included in the rendered HTML page. If set to false,
     * the rendered HTML page does not include the HTML for the component. If
     * the component is not rendered, it is also not processed on any subsequent
     * form submission.
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * Use absolute grid positioning, or flow layout.
     * @return String
     */
    private String doGetPanelLayout() {
        return this.panelLayout;
    }

    /**
     * Use absolute grid positioning, or flow layout.
     *
     * @see #getPanelLayout()
     * @param newPanelLayout panelLayout
     */
    private void doSetPanelLayout(final String newPanelLayout) {
        this.panelLayout = newPanelLayout;
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

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.panelLayout = (String) values[1];
        this.style = (String) values[2];
        this.styleClass = (String) values[3];
        this.visible = ((Boolean) values[4]);
        this.visibleSet = ((Boolean) values[5]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[6];
        values[0] = super.saveState(context);
        values[1] = this.panelLayout;
        values[2] = this.style;
        values[3] = this.styleClass;
        if (this.visible) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        return values;
    }
}
