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

import com.sun.appserv.jdbc.DataSource;

import javax.naming.*;
import javax.sql.*;
import java.sql.*;


public class SimpleBMPBean
        implements EntityBean {

    protected com.sun.appserv.jdbc.DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context = new InitialContext();
            ds = (com.sun.appserv.jdbc.DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
        System.out.println("[**SimpleBMPBean**] Done with setEntityContext....");
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    private int getRandomNumber(int maxLimit) {
        //return (int)(Math.random() *5 );
        return maxLimit;
    }

    public boolean test1(int maxConnections) {
        boolean result = false;
        Connection connections[] = new Connection[getRandomNumber(maxConnections)];
        try {
            for (int i = 0; i < connections.length; i++) {
                connections[i] = ds.getConnection();
            }
        } catch (Exception e) {
            result = false;
        } finally {
            for (int i = 0; i < connections.length; i++) {
                try {
                    connections[i].close();
                } catch (Exception e) {
                }
                result = true;
            }
        }
        return result;
    }

    public boolean test2() {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            System.out.println("test-2() : " + ds.getConnection(conn));
            Context ic = new InitialContext();
            NestedBMPHome home = (NestedBMPHome) javax.rmi.PortableRemoteObject.narrow(ic.lookup("java:comp/env/ejb/NestedBMPEJB"), NestedBMPHome.class);
            NestedBMP nestedBean = home.create();

            nestedBean.test1();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
        return true;
    }

    public void test3() {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            System.out.println("test-3() : " + ds.getConnection(conn));
        } catch (Exception e) {
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
            }
        }
    }


    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }
}
