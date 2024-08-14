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

package com.sun.enterprise.util.net;

import com.sun.enterprise.util.CULoggerInfo;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class JarURIPattern {
    private static final Logger logger = CULoggerInfo.getLogger();

    /**
     * This method is used to extract URI of jar entries that match
     * a given pattern.
     * @param uri
     * @param pattern
     */
    public static List<String> getJarEntries(URI uri, Pattern pattern) {
        List<String> results = new ArrayList<String>();

        File file = null;
        try {
            file = new File(uri);
        } catch(Exception ex) {
            // ignore
        }
        if (file == null || file.isDirectory()) {
            return results;
        }

        String fileName = file.getName();

        // only look at jar file
        if (fileName.endsWith(".jar")) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(new File(uri));
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry)entries.nextElement();
                    String entryName = entry.getName();
                    if (pattern.matcher(entryName).matches()) {
                        results.add(entryName);
                    }
                }
            } catch(Exception ex) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING,
                            CULoggerInfo.getString(CULoggerInfo.exceptionJarOpen, fileName),
                            ex);
                }
                throw new RuntimeException(ex);
            } finally {
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (Throwable t) {
                        // Ignore
                    }
                }
            }
        }

        return results;
    }

}
