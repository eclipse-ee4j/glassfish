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
 * AdminBaseDevTest is a base class for administration CLI dev tests.
 *
 * @author Byron Nevins
 * @author tmueller
 */
package admin;

import com.sun.appserv.test.BaseDevTest;
import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.SortedSet;
import java.util.TreeSet;
import static admin.Constants.*;
import javax.xml.namespace.QName;

public abstract class AdminBaseDevTest extends BaseDevTest implements Runnable {

    static enum PasswordType {

        SSH_PASS, KEY_PASS, ALIAS_PASS
    };

    @Override
       protected final void writeFailure() {
        // writes to stdout
        super.writeFailure();
        AsadminReturn ret = getLastAsadminReturn();

        // put the information right into the CLI log.  Makes it much easier
        // to figure out failures.

        if (ret != null) {
           TestUtils.writeErrorToDebugLog(ret);
        }

        // byron nevins, June 2013.  We need an easy way to see if there were
        // ANY errors at all
        TestUtils.setErrorFile();
    }

    protected AdminBaseDevTest() {
        boolean verbose = false;
        try {
            verbose = Boolean.parseBoolean(System.getProperty("verbose"));
        }
        catch (Exception e) {
        }

        setVerbose(verbose);
        if (!verbose) {
            System.out.println("#####  Non-Verbose: Only Failures Are Printed #####");
        }
    }

    // Byron Says:
    // let's make this an abstract method someday when we have time to change
    // all the test classes
    public void subrun() {
        // all subclasses someday should have this method which is called by run()
        // the subclass should call run() which will help with housekeeping.
        // I'm not enforcing yet because I don't have time to change all the
        // classes!  10/12/11
        // do nothing.
    }

    @Override
    public File getGlassFishHome() {
        return TestEnv.getGlassFishHome();
    }

    // Allow old-style and new-style simultaneously.  But prefer the latter.
    @Override
    public File getDASDomainDir() {
        return getDASDomainDir("domain1");
    }

    File getDASDomainDir(String domainName) {
        File f = null;

        if (isHadas())
            f = new File(DOMAINS_DIR, domainName + "/server");
        else
            f = new File(DOMAINS_DIR, domainName);

        if (!f.exists())
            report(false, "Domain Directory does not exist: " + f);

        return f;
    }

     @Override
    public File getDASDomainXML() {
         return TestEnv.getDomainXml();
    }

    File getDASDomainXML(String domainName) {
         return TestEnv.getDomainXml(domainName);
    }

    @Override
    public Object evalXPath(String expr, QName ret) {
        return evalXPath(expr, getDASDomainXML(), ret);
    }

    @Override
    public final void run() {
        System.out.println(getTestDescription());
        System.out.println(getTestName());
        subrun();
        stat.printSummary();
    }

    @Override
    public String getTestName() {
        return this.getClass().getName();
    }

    // convenience method
    static boolean isHadas() {
        return TestEnv.isHadas();
    }

    public static File getLogFile(File installDir, String domainName) {
        if (isHadas()) {
            return new File(installDir, "domains/" + domainName + "/server/logs/server.log");
        }
        else {
            return new File(installDir, "domains/" + domainName + "/logs/server.log");
        }
    }

    public void report(boolean success, String name) {
        // WBN ==> sometimes I like it in this order!
        report(name, success);
    }

