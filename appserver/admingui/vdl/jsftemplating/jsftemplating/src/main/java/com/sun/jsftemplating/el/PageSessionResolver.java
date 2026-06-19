/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2006, 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.el;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This {@link ELResolver} exists to resolve "page session" attributes. This concept, borrowed from NetDynamics / JATO,
 * stores data w/ the page so that it is available throughout the life of the page. This is longer than request scope,
 * but usually shorter than session.
 *
 * <p>
 * This implementation stores the attributes on the {@link UIViewRoot}.
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class PageSessionResolver extends ELResolver {

    /**
     * The name an expression must use when it explicitly specifies page session ("pageSession").
     */
    public static final String PAGE_SESSION = "pageSession";

    /**
     * The attribute key in which to store the "page" session Map.
     */
    private static final String PAGE_SESSION_KEY = "_ps";

    /**
     * Checks standard scopes and "page session" to see if the value exists.
     */
    @Override
    public Object getValue(ELContext elContext, Object base, Object property) {
        if (base != null) {
            return null;
        }

        if (property == null) {
            throw new PropertyNotFoundException();
        }

        elContext.setPropertyResolved(true);

        FacesContext facesContext = (FacesContext) elContext.getContext(FacesContext.class);
        ExternalContext externalContext = facesContext.getExternalContext();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        Map<String, Serializable> pageSession = getPageSession(facesContext, viewRoot);
        String attribute = (String) property;

        // Check to see if expression explicitly asks for PAGE_SESSION
        if (property.equals(PAGE_SESSION)) {
            // It does, return the Map
            if (pageSession == null) {
                // No Map! That's ok, create one...
                pageSession = createPageSession(facesContext, viewRoot);
            }
            return pageSession;
        }

        // Check page session exists and contains a property
        if (pageSession == null || !pageSession.containsKey(attribute)) {
            elContext.setPropertyResolved(false);
            return null;
        }

        // Check request map
        Object value = externalContext.getRequestMap().get(attribute);
        if (value != null) {
            return value;
        }

        // Check view map
        Map<String, Object> viewMap = viewRoot.getViewMap(false);
        if (viewMap != null) {
            value = viewMap.get(attribute);
            if (value != null) {
                return value;
            }
        }

        // Check session map
        value = externalContext.getSessionMap().get(attribute);
        if (value != null) {
            return value;
        }

        // Check application map
        value = externalContext.getApplicationMap().get(attribute);
        if (value != null) {
            return value;
        }

        // Not found updated property in the standard scopes.
        // Return value from page session.
        return pageSession.get(attribute);
    }

    @Override
    public Class<?> getType(ELContext elContext, Object base, Object property) {
        checkPropertyFound(base, property);
        return null;
    }

    @Override
    public void setValue(ELContext elContext, Object base, Object property, Object value) {
        checkPropertyFound(base, property);
    }

    @Override
    public boolean isReadOnly(ELContext elContext, Object base, Object property) {
        checkPropertyFound(base, property);
        return false;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext elContext, Object base) {
        return base == null ? String.class : null;
    }

    /**
     * This method provides access to the "page session" {@link Map}. If it doesn't exist, it returns {@code null}. If the
     * given {@link UIViewRoot} is {@code null}, then the current {@link UIViewRoot} will be used.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Serializable> getPageSession(FacesContext facesContext, UIViewRoot viewRoot) {
        if (viewRoot == null) {
            viewRoot = facesContext.getViewRoot();
        }
        return (Map<String, Serializable>) viewRoot.getAttributes().get(PAGE_SESSION_KEY);
    }

    /**
     * This method will create a new "page session" {@code Map} if it doesn't exist yet. It will overwrite any existing
     * "page session" {@code Map}, so be careful.
     */
    public static Map<String, Serializable> createPageSession(FacesContext facesContext, UIViewRoot viewRoot) {
        if (viewRoot == null) {
            viewRoot = facesContext.getViewRoot();
        }

        // Create it...
        Map<String, Serializable> pageSession = new HashMap<>(4);

        // Store it...
        viewRoot.getAttributes().put(PAGE_SESSION_KEY, pageSession);

        // Return it...
        return pageSession;
    }

    private static void checkPropertyFound(Object base, Object property) {
        if (base == null && property == null) {
            throw new PropertyNotFoundException();
        }
    }
}
