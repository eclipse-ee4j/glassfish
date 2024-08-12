/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.common.util;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

import java.io.ByteArrayInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 *
 * @author jdlee
 */
public class MiscUtil {

    /**
     * <p>This utility method can be used to create a ValueExpression and set its value.
     * An example usage might look like this:</p>
     * <code>
     *      ValueExpression ve = MiscUtil.setValueExpression("#{myMap}", new HashMap());
     * </code>
     * @param expression The expression to create. Note that this requires the #{ and } wrappers.
     * @param value  The value to which to set the ValueExpression
     * @return The newly created ValueExpression
     */
    public static ValueExpression setValueExpression(String expression, Object value) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueExpression ve = facesContext.getApplication().getExpressionFactory().
                createValueExpression(facesContext.getELContext(), expression, Object.class);
        ve.setValue(facesContext.getELContext(), value);

        return ve;
    }

    public static Document getDocument(String input) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(input.getBytes()));
            return doc;
        } catch (Exception ex) {
            GuiUtil.prepareAlert("error", ex.getMessage() + ": " + input, null);
            return null;
        }
    }

}
