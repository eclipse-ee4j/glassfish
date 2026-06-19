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

package com.sun.jsftemplating.component.factory.basic;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;

/**
 * <p>
 * This factory is capable of creating any UIComponent that can be created via the
 * <code>Application.createComponent(componentType)</code> method. It requires that the <code>componentType</code>
 * property be set indicating what type of component should be instantiated.
 * </p>
 *
 * <p>
 * The {@link com.sun.jsftemplating.layout.descriptors.ComponentType} id for this factory is: "component".
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("component")
public class GenericFactory extends ComponentFactoryBase {

    /**
     * <p>
     * This is the factory method responsible for creating the <code>UIComponent</code>. It requires that the
     * "componentType" attribute be supplied with a valid JSF ComponentType. This may be supplied in the page on a per-use
     * basis, or on an instance of this Factory via the {@link ComponentFactoryBase#setExtraInfo(Serializable)} method.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     *
     * @return The newly created <code>UIComponent</code>.
     */
    @Override
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Determine the componentType
        String componentType = (String) descriptor.getEvaluatedOption(context, COMPONENT_TYPE, parent);
        if (componentType == null) {
            Serializable extraInfo = getExtraInfo();
            if (extraInfo != null) {
                // This component allows the (default) CompnentType to be set
                // on the factory.
                componentType = extraInfo.toString();
            } else {
                throw new IllegalArgumentException("\"&gt;component&lt;\" requires a \"" + COMPONENT_TYPE + "\" property to be set to the componentType of the "
                        + "component you wish to create.");
            }
        }

        // Create the UIComponent
        UIComponent comp = createComponent(context, componentType, descriptor, parent);

        // Set all the attributes / properties
        setOptions(context, descriptor, comp);

        // Return the component
        return comp;
    }

    /**
     * <p>
     * This the is the property name ("componentType") that will be used to define the componentType to be used to create
     * the <code>UIComponent</code>. This much match the defined component type in the faces-config.xml file (this is
     * usually stored along with the component .class files in the component's jar file).
     * </p>
     */
    public static final String COMPONENT_TYPE = "componentType";
}
