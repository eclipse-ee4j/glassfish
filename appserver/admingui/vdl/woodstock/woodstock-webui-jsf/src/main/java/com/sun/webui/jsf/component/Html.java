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
 * The HTML component is used to create the &lt;html&gt; element.
 */
@Component(type = "com.sun.webui.jsf.Html",
        family = "com.sun.webui.jsf.Html",
        displayName = "Html", tagName = "html",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_html",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_html_props")
        //CHECKSTYLE:ON
public final class Html extends UIComponentBase {

    /**
     * Sets the language code for this document.
     */
    @Property(name = "lang", displayName = "Lang", category = "Advanced")
    private String lang;

    /**
     * Defines the XML namespace attribute. Default value is:
     * {@code http://www.w3.org/1999/xhtml}
     */
    @Property(name = "xmlns",
            displayName = "XML Namespace",
            category = "Advanced",
            isDefault = true)
    private String xmlns;

    /**
     * Construct a new {@code Html}.
     */
    public Html() {
        super();
        setRendererType("com.sun.webui.jsf.Html");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Html";
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

    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * Sets the language code for this document.
     * @return String
     */
    public String getLang() {
        if (this.lang != null) {
            return this.lang;
        }
        ValueExpression vb = getValueExpression("lang");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Sets the language code for this document.
     *
     * @see #getLang()
     * @param newLang lang
     */
    public void setLang(final String newLang) {
        this.lang = newLang;
    }

    /**
     * Defines the XML namespace attribute. Default value is:
     * {@code http://www.w3.org/1999/xhtml}
     * @return String
     */
    public String getXmlns() {
        if (this.xmlns != null) {
            return this.xmlns;
        }
        ValueExpression vb = getValueExpression("xmlns");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return "http://www.w3.org/1999/xhtml";
    }

    /**
     * Defines the XML namespace attribute. Default value is:
     * {@code http://www.w3.org/1999/xhtml}
     *
     * @see #getXmlns()
     * @param newXmlns xmlns
     */
    public void setXmlns(final String newXmlns) {
        this.xmlns = newXmlns;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.lang = (String) values[1];
        this.xmlns = (String) values[2];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = super.saveState(context);
        values[1] = this.lang;
        values[2] = this.xmlns;
        return values;
    }
}
