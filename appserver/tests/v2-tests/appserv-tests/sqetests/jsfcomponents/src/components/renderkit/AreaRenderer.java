/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package components.renderkit;


import components.components.AreaComponent;
import components.components.MapComponent;
import components.model.ImageArea;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;


/**
 * This class converts the internal representation of a <code>UIArea</code>
 * component into the output stream associated with the response to a
 * particular request.
 */

public class AreaRenderer extends BaseRenderer {


    // -------------------------------------------------------- Renderer Methods


    /**
     * <p>No decoding is required.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void decode(FacesContext context, UIComponent component) {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

    }


    /**
     * <p>No begin encoding is required.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

    }


    /**
     * <p>No children encoding is required.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

    }


    /**
     * <p>Encode this component.</p>
     *
     * @param context   <code>FacesContext</code>for the current request
     * @param component <code>UIComponent</code> to be decoded
     */
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        AreaComponent area = (AreaComponent) component;
        String targetImageId =
            area.findComponent(area.getTargetImage()).getClientId(context);
        ImageArea iarea = (ImageArea) area.getValue();
        ResponseWriter writer = context.getResponseWriter();
        StringBuffer sb = null;

        writer.startElement("area", area);
        writer.writeAttribute("alt", iarea.getAlt(), "alt");
        writer.writeAttribute("coords", iarea.getCoords(), "coords");
        writer.writeAttribute("shape", iarea.getShape(), "shape");
        // PENDING(craigmcc) - onmouseout only works on first form of a page
        sb =
            new StringBuffer("document.forms[0]['").append(targetImageId)
            .append("'].src='");
        sb.append(
            getURI(context, (String) area.getAttributes().get("onmouseout")));
        sb.append("'");
        writer.writeAttribute("onmouseout", sb.toString(), "onmouseout");
        // PENDING(craigmcc) - onmouseover only works on first form of a page
        sb =
            new StringBuffer("document.forms[0]['").append(targetImageId)
            .append("'].src='");
        sb.append(
            getURI(context, (String) area.getAttributes().get("onmouseover")));
        sb.append("'");
        writer.writeAttribute("onmouseover", sb.toString(), "onmouseover");
        // PENDING(craigmcc) - onclick only works on first form of a page
        sb = new StringBuffer("document.forms[0]['");
        sb.append(getName(context, area));
        sb.append("'].value='");
        sb.append(iarea.getAlt());
        sb.append("'; document.forms[0].submit()");
        writer.writeAttribute("onclick", sb.toString(), "value");
        writer.endElement("area");

    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Return the calculated name for the hidden input field.</p>
     *
     * @param context   Context for the current request
     * @param component Component we are rendering
     */
    private String getName(FacesContext context, UIComponent component) {
        while (component != null) {
            if (component instanceof MapComponent) {
                return (component.getId() + "_current");
            }
            component = component.getParent();
        }
        throw new IllegalArgumentException();
    }


    /**
     * <p>Return the path to be passed into JavaScript for the specified
     * value.</p>
     *
     * @param context Context for the current request
     * @param value   Partial path to be (potentially) modified
     */
    private String getURI(FacesContext context, String value) {
        if (value.startsWith("/")) {
            return (context.getExternalContext().getRequestContextPath() +
                value);
        } else {
            return (value);
        }
    }


}
