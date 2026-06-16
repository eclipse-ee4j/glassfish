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
 * This factory is responsible for instantiating a <code>Hyperlink
 *    UIComponent</code> that is configured to submit an Ajax request.
 * </p>
 *
 * <p>
 * All properties are passed through to the underlying <code>Hyperlink</code> UIComponent.
 * </p>
 *
 * <p>
 * The {@link com.sun.jsftemplating.layout.descriptors.ComponentType} id for this factory is: "ajaxRequest".
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("ajaxRequest")
public class AjaxRequestFactory extends ComponentFactoryBase {

    /**
     * <p>
     * This is the factory method responsible for creating the <code>UIComponent</code>. You should specify the
     * <code>ajaxTarget</code> for this component. See {@link #AJAX_TARGET}.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     *
     * @return The newly created <code>AjaxRequest</code>.
     */
    @Override
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Create the UIComponent
        UIComponent comp = createComponent(context, COMPONENT_TYPE, descriptor, parent);

        // Set all the attributes / properties
        setOptions(context, descriptor, comp);

        // Setup the AjaxRequest Request
// FIXME: support javascript function to handle return value
// FIXME: support extra NVPs??  Maybe not needed?  UIParameter instead?  May be easier as a &n=v&... string.
        String clientId = comp.getClientId(context);
// FIXME: XXX DEAL WITH THIS!!!
// FIXME: BUG: JSF automatically converts the '&' characters in attributes to &amp; this causes a problem... talk to LH and JSF about this.
//    String extraNVPs = "'&" + clientId + "_submittedField=" + clientId + "'";
        String extraNVPs = "'" + clientId + "_submittedField=" + clientId + "'";
        String jsHandlerFunction = "null";
// FIXME: Support multiple targets?  If I render multiple sections of the UIComponent tree and return a document describing the results...
        String target = (String) descriptor.getEvaluatedOption(context, AJAX_TARGET, comp);
        if (target == null || target.equals("")) {
            target = clientId;
        }
        comp.getAttributes().put("onClick", "submitAjaxRequest('" + target + "', " + extraNVPs + ", " + jsHandlerFunction + "); return false;");

        // Return the component
        return comp;
    }

    /**
     * <p>
     * This is the property name that specifies the target for the Ajax request. If not specified, the link itself will be
     * the target (which is likely not the desired target of the XMLHttpRequest). The target should be the clientId (i.e.
     * the "id" you see in the HTML source) of the <code>UIComponent</code>.
     * </p>
     *
     * <p>
     * The property name is: <code>ajaxTarget</code>.
     * </p>
     */
    public static final String AJAX_TARGET = "ajaxTarget";

    /**
     * <p>
     * The <code>UIComponent</code> type that must be registered in the <code>faces-config.xml</code> file mapping to the
     * UIComponent class to use for this <code>UIComponent</code>.
     * </p>
     */
    public static final String COMPONENT_TYPE = "com.sun.webui.jsf.Hyperlink";
}
