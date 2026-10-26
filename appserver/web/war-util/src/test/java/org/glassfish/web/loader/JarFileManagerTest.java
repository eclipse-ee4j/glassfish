/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.web.loader;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JarFileManagerTest {

    private static final InheritableThreadLocal<ClassLoader> INHERITED = new InheritableThreadLocal<>();

    @TempDir
    private Path tempDir;

    /**
     * The JAR files are opened by whatever thread loads a class first, ie. the admin thread
     * deploying or undeploying another application. The scheduler thread must not keep that
     * thread's context class loader reachable, not even if the manager is never closed.
     */
    @Test
    void schedulerDoesNotRetainContextClassLoaderOfOpeningThread() throws Exception {
        final File jar = Files.createFile(tempDir.resolve("test.jar")).toFile();
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar.toPath()))) {
            // empty jar
        }

        final WeakReference<ClassLoader> otherApplication = openJarsFromOtherApplication(jar);

        final Thread scheduler = Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getName().startsWith("JarFileManager")).findFirst().orElseThrow();
        assertSame(JarFileManager.class.getClassLoader(), scheduler.getContextClassLoader());

        for (int i = 0; i < 50 && otherApplication.get() != null; i++) {
            System.gc();
            Thread.sleep(100L);
        }
        assertNull(otherApplication.get(), "The class loader of the other application is still reachable");
    }


    private static WeakReference<ClassLoader> openJarsFromOtherApplication(File jar) throws Exception {
        final URLClassLoader otherApplication = new URLClassLoader(new URL[0], null);
        final JarFileManager manager = new JarFileManager();
        manager.addJarFile(jar);
        final JarFile[][] jarFiles = new JarFile[1][];
        final Thread thread = new Thread(() -> {
            INHERITED.set(otherApplication);
            // Starts a new thread if the scheduler has none yet.
            jarFiles[0] = manager.getJarFiles();
            INHERITED.remove();
        });
        thread.setContextClassLoader(otherApplication);
        thread.start();
        thread.join();
        assertAll(
            () -> assertThat(jarFiles[0], arrayWithSize(1)),
            () -> assertThat(manager.getJarRealFiles(), arrayWithSize(1))
        );
        manager.closeJarFiles();
        otherApplication.close();
        return new WeakReference<>(otherApplication);
    }
}
