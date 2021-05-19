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
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * FilePath provides general file path manipulation utilities.
 */
public class FilePath {
    private static String cwdAbsolute;

    /**
     * Return the absolute path for a file.  All directory separators are
     * converted to be File.separatorChar
     */
    public static String getAbsolutePath(File file) {
        /* VJ++ blows it here and doesn't use File.separatorChar when making
       a relative path absolute.  It uses '/' instead.  */

        String basicAbsolute = file.getAbsolutePath();
        if (file.separatorChar == '/')
            return basicAbsolute.replace('\\', '/');
        else
            return basicAbsolute.replace('/', '\\');
    }

    private static String getCwdAbsolute() {
        if (cwdAbsolute == null)
            cwdAbsolute = getAbsolutePath(new File("."));//NOI18N
        return cwdAbsolute;
    }

    /**
     * Attempt to produce a canonical path name for the specified file
     */
    public static String canonicalize(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException ioe) {
            /* JDK 1.1.4 gets an IOException if you pass it a UNC path name. */
        }

        /* Do it the hard way if getCanonicalPath fails.
         * This is far from perfect.
         * - It doesn't know about multiple mount points
         * - Doesn't deal with case differences.
         */
        String absolutePath = getAbsolutePath(file);
        Vector components = new Vector();
        StringTokenizer parser =
            new StringTokenizer(absolutePath, File.separator, true);
        while (parser.hasMoreElements())
            components.addElement(parser.nextToken());

        boolean editted = true;
        while (editted) {
            editted = false;
            for (int i=1; i<components.size() && !editted; i++) {
                String s = (String)components.elementAt(i);
                if (s.equals(".")) {//NOI18N
                    components.removeElementAt(i);
                    components.removeElementAt(i-1);
                    editted = true;
                } else if (s.equals("..")) {//NOI18N
                    components.removeElementAt(i);
                    components.removeElementAt(i-1);
                    if (i > 2) {
                        if (!((String)components.elementAt(i-2)).equals(File.separator) &&
                            ((String)components.elementAt(i-3)).equals(File.separator)) {
                            components.removeElementAt(i-2);
                            components.removeElementAt(i-3);
                        }
                    }
                    editted = true;
                }
            }
        }

        /* Special case for Windows */
        String cwd = getCwdAbsolute();
        if (cwd.length() > 2 &&
            cwd.charAt(0) != File.separatorChar &&
            cwd.charAt(1) == ':') {
            /* probably a drive letter */
            if (((String)components.elementAt(0)).equals(File.separator) &&
                (components.size() == 1 ||
                !((String)components.elementAt(1)).equals(File.separator))) {
                String drive = cwd.substring(0,2);
                components.insertElementAt(drive, 0);
            }
        }

        /* Remove a trailing File.separatorChar */
        if (components.size() > 0 &&
            ((String)components.elementAt(components.size()-1)).equals(
                File.separator))
            components.removeElementAt(components.size()-1);

        StringBuffer result = new StringBuffer();
        for (int j=0; j<components.size(); j++)
            result.append((String)components.elementAt(j));

        return result.toString();
    }

    /**
     * Compare two "canonical" file names for equivalence
     */
    public static boolean canonicalNamesEqual(String f1, String f2) {
        boolean equal;
        String cwd = getCwdAbsolute();
        if (cwd.length() > 2 &&
            cwd.charAt(0) != File.separatorChar &&
            cwd.charAt(1) == ':') {
            equal = f1.equalsIgnoreCase(f2);
        }
        else
            equal = f1.equals(f2);
        return equal;
    }

}

