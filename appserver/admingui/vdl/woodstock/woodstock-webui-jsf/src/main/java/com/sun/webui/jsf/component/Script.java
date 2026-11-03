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
 * The Script component is can be used to refer to a JS file, by using
 * the URL attribute. The tag can also be used embed JS code within the
 * rendered HTML page.
 */
@Component(type = "com.sun.webui.jsf.Script",
        family = "com.sun.webui.jsf.Script",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_script",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_script_props")
        //CHECKSTYLE:ON
public final class Script extends UIComponentBase {

    /**
     * Defines the character (charset) encoding of the target URL. See iana.org
     * for a complete list of character encoding.
     */
    @Property(name = "charset",
            displayName = "Charset",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String charset = null;

    /**
     * Indicates the MIME type of the script. Default is
     * {@code "text/javascript"}.
     */
    @Property(name = "type",
            displayName = "Type",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String type = null;

    /**
     * Defines the absolute or relative URL to a file that contains the script.
     * Use this attribute to refer to a file instead of inserting the script
     * into your HTML document.
     */
    @Property(name = "url",
            displayName = "URL",
            category = "Data",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String url = null;

    /**
     * Default constructor.
     */
    public Script() {
        super();
        setRendererType("com.sun.webui.jsf.Script");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Script";
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
     * Defines the character (charset) encoding of the target URL. See iana.org
     * for a complete list of character encoding.
     * @return String
     */
    public String getCharset() {
        if (this.charset != null) {
            return this.charset;
        }
        ValueExpression vb = getValueExpression("charset");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines the character (charset) encoding of the target URL. See iana.org
     * for a complete list of character encoding.
     *
     * @see #getCharset()
     * @param newCharset charset
     */
    public void setCharset(final String newCharset) {
        this.charset = newCharset;
    }

    /**
     * Indicates the MIME type of the script. Default is
     * {@code "text/javascript"}.
     *
     * @return String
     */
    public String getType() {
        if (this.type != null) {
            return this.type;
        }
        ValueExpression vb = getValueExpression("type");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return "text/javascript";
    }

    /**
     * Indicates the MIME type of the script. Default is
     * {@code "text/javascript"}.
     *
     * @see #getType()
     * @param newType type
     */
    public void setType(final String newType) {
        this.type = newType;
    }

    /**
     * Defines the absolute or relative URL to a file that contains the script.
     * Use this attribute to refer to a file instead of inserting the script
     * into your HTML document.
     * @return String
     */
    public String getUrl() {
        if (this.url != null) {
            return this.url;
        }
        ValueExpression vb = getValueExpression("url");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines the absolute or relative URL to a file that contains the script.
     * Use this attribute to refer to a file instead of inserting the script
     * into your HTML document
     *
     * @see #getUrl()
     * @param newUrl URL
     */
    public void setUrl(final String newUrl) {
        this.url = newUrl;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.charset = (String) values[1];
        this.type = (String) values[2];
        this.url = (String) values[3];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[4];
        values[0] = super.saveState(context);
        values[1] = this.charset;
        values[2] = this.type;
        values[3] = this.url;
        return values;
    }
}
