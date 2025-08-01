/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.payload;

import com.sun.enterprise.util.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import org.glassfish.api.admin.Payload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Tim Quinn
 */
public class PayloadImplTest {

    private static class FileInfo {
        private final String name;
        private final String path;
        private final String pathPrefix;
        private final String content;

        private FileInfo(final String name, final String pathPrefix, final String content) {
            this.name = name;
            this.pathPrefix = pathPrefix;
            this.path = (pathPrefix == null ? "" : pathPrefix + '/') + name;
            this.content = content;
        }

        private File file(final File rootDir) {
            return new File(rootDir.toURI().resolve(path));
        }
    }

    private static final String SUBDIR_LEVEL_1 = "a";
    private static final String SUBDIR_LEVEL_2 = "a1";

    private static final FileInfo ORIGINAL_FILE_X =
            new FileInfo("x.txt", SUBDIR_LEVEL_1 + '/' + SUBDIR_LEVEL_2, "old x");

    private static final FileInfo ORIGINAL_FILE_Y = new FileInfo("y.txt", SUBDIR_LEVEL_1, "old y");

    private static final FileInfo[] ORIGINAL_FILES = {ORIGINAL_FILE_X, ORIGINAL_FILE_Y};

    private static final FileInfo ADDED_FILE_Z = new FileInfo("z.txt", SUBDIR_LEVEL_1 + '/' + SUBDIR_LEVEL_2, "new z");

    private static final FileInfo REPLACED_FILE_X = new FileInfo(ORIGINAL_FILE_X.name, ORIGINAL_FILE_X.pathPrefix,
        "replaced x");

    private static final String REPL_SUBDIR_LEVEL_1 = "repl-a";
    private static final String REPL_SUBDIR_LEVEL_2 = "repl-a1";

    private static final FileInfo REPLACEMENT_FILE_A = new FileInfo("r-a.txt", REPL_SUBDIR_LEVEL_1, "replacement a");
    private static final FileInfo REPLACEMENT_FILE_B = new FileInfo("r-b.txt", null, "replacement b");
    private static final FileInfo REPLACEMENT_FILE_C = new FileInfo("r-c.txt",
        REPL_SUBDIR_LEVEL_1 + '/' + REPL_SUBDIR_LEVEL_2, "replacement c");
    private static final FileInfo[] REPLACEMENT_FILES = {REPLACEMENT_FILE_A, REPLACEMENT_FILE_B, REPLACEMENT_FILE_C};

    /** The path of the original file that is replaced but the name and content of the replacement file */
    private static final FileInfo REPLACED_FILE_C = new FileInfo(REPLACEMENT_FILE_C.name,
        SUBDIR_LEVEL_1 + '/' + SUBDIR_LEVEL_2, REPLACEMENT_FILE_C.content);

    private static final String LINE_SEP = System.getProperty("line.separator");

    private File workingDir;
    private File replDir;

