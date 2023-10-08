/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.test.util.results;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding One Test info.
 *
 * @Author : Ramesh Mandava
 * @Last Modified :Initial creation By Ramesh on 10/24/2001
 * @Last Modified : By Ramesh on 1/20/2002 , For preserving order of entry of testcases used a separate
 * testCaseIdVector
 * @Last Modified : By Justin Lee on 10/05/2009
 */
public class Test {
    private String id = ReporterConstants.NA;
    private String name = ReporterConstants.NA;
    private String description = ReporterConstants.NA;
    private String status = ReporterConstants.OPTIONAL;
    private String statusDescription = ReporterConstants.OPTIONAL;
    private String expected;
    private String actual;
    private List<TestCase> testCases = new ArrayList<TestCase>();

    public Test() {
    }

    public Test(String name) {
        this();
        this.name = SimpleReporterAdapter.checkNA(name);
        id = name;
    }

    public Test(String name, String description) {
        this(name);
        this.description = SimpleReporterAdapter.checkNA(description);
    }

    public void setStatus(String status) {
        this.status = SimpleReporterAdapter.checkNA(status);
    }

    public void setStatusDescription(String desc) {
        statusDescription = SimpleReporterAdapter.checkNA(desc);
        expected = null;
        actual = null;
    }

    public void setExpected(String expected) {
        this.expected = SimpleReporterAdapter.checkNA(expected);
    }

    public void setActual(String actual) {
        this.actual = SimpleReporterAdapter.checkNA(actual);
    }

    public String getId() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        name = SimpleReporterAdapter.checkNA(value);
        id = name;
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

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void addTestCase(TestCase testCase) {
        for (TestCase aCase : testCases) {
            if (aCase.getName().equals(testCase.getName())) {
                testCase.setName(testCase.getName() + SimpleReporterAdapter.DUPLICATE);
            }
        }
        testCases.add(testCase);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Test");
        sb.append("{id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", actual='").append(actual).append('\'');
        sb.append(", expected='").append(expected).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", statusDescription='").append(statusDescription).append('\'');
        sb.append(", testCases='").append(testCases).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String toXml() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<test>\n");
        buffer.append("<id>" + id + "</id>\n");
        if (!name.equals(ReporterConstants.NA)) {
            buffer.append("<name>" + name.trim() + "</name>\n");
        }
        if (!description.equals(ReporterConstants.NA)) {
            buffer.append("<description><![CDATA[" + description.trim() + "]]></description>\n");
        }
        if (!status.equals(ReporterConstants.OPTIONAL)) {
            buffer.append("<status value=\"" + status + "\">");
            if (!description.equals(ReporterConstants.OPTIONAL)) {
                buffer.append("<![CDATA[" + description.trim() + "]]>");
            } else if (expected != null && actual != null) {
                buffer.append("<expected><![CDATA[" + expected.trim() + "]]></expected>"
                    + "<actual><![CDATA[" + actual.trim() + "]]></actual>");
            }
            buffer.append("</status>\n");
        }
        if (!testCases.isEmpty()) {
            buffer.append("<testcases>\n");
            for (TestCase myTestCase : testCases) {
                buffer.append(myTestCase.toXml());
            }
            buffer.append("</testcases>\n");
        }
        buffer.append("</test>\n");
        return buffer.toString();
    }

}
