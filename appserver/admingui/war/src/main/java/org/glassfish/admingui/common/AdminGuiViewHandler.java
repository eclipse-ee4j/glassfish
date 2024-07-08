/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.admingui.common;

import jakarta.faces.application.Application;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.application.ViewHandlerWrapper;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author Ondro Mihalyi
 */
public class AdminGuiViewHandler extends ViewHandlerWrapper {

    private ViewHandler defaultViewHandler = null;

    public AdminGuiViewHandler(ViewHandler wrapped) {
        super(wrapped);
        Application app = FacesContext.getCurrentInstance().getApplication();
        if (app instanceof AdminGuiApplication) {
            defaultViewHandler = ((AdminGuiApplication) app).getDefaultViewHandler();
        } else {
            defaultViewHandler = wrapped;
        }
    }

    @Override
    public ViewHandler getWrapped() {
        String requestServletPath = null;
        if (FacesContext.getCurrentInstance() != null) {
            requestServletPath = FacesContext.getCurrentInstance().getExternalContext().getRequestServletPath();
        }
        if (defaultViewHandler != null && requestServletPath != null && requestServletPath.endsWith(".xhtml")) {
            return defaultViewHandler;
        }
        return super.getWrapped();
    }



}
