/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.annotation.introspection.AnnotationScanner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.glassfish.api.deployment.archive.ReadableArchive;

/**
 * Subclass for connector annotation detection.
 * Connector annotation detector need to scan for top level jars as well
 *
 */
public class ConnectorAnnotationDetector extends AnnotationDetector {

    public ConnectorAnnotationDetector(AnnotationScanner scanner) {
        super(scanner);
    }

    @Override
    public boolean hasAnnotationInArchive(ReadableArchive archive) throws IOException {

        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement();
            if (entryName.endsWith(".class")) {
                if (containsAnnotation(archive, entryName)) {
                    return true;
                }
            }

            // scan classes in top level jars
            File archiveFile = new File(archive.getURI());
            File[] jarFiles = archiveFile.listFiles(new FileFilter() {
                 public boolean accept(File pathname) {
                     return (pathname.isFile() &&
                            pathname.getAbsolutePath().endsWith(".jar"));
                 }
            });

            if (jarFiles != null && jarFiles.length > 0) {
                for (File file : jarFiles) {
                    JarFile jarFile = null;
                    try {
                        jarFile = new JarFile(file);
                        Enumeration<JarEntry> jarEntries = jarFile.entries();
                        while (jarEntries.hasMoreElements()) {
                            JarEntry jarEntry = jarEntries.nextElement();
                            if (jarEntry.getName().endsWith(".class")) {
                                if (containsAnnotation(jarFile.getInputStream(
                                    jarEntry), jarEntry.getSize())) {
                                    return true;
                                }
                            }
                        }
                    } finally {
                        if (jarFile != null) {
                            jarFile.close();
                        }
                    }
                }
            }
        }
        return false;
    }
}
