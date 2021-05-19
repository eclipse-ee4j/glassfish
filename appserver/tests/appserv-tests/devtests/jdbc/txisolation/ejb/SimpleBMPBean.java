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

package com.sun.s1asdev.jdbc.txisolation.ejb;

import jakarta.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;


public class SimpleBMPBean
        implements SessionBean {


    DataSource ds;
    public void setSessionContext(SessionContext sessionContext) {
        initializeDataSource();
    }

    private DataSource getDataSource() {
        if(ds !=null){
            return ds;
        }else{
            initializeDataSource();
            return ds;
        }
    }

    private void initializeDataSource(){
      Context context = null;
        try {
            context = new InitialContext();
              ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean test1(int isolationLevel){
        boolean result = false;
    Connection con = null;
        try{
            con = getDataSource().getConnection();
            if(isolationLevel == con.getTransactionIsolation()){
               result = true;
            }
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }finally{
            if(con != null){
                try{
                    con.close();
                }catch(Exception e){e.printStackTrace();}
            }
        }
    return result;
    }

    public boolean modifyIsolation(int isolationLevel){
        boolean result = false;
    Connection con = null;
        try{
            con = getDataSource().getConnection();
            con.setTransactionIsolation(isolationLevel);
            result = true;
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }finally{
            if(con != null){
                try{
                    con.close();
                }catch(Exception e){e.printStackTrace();}
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
