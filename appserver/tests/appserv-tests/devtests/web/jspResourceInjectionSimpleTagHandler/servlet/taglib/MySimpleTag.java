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
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import javax.naming.*;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import javax.sql.DataSource;

@Resource(name = "myDataSource4", type = DataSource.class)
@Resources({ 
    @Resource(name = "myDataSource5", type = DataSource.class), 
    @Resource(name = "jdbc/myDataSource6", type = DataSource.class) })
public class MySimpleTag extends SimpleTagSupport {

    @Resource 
    private DataSource ds1;
    
    @Resource(name = "myDataSource2") 
    private DataSource ds2;
    
    private DataSource ds3;

    @Resource(name = "jdbc/myDataSource3")
    private void setDataSource(DataSource ds) {
        ds3 = ds;
    }

    public void doTag() throws JspException, IOException {

        try {

            JspWriter jsw = getJspContext().getOut();

            int loginTimeout = ds1.getLoginTimeout();
            jsw.write("ds1-login-timeout=" + loginTimeout);
            loginTimeout = ds2.getLoginTimeout();
            jsw.write(",ds2-login-timeout=" + loginTimeout);
            loginTimeout = ds3.getLoginTimeout();
            jsw.write(",ds3-login-timeout=" + loginTimeout);

            InitialContext ic = new InitialContext();

            DataSource ds4 = (DataSource) ic.lookup("java:comp/env/myDataSource4");
            loginTimeout = ds4.getLoginTimeout();
            jsw.write(",ds4-login-timeout=" + loginTimeout);

            DataSource ds5 = (DataSource) ic.lookup("java:comp/env/myDataSource5");
            loginTimeout = ds5.getLoginTimeout();
            jsw.write(",ds5-login-timeout=" + loginTimeout);

            DataSource ds6 = (DataSource) ic.lookup("java:comp/env/jdbc/myDataSource6");
            loginTimeout = ds6.getLoginTimeout();
            jsw.write(",ds6-login-timeout=" + loginTimeout);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new JspException(ex);
        }
    }
}
