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
import com.sun.jsftemplating.component.dataprovider.MultipleListDataProvider;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.util.LogUtil;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This factory is responsible for instantiating a <code>TableRowGroup
 *    UIComponent</code>.
 * </p>
 *
 * <p>
 * The {@link com.sun.jsftemplating.layout.descriptors.ComponentType} id for this factory is: "sun:tableRowGroup".
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("sun:tableRowGroup")
public class TableRowGroupFactory extends ComponentFactoryBase {

    /**
     * <p>
     * This is the factory method responsible for creating the <code>UIComponent</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     *
     * @return The newly created <code>TableRowGroup</code>.
     */
    @Override
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Create the UIComponent
        UIComponent comp = createComponent(context, COMPONENT_TYPE, descriptor, parent);

        // Set all the attributes / properties
        setOptions(context, descriptor, comp);

        // Handle "data" option specially...
        ComponentUtil compUtil = ComponentUtil.getInstance(context);
        Object data = compUtil.resolveValue(context, descriptor, comp, descriptor.getOption("data"));
        if (data != null) {
            // Create a DataProvider
            if (!(data instanceof List)) {
                throw new IllegalArgumentException("TableRowGroupFactory " + "only supports values of type \"java.util.List\" " + "for the 'data' attribute.");
            }
            if (((List) data).size() > 0 && !(((List) data).get(0) instanceof List)) {
                Object obj = ((List) data).get(0);
                if (obj == null) {
                    // In this case, treat this as an empty List and log a
                    // warning... this prevents some cases from blowing up
                    // just b/c there is no data.
                    if (LogUtil.configEnabled()) {
                        LogUtil.config("JSFT0008");
                    }
                    ((List) data).set(0, new ArrayList());
                } else {
                    obj = obj.getClass().getName();
                    // We don't have a List of List of Object!
                    throw new IllegalArgumentException(
                            "TableRowGroupFactory " + "expects a List<List<Object>>.  Where the outer " + "List should be a List of sources, and the inner"
                                    + " List should be a List of rows.  However, the " + "following was passed in: List<" + obj + ">.");
                }
            }
            List<List<Object>> lists = (List<List<Object>>) data;
            Object dataProvider = new MultipleListDataProvider(lists);

            // Remove the data object from the UIComponent, not needed
            Map<String, Object> atts = comp.getAttributes();
            atts.remove("data");
// FIXME: This stores the *data* in the UIComponent... change to use a #{} value binding to push the data somewhere else. Session?? Configurable?
            atts.put("sourceData", dataProvider);
        }

        // Return the component
        return comp;
    }

    /**
     * <p>
     * The <code>UIComponent</code> type that must be registered in the <code>faces-config.xml</code> file mapping to the
     * UIComponent class to use for this <code>UIComponent</code>.
     * </p>
     */
    public static final String COMPONENT_TYPE = "com.sun.webui.jsf.TableRowGroup";
}
