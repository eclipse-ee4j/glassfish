/*
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

package com.sun.enterprise.admin.servermgmt.stringsubs;

import com.sun.enterprise.admin.servermgmt.stringsubs.impl.AttributePreprocessorImpl;
import com.sun.enterprise.admin.servermgmt.stringsubs.impl.StringSubstitutionEngineTest;
import com.sun.enterprise.admin.servermgmt.stringsubs.impl.SubstituableFactoryImpl;
import com.sun.enterprise.admin.servermgmt.test.ServerMgmgtTestFiles;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Group;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit Test class for {@link StringSubstitutionFactory}.
 */
public class StringSubstitutionFactoryTest {

    private static final ServerMgmgtTestFiles TEST_FILES = new ServerMgmgtTestFiles(StringSubstitutionEngineTest.class);
    private static final String TEST_FILE_NAME = "testStringSubs.txt";
    private static final String TEST_ARCHIVE_NAME = "testStringSubsArchive.jar";
    private static final String VALID_GROUP_ID = "valid_group";
    private static final Map<String, String> SUBSTITUTION_MAP = Map.of(
            "JAVA", "REPLACED_JAVA",
            "JAVA_HOME", "REPLACED_JAVA_HOME",
            "MW_HOME", "REPLACED_MW_HOME",
            "TEST_FILE_DIR_PATH", TEST_FILES.getBasePackageAbsolutePath().toString());


    /**
     * Test String substitution for invalid stream.
     */
    @Test
    public void testStringSubstitutorInvalidStream() throws Exception {
        try (InputStream invalidStream = TEST_FILES.openInputStream(TEST_FILES.getBaseClass().getSimpleName() + ".class")) {
            StringSubstitutionFactory.createStringSubstitutor(invalidStream);
            fail("No exception thrown for invalid stream.");
        } catch (StringSubstitutionException e) {
            assertEquals("Failed to parse given stream against the schema xsd/schema/stringsubs.xsd.", e.getMessage());
        }
    }

    /**
     * Test String substitution for null stream.
     */
    @Test
    public void testStringSubstitutorNullStream() {
        try {
            StringSubstitutionFactory.createStringSubstitutor(null);
            fail("No exception thrown for null stream.");
        } catch (StringSubstitutionException e) {
            assertEquals("InputStream is null", e.getMessage());
        }
    }

    /**
     * Test String substitution for valid stream.
     * @throws StringSubstitutionException
     * @throws IOException
     */
    @Test
    public void testStringSubstitutorValidStream() throws StringSubstitutionException, IOException {
        final StringSubstitutor substitutor;
        try (InputStream validStream = TEST_FILES.openInputStream("stringsubs.xml")) {
            substitutor = StringSubstitutionFactory.createStringSubstitutor(validStream);
        }
        substitutor.setAttributePreprocessor(new AttributePreprocessorImpl(SUBSTITUTION_MAP));
        backUpTestFile();
        try {
            substitutor.substituteAll();
            for (Group group : substitutor.getStringSubsDefinition().getGroup()) {
                if (group.getId().equals(VALID_GROUP_ID)) {
                    validateSubstitutedArchiveEntries(group);
                }
            }
        } finally {
            restoreTestFile();
        }
    }

    /**
     * Validate if the substitution occurred properly in the test file.
     */
    private void validateTestFile(File testFile) throws IOException {
        final List<String> lines = Files.readAllLines(testFile.toPath());
        assertEquals(2, lines.size());
        assertEquals("Substitute REPLACED_JAVA_HOME REPLACED_JAVA @MW_", lines.get(0));
        assertEquals("HOME@", lines.get(1));
    }

    /**
     * Validate all the substitutable archive entries.
     */
    private void validateSubstitutedArchiveEntries(Group group) throws IOException {
        List<? extends Substitutable> substituables = new SubstituableFactoryImpl().getArchiveEntrySubstitutable(group.getArchive().get(0));
        for (Substitutable substituable : substituables) {
            validateTestFile(new File(substituable.getName()));
            substituable.finish();
        }
    }

    /**
     * Restore the archive by performing reverse substitution.
     */
    private void backUpTestFile() {
        try {
            for (File file : TEST_FILES.getBasePackageDirectory().listFiles()) {
                if (file.getName().endsWith(TEST_ARCHIVE_NAME) || file.getName().endsWith(TEST_FILE_NAME)) {
                    Files.copy(file.toPath(), new File(file.getAbsolutePath() + ".bkp").toPath());
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error occured while creating a backup archive before subsitution.", e);
        }
    }

    private void restoreTestFile() {
        try {
            for (File file : TEST_FILES.getBasePackageDirectory().listFiles()) {
                if (file.getName().endsWith(TEST_ARCHIVE_NAME) || file.getName().endsWith(TEST_FILE_NAME)) {
                    file.delete();
                }
            }
            for (File file : TEST_FILES.getBasePackageDirectory().listFiles()) {
                if (file.getAbsolutePath().endsWith(".bkp")) {
                    file.renameTo(new File(file.getAbsolutePath().replace(".bkp", "")));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error occured while restoring the archive after subsitution.", e);
        }
    }
}
