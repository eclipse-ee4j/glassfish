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

import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.admin.servermgmt.stringsubs.SubstitutionAlgorithm;
import com.sun.enterprise.admin.servermgmt.stringsubs.impl.LargeFileSubstitutionHandler;
import com.sun.enterprise.admin.servermgmt.stringsubs.impl.SmallFileSubstitutionHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Abstract class to test substitution algorithm. Derived classes will
 * provide the implementation of {@link SubstitutionAlgorithm} use to
 * execute the test cases, by defining the abstract method
 * {@link AbstractSubstitutionAlgo#getAlgorithm(Map)}
 */
abstract class AbstractSubstitutionAlgo {
    private final String testFileName = "testStringSubs.txt";
    private File testFile;
    private SubstitutionAlgorithm algorithm;

    /**
     * Create test file used as a input file for string substitution.
     */
    @BeforeEach
    public void init() {
        Map<String, String> substitutionMap = new HashMap<>();
        substitutionMap.put("line", "replacedLine");
        substitutionMap.put("file", "testFile");
        substitutionMap.put("HTTP_PORT", "8080");
        substitutionMap.put("HTTPS_PORT", "8443");
        algorithm = getAlgorithm(substitutionMap);
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
    @Test
    public void testSubstitutionForNullMap() {
        assertThrows(IllegalArgumentException.class, () -> getAlgorithm(null));
    }

    /**
     * Test the {@link SubstitutionAlgorithm} instance for empty map.
     */
    @Test
    public void testSubstitutionForEmptyMap() {
        assertThrows(IllegalArgumentException.class, () -> getAlgorithm(new HashMap<>()));
    }

    /**
     * Test substitution for small text file.
     * @throws Exception
     */
    @Test
    public void testSmallTextFileSubstitution() throws Exception {
        createTextFile();
        Substitutable resolver = new SmallFileSubstitutionHandler(testFile);
        algorithm.substitute(resolver);
        resolver.finish();
        final List<String> lines = Files.readAllLines(testFile.toPath());
        assertEquals(2, lines.size());
        assertEquals(lines.get(0), "First replacedLine in testFile repeat First replacedLine in testFile");
        assertEquals(lines.get(1), "Second replacedLine in testFile");
    }

    /**
     * Test substitution for small XML file.
     * @throws Exception
     */
    @Test
    public void testSmallXMLFileSubstitution() throws Exception {
        String fileName = testFileName.replace(".txt", ".xml");
        createXMLFile(fileName);
        Substitutable resolver = new SmallFileSubstitutionHandler(new File(fileName));
        algorithm.substitute(resolver);
        resolver.finish();
        final List<String> lines = Files.readAllLines(testFile.toPath());
        assertEquals(4, lines.size());
        assertEquals(lines.get(0), " <ports>");
        assertEquals(lines.get(1), "<port name=\"http\" value=\"8080\"></port>");
        assertEquals(lines.get(2), "<port name=\"https\" value=\"8443\"></port>");
        assertEquals(lines.get(3), "</ports>");
    }

    /**
     * Test substitution for large text file.
     * @throws Exception
     */
    @Test
    public void testLargeTextFileSubstitution() throws Exception {
        createTextFile();
        Substitutable resolver = new LargeFileSubstitutionHandler(testFile);
        algorithm.substitute(resolver);
        resolver.finish();
        final List<String> lines = Files.readAllLines(testFile.toPath());
        assertEquals(2, lines.size());
        assertEquals(lines.get(0), "First replacedLine in testFile repeat First replacedLine in testFile");
        assertEquals(lines.get(1), "Second replacedLine in testFile");
    }

    /**
     * Test substitution for large XML file.
     * @throws Exception
     */
    @Test
    public void testLargeXMLFileSubstitution() throws Exception {
        String fileName = testFileName.replace(".txt", ".xml");
        createXMLFile(fileName);
        Substitutable resolver = new LargeFileSubstitutionHandler(testFile);
        algorithm.substitute(resolver);
        resolver.finish();
        final List<String> lines = Files.readAllLines(testFile.toPath());
        assertEquals(4, lines.size());
        assertEquals(lines.get(0), " <ports>");
        assertEquals(lines.get(1), "<port name=\"http\" value=\"8080\"></port>");
        assertEquals(lines.get(2), "<port name=\"https\" value=\"8443\"></port>");
        assertEquals(lines.get(3), "</ports>");
    }

    /**
     * Delete test file after test case executions.
     */
    @AfterEach
    public void destroy() {
        if (testFile != null) {
            if(!testFile.delete())  {
                System.err.println("Not able to delete the temp file : " + testFile.getAbsolutePath());
            }
        }
    }

    /**
     * Creates text file.
     * @throws Exception
     */
    private void createTextFile() throws Exception {
        testFile = new File(testFileName);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile), UTF_8))) {
            writer.write("First line in file repeat First line in file");
            writer.newLine();
            writer.write("Second line in file");
        }
    }

    /**
     * Creates XML file.
     * @throws Exception
     */
    private void createXMLFile(String fileName) throws Exception {
        testFile = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile), UTF_8))) {
            writer.write(" <ports>");
            writer.newLine();
            writer.write("<port name=\"http\" value=\"HTTP_PORT\"></port>");
            writer.newLine();
            writer.write("<port name=\"https\" value=\"HTTPS_PORT\"></port>");
            writer.newLine();
            writer.write("</ports>");
        }
    }
}
