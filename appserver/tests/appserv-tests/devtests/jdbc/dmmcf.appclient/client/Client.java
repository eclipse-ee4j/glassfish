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

package com.sun.s1asdev.jdbc.markconnectionasbad.xa.client;

import javax.naming.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.Set;
import java.util.HashSet;
import java.sql.*;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    public static final int MAX_POOL_SIZE = 5;


    public static void main(String[] args)
            throws Exception {

        Client client = new Client();
        client.runTest();
    }

    public void runTest() throws Exception {


        Set<Integer> localdsSet = new HashSet();
        Set<Integer> localdsAfterSet = new HashSet();
        int countLocalds = 0;

        InitialContext ic = new InitialContext();
        com.sun.appserv.jdbc.DataSource ds = (com.sun.appserv.jdbc.DataSource) ic.lookup("jdbc/localdatasource");

        stat.addDescription("DMMCF Mark-Connection-As-Bad Appclient ");


        localdsSet = getFromLocalDS(MAX_POOL_SIZE, ds);
        System.out.println("localds = " + localdsSet);

        //jdbc-local-pool
        test1(ds);

        localdsAfterSet = getFromLocalDS(MAX_POOL_SIZE, ds);
        System.out.println("localdsAfter = " + localdsAfterSet);

        countLocalds = compareAndGetCount(localdsSet, localdsAfterSet);
        if(MAX_POOL_SIZE-countLocalds == 5) {
            stat.addStatus(" Mark-Connection-As-Bad destroyedCount localds: ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad destroyedCount localds: ", stat.FAIL);
        }

        stat.printSummary();
    }

    public int compareAndGetCount(Set<Integer> beforeSet, Set<Integer> afterSet) {
        //denotes the count of hashcodes that matched in both sets.
        int contains = 0;
        if(!beforeSet.containsAll(afterSet)) {
            //if it does not contain all the elements of the after set
            //find how many are absent from the beforeSet
            for(int afterInt : afterSet) {
                    if(beforeSet.contains(afterInt)) {
                            contains++;
                        }
            }
        }
        return contains;
    }

    public Set<Integer> getFromLocalDS(int count, com.sun.appserv.jdbc.DataSource localds1) {
        int connHashCode = 0;
        Connection conn = null;
        Set<Integer> hashCodeSet = new HashSet();
        for (int i = 0; i < count; i++) {
            try {
                conn = localds1.getNonTxConnection();
                connHashCode = (localds1.getConnection(conn)).hashCode();
                hashCodeSet.add(connHashCode);
            } catch (Exception e) {

            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }
        return hashCodeSet;
    }

    /* Read Operation - Driver  - shareable */
    public boolean test1(com.sun.appserv.jdbc.DataSource localds1) {

        boolean passed = true;
        for (int i = 0; i < 5; i++) {
            Connection conn = null;
            try {
                conn = localds1.getConnection();
            } catch (Exception e) {
                passed = false;
            } finally {
                if (conn != null) {
                    try {
                        localds1.markConnectionAsBad(conn);
                        conn.close();
                    } catch (Exception e1) {
                    }
                }
            }
        }

        return passed;
    }

}
