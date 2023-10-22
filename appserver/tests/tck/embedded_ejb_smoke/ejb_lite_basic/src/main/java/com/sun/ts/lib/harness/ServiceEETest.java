/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ts.lib.harness;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sun.javatest.Status;
import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.common.vehicle.VehicleRunnable;
import com.sun.ts.tests.common.vehicle.VehicleRunnerFactory;

/**
 * This abstract class must be extended by all clients of tests of J2EE service apis; for example,
 * JDBC, RMI-IIOP, JavaMail, JMS, etc. When a service test is encountered by the JavaTest Client,
 * the instance is passed to a J2EE server component and run from that remote location. Using this
 * model to develop tests allows the same test to be run from different locations within the
 * scope of the J2EE Application Programming Model.
 *
 * @author Kyle Grucci
 */
public abstract class ServiceEETest extends EETest {

    /*
     * Please do NOT change this class in an incompatible manner with respect to
     * serialization. Please see the serialization specification to determine what
     * is a compatible change versus incompatible. If you do need to change this
     * class in an incompatible manner you will need to rebuild the compat tests.
     * You should also increment the serialVersionUID field to denote that this
     * class is incompatible with older versions.
     */
    // static final long serialVersionUID = -1396452037848185296L;

    String[] sVehicles;

    private Object theSharedObject;

    private Object[] theSharedObjectArray;

    /**
     * Returns any additional properties that may need to be set by a subclass of ServiceEETest
     * for use by a specific vehicle. This impl returns an empty properties object by default.
     *
     * @param p user configured properties used by the test
     * @return Properties Additional properties that may need to be set by a subclass of ServiceEETest
     * for use by a specific vehicle.
     */
    public Properties getVehicleSpecificClientProps(Properties p) {
        return new Properties();
    }

    /**
     * When called within the harness VM, this method passes an instance of itself to the appropriate
     * J2EE server component. When called from within that server component, EETest's run method is
     * called and the test is run.
     *
     * @param argv an array of arguments that a test may use
     * @param p user configured properties used by this test
     * @return a Javatest Status object (passed or failed)
     */
    @Override
    public Status run(String[] argv, Properties p) {
        Status status = null;
        if (TestUtil.iWhereAreWe == TestUtil.VM_HARNESS && this instanceof com.sun.ts.tests.common.vehicle.VehicleClient) {
            TestUtil.logTrace("in ServiceEETest.run() method");
            String sVehicle = p.getProperty("vehicle");
            String className = this.getClass().getName();
            // use this name for the context root or jndi name to eliminate
            // naming conflicts for apps deployed at the same time
            String sVehicleEarName = p.getProperty("vehicle_ear_name");
            TestUtil.logTrace("Vehicle to be used for this test is:  " + sVehicle);
            // call to the Deliverable to run in deliverable specific vehicles
            // This should never be called on the server, so there is
            // no need to pass in the deliverable.class system property
            try {
                VehicleRunnable runner = VehicleRunnerFactory.getVehicleRunner(sVehicle);
                p.putAll(getVehicleSpecificClientProps(p));
                status = runner.run(argv, p);
            } catch (Throwable e) {
                e.printStackTrace();
                return Status.failed("Vehicle runner failed.");
            }

            return status;
        } else {
            // we're on the server in a custom vehicle, so just call on EETest
            TestUtil.logTrace("in custom vehicle so call on EETest.");
            return super.run(argv, p);
        }
    }

    @Override
    protected Properties getTestPropsFromArgs(String[] argv) {
        Properties p = new Properties();
        Properties ap = new Properties();
        String sProp = null;
        String sVal = null;
        boolean bRunIndividualTest = false;
        vLeftOverTestArgs = new Vector();

        if (TestUtil.harnessDebug)
            TestUtil.logHarnessDebug("ServiceEETest: " + argv.length + " args: " + Arrays.asList(argv));
        // load a props object if used with -p
        boolean tFound = false;
        String argItem = null;
        for (int ii = 0; ii < argv.length; ii++) {
            argItem = argv[ii];
            if (argItem.equals("-p") || argItem.equals("-ap")) {
                ap = initializeProperties(argv[++ii]);
                // add additional props to "p"
                Enumeration<?> e = ap.propertyNames();
                String key;
                while (e.hasMoreElements()) {
                    key = (String) e.nextElement();
                    p.put(key, ap.getProperty(key));
                }
            } else if (argItem.startsWith("-d") && argItem.indexOf('=') != -1) {
                int equalSign = argItem.indexOf('=');
                sProp = argItem.substring(2, equalSign);
                sVal = argItem.substring(equalSign + 1);
                p.put(sProp, sVal);
            }
            // the first -t specifies test name and should be consumed by harness.
            // Any subsequent -t is to be passed along to test.
            else if (argItem.equalsIgnoreCase("-t") && !tFound) {
                sTestCase = argv[++ii];
                tFound = true;
                bRunIndividualTest = true;
            } else if (argItem.equalsIgnoreCase("-vehicle")) {
                sVehicles = new String[1];
                sVehicles[0] = argv[++ii];
            } else {
                // there must be args that the test needs,
                // so pass these on
                vLeftOverTestArgs.addElement(argItem);
            }
        }
        if (bRunIndividualTest)
            p.setProperty("testName", sTestCase);
        return p;
    }

