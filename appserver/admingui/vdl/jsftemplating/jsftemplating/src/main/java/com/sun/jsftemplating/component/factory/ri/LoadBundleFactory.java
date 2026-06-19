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

package com.sun.jsftemplating.component.factory.ri;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.EventComponent;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerDefinition;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <p>
 * When using JSP as the view technology for JSF, you not only have components but also JSP tags that interact with JSF.
 * In JSFTemplating the recommended approach to doing this is to use handlers. This offers a clean way to execute
 * arbitrary Java code. While that is still the recommendation, this class is provided for added flexibility. The
 * purpose of this class is to read a ResourceBundle and make it available to the page. The better approach would be to
 * use the {@link com.sun.jsftemplating.handlers.ScopeHandlers#setResourceBundle} handler.
 * </p>
 *
 * <p>
 * The &gt;f:loadBundle&lt; tag does not represent a component, so this handler does not create a component, it returns
 * the <code>parent</code> (which is passed in) after configuring the <code>ResourceBundle</code>.
 * </p>
 *
 * <p>
 * The {@link com.sun.jsftemplating.layout.descriptors.ComponentType} id for this factory is: "f:loadBundle". It
 * requires a <code>basename</code> and a <code>var</code> property to be passed in. Optionally the <code>locale</code>
 * can be passed in (this is not a feature of the JSF version, but an extra feature supported by the handler in
 * JSFTemplating.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("f:loadBundle")
public class LoadBundleFactory extends ComponentFactoryBase {

    /**
     * <p>
     * This is the factory method loads a <code>ResourceBundle</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor.
     * @param parent The parent <code>UIComponent</code> (not used)
     *
     * @return <code>parent</code>.
     */
    @Override
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Create an Event component for this
        EventComponent event = new EventComponent();
        if (parent != null) {
            addChild(context, descriptor, parent, event);
        }

        // Get the inputs
        String baseName = (String) descriptor.getEvaluatedOption(context, "basename", parent);
        String var = (String) descriptor.getEvaluatedOption(context, "var", parent);
        Locale locale = (Locale) descriptor.getEvaluatedOption(context, "locale", parent);

        // Create a handler (needed to execute code each time displayed)...
        HandlerDefinition def = LayoutDefinitionManager.getGlobalHandlerDefinition("setResourceBundle");
        Handler handler = new Handler(def);
        handler.setInputValue("bundle", baseName);
        handler.setInputValue("key", var);
        handler.setInputValue("locale", locale);
        List<Handler> handlers = new ArrayList<>();
        handlers.add(handler);
        event.getAttributes().put("beforeEncode", handlers);

        // Return (parent)
        return event;
    }
}
