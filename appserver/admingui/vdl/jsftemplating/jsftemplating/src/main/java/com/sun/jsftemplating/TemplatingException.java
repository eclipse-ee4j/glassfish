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

package com.sun.jsftemplating;

import com.sun.jsftemplating.layout.descriptors.LayoutElement;

import jakarta.faces.component.UIComponent;

/**
 * <p>
 * This is the base exception class for other exception types that may be used in this project. It provides a means for
 * setting / obtaining the responsible {@link LayoutElement} and / or <code>UIComponent</code> associated with the
 * <code>Exception</code>. This information is optional and may be null.
 * </p>
 */
public class TemplatingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * This is the preferred constructor.
     * </p>
     */
    public TemplatingException(String msg, Throwable ex, LayoutElement elt, UIComponent comp) {
        super(msg, ex);

        // Setup the rest
        setResponsibleLayoutElement(elt);
        setResponsibleUIComponent(comp);
    }

    /**
     *
     */
    public TemplatingException() {
        super();
    }

    /**
     *
     */
    public TemplatingException(LayoutElement elt, UIComponent comp) {
        super();

        // Setup the rest
        setResponsibleLayoutElement(elt);
        setResponsibleUIComponent(comp);
    }

    /**
     *
     */
    public TemplatingException(Throwable ex) {
        super(ex);
    }

    /**
     *
     */
    public TemplatingException(Throwable ex, LayoutElement elt, UIComponent comp) {
        super(ex);

        // Setup the rest
        setResponsibleLayoutElement(elt);
        setResponsibleUIComponent(comp);
    }

    /**
     *
     */
    public TemplatingException(String msg) {
        super(msg);
    }

    /**
     * This is the preferred constructor if there is no root cause.
     */
    public TemplatingException(String msg, LayoutElement elt, UIComponent comp) {
        super(msg);

        // Setup the rest
        setResponsibleLayoutElement(elt);
        setResponsibleUIComponent(comp);
    }

    /**
     *
     */
    public TemplatingException(String msg, Throwable ex) {
        super(msg, ex);
    }

    /**
     * Allow the Exception to hold the responsible UIComponent
     */
    public void setResponsibleUIComponent(UIComponent comp) {
        _comp = comp;
    }

    /**
     * Allow the Exception to hold the responsible UIComponent
     */
    public UIComponent getResponsibleUIComponent() {
        return _comp;
    }

    /**
     * Allow the Exception to hold the responsible LayoutElement
     */
    public void setResponsibleLayoutElement(LayoutElement elt) {
        _elt = elt;
    }

    /**
     * Allow the responsible LayoutElement to be obtained.
     *
     * @return The responsible LayoutElement (null if not specified)
     */
    public LayoutElement getResponsibleLayoutElement() {
        return _elt;
    }

    private UIComponent _comp = null;
    private LayoutElement _elt = null;
}
