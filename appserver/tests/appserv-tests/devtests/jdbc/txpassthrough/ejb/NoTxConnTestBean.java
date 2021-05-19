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

package com.sun.s1asdev.jdbc.txpassthrough.ejb;

import java.util.*;
import java.io.*;
import java.rmi.*;
import jakarta.ejb.*;
import jakarta.transaction.UserTransaction;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class NoTxConnTestBean implements SessionBean {

    private EJBContext ejbcontext;
    private transient jakarta.ejb.SessionContext m_ctx = null;
    transient javax.sql.DataSource ds;


    public void setSessionContext(jakarta.ejb.SessionContext ctx) {
        m_ctx = ctx;
    }

    public void ejbCreate() {}

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    public boolean test1() {
        System.out.println(" @@@@ Entering Bean 1 @@@@ ");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {

            InitialContext ctx = new InitialContext();
            ds = (javax.sql.DataSource) ctx.lookup("java:comp/env/jdbc/txpassthrough");
            conn = ds.getConnection("dbuser", "dbpassword");
            stmt = conn.createStatement();
            String query1 = "SELECT * FROM ONLYGETCONNECTION";
            rs = stmt.executeQuery(query1);

            Object o = ctx.lookup("java:comp/env/ejb/SecondEJB");
            SecondHome home = (SecondHome)
                javax.rmi.PortableRemoteObject.narrow(o, SecondHome.class );
            Second bean = home.create();
            return bean.test1();
        } catch (Exception e) {
            System.out.println("Caught Exception in 1st Bean---");
            e.printStackTrace();
            return false;
        } finally {
            if (rs != null ) {
                try { rs.close(); } catch( Exception e1) {}
            }
            if (stmt != null ) {
                try {stmt.close(); } catch( Exception e1) {}
            }
            if (conn != null ) {
                try {conn.close();} catch( Exception e1) {}
            }
            System.out.println(" @@@@ Exiting Bean 1 @@@@ ");

               }
    }



}



