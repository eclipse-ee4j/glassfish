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
 * The Meta component is used to create an HTML {@code <meta>} element in the
 * rendered HTML page.
 */
@Component(type = "com.sun.webui.jsf.Meta", family = "com.sun.webui.jsf.Meta",
        displayName = "Meta", tagName = "meta",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_meta",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_meta_props")
        //CHECKSTYLE:ON
public final class Meta extends UIComponentBase {

    /**
     * The content attribute is used to specify the data to associate with a
     * name attribute or httpEquiv attribute in the {@code webuijsf:meta} tag.
     */
    @Property(name = "content",
            displayName = "Content",
            category = "Appearance",
            isDefault = true)
    private String content;

    /**
     * The httpEquiv attribute is used to specify a value for the http-equiv
     * attribute of an HTML Meta element. The http-equiv attribute specifies
     * HTTP properties that the web server can use in the HTTP response
     * header.
     */
    @Property(name = "httpEquiv",
            displayName = "HTTP header",
            category = "Appearance")
    private String httpEquiv;

    /**
     * The identifier that is assigned to a property in the meta element. The
     * content attribute provides the actual content of the property that is
     * identified by the name attribute.
     */
    @Property(name = "name", displayName = "Name", category = "Appearance")
    private String name;

    /**
     * Defines a format to be used to interpret the value of the content
     * attribute.
     */
    @Property(name = "scheme", displayName = "Scheme", category = "Advanced")
    private String scheme;

    /**
     * Construct a new {@code Meta}.
     */
    public Meta() {
        super();
        setRendererType("com.sun.webui.jsf.Meta");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Meta";
    }

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
     * The content attribute is used to specify the data to associate with a
     * name attribute or httpEquiv attribute in the {@code webuijsf:meta} tag.
     * @return String
     */
    public String getContent() {
        if (this.content != null) {
            return this.content;
        }
        ValueExpression vb = getValueExpression("content");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The content attribute is used to specify the data to associate with a
     * name attribute or httpEquiv attribute in the {@code webuijsf:meta} tag.
     *
     * @param newContent content
     * @see #getContent()
     */
    public void setContent(final String newContent) {
        this.content = newContent;
    }

    /**
     * The httpEquiv attribute is used to specify a value for the http-equiv
     * attribute of an HTML Meta element.The http-equiv attribute specifies HTTP
     * properties that the web server can use in the HTTP response header.
     *
     * @return String
     */
    public String getHttpEquiv() {
        if (this.httpEquiv != null) {
            return this.httpEquiv;
        }
        ValueExpression vb = getValueExpression("httpEquiv");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The httpEquiv attribute is used to specify a value for the http-equiv
     * attribute of an HTML Meta element. The http-equiv attribute specifies
     * HTTP properties that the web server can use in the HTTP response
     * header.
     *
     * @see #getHttpEquiv()
     * @param newHttpEquiv httpEquiv
     */
    public void setHttpEquiv(final String newHttpEquiv) {
        this.httpEquiv = newHttpEquiv;
    }

    /**
     * The identifier that is assigned to a property in the meta element. The
     * content attribute provides the actual content of the property that is
     * identified by the name attribute.
     * @return String
     */
    public String getName() {
        if (this.name != null) {
            return this.name;
        }
        ValueExpression vb = getValueExpression("name");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The identifier that is assigned to a property in the meta element. The
     * content attribute provides the actual content of the property that is
     * identified by the name attribute.
     *
     * @see #getName()
     * @param newName name
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Defines a format to be used to interpret the value of the content
     * attribute.
     * @return String
     */
    public String getScheme() {
        if (this.scheme != null) {
            return this.scheme;
        }
        ValueExpression vb = getValueExpression("scheme");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Defines a format to be used to interpret the value of the content
     * attribute.
     *
     * @see #getScheme()
     * @param newScheme scheme
     */
    public void setScheme(final String newScheme) {
        this.scheme = newScheme;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.content = (String) values[1];
        this.httpEquiv = (String) values[2];
        this.name = (String) values[3];
        this.scheme = (String) values[4];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[5];
        values[0] = super.saveState(context);
        values[1] = this.content;
        values[2] = this.httpEquiv;
        values[3] = this.name;
        values[4] = this.scheme;
        return values;
    }
}
