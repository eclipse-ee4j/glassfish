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

package com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation.client;

import java.io.*;
import java.util.*;
import java.sql.*;
import jakarta.ejb.EJB;
import jakarta.transaction.UserTransaction;
import jakarta.annotation.Resource;
import javax.sql.DataSource;
import com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String personName;

    private /** static @Resource(mappedName="jdbc/xa") **/ DataSource ds;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-persistence-tx_propagation");
        Client client = new Client(args);
        client.doTest(false);
        client.doTest(true);
        stat.printSummary("ejb-ejb30-persistence-tx_propagationID");
    }

    public Client (String[] args) {

        personName = "duke";

        if( args.length > 0 ) {
            personName = args[0];
        }

    }

    private static @EJB Sful sful;

    public void doTest(boolean commit) {

        ResultSet rs = null;
        Connection connection = null;
        PreparedStatement s = null;
        try {
            System.err.println("I am in client");

            UserTransaction utx = (UserTransaction)(new javax.naming.InitialContext()).lookup("java:comp/UserTransaction");
            ds = (DataSource)(new javax.naming.InitialContext()).lookup("jdbc/xa");

            System.err.println("calling createPerson(" + personName + ")");
            if (sful == null) {
                // Java SE client
                sful = (Sful)(new javax.naming.InitialContext()).lookup("com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation.Sful");
            }
            sful.setName(personName);
            System.err.println("created ");

            utx.begin();
            System.err.println("utx.begin called ");

            connection = ds.getConnection();
            s = connection.prepareStatement("insert into EJB30_PERSISTENCE_EEM_INJECTION_PERSON values('1', '1')");
            s.execute();
            System.err.println("inserted 1,1 into table ");

            Map<String, Boolean> map = sful.doTests();
            if (commit) {
                System.err.println("calling utx.commit ");
                utx.commit();
                System.err.println("utx.commit called ");
            } else {
                System.err.println("calling utx.rollback ");
                utx.rollback();
                System.err.println("utx.rollback called ");
            }

            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String testName = iter.next();
                boolean result = map.get(testName);
                stat.addStatus("local " + testName,
                        (result) ? stat.PASS : stat.FAIL);
            }

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        } finally {

            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) { }

            try {
                if (s != null)
                    s.close();
            } catch (Exception e) { }

            try {
                if (connection != null)
                    connection.close();
            } catch (Exception e) { }

        }

            return;
    }


}

