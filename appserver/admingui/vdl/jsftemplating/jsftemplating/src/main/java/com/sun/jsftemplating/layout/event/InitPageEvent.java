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

/**
 * <p>
 * This event is fired for each page accessed during each request. This may be fired during the UIComponent tree
 * creation time, while restoring the View, when forwarding to a new page, or when simply accessing a page via Java
 * code.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class InitPageEvent extends EventObjectBase implements UIComponentHolder {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param component The <code>UIComponent</code> associated with this <code>EventObject</code> (likely null).
     */
    public InitPageEvent(Object component) {
        super(component);
    }
}
