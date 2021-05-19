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

package test;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;
import jakarta.servlet.jsp.tagext.DynamicAttributes;

/**
 * SimpleTag handler that echoes all its attributes
 */
public class EchoAttributesTag
    extends TagSupport
    implements DynamicAttributes
{
    private ArrayList keys;
    private ArrayList values;

    public EchoAttributesTag() {
        keys = new ArrayList();
        values = new ArrayList();
    }

    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
            for (int i = 0; i < keys.size(); i++) {
                String key = (String) keys.get(i);
                Object value = values.get(i);
                out.print(key + "=" + value);
                if (i < keys.size()-1) {
                    out.print(",");
                }
            }
        } catch (IOException ioe) {
            throw new JspException(ioe.toString(), ioe);
        }


        return EVAL_PAGE;
    }

    public void setDynamicAttribute(String uri, String localName,
                                    Object value)
            throws JspException
    {
        keys.add(localName);
        values.add(value);
    }
}


