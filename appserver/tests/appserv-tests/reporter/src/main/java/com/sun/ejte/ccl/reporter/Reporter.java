/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.ejte.ccl.reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Main class used for Uniform reporting of results
 *
 * @author : Ramesh Mandava
 */
public class Reporter extends Thread implements Serializable {

    private static final String MACHINE_NAME = getMachineName();

    private static Reporter reporterInstance;

    private String resultFile = "default.xml";

    private static String ws_home="sqe-pe";
    /** testSuiteHash is a Hashtable holding info about different testsuites. */
    private static Hashtable<String, TestSuite> testSuiteHash = new Hashtable<>();

    private static final String getMachineName() {
        try (InputStream in = Runtime.getRuntime().exec("uname -n").getInputStream()) {
            return new String(in.readAllBytes()).strip();
        } catch (Exception me) {
            return "unavailable";
        }
    }


    // To be used as a shutdown hook
    @Override
    public void run(){
        //System.out.println("REPORTER\t Inside run");
        generateValidReport();
    }

    /**
     * This method is used for setting the TestSuite Info
     */
    public void setTestSuite(String testSuiteId, String name, String description) {
        String id = testSuiteId.strip();
        TestSuite myTestSuite = findTestSuite(id);
        if (myTestSuite == null) {
            myTestSuite = new TestSuite(testSuiteId, name, description);
            putTestSuite(id, myTestSuite);
        }
    }


    /**
     * This method is used for setting the TestSuite Info
     */
    public void setTestSuite(String id, String name) {
        TestSuite myTestSuite = findTestSuite(id);
        if (myTestSuite == null) {
            myTestSuite = new TestSuite(id, name);
            putTestSuite(id, myTestSuite);
        }
    }


    /**
     * This method is used for setting the TestSuite Info
     */
    public void setTestSuite(String id) {
        System.err.println("setTestSuite with id -> " + id);
        TestSuite myTestSuite = findTestSuite(id);
        if (myTestSuite == null) {
            myTestSuite = new TestSuite(id);
            putTestSuite(id, myTestSuite);
        }
    }


