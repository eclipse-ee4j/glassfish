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

package com.sun.s1asdev.connector.rar_accessibility_test.ejb;

import jakarta.ejb.EJBContext;
import jakarta.ejb.SessionBean;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SimpleSessionBean implements SessionBean {

    private EJBContext ejbcontext;
    private transient jakarta.ejb.SessionContext m_ctx = null;
    transient javax.sql.DataSource ds;


    public void setSessionContext(jakarta.ejb.SessionContext ctx) {
        m_ctx = ctx;
    }

    public void ejbCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public boolean test1(int expectedCount) {

        System.out.println("Excpected count : " + expectedCount);
        String [] resources = new String[] {"blackbox-tx-cr","blackbox-notx-cr","blackbox-xa-cr"};

        int count = 0;
        for(String res : resources){
            try{
                InitialContext ctx = new InitialContext();
                Object o = ctx.lookup(res);
                System.out.println("CLASS_NAME: "+o.getClass().getName());
                System.out.println("CLASS_LOADER: " +
                        Thread.currentThread().getContextClassLoader().loadClass(o.getClass().getName()).getClassLoader());
                count++;
            }catch(Throwable e){
                e.printStackTrace();
            }
        }
        System.out.println("Actual count : " + count);
        return (expectedCount == count);
    }
}



