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

package com.sun.s1asdev.jdbc.cpds.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class SimpleBMPBean
        implements EntityBean {

    protected DataSource ds;
    int id;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean test1() {
        //application auth + user/pwd not specified - should fail
        Connection conn = null;
        boolean passed = false;
        try {
            conn = ds.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            passed = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }
        return passed;
    }

    public boolean test2() {
        //application auth + user/pwd  specified - should pass
        System.out.println("-----------------Start test2--------------");
        Connection conn = null;
        boolean passed = true;
        try {
            conn = ds.getConnection("dbuser", "dbpassword");
        } catch (Exception e) {
            passed = false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }
        System.out.println("-----------------End test2--------------");
        return passed;
    }

    public boolean test3() {
        //application auth + wrong user/pwd
        System.out.println("-----------------Start test3--------------");
        Connection conn = null;
        boolean passed = false;
        try {
            conn = ds.getConnection("xyz", "xyz");
        } catch (Exception e) {
            passed = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }
        System.out.println("-----------------End test3--------------");

        return passed;
    }

    public boolean test4() {
        //application auth + user/pwd  specified - right initiall
        //wrong the second time
        System.out.println("-----------------Start test4--------------");
        Connection conn = null;
        boolean passed = false;

        try {
            conn = ds.getConnection("APP", "APP");
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }

        try {
            //conn = ds.getPooledConnection("xyz", "xyz" ).getConnection();
            conn = ds.getConnection("xyz", "xyz");
        } catch (Exception e) {
            passed = true;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e1) {
                }
            }
        }
        System.out.println("-----------------End test4--------------");
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
