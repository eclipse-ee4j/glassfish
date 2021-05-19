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

package com.sun.s1asdev.jdbc.tracingsql.logger;

import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TraceLogger implements SQLTraceListener {

    DataSource ds;
    public TraceLogger() {
        try {
        InitialContext ic = new InitialContext();
        ds = (DataSource) ic.lookup("jdbc/tracingsql-res");
        } catch(NamingException ex) {}
    }

    /**
     * Writes the record to a database.
     */
    public void sqlTrace(SQLTraceRecord record) {

        try {
            //System.out.println("### ds=" + ds);

            Object[] params = record.getParams();
            StringBuffer argsBuf = new StringBuffer();
            if (params != null && params.length > 0) {
                for (Object param : params) {
                    if (param != null) {
                        argsBuf.append(param.toString() + ";");
                    }
                }
            }
            System.out.println(
                "SQLTrace called: Details: class=" + record.getClassName() +
                    " method=" + record.getMethodName() + " args=" +
                    argsBuf.toString());
            writeRecord(ds, record.getClassName(), record.getMethodName(),
                argsBuf.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeRecord(DataSource ds, String classname, String methodname, String args) {
        Connection conFromDS = null;
        PreparedStatement stmt = null;
        try{
            conFromDS = ds.getConnection();
            //System.out.println("###con=" + conFromDS);
            stmt = conFromDS.prepareStatement(
                "insert into sql_trace values (?, ?, ?)" );

            System.out.println("### stmt=" + stmt);
            stmt.setString(1, classname);
            stmt.setString(2, methodname);
            stmt.setString(3, args);

            int count = stmt.executeUpdate();
            //System.out.println("### inserted " + count + " rows");

        }catch(SQLException sqe){
        }finally{

            try{
                if(stmt != null){
                    stmt.close();
                }
            }catch(SQLException sqe){}

            try{
                if(conFromDS != null){
                    conFromDS.close();
                }
            }catch(SQLException sqe){}
        }
    }

}
