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

package com.sun.s1asdev.jdbc.statementwrapper.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

public class SimpleBMPClient {

    public static final String poolName = "ql-jdbc-pool";
    public static final int JMX_PORT = 8686;
    public static final String HOST_NAME = "localhost";
    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "StatementLeakDetection ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        stat.addDescription("JDBC Statement Leak Detection & Reclaim Tests");
        boolean result = true;

        //Testing Statement objects
        for (int i = 0; i < 2; i++) {
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP simpleBMP = simpleBMPHome.create();

            if (!simpleBMP.statementTest()) {
                result = false;
                break;
            }
            Thread.sleep(5000);
        }
        SimpleBMPHome simpleBMPHome1 = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP1 = simpleBMPHome1.create();
        if (result && simpleBMP1.compareRecords("S")) {
            stat.addStatus(testSuite + " statementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " statementTest : ", stat.FAIL);
        }

        if(getMonitorablePropertyOfConnectionPool() == 2) {
            stat.addStatus(testSuite + " Monitoring after statementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " Monitoring after statementTest : ", stat.FAIL);
        }
        //Testing PreparedStatement object
        for (int i = 0; i < 2; i++) {
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP simpleBMP = simpleBMPHome.create();

            if (!simpleBMP.preparedStatementTest()) {
                result = false;
                break;
            }
            Thread.sleep(5000);
        }
        SimpleBMPHome simpleBMPHome2 = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP2 = simpleBMPHome2.create();
        if (result && simpleBMP2.compareRecords("PS")) {
            stat.addStatus(testSuite + " preparedStatementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " preparedStatementTest : ", stat.FAIL);
        }

        if(getMonitorablePropertyOfConnectionPool() == 4) {
            stat.addStatus(testSuite + " Monitoring after preparedStatementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " Monitoring after preparedStatementTest : ", stat.FAIL);
        }
        //Testing CallableStatement objects
        for (int i = 0; i < 2; i++) {
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP simpleBMP = simpleBMPHome.create();

            if (!simpleBMP.callableStatementTest()) {
                result = false;
                break;
            }
            Thread.sleep(5000);
        }
        SimpleBMPHome simpleBMPHome3 = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP3 = simpleBMPHome3.create();
        if (result && simpleBMP3.compareRecords("CS")) {
            stat.addStatus(testSuite + " callableStatementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " callableStatementTest : ", stat.FAIL);
        }

        if(getMonitorablePropertyOfConnectionPool() == 6) {
            stat.addStatus(testSuite + " Monitoring after callableStatementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " Monitoring after callableStatementTest : ", stat.FAIL);
        }
        stat.printSummary();
    }

    public static int getMonitorablePropertyOfConnectionPool() throws Exception {
        final String monitoringStat = "numpotentialstatementleak";
        final String urlStr = "service:jmx:rmi:///jndi/rmi://" + HOST_NAME + ":" + JMX_PORT + "/jmxrmi";
        final JMXServiceURL url = new JMXServiceURL(urlStr);

        final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
        final MBeanServerConnection connection = jmxConn.getMBeanServerConnection();

        ObjectName objectName =
            new ObjectName("amx:pp=/mon/server-mon[server],type=jdbcra-mon,name=resources/" + poolName);

        javax.management.openmbean.CompositeDataSupport returnValue =
                (javax.management.openmbean.CompositeDataSupport)
                connection.getAttribute(objectName, monitoringStat);

        System.out.println(">>>" + monitoringStat + "=" + returnValue.get("count"));
        return new Integer(returnValue.get("count").toString());
    }
}
