/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.descriptors.handler;

import com.sun.jsftemplating.component.ComponentUtil;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This class implements the OutputType interface to provide a way to get/set Output values via standard EL.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ELOutputType implements OutputType {

    /**
     * <p>
     * This method is responsible for retrieving the value of the output from EL. The "key" is expected to be the EL
     * expression, including the opening and closing delimiters: #{some.el}
     * </p>
     *
     * @param context The HandlerContext.
     *
     * @param outDesc The {@link IODescriptor} for this output in which we are obtaining a value (not used).
     *
     * @param key The EL expression to evaluate.
     *
     * @return The requested value.
     */
    @Override
    public Object getValue(HandlerContext context, IODescriptor outDesc, String key) {
        if (key == null) {
            throw new IllegalArgumentException("ELOutputType's key may not be null!");
        }

        // Make sure it is an EL expression...
        if (!key.startsWith("#{")) {
            // If the key is not an EL expression, make it one... while this
            // may cover some user-errors, I think it adds a nice ease-of-use
            // feature that people may like...
            key = "#{requestScope['" + key + "']}";
        }

        // See if we can find the UIComp...
        UIComponent uicomp = null;
        Object eventObj = context.getEventObject();
        if (eventObj instanceof UIComponent) {
            uicomp = (UIComponent) eventObj;
        }

        // Get it from the EL expression
        FacesContext ctx = context.getFacesContext();
        return ComponentUtil.getInstance(ctx).resolveValue(ctx, context.getLayoutElement(), uicomp, key);
    }

    /**
     * <p>
     * This method is responsible for setting the value of the output via EL. The "key" is expected to be the EL expression,
     * including the opening and closing delimiters: #{some.el}
     * </p>
     *
     * @param context The HandlerContext.
     *
     * @param outDesc The IODescriptor for this Output value in which to obtain the value. Used to pull EL expression from
     * the Handler.
     *
     * @param key The EL expression to evaluate.
     *
     * @param value The value to set.
     */
    @Override
    public void setValue(HandlerContext context, IODescriptor outDesc, String key, Object value) {
        // It should never be null...
        if (key == null) {
            throw new IllegalArgumentException("ELOutputType's key may not be null!");
        }

        // Make sure it is an EL expression...
        if (!key.startsWith("#{")) {
            // If the key is not an EL expression, make it one... while this
            // may cover some user-errors, I think it adds a nice ease-of-use
            // feature that people may like...
            key = "#{requestScope['" + key + "']}";
        }

        // Set it in EL
        FacesContext facesContext = context.getFacesContext();
        ELContext elctx = facesContext.getELContext();
        ValueExpression ve = facesContext.getApplication().getExpressionFactory().createValueExpression(elctx, key, Object.class);
        try {
            ve.setValue(elctx, value);
        } catch (ELException ex) {
            throw new RuntimeException("Unable to set handler output value named \"" + outDesc.getName() + "\" mapped to EL expression \"" + key
                    + "\" on the \"" + context.getLayoutElement().getUnevaluatedId() + "\" element.", ex);
        }
    }
}
