/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.statementwrapper.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;


public class SimpleBMPBean
        implements SessionBean {

    protected DataSource ds;
    private transient jakarta.ejb.SessionContext m_ctx = null;

    public void setSessionContext(SessionContext context) {
        m_ctx = context;
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }


/* Get a single connection and do not close it */
    public boolean test1() {
        Connection conn = null;
        boolean passed = true;
        Context context = null;
        long startTime = 0, endTime = 0, timeTaken =0;
        try {
            context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/jdbc/DataSource");
        } catch(NamingException e) {
             throw new EJBException("cant find datasource");
        }
        try {
            startTime = System.currentTimeMillis();
            conn = ds.getConnection();
            endTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        }
        timeTaken = (endTime-startTime)/1000;
        System.out.println("preparedStmtTest : TimeTaken : " + timeTaken);
        if(timeTaken > 59) {
            passed = false;
        }
        return passed;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
