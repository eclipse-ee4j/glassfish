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

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The {@code PropertySheetSection} component was written to be used within the
 * {@code PropertySheet} component. It allows you to group null {@code Property}
 * components together in sections within the {@code PropertySheet}
 * component.
 * <p>
 * When you use this component to create a grouping of {@code Property}
 * components, you may provide a description for
 * the{@code PropertySheetSection}. This is done via the {@code label}
 * attribute. Set this attribute to the desired value, which, of course, may be
 * a ValueBinding expression or a literal String.<p>
 * For an example, please see the documentation for the {@code propertySheet}
 * Tag.</p>
 */
@com.sun.faces.annotation.Component(
        type = "com.sun.webui.jsf.PropertySheetSection",
        family = "com.sun.webui.jsf.PropertySheetSection",
        displayName = "Property Sheet Section",
        instanceName = "section",
        tagName = "propertySheetSection",
        //CHECKSTYLE:OFF
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_property_sheet_sections")
        //CHECKSTYLE:ON
public final class PropertySheetSection extends UIComponentBase
        implements NamingContainer {

    /**
     * This attribute allows you to provide a label or title for the section
     * this {@code PropertySheetSection} defines for the{@code PropertySheet}
     * component. The value may be a literal String, or it may be a ValueBinding
     * expression (useful for localization).
     */
    @com.sun.faces.annotation.Property(name = "label",
            displayName = "Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String label = null;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @com.sun.faces.annotation.Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @com.sun.faces.annotation.Property(name = "styleClass",
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
    @com.sun.faces.annotation.Property(name = "visible",
            displayName = "Visible",
            category = "Behavior")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Used to cache the visible children.
     */
    private transient List<UIComponentBase> visibleChildren = null;

    /**
     * Child count.
     */
    private transient int childCount = -1;

    /**
     * Constructor.
     */
    public PropertySheetSection() {
        super();
        setRendererType("com.sun.webui.jsf.PropertySheetSection");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.PropertySheetSection";
    }

    /**
     * This method calculates the number of visible child
     * {@link PropertySheetSection} or {@link Property} {@code UIComponent}. A
     * {@link PropertySheetSection} or {@link Property} can be made not visible
     * by setting their rendered property to false.
     *
     * @return The number of visible {@link PropertySheetSection} children.
     */
    public int getSectionChildrenCount() {
        // Set the output value
        return getVisibleSectionChildren().size();
    }

    /**
     * This method creates a {@code List} of visible (rendered=true) child
     * {@link PropertySheetSection} or {@link Property} components.
     *
     * @return {@code List} of child {@link PropertySheetSection} or
     * {@link Property} {@code UIComponent} objects.
     */
    public List getVisibleSectionChildren() {
        int numChildren = getChildCount();

        // See if we've already figured this out
        if ((visibleChildren != null) && (childCount == numChildren)) {
            return visibleChildren;
        }
        childCount = numChildren;

        // Make sure we have children
        if (numChildren == 0) {
            // Avoid creating child UIComponent List by checking for 0 sections
            visibleChildren = new ArrayList<UIComponentBase>(0);
            return visibleChildren;
        }

        // Add the visible sections to the result List
        UIComponent child;
        visibleChildren = new ArrayList<UIComponentBase>();
        Iterator it = getChildren().iterator();
        while (it.hasNext()) {
            child = (UIComponent) it.next();
            if (((child instanceof Property)
                    || (child instanceof PropertySheetSection))
                    && child.isRendered()) {
                visibleChildren.add((UIComponentBase) child);
            }
        }

        // Return the List
        return visibleChildren;
    }

    /**
     * If the rendered property is true, render the beginning of the current
     * state of this UIComponent to the response contained in the specified
     * FacesContext.
     *
     * If a Renderer is associated with this UIComponent, the actual encoding
     * will be delegated to Renderer.encodeBegin(FacesContext, UIComponent).
     *
     * @param context FacesContext for the current request.
     *
     * @exception IOException if an input/output error occurs while rendering.
     * @exception NullPointerException if FacesContext is null.
     */
    @Override
    public void encodeBegin(final FacesContext context) throws IOException {
        // Clear cached variables -- bugtraq #6270214.
        visibleChildren = null;
        childCount = -1;
        super.encodeBegin(context);
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     *
     * @param id id
     */
    @com.sun.faces.annotation.Property(name = "id")
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
    @com.sun.faces.annotation.Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * This attribute allows you to provide a label or title for the section
     * this {@code PropertySheetSection} defines for the{@code PropertySheet}
     * component. The value may be a literal String, or it may be a ValueBinding
     * expression (useful for localization).
     *
     * @return String
     */
    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }
        ValueExpression vb = getValueExpression("label");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * This attribute allows you to provide a label or title for the section
     * this {@code PropertySheetSection} defines for the{@code PropertySheet}
     * component. The value may be a literal String, or it may be a ValueBinding
     * expression (useful for localization).
     *
     * @see #getLabel()
     * @param newLabel label
     */
    public void setLabel(final String newLabel) {
        this.label = newLabel;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
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
     *
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
     *
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
        this.label = (String) values[1];
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
        values[1] = this.label;
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
