/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.ejte.ccl.reporter;


import java.util.Hashtable;
import java.util.Vector;

/**
 * Class holding One Test info.
 * @author Ramesh Mandava
 * LastModified :Initial creation By Ramesh on 10/24/2001
 * LastModified : By Ramesh on 1/20/2002 , For preserving order of entry of testcases used a separate testCaseIdVector
 */
public class Test {

    private String id;
    private String name;
    private String description;

    private String status;
    private String statusDescription;

    private String expected;
    private String actual;

    private Hashtable<String, TestCase> testCaseHash;
    private Vector<String> testCaseIdVector; // Added to preserve order of entry

    public Test(String id, String name, String description) {
        this.id = id.strip();
        this.name = name;
        this.description = description;
        this.status = ReporterConstants.OPTIONAL;
        this.statusDescription = ReporterConstants.OPTIONAL;
        this.expected = null;
        this.actual = null;
        testCaseHash = new Hashtable<>();
        testCaseIdVector = new Vector<>();
    }


    public Test(String id, String name) {
        this.id = id.strip();
        this.name = name;
        this.description = ReporterConstants.NA;
        this.status = ReporterConstants.OPTIONAL;
        this.statusDescription = ReporterConstants.OPTIONAL;
        this.expected = null;
        this.actual = null;
        testCaseHash = new Hashtable<>();
        testCaseIdVector = new Vector<>();
    }


    public Test(String id) {
        this.id = id.strip();
        this.name = ReporterConstants.NA;
        this.description = ReporterConstants.NA;
        this.status = ReporterConstants.OPTIONAL;
        this.statusDescription = ReporterConstants.OPTIONAL;
        this.expected = null;
        this.actual = null;
        testCaseHash = new Hashtable<>();
        testCaseIdVector = new Vector<>();
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
        this.expected = null;
        this.actual = null;
    }


    public void setExpected(String expected) {
        this.expected = expected;
    }


    public void setActual(String actual) {
        this.actual = actual;
    }


    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }


    public String getStatus() {
        return status;
    }


    public String getStatusDescription() {
        return statusDescription;
    }


    public String getExpected() {
        return expected;
    }


    public String getActual() {
        return actual;
    }


    public Vector<String> getTestCaseIdVector() {
        return testCaseIdVector;
    }


    public void setTestCaseIdVector(Vector<String> tidVector) {
        testCaseIdVector = tidVector;
    }


    public TestCase findTestCase(String id) {
        return testCaseHash.get(id.strip());
    }


    public void addTestCase(TestCase testCase) {
        if (testCaseHash.put(testCase.getId(), testCase) != null) {
            System.err.println(" Error? Test Case : " + testCase.getId()
                + " already added. Overwriting the previous test case");
        }
        testCaseIdVector.addElement(testCase.getId());
    }
}
