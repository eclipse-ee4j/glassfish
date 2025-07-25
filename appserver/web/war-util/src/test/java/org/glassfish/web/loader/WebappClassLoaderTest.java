/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.util.io.FileUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.naming.resources.WebDirContext;
import org.glassfish.main.jdke.cl.GlassfishUrlClassLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
class WebappClassLoaderTest {

    private static Path docBase;
    private static Path repo;


    @BeforeAll
    static void initRepositories() throws Exception {
        docBase = Files.createTempDirectory(WebappClassLoaderTest.class.getSimpleName() + "-docBase");
        repo = Files.createDirectory(docBase.resolve("repo1"));
        Files.createFile(repo.resolve("file1.txt"));
        Path dir1 = Files.createDirectory(repo.resolve("Dir1"));
        Files.createFile(dir1.resolve("fileInDir1.txt"));
    }


    @AfterAll
    static void removeRepositories() throws Exception {
        FileUtils.getAllFilesAndDirectoriesUnder(docBase.toFile());
    }


    @Test
    public void isParallel() {
        assertAll(
            () -> assertTrue(new URLClassLoader(new URL[0]).isRegisteredAsParallelCapable()),
            () -> assertTrue(new GlassfishUrlClassLoader("WebappClassLoaderTest", new URL[0]).isRegisteredAsParallelCapable()),
            () -> assertTrue(new WebappClassLoader(null).isRegisteredAsParallelCapable())
        );
    }


    @Test
    public void getResource() {
        WebappClassLoader classLoader = createCL();
        classLoader.start();
        assertAll(
            () -> assertThat(classLoader.getResource(".").toExternalForm(), endsWith("/repo1/")),
            () -> assertThat(classLoader.getResource("").toExternalForm(), endsWith("/repo1/")),
            () -> assertThat(classLoader.getResource("file1.txt").toExternalForm(), endsWith("/file1.txt")),
            () -> assertThat(classLoader.getResource("Dir1").toExternalForm(), endsWith("/Dir1/")),
            () -> assertThat(classLoader.getResource("Dir1/fileInDir1.txt").toExternalForm(),
                endsWith("/Dir1/fileInDir1.txt"))        );
    }


    @Test
    public void getResourceAsStream() {
        WebappClassLoader classLoader = createCL();
        classLoader.start();
        assertAll(
            // streams of 4 bytes returned by the delegate CL
            () -> assertNotNull(classLoader.getResourceAsStream("."), "."),
            () -> assertNotNull(classLoader.getResourceAsStream(""), ""),
            () -> assertNotNull(classLoader.getResourceAsStream("file1.txt"), "file1.txt"),
            // WebappCL returns null.
            () -> assertNull(classLoader.getResourceAsStream("Dir1"), "Dir1"),
            () -> assertNotNull(classLoader.getResourceAsStream("Dir1/fileInDir1.txt"), "Dir1/fileInDir1.txt")
        );
    }


    @Test
    public void getURLs() throws Exception {
        WebappClassLoader classLoader = createCL();
        assertThat(classLoader.getURLs(), arrayWithSize(1));
        classLoader.start();
        assertThat(classLoader.getURLs(), arrayWithSize(1));
        classLoader.close();
        assertThat(classLoader.getURLs(), arrayWithSize(0));
    }


    private WebappClassLoader createCL() {
        WebappClassLoader classLoader = new WebappClassLoader(WebappClassLoaderTest.class.getClassLoader());
        WebDirContext webDirContext = new WebDirContext();
        webDirContext.setDocBase(docBase.toFile().getAbsolutePath());
        classLoader.setResources(webDirContext);
        classLoader.addRepository(repo.getFileName().toString() + "/", repo.toFile());
        classLoader.setDelegate(false);
        return classLoader;
    }
}
