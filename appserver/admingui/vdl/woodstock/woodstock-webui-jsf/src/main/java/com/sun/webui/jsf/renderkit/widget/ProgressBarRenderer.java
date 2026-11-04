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

package com.sun.webui.jsf.renderkit.widget;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.ProgressBar;
import com.sun.webui.jsf.component.TextArea;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.JsonObjectBuilder;

import java.io.IOException;

import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.JsonUtilities.jsonValueOf;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;
import static com.sun.webui.jsf.util.WidgetUtilities.renderComponent;

/**
 * This class renders ProgressBar components.
 */
@Renderer(@Renderer.Renders(
        rendererType = "com.sun.webui.jsf.widget.ProgressBar",
        componentFamily = "com.sun.webui.jsf.ProgressBar"))
public final class ProgressBarRenderer extends RendererBase {

    /**
     * The set of pass-through attributes to be rendered.
     */
    private static final String[] ATTRIBUTES = {
        "style"
    };

    @Override
    protected String[] getModuleNames(final UIComponent component) {
        ProgressBar progressBar = (ProgressBar) component;
        if (progressBar.isAjaxify()) {
            return new String[]{
                "progressBar",
                "jsfx/progressBar"
            };
        }
        return new String[]{
            "progressBar"
        };
    }

    @Override
    protected JsonObjectBuilder getProperties(final FacesContext context,
            final UIComponent component) throws IOException {

        ProgressBar progressBar = (ProgressBar) component;
        Theme theme = getTheme(context);
        JsonObjectBuilder jsonBuilder = JSON_BUILDER_FACTORY
                .createObjectBuilder()
        .add("barHeight", progressBar.getHeight())
        .add("barWidth", progressBar.getWidth())
        .add("failedStateText", jsonValueOf(progressBar.getFailedStateText()))
        .add("logMessage", jsonValueOf(progressBar.getLogMessage()))
        .add("overlayAnimation", progressBar.isOverlayAnimation())
        .add("percentChar", jsonValueOf(
                theme.getMessage("ProgressBar.percentChar")))
        .add("progress", progressBar.getProgress())
        .add("progressImageUrl", jsonValueOf(
                progressBar.getProgressImageUrl()))
        .add("refreshRate", progressBar.getRefreshRate())
        .add("taskState", jsonValueOf(progressBar.getTaskState()));
        if (progressBar.getToolTip() != null) {
            jsonBuilder.add("toolTip", progressBar.getToolTip());
        } else {
            jsonBuilder.add("toolTip", theme.getMessage("ProgressBar.toolTip"));
        }
        jsonBuilder.add("type", progressBar.getType());
        jsonBuilder.add("visible", progressBar.isVisible());
        addAttributeProperties(ATTRIBUTES, component, jsonBuilder);
        return jsonBuilder;
    }

    @Override
    protected void renderNestedContent(final FacesContext context,
            final UIComponent component) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        ProgressBar pb = (ProgressBar) component;

        // Add busy icon.
        writer.append(renderComponent(context, pb.getBusyIcon()));

        // Button facets.
        UIComponent rightButtonCon = component.getFacet(
                ProgressBar.RIGHTTASK_CONTROL_FACET);
        UIComponent bottomButtonCon = component.getFacet(
                ProgressBar.BOTTOMTASK_CONTROL_FACET);

        if (rightButtonCon != null) {
            writer.append(renderComponent(context, rightButtonCon));
        }
        if (bottomButtonCon != null) {
            writer.append(renderComponent(context, bottomButtonCon));
        }

        if (pb.getLogMessage() != null) {
            // TextArea for running log
            UIComponent textArea = (TextArea) pb.getLogMsgComponent(pb);
            writer.append(renderComponent(context, textArea));
        }

        // Bottom text facet.
        UIComponent bottomTextFacet = component.getFacet(
                ProgressBar.BOTTOMTEXT_FACET);
        if (bottomTextFacet != null) {
            writer.append(renderComponent(context, bottomTextFacet));
        } else {
            writer.append("<span>bottomText:")
                    .append(pb.getStatus())
                    .append("</span>");
        }

        // Top Text facet.
        UIComponent topTextFacet = component
                .getFacet(ProgressBar.TOPTEXT_FACET);
        if (topTextFacet != null) {
            writer.append(renderComponent(context, topTextFacet));
        } else {
            writer.append("<span>topText:")
                    .append(pb.getDescription())
                    .append("</span>");
        }
    }
}
