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

 /*
 * PasswordFieldRenderer.java
 */
package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.PasswordField;
import com.sun.webui.jsf.util.MessageUtil;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.IOException;

/**
 * Renderer for PasswordFieldRenderer {@link PasswordField} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.PasswordField"))
public final class PasswordFieldRenderer extends FieldRenderer {

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (!(component instanceof PasswordField)) {
            Object[] params = {
                component.toString(),
                this.getClass().getName(),
                PasswordField.class.getName()
            };
            String message = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "Renderer.component", params);
            throw new FacesException(message);
        }

        super.renderField(context, (PasswordField) component, "password",
                getStyles(context));
    }
}
