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

package com.sun.webui.jsf.renderkit.ajax;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.ProgressBar;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.util.Map;

import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.JsonUtilities.writeJsonObject;

/**
 * This class responds to Ajax requests made to ProgressBar components.
 */
@Renderer(@Renderer.Renders(rendererType = "com.sun.webui.jsf.ajax.ProgressBar",
componentFamily = "com.sun.webui.jsf.ProgressBar"))
public final class ProgressBarRenderer extends jakarta.faces.render.Renderer {

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        String id = component.getClientId(context);
        Map params = context.getExternalContext().getRequestParameterMap();

        String buttonId = id + "_" + "controlType";

        Object valueObject = params.get(buttonId);
        String value;
        if (valueObject != null) {
            value = ((String) valueObject).trim();
            ProgressBar progressBar = (ProgressBar) component;

            if (ProgressBar.TASK_STOPPED.equals(value)) {
                progressBar.setTaskState(ProgressBar.TASK_STOPPED);
            } else if (ProgressBar.TASK_PAUSED.equals(value)) {
                progressBar.setTaskState(ProgressBar.TASK_PAUSED);
            } else if (ProgressBar.TASK_RESUMED.equals(value)) {
                progressBar.setTaskState(ProgressBar.TASK_RESUMED);
            } else if (ProgressBar.TASK_CANCELED.equals(value)) {
                progressBar.setTaskState(ProgressBar.TASK_CANCELED);
            }
        }
    }

    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) throws IOException {

        // Do nothing...
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        // Do nothing...
    }

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            return;
        }

        ProgressBar progressBar = (ProgressBar) component;
        int progress = progressBar.getProgress();

        String status = progressBar.getStatus();
        String topText = progressBar.getDescription();
        String logMessage = progressBar.getLogMessage();
        String failedStateText = progressBar.getFailedStateText();

        // Top text facet.
        UIComponent topTextFacet = component
                .getFacet(ProgressBar.TOPTEXT_FACET);
        if (topTextFacet != null) {
            topText = null;
        }
        UIComponent bottomTextFacet = component
                .getFacet(ProgressBar.BOTTOMTEXT_FACET);
        if (bottomTextFacet != null) {
            status = null;
        }

        JsonObject json = JSON_BUILDER_FACTORY
                .createObjectBuilder()
                .add("taskState", progressBar.getTaskState())
                .add("progress", progress)
                .add("status", status)
                .add("topText", topText)
                .add("logMessage", logMessage)
                .add("failedStateText", failedStateText)
                .build();
        writeJsonObject(json, context.getResponseWriter());
    }
}
