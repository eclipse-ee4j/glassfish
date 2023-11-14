/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.devtests.reportbuilder;

import com.sun.appserv.test.util.results.HtmlReportProducer;
import com.sun.appserv.test.util.results.SimpleReporterAdapter;
import com.sun.appserv.test.util.results.Test;
import com.sun.appserv.test.util.results.TestCase;
import com.sun.appserv.test.util.results.TestSuite;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportTest {

    @org.junit.jupiter.api.Test
    public void htmlReporter() throws IOException, XMLStreamException {
        try (HtmlReportProducer producer = new HtmlReportProducer(
            "target/test-classes/ejb_devtests_test_resultsValid.xml", false)) {
            producer.produce();
        }
    }

    @org.junit.jupiter.api.Test
    public void stats() {
        SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "report");
        stat.addStatus("first test", SimpleReporterAdapter.FAIL);
        stat.addStatus("second test", SimpleReporterAdapter.PASS);
        stat.addStatus("second test", SimpleReporterAdapter.FAIL);
        stat.printSummary();
        final List<Test> tests = stat.getSuite().getTests();
        assertEquals(tests.size(), 1, "Should be only 1 Test");
        final List<TestCase> testCases = tests.iterator().next().getTestCases();
        assertEquals(testCases.size(), 3, "Should have 3 test cases");
        // first test
        final Iterator<TestCase> iterator = testCases.iterator();
        assertEquals(iterator.next().getStatus(), SimpleReporterAdapter.FAIL, "Should have failed.");
        // second test
        assertEquals(iterator.next().getStatus(), SimpleReporterAdapter.PASS, "Should have passed.");
        assertEquals(iterator.next().getStatus(), SimpleReporterAdapter.FAIL, "Should have failed.");
    }

    @org.junit.jupiter.api.Test
    public void duplicates() {
        TestSuite suite = new TestSuite("suite");
        Test test = new Test("test", "i have duplicates");
        suite.addTest(test);
        final String name = "case 1";
        final TestCase case1 = new TestCase(name);
        final TestCase case2 = new TestCase(name);
        final TestCase case3 = new TestCase(name);
        test.addTestCase(case1);
        test.addTestCase(case2);
        assertEquals(2, test.getTestCases().size());
        assertEquals(case1.getName(), name);
        assertEquals(case2.getName(), name + SimpleReporterAdapter.DUPLICATE);
        test.addTestCase(case3);
        assertEquals(case3.getName(), name + SimpleReporterAdapter.DUPLICATE + SimpleReporterAdapter.DUPLICATE);
    }
}
