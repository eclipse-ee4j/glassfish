/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl.algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.admin.servermgmt.stringsubs.SubstitutionAlgorithm;
import com.sun.enterprise.admin.servermgmt.stringsubs.impl.LargeFileSubstitutionHandler;
import com.sun.enterprise.admin.servermgmt.stringsubs.impl.SmallFileSubstitutionHandler;

/**
 * Abstract class to test substitution algorithm. Derived classes will
 * provide the implementation of {@link SubstitutionAlgorithm} use to
 * execute the test cases, by defining the abstract method
 * {@link AbstractSubstitutionAlgo#getAlgorithm(Map)}
 */
public abstract class AbstractSubstitutionAlgo
{
    private String _testFileName = "testStringSubs.txt";
    private File _testFile;
    private SubstitutionAlgorithm _algorithm;

    /**
     * Create test file used as a input file for string substitution.
     */
    @BeforeClass
    public void init() {
        Map<String, String> substitutionMap = new HashMap<String, String>();
        substitutionMap.put("line", "replacedLine");
        substitutionMap.put("file", "testFile");
        substitutionMap.put("HTTP_PORT", "8080");
        substitutionMap.put("HTTPS_PORT", "8443");
        _algorithm = getAlgorithm(substitutionMap);
    }

    /**
     * Gets the substitution algorithm.
     *
     * @return Algorithm to perform substitution.
     */
    protected abstract SubstitutionAlgorithm getAlgorithm(Map<String, String> substitutionMap);

