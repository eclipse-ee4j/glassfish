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

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class TestURLPattern {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::JACC API testURLPattern ";

    private static void test_patternType(String p, int expected) {
        String description = "testPatternType:" + p;
        URLPattern u = new URLPattern(p);
        int result = u.patternType();
        if (result == expected) { 
            System.out.println("patternType: " + expected + " " +
                    result + " succeded " + u);
            stat.addStatus(description, stat.PASS);

        } else {
            System.out.println("patternType: " + expected + " " +
                    result + " failed   " + u);
            stat.addStatus(description, stat.FAIL);
        }
    }

    private static void test_compareTo(String p1, int p1Type,
            String p2, int p2Type) {
        String description = "testCompareTo:" +
            p1 + "-" + p1Type + "-" + p2 + "-" + p2Type;
        URLPattern u1 = new URLPattern(p1);
        URLPattern u2 = new URLPattern(p2);

        int expected = p1Type - p2Type;
        boolean unknown = false;
        if (expected == 0 && p1.compareTo(p2) != 0) unknown = true;
        expected = expected > 0 ? 1 : (expected < 0 ? -1 : 0);

        int result = u1.compareTo(u2);
        int inverse = u2.compareTo(u1);

        if (result == inverse * -1) {
            if (result == expected) {
                System.out.println("compareTo(->): " + expected + " " +
                        result + " succeded " + u1 + "\t" + u2);
                stat.addStatus(description, stat.PASS);
            } else if (result != 0 && unknown) {
                System.out.println("compareTo(->): !0 " + 
                        result +  " succeded " + u1 + "\t" + u2);
                stat.addStatus(description, stat.PASS);
            } else {
                System.out.println("compareTo(->): " + expected + " " + 
                        result + " failed    " + u1 + "\t" + u2);
                stat.addStatus(description, stat.FAIL);
            }
        } else {
            System.out.println("compareTo(<-): " + expected * -1 + " " + 
                    inverse + " failed    " + u2 + "\t" + u1);
            stat.addStatus(description, stat.FAIL);
        }
    }

    private static boolean get_impliesExpected(
            String p1, int p1Type, String p2, int p2Type) {
        boolean expected = false;
        switch(p1Type) {
        case URLPattern.PT_EXACT:
            if (p2Type == URLPattern.PT_EXACT && p1.equals(p2)) {
                expected = true;
            }
            break;
        case URLPattern.PT_DEFAULT:    
            expected = true;
            break;
        case URLPattern.PT_EXTENSION:
            if (p2Type == URLPattern.PT_EXTENSION && p1.equals(p2)) {
                expected = true;
            } else if (p2Type == URLPattern.PT_EXACT && 
                     p2.endsWith(p1.substring(1))) {
                expected = true;
            }
            break;
        case URLPattern.PT_PREFIX:
            if (p1.equals("/*")) {
                expected = true;
            } else if (p2.startsWith(p1.substring(0,p1.length()-2)) &&
                     (p2.length() == p1.length()-2 || 
                      p2.substring(p1.length()-2).startsWith("/"))) {
                expected = true;
            }
            break;
        }

        return expected;
    }

    private static void test_implies(
            String p1, int p1Type, String p2, int p2Type) {
        String description = "testImplies:" +
            p1 + "-" + p1Type + "-" + p2 + "-" + p2Type;
        URLPattern u1 = new URLPattern(p1);
        URLPattern u2 = new URLPattern(p2);

        boolean expected = get_impliesExpected(p1,p1Type,p2,p2Type);
        boolean expectedInverse = get_impliesExpected(p2,p2Type,p1,p1Type);

        boolean result = u1.implies(u2);
        boolean inverse = u2.implies(u1);

        if (result == expected) { 
            System.out.println("implies(->): " + expected + " " + result + 
                    " succeded " + u1 + "\t" + u2);
            stat.addStatus(description, stat.PASS);
        } else if (inverse != expectedInverse) {
            System.out.println("implies(<-): " + expectedInverse + " " + 
                    inverse + " failed    " + u2 + "\t" + u1);
            stat.addStatus(description, stat.FAIL);
        } else {
            System.out.println("implies(->): " + expected + " " + result + 
                    " failed    " + u1 + "\t" + u2);
            stat.addStatus(description, stat.FAIL);
        }
    }


    private static void test_equals(
            String p1, int p1Type, String p2, int p2Type) {
        String description = "testEquals:" + 
            p1 + "-" + p1Type + "-" + p2 + "-" + p2Type;
        URLPattern u1 = new URLPattern(p1);
        URLPattern u2 = new URLPattern(p2);

        boolean expected = (p1Type == p2Type) ? p1.compareTo(p2) == 0 : false;

        boolean result = u1.equals(u2);
        boolean inverse = u2.equals(u1);

        if (result == inverse) {
            if (result == true && (!u1.implies(u2) || !u2.implies(u1))) {
                System.out.println("equals(<->): " + expected + " " + 
                        result + " failed    " + u2 + "\t" + u1);
                stat.addStatus(description, stat.FAIL);

            } else if (result == expected) {
                System.out.println("equals(-->): " + expected + " " +
                        result + " succeded " + u1 + "\t" + u2);
                stat.addStatus(description, stat.PASS);

            } else { 
                System.out.println("equals(-->): " + expected + " " + 
                        result + " failed    " + u1 + "\t" + u2);
                stat.addStatus(description, stat.FAIL);
            }
        } else {
            System.out.println("equals(<--): " + result + " " + 
                    inverse + " failed    " + u2 + "\t" + u1);
            stat.addStatus(description, stat.FAIL);
        }
    }

    public static void main ( String[] args ) {
        stat.addDescription(testSuite);

        String upArray[] = {
            "/a/b/c.jsp",
            "/a/c",
            "/*",
            "/a/*", 
            "/a/c/*",
            "//*",
            "*.jsp",
            "*.asp",
            "/"
            /* COMMENTED OUT! ,"//" */
        };

        int upTypeArray[] = {
            URLPattern.PT_EXACT,
            URLPattern.PT_EXACT,
            URLPattern.PT_PREFIX,
            URLPattern.PT_PREFIX,
            URLPattern.PT_PREFIX,
            URLPattern.PT_PREFIX,
            URLPattern.PT_EXTENSION,
            URLPattern.PT_EXTENSION,
            URLPattern.PT_DEFAULT
            /* COMMENTED OUT! ,URLPattern.PT_DEFAULT */
        };

        for (int i=0; i<upArray.length; i++) { 
            test_patternType(upArray[i],upTypeArray[i]);
        }
    
        for (int i=0; i<upArray.length; i++) {
            for (int j=0; j<upArray.length; j++) {
                test_compareTo(upArray[i],upTypeArray[i],
                               upArray[j],upTypeArray[j]);
            }
        }
    
        for (int i=0; i<upArray.length; i++) {
            for (int j=0; j<upArray.length; j++) {
                test_implies(upArray[i],upTypeArray[i],
                             upArray[j],upTypeArray[j]);
            }
        }

        for (int i=0; i<upArray.length; i++) { 
            for (int j=0; j<upArray.length; j++) {
                test_equals(upArray[i],upTypeArray[i],
                             upArray[j],upTypeArray[j]);
            }
        }
    
        stat.printSummary(testSuite);
    }
}
