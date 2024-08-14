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

package com.sun.enterprise.backup.util;

import com.sun.enterprise.util.OS;

import java.io.File;

public class BackupUtils {

    private BackupUtils() {
    }

    public static boolean protect(File f) {
        if(!f.exists())
            return true;

        try {
            File[]   files = null;
            boolean  ret = true;

            if(f.isDirectory()) {
                // chmod to rwx------
                // and chmod files inside dir to rw-------
                // 6580444 -- make any subdirs drwxr-xr-x (0755) otherwise we
                // can't delete the whole tree as non-root for some reason.
                // notice that the original file, if a directory, WILL have 0700
                // this is exactly the way the permissions exist in the original
                // domain files.

                if (!f.setExecutable(true, true)) {
                    ret = false;
                }

                if (!f.setReadable(true, true)) {
                    ret = false;
                }

                if (!f.setWritable(true, true)) {
                    ret = false;
                }

                files = f.listFiles();

                if(files == null || files.length < 1)
                    return ret;
            } else {
                files = new File[] { f };
            }

            for(File file : files) {
                if(file.isDirectory()) {
                    if (!file.setExecutable(true, false)) {
                        ret = false;
                    }

                    if (!file.setReadable(true, false)) {
                        ret = false;
                    }

                    if (!file.setWritable(true, true)) {
                        ret = false;
                    }
                } else {
                    if (!file.setExecutable(false, false)) {
                        ret = false;
                    }

                    if (!file.setReadable(true, true)) {
                        ret = false;
                    }

                    if (!file.setWritable(true, true)) {
                        ret = false;
                    }
                }
            }
            return ret;
        } catch(Exception e) {
            return false;
        }
    }

    /**
     **/

    public static boolean makeExecutable(File f)
    {
        if(!OS.isUNIX())
            return true;        // no such thing in Windows...

        if(!f.exists())
            return true; // no harm, no foul...

        if(!f.isDirectory())
            return makeExecutable(new File[] { f} );

        // if we get here -- f is a directory

        return makeExecutable(f.listFiles());
    }

    private static boolean makeExecutable(File[] files) {

        boolean ret = true;

        // WBN October 2005
        // dirspace bugfix -- what if there is a space in the dirname?  trouble!
        // changed the argument to a File array

        // we are using a String here so that you can pass in a bunch
        // of space-separated filenames.  Doing it one at a time would be inefficient...
        // make it executable for ONLY the user

        // Jan 19, 2005 -- rolled back the fix for 6206176.  It has been decided
        // that this is not a bug but rather a security feature.


        // BUGFIX: 6206176
        // permissions changed from 744 to 755.
        // The reason is that if user 'A' does a restore then user 'A' will be the only
        // user allowed to start or stop a domain.  Whether or not a user is allowed to start a domain
        // needs to be based on the AppServer authentication mechanism (username-password) rather
        // than on the OS authentication mechanism.
        // This case actually is common:  user 'A' does the restore, root tries to start the restored domain.

        if(files == null || files.length <= 0)
            return true;

        for(File f : files) {
            if (!f.setExecutable(true, true)) {
                ret = false;
            }

            if (!f.setReadable(true, false)) {
                ret = false;
            }

            if (!f.setWritable(false, false)) {
                ret = false;
            }
        }

        return ret;
    }
}
