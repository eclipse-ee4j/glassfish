/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.cluster.ssh.sftp;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SFTPPathTest {

    @Test
    void constantCalls() {
        SFTPPath root = SFTPPath.of("/");
        SFTPPath abc = SFTPPath.ofAbsolutePath("a", "b", "c");
        assertEquals(root, abc.getRoot());
        assertEquals(root.hashCode(), abc.getRoot().hashCode());
        assertEquals(0, root.compareTo(abc.getRoot()));
        assertNotEquals(root, abc);
        assertNotEquals(SFTPPath.ofRelativePath("a"), Path.of("a"));
    }

    @Test
    void contains() {
        SFTPPath abc = SFTPPath.ofAbsolutePath("a", "b", "c");
        assertFalse(abc.contains("d"));
        assertTrue(abc.contains("b"));
        assertFalse(abc.contains(""));
    }

    @Test
    void startsWith() {
        SFTPPath abc = SFTPPath.ofAbsolutePath("a", "b", "c");
        assertFalse(abc.startsWith("g"));
        assertFalse(abc.startsWith("d"));
        assertTrue(abc.startsWith("/a/b"));
        assertTrue(abc.startsWith("/a/b/c"));
        assertTrue(abc.startsWith("/a/b/c"));
        assertFalse(abc.startsWith("/a/b/c/d"));
        assertFalse(abc.startsWith("a"));
        assertFalse(SFTPPath.ofRelativePath("a", "b", "c").startsWith(abc));
        assertFalse(abc.startsWith("a/b/c"));
    }

    @Test
    void endsWith() {
        SFTPPath abc = SFTPPath.ofAbsolutePath("a", "b", "c");
        assertFalse(abc.endsWith("a"));
        assertFalse(abc.endsWith("d"));
        assertTrue(abc.endsWith("b/c"));
        assertTrue(abc.endsWith("a/b/c"));
        assertTrue(abc.endsWith("/a/b/c"));
        assertFalse(abc.endsWith("a/b/c/d"));
        assertFalse(SFTPPath.ofRelativePath("a", "b", "c").endsWith("/a/b/c"));
    }

    @Test
    void iterator() {
        SFTPPath abc = SFTPPath.ofAbsolutePath("a", "b");
        Iterator<Path> iterator = abc.iterator();

        assertTrue(iterator.hasNext());
        Path a = iterator.next();
        assertEquals(SFTPPath.ofFileName("a"), a);
        assertFalse(a.isAbsolute());

        assertTrue(iterator.hasNext());
        Path b = iterator.next();
        assertEquals(SFTPPath.ofFileName("b"), b);
        assertFalse(b.isAbsolute());
        assertNotEquals(a, b);

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @Test
    void resolve() {
        SFTPPath abs = SFTPPath.ofAbsolutePath("a", "b");
        assertEquals(SFTPPath.of("/a/b/c"), abs.resolve("c"));
        assertEquals(SFTPPath.of("/c"), abs.resolve("/c"));
        assertEquals(abs, abs.resolve(""));
    }

    @Test
    void subpath() {
        SFTPPath abs = SFTPPath.ofAbsolutePath("a", "b", "c");
        assertEquals(SFTPPath.of("b/c"), abs.subpath(1, 3));
        assertEquals(SFTPPath.of("/a/b"), abs.subpath(0, 2));
        assertEquals(SFTPPath.of("/a"), abs.subpath(0, 1));
        assertEquals(SFTPPath.of("/a"), SFTPPath.ofAbsolutePath("a").subpath(0, 1));
        assertEquals(SFTPPath.of("a"), SFTPPath.ofRelativePath("a").subpath(0, 1));
    }

    @Test
    void ofEmpty() {
        SFTPPath path = SFTPPath.of("");
        assertEquals("", path.toString());
        assertEquals("", path.normalize().toString());
        assertEquals(0, path.getNameCount());
        assertNull(path.getFileName());
        assertNull(path.getParent());
        assertFalse(path.isAbsolute());
    }

    @Test
    void ofRoot() {
        SFTPPath path = SFTPPath.of("/");
        assertEquals("/", path.toString());
        assertEquals("/", path.normalize().toString());
        assertEquals(0, path.getNameCount());
        assertNull(path.getFileName());
        assertNull(path.getParent());
        assertTrue(path.isAbsolute());
    }

    @Test
    void ofRelativePath() {
        SFTPPath path = SFTPPath.of("./../a/b/../xx/zz/");
        assertEquals("./../a/b/../xx/zz", path.toString());
        assertEquals("../a/xx/zz", path.normalize().toString());
        assertEquals(7, path.getNameCount());
        assertEquals(SFTPPath.ofRelativePath("zz"), path.getFileName());
        assertEquals(SFTPPath.ofRelativePath(".", "..", "a", "b", "..", "xx"), path.getParent());
        assertFalse(path.isAbsolute());
    }

    @Test
    void ofAbsolutePath() {
        SFTPPath path = SFTPPath.of("/a/b/../xx/zz/");
        assertEquals("/a/b/../xx/zz", path.toString());
        assertEquals("/a/xx/zz", path.normalize().toString());
        assertEquals(5, path.getNameCount());
        assertEquals(SFTPPath.ofRelativePath("zz"), path.getFileName());
        assertEquals(SFTPPath.ofAbsolutePath("a", "b", "..", "xx"), path.getParent());
        assertTrue(path.isAbsolute());
    }

    @Test
    void ofAbsolutePathWithTooManyStepsUp() {
        SFTPPath path = SFTPPath.of("/a/b/../../../xx/zz");
        assertEquals("/a/b/../../../xx/zz", path.toString());
        assertEquals("/../xx/zz", path.normalize().toString());
        assertEquals(7, path.getNameCount());
        assertTrue(path.isAbsolute());
    }

    @Test
    void ofRelativePathWithTooManyStepsUp() {
        SFTPPath path = SFTPPath.of("a/b/../../../xx/zz");
        assertEquals("a/b/../../../xx/zz", path.toString());
        assertEquals("../xx/zz", path.normalize().toString());
        assertEquals(7, path.getNameCount());
        assertFalse(path.isAbsolute());
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void ofRelativeFileLinux() {
        SFTPPath origPath = SFTPPath.of(new File("a/b/../xx/zz/"));
        SFTPPath path = SFTPPath.of(origPath);
        assertEquals("a/xx/zz", path.toString());
        assertEquals("a/xx/zz", path.normalize().toString());
        assertEquals(3, path.getNameCount());
        assertFalse(path.isAbsolute());
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void ofAbsoluteFileLinux() {
        SFTPPath origPath = SFTPPath.of(new File("/a/b/../xx/zz/"));
        SFTPPath path = SFTPPath.of(origPath);
        assertEquals("/a/xx/zz", path.toString());
        assertEquals("/a/xx/zz", path.normalize().toString());
        assertEquals(3, path.getNameCount());
        assertTrue(path.isAbsolute());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void ofAbsoluteFileWindows() {
        Path origPath = new File("E:\\a\\b\\..\\xx\\zz\\").toPath();
        SFTPPath path = SFTPPath.of(origPath);
        assertEquals("/a/xx/zz", path.toString());
        assertEquals("/a/xx/zz", path.normalize().toString());
        assertEquals(3, path.getNameCount());
        assertTrue(path.isAbsolute());
    }
}
