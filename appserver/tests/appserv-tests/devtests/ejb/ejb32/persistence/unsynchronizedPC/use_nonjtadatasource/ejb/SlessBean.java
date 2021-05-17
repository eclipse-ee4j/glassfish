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

package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.use_nonjtadatasource.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import jakarta.transaction.UserTransaction;

@Stateless
@EJB(name="ejb/SfulBean",
beanInterface=com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.use_nonjtadatasource.ejb.SfulBean.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SlessBean implements Tester{

    public Map<String, Boolean> doTest() {
        Map<String, Boolean> resultMap = new HashMap<String, Boolean>();

        DataSource ds = null;
        Connection conn = null;
        PreparedStatement ps = null;
        UserTransaction utx = null;
        try {
            System.out.println("I am in client");

            utx = (UserTransaction)(new javax.naming.InitialContext()).lookup("java:comp/UserTransaction");
            ds = (DataSource)(new javax.naming.InitialContext()).lookup("java:comp/DefaultDataSource");

            utx.begin();
            conn = ds.getConnection();
            ps = conn.prepareStatement("Update EJB32_PERSISTENCE_CONTEXT_PERSON set name = 'newName' where id = 1");

            String lookupName = "java:comp/env/ejb/SfulBean";

            InitialContext initCtx = new InitialContext();
            SfulBean sfulBean = (SfulBean) initCtx.lookup(lookupName);

            Person person = sfulBean.testUsingNonJTADataSource(resultMap);

            utx.rollback();
            utx = null;
            resultMap.put("testRollBackDoesNotClearUnsynchPC", sfulBean.testRollBackDoesNotClearUnsynchPC(person));


            System.out.println("DoTest method ends");
            return resultMap;
        } catch (Exception e) {
            if (utx != null) {
                try {
                    utx.rollback();
                } catch (Exception ex) {}
            }
            throw new EJBException(e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {}

        }
    }
}
