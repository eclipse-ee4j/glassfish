/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deploy.shared;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.jul.handler.LogCollectorHandler;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Tim Quinn
 */
public class FileArchiveTest {

    private static final String EXPECTED_LOG_KEY = "NCLS-DEPLOYMENT-00022";
    private static final String LINE_SEP = System.lineSeparator();
    private static final String STALE_ENTRY = "oldLower/oldFile.txt";
    private static final String SUBARCHIVE_NAME = "subarch";

    private File archiveDir;
    private final Set<String> usualEntryNames =
            new HashSet<>(Arrays.asList(new String[] {"sample.txt", "lower/other.txt"}));

    private final Set<String> usualExpectedEntryNames = initUsualExpectedEntryNames();
    private final Set<String> usualExpectedEntryNamesWithOverwrittenStaleEntry =
            initUsualExpectedEntryNamesWithOverwrittenStaleEntry();

    private final Set<String> usualSubarchiveEntryNames =
            new HashSet<>(Arrays.asList(new String[] {"a.txt", "under/b.txt"}));

    private final Set<String> usualExpectedSubarchiveEntryNames = initUsualExpectedSubarchiveEntryNames();

    private static ServiceLocator locator;
    private static ModulesRegistry registry;
    private static ArchiveFactory archiveFactory;
    private static LogCollectorHandler handler;

    @BeforeAll
    public static void setUpClass() throws Exception {
        registry = new StaticModulesRegistry(FileArchiveTest.class.getClassLoader());
        locator = registry.createServiceLocator("default");
        archiveFactory = locator.getService(ArchiveFactory.class);
        handler = new LogCollectorHandler(deplLogger);
    }

    @BeforeEach
    public void setUp() throws IOException {
        archiveDir = tempDir();
        assertThat(deplLogger.getHandlers(), arrayContainingInAnyOrder(handler));
    }

    @AfterEach
    public void tearDown() {
        if (archiveDir != null) {
            clean(archiveDir);
        }
        archiveDir = null;
    }

    @AfterAll
    public static void shutdownLocator() {
        if (locator != null) {
            locator.shutdown();
        }
        if (registry != null) {
            registry.shutdown();
        }
        if (handler != null) {
            handler.close();
        }
    }


    private Set<String> initUsualExpectedEntryNames() {
        final Set<String> expectedEntryNames = new HashSet<>(usualEntryNames);
        expectedEntryNames.add("lower");
        return expectedEntryNames;
    }

    private Set<String> initUsualExpectedEntryNamesWithOverwrittenStaleEntry() {
        final Set<String> result = initUsualExpectedEntryNames();
        result.add(STALE_ENTRY);
        result.add("oldLower");
        return result;
    }

    private  Set<String> initUsualExpectedSubarchiveEntryNames() {
        final Set<String> result = new HashSet<>(usualSubarchiveEntryNames);
        result.add("under");
        return result;
    }

    private File tempDir() throws IOException {
        final File f = File.createTempFile("FileArch", "");
        f.delete();
        f.mkdir();
        return f;
    }

