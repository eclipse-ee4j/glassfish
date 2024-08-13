/*
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

package org.glassfish.deployment.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class AnnotationScanner extends ClassVisitor {

    public static final Logger deplLogger = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    @LogMessageInfo(message = "Exception while scanning {0}", level="WARNING")
    private static final String SCANNING_EXCEPTION = "NCLS-DEPLOYMENT-00001";

    @LogMessageInfo(message = "Error scan jar entry {0} {1}", level="WARNING")
    private static final String JAR_ENTRY_SCAN_ERROR = "NCLS-DEPLOYMENT-00002";

    @LogMessageInfo(message = "Failed to scan archive for annotations", level="WARNING")
    private static final String FAILED_ANNOTATION_SCAN = "NCLS-DEPLOYMENT-00003";

    public AnnotationScanner(int api) {
        super(api);
    }

    public void visit(int version,
           int access,
           String name,
           String signature,
           String superName,
           String[] interfaces) {
    }

    public void visitSource(String s, String s1) {}

    public void visitOuterClass(String s, String s1, String s2) {

    }

    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        return null;
    }

    public void visitAttribute(Attribute attribute) {

    }

    public void visitInnerClass(String s, String s1, String s2, int i) {

    }

    public FieldVisitor visitField(int i, String s, String s1, String s2, Object o) {
        return null;
    }

    public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
        return null;
    }

    public void visitEnd() {

    }

    public void scanArchive(ReadableArchive archive) {
        try {
            int crFlags = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
                | ClassReader.SKIP_FRAMES;
            Enumeration<String> entries = archive.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement();
                if (entryName.endsWith(".class")) {
                    // scan class files
                    InputStream is = archive.getEntry(entryName);
                    try {
                        ClassReader cr = new ClassReader(is);
                        cr.accept(this, crFlags);
                    } catch(Exception e) {
                      LogRecord lr = new LogRecord(Level.WARNING, SCANNING_EXCEPTION);
                      Object args[] = { entryName };
                      lr.setParameters(args);
                      lr.setThrown(e);
                      deplLogger.log(lr);
                    } finally {
                        is.close();
                    }
                } else if (entryName.endsWith(".jar")) {
                    // scan class files inside jar
                    try {
                        File archiveRoot = new File(archive.getURI());
                        File file = new File(archiveRoot, entryName);
                        JarFile jarFile = new JarFile(file);
                        try {
                            Enumeration<JarEntry> jarEntries = jarFile.entries();
                            while (jarEntries.hasMoreElements()) {
                                JarEntry entry = jarEntries.nextElement();
                                String jarEntryName = entry.getName();
                                if (jarEntryName.endsWith(".class")) {
                                    InputStream is = jarFile.getInputStream(entry);
                                    try {
                                        ClassReader cr = new ClassReader(is);
                                        cr.accept(this, crFlags);
                                    } catch(Exception e) {
                                      deplLogger.log(Level.FINE,
                                                     "Exception while scanning " +
                                                     entryName, e);
                                    } finally {
                                        is.close();
                                    }
                                }
                            }
                        } finally {
                            jarFile.close();
                        }
                    } catch (IOException ioe) {
                        Object args[] = { entryName, ioe.getMessage() };
                        deplLogger.log(Level.WARNING, JAR_ENTRY_SCAN_ERROR, args);
                    }
                }
            }
        } catch (Exception e) {
            deplLogger.log(Level.WARNING, FAILED_ANNOTATION_SCAN, e);
        }
    }
}
