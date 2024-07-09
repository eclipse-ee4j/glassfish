/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.itest.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

/**
 * Tools useful just for tests, so they don't belong to any application code.
 */
public final class TestUtilities {

    private TestUtilities() {
        // hidden
    }


    /**
     * Deletes files if they exist.
     * If it existed but was not possible to delete the file, uses NIO to delete it again - NIO
     * throws an exception in such case.
     * <p>
     * Attempts to delete all files and throws the {@link IOException} if any of them was not
     * possible to delete. Therefore this method should be the last call in your cleanup method (ie.
     * AfterEach or AfterAll)
     *
     * @param files files to delete
     * @throws IOException some files were not deleted.
     */
    public static void delete(final File... files) throws IOException {
        final Set<File> failed = new HashSet<>(files.length);
        for (File file : files) {
            if (file == null || !file.exists() || file.delete()) {
                continue;
            }
            failed.add(file);
        }
        if (failed.isEmpty()) {
            return;
        }
        final IOException failures = new IOException("Could not delete files: " + failed);
        for (File file : failed) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                failures.addSuppressed(e);
            }
        }
        throw failures;
    }
}
