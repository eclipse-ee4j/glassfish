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

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings({"StringContatenationInLoop"})
public class SimpleReporterAdapter implements Serializable {
    public static final String PASS = "pass";
    public static final String DID_NOT_RUN = "did_not_run";
    public static final String FAIL = "fail";
    private static final Pattern TOKENIZER;
    private final boolean debug = true;
    private final String ws_home;
    private Test test;
    private final String testSuiteName;
    private final TestSuite suite;
    private Reporter reporter;
    public static final String DUPLICATE = " -- DUPLICATE";

    static {
        String pattern = or(
            split("x", "X"),     // AbcDef -> Abc|Def
            split("X", "Xx"),    // USArmy -> US|Army
            //split("\\D","\\d"), // SSL2 -> SSL|2
            split("\\d", "\\D")  // SSL2Connector -> SSL|2|Connector
        );
        pattern = pattern.replace("x", "\\p{Lower}").replace("X", "\\p{Upper}");
        TOKENIZER = Pattern.compile(pattern);
    }

    @Deprecated
    public SimpleReporterAdapter() {
        this("appserv-tests", null);
    }

    @Deprecated
    public SimpleReporterAdapter(String ws_root) {
        this(ws_root, null);
    }

    public SimpleReporterAdapter(String ws_root, String suiteName) {
        ws_home = ws_root;
        if (suiteName == null) {
            testSuiteName = getTestSuiteName();
        } else {
            testSuiteName = suiteName;
        }
        suite = new TestSuite(testSuiteName);
        test = new Test(testSuiteName);
        suite.addTest(test);
    }

    public Reporter getReporter() {
        return reporter;
    }

    public TestSuite getSuite() {
        return suite;
    }

    public void addStatus(String testCaseName, String status) {
        addStatus(testCaseName, status, "");
    }

    public void addStatus(String testCaseName, String status, String message) {
        final TestCase testCase = new TestCase(testCaseName, message);
        testCase.setStatus(status);
        test.addTestCase(testCase);
    }

    public void addDescription(String description) {
        suite.setDescription(description);
    }

    @Deprecated
    public void printSummary(String s) {
        printSummary();
    }

    public void printSummary() {
        try {
            reporter = Reporter.getInstance(ws_home);
            if (debug) {
                System.out.println("Generating report at " + reporter.getResultFile());
            }
            reporter.setTestSuite(suite);
            int pass = 0;
            int fail = 0;
            int d_n_r = 0;
            System.out.println("\n\n-----------------------------------------");
            for (Test test : suite.getTests()) {
                for (TestCase testCase : test.getTestCases()) {
                    String status = testCase.getStatus();
                    if (status.equalsIgnoreCase(PASS)) {
                        pass++;
                    } else if (status.equalsIgnoreCase(DID_NOT_RUN)) {
                        d_n_r++;
                    } else {
                        fail++;
                    }
                    System.out.println(String.format("- %-37s -", testCase.getName() + ": " + status.toUpperCase()));
                }
            }
            if (pass == 0 && fail == 0 && d_n_r == 0) {
                d_n_r++;
                System.out.println(String.format("- %-37s -", testSuiteName + ": " + DID_NOT_RUN));
                final TestCase testCase = new TestCase(testSuiteName);
                testCase.setStatus(DID_NOT_RUN);
                test.addTestCase(testCase);
            }
            System.out.println("-----------------------------------------");
            result("PASS", pass);
            result("FAIL", fail);
            result("DID NOT RUN", d_n_r);
            System.out.println("-----------------------------------------");
            reporter.flushAll();
            createConfirmationFile();
        }
        catch (Throwable ex) {
            System.out.println("Reporter exception occurred!");
            if (debug) {
                ex.printStackTrace();
            }
        }
    }

    private void result(final String label, final int count) {
        System.out.println(String.format("- Total %-12s: %-17d -", label, count));
    }

    public void createConfirmationFile() {
        try {
            FileOutputStream fout = new FileOutputStream("RepRunConf.txt");
            try {
                fout.write("Test has been reported".getBytes());
            } finally {
                fout.close();
            }
        } catch (Exception e) {
            System.out.println("Exception while creating confirmation file!");
            if (debug) {
                e.printStackTrace();
            }
        }
    }

    private String getTestSuiteName() {
        List<StackTraceElement> list = new ArrayList<StackTraceElement>(
            Arrays.asList(Thread.currentThread().getStackTrace()));
        list.remove(0);
        File jar = locate(getClass().getName().replace('.', '/') + ".class");
        while (jar.equals(locate(list.get(0).getClassName().replace('.', '/') + ".class"))) {
            list.remove(0);
        }
        StackTraceElement element = list.get(0);
        File file = locate(element.getClassName().replace('.', '/') + ".class");
        StringBuilder buf = new StringBuilder(file.getName().length());
        for (String t : TOKENIZER.split(file.getName())) {
            if (buf.length() > 0) {
                buf.append('-');
            }
            buf.append(t.toLowerCase());
        }
        return buf.toString().trim();
    }

    public File locate(String resource) {
        String u = getClass().getClassLoader().getResource(resource).toString();
        File file = null;
        try {
            if (u.startsWith("jar:file:")) {
                file = new File(new URI(u.substring(4, u.indexOf("!"))));
            } else if (u.startsWith("file:")) {
                file = new File(new URI(u.substring(0, u.indexOf(resource))));
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return file;
    }

    private static String or(String... tokens) {
        StringBuilder buf = new StringBuilder();
        for (String t : tokens) {
            if (buf.length() > 0) {
                buf.append('|');
            }
            buf.append(t);
        }
        return buf.toString();
    }

    private static String split(String lookback, String lookahead) {
        return "((?<=" + lookback + ")(?=" + lookahead + "))";
    }

    public static String checkNA(final String value) {
        return value == null ? ReporterConstants.NA : value.trim();
    }
}
