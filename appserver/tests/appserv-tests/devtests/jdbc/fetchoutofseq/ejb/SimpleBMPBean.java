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

package com.sun.s1asdev.jdbc.fetchoutofseq.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.rmi.*;
import java.util.*;
import java.sql.*;

public class SimpleBMPBean
    implements EntityBean
{

    protected DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context    = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean test1() {
        return issueQuery( true );
    }

    public boolean test2() {
        return issueQuery( false );
    }

    /**
     *
     * If a connection's autocommit is set to true and we attempt a select
     * for update sql query, Oracle throws a "ORA-01002: Fetch out of sequence"
     * exception. This test tries to :
     * 1. get a connection using getNonTxConnection API and try a "select for
     * update" query. Since a connection obtained using getNonTxConnection
     * is not managed (transaction-wise), its autocommit is set to true by
     * default. So this fails.
     * 2. gets a connection as above but sets its autocommit to true. The
     * query will then pass.
     */
    public boolean issueQuery(boolean autoCommit) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean passed  = (autoCommit ? false : true);
        try {
            //conn = ds.getConnection();
            conn = ((com.sun.appserv.jdbc.DataSource)ds).getNonTxConnection();
            if ( !autoCommit ) {
                conn.setAutoCommit( false );
            }
            stmt = conn.prepareStatement("SELECT c_id, c_phone FROM O_CUSTOMER FOR UPDATE OF c_id");
            stmt.executeQuery();
        } catch (Exception e) {
            if (autoCommit) {
                passed = true;
            } else {
                passed = false;
            }
            e.printStackTrace();
        } finally {
            if (stmt != null ) {
                try {stmt.close();} catch(Exception e1) {}
            }
            if ( conn != null ) {
                try { conn.close(); } catch( Exception e1) {}
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
