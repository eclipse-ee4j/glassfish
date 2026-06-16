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

package com.sun.jsftemplating.component.factory;

import com.sun.jsftemplating.layout.descriptors.LayoutComponent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;

/**
 * <p>
 * This interface must be implemented by all UIComponent factories. This enabled UIComponents to be created via a
 * consistent interface. This is critical to classes such as
 * {@link com.sun.jsftemplating.component.TemplateComponentBase} and {@link LayoutComponent}.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public interface ComponentFactory {

    /**
     * <p>
     * This is the factory method responsible for creating the <code>UIComponent</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     *
     * @return The newly created <code>UIComponent</code>.
     */
    UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent);

    /**
     * <p>
     * This method returns the extraInfo that was set for this <code>ComponentFactory</code> from the
     * {@link com.sun.jsftemplating.layout.descriptors.ComponentType}.
     * </p>
     */
    Serializable getExtraInfo();

    /**
     * <p>
     * This method is invoked from the {@link com.sun.jsftemplating.layout.descriptors.ComponentType} to provide more
     * information to the factory. For example, if the JSF component type was passed in, a single factory class could
     * instatiate multiple components the extra info that is passed in.
     * </p>
     *
     * <p>
     * Some factory implementations may want to use this method to execute intialization code for the factory based in the
     * value passed in.
     * </p>
     */
    void setExtraInfo(Serializable extraInfo);
}
