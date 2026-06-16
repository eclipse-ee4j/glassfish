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

package com.sun.jsftemplating.component;

import com.sun.jsftemplating.layout.descriptors.LayoutComponent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This interface defines a method to find or create a child <code>UIComponent</code>. It is designed to be used in
 * conjunction with <code>UIComponent</code> implementations.
 * </p>
 *
 * @see TemplateComponent
 * @see com.sun.jsftemplating.component.ComponentUtil
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public interface ChildManager {

    /**
     * <p>
     * This method will find the request child UIComponent by id (the id is obtained from the given
     * {@link LayoutComponent}). If it is not found, it will attempt to create it from the supplied {@link LayoutComponent}.
     * </p>
     *
     * @param context FacesContext
     * @param descriptor {@link LayoutComponent} describing the UIComponent
     *
     * @return Requested UIComponent
     */
    UIComponent getChild(FacesContext context, LayoutComponent descriptor);
}
