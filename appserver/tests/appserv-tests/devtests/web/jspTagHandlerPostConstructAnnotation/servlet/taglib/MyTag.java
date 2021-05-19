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

package taglib;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

public class MyTag extends TagSupport {

    private StringBuffer sb;

    private @Resource DataSource ds1;
    private @Resource(name="myDataSource2") DataSource ds2;
    private DataSource ds3;

    @Resource(name="jdbc/myDataSource3")
    private void setDataSource(DataSource ds) {
        ds3 = ds;
    }

    @PostConstruct
    public void init() {
        sb = new StringBuffer();
        try {
            int loginTimeout = ds1.getLoginTimeout();
            sb.append("ds1-login-timeout=" + loginTimeout);
            loginTimeout = ds2.getLoginTimeout();
            sb.append(",ds2-login-timeout=" + loginTimeout);
            loginTimeout = ds3.getLoginTimeout();
            sb.append(",ds3-login-timeout=" + loginTimeout);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public int doStartTag() throws JspException {
        try {
          pageContext.getOut().print(sb.toString());
          return SKIP_BODY;
        } catch (IOException ioe) {
            throw new JspException(ioe);
        }
    }
}
