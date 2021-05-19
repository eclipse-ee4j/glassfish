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
 * @Class: TestSuite
 * @Description: Class holding One TestSuite info.
 * @Author : Ramesh Mandava
 * @Last Modified : By Ramesh on 10/24/2001
 * @Last Modified : By Ramesh on 1/20/2002 , For preserving order of entry of tests                 used a separate testIdVector
 * @Last Modified : By Justin Lee on 10/05/2009
 */
public class TestSuite {
    private String id;
    private String name = ReporterConstants.NA;
    private String description = ReporterConstants.NA;
    private List<Test> tests = new ArrayList<Test>();
    int pass;
    int fail;
    int didNotRun;
    int total;
    public int number;
    private boolean written;

    public TestSuite() {
    }

    public TestSuite(String name) {
        this();
        this.name = SimpleReporterAdapter.checkNA(name);
        id = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = SimpleReporterAdapter.checkNA(description);
    }

    public List<Test> getTests() {
        return tests;
    }

    public void addTest(Test test) {
        for (Test aTest : tests) {
            if(aTest.getName().equals(test.getName())) {
                test.setName(test.getName() + SimpleReporterAdapter.DUPLICATE);
            }
        }
        tests.add(test);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TestSuite");
        sb.append("{id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", tests=").append(tests);
        sb.append('}');
        return sb.toString();
    }

    public String toXml() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<testsuite>\n");
        buffer.append("<id>" + id.trim() + "</id>\n");
        if (!name.equals(ReporterConstants.NA)) {
            buffer.append("<name>" + name.trim() + "</name>\n");
        }
        if (!description.equals(ReporterConstants.NA)) {
            buffer.append("<description><![CDATA[" + description.trim() + "]]></description>\n");
        }
        buffer.append("<tests>\n");
        for (Test myTest : tests) {
            buffer.append(myTest.toXml());
        }
        buffer.append("</tests>\n");
        buffer.append("</testsuite>\n");
        return buffer.toString();
    }

    public String toHtml() {
        StringBuilder table = new StringBuilder(
            "<div id=\"table" + number + "\" class=\"suiteDetail\"><table width=\"40%\">"
                + ReportHandler.row(null, "td", "Testsuite Name", getName())
                + ReportHandler.row(null, "td", "Testsuite Description", getDescription())
                + ReportHandler.row(null, "th", "Name", "Status"));
        for (Test test : getTests()) {
            for (TestCase testCase : test.getTestCases()) {
                final String status = testCase.getStatus();
                table.append(String.format("<tr><td>%s</td>%s", testCase.getName(),
                    ReportHandler.cell(status.replaceAll("_", ""), 1, status)));
            }
        }
        return table
            + "<tr class=\"nav\"><td colspan=\"2\">"
            + "[<a href=#DetailedResults>Detailed Results</a>"
            + "|<a href=#Summary>Summary</a>"
            + "|<a href=#TOP>Top</a>]"
            + "</td></tr>"
            + "</table></div><p>";

    }

    public boolean getWritten() {
        return written;
    }

    public void setWritten(final boolean written) {
        this.written = written;
    }
}
