/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.annotation.impl;

import com.sun.enterprise.deployment.ConnectorDescriptor;

import java.io.IOException;
import java.io.File;
import java.io.FileFilter;
import java.util.logging.Level;

import org.glassfish.apf.impl.AnnotationUtils;
import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;


@Service(name="rar")
@PerLookup
public class RarScanner extends ModuleScanner<ConnectorDescriptor>{

    public void process(File archiveFile, ConnectorDescriptor desc,
        ClassLoader classLoader) throws IOException {
        if (AnnotationUtils.getLogger().isLoggable(Level.FINE)) {
            AnnotationUtils.getLogger().fine("archiveFile is " + archiveFile);
            AnnotationUtils.getLogger().fine("classLoader is " + classLoader);
        }
        this.archiveFile = archiveFile;
        this.classLoader = classLoader;
        if (archiveFile.isDirectory()) {
            addScanDirectory(archiveFile);

            // add top level jars for scanning
            File[] jarFiles = archiveFile.listFiles(new FileFilter() {
                 public boolean accept(File pathname) {
                     return (pathname.isFile() &&
                            pathname.getAbsolutePath().endsWith(".jar"));
                 }
            });

            if (jarFiles != null && jarFiles.length > 0) {
                for (File jarFile : jarFiles) {
                    addScanJar(jarFile);
                }
            }
        }else{
            AnnotationUtils.getLogger().fine("RARScanner : not a directory : " + archiveFile.getName());
        }
    }
}