    @BeforeEach
    public void setUp() {
        try {
            workingDir = createAndPopulateWorkingDir();
            log("  Working dir is set up at " + workingDir.getAbsolutePath());
            replDir = createAndPopulateReplacementDir();
            log("  Replacement dir is set up at " + replDir.getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @AfterEach
    public void tearDown() {
        cleanDir(workingDir);
        log("  Working dir cleaned");
        cleanDir(replDir);
        log("  Replacement dir cleaned");
    }

    @Test
    public void testDirectoryReplaceRequest() {
        /*
         * We try to replace the original subdir1/subdir2 in the working dir
         * with some of the replacement content from the repl dir.
         */
        System.out.println("testDirectoryReplaceRequest");
        File dirToUseForReplacement = new File(replDir, REPL_SUBDIR_LEVEL_1);
        dirToUseForReplacement = new File(dirToUseForReplacement, REPL_SUBDIR_LEVEL_2);

        File zipFile = null;
        try {
            Payload.Outbound outboundPayload = PayloadImpl.Outbound.newInstance();
            outboundPayload.requestFileReplacement("application/octet-stream",
                    new URI(SUBDIR_LEVEL_1 + '/' + SUBDIR_LEVEL_2 + '/'), "replacement",
                    null, dirToUseForReplacement, true);
            zipFile = writePayloadToFile(outboundPayload, File.createTempFile("payloadZip", ".zip"));

            preparePFM(zipFile);

            /*
             * Make sure the directory was replaced as desired and that other
             * original files are still there.  Specificaly, original file Y should still
             * exist, but original file X should be gone.  Replacement file C should
             * now exist in the original directory but not other replacement files.
             */
            checkFile(ORIGINAL_FILE_Y, ORIGINAL_FILE_Y.file(workingDir));
            checkNoFile(ORIGINAL_FILE_X, ORIGINAL_FILE_X.file(workingDir));
            checkFile(REPLACED_FILE_C, REPLACED_FILE_C.file(workingDir));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            deleteAndLogFailure(zipFile);
        }
    }

    private void checkFile(final FileInfo fileInfo, final File f) throws FileNotFoundException, IOException {
        assertTrue(f.exists(), "Expected output file " + fileInfo.path + " does not exist");
        assertEquals(fileInfo.content, readFromFile(f), "Expected file contents for " + fileInfo.path + " not correct");
    }

    private void checkNoFile(final FileInfo fileInfo, final File f) {
        assertFalse(f.exists(), "File " + fileInfo.path + " not expected but exists");
    }

    @Test
    public void testSingleFileReplaceRequest() throws Exception {
        System.out.println("testSingleFileReplaceRequest");
        File newVersion = null;
        File zipFile = null;

        try {
            Payload.Outbound outboundPayload = PayloadImpl.Outbound.newInstance();
            newVersion = File.createTempFile("payload",".txt");
            populateFile(newVersion, REPLACED_FILE_X.content);
            outboundPayload.requestFileReplacement("application/octet-stream",
                    new URI(REPLACED_FILE_X.path), "replacement",
                    null, newVersion, false);
            zipFile = writePayloadToFile(outboundPayload, File.createTempFile("payloadZip", ".zip"));

            for (Map.Entry<File,Properties> entry : preparePFM(zipFile).entrySet()) {
                final File processedFile = entry.getKey();
                if (processedFile.toURI().getPath().endsWith(REPLACED_FILE_X.path)) {
                    assertTrue(processedFile.exists(),
                        "Expected original output file " + REPLACED_FILE_X.path + " does not exist");
                    assertEquals(REPLACED_FILE_X.content, readFromFile(processedFile),
                        "Expected new file contents for " + REPLACED_FILE_X.path + " not correct");
                }
            }
        } finally {
            deleteAndLogFailure(newVersion);
            deleteAndLogFailure(zipFile);
        }
    }

    private static void deleteAndLogFailure(final File f) {
        if (f != null) {
            if ( ! f.delete()) {
                System.err.println("Could not delete " + f.getAbsolutePath() + "; continuing");
            }
        }
    }

    @Test
    public void testAddFiles() throws Exception {
        System.out.println("testAddFiles");
        File fileToBeAddedToPayload = null;
        File zipFile = null;
        try {
            Payload.Outbound outboundPayload = PayloadImpl.Outbound.newInstance();
            fileToBeAddedToPayload = File.createTempFile("payload",".txt");
            populateFile(fileToBeAddedToPayload, ADDED_FILE_Z.content);
            log("  Populated " + fileToBeAddedToPayload.getAbsolutePath());

            /*
             * Use application/octet-stream here.  text/plain might seem more
             * logical, but the payload implementation correctly treats a
             * payload containing a single text entry differently from a
             * multi-part payload or a payload containing a single non-text part.
             * (This is because admin operations return their results in a text
             * entry in the payload of the HTTP request. If no other data is
             * being streamed then the requirement is to return regular text in
             * the payload which makes non-asadmin clients - such as IDEs and
             * web browsers - happier.)
             */
            outboundPayload.attachFile("application/octet-stream", new URI(ADDED_FILE_Z.path), "addText",
                    fileToBeAddedToPayload);
            log("  Attached " + ADDED_FILE_Z.path);

            zipFile = writePayloadToFile(outboundPayload, File.createTempFile("payloadZip", ".zip"));
            log("  Wrote payload to " + zipFile.getAbsolutePath());


        // XXX consume the map; check for the new file and contents
            boolean isFileProcessed = false;
            for (Map.Entry<File,Properties> entry : preparePFM(zipFile).entrySet()) {
                final File processedFile = entry.getKey();
                if (processedFile.toURI().getPath().endsWith(ADDED_FILE_Z.path)) {
                    isFileProcessed = true;
                    assertTrue(processedFile.exists(), "Expected output file " + ADDED_FILE_Z.path + " does not exist");
                    assertEquals(ADDED_FILE_Z.content, readFromFile(processedFile),
                        "Expected new file contents for " + ADDED_FILE_Z.path + "not correct");
                }
            }
            assertTrue(isFileProcessed, "Added file " + ADDED_FILE_Z.path + " did not appear in the processed output");

            /*
             * Make sure the original file still exists and contains what it should.
             */
            final File originalFile = new File(workingDir.toURI().resolve(ORIGINAL_FILE_X.path));
            assertTrue(originalFile.exists(), "Original file " + ORIGINAL_FILE_X.path + " no longer exists but it should");
            assertEquals(ORIGINAL_FILE_X.content, readFromFile(originalFile),
                "Original file " + ORIGINAL_FILE_X.path + " exists as expected but no longer contains what it should");
        } finally {
            deleteAndLogFailure(fileToBeAddedToPayload);
            deleteAndLogFailure(zipFile);
        }
    }

    private Map<File, Properties> preparePFM(final File zipFile) throws FileNotFoundException, IOException, Exception {
        PayloadFilesManager.Perm pfm = new PayloadFilesManager.Perm(workingDir, null);

        final InputStream is = new BufferedInputStream(new FileInputStream(zipFile));
        Payload.Inbound inboundPayload = PayloadImpl.Inbound.newInstance("application/zip", is);
        final Map<File,Properties> map = pfm.processPartsExtended(inboundPayload);
        return map;
    }

    private File writePayloadToFile(final Payload.Outbound ob, final File f) throws FileNotFoundException, IOException {
        final OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
        ob.writeTo(os);
        return f;
    }

    private String readFromFile(final File f) throws FileNotFoundException, IOException {
        final StringBuilder sb = new StringBuilder();
        final LineNumberReader r = new LineNumberReader(new FileReader(f, UTF_8));
        try {
            String line;
            while ((line = r.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append(LINE_SEP);
                }
                sb.append(line);
            }
            return sb.toString();
        } finally {
            r.close();
        }
    }

    /**
     * Create a directory anchored at a temp directory that looks like this:
     * a/
     * a/a1/
     * a/a1/x.txt (contains "old x")
     *
     * @return
     */
    private File createAndPopulateWorkingDir() throws IOException {
        return createAndPopulateDir(SUBDIR_LEVEL_1, SUBDIR_LEVEL_2, ORIGINAL_FILES);
    }

    private File createAndPopulateReplacementDir() throws IOException {
        return createAndPopulateDir(REPL_SUBDIR_LEVEL_1,
                REPL_SUBDIR_LEVEL_2, REPLACEMENT_FILES);
    }

    private File createAndPopulateDir(final String subdirLevel1Path, final String subdirLevel2Path,
            final FileInfo[] files) throws IOException {
        final File top = createTempDir("payload", "");
        final File subdirLevel1 = createSubdir(top, subdirLevel1Path);
        createSubdir(subdirLevel1, subdirLevel2Path);
        for (FileInfo replFile : files) {
            populateFile(new File(top.toURI().resolve(replFile.path)),
                    replFile.content);
        }
        return top;
    }

    private File createTempDir(final String prefix, final String suffix) throws IOException {
        File temp = File.createTempFile(prefix, suffix);
        if ( ! temp.delete()) {
            throw new IOException("Cannot delete temp file " + temp.getAbsolutePath());
        }
        if ( ! temp.mkdirs()) {
            throw new IOException("Cannot create temp directory" + temp.getAbsolutePath());
        }
        return temp;
    }

    private File createSubdir(final File parent, final String subdirPath) throws IOException {
        final File f = new File(parent, subdirPath);
        if ( ! f.mkdirs()) {
            throw new IOException("Cannot create temp subdir " + f.getAbsolutePath());
        }
        return f;
    }

    private void populateFile(final File f, final String content) throws IOException {
        try (PrintStream ps = new PrintStream(f, UTF_8)) {
            ps.println(content);
        }
    }

    private void cleanDir(final File d) {
        FileUtils.whack(d);
    }

    private void log(final String s) {
        //System.out.println(s);
    }

}
