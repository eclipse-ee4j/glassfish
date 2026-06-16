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

package com.sun.jsftemplating.component.factory.sun;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This factory is responsible for creating a <code>ImageComponent</code> UIComponent.
 * </p>
 *
 * <p>
 * The {@link com.sun.jsftemplating.layout.descriptors.ComponentType} id for this factory is: "sun:image".
 * </p>
 *
 * @author Rick Ratta
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("sun:image")
public class ImageComponentFactory extends ComponentFactoryBase {

    /**
     * <p>
     * This is the factory method responsible for creating the <code>ImageComponent</code> UIComponent.
     * </p>
     *
     * @param context The FacesContext
     *
     * @param descriptor The {@link LayoutComponent} descriptor that is associated with the requested
     * <code>ImageComponent</code>.
     *
     * @param parent The parent UIComponent
     *
     * @return The newly created <code>ImageComponent</code>.
     */
    @Override
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Create the UIComponent
        UIComponent comp = createComponent(context, COMPONENT_TYPE, descriptor, parent);

        // Set all the attributes / properties (allow these to override theme)
        setOptions(context, descriptor, comp);

        // Return the value
        return comp;
    }

    /**
     * <p>
     * The <code>UIComponent</code> type that must be registered in the <code>faces-config.xml</code> file mapping to the
     * UIComponent class to use for this <code>UIComponent</code>.
     * </p>
     */
    public static final String COMPONENT_TYPE = "com.sun.webui.jsf.Image";
}
