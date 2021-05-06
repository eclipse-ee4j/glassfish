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

package com.sun.s1asdev.jdbc.reconfig.maxpoolsize.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean implements EntityBean {

    protected DataSource ds;
    int id;

    public void setEntityContext(EntityContext entityContext) {
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    /**
     * The basic strategy here is that we try to get 1 more connection
     * than the maxpoolsize. This single extra getConnection should not
     * pass. If this happens, the test has passed.
     */

    public boolean test1( int maxPoolSize, boolean throwException, boolean useXA ) {
        try {
            InitialContext ic = new InitialContext();
            if ( useXA ) {
                ds = (DataSource) ic.lookup("java:comp/env/DataSource_xa");
            } else {
                ds = (DataSource) ic.lookup("java:comp/env/DataSource");
            }
        } catch( Exception e ) {
            e.printStackTrace();
            return false;
        }
        boolean passed = true;
        Connection[] conns = new Connection[maxPoolSize];
        for( int i = 0; i < maxPoolSize; i++ ) {
            System.out.println("throwException is : " + throwException );
            try {
                System.out.println("########Getting connection : " + i );
                conns[i] = ds.getConnection();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }
        //try getting an extra connection
        System.out.println("---Try getting extra connection");
        Connection con = null;
        try {
            con = ds.getConnection();
        } catch( Exception e) {
            System.out.print("Caught exception : " ) ;
            if ( throwException ) {
                System.out.println("Setting passed to true");
                passed = true;
            } else {
                passed = false;
            }

        } finally {
            try { con.close(); } catch ( Exception e ) {}
        }

        for (int i = 0 ; i < maxPoolSize;i++ ) {
            try {
                conns[i].close();
            } catch( Exception e) {
                //passed = false;
            }
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
