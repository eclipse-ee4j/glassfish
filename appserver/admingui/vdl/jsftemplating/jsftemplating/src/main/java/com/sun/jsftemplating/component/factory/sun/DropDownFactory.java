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
import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.util.Util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// FIXME: Document
/**
 * <p>
 * This factory is responsible for instantiating a <code>DropDrown
 *    UIComponent</code>.
 * </p>
 *
 * <p>
 * The {@link com.sun.jsftemplating.layout.descriptors.ComponentType} id for this factory is: "sun:dropDown".
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("sun:dropDown")
public class DropDownFactory extends ComponentFactoryBase {

    /**
     * <p>
     * This is the factory method responsible for creating the <code>UIComponent</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     *
     * @return The newly created <code>DropDown</code>.
     */
    @Override
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Create the UIComponent
        UIComponent comp = createComponent(context, getComponentType(), descriptor, parent);

        // Set all the attributes / properties
        setOptions(context, descriptor, comp);

        // Check to see if the user is passing in Lists to be converted to a
        // List of Option objects for the "items" property.
        ComponentUtil compUtil = ComponentUtil.getInstance(context);
        Object labels = compUtil.resolveValue(context, descriptor, comp, descriptor.getOption("labels"));
        if (labels != null) {
            List optionList = new ArrayList();
            Object values = compUtil.resolveValue(context, descriptor, comp, descriptor.getOption("values"));
            if (values == null) {
                values = labels;
            }

            try {
                // Use reflection (for now) to avoid a build dependency
                // Find the Option constuctor...
                Constructor optConst = Util.loadClass("com.sun.webui.jsf.model.Option", this).getConstructor(Object.class, String.class);

                if (values instanceof List) {
                    // We have a List, we need to convert to Option objects.
                    Iterator<Object> it = ((List<Object>) labels).iterator();
                    for (Object obj : (List<Object>) values) {
                        optionList.add(optConst.newInstance(obj, it.next().toString()));
                    }
                } else if (values instanceof Object[]) {
                    Object[] valArr = (Object[]) values;
                    Object[] labArr = (Object[]) labels;
                    int len = valArr.length;
                    // Convert the array to Option objects
                    for (int count = 0; count < len; count++) {
                        optionList.add(optConst.newInstance(valArr[count], labArr[count].toString()));
                    }
                }
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }

            // Set the options
            comp.getAttributes().put("items", optionList);
        }

        // Return the component
        return comp;
    }

    /**
     * <p>
     * This method returns the ComponentType of this component. It is implemented this way to allow subclasses to easily
     * provide a different ComponentType.
     * </p>
     */
    protected String getComponentType() {
        return COMPONENT_TYPE;
    }

    /**
     * <p>
     * The <code>UIComponent</code> type that must be registered in the <code>faces-config.xml</code> file mapping to the
     * UIComponent class to use for this <code>UIComponent</code>.
     * </p>
     */
    public static final String COMPONENT_TYPE = "com.sun.webui.jsf.DropDown";
}
