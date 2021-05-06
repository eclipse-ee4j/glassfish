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

package com.sun.s1asdev.jdbc.nopasswdfordb.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleSessionBean implements SessionBean
{

    private SessionContext ctxt_;
    private InitialContext ic_;
    public void setSessionContext(SessionContext context) {
        ctxt_ = context;
        try {
            ic_ = new InitialContext();
        } catch( NamingException ne ) {
            ne.printStackTrace();
        }
    }

    public void ejbCreate() throws CreateException {
    }

    /**
     * Lookup a datasource with resource-ref specifying an
     * empty password in default-resource-principal
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *     <password></password>
     *   </default-resource-principal>
     * </resource-ref>
     *
     * Note that the pool still has a valid password set in it
     */
    public boolean test1() throws Exception {
        DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource");
        Connection conn1 = null;
        boolean passed = false;
        //clean the database
        try {
            conn1 = ds.getConnection();
        } catch( Exception e) {
            e.printStackTrace();
            return true;
        } finally {
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        return false;
    }

    /**
     * Lookup a datasource without resource-ref specifying
     * password in default-resource-principal
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *   </default-resource-principal>
     * </resource-ref>
     *
     * Note that the pool still has a valid password set in it
     */

    public boolean test2() throws Exception {
            DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource1");
        Connection conn1 = null;
        boolean passed = false;
        //clean the database
        try {
            conn1 = ds.getConnection();
        } catch( Exception e) {
            e.printStackTrace();
            return true;
        } finally {
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        return false;
    }

    /**
     * Lookup a datasource with resource-ref specifying
     * password in default-resource-principal but no password
     * in the pool
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *     <password>DBPASSWORD</password>
     *   </default-resource-principal>
     * </resource-ref>
     *
     * Note that the pool has no password set in it
     */

    public boolean test3() throws Exception {
            DataSource ds = (DataSource)ic_.lookup("java:comp/env/DataSource2");
        Connection conn1 = null;
        boolean passed = false;
        //clean the database
        try {
            conn1 = ds.getConnection();
        } catch( Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        return true;
    }

    /**
     * Lookup a datasource with resource-ref specifying an
     * empty password in default-resource-principal
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *     <password></password>
     *   </default-resource-principal>
     * </resource-ref>
     *
     * Note that the pool still has a valid password set in it
     */
    public boolean test4() throws Exception {
        DataSource ds = (DataSource)ic_.lookup("java:comp/env/XADataSource");
        Connection conn1 = null;
        boolean passed = false;
        //clean the database
        try {
            conn1 = ds.getConnection();
        } catch( Exception e) {
            e.printStackTrace();
            return true;
        } finally {
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        return false;
    }

    /**
     * Lookup a datasource without resource-ref specifying
     * password in default-resource-principal
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *   </default-resource-principal>
     * </resource-ref>
     *
     * Note that the pool still has a valid password set in it
     */

    public boolean test5() throws Exception {
            DataSource ds = (DataSource)ic_.lookup("java:comp/env/XADataSource1");
        Connection conn1 = null;
        boolean passed = false;
        //clean the database
        try {
            conn1 = ds.getConnection();
        } catch( Exception e) {
            e.printStackTrace();
            return true;
        } finally {
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        return false;
    }

    /**
     * Lookup a datasource with resource-ref specifying
     * password in default-resource-principal but no password
     * in the pool
     * <resource-ref>
     *   <default-resource-principal>
     *     <name>DBUSER</name>
     *     <password>DBPASSWORD</password>
     *   </default-resource-principal>
     * </resource-ref>
     *
     * Note that the pool has no password set in it
     */

    public boolean test6() throws Exception {
            DataSource ds = (DataSource)ic_.lookup("java:comp/env/XADataSource2");
        Connection conn1 = null;
        boolean passed = false;
        //clean the database
        try {
            conn1 = ds.getConnection();
        } catch( Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn1 != null) {
                try { conn1.close(); } catch( Exception e1 ) {}
            }
        }

        return true;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