    private void clean(final File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                clean(f);
            }
            if (!f.delete()) {
                f.deleteOnExit();
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
        }
    }


    private ReadableArchive createAndPopulateArchive(final Set<String> entryNames) throws Exception {
        WritableArchive instance = archiveFactory.createArchive(archiveDir.toURI());
        instance.create(archiveDir.toURI());

        // Add some entries.
        for (String entryName : entryNames) {
            instance.putNextEntry(entryName);
            instance.closeEntry();
        }
        instance.close();
        return archiveFactory.openArchive(archiveDir);
    }

    private ReadableArchive createAndPopulateSubarchive(
            final WritableArchive parent,
            final String subarchiveName,
            final Set<String> entryNames) throws Exception {
        final WritableArchive result = parent.createSubArchive(subarchiveName);
        for (String entryName : entryNames) {
            result.putNextEntry(entryName);
            result.closeEntry();
        }
        result.close();

        final ReadableArchive readableParent = archiveFactory.openArchive(parent.getURI());
        return readableParent.getSubArchive(subarchiveName);
    }


    private void createAndPopulateAndCheckArchive(final Set<String> entryNames) throws Exception {
        final ReadableArchive instance = createAndPopulateArchive(entryNames);
        checkArchive(instance, usualExpectedEntryNames);
    }


    private void checkArchive(final ReadableArchive instance, final Set<String> expectedEntryNames) {
        final Set<String> foundEntryNames = new HashSet<>();
        for (Enumeration<String> e = instance.entries(); e.hasMoreElements();) {
            foundEntryNames.add(e.nextElement());
        }
        assertEquals(expectedEntryNames, foundEntryNames, "Missing or unexpected entry names reported");
    }


    private void getListOfFiles(final FileArchive instance, final Set<String> expectedEntryNames, final Logger logger) {
        final List<String> foundEntryNames = new ArrayList<>();
        instance.getListOfFiles(archiveDir, foundEntryNames, null, logger);
        assertEquals(expectedEntryNames, new HashSet<>(foundEntryNames), "Missing or unexpected entry names reported");
    }

    private void getListOfFilesCheckForLogRecord(FileArchive instance, final Set<String> expectedEntryNames) throws IOException {
        handler.reset();
        getListOfFiles(instance, expectedEntryNames, deplLogger);
        List<GlassFishLogRecord> records = handler.getAll();
        if (records.size() != 1) {
            final StringBuilder sb = new StringBuilder();
            for (LogRecord record : records) {
                sb.append(record.getLevel().getLocalizedName())
                        .append(": ")
                        .append(record.getMessage())
                        .append(LINE_SEP);
            }
            fail("Expected 1 log message but received " + records.size() + " as follows:" + LINE_SEP + sb);
        }

        // We have a stale file under a stale directory.  Make sure a direct
        // request for the stale file fails.  (We know already from above that
        // getting the entries list triggers a warning about the skipped stale file.)
        final InputStream is = instance.getEntry(STALE_ENTRY);
        assertNull(is, "Incorrectly located stale FileArchive entry " + STALE_ENTRY);
    }

    /**
     * Computes the expected entry names for an archive which contains a subarchive.
     * <p>
     * The archive's entries method will report all the entries in the main
     * archive, plus the subarchive name, plus the entries in the subarchive.
     * @param expectedFromArchive entries from the main archive
     * @param subarchiveName name of the subarchive
     * @param expectedFromSubarchive entries in the subarchive
     * @return entry names that should be returned from the main archive's entries() method
     */
    private Set<String> expectedEntryNames(Set<String> expectedFromArchive, final String subarchiveName, Set<String>expectedFromSubarchive) {
        final Set<String> result = new HashSet<>(expectedFromArchive);
        result.add(subarchiveName);
        for (String expectedSubarchEntryName : expectedFromSubarchive) {
            final StringBuilder path = new StringBuilder();
            path.append(subarchiveName).append("/");
            final String[] segments = expectedSubarchEntryName.split("/");
            for (int i = 0; i < segments.length; i++) {
                path.append(segments[i]);
                result.add(path.toString());
                if (i < segments.length) {
                    path.append("/");
                }
            }
        }
        return result;
    }

    @Test
    public void testSubarchive() throws Exception {
        try (ArchiveAndSubarchive archives = createAndPopulateArchiveAndSubarchive()) {
            checkArchive(archives.parent, archives.fullExpectedEntryNames);
            checkArchive(archives.subarchive, usualExpectedSubarchiveEntryNames);
        }
    }

    @Test
    public void testSubArchiveCreateWithStaleEntry() throws Exception {
        // Subarchives are a little tricky.  The marker file lives only at
        // the top level (because that's where undeployment puts it).  So
        // when a subarchive tests to see if an entry is valid it needs to
        // consult the marker file (if any) in the top-level owning archive.
        //
        // This test creates a directory structure containing a stale file
        // in a lower-level directory, creates the top-level marker file
        // as undeployment would, then creates an archive for the top level
        // and a subarchive for the lower-level directory (as the next
        // deployment would).  The archive and subarchive need to skip the
        // stale file.

        //  Create a file in the directory before creating the archive.
        final File oldDir = new File(archiveDir, SUBARCHIVE_NAME);
        final File oldFile = new File(oldDir, STALE_ENTRY);
        oldFile.getParentFile().mkdirs();
        oldFile.createNewFile();

        //  Mimic what undeployment does by creating a marker file for the
        //  archive recording the pre-existing file.
        FileArchive.StaleFileManager.Util.markDeletedArchive(archiveDir);

        //  Now create the archive and subarchive on top of the directories
        //  which already exist and contain the stale file and directory.
        try (ArchiveAndSubarchive archives = createAndPopulateArchiveAndSubarchive()) {
            checkArchive(archives.parent, archives.fullExpectedEntryNames);
            checkArchive(archives.subarchive, usualExpectedSubarchiveEntryNames);
            getListOfFilesCheckForLogRecord((FileArchive) archives.parent, archives.fullExpectedEntryNames);
        }
    }

    private static class ArchiveAndSubarchive implements AutoCloseable {
        ReadableArchive parent;
        ReadableArchive subarchive;
        Set<String> fullExpectedEntryNames;
        @Override
        public void close() throws Exception {
            parent.close();
            subarchive.close();
        }
    }

    private ArchiveAndSubarchive createAndPopulateArchiveAndSubarchive() throws Exception {
        final ArchiveAndSubarchive result = new ArchiveAndSubarchive();
        result.parent = createAndPopulateArchive(usualEntryNames);
        result.subarchive = createAndPopulateSubarchive(
                (FileArchive) result.parent,
                SUBARCHIVE_NAME,
                usualSubarchiveEntryNames);
        result.fullExpectedEntryNames = expectedEntryNames(
                usualExpectedEntryNames, SUBARCHIVE_NAME, usualSubarchiveEntryNames);

        return result;
    }

    /**
     * Test of open method, of class FileArchive.
     */
    @Test
    public void testNormalCreate() throws Exception {
        createAndPopulateAndCheckArchive(usualEntryNames);
    }

    @Test
    public void testCreateWithOlderLeftoverEntry() throws Exception {
        final ReadableArchive instance = createWithOlderLeftoverEntry(usualEntryNames);
        getListOfFilesCheckForLogRecord((FileArchive) instance, usualExpectedEntryNames);
    }

    @Test
    public void testCreateWithOlderLeftoverEntryWhichIsCreatedAgain() throws Exception {
        final FileArchive instance = (FileArchive) createWithOlderLeftoverEntry(usualEntryNames);
        // Now add the stale entry explicitly which should make it valid.
        try (OutputStream os = instance.putNextEntry(STALE_ENTRY)) {
            os.write("No longer stale!".getBytes());
        }
        checkArchive(instance, usualExpectedEntryNamesWithOverwrittenStaleEntry);
    }

    private ReadableArchive createWithOlderLeftoverEntry(final Set<String> entryNames) throws Exception {
        // Create a file in the directory before creating the archive.
        final File oldFile = new File(archiveDir, STALE_ENTRY);
        oldFile.getParentFile().mkdirs();
        oldFile.createNewFile();

        // Mimic what undeployment does by creating a marker file for the
        // archive recording the pre-existing file.
        FileArchive.StaleFileManager.Util.markDeletedArchive(archiveDir);

        // Now create the archive.  The archive should not see the old file.
        return createAndPopulateArchive(entryNames);
    }

    @Test
    public void testCreateWithOlderLeftoverEntryAndThenOpen() throws Exception {
        createWithOlderLeftoverEntry(usualEntryNames);
        final FileArchive openedArchive = new FileArchive();
        openedArchive.open(archiveDir.toURI());
        System.err.println("A WARNING should appear next");
        checkArchive(openedArchive, usualExpectedEntryNames);
    }

    @Test
    public void testOpenWithPreexistingDir() throws Exception {
        createPreexistingDir();
        final FileArchive openedArchive = new FileArchive();
        openedArchive.open(archiveDir.toURI());
        checkArchive(openedArchive, usualExpectedEntryNames);
    }

    private void createPreexistingDir() throws IOException {
         for (String entryName : usualEntryNames) {
             final File f = fileForPath(archiveDir, entryName);
             final File parentDir = f.getParentFile();
             if(parentDir != null) {
                 parentDir.mkdirs();
             }
             try {
                 f.createNewFile();
             } catch (Exception ex) {
                 throw new IOException(f.getAbsolutePath(), ex);
             }
         }
     }

    private File fileForPath(File anchor, final String path) {
         final String[] interveningDirNames = path.split("/");
         File interveningDir = anchor;
         for (int i = 0; i < interveningDirNames.length - 1; i++) {
             String name = interveningDirNames[i];
             interveningDir = new File(interveningDir, name + "/");
         }
         return new File(interveningDir,interveningDirNames[interveningDirNames.length - 1]);
     }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void testInaccessibleDirectoryInFileArchive() throws Exception {
        final FileArchive archive = (FileArchive) createAndPopulateArchive(usualEntryNames);

        // Now make the lower-level directory impossible to execute - therefore
        // the attempt to list the files should fail.
        final File lower = new File(archiveDir, "lower");
        lower.setExecutable(false, false);
        assertTrue(lower.setReadable(false, false));

        // Try to list the files.  This should fail with our logger getting one record.
        final Vector<String> fileList = new Vector<>();
        handler.reset();
        archive.getListOfFiles(lower, fileList, null /* embeddedArchives */, deplLogger);

        List<GlassFishLogRecord> logRecords = handler.getAll();
        assertThat("FileArchive logged no message about being unable to list files; expected " + EXPECTED_LOG_KEY,
            logRecords, not(emptyIterable()));
        assertThat(logRecords.get(0), instanceOf(GlassFishLogRecord.class));
        GlassFishLogRecord record0 = logRecords.get(0);
        assertEquals(EXPECTED_LOG_KEY, record0.getMessageKey());
        // Change the protection back.
        lower.setExecutable(true, false);
        lower.setReadable(true, false);
        handler.reset();

        archive.getListOfFiles(lower, fileList, null, deplLogger);
        assertNull(handler.pop(), "FileArchive was incorrectly unable to list files; error key in log record");
    }
}

