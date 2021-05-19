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


public class SimpleBMPBean implements EntityBean{

    protected DataSource ds;

    public void setEntityContext(EntityContext entityContext) {
        Context context = null;
        try {
            context    = new InitialContext();
            ds = (DataSource) context.lookup("java:comp/env/jdbc/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
            return new Integer(1);
    }

    public boolean preparedStatementTest1(String tableName, String value){
       return  preparedStatementInternalTest1(tableName, value);
    }

    public boolean preparedStatementInternalTest1(String tableName, String value) {
        boolean result = true;
        Connection conFromDS = null;
        PreparedStatement stmt = null;
        try {
            conFromDS = ds.getConnection();
            try {
                stmt = conFromDS.prepareStatement("select * from "+ tableName +" where c_phone= ? ");
                stmt.setString(1, value);
                ResultSet rs = stmt.executeQuery();
                rs.close();
            } catch (SQLException sqe) {
                result = false;
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException sqe) {
                    result = false;
                }
            }
        } catch (SQLException sqe) {
            result = false;
        }
        finally {
            try {
                if (conFromDS != null) {
                    conFromDS.close();
                }
            } catch (SQLException sqe) {
                result = false;
            }
        }
        return result;
    }

    public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
