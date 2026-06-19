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

package com.sun.jsftemplating.layout.event;

import jakarta.faces.component.UIComponent;

import java.util.EventObject;

/**
 * <p>
 * This class serves as the base class for <code>EventObject</code>s in this package.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class EventObjectBase extends EventObject implements UIComponentHolder {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * This constructor should not be used.
     * </p>
     */
    private EventObjectBase() {
        super(null);
    }

    /**
     * <p>
     * This constructor is protected to avoid direct instantiation, one of the sub-classes of this class should be used
     * instead.
     * </p>
     *
     * @param component The <code>UIComponent</code> associated with this <code>EventObject</code>.
     */
    protected EventObjectBase(UIComponent component) {
        super(component);
    }

    /**
     * <p>
     * This constructor should only be used when a <code>UIComponent</code> is not available.
     * </p>
     */
    protected EventObjectBase(Object obj) {
        super(obj);
    }

    /**
     * <P>
     * This method returns the <code>UIComponent</code> held by the <code>Object</code> implementing this interface (or null
     * if the event source is not a UIComponent).
     * </p>
     *
     * @return The <code>UIComponent</code>.
     */
    @Override
    public UIComponent getUIComponent() {
        Object source = getSource();
        if (source instanceof UIComponent) {
            return (UIComponent) getSource();
        }
        return null;
    }
}
