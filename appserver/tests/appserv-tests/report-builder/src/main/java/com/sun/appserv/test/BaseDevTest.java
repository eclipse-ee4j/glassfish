/*
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

package com.sun.appserv.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.sun.appserv.test.util.process.ProcessManager;
import com.sun.appserv.test.util.process.ProcessManagerException;
import com.sun.appserv.test.util.process.ProcessManagerTimeoutException;
import com.sun.appserv.test.util.results.SimpleReporterAdapter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class BaseDevTest {
    public final SimpleReporterAdapter stat;

    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
    public BaseDevTest() {
        stat = new SimpleReporterAdapter("appserv-tests", getTestName());
        stat.addDescription(getTestDescription());
    }

    protected abstract String getTestName();

    protected abstract String getTestDescription();

    public void report(String step, boolean success) {
        stat.addStatus(step, success ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
    }

    public void report(String step, AsadminReturn ret) {
        stat.addStatus(step, ret.returnValue ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
    }

    /**
     * Runs the command with the args given
     *
     * @param args
     *
     * @return true if successful
     */
    public boolean asadmin(int timeout, final String... args) {
        asadminWithOutput(timeout, args);
        return lastAsadminReturn.returnValue;
    }

    public boolean asadmin(final String... args) {
        return asadmin(DEFAULT_TIMEOUT_MSEC, args);
    }

    /**
     * Runs the command with the args given Returns the precious output strings
     * for further processing.
     *
     * @param args
     *
     * @return true if successful
     */
    public AsadminReturn asadminWithOutput(final String... args) {
        return asadminWithOutput(DEFAULT_TIMEOUT_MSEC, args);
    }

    public AsadminReturn asadminWithOutput(final int timeout, final String... args) {
        AsadminReturn ret = new AsadminReturn();
        String cmd = isWindows() ? "/bin/asadmin.bat" : "/bin/asadmin";
        List<String> command = new ArrayList<String>();
        command.add(System.getenv().get("S1AS_HOME") + cmd);
        command.addAll(Arrays.asList(antProp("as.props").split(" ")));
        command.addAll(Arrays.asList(args));

        ProcessManager pm = new ProcessManager(command);

        // the tests may be running unattended -- don't wait forever!
        pm.setTimeoutMsec(timeout);

        pm.setEcho(false);
        int exit;
        String myErr = "";
        try {
            exit = pm.execute();
        }
        catch (ProcessManagerTimeoutException tex) {
            myErr = "\nProcessManagerTimeoutException: command timed out after " + timeout + " ms.";
            exit = 1;
        }
        catch (ProcessManagerException ex) {
            exit = 1;
        }

        ret.out = pm.getStdout();
        ret.err = pm.getStderr() + myErr;
        ret.outAndErr = ret.out + ret.err;
        ret.returnValue = exit == 0 && validResults(ret.out,
                String.format("Command %s failed.", args[0]), "list-commands");
        lastAsadminReturn = ret;
        write(lastAsadminReturn.out);
        write(lastAsadminReturn.err);

        return ret;
    }

    protected boolean validResults(String text, String... invalidResults) {
        for (String result : invalidResults) {
            if (text.contains(result)) {
                return false;
            }
        }
        return true;
    }

    public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public String antProp(final String key) {
        String value = System.getProperty(key);
        if (value == null) {
            try {
                Properties props = new Properties();
                String apsHome = System.getenv("APS_HOME");
                FileReader reader = new FileReader(new File(apsHome, "config.properties"));
                try {
                    props.load(reader);
                }
                finally {
                    reader.close();

                }
                System.getProperties().putAll(props);
                System.setProperty("as.props", String.format("--user %s --passwordfile %s --host %s --port %s"
                        + " --echo=true --terse=true", antProp("admin.user"), antProp("admin.password.file"),
                        antProp("admin.host"), antProp("admin.port")));
                value = System.getProperty(key);
                int index;
                while ((index = value.indexOf("${env.")) != -1) {
                    int end = value.indexOf("}", index);
                    String var = value.substring(index, end + 1);
                    final String name = var.substring(6, var.length() - 1);
                    value = value.replace(var, System.getenv(name));
                    System.setProperty(key, value);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return value;
    }

    protected final void write() {
        if (verbose) {
            write(lastAsadminReturn.out);
            write(lastAsadminReturn.err);
        }
    }

    public void write(final String text) {
        if (verbose)
            System.out.println(text);
    }

    protected void writeFailure() {
        System.out.println(FAILURE_START);

        if (lastAsadminReturn != null) {
            System.out.println(lastAsadminReturn.out);
            System.out.println(lastAsadminReturn.err);
        }
    }

    /**
     * Evaluates the Xpath expression
     *
     * @param expr The expression to evaluate
     * @param f The file to parse
     * @param ret The return type of the expression can be
     *
     * XPathConstants.NODESET XPathConstants.BOOLEAN XPathConstants.NUMBER
     * XPathConstants.STRING XPathConstants.NODE
     *
     * @return the object after evaluation can be of type number maps to a
     * java.lang.Double string maps to a java.lang.String boolean maps to a
     * java.lang.Boolean node-set maps to an org.w3c.dom.NodeList
     *
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Object evalXPath(String expr, File f, QName ret) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(f);
            write("Parsing" + f.getAbsolutePath());
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression xexpr = xpath.compile(expr);
            Object result = xexpr.evaluate(doc, ret);
            write("Evaluating" + f.getAbsolutePath());
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }


    }

    /**
     * Evaluates the Xpath expression by parsing the DAS's domain.xml
     *
     * @param expr The Xpath expression to evaluate
     *
     * @return the object after evaluation can be of type number maps to a
     * java.lang.Double string maps to a java.lang.String boolean maps to a
     * java.lang.Boolean node-set maps to an org.w3c.dom.NodeList
     *
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Object evalXPath(String expr, QName ret) {
        return evalXPath(expr, getDASDomainXML(), ret);

    }

    /**
     * Gets the domains folder for DAS
     *
     * @return GF_HOME/domains/domain1
     */
    public File getDASDomainDir() {
        return new File(new File(getGlassFishHome(), "domains"), "domain1");
    }

    /**
     * Gets the domain.xml for DAS
     *
     * @return GF_HOME/domains/domain1/config/domain.xml
     */
    public File getDASDomainXML() {
        return new File(new File(getDASDomainDir(), "config"), "domain.xml");
    }

    /**
     * Get the Glassfish home from the environment variable S1AS_HOME
     *
     * @return
     */
    public File getGlassFishHome() {
        String home = System.getenv("S1AS_HOME");
        if (home == null) {
            throw new IllegalStateException("No S1AS_HOME set!");
        }
        File glassFishHome = new File(home);
        try {
            glassFishHome = glassFishHome.getCanonicalFile();
        }
        catch (Exception e) {
            glassFishHome = glassFishHome.getAbsoluteFile();
        }
        if (!glassFishHome.isDirectory()) {
            throw new IllegalStateException("S1AS_HOME is not pointing at a real directory!");
        }
        return glassFishHome;

    }

    /**
     * Implementations can override this method to do the cleanup for eg
     * deleting instances, deleting clusters etc
     */
    public void cleanup() {
    }

    protected final boolean isVerbose() {
        return verbose;
    }

    protected final void setVerbose(boolean b) {
        verbose = b;
    }

    protected final AsadminReturn getLastAsadminReturn() {
        return lastAsadminReturn;
    }

    // simple C-struct -- DIY
    public static class AsadminReturn {
        public boolean returnValue;
        public String out;
        public String err;
        public String outAndErr;

        @Override
        public final String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("AsadminReturn Object contains:").append('\n');
            sb.append("returnValue = ").append(returnValue).append('\n');
            sb.append("output stream = ").append(out).append('\n');
            sb.append("error stream = ").append(err).append('\n');
            return sb.toString();
        }
    }
    private static final int DEFAULT_TIMEOUT_MSEC = 480000; // 8 minutes
    private boolean verbose = true;
    // in case the command fails so that it can be printed (Hack bnevins)
    private AsadminReturn lastAsadminReturn;
    private static final String FAILURE_START = "#########    FAILURE   #########";
}