    /**
     * After setting Test Suite info. suing setTestSuite, We need to use this addTest method
     * for adding information about particular Test. We need to pass both TestSuiteId and
     * TestId along with othe info of Test
     */
    public void addTest(String testSuiteId, String testId, String testName, String testDescription) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err
                .println("ERROR:addTest might have called without setTestSuite. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id -> " + testSuiteId);
            return;
        }

        Test myTest = new Test(testId, testName, testDescription);
        myTestSuite.addTest(myTest);
    }


    /**
     * After setting Test Suite info. suing setTestSuite, We need to use this addTest method
     * for adding information about particular Test. We need to pass both TestSuiteId and
     * TestId along with othe info of Test
     */
    public void addTest(String testSuiteId, String testId, String testName) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err
                .println("ERROR:addTest might have called without setTestSuite. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id -> " + testSuiteId);
            return;
        }

        Test myTest = new Test(testId, testName);
        myTestSuite.addTest(myTest);
    }


    /**
     * After using setthing Test Suite info. We need to use this addTest method
     * for adding information about particular Test. We need to pass both TestSuiteId and
     * TestId
     */
    public void addTest(String testSuiteId, String testId) {
        System.out.println("addTest with testSuiteId:: testId -> " + testSuiteId + "::" + testId);
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err
                .println("ERROR:addTest might have called without setTestSuite. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id -> " + testSuiteId);
            return;
        }

        Test myTest = new Test(testId);
        myTestSuite.addTest(myTest);
    }


    /**
     * After adding a Test using addTest , We need to use this setTestStatus method
     * for setting Test status(pass/fail) information about particular Test. We need to pass both
     * TestSuiteId and TestId along with expected and actual result. This is optional as in some
     * case only TestCases will have status
     */
    public void setTestStatus(String testSuiteId, String testId, String status, String expected, String actual) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);

        if (myTestSuite == null) {
            System.err.println(
                "ERROR:setTestStatus might have called without setTestSuite. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id -> " + testSuiteId);
            return;
        }

        Test myTest = myTestSuite.findTest(testId);
        if (myTest == null) {
            System.err
                .println("ERROR:setTestStatus might have called without addTest. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId + "::" + testId);
            return;
        }
        myTest.setStatus(status);
        myTest.setExpected(expected);
        myTest.setActual(actual);

    }


    /**
     * After adding a Test using addTest , We need to use this setTestStatus method
     * for setting Test status(pass/fail) information about particular Test. We need to pass both
     * TestSuiteId and TestId along with status information. This is optional as in some case
     * only TestCases will have status
     */
    public void setTestStatus(String testSuiteId, String testId, String status, String statusDescription) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);

        if (myTestSuite == null) {
            System.err.println(
                "ERROR:setTestStatus might have called without setTestSuite. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id -> " + testSuiteId);
            return;
        }

        Test myTest = myTestSuite.findTest(testId);
        if (myTest == null) {
            System.err
                .println("ERROR:setTestStatus might have called without addTest. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId + "::" + testId);
            return;
        }
        myTest.setStatus(status);
        myTest.setStatusDescription(statusDescription);

    }


    /**
        After adding a Test using addTest , We need to use this setTestStatus method
      for setting Test status(pass/fail) information about particular Test. We need to  pass both TestSuiteId and TestId  along with status information. This is optional as in some case
     only TestCases will have status
     */
    public void setTestStatus(String testSuiteId, String testId, String status) {
        System.out
            .println("setTestStatus testSuiteId::testId::status -> " + testSuiteId + "::" + testId + "::" + status);
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err.println(
                "ERROR:setTestStatus might have called without setTestSuite. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id -> " + testSuiteId);
            return;
        }

        Test myTest = myTestSuite.findTest(testId);
        if (myTest == null) {
            System.err
                .println("ERROR:setTestStatus might have called without addTest. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId + "::" + testId);
            return;
        }
        myTest.setStatus(status);
    }


    /**
     * After adding a Test using addTest, We need to use this addTestCase method
     * for adding information about particular TestCase corresponding to that Test. We need to pass
     * TestSuiteId, TestId and TestCaseId along with othe info of TestCase
     */
    public void addTestCase(String testSuiteId, String testId, String testCaseId) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err.println(
                "ERROR:addTestCase might have called without setTestSuite. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id -> " + testSuiteId);
            return;
        }
        Test myTest = myTestSuite.findTest(testId);
        if (myTest == null) {
            System.err
                .println("ERROR:addTestCase might have called without addTest. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId + "::" + testId);
            return;
        }

        TestCase myTestCase = new TestCase(testCaseId);
        myTest.addTestCase(myTestCase);
    }


    /**
     * After adding a Test using addTest, We need to use this addTestCase method
     * for adding information about particular TestCase corresponding to that Test. We need to pass
     * TestSuiteId, TestId and TestCaseId along with othe info of TestCase
     */
    public void addTestCase(String testSuiteId, String testId, String testCaseId, String testCaseName) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err.println(
                "ERROR:addTestCase might have called without setTestSuite. PENDING : Shall we throw Exception?");
            return;
        }
        Test myTest = myTestSuite.findTest(testId);
        if (myTest == null) {
            System.err
                .println("ERROR:addTestCase might have called without addTest. PENDING : Shall we throw Exception?");
            System.err.println("Given TestSuite Id::Test Id -> " + testSuiteId + "::" + testId);
            return;
        }

        TestCase myTestCase = new TestCase(testCaseId, testCaseName);
        myTest.addTestCase(myTestCase);
    }


    /**
     * After adding a Test using addTest, We need to use this addTestCase method
     * for adding information about particular TestCase corresponding to that Test. We need to pass
     * TestSuiteId, TestId and TestCaseId along with othe info of TestCase
     */
    public void addTestCase(String testSuiteId, String testId, String testCaseId, String testCaseName,
        String testCaseDescription) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err.println(
                "ERROR:addTestCase might have called without setTestSuite. PENDING : Shall we throw Exception?");
            return;
        }
        Test myTest = myTestSuite.findTest(testId);
        if (myTest == null) {
            System.err
                .println("ERROR:addTestCase might have called without addTest. PENDING : Shall we throw Exception?");
            return;
        }

        TestCase myTestCase = new TestCase(testCaseId, testCaseName, testCaseDescription);
        myTest.addTestCase(myTestCase);
    }


    /**
     * After adding a TestCase using addTestCase , We need to use this setTestCaseStatus method
     * for setting TestCase status(pass/fail) information about particular TestCase. We need to pass
     * TestSuiteId, TestId and TestCaseId along with status information. We pass expected and actual
     * information along with pass/fail here
     */
    public void setTestCaseStatus(String testSuiteId, String testId, String testCaseId, String status, String expected,
        String actual) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err.println(
                "ERROR:setTestCaseStatus might have called without setTestSuite. PENDING : Shall we throwException?");
            return;
        }

        Test myTest = myTestSuite.findTest(testId);
        if (myTest == null) {
            System.err.println(
                "ERROR:setTestCaseStatus might have called without addTest. PENDING : Shall we throw Exception?");
            return;
        }
        TestCase myTestCase = myTest.findTestCase(testCaseId);
        if (myTestCase == null) {
            System.err.println(
                "ERROR:setTestCaseStatus might have called without addTestCase. PENDING : Shall we throwException?");
            return;
        }
        myTestCase.setStatus(status);
        myTestCase.setExpected(expected);
        myTestCase.setActual(actual);
    }


    /**
     * After adding a TestCase using addTestCase , We need to use this setTestCaseStatus method
     * for setting TestCase status(pass/fail) information about particular TestCase. We need to pass
     * TestSuiteId, TestId and TestCaseId along with status information.
     * Each TestCase will have status
     */
    public void setTestCaseStatus(String testSuiteId, String testId, String testCaseId, String status,
        String statusDescription) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err.println(
                "ERROR:setTestCaseStatus might have called without setTestSuite. PENDING : Shall we throw Exception?");
            return;
        }

        Test myTest = myTestSuite.findTest(testId);
        if (myTest == null) {
            System.err.println(
                "ERROR:setTestCaseStatus might have called without addTest. PENDING : Shall we throw Exception?");
            return;
        }
        TestCase myTestCase = myTest.findTestCase(testCaseId);
        if (myTestCase == null) {
            System.err.println(
                "ERROR:setTestCaseStatus might have called without addTestCase. PENDING : Shall we throw Exception?");
            return;
        }
        myTestCase.setStatus(status);
        myTestCase.setStatusDescription(statusDescription);

    }


    /**
        After adding a TestCase using addTestCase , We need to use this setTestCaseStatus method
      for setting TestCase status(pass/fail) information about particular TestCase. We need to  pass TestSuiteId, TestId and TestCaseId  along with status information.
     only TestCases will have status
     */
    public void setTestCaseStatus(String testSuiteId, String testId, String testCaseId, String status) {
        TestSuite myTestSuite = findTestSuite(testSuiteId);
        if (myTestSuite == null) {
            System.err.println("ERROR:setTestCaseStatus might have called without setTestSuite. TestSuiteID="
                + testSuiteId + ", TestID=" + testId + ". PENDING : Shall we throw Exception?");
            return;
        }

        Test myTest = myTestSuite.findTest(testId);
        if (myTest == null) {
            System.err.println(
                "ERROR:setTestCaseStatus might have called without addTest. TestSuiteID=" + testSuiteId + ", TestID="
                    + testId + ". PENDING : Shall we throw Exception?");
            return;
        }
        TestCase myTestCase = myTest.findTestCase(testCaseId);
        if (myTestCase == null) {
            System.err.println("ERROR:setTestCaseStatus might have called without addTestCase. TestSuiteID="
                + testSuiteId + ", TestID=" + testId + ". PENDING : Shall we throw Exception?");
            return;
        }
        myTestCase.setStatus(status);
    }


    /**
     * Change done on 7/10/02 by Deepa Singh
     * Now Reporter will by default create results file in $EJTE_HOME if no results file is
     * specified
     * So no need to pass the environment variable.
     * Reporter.getInstance should create test_results.xml at j2ee-test/
     */
    public static Reporter getInstance() {
        if (reporterInstance == null) {
            // reporterInstance = new Reporter( );
            String rootpath = new File(".").getAbsolutePath();
            String ejte_home = rootpath.substring(0, rootpath.indexOf(ws_home));
            // ejte_home contains OS dependent path separator character without j2ee-test
            String outputDir = ejte_home + ws_home;
            reporterInstance = Reporter.getInstance(outputDir + File.separatorChar + "test_results.xml");
        }
        return reporterInstance;
    }


    public static Reporter getInstance(String wshome) {
        if (reporterInstance == null) {
            String rootpath = (new File(".")).getAbsolutePath();
            String ejte_home = rootpath.substring(0, rootpath.indexOf(wshome));
            String outputDir = ejte_home + wshome;
            reporterInstance = new Reporter(outputDir + File.separatorChar + "test_results.xml");
        }
        return reporterInstance;
    }


    public static Reporter getInstance(String resultFilePath, boolean pathSpecified) {
        if (reporterInstance == null) {
            reporterInstance = new Reporter(resultFilePath);

        }
        return reporterInstance;
    }

    private Reporter() {
        Runtime.getRuntime().addShutdownHook(this);
    }


    private Reporter(String resultFilePath) {
        resultFile = resultFilePath;
        Runtime.getRuntime().addShutdownHook(this);
    }


    public void generateValidReport() {
        // Now flush all the TestSuite info
        flushAll();

        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String currentDate = (new Date()).toString();

        String extraXML = "<report><date> " + currentDate + "</date><configuration>";
        extraXML += "<os>" + osName + osVersion + "</os>";
        extraXML += "<jdkVersion>" + Runtime.version() + "</jdkVersion>";

        extraXML += "<machineName>" + MACHINE_NAME + "</machineName>";
        extraXML += "</configuration> <testsuites>";

        final String oFileName;
        int dotLastIndex = resultFile.lastIndexOf(".");
        if (dotLastIndex > 0) {
            oFileName = resultFile.substring(0, dotLastIndex) + "Valid.xml";
        } else {
            oFileName = resultFile + "Valid.xml";
        }
        try (FileOutputStream outputStream = new FileOutputStream(oFileName);
            FileInputStream inputStream = new FileInputStream(resultFile)) {
            FileChannel rChannel = inputStream.getChannel();
            FileChannel wChannel = outputStream.getChannel();
            wChannel.write(ByteBuffer.wrap(extraXML.getBytes()));
            wChannel.transferFrom(rChannel, wChannel.position(), rChannel.size());
            wChannel.position(wChannel.position() + rChannel.size());
            wChannel.write(ByteBuffer.wrap("</testsuites>\n</report>\n".getBytes()));
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
        }
    }


    public void flushAll() {
        if (resultFile.equals("default.xml")) {
            resultFile = "result_" + MACHINE_NAME + "_" + LocalDate.now() + ".xml";
        }
        try (FileOutputStream foutput = new FileOutputStream(resultFile, true)) {
            Enumeration<String> testSuiteEnum = testSuiteHash.keys();

            while (testSuiteEnum.hasMoreElements()) {
                String testSuiteId = testSuiteEnum.nextElement();
                flush(testSuiteId, foutput);
            }

            System.out.println("in flushAll , creating new testSuiteHash");
            // Now take out the TestSuite info from memory
            testSuiteHash = new Hashtable<>();
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
        }
    }


    /**
     * This method prepares and output an XML representation of the Reporter class' content for the
     * given testSuite.
     *
     * @param testSuiteId the test suite's name.
     * @return returns true if the file is succesfully created
     */
    public boolean flush(String testSuiteId) {
        if (resultFile.equals("default.xml")) {
            resultFile = "result_" + MACHINE_NAME + "_" + LocalDate.now() + ".xml";
        }
        try (FileOutputStream foutput = new FileOutputStream(resultFile, true)) {
            return flush(testSuiteId, foutput);
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            return false;
        }
    }


    /**
     * This method prepares and output an XML representation of the Reporter class' content for the
     * given testSuite.
     *
     * @param testSuiteId the test suite's name.
     * @param foutput the FileOutputStream in which we need to write.
     * @return returns true if the file is succesfully created
     */
    public boolean flush(String testSuiteId, FileOutputStream foutput) {
        // System.out.println("REPORTER\t flush(testsuiteId,fout)");
        try {
            StringBuilder xmlRepresentation = new StringBuilder();

            /*
             * xmlRepresentation.append("<?xml version=\"1.0\"?>\n");
             * xmlRepresentation.append("<!DOCTYPE testsuite SYSTEM \"test_suite.dtd\">\n");
             * xmlRepresentation.
             * append("<!-- ID are defined as: test suite: test case : local id-->\n");
             */

            TestSuite myTestSuite = findTestSuite(testSuiteId);
            if (myTestSuite == null) {
                System.err.println("ERROR: Information for TestSuite Id : " + testSuiteId + " doesn't exist");
                return false;
            }
            String testSuiteName = myTestSuite.getName();
            String testSuiteDescription = myTestSuite.getDescription();

            xmlRepresentation.append("<testsuite>\n");
            xmlRepresentation.append("  <id>" + testSuiteId + "</id>\n");

            if (!testSuiteName.equals(ReporterConstants.NA)) {
                xmlRepresentation.append("<name>" + testSuiteName + "</name>\n");
            }
            if (!testSuiteDescription.equals(ReporterConstants.NA)) {
                xmlRepresentation.append("<description><![CDATA[" + testSuiteDescription + "]]></description>\n");
            }

            Vector<String> testIdVector = myTestSuite.getTestIdVector();
            xmlRepresentation.append("<tests>\n");
            for (int ti = 0; ti < testIdVector.size(); ti++) {

                String testId = testIdVector.elementAt(ti);
                Test myTest = myTestSuite.findTest(testId);
                String testName = myTest.getName();
                String testDescription = myTest.getDescription();
                String testStatus = myTest.getStatus();
                String testStatusDescription = myTest.getStatusDescription();

                String testExpected = myTest.getExpected();
                String testActual = myTest.getActual();

                xmlRepresentation.append("<test>\n");
                xmlRepresentation.append("<id>" + testId + "</id>\n");

                if (!testName.equals(ReporterConstants.NA)) {
                    xmlRepresentation.append("<name>" + testName + "</name>\n");
                }

                if (!testDescription.equals(ReporterConstants.NA)) {
                    xmlRepresentation.append("<description><![CDATA[" + testDescription + "]]></description>\n");
                }
                if (!testStatus.equals(ReporterConstants.OPTIONAL)) {
                    if (!testStatusDescription.equals(ReporterConstants.OPTIONAL)) {
                        xmlRepresentation.append("<status value=\"" + testStatus + "\"><![CDATA["
                            + testStatusDescription + "]]></status>\n");
                    } else if (testExpected != null && testActual != null) {
                        xmlRepresentation
                            .append("<status value=\"" + testStatus + "\"> <expected><![CDATA[" + testExpected
                                + "]]></expected><actual><![CDATA[" + testActual + "]]></actual></status>\n");
                    } else {
                        xmlRepresentation.append("<status value=\"" + testStatus + "\"></status>\n");
                    }
                }

                Vector<String> testCaseIdVector = myTest.getTestCaseIdVector();

                /*
                 * if ( testCaseIdVector.size( ) < 1 )
                 * {
                 * // This means there are no test cases and Test has the status info
                 * xmlRepresentation.append("</test>\n");
                 * }
                 */
                if (testCaseIdVector.size() >= 1) {
                    xmlRepresentation.append("<testcases>\n");

                    for (int tc = 0; tc < testCaseIdVector.size(); tc++) {
                        String testCaseId = testCaseIdVector.elementAt(tc);
                        TestCase myTestCase = myTest.findTestCase(testCaseId);

                        String testCaseName = myTestCase.getName();
                        String testCaseDescription = myTestCase.getDescription();
                        String testCaseStatus = myTestCase.getStatus();
                        String testCaseStatusDescription = myTestCase.getStatusDescription();
                        String testCaseExpected = myTestCase.getExpected();
                        String testCaseActual = myTestCase.getActual();

                        xmlRepresentation.append("<testcase>\n");
                        xmlRepresentation.append("<id>" + testCaseId + "</id>\n");
                        if (!testCaseName.equals(ReporterConstants.NA)) {
                            xmlRepresentation.append("<name>" + testCaseName + "</name>\n");
                        }

                        if (!testCaseDescription.equals(ReporterConstants.NA)) {
                            xmlRepresentation
                                .append("<description><![CDATA[" + testCaseDescription + "]]></description>\n");
                        }
                        if (!testCaseStatusDescription.equals(ReporterConstants.NA)) {
                            xmlRepresentation.append("<status value=\"" + testCaseStatus + "\"><![CDATA["
                                + testCaseStatusDescription + "]]></status>\n");
                        } else if ((testCaseExpected != null) && (testCaseActual != null)) {
                            xmlRepresentation.append(
                                "<status value=\"" + testCaseStatus + "\"> <expected><![CDATA[" + testCaseExpected
                                    + "]]></expected><actual><![CDATA[" + testCaseActual + "]]></actual></status>\n");
                        } else {
                            xmlRepresentation.append("<status value=\"" + testCaseStatus + "\"></status>\n");
                        }
                        xmlRepresentation.append("</testcase>\n");
                    }

                    xmlRepresentation.append("</testcases>\n");

                }

                xmlRepresentation.append("</test>\n");
            }

            xmlRepresentation.append("</tests>\n");
            xmlRepresentation.append("</testsuite>\n");
            boolean writeResult = writeXMLFile(xmlRepresentation, foutput);
            // Now remove TestSuite from Hashtable: PENDING
            if (writeResult == true) {
                // If we could write the content properly then remove the TestSuite from Hashtable
                removeTestSuite(testSuiteId);
            }
            return writeResult;
        } catch (java.lang.Exception ex) {
            return false;
        }
    }

    private boolean writeXMLFile(StringBuilder xmlStringBuffer, FileOutputStream  fout){
        try{
            fout.write( xmlStringBuffer.toString().getBytes() );
            fout.flush( );
        } catch(java.io.IOException ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }



    private TestSuite findTestSuite(String testSuiteId) {
        return testSuiteHash.get(testSuiteId.strip());
    }

    private TestSuite removeTestSuite(String testSuiteId) {
        return testSuiteHash.remove(testSuiteId.strip());
    }

    private TestSuite putTestSuite(String testSuiteId, TestSuite testSuite) {
        return testSuiteHash.put(testSuiteId.strip(), testSuite);
    }
}
