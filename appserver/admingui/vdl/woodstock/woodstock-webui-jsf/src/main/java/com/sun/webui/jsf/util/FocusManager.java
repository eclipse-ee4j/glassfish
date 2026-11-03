/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.util;

import jakarta.faces.context.FacesContext;

/**
 * Methods to manage focus during a request and response. The actual component
 * that receives the focus in the rendered page may not be the component
 * identified by these interfaces. The {@code com.sun.webui.component.Body}
 * component can be used explicitly set the component that will receive the
 * focus in the rendered page.
 */
public final class FocusManager {

    /**
     * Cannot be instanciated.
     */
    private FocusManager() {
    }

    /**
     * Request parameter element id that maintains the last element that
     * received the focus.
     */
    public static final String FOCUS_FIELD_ID
            = "com_sun_webui_util_FocusManager_focusElementId";

    /**
     * Return the client id of the component that is considered to have the
     * focus during this request.This may not be the component that has the
     * focus when the response is rendered.
     * <p>
     * This interface expects {@code setRequestFocusElementId} was called the
     * actual client id of the component that is to receive the focus. This
     * means that the caller was responsible for calling
     * {@code ComplexComponent.getFocusElementId} if necessary.
     * </p>
     * @param context faces context
     * @return String, may be {@code null}
     */
    public static String getRequestFocusElementId(final FacesContext context) {
        return (String) context.getExternalContext().getRequestMap()
                .get(FOCUS_FIELD_ID);
    }

    /**
     * Set the client id of the component that is considered to have the focus
     * in this request.This may not be the component that has the focus when the
     * response is rendered.Returns the last client id that was set in the
     * request map.
     * <p>
     * If {@code clientId} is {@code null} then the default focus component id,
     * defined on the {@code Body} component is used to establish focus.
     * </p>
     * <p>
     * This interface expects the actual {@code clientId} of the component that
     * is to receive the focus. This means that the caller is responsible for
     * calling {@code ComplexComponent.getFocusElementId} if necessary.
     * </p>
     *
     * @param context faces context
     * @param clientId client id
     * @return the focus element id set
     */
    public static String setRequestFocusElementId(final FacesContext context,
            final String clientId) {

        String result = getRequestFocusElementId(context);
        context.getExternalContext().getRequestMap().put(FOCUS_FIELD_ID,
                clientId);
        return result;
    }
}
