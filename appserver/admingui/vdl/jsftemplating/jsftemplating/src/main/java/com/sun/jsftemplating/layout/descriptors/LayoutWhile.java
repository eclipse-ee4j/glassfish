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

import com.sun.jsftemplating.el.PermissionChecker;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.event.AfterLoopEvent;
import com.sun.jsftemplating.layout.event.BeforeLoopEvent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.IOException;

/**
 * <p>
 * This class defines a LayoutWhile {@link LayoutElement}. The LayoutWhile provides the functionality necessary to
 * iteratively display a portion of the layout tree. The condition is a boolean equation and may use "$...{...}" type
 * expressions to substitute values.
 * </p>
 *
 * @see com.sun.jsftemplating.el.VariableResolver
 * @see com.sun.jsftemplating.el.PermissionChecker
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutWhile extends LayoutIf {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public LayoutWhile(LayoutElement parent, String condition) {
        super(parent, condition, LayoutDefinitionManager.getGlobalComponentType(null, "while"));
    }

    /**
     * <p>
     * This method always returns true. The condition is checked in {@link #shouldContinue(UIComponent)} instead of here
     * because the {@link #encode(FacesContext, UIComponent)} method evaluates the condition and calls the super. Performing
     * the check here would cause the condition to be evaluated twice.
     * </p>
     *
     * @param context The FacesContext
     * @param component The UIComponent
     *
     * @return true
     */
    @Override
    public boolean encodeThis(FacesContext context, UIComponent component) {
        return true;
    }

    /**
     * <p>
     * This method returns true if the condition of this LayoutWhile is met, false otherwise. This provides the
     * functionality for iteratively displaying a portion of the layout tree.
     * </p>
     *
     * @param component The UIComponent
     *
     * @return true if children are to be rendered, false otherwise.
     */
    protected boolean shouldContinue(UIComponent component) {
        PermissionChecker checker = new PermissionChecker(this, component, (String) getOption("condition"));
        return checker.hasPermission();
    }

    /**
     * <p>
     * This implementation overrides the parent <code>encode</code> method. It does this to cause the encode process to loop
     * while {@link #shouldContinue(UIComponent)} returns true. Currently there is no infinite loop checking, so be careful.
     * </p>
     *
     * @param context The FacesContext
     * @param component The UIComponent
     */
    @Override
    public void encode(FacesContext context, UIComponent component) throws IOException {
        dispatchHandlers(context, BEFORE_LOOP, new BeforeLoopEvent(component));
        while (shouldContinue(component)) {
            super.encode(context, component);
        }
        dispatchHandlers(context, AFTER_LOOP, new AfterLoopEvent(component));
    }

    /**
     * <p>
     * This is the event "type" for {@link com.sun.jsftemplating.layout.descriptors.handler.Handler} elements to be invoked
     * after this LayoutWhile is processed (outside loop).
     * </p>
     */
    public static final String AFTER_LOOP = "afterLoop";

    /**
     * <p>
     * This is the event "type" for {@link com.sun.jsftemplating.layout.descriptors.handler.Handler} elements to be invoked
     * before this LayoutWhile is processed (outside loop).
     * </p>
     */
    public static final String BEFORE_LOOP = "beforeLoop";
}