    /**
     * Test the {@link SubstitutionAlgorithm} instance for null map.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSubstitutionForNullMap() {
        getAlgorithm(null);
    }

    /**
     * Test the {@link SubstitutionAlgorithm} instance for empty map.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSubstitutionForEmptyMap() {
        getAlgorithm(new HashMap<String, String>());
    }

    /**
     * Test substitution for small text file.
     */
    @Test
    public void testSmallTextFileSubstitution() {
        createTextFile();
        Substitutable resolver = null;
        try {
            resolver = new SmallFileSubstitutionHandler(_testFile);
            _algorithm.substitute(resolver);
            resolver.finish();
        } catch (Exception e) {
            Assert.fail("Test case execution failed", e);
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(_testFile)));
            String afterSubstitutionLine = null;
            int i = 0;
            while ((afterSubstitutionLine = reader.readLine()) != null) {
                switch (i++)
                {
                    case 0:
                        Assert.assertEquals(afterSubstitutionLine, "First replacedLine in testFile repeat First replacedLine in testFile");
                        break;
                    case 1:
                        Assert.assertEquals(afterSubstitutionLine, "Second replacedLine in testFile");
                        break;
                    default:
                        break;
                }
            }
            reader.close();
        } catch (IOException e) {
            Assert.fail("Not able to read test file");
        } finally {
            _testFile.delete();
        }
    }

    /**
     * Test substitution for small XML file.
     */
    @Test
    public void testSmallXMLFileSubstitution() {
        String fileName = _testFileName.replace(".txt", ".xml");
        createXMLFile(fileName);
        Substitutable resolver = null;
        try {
            resolver = new SmallFileSubstitutionHandler(new File(fileName));
            _algorithm.substitute(resolver);
            resolver.finish();
        } catch (Exception e) {
            Assert.fail("Test case failed", e);
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(_testFile)));
            String afterSubstitutionLine = null;
            int i = 0;
            while ((afterSubstitutionLine = reader.readLine()) != null) {
                switch (i++)
                {
                    case 1:
                        Assert.assertEquals(afterSubstitutionLine,
                                "<port name=\"http\" value=\"8080\"></port>");
                        break;
                    case 2:
                        Assert.assertEquals(afterSubstitutionLine,
                                "<port name=\"https\" value=\"8443\"></port>");
                        break;
                    default:
                        break;
                }
            }
            reader.close();
        } catch (IOException e) {
            Assert.fail("Not able to read test file.", e);
        } finally {
            _testFile.delete();
        }
    }

    /**
     * Test substitution for large text file.
     */
    //@Test
    //TODO: Test case failing on hudson, Test case execution create temporary file
    // to perform substitution.
    public void testLargeTextFileSubstitution() {
        createTextFile();
        Substitutable resolver = null;
        try {
            resolver = new LargeFileSubstitutionHandler(_testFile);
            _algorithm.substitute(resolver);
            resolver.finish();
        } catch (Exception e) {
            Assert.fail("Test case failed : " + e.getMessage());
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(_testFileName))));
        } catch (FileNotFoundException e) {
            Assert.fail("Not able to locate test file : " + _testFileName, e);
        }
        String afterSubstitutionLine = null;
        try {
            int i = 0;
            while ((afterSubstitutionLine = reader.readLine()) != null) {
                switch (i++)
                {
                    case 0:
                        Assert.assertEquals(afterSubstitutionLine,
                                "First replacedLine in testFile repeat First replacedLine in testFile");
                        break;
                    case 1:
                        Assert.assertEquals(afterSubstitutionLine,
                                "Second replacedLine in testFile");
                        break;
                    default:
                        break;
                }
            }
            reader.close();
        } catch (IOException e) {
            Assert.fail("Not able to read test file");
        } finally {
            _testFile.delete();
        }
    }

    /**
     * Test substitution for large XML file.
     */
    //@Test
    //TODO: Test case failing on hudson, Test case execution create temporary file
    // to perform substitution.
    public void testLargeXMLFileSubstitution() {
        String fileName = _testFileName.replace(".txt", ".xml");
        createXMLFile(fileName);
        Substitutable resolver = null;
        try {
            resolver = new LargeFileSubstitutionHandler(_testFile);
            _algorithm.substitute(resolver);
            resolver.finish();
        } catch (Exception e) {
            Assert.fail("Test case failed : " + e.getMessage());
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
        } catch (FileNotFoundException e) {
            Assert.fail("Test case failed : " + e.getMessage());
        }
        String afterSubstitutionLine = null;
        try {
            int i = 0;
            while ((afterSubstitutionLine = reader.readLine()) != null) {
                switch (i++)
                {
                    case 1:
                        Assert.assertEquals(afterSubstitutionLine,
                                "<port name=\"http\" value=\"8080\"></port>");
                        break;
                    case 2:
                        Assert.assertEquals(afterSubstitutionLine,
                                "<port name=\"https\" value=\"8443\"></port>");
                        break;
                    default:
                        break;
                }
            }
            reader.close();
        } catch (IOException e) {
            Assert.fail("Not able to read test file");
        } finally {
            _testFile.delete();
        }
    }

    /**
     * Delete test file after test case executions.
     */
    @AfterTest
    public void destroy() {
        if (_testFile != null && _testFile.exists()) {
            if(!_testFile.delete())  {
                System.out.println("Not able to delete the temp file : " + _testFile.getAbsolutePath());
            }
        }
    }

    /**
     * Creates text file.
     */
    private void createTextFile() {
        BufferedWriter writer = null;
        try {
            _testFile = new File(_testFileName);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_testFile)));
            writer.write("First line in file repeat First line in file");
            writer.newLine();
            writer.write("Second line in file");
            writer.close();
        } catch (Exception e) {
            Assert.fail("Not able to create test Text file : " + _testFile.getAbsolutePath() + e.getMessage());
        }
    }

    /**
     * Creates XML file.
     */
    private void createXMLFile(String fileName) {
        _testFile = new File(fileName);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_testFile)));
            writer.write(" <ports>");
            writer.newLine();
            writer.write("<port name=\"http\" value=\"HTTP_PORT\"></port>");
            writer.newLine();
            writer.write("<port name=\"https\" value=\"HTTPS_PORT\"></port>");
            writer.newLine();
            writer.write("</ports>");
            writer.close();
        } catch (Exception e) {
            Assert.fail("Not able to create test XML file : " + _testFile.getAbsolutePath() + e.getMessage());
        }
    }
}
