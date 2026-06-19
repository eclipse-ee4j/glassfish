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
import jakarta.faces.application.StateManagerWrapper;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

import java.util.Objects;

/**
 * {@inheritDoc}
 */
public class LayoutStateManager extends StateManagerWrapper {

    public LayoutStateManager(StateManager wrapped) {
        super(wrapped);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewState(FacesContext facesContext) {
        Objects.requireNonNull(facesContext);

        Object savedView = null;
        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot != null && !viewRoot.isTransient()) {
            if (ViewRootUtil.getLayoutDefinition(viewRoot) == null) {
                // No layout definition, fall back to default behavior
                return getWrapped().getViewState(facesContext);
            } else {
                savedView = StateManagerUtil.saveView(facesContext, viewRoot.getViewId());
            }
        }

        return facesContext.getRenderKit().getResponseStateManager().getViewState(facesContext, savedView);
    }
}
