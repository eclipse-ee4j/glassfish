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

package org.glassfish.ejb.deployment.annotation.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.glassfish.hk2.api.PerLookup;

import org.jvnet.hk2.annotations.Service;

@Service
@PerLookup
public class EjbInWarScanner extends EjbJarScanner {

    @Override
    protected void addScanDirectories() throws IOException {
        // add WEB-INF/classes
        File webinf = new File(archiveFile, "WEB-INF");
        File classes = new File(webinf, "classes");
        if (classes.exists()) {
            addScanDirectory(classes);
        }

        // add WEB-INF/lib
        File lib = new File(webinf, "lib");
        if (lib.exists()) {
            File[] jarFiles = lib.listFiles(new FileFilter() {
                 @Override
                public boolean accept(File pathname) {
                     return (pathname.getAbsolutePath().endsWith(".jar"));
                 }
            });

            if (jarFiles != null && jarFiles.length > 0) {
                for (File jarFile : jarFiles) {
                    // support exploded jar file
                    if (jarFile.isDirectory()) {
                        addScanDirectory(jarFile);
                    } else {
                        addScanJar(jarFile);
                    }
                }
            }
        }
    }

}
