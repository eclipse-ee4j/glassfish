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
 * The {@code PropertySheet} component is a {@code NamingContainer}
 * used to layout {@code PropertySheetSection} components on a page. Each
 * {@code PropertySheetSection} may in turn have any number of
 * {@code Property} components within it. This allows you to easily format
 * a page with a number of input or read-only fields.
 * {@code PropertySheetSection}s allow you to group {@code Property}
 * components together and provide a {@code label} for the set of enclosed
 * {@code Property}s.
 * <p>
 * The {@code PropertySheet} allows each{@code PropertySheetSection}
 * to have an optional "jump link" from the top of the
 * {@code PropertySheet} to each {@code PropertySheetSection} within
 * the {@code PropertySheet}. This is accomplished by supplying the
 * attribute {@code jumpLinks} with a value of true. If not specified, this
 * attribute defaults to false.
 * </p>
 * <p>
 * For an example, please see the documentation for the
 * {@code propertySheet} Tag.
 * </p>
 */
@Component(type = "com.sun.webui.jsf.PropertySheet",
        family = "com.sun.webui.jsf.PropertySheet",
        displayName = "Property Sheet",
        tagName = "propertySheet",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_property_sheet",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_property_sheet_props")
        //CHECKSTYLE:ON
public final class PropertySheet extends UIComponentBase
        implements NamingContainer {

    /**
     * Used to cache the visible sections.
     */
    private transient List visibleSections;

    /**
     * child count.
     */
    private transient int childCount = -1;

    /**
     * This boolean attribute allows you to control whether jump links will be
     * created at the top of this {@code PropertySheet} or not. The default
     * is NOT to create the links -- setting this attribute to "true" turns this
     * feature on.
     */
    @Property(name = "jumpLinks",
            displayName = "Show Jump Links",
            category = "Appearance")
    private boolean jumpLinks;

    /**
     * jumpLinks set flag.
     */
    private boolean jumpLinksSet;

    /**
     * Specifies whether to display a required field legend in the upper right
     * area of the property sheet. This attribute should be set to true if one
     * or more properties in the property sheet sections are marked required.
     */
    @Property(name = "requiredFields",
            displayName = "Required Field Legend",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.RequiredFieldsPropertyEditor")
            //CHECKSTYLE:ON
    private String requiredFields;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style;

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
    private String styleClass;

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
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Constructor.
     */
    public PropertySheet() {
        super();
        setRendererType("com.sun.webui.jsf.PropertySheet");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.PropertySheet";
    }

    /**
     * This method calculates the number of visible
     * {@link PropertySheetSection}s. A {@link PropertySheetSection} can be made
     * not visible by setting its rendered property to false. It is also
     * considered not visible if it contains no children (sub-sections or
     * properties).
     *
     * @return The number of visible sections.
     */
    public int getSectionCount() {
        // Return the answer
        return getVisibleSections().size();
    }

    /**
     * This method creates a {@code List} of visible (rendered=true)
     * {@link PropertySheetSection} components. {@link PropertySheetSection}s
     * must also contain some content to be considered visible.
     *
     * @return A {@code List} of visible {@link PropertySheetSection}
     * objects.
     */
    public List getVisibleSections() {
        int numChildren = getChildCount();

        // See if we've already figured this out
        if ((visibleSections != null) && (childCount == numChildren)) {
            return visibleSections;
        }
        childCount = numChildren;

        // Make sure we have children
        if (numChildren == 0) {
            visibleSections = new ArrayList(0);
            return visibleSections;
        }

        // Add the visible sections to the result List
        UIComponent child;
        List<PropertySheetSection> sections =
                new ArrayList<PropertySheetSection>();
        Iterator it = getChildren().iterator();
        while (it.hasNext()) {
            child = (UIComponent) it.next();
            if ((child instanceof PropertySheetSection)
                    && child.isRendered()) {
                if (((PropertySheetSection) child)
                        .getVisibleSectionChildren().size() > 0) {
                    sections.add((PropertySheetSection) child);
                }
            }
        }

        // Return the visible PropertySheetSections
        this.visibleSections = sections;
        return this.visibleSections;
    }

    /**
     * If the rendered property is true, render the begining of the current
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
        visibleSections = null;
        childCount = -1;
        super.encodeBegin(context);
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     * @param id id
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
     * @param rendered rendered
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * This {@code boolean} attribute allows you to control whether jump links
     * will be created at the top of this {@code PropertySheet} or not. The
     * default is NOT to create the links -- setting this attribute to "true"
     * turns this feature on.
     * @return {@code boolean}
     */
    public boolean isJumpLinks() {
        if (this.jumpLinksSet) {
            return this.jumpLinks;
        }
        ValueExpression vb = getValueExpression("jumpLinks");
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
     * This {@code boolean} attribute allows you to control whether jump links
     * will be created at the top of this {@code PropertySheet} or not. The
     * default is NOT to create the links -- setting this attribute to "true"
     * turns this feature on.
     *
     * @see #isJumpLinks()
     * @param newJumpLinks jumpLinks
     */
    public void setJumpLinks(final boolean newJumpLinks) {
        this.jumpLinks = newJumpLinks;
        this.jumpLinksSet = true;
    }

    /**
     * Specifies whether to display a required field legend in the upper right
     * area of the property sheet. This attribute should be set to true if one
     * or more properties in the property sheet sections are marked required.
     * @return String
     */
    public String getRequiredFields() {
        if (this.requiredFields != null) {
            return this.requiredFields;
        }
        ValueExpression vb = getValueExpression("requiredFields");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies whether to display a required field legend in the upper right
     * area of the property sheet. This attribute should be set to true if one
     * or more properties in the property sheet sections are marked required.
     *
     * @see #getRequiredFields()
     * @param newRequiredFields requiredFields
     */
    public void setRequiredFields(final String newRequiredFields) {
        this.requiredFields = newRequiredFields;
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
        this.jumpLinks = ((Boolean) values[1]);
        this.jumpLinksSet = ((Boolean) values[2]);
        this.requiredFields = (String) values[3];
        this.style = (String) values[4];
        this.styleClass = (String) values[5];
        this.visible = ((Boolean) values[6]);
        this.visibleSet = ((Boolean) values[7]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[8];
        values[0] = super.saveState(context);
        if (this.jumpLinks) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.jumpLinksSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.requiredFields;
        values[4] = this.style;
        values[5] = this.styleClass;
        if (this.visible) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        return values;
    }
}