    @Override
    public Status run(String[] argv, PrintWriter log, PrintWriter err) {
        Status s = Status.passed("OK");
        Properties props;
        props = getTestPropsFromArgs(argv);
        // get the # of secs we should delay to allow reporting to finish
        try {
            iLogDelaySeconds = Integer.parseInt(props.getProperty("harness.log.delayseconds", "1")) * 1000;
        } catch (NumberFormatException e) {
            // set the default if a number was not set
            iLogDelaySeconds = 1000;
        }
        if (sVehicles == null) {
            if (TestUtil.harnessDebug)
                TestUtil.logHarnessDebug("ServiceEETest.run(): vehicles = null");
            sVehicles = getVehicles(props);
        }
        if (props.isEmpty())
            return Status.failed("FAILED:  An error occurred while trying to load the test properties");
        // copy leftover args to an array and pass them on
        int iSize = vLeftOverTestArgs.size();
        if (iSize == 0) {
            argv = null;
        } else {
            argv = new String[iSize];
            for (int ii = 0; ii < iSize; ii++) {
                argv[ii] = vLeftOverTestArgs.elementAt(ii);
            }
        }
        if (sTestCase == null) {
            return runAllTestCases(argv, props, log, err);
        } else {
            for (int ii = 0; ii < sVehicles.length; ii++) {
                props.put("vehicle", sVehicles[ii]);
                // need to pass these streams to the Local Reporter
                TestUtil.setCurrentTest(sTestCase, new PrintWriter(log, true), new PrintWriter(err, true));
                TestUtil.initClient(props);
                s = getPropsReady(argv, props);
                try {
                    Thread.sleep(iLogDelaySeconds);
                    TestUtil.logTrace("SLEPT FOR:  " + iLogDelaySeconds);
                } catch (InterruptedException e) {
                    logErr("Exception: " + e);
                }
            }
        }
        return s;
    }

    // Overridden to allow service tests to run in standalone mode outside of javatest
    protected Status runAllTestCases(String[] argv, Properties p, PrintStream log, PrintStream err) {
        if (sVehicles == null) {
            if (TestUtil.harnessDebug)
                TestUtil.logHarnessDebug("ServiceEETest.runAllTestCases(): vehicles = null");
            sVehicles = getVehicles(p);
        }
        Status s = Status.passed("OK");
        for (int ii = 0; ii < sVehicles.length; ii++) {
            p.put("vehicle", sVehicles[ii]);
            s = super.runAllTestCases(argv, p, new PrintWriter(log, true), new PrintWriter(err, true));
            log.println("Completed running tests in " + sVehicles[ii] + " vehicle.");
        }
        return s;
    }

    private String[] getVehicles(Properties p) {
        String[] sReturn = null;
        String sVal = null;
        String sVehiclesToUse = null;
        StringTokenizer st = null;
        try {
            // get vehicles property (DEFAULT to all)
            sVal = p.getProperty("service_eetest.vehicles");
        } catch (Exception e) {
            // got an exception looking up the prop, so set defaults
            sVal = "ejb servlet jsp";
        }
        if (sVal == null || sVal.isEmpty()) {
            sVehiclesToUse = "ejb servlet jsp";
            if (TestUtil.harnessDebug)
                TestUtil.logHarnessDebug("getVehicles:  " + "Using default - all vehicles");
        } else {
            sVehiclesToUse = sVal;
            if (TestUtil.harnessDebug)
                TestUtil.logHarnessDebug("getVehicles: using vehicle(s) - " + sVehiclesToUse);
        }
        st = new StringTokenizer(sVehiclesToUse);
        int iCount = st.countTokens();
        sReturn = new String[iCount];
        for (int ii = 0; ii < iCount; ii++) {
            // create 1 desc for each vehicle to be tested
            sReturn[ii] = st.nextToken().trim();
        }
        return sReturn;
    }

    /*
     * Set shared object
     */
    public void setSharedObject(Object o) {
        theSharedObject = o;
    }

    /*
     * Get shared object
     */
    public Object getSharedObject() {
        return theSharedObject;
    }
}
