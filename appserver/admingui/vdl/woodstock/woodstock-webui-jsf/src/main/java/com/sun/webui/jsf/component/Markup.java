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
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The Markup component allows HTML elements to be inserted into the JSP page
 * where HTML is not permitted inside a JSF tag.
 */
@Component(type = "com.sun.webui.jsf.Markup",
        family = "com.sun.webui.jsf.Markup",
        displayName = "Markup",
        tagName = "markup",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_markup",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_markup_props")
        //CHECKSTYLE:ON
public final class Markup extends UIComponentBase {

    /**
     * Add the rest of the attribute name="value" type pairs inside this
     * attribute. The inserted attributes will need to be escaped.
     */
    @Property(name = "extraAttributes",
            displayName = "Extra Attributes",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String extraAttributes;

    /**
     * Flag indicating that tag is a singleton tag and that it should end with a
     * trailing {@code "/"}.
     */
    @Property(name = "singleton",
            displayName = "Single Tag",
            category = "Advanced")
    private boolean singleton;

    /**
     * singleton set flag.
     */
    private boolean singletonSet;

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
     * Name of the HTML element to render.
     */
    @Property(name = "tag",
            displayName = "Tag Name",
            category = "Advanced",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String tag;

    /**
     * Construct a new {@code Markup}.
     */
    public Markup() {
        super();
        setRendererType("com.sun.webui.jsf.Markup");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Markup";
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
     * Add the rest of the attribute name="value" type pairs inside this
     * attribute. The inserted attributes will need to be escaped.
     * @return String
     */
    public String getExtraAttributes() {
        if (this.extraAttributes != null) {
            return this.extraAttributes;
        }
        ValueExpression vb = getValueExpression("extraAttributes");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Add the rest of the attribute name="value" type pairs inside this
     * attribute. The inserted attributes will need to be escaped.
     *
     * @see #getExtraAttributes()
     * @param newExtraAttributes extraAttributes
     */
    public void setExtraAttributes(final String newExtraAttributes) {
        this.extraAttributes = newExtraAttributes;
    }

    /**
     * Flag indicating that tag is a singleton tag and that it should end with a
     * trailing {@code "/"}.
     * @return {@code boolean}
     */
    public boolean isSingleton() {
        if (this.singletonSet) {
            return this.singleton;
        }
        ValueExpression vb = getValueExpression("singleton");
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
     * Flag indicating that tag is a singleton tag and that it should end with a
     * trailing {@code "/"}.
     *
     * @see #isSingleton()
     * @param newSingleton singleton
     */
    public void setSingleton(final boolean newSingleton) {
        this.singleton = newSingleton;
        this.singletonSet = true;
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
     * Name of the HTML element to render.
     * @return String
     */
    public String getTag() {
        if (this.tag != null) {
            return this.tag;
        }
        ValueExpression vb = getValueExpression("tag");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Name of the HTML element to render.
     *
     * @see #getTag()
     * @param newTag tag
     */
    public void setTag(final String newTag) {
        this.tag = newTag;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.extraAttributes = (String) values[1];
        this.singleton = ((Boolean) values[2]);
        this.singletonSet = ((Boolean) values[3]);
        this.style = (String) values[4];
        this.styleClass = (String) values[5];
        this.tag = (String) values[6];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[7];
        values[0] = super.saveState(context);
        values[1] = this.extraAttributes;
        if (this.singleton) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.singletonSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        values[4] = this.style;
        values[5] = this.styleClass;
        values[6] = this.tag;
        return values;
    }
}
