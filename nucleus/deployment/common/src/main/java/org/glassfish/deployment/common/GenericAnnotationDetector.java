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

import javassist.bytecode.Opcode;
import org.objectweb.asm.*;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.net.URI;

import org.glassfish.api.deployment.archive.ReadableArchive;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.glassfish.internal.api.Globals;

import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * This class will detect whether an archive contains specified annotations.
 */
public class GenericAnnotationDetector extends AnnotationScanner {

    public static final Logger deplLogger = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    @LogMessageInfo(message = "Cannot find archive {0} referenced from archive {1}, it will be ignored for annotation scanning", level="WARNING")
    private static final String ARCHIVE_NOT_FOUND = "NCLS-DEPLOYMENT-00006";

    @LogMessageInfo(message = "Exception caught {0}", level="WARNING")
    private static final String EXCEPTION_CAUGHT = "NCLS-DEPLOYMENT-00007";

    @LogMessageInfo(message = "Error in jar entry {0}:  {1}", level="WARNING")
    private static final String JAR_ENTRY_ERROR = "NCLS-DEPLOYMENT-00008";

    @LogMessageInfo(message = "Failed to scan archive for annotations: {0}", level="WARNING")
    private static final String FAILED_ANNOTATION_SCAN = "NCLS-DEPLOYMENT-00009";
    boolean found = false;
    List<String> annotations = new ArrayList<String>();;

    public GenericAnnotationDetector(Class[] annotationClasses) {
        super(Opcodes.ASM7);
        if (annotationClasses != null) {
            for (Class annClass : annotationClasses) {
                annotations.add(Type.getDescriptor(annClass));
            }
        }
    }

    public boolean hasAnnotationInArchive(ReadableArchive archive) {
        scanArchive(archive);
        if (found) {
            return found;
        }
        ArchiveFactory archiveFactory = null;
        if (Globals.getDefaultHabitat() != null) {
            archiveFactory = Globals.getDefaultHabitat().getService(ArchiveFactory.class);
        }

        if (archiveFactory != null) {
            List<URI> externalLibs = DeploymentUtils.getExternalLibraries(archive);
            for (URI externalLib : externalLibs) {
                try {
                    scanArchive(archiveFactory.openArchive(new File(externalLib.getPath())));
                } catch(FileNotFoundException fnfe) {
                    Object args[] = { externalLib.getPath(), archive.getName() };
                    deplLogger.log(Level.WARNING, ARCHIVE_NOT_FOUND, args);
                } catch (Exception e) {
                    LogRecord lr = new LogRecord(Level.WARNING, EXCEPTION_CAUGHT);
                    Object args[] = { e.getMessage() };
                    lr.setParameters(args);
                    lr.setThrown(e);
                    deplLogger.log(lr);
                }
            }
        }
        return found;
    }

    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        if (annotations.contains(s)) {
            found = true;
        }
        return null;
    }

    @Override
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
                        if (found) {
                            return;
                        }
                    } finally {
                        is.close();
                    }
                } else if (entryName.endsWith(".jar") &&
                    entryName.indexOf('/') == -1) {
                    // scan class files inside top level jar
                    try {
                        ReadableArchive jarSubArchive = null;
                        try {
                            jarSubArchive = archive.getSubArchive(entryName);
                            Enumeration<String> jarEntries =
                                jarSubArchive.entries();
                            while (jarEntries.hasMoreElements()) {
                                String jarEntryName = jarEntries.nextElement();
                                if (jarEntryName.endsWith(".class")) {
                                    InputStream is =
                                        jarSubArchive.getEntry(jarEntryName);
                                    try {
                                        ClassReader cr = new ClassReader(is);
                                        cr.accept(this, crFlags);
                                        if (found) {
                                            return;
                                        }
                                    } finally {
                                        is.close();
                                    }
                                }
                            }
                        } finally {
                            jarSubArchive.close();
                        }
                    } catch (IOException ioe) {
                        Object args[] = { entryName, ioe.getMessage() };
                        deplLogger.log(Level.WARNING, JAR_ENTRY_ERROR, args);
                    }
                }
            }
        } catch (Exception e) {
          deplLogger.log(Level.WARNING, FAILED_ANNOTATION_SCAN, e.getMessage());
        }
    }
}
