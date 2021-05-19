/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.util.*;
//import com.sun.enterprise.util.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.management.MBeanServerConnection;
import org.testng.annotations.*;
import test.*;

public class TestNGDriver {

    private String adminUser;
    private String adminPassword;
    private String adminHost;
    private String adminPort;
    private String isSecure;
    private boolean useRmi;
    private MBeanServerConnection mbsc;
    private String      testfileName;

    private static final String SCRIPT_COMMENT = "#"; //this is how comment is denoted, traditionally

    @Configuration(beforeTestClass = true)
    public void setUp() throws Exception {
        loadProperties();
        initializeConnection();
    }


    ///// private methods /////
    private void initializeConnection() throws Exception {
        System.out.println("Connection Properties: " + adminUser + " " + adminPassword + " " + adminHost + " " + adminPort + " " + isSecure);
        if (useRmi) {
            mbsc = MBeanServerConnectionFactory.getMBeanServerConnectionRMI(adminUser, adminPassword, adminHost, adminPort, isSecure);
            System.out.println("Using RMI: " + mbsc.toString());
        }
        else {
            mbsc = MBeanServerConnectionFactory.getMBeanServerConnectionHTTPOrHTTPS(adminUser, adminPassword, adminHost, adminPort, isSecure);
            System.out.println("Using HTTP: " + mbsc.toString());
        }
    }

    private RemoteAdminQuicklookTest c2T(final String testClass) throws RuntimeException {
        try {
            final Class c                       = Class.forName(testClass);
            final RemoteAdminQuicklookTest t    = (RemoteAdminQuicklookTest) c.newInstance();
            System.out.println("mbsc.... "  + mbsc.getDefaultDomain());
            t.setMBeanServerConnection(this.mbsc);
            return ( t );
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void runRemoteAdminTest(String testClass) {
        RemoteAdminQuicklookTest t = c2T(testClass);
        t.test();
    }

    @Test(groups = {"RemoteAdminTests"})
    public void runSimpleStandardCustomMBeanTest()
    {
        runRemoteAdminTest("test.SimpleStandardCustomMBeanTest");
    }

    @Test(groups = {"RemoteAdminTests"})
    public void runPrimitiveDataTypeMBeanTest()
    {
        runRemoteAdminTest("test.PrimitiveDataTypeMBeanTest");
    }

    @Test(groups = {"RemoteAdminTests"})
    public void runObjectNameTest()
    {
        runRemoteAdminTest("test.ObjectNameTest");
    }


    @Test(groups = {"RemoteAdminTests"})
    public void runJVMInformationTest()
    {
        runRemoteAdminTest("test.JVMInformationTest");
    }


    @Test(groups = {"RemoteAdminTests"})
    public void runAttributeSniffer()
    {
        runRemoteAdminTest("test.AttributeSniffer");
    }


    @Test(groups = {"RemoteAdminTests", "brokenTests"})
    public void runSMFTest()
    {
        runRemoteAdminTest("test.SMFTest");
    }

    @Test(groups = {"RemoteAdminTests"})
    public void runDeployManyMBeans()
    {
        runRemoteAdminTest("test.DeployManyMBeans");
    }

    @Test(groups = {"RemoteAdminTests"})
    public void runStringTest()
    {
        runRemoteAdminTest("test.StringTest");
    }

    @Test(groups = {"RemoteAdminTests", "interactiveTests"})
    public void runGetResourceTest()
    {
        runRemoteAdminTest("test.GetResourceTest");
    }



    /***
     * private void loadRmiProperties() throws Exception {
        rmip = new Properties();
        rmip.load(new BufferedInputStream(new FileInputStream(rmipf)));
        useRmi = Boolean.valueOf(rmip.getProperty("useRmi"));
        adminUser = rmip.getProperty("adminUser");
        adminPassword = rmip.getProperty("adminPassword");
        adminHost = rmip.getProperty("adminHost");
        adminPort = rmip.getProperty("adminPort");
        isSecure = rmip.getProperty("isSecure");
    }
*/
    private void loadProperties()
    {
        LocalStringsImpl lsi    = new LocalStringsImpl();
        useRmi              = lsi.getBoolean("useRmi",          true);
        adminUser           = lsi.getString("adminUser",        "admin");
        adminPassword       = lsi.getString("adminPassword",    "adminadmin");
        adminHost           = lsi.getString("adminHost",        "localhost");
        adminPort           = lsi.getString("adminPort",        "4849");
        Boolean bisSecure   = lsi.getBoolean("isSecure",        true);
        testfileName        = lsi.getString("testfile",         "tests.list");
        isSecure            = bisSecure.toString();
    }

    ///// private methods /////
}
