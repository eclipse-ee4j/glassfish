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

package com.sun.jdo.api.persistence.enhancer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipFile;

/**
 * ZipFileRegistry provides a central point for lookup of zip files
 * within the filter tool. It needs to be public because it's
 * accessed from outside the filter.util package.
 */
public class ZipFileRegistry {

    /* A mapping of file name to ZipFile */
    private static Hashtable zipFileMap = new Hashtable(11);

    /**
     * Return a zip file which may already be open
     */
    public static ZipFile openZipFile(File f) throws FileNotFoundException, IOException {
        ZipFile zf = (ZipFile) zipFileMap.get(f.getPath());
        if (zf == null) {
            zf = new ZipFile(f);
            zipFileMap.put(zf.getName(), zf);
        }
        return zf;
    }


    /**
     * Return a zip file which must already be open
     */
    public static ZipFile getZipFile(String path) {
        return (ZipFile) zipFileMap.get(path);
    }


    /**
     * Returns an enumeration of the zip files in the registry
     * Each element is a ZipFile.
     */
    public static Enumeration zipFiles() {
        return zipFileMap.elements();
    }

}
