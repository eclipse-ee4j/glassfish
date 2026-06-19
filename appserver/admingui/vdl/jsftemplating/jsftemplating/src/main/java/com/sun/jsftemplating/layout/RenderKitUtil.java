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

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

import java.util.Objects;

/**
 * Render kit utility class.
 */
final class RenderKitUtil {

    private RenderKitUtil() {
        throw new AssertionError();
    }

    /**
     * Returns the {@code RenderKit} for the specified {@code renderKitId}.
     *
     * @param facesContext the Face context
     * @param renderKitId identifier of the render kit
     * @return the render kit for the specified {@code renderKitId}
     * @throws FacesException if an exception occurs while getting the render kit
     */
    public static RenderKit getRenderKit(FacesContext facesContext, String renderKitId) {
        Objects.requireNonNull(facesContext);
        Objects.requireNonNull(renderKitId);

        RenderKit renderKit = facesContext.getRenderKit();

        if (renderKit == null) {
            RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            if (renderKitFactory == null) {
                throw new FacesException("Unable to locate factory for " + FactoryFinder.RENDER_KIT_FACTORY);
            }

            renderKit = renderKitFactory.getRenderKit(facesContext, renderKitId);
            if (renderKit == null) {
                UIViewRoot viewRoot = facesContext.getViewRoot();
                if (viewRoot != null) {
                    viewRoot.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
                }

                renderKit = renderKitFactory.getRenderKit(facesContext, RenderKitFactory.HTML_BASIC_RENDER_KIT);
                if (renderKit == null) {
                    throw new FacesException("Unable to locate render kit instance for " + RenderKitFactory.HTML_BASIC_RENDER_KIT);
                }
            }
        }

        return renderKit;
    }
}