    @Override
    public void report(String name, boolean success) {
        // bnevins june 6 2010

        // crazy base class uses a Map to store these reports.  If you use
        // the same name > 1 time they are ignored and thrown away!!!
        // I went with this outrageous kludge because (1) it is just tests
        // and (2) there are tens of thousands of other files in this harness!!!

        // another issue is hacking off strings after a space.  Makes no sense to me!!
        if (!success) {
            int x = 5;  // a place to hang a breakpoint when debugging!!
        }
        writeTimestamp(name, success);
        if (name.length() > MAX_LENGTH - 3) {
            name = name.substring(0, MAX_LENGTH - 3);
        }
        String name2 = name.replace(' ', '_');
        if (!name2.equals(name)) {
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

        if (numpads > 0) {
            name2 += DASHES.substring(0, numpads);
        }

        super.report(name2, success);

        if (!success) {
            writeFailure();
        }
    }

    @Override
    public void report(String step, AsadminReturn ret) {
        report(step, ret.returnValue);
    }

    protected void startDomain(String domainname) {
        report(getTestName() + "-start-domain" + startstops++,
                asadmin("start-domain", domainname));
    }

    protected void startDomain() {
        report(getTestName() + "-start-def-domain" + startstops++,
                asadmin("start-domain"));
    }

    protected void startDomainDebug() {
        report(getTestName() + "-start-def-domain" + startstops++,
                asadmin("start-domain", "--debug"));
    }

    protected void stopDomain(String domainname) {
        report(getTestName() + "-stop-domain" + startstops++,
                asadmin("stop-domain", domainname));
    }

    protected void stopDomain() {
        report(getTestName() + "-stop-def-domain" + startstops++,
                asadmin("stop-domain"));
    }

    protected final boolean verifyNoClusters() {
        AsadminReturn ret = asadminWithOutput("list-clusters");
        String s = (ret.out == null) ? "" : ret.out.trim();

        s = s.toLowerCase();
        return s.endsWith("list-clusters") || s.indexOf("nothing to list") >= 0;
    }

    protected final boolean verifyNoInstances() {
        AsadminReturn ret = asadminWithOutput("list-instances");
        String s = (ret.out == null) ? "" : ret.out.trim();
        return s.toLowerCase().indexOf("nothing to list") >= 0;
    }

    /*
     * Returns true if String b contains String a.
     */
    protected static boolean matchString(String a, String b) {
        return b.indexOf(a) != -1;
    }

    static String getURL(String urlstr) {
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
            printf("unable to fetch URL:" + urlstr + ", reason: " + ex.getMessage());
            return "";
        }
    }

    static void printf(String fmt, Object... args) {
        if (DEBUG) {
            System.out.printf("**** DEBUG MESSAGE ****  " + fmt + "\n", args);
        }
    }
    private final SortedSet<String> reportNames = new TreeSet<String>();
    private int startstops = 0;
    protected final static boolean DEBUG;
    protected final static boolean isHudson = Boolean.parseBoolean(System.getenv("HUDSON"));

    static {
        if (isHudson) {
            DEBUG = true;
        }
        else if (Boolean.parseBoolean(System.getenv("AS_DEBUG"))) {
            DEBUG = true;
        }
        else {
            DEBUG = false;
        }
    }

    String generateInstanceName() {
        String s = "" + System.currentTimeMillis();
        s = s.substring(4, 10);
        return "in_" + s;
    }

    String generateClusterName() {
        String s = "" + System.currentTimeMillis();
        s = s.substring(4, 10);
        return "cl_" + s;
    }

    String get(String what) {
        // note that the returned string is full of junk -- namely the HUGE asadmin
        // command is prepended to the output.
        // the "get" key will appear TWICE!!!!!!  Once for the echo of the command itself
        // and once for the output of the command.

        AsadminReturn ret = asadminWithOutput("get", what);
        if (!ret.returnValue) {
            return null;
        }

        int index = ret.outAndErr.lastIndexOf(what);
        int len = ret.outAndErr.length();

        if (index < 0 || len - index <= 2) {
            return null;
        }

        // e.g. "asadmin blah foo=xyz  len==20, index==13,  start at index=17
        // which is index+lenofget-string+1
        int valueIndex = index + what.length() + 1;
        return ret.outAndErr.substring(index + what.length() + 1).trim();
    }

    public final boolean doesGetMatch(String what, String match) {
        String ret = get(what);

        if (!ok(match) && !ok(ret)) {
            return true;
        }

        if (!ok(match) || !ok(ret)) {
            return false;
        }

        return (match.equals(ret));
    }

    /**
     * returns true if there is anything in the config that matches. returns
     * false if there is no such thing
     *
     * @param what
     * @return
     */
    public final boolean doesGetMatch(String what) {
        AsadminReturn ret = asadminWithOutput("get", what);

        if (!ret.returnValue) {
            return false;
        }

        if (ret.err != null && ret.err.length() > 1) {
            return false;
        }

        return ret.out != null && ret.out.indexOf("=") >= 0;
    }

    public static void runFakeServerDaemon(int port) {
        Thread t = new Thread(new FakeServer(port), "FakeServerListeningOn: " + port);
        t.setDaemon(true);
        t.start();
    }

    /**
     *
     * typical output as og 6/6/10
     * C:\glassfishv3\glassfish\nodeagents\vaio>asadmin list-instances in_879669
     * not running i20 running
     */
    public boolean isInstanceRunning(String iname) {
        AsadminReturn ret = asadminWithOutput("list-instances");
        String[] lines = ret.out.split("[\r\n]");

        // beware!  The echo'd command can have a pathwith the instance name embedded,
        // innocently in it.  THis happened on a Hudson build which had a directory named
        // makati1 in it!!
        // So now we throw away the echo'd command line if we see it (just in case we turn
        // echoing off later)

        for (String line : lines) {
            if (line.indexOf("list-instances") >= 0) {
                continue;
            }

            if (line.indexOf(iname) >= 0) {
                printf("Line from list-instances = " + line);
                return line.indexOf("  running") >= 0;
            }
        }
        return false;
    }

