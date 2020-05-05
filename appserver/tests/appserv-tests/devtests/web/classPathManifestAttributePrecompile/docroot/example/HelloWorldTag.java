/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package example;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.SkipPageException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.*;
import java.io.IOException;

/**
 * Prints Hello World
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class HelloWorldTag extends TagSupport {

    @Override public int doStartTag() throws JspException {
        try {
            pageContext.getOut().write("Hello World");
        } catch (IOException ioe) {
            throw new JspException(ioe);
        }
        return SKIP_BODY;
    }

    @Override public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

}
