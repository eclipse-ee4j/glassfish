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

package com.sun.s1asdev.jdbc.stmtcaching.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.stmtcaching.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.stmtcaching.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.rmi.RemoteException;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();

    public static void main(String[] args)
            throws Exception {

        Client client = new Client();
        client.runTest();
    }

    public void runTest() throws Exception {
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        stat.addDescription("Statement Caching Hit miss tests");

        if (simpleBMP.testHit()) {
            stat.addStatus(" Statement Caching  -  (Hit): ", stat.PASS);
        } else {
            stat.addStatus(" Statement Caching  -  (Hit): ", stat.FAIL);
        }

        if (simpleBMP.testMiss()) {
            stat.addStatus(" Statement Caching  -  (Miss): ", stat.PASS);
        } else {
            stat.addStatus(" Statement Caching  -  (Miss): ", stat.FAIL);
        }

        if (simpleBMP.testHitColumnIndexes()) {
            stat.addStatus(" Statement Caching  -  (hit columnIndexes) : ", stat.PASS);
        } else {
            stat.addStatus(" Statement Caching  -  (hit columnIndexes) : ", stat.FAIL);
        }

        if (simpleBMP.testHitColumnNames()) {
            stat.addStatus(" Statement Caching  -  (hit columnNames) : ", stat.PASS);
        } else {
            stat.addStatus(" Statement Caching  -  (hit columnNames) : ", stat.FAIL);
        }
        stat.printSummary();
    }

}
