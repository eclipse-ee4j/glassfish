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

package com.sun.enterprise.tools.verifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Reads the list of file names from the file cleandirs.txt and
 * calls deleteFile to recursively delete the directories.
 * NOTE : This independent class gets called only on W2K, where the
 * Normal cleanAll() call from doit() in verifier fails.
 */
public class TmpCleaner {

    private final static String TMPDIR = System.getProperty("java.io.tmpdir");

    public void run() {

        // read the file
        try {
            String cleandirs = TMPDIR + File.separator + "cleandirs.txt"; // NOI18N
            File tmpfile = new File(cleandirs);
            if (!tmpfile.exists())
                return;
            BufferedReader br = new BufferedReader(new FileReader(cleandirs));

            try {
                do {
                    String str = br.readLine();
                    String file = TMPDIR + File.separator + str;
                    File toDelete = new File(file);
                    deleteFile(toDelete);
                    toDelete.deleteOnExit();
                } while (br.ready());
            } catch (Exception e) {
            }


            br.close();
            File f = new File(cleandirs);
            f.delete();
        } catch (Exception e) {
        }
    }

    private void deleteFile(File p_file) {
        String FILE_SEPARATOR = System.getProperty("file.separator");
        // If it is a directory, empty it first
        if (p_file.isDirectory()) {
            String[] dirList = p_file.list();
            for (int i = 0; i < dirList.length; i++) {

                File aFile = new File(
                        p_file.getPath() + FILE_SEPARATOR + dirList[i]);
                if (aFile.isDirectory()) {
                    deleteFile(aFile);
                }
                aFile.delete();
            }
        }
        p_file.delete();
    }


    public static void main(String[] args) {
        try {
            TmpCleaner t = new TmpCleaner();
            System.gc();
            t.run();
        } catch (Exception e) {
        }
        System.exit(0);
    }

}
