/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.descriptors;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * <p>
 * This class defines a LayoutAttribute. A LayoutAttribute provides a means to write an attribute for the current markup
 * tag. A markup tag must be started, but not yet closed for this to work.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutAttribute extends LayoutElementBase implements LayoutElement {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public LayoutAttribute(LayoutElement parent, String name, String value, String property) {
        super(parent, name);
        _name = name;
        _value = value;
        _property = property;
    }

    /**
     *
     */
    public String getName() {
        return _name;
    }

    /**
     *
     */
    public String getValue() {
        return _value;
    }

    /**
     *
     */
    public String getProperty() {
        return _property;
    }

    /**
     * <p>
     * This method displays the text described by this component. If the text includes an EL expression, it will be
     * evaluated. It returns false to avoid attempting to render children.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param component The <code>UIComponent</code>
     *
     * @return false
     */
    @Override
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
        // Get the ResponseWriter
        ResponseWriter writer = context.getResponseWriter();

        // Render...
        Object value = resolveValue(context, component, getValue());
        if (value != null && !value.toString().trim().equals("")) {
            String name = getName();
            String prop = getProperty();
            if (prop == null) {
                // Use the name if property is not supplied
                prop = name;
            } else if (prop.equals("null")) {
                prop = null;
            }
            writer.writeAttribute(name, value, prop);
        }

        // No children
        return false;
    }

    private String _name = null;
    private String _value = null;
    private String _property = null;
}
