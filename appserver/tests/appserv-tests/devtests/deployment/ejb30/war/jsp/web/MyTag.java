/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.deployment.ejb30.web.jsp;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

import jakarta.annotation.Resource;
import javax.sql.DataSource;

public class MyTag extends TagSupport {

    private @Resource(mappedName="jdbc/__default") DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;

    public int doStartTag() throws JspException {

        try {

            JspWriter jsw = pageContext.getOut();

            int loginTimeout = ds1.getLoginTimeout();
            jsw.print("ds1-login-timeout=" + loginTimeout);
            loginTimeout = ds2.getLoginTimeout();
            jsw.print("ds2-login-timeout=" + loginTimeout);

            jsw.print(", Hello World");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new JspException(ex);
        }

        return SKIP_BODY;
    }
}
