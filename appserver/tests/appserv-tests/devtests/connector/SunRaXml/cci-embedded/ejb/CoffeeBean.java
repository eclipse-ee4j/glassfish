/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.connector.cci;

import java.math.BigDecimal;
import java.util.*;
import jakarta.ejb.*;
import jakarta.resource.cci.*;
import jakarta.resource.ResourceException;
import javax.naming.*;
import com.sun.connector.cciblackbox.*;

public class CoffeeBean implements SessionBean {

    private SessionContext sc;
    private String user;
    private String password;
    private ConnectionFactory cf;


    public void ejbCreate() throws CreateException {
    }

    public void setSessionContext(SessionContext sc) {
        try {
            this.sc = sc;
            Context ic = new InitialContext();
            user = (String) ic.lookup("java:comp/env/user");
            password = (String) ic.lookup("java:comp/env/password");
            cf = (jakarta.resource.cci.ConnectionFactory) ic.lookup("java:comp/env/eis/EMBEDDEDCCIEIS");
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }

    public int getCoffeeCount() {
        int count = -1;
        try {
            Connection con = getCCIConnection();
            Interaction ix = con.createInteraction();
            CciInteractionSpec iSpec = new CciInteractionSpec();
            iSpec.setSchema(user);
            iSpec.setCatalog(null);
            iSpec.setFunctionName("COUNTCOFFEE");
            RecordFactory rf = cf.getRecordFactory();
            IndexedRecord iRec = rf.createIndexedRecord("InputRecord");
            Record oRec = ix.execute(iSpec, iRec);
            Iterator iterator = ((IndexedRecord)oRec).iterator();
            while(iterator.hasNext()) {
                Object obj = iterator.next();
                if(obj instanceof Integer) {
                    count = ((Integer)obj).intValue();
                }
                else if(obj instanceof BigDecimal) {
                    count = ((BigDecimal)obj).intValue();
                }
            }
            closeCCIConnection(con);
        }catch(ResourceException ex) {
            ex.printStackTrace();
        }
        return count;
    }


    public void insertCoffee(String name, int qty) {
        try {
            Connection con = getCCIConnection();
            Interaction ix = con.createInteraction();
            CciInteractionSpec iSpec = new CciInteractionSpec();
            iSpec.setFunctionName("INSERTCOFFEE");
            iSpec.setSchema(user);
            iSpec.setCatalog(null);
            RecordFactory rf = cf.getRecordFactory();
            IndexedRecord iRec = rf.createIndexedRecord("InputRecord");
            boolean flag = iRec.add(name);
            flag = iRec.add(new Integer(qty));
            ix.execute(iSpec, iRec);
            closeCCIConnection(con);
        }catch(ResourceException ex) {
            ex.printStackTrace();
        }
    }

    private Connection getCCIConnection() {
        Connection con = null;

        System.out.println("<========== IN get cci Connection ===========>");
        try {
            ConnectionSpec spec = new CciConnectionSpec("DBUSER", "DBPASSWORD");
        System.out.println("<========== Created ISpec ===========>");
        System.out.println("CF value : " + cf);
            con = cf.getConnection(spec);
        } catch (ResourceException ex) {
            ex.printStackTrace();
        }
        return con;
    }

    private void closeCCIConnection(Connection con) {
        try {
            con.close();
        } catch (ResourceException ex) {
            ex.printStackTrace();
        }
    }

    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}

}
