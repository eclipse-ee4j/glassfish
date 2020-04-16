/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: BaseRenderer.java,v 1.3 2004/11/14 07:33:14 tcfujii Exp $
 */

package components.renderkit;


import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>Convenient base class for <code>Renderer</code> implementations.</p>
 */

public abstract class BaseRenderer extends Renderer {

    public static final String BUNDLE_ATTR = "com.sun.faces.bundle";


    public String convertClientId(FacesContext context, String clientId) {
        return clientId;
    }


    protected String getKeyAndLookupInBundle(FacesContext context,
                                             UIComponent component,
                                             String keyAttr)
        throws MissingResourceException {
        String key = null, bundleName = null;
        ResourceBundle bundle = null;

        key = (String) component.getAttributes().get(keyAttr);
        bundleName = (String) component.getAttributes().get(BUNDLE_ATTR);

        // if the bundleName is null for this component, it might have
        // been set on the root component.
        if (bundleName == null) {
            UIComponent root = context.getViewRoot();

            bundleName = (String) root.getAttributes().get(BUNDLE_ATTR);
        }
        // verify our component has the proper attributes for key and bundle.
        if (null == key || null == bundleName) {
            throw new MissingResourceException("Can't load JSTL classes",
                                               bundleName, key);
        }

        // verify the required Class is loadable
        // PENDING(edburns): Find a way to do this once per ServletContext.
        if (null == Thread.currentThread().getContextClassLoader().
            getResource("jakarta.servlet.jsp.jstl.fmt.LocalizationContext")) {
            Object[] params = {
                "jakarta.servlet.jsp.jstl.fmt.LocalizationContext"
            };
            throw new MissingResourceException("Can't load JSTL classes",
                                               bundleName, key);
        }

        // verify there is a ResourceBundle in scoped namescape.
        jakarta.servlet.jsp.jstl.fmt.LocalizationContext locCtx = null;
        if (null == (locCtx = (jakarta.servlet.jsp.jstl.fmt.LocalizationContext)
            (Util.getValueBinding(bundleName)).getValue(context)) ||
            null == (bundle = locCtx.getResourceBundle())) {
            throw new MissingResourceException("Can't load ResourceBundle ",
                                               bundleName, key);
        }

        return bundle.getString(key);
    }


    protected void encodeRecursive(FacesContext context, UIComponent component)
        throws IOException {

        component.encodeBegin(context);
        if (component.getRendersChildren()) {
            component.encodeChildren(context);
        } else {
            Iterator kids = component.getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                encodeRecursive(context, kid);
            }
        }
        component.encodeEnd(context);

    }


}
