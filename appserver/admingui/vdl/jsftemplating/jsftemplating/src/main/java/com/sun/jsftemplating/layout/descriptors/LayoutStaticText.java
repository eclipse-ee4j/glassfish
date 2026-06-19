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

import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * <p>
 * This class defines a LayoutStaticText. A LayoutStaticText describes a text to be output to the screen. This element
 * is NOT a <code>UIComponent</code>.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutStaticText extends LayoutComponent {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public LayoutStaticText(LayoutElement parent, String id, String value) {
        super(parent, id, LayoutDefinitionManager.getGlobalComponentType(null, "staticText"));
        addOption("value", value);
        _value = value;
    }

    /**
     *
     */
    public String getValue() {
        return _value;
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
    public boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
        // Get the ResponseWriter
        ResponseWriter writer = context.getResponseWriter();

        // Render the child UIComponent
//    if (staticText.isEscape()) {
//        writer.writeText(getValue(), "value");
//    } else {
        // This code depends on the side-effect of Util.setOption
        // converting the string to a ValueExpression if needed. The
        // "__value" is arbitrary.
        Object value = ComponentUtil.getInstance(context).setOption(context, "__value", getValue(), getLayoutDefinition(), component);

        // JSF 1.2 VB:
        if (value instanceof ValueExpression) {
            value = ((ValueExpression) value).getValue(context.getELContext());
        }

        if (value != null) {
            writer.write(value.toString());
        }
//    }

        // No children
        return false;
    }

    private String _value = null;
}
