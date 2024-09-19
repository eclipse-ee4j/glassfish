/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018-2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import com.sun.enterprise.admin.servermgmt.stringsubs.AttributePreprocessor;
import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutionException;
import com.sun.enterprise.admin.servermgmt.test.ServerMgmgtTestFiles;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.StringsubsDefinition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test class to test {@link StringSubstitutionEngine} functionality.
 */
public class StringSubstitutionEngineTest {

    private static final ServerMgmgtTestFiles TEST_FILES = new ServerMgmgtTestFiles(StringSubstitutionEngineTest.class);
    private static final String GROUP_WITHOUT_CHANGE_PAIR = "group_without_change_pair";
    private static final String GROUP_WITHOUT_FILES = "group_without_files";
    private static final String GROUP_WITH_INVALID_FILE_PATHS = "group_invalid_file_paths";
    private static final String _testFileName = "testStringSubs.txt";
    private final Map<String, String> substitutionRestoreMap = new HashMap<>();

    private File testFile;
    private Path archiveDirPath;
    private StringSubstitutionEngine engine;

    @BeforeEach
    public void init() throws Exception {
        archiveDirPath = TEST_FILES.getBasePackageAbsolutePath();

        final Map<String, String> lookUpMap = new HashMap<>();
        lookUpMap.put("ORACLE_HOME", "REPLACED_ORACLE_HOME");
        lookUpMap.put("MW_HOME", "REPLACED_MW_HOME");
        substitutionRestoreMap.put("REPLACED_ORACLE_HOME", "@ORACLE_HOME@");
        substitutionRestoreMap.put("REPLACED_MW_HOME", "@MW_HOME@");
        engine = new StringSubstitutionEngine(TEST_FILES.openInputStream("stringsubs.xml"));
        engine.setAttributePreprocessor(new CustomAttributePreprocessor(lookUpMap));
    }

    /**
     * Test the engine initialization for invalid stream.
     */
    @Test
    public void testInitializationForInvalidStream() {
        try {
            new StringSubstitutionEngine(null);
            fail("Allowing to parse the invalid stringsubs.xml stream.");
        } catch (StringSubstitutionException e) {
            assertEquals("InputStream is null", e.getMessage());
        }
    }

    /**
     * Test the loaded string-subs.xml object.
     */
    @Test
    public void testXMLLoading() {
        StringsubsDefinition def = engine.getStringSubsDefinition();
        assertNotNull(def);
        assertNotNull(def.getComponent());
        assertEquals(def.getComponent().size(), 2);
        assertNotNull(def.getVersion());
        assertFalse(def.getChangePair().isEmpty());
    }

    /**
     * Test substitution for null Component.
     */
    @Test
    public void testSubstitutionForNullComponent() {
        try {
            engine.substituteComponents(null);
            fail("Allowing to peform substitution for null Component.");
        } catch (StringSubstitutionException e) {
            assertEquals("No Component identifiers for substitution.", e.getMessage());
        }
    }

    /**
     * Test substitution for empty Component.
     */
    @Test
    public void testSubstitutionForEmptyComponent() {
        try {
            engine.substituteComponents(new ArrayList<>(1));
            fail("Allowing to peform substitution for empty Component.");
        } catch (StringSubstitutionException e) {
            assertEquals("No Component identifiers for substitution.", e.getMessage());
        }
    }

    /**
     * Test substitution for invalid Component.
     * @throws Exception
     */
    @Test
    public void testSubstitutionForInvalidComponent() throws Exception {
        List<String> componentIDs = new ArrayList<>(1);
        componentIDs.add("invalidComponent");
        engine.substituteComponents(componentIDs);
    }

    /**
     * Test substitution for invalid Component.
     * @throws Exception
     */
    @Test
    public void testSubstitutionForComponentWithoutGroup() throws Exception {
        List<String> componentIDs = new ArrayList<>(1);
        componentIDs.add("component_without_group");
        engine.substituteComponents(componentIDs);
    }

    /**
     * Test substitution for null Group.
     */
    @Test
    public void testSubstitutionForNullGroup() {
        try {
            engine.substituteGroups(null);
            fail("Allowing to peform substitution for null Groups.");
        } catch (StringSubstitutionException e) {
            assertEquals("No Group identifiers for substitution.", e.getMessage());
        }
    }

    /**
     * Test substitution for empty Group.
     */
    @Test
    public void testSubstitutionForEmptyGroup() {
        try {
            engine.substituteGroups(new ArrayList<>(1));
            fail("Allowing to peform substitution for empty Groups.");
        } catch (StringSubstitutionException e) {
            assertEquals("No Group identifiers for substitution.", e.getMessage());
        }
    }

    /**
     * Test substitution for invalid Group.
     * @throws Exception
     */
    @Test
    public void testSubstitutionForInvalidGroup() throws Exception {
        List<String> groupIDs = new ArrayList<>(1);
        groupIDs.add("invalidGroup");
        engine.substituteGroups(groupIDs);
    }

    /**
     * Test substitution for Group without any change pair.
     * @throws Exception
     */
    @Test
    public void testSubstitutionForGroupWithoutChangePair() throws Exception {
        List<String> groupIDs = new ArrayList<>(1);
        groupIDs.add(GROUP_WITHOUT_CHANGE_PAIR);
        createTextFile();
        engine.substituteGroups(groupIDs);
        final List<String> lines = Files.readAllLines(testFile.toPath());
        assertEquals("@ORACLE_HOME@ First word in first line", lines.get(0));
        assertEquals("Second line last word @MW_HOME@", lines.get(1));
    }

    /**
     * Test substitution for Group pointing to invalid file paths.
     * @throws Exception
     */
    @Test
    public void testSubstitutionForGroupInvalidFilePath() throws Exception {
        List<String> groupIDs = new ArrayList<>(1);
        groupIDs.add(GROUP_WITH_INVALID_FILE_PATHS);
        engine.substituteGroups(groupIDs);
    }

    /**
     * Test substitution for empty Group.
     * @throws Exception
     */
    @Test
    public void testSubstitutionForGroupWithoutFiles() throws Exception {
        List<String> groupIDs = new ArrayList<>(1);
        groupIDs.add(GROUP_WITHOUT_FILES);
        engine.substituteGroups(groupIDs);
    }

    /**
     * Creates text file.
     * @throws Exception
     */
    private void createTextFile() throws Exception {
        if (testFile != null) {
            destroy();
        }
        testFile = new File(_testFileName);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile), UTF_8))) {
            writer.write("@ORACLE_HOME@ First word in first line");
            writer.newLine();
            writer.write("Second line last word @MW_HOME@");
        }
    }

    /**
     * Custom implementation of {@link AttributePreprocessor}.
     */
    private class CustomAttributePreprocessor extends AttributePreprocessorImpl {
        private final String testFilePath;

        CustomAttributePreprocessor(Map<String, String> lookUpMap) throws Exception {
            super(lookUpMap);
            if (testFile == null) {
                createTextFile();
            }
            testFilePath = testFile.getAbsolutePath().replace(File.separator + _testFileName, "");
            lookUpMap.put("ARCHIVE_DIR", archiveDirPath.toString());
            lookUpMap.put("TEST_FILE_DIR", testFilePath);
        }
    }

    /**
     * Delete test file after test case executions.
     */
    @AfterEach
    public void destroy() {
        if (testFile != null) {
            testFile.delete();
            testFile = null;
        }
    }
}
