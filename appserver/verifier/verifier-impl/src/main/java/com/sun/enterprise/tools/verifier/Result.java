/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.verifier;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import com.sun.enterprise.tools.verifier.util.LogDomains;

public class Result {

    public static final int PASSED = 0;
    public static final int FAILED = 1;
    public static final int WARNING = 2;
    public static final int NOT_APPLICABLE = 3;
    public static final int NOT_RUN = 4;
    public static final int NOT_IMPLEMENTED = 5;
    private int status = NOT_RUN;

    public static final String APP = "application"; // NOI18N
    public static final String EJB = "ejb"; // NOI18N
    public static final String WEB = "web"; // NOI18N
    public static final String APPCLIENT = "appclient"; // NOI18N
    public static final String CONNECTOR = "connector"; // NOI18N
    public static final String WEBSERVICE = "webservice"; // NOI18N
    public static final String WEBSERVICE_CLIENT = "webservice_client"; // NOI18N

    private String moduleName;

    private String componentName;
    private String assertion;
    private String testName;
    private Vector<String> errorDetails = new Vector<String>();
    private Vector<String> goodDetails = new Vector<String>();
    private Vector<String> warningDetails = new Vector<String>();
    private Vector<String> naDetails = new Vector<String>();
    boolean debug = Verifier.isDebug();

    private Logger logger = LogDomains.getLogger(
            LogDomains.AVK_VERIFIER_LOGGER);
    private FaultLocation faultLocation;

    /**
     * Result Constructor
     */
    public Result() {
        faultLocation = new FaultLocation();
    }


    /**
     * Initialize the Result object
     *
     * @param c Class of the current test/assertion
     * @param compName
     */
     private static final LocalStringsImpl strings = new LocalStringsImpl(Verifier.class);
    public void init(Class c, String version, String compName) {
        setComponentName(compName);
        StringBuffer assertion = new StringBuffer(
                StringManagerHelper.getLocalStringsManager().getLocalString(
                        (c.getName() + ".assertion"), "")); // NOI18N
        String key = ".specMappingInfo_"+version; // NOI18N
        String file="server log";
        StringBuffer specMappingInfo = new StringBuffer(
                StringManagerHelper.getLocalStringsManager().getLocalString(
                        (c.getName() + key), ""));
        // if specMappingInfo_<version> is unavailable then try just specMappingInfo
        if(specMappingInfo == null || specMappingInfo.length() == 0) {
            key = c.getName() + ".specMappingInfo";
            specMappingInfo = new StringBuffer(StringManagerHelper.getLocalStringsManager().getLocalString(key, ""));
        }
         String  prefix = strings.get(
                (getClass().getName() + ".prefix"), file); // NOI18N
        String  suffix = StringManagerHelper.getLocalStringsManager().getLocalString(
                (getClass().getName() + ".suffix"), ""); // NOI18N

        if (specMappingInfo != null && specMappingInfo.length()!=0)
            setAssertion(assertion.append(" ").append(prefix+" ").append(specMappingInfo).append(" "+suffix).toString()); // NOI18N
        else
            setAssertion(assertion.toString());
        String this_package = "com.sun.enterprise.tools.verifier."; // NOI18N
        setTestName(c.getName().substring(this_package.length()));
    }

    /**
     * Store passed info
     *
     * @param detail Details of passed test
     */
    public void passed(String detail) {
        setStatus(PASSED);
        addGoodDetails(detail);
    }

    /**
     * Store warning info
     *
     * @param detail Details of warning test
     */
    public void warning(String detail) {
        setStatus(WARNING);
        addWarningDetails(detail);
    }

    /**
     * Store Not Applicable info
     *
     * @param detail Details of not applicable test
     */
    public void notApplicable(String detail) {
        setStatus(NOT_APPLICABLE);
        addNaDetails(detail);
    }

