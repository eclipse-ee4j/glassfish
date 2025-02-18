/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * This is the same class as the one in v2/appserv-tests/devtests/admin/cli/src/admin.
 *
 * Thanks Tom and Byron for the contributions.
 */

package com.sun.appserv.test;

//import com.sun.appserv.test.BaseDevTest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class AdminBaseDevTest extends BaseDevTest {

    public AdminBaseDevTest() {
        boolean verbose = false;
        try {
            verbose = Boolean.parseBoolean(System.getProperty("verbose"));
        }
        catch (Exception e) {}

        setVerbose(verbose);
        if (!verbose) {
//            System.out.println("#####  Non-Verbose: Only Failures Are Printed #####");
        }
    }

    @Override
    public String getTestName() {
        return this.getClass().getName();
    }

    @Override
    public boolean report(String name, boolean success) {
        // bnevins june 6 2010

        // crazy base class uses a Map to store these reports.  If you use
        // the same name > 1 time they are ignored and thrown away!!!
        // I went with this outrageous kludge because (1) it is just tests
        // and (2) there are tens of thousands of other files in this harness!!!

        // another issue is hacking off strings after a space.  Makes no sense to me!!

        if (name.length() > MAX_LENGTH - 3)
            name = name.substring(0, MAX_LENGTH - 3);
        String name2 = name.replace(' ', '_');
        if (!name2.equals(name)) {
            write("Found spaces in the name.  Replaced with underscore. "
                    + "before: " + name + ", after: " + name2);
            name = name2;   // don't foul logic below!
        }

        int i = 0;

        while (reportNames.add(name2) == false) {
            name2 = name + i++;
        }

        if (!name2.equals(name)) {
            write("Duplicate name found (" + name
                    + ") and replaced with: " + name2);
        }

        int numpads = 60 - name2.length();

        if (numpads > 0)
            name2 += DASHES.substring(0, numpads);

        super.report(name2, success);

        if(!success && !isVerbose())
            writeFailure();
        return success;
    }

    @Override
    public boolean report(String step, AsadminReturn ret) {
        report(step, ret.returnValue);
        return ret.returnValue;
    }

    void startDomain(String domainname) {
        report(getTestName() + "-start-domain" + startstops++,
                asadmin("start-domain", domainname));
    }

    void startDomain() {
        report(getTestName() + "-start-def-domain" + startstops++,
                asadmin("start-domain"));
    }

    void stopDomain(String domainname) {
        report(getTestName() + "-stop-domain" + startstops++,
                asadmin("stop-domain", domainname));
    }

    void stopDomain() {
        report(getTestName() + "-stop-def-domain" + startstops++,
                asadmin("stop-domain"));
    }

    final boolean verifyNoClusters() {
        AsadminReturn ret = asadminWithOutput("list-clusters");
        String s = (ret.out == null) ? "" : ret.out.trim();

        System.out.println("WARNING!!!!  Work-around for ISSUE 12320 !!!!!!!!");

        // hack -- if there are no clusters than there is no output
        return s.toLowerCase().endsWith("list-clusters");
    }

    final boolean verifyNoInstances() {
        AsadminReturn ret = asadminWithOutput("list-instances");
        String s = (ret.out == null) ? "" : ret.out.trim();
        return s.toLowerCase().indexOf("nothing to list") >= 0;
    }

    /*
     * Returns true if String b contains String a.
     */
    public static boolean matchString(String a, String b) {
        return b.indexOf(a) != -1;
    }

    public static String getURL(String urlstr) {
        try {
            URL u = new URL(urlstr);
            URLConnection urlc = u.openConnection();
            BufferedReader ir = new BufferedReader(new InputStreamReader(urlc.getInputStream(),
                    "ISO-8859-1"));
            StringWriter ow = new StringWriter();
            String line;
            while ((line = ir.readLine()) != null) {
                ow.write(line);
                ow.write("\n");
            }
            ir.close();
            ow.close();
            return ow.getBuffer().toString();
        }
        catch (IOException ex) {
            printf("unable to fetch URL:" + urlstr);
            return "";
        }
    }

    public static void printf(String fmt, Object... args) {
        if (DEBUG) {
            System.out.printf("**** DEBUG MESSAGE ****  " + fmt + "\n", args);
        }
    }
    private final SortedSet<String> reportNames = new TreeSet<String>();
    private int startstops = 0;
    protected final static boolean DEBUG;
    protected final static boolean isHudson = Boolean.parseBoolean(System.getenv("HUDSON"));

    static {
        if (isHudson)
            DEBUG = true;
        else if (Boolean.parseBoolean(System.getenv("AS_DEBUG")))
            DEBUG = true;
        else
            DEBUG = false;
    }

    String generateInstanceName() {
        String s = "" + System.currentTimeMillis();
        s = s.substring(4, 10);
        return "in_" + s;
    }

    String get(String what) {
        // note that the returned string is full of junk -- namely the HUGE asadmin
        // command is prepended to the output.
        // the "get" key will appear TWICE!!!!!!  Once for the echo of the command itself
        // and once for the output of the command.

        AsadminReturn ret = asadminWithOutput("get", what);
        if (!ret.returnValue)
            return null;

        int index = ret.outAndErr.lastIndexOf(what);
        int len = ret.outAndErr.length();

        if (index < 0 || len - index <= 2)
            return null;

        // e.g. "asadmin blah foo=xyz  len==20, index==13,  start at index=17
        // which is index+lenofget-string+1
        int valueIndex = index + what.length() + 1;
        return ret.outAndErr.substring(index + what.length() + 1).trim();
    }

    final boolean doesGetMatch(String what, String match) {
        String ret = get(what);

        if (!ok(match) && !ok(ret))
            return true;

        if (!ok(match) || !ok(ret))
            return false;

        return (match.equals(ret));
    }

//    public static void runFakeServerDaemon(int port) {
//        Thread t = new Thread(new FakeServer(port), "FakeServerListeningOn: " + port);
//        t.setDaemon(true);
//        t.start();
//    }

    final boolean ok(String s) {
        return s != null && s.length() > 0;
    }
    private static final int MAX_LENGTH = 51;
    private static final String DASHES =
            "------------------------------------------------------------------------------------------------------------------------------";
}
