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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Main class used for Uniform reporting of results
 *
 * @author Ramesh.Mandava
 * @author Justin.Lee@sun.com
 */
@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "StaticNonFinalField"})
public class Reporter implements Serializable {
    private static Reporter reporterInstance = null;
    private String resultFile = "default.xml";
    transient public PrintWriter out = new PrintWriter(System.out);
    private List<TestSuite> suites = new ArrayList<TestSuite>();

    public String getResultFile() {
        return resultFile;
    }

    public void setTestSuite(TestSuite suite) {
        if (suite != null) {
            suites.add(suite);
        }
    }

    public static Reporter getInstance(String home) {
        if (reporterInstance == null) {
            String path = new File(".").getAbsolutePath();
            String outputDir = path.substring(0, path.indexOf(home)) + home;
            reporterInstance = new Reporter(outputDir + "/test_results.xml");
        }
        return reporterInstance;
    }

    private Reporter(String resultFilePath) {
        try {
            resultFile = resultFilePath;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    flushAll();
                }
            }));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void flushAll() {
        try {
            if ("default.xml".equals(resultFile)) {
                InputStream in = Runtime.getRuntime().exec("uname -n").getInputStream();
                byte[] bytes = new byte[200];
                in.read(bytes);
                String file = "result_";
                String machineName = new String(bytes).trim();
                file += machineName;
                Calendar cal = Calendar.getInstance();
                String month = Integer.toString(cal.get(Calendar.MONTH));
                String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                String year = Integer.toString(cal.get(Calendar.YEAR));
                file += "_" + month + day + year + ".xml";
                resultFile = file;
            }
            FileOutputStream output = new FileOutputStream(resultFile, true);
            Iterator<TestSuite> it = suites.iterator();
            while (it.hasNext()) {
                if (flush(it.next(), output)) {
                    it.remove();
                }
            }
            output.close();
            suites.clear();
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
        }
    }

    /**
     * This method prepares and output an XML representation of the Reporter class' content for the given testSuite.
     *
     * @param suite the test suite
     * @param output the FileOutputStream in which we need to write.
     *
     * @return returns true if the file is successfully created
     */
    public boolean flush(TestSuite suite, FileOutputStream output) {
        try {
            if (suite != null && !suite.getWritten()) {
                suite.setWritten(writeXMLFile(suite.toXml(), output));
                return suite.getWritten();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean writeXMLFile(String xmlStringBuffer, FileOutputStream out) {
        try {
            out.write(xmlStringBuffer.getBytes());
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
