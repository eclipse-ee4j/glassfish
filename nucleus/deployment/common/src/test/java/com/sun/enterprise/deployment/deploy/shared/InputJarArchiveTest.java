/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.deploy.shared;

import com.sun.enterprise.deployment.deploy.shared.InputJarArchive.CollectionWrappedEnumeration;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tim
 */
public class InputJarArchiveTest {

    private static final String NESTED_JAR_ENTRY_NAME = "nested/archive.jar";

    /**
     * Test of getArchiveSize method, of class InputJarArchive.
     */
    @Test
    public void testCollectionWrappedEnumerationSimple() {
        final Enumeration<String> e = testEnum();
        CollectionWrappedEnumeration<String> cwe = new CollectionWrappedEnumeration<>(() -> e);
        ArrayList<String> answer = new ArrayList<>(cwe);
        assertEquals(testStringsAsArrayList(), answer, "resulting array list != original");
    }

    @Test
    public void testCollectionWrappedEnumerationInitialSize() {
        final Enumeration<String> e = testEnum();
        CollectionWrappedEnumeration<String> cwe = new CollectionWrappedEnumeration<>(() -> e);
        int size = cwe.size();
        ArrayList<String> answer = new ArrayList<>(cwe);
        assertEquals(testStringsAsArrayList(), answer, "array list of size " + size + " after initial size != original");
    }

    @Test
    public void testCollectionWrappedEnumerationMiddleSize() {
        CollectionWrappedEnumeration<String> cwe = new CollectionWrappedEnumeration<>(InputJarArchiveTest::testEnum);
        ArrayList<String> answer = new ArrayList<>();
        Iterator<String> it = cwe.iterator();
        answer.add(it.next());
        answer.add(it.next());
        answer.add(it.next());
        int size = cwe.size();
        answer.add(it.next());
        answer.add(it.next());
        assertEquals(testStringsAsArrayList(), answer, "array list of size " + size + " after middle size call != original");
    }

    @Test
    public void testCollectionWrappedEnumerationEndSize() {
        CollectionWrappedEnumeration<String> cwe = new CollectionWrappedEnumeration<>(InputJarArchiveTest::testEnum);
        List<String> answer = new ArrayList<>();
        Iterator<String> it = cwe.iterator();
        answer.add(it.next());
        answer.add(it.next());
        answer.add(it.next());
        answer.add(it.next());
        answer.add(it.next());
        int size = cwe.size();
        assertEquals(testStringsAsArrayList(), answer,
            "array list of size " + size + " after middle size call != original");
    }

    private ReadableArchive getArchiveForTest() throws IOException {
        File tempJAR = createTestJAR();
        return new InputJarArchive(tempJAR);
    }

    private void retireArchive(final ReadableArchive arch) throws IOException {
        arch.close();
        final File tempJAR = new File(arch.getURI().getSchemeSpecificPart());
        tempJAR.delete();
    }

    @Test
    public void testTopLevelDirEntryNamesForInputJarArchive() throws Exception {
        final ReadableArchive arch = getArchiveForTest();
        final Set<String> returnedNames = new HashSet<>(arch.getDirectories());
        assertEquals(testJarTopLevelDirEntryNames(), returnedNames,
            "Returned top-level directories do not match expected");
        retireArchive(arch);
    }

    @Test
    public void testNonDirEnryNames() throws Exception {
        final ReadableArchive arch = getArchiveForTest();
        final Set<String> returnedNames = new HashSet<>(setFromEnumeration(arch.entries()));
        assertEquals(testStandAloneArchiveJarNonDirEntryNames(), returnedNames,
            "Returned non-directory entry names do not match expected");
        retireArchive(arch);
    }

    @Test
    public void testNestedTopLevelDirEntryNames() throws Exception {
        final ReadableArchive arch = getArchiveForTest();
        ReadableArchive subArchive = arch.getSubArchive(NESTED_JAR_ENTRY_NAME);

        final Set<String> returnedNames = new HashSet<>(subArchive.getDirectories());
        assertEquals(testJarTopLevelDirEntryNames(), returnedNames,
            "Returned nested top-level directories do not match expected");
        retireArchive(arch);
    }

    @Test
    public void testNestedNonDirEntryNames() throws Exception {
        final ReadableArchive arch = getArchiveForTest();
        ReadableArchive subArchive = arch.getSubArchive(NESTED_JAR_ENTRY_NAME);

        final Set<String> returnedNames = new HashSet<>(setFromEnumeration(subArchive.entries()));
        assertEquals(testSubArchiveNonDirEntryNames(), returnedNames,
            "Returned nested non-directories do not match expected");
        retireArchive(arch);
    }

    private File createTestJAR() throws IOException {
        final File tempJAR = File.createTempFile("InputJarArchive", ".jar");
        tempJAR.deleteOnExit();
        final Manifest mf = new Manifest();
        Attributes mainAttrs = mf.getMainAttributes();
        mainAttrs.put(Name.MANIFEST_VERSION, "1.0");

        final JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tempJAR)),
                mf);


        for (String entryName : testJarEntryNames()) {
            final JarEntry entry = new JarEntry(entryName);
            jos.putNextEntry(entry);
            jos.closeEntry();
            // Note that these entries in the test JAR are empty - we just need to test the names
        }

        //  Now create a nested JAR within the main test JAR.  For simplicity
        //  use the same entry names as the outer JAR.
        final JarEntry nestedJarEntry = new JarEntry(NESTED_JAR_ENTRY_NAME);
        jos.putNextEntry(nestedJarEntry);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final JarOutputStream nestedJOS = new JarOutputStream(baos);

        for (String entryName : testJarEntryNames()) {
            final JarEntry nestedEntry = new JarEntry(entryName);
            nestedJOS.putNextEntry(nestedEntry);
            nestedJOS.closeEntry();
        }

        nestedJOS.close();
        jos.write(baos.toByteArray());
        jos.closeEntry();

        jos.close();
        return tempJAR;
    }


    private static Enumeration<String> testEnum() {
        return Collections.enumeration(testStringsAsArrayList());
    }

    private static List<String> testStringsAsArrayList() {
        return Arrays.asList("one","two","three","four","five");
    }

    private static List<String> testJarEntryNames() {
        return Arrays.asList(
                "topLevelNonDir",
                "topLevelDir/",
                "topLevelDir/secondLevelNonDir",
                "topLevelDir/secondLevelDir/",
                "topLevelDir/secondLevelDir/thirdLevelNonDir");
    }


    private static Set<String> testJarTopLevelDirEntryNames() {
        return Set.of("topLevelDir/");
    }


    private static Set<String> testSubArchiveNonDirEntryNames() {
        return Set.of("topLevelNonDir", "topLevelDir/secondLevelNonDir", "topLevelDir/secondLevelDir/thirdLevelNonDir");
    }


    private static Set<String> testStandAloneArchiveJarNonDirEntryNames() {
        final Set<String> result = new HashSet<>(testSubArchiveNonDirEntryNames());
        result.add(NESTED_JAR_ENTRY_NAME);
        return result;
    }


    private static <T> Set<T> setFromEnumeration(final Enumeration<T> e) {
        final Set<T> result = new HashSet<>();
        while (e.hasMoreElements()) {
            result.add(e.nextElement());
        }
        return result;
    }
}