    public boolean isClusterRunning(String cname) {
        AsadminReturn ret = asadminWithOutput("list-clusters");
        String[] lines = ret.out.split("[\r\n]");

        for (String line : lines) {
            if (line.indexOf(cname) >= 0) {
                printf("Line from list-clusters = " + line);
                return line.indexOf(cname + " running") >= 0;
            }
        }
        return false;
    }

    public boolean isClusterPartiallyRunning(String cname) {
        AsadminReturn ret = asadminWithOutput("list-clusters");
        String[] lines = ret.out.split("[\r\n]");

        for (String line : lines) {
            if (line.indexOf(cname) >= 0) {
                printf("Line from list-clusters = " + line);
                return line.indexOf(cname + " partially running") >= 0;
            }
        }
        return false;
    }

    /*
     * Delete the directories for a local node (since delete-node-* doesn't do
     * this
     */
    public boolean deleteNodeDirectory(String node) {
        return deleteDirectory(new File(new File(getGlassFishHome(), "nodes"), node));
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                }
                else {
                    f.delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * Add a password to the asadmin password file used by the test.
     *
     * @param value Password to use
     * @param passType SSH_PASS if you are setting ssh password. KEY_PASS if you
     * are setting encryption key
     */
    void addPassword(String value, PasswordType passType) {
        BufferedWriter out = null;

        if (value == null) {
            value = "";
        }
        try {
            final File f = new File(pFile);
            out = new BufferedWriter(new FileWriter(f, true));
            out.newLine();
            switch (passType) {
                case SSH_PASS:
                    out.write("AS_ADMIN_SSHPASSWORD=" + value);
                    break;
                case KEY_PASS:
                    out.write("AS_ADMIN_SSHKEYPASSPHRASE=" + value);
                    break;
                case ALIAS_PASS:
                    out.write("AS_ADMIN_ALIASPASSWORD=" + value);
                    break;
                default:
                //do nothing
            }
        }
        catch (IOException ioe) {
            //ignore
        }
        finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            }
            catch (final Exception ignore) {
            }
        }

        return;
    }

    /**
     * Remove SSH related passwords from the asadmin password file id == "SSH"
     * or "DCOM"
     */
    void removePasswords(String id) {
        final File f = new File(pFile);
        final File tempFile = new File(tmpFile);

        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(tempFile));
            reader = new BufferedReader(new FileReader(f));

            String currentLine;

            while ((currentLine = reader.readLine()) != null && !currentLine.trim().isEmpty()) {
                if (currentLine.trim().startsWith("AS_ADMIN_" + id)) {
                    continue;
                }
                writer.write(currentLine);
                writer.newLine();
            }

            reader.close();
            writer.close();

            //On Windows, rename will fail if destination file already exists
            if (!f.delete()) {
                System.out.println("Failed to delete original file");
            }

            if (!tempFile.renameTo(f)) {
                System.out.println("Failed to restore password file.");
            }
        }
        catch (IOException ioe) {
            //ignore
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            }
            catch (final Exception ignore) {
            }
        }

        return;
    }

    final boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    // bnevins - horribly inefficient but we are guaranteeing to see contents when exited abruptly.
    // It can't possibly take all that long for a few thousand calss - right?
    // If a problem it can be tuned easily.
    // this will give a rough idea of the time spent for every report call
    private static void writeTimestamp(String s, boolean b) {
        BufferedWriter out = null;
        long prevTime = lastReportTime;
        lastReportTime = System.currentTimeMillis();

        long elapsed = prevTime == 0 ? 0 : lastReportTime - prevTime;

        try {
            out = new BufferedWriter(new FileWriter(TIMESTAMP_FILE, true));
            out.write("" + elapsed + "ms " + b + "  [" + s + "]");
        }
        catch (IOException e) {
            // It is just a debug file.
        }
        finally {
            if (out != null) {
                try {
                    out.write("\n");
                    out.close();
                }
                catch (Exception e) {
                    // ignore
                }
            }
        }
    }
    private static final File TIMESTAMP_FILE = new File("timestamps.out").getAbsoluteFile();
    private final File GF_HOME = getGlassFishHome();
    private final File DOMAINS_DIR = new File(GF_HOME, "domains");
    private static long lastReportTime = 0;
    private static final int MAX_LENGTH = 101;
    private static final String DASHES =
            "------------------------------------------------------------------------------------------------------------------------------";
}