    /**
     * Store Failed info
     *
     * @param detail Details of failed test
     */
    public void failed(String detail) {
        setStatus(FAILED);
        addErrorDetails(detail);
    }

    /**
     * Retrieve Not Applicable details
     *
     * @return <code>Vector</code> not applicable details
     */
    public Vector getNaDetails() {
        if(naDetails.isEmpty()){
            Vector<String> result = new Vector<String>();
            result.add(StringManagerHelper.getLocalStringsManager()
                    .getLocalString("tests.componentNameConstructor", // NOI18N
                            "For [ {0} ]", // NOI18N
                            new Object[]{getComponentName()}));
            result.add(StringManagerHelper.getLocalStringsManager()
                    .getLocalString(getClass().getName() + ".defaultNADetails", //NOI18N
                            "Test is not applicable.")); // NOI18N
            logger.fine("Returning default NADetails."); // NOI18N
            return result;
        }
        return naDetails;
    }

    /**
     * Retrieve Warning details
     *
     * @return <code>Vector</code> warning details
     */
    public Vector getWarningDetails() {
        return warningDetails;
    }

    /**
     * Retrieve Not Applicable details
     *
     * @param s not applicable details
     */
    public void addNaDetails(String s) {
        naDetails.addElement(s);
        logger.log(Level.FINE, s);
    }

    /**
     * Retrieve Good details
     *
     * @return <code>Vector</code> good details
     */
    public Vector getGoodDetails() {
        if(goodDetails.isEmpty()){
            Vector<String> result = new Vector<String>();
            result.add(StringManagerHelper.getLocalStringsManager()
                    .getLocalString("tests.componentNameConstructor", // NOI18N
                            "For [ {0} ]", // NOI18N
                            new Object[]{getComponentName()}));
            result.add(StringManagerHelper.getLocalStringsManager()
                    .getLocalString(getClass().getName() + ".defaultGoodDetails", //NOI18N
                            "There were no errors reported.")); // NOI18N
            logger.fine("Returning default GoodDetails."); // NOI18N
            return result;
        }
        return goodDetails;
    }

    /**
     * Fill in Good details
     *
     * @param s good detail string
     */
    public void addGoodDetails(String s) {
        goodDetails.addElement(s);
        logger.log(Level.FINE, s);
    }

    /**
     * Fill in Warning details
     *
     * @param s warning detail string
     */
    public void addWarningDetails(String s) {
        warningDetails.addElement(s);
        logger.log(Level.FINE, s);
    }

    /**
     * Retrieve Error details
     *
     * @return <code>Vector</code> error details
     */
    public Vector getErrorDetails() {
        return errorDetails;
    }

    /**
     * Fill in Error details
     *
     * @param s error detail string
     */
    public void addErrorDetails(String s) {
        errorDetails.addElement(s);
        logger.log(Level.FINE, s);
    }

    /**
     * Retrieve test result status
     *
     * @return <code>int</code> test result status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set test result status
     *
     * @param s test result status
     */
    public void setStatus(int s) {
        status = s;
    }

    /**
     * Retrieve assertion
     *
     * @return <code>String</code> assertion string
     */
    public String getAssertion() {
        return assertion;
    }

    /**
     * Set assertion
     *
     * @param s assertion string
     */
    public void setAssertion(String s) {
        assertion = s;
    }

    /**
     * Retrieve component/module name
     *
     * @return <code>String</code> component/module name
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Set component/module name
     *
     * @param s component/module name
     */
    public void setComponentName(String s) {
        componentName = s;
    }

    /**
     * Retrieve test name
     *
     * @return <code>String</code> test name
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Set test name
     *
     * @param s test name
     */
    public void setTestName(String s) {
        testName = s;
    }

    public void setModuleName(String name) {
        moduleName = name;
    }

    public String getModuleName() {
        return moduleName;
    }

    public FaultLocation getFaultLocation() {
        return faultLocation;
    }
} // Result class
