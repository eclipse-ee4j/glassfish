/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout;

import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.StateManagementStrategy;
import jakarta.faces.view.ViewDeclarationLanguage;

import java.util.Map;

/**
 * View state management utility class.
 */
final class StateManagerUtil {

    private StateManagerUtil() {
        throw new AssertionError();
    }

    /**
     * Return an opaque {@code Object} containing sufficient information for this same instance to restore the state of the
     * current {@code UIViewRoot} on a subsequent request. The returned object must implement {@code java.io.Serializable}.
     * If there is no state information to be saved, return {@code null} instead.
     *
     * @param facesContext the Faces context
     * @param viewId view identifier of the view to be saved
     * @return the saved view, or {@code null}
     */
    public static Object saveView(FacesContext facesContext, String viewId) {
        Map<Object, Object> contextAttributes = facesContext.getAttributes();
        contextAttributes.put(StateManager.IS_SAVING_STATE, Boolean.TRUE);
        try {
            return getStateManagementStrategy(facesContext, viewId).saveView(facesContext);
        } finally {
            contextAttributes.remove(StateManager.IS_SAVING_STATE);
        }
    }

    /**
     * Restore the tree structure and the component state of the view for specified {@code viewId} and return the restored
     * {@code UIViewRoot}. If there is no saved state information available for this {@code viewId}, return {@code null}
     * instead.
     *
     * @param facesContext the Faces context
     * @param viewId view identifier of the view to be restored
     * @param renderKitId identifier of the render kit used to render this response. Must not be {@code null}
     * @return the view root, or {@code null}
     */
    public static UIViewRoot restoreView(FacesContext facesContext, String viewId, String renderKitId) {
        return getStateManagementStrategy(facesContext, viewId).restoreView(facesContext, viewId, renderKitId);
    }

    /**
     * Returns a state management strategy for the given Faces context and view id.
     *
     * @param facesContext the Faces context
     * @param viewId the view id
     * @return a state management strategy corresponds {@code facesContext} and {@code viewId}
     */
    private static StateManagementStrategy getStateManagementStrategy(FacesContext facesContext, String viewId) {
        StateManagementStrategy strategy = null;

        ViewDeclarationLanguage vdl = facesContext.getApplication().getViewHandler().getViewDeclarationLanguage(facesContext, viewId);

        if (vdl != null) {
            strategy = vdl.getStateManagementStrategy(facesContext, viewId);
        }

        if (strategy == null) {
            strategy = new LayoutStateManagementStrategy();
        }

        return strategy;
    }
}
