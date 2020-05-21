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

package jakarta.security.jacc;

import jakarta.security.jacc.URLPattern;
import jakarta.security.jacc.URLPatternSpec;
import java.util.StringTokenizer;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Test {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::JACC API test ";

    public static void main ( String[] args ) {
        stat.addDescription(testSuite);
        String description = null;

        description = testSuite + "test1";
        String s = new String("/a/*:/a/b/joe.jsp:/a/b/c:/a/b/*");
        URLPatternSpec ups = new URLPatternSpec(s);
        System.out.println("s:   " + s);
        System.out.println("ups: " + ups);
        if ("/a/*:/a/b/*".equals(ups.toString())) {
            stat.addStatus(description, stat.PASS);
        } else {
            stat.addStatus(description, stat.FAIL);
        }

        description =  testSuite + "test2";
        s = new String("/:/a/b/joe.jsp:/a/b/c:/a/b/*:*.jsp:/a/*");
        ups = new URLPatternSpec(s);
        System.out.println("s:   " + s);
        System.out.println("ups: " + ups);
        if ("/:*.jsp:/a/*".equals(ups.toString())) {
            stat.addStatus(description, stat.PASS);
        } else {
            stat.addStatus(description, stat.FAIL);
        }

        stat.printSummary(testSuite);
    }
}
