/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.uberjar.builder.installroot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * @author bhavanishankar@dev.java.net
 */

public class InstallRootBuilderUtil {

    private static final Logger logger = Logger.getLogger("embedded-glassfish");
    private static String resourceroot = "glassfish9/glassfish/";

    public static void buildInstallRoot(String installRoot) throws Exception {
        ClassLoader cl = InstallRootBuilderUtil.class.getClassLoader();
        String resourceName = resourceroot + "lib/";
        URL resource = cl.getResource(resourceName);
        URLConnection urlConn = resource.openConnection();
        if (urlConn instanceof JarURLConnection) {
            JarFile jarFile = ((JarURLConnection) urlConn).getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.indexOf(resourceName) != -1 && !entryName.endsWith("/")) {
                    copy(cl.getResourceAsStream(entryName), installRoot,
                            entryName.substring(entryName.indexOf(resourceName) + resourceroot.length()));
                }
            }
            jarFile.close();
        }
    }

    public static void copy(InputStream stream, String destDir, String path) {
        if (stream != null) {
            try {
                File f = new File(destDir, path);
                // create directory.
                if (!f.exists()) {
                    f.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(new File(destDir, path));
                    byte[] data = new byte[2048];
                    int count = 0;
                    while ((count = stream.read(data)) != -1) {
                        fos.write(data, 0, count);
                    }
                    logger.fine("Created " + f);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (Exception ex) {
                }
            }
        }
    }

}
