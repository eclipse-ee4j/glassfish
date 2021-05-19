/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package embedded.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static void zipInstanceDirectory() {
        zipInstanceDirectory("tmp");
    }
    public static void zipInstanceDirectory(String name) {
        try {
            ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( new File(name + "-instance-dir.zip") ) );
            File directory = new File(System.getProperty("com.sun.aas.instanceRoot"));
            zip( directory, directory.getParentFile(), zos );
            zos.close();
        } catch (IOException e) {
            System.out.println("ERROR preserving the directory:");
            e.printStackTrace();
        }
    }

    private static final void zip(File directory, File base, ZipOutputStream zos) throws IOException {
        File[] files = directory.listFiles();
        byte[] buffer = new byte[8192];
        int read = 0;
        for (int i = 0, n = files.length; i < n; i++) {
            if (files[i].getCanonicalPath().indexOf("osgi") != -1 ||
                    files[i].getCanonicalPath().indexOf("ejbtimer") != -1 ) {
                continue;
            } else {
                ZipEntry entry = new ZipEntry(files[i].getPath().substring(
                      base.getPath().length() + 1));
                zos.putNextEntry(entry);
                if (files[i].isDirectory()) {
                    zip(files[i], base, zos);
                } else {
                    FileInputStream in = new FileInputStream(files[i]);
                    while (-1 != (read = in.read(buffer))) {
                        zos.write(buffer, 0, read);
                    }
                    in.close();
                }
            }
        }
    }
}
