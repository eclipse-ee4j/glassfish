/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.io;

import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import com.sun.enterprise.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A class for sanitizing Files.
 * Note that the main reason for this class is that on non-Windows,
 * getCanonicalXXX and getAbsoluteXXX might point at different files.
 * If the file is a soft link then the Canonical will be the file that is linked to.
 * The Absolute will be the link file itself.
 * This method will give you the benefits of Canonical -- but will always point
 * at the link file itself.
 * Windows is horribly complex compared to "everything else".  Windows does not have
 * the symbolic link issue -- so use getCanonicalXXX to do the work on Windows.
 * Windows will return paths with all forward slashes -- no backward slashes unless it
 * is the special Windows network address that starts with "\\"
 * <p>
 * I.e. It is just like getAbsoluteXXX -- but it removes all relative path
 * elements from the path.
 * @author bnevins
 */
public class SmartFile {

    /**
     * Sanitize a File object -- remove all relative path portions, i.e. dots
     * e.g. "/xxx/yyy/././././../yyy"  --> /xxx/yyy on UNIX, perhaps C:/xxx/yyy on Windows
     * @param f The file to sanitize
     * @return THe sanitized File
     */
    public static File sanitize(File f) {
        SmartFile sf = new SmartFile(f);
        return new File(sf.path);
    }

    /**
     * Sanitize a path -- remove all relative path portions, i.e. dots
     * e.g. "/xxx/yyy/././././../yyy"  --> /xxx/yyy on UNIX, perhaps C:/xxx/yyy on Windows
     * Note that the main reason for this class is that on non-Windows,
     * getCanonicalXXX and getAbsoluteXXX might point at different files.
     * If the file is a soft link then the Canonical will be the file that is linked to.
     * The Absolute will be the link file itself.
     * This method will give you the benefits of Canonical -- but will always point
     * at the link file path itself.
     * @param filename The path to sanitize
     * @return The sanitized path
     */
    public static String sanitize(String filename) {
        SmartFile sf = new SmartFile(filename);
        return sf.path;
    }

    /**
     * Sanitize a "Classpath-like" list of Paths.
     * @param pathsString A string of paths, each separated by File.pathSeparator
     * @return The sanitized paths
     */
    public static String sanitizePaths(String pathsString) {
        if (!ok(pathsString)) {
            return pathsString;
        }

        try {
            String[] paths = pathsString.split(File.pathSeparator);
            StringBuilder sb = new StringBuilder();
            Set<String> pathsSet = new HashSet<>();
            List<String> pathsList = new LinkedList<>();

            for (String path2 : paths) {
                String path = path2;

                // ignore empty path elements.  E.g. "c:/foo;;;;;;;" should become "C:/foo"
                // not "c:/foo;thisdir;thisdir;thisdir etc"
                if (!ok(path)) {
                    continue;
                }

                // pathsSet is only here for removing duplicates.  We need the
                // List to maintain the original order!
                path = SmartFile.sanitize(path);

                if (pathsSet.add(path)) {
                    pathsList.add(path);
                }
            }

            boolean firstElement = true;
            for (String path : pathsList) {
                if (firstElement) {
                    firstElement = false;
                } else {
                    sb.append(File.pathSeparator);
                }

                sb.append(path);
            }
            return sb.toString();
        }
        catch (Exception e) {
            return pathsString;
        }
    }

    private SmartFile(File f) {
        if (f == null) {
            throw new NullPointerException();
        }

        convert(f.getAbsolutePath());
    }

    private SmartFile(String s) {
        if (s == null) {
            throw new NullPointerException();
        }

        // note that "" is a valid filename
        // IT 7500 get rid of quotes!!!
        s = StringUtils.removeEnclosingQuotes(s);
        convert(new File(s).getAbsolutePath());
    }

    private void convert(String oldPath) {
        if (GFLauncherUtils.isWindows()) {
            convertWindows(oldPath);
        } else {
            convertNix(oldPath);
        }
    }

    /*
     * There is no symlink issue with getCanonical vs getAbsolute
     * so we do it the EASY way here...
     */
    private void convertWindows(String oldPath) {
        try {
            path = new File(oldPath).getCanonicalPath();
            if (!path.startsWith("\\")) { // network address...
                path = path.replace('\\', '/');
            }
        }
        catch (IOException ex) {
            // what to do?  This has never happened to me and I use File I/O
            //** a lot **
            path = oldPath.replace('\\', '/');
        }
    }

    private void convertNix(String oldPath) {
        // guarantee -- the beginning will not have "." or ".."
        // (because of getAbsolutePath()...)
        char[] p = oldPath.toCharArray();

        int from, to;
        for (from = 0, to = 0; from < p.length; from++) {
            if (p[from] == '/' &&
                ((from + 3 < p.length &&
                  p[from+1] == '.' && p[from+2] == '.' && p[from+3] == '/') ||
                 (from + 3 == p.length &&
                 p[from+1] == '.' && p[from+2] == '.'))) {
                // remove the previous directory due to /../
                while (to > 0 && p[--to] != '/') {
                }
                from += 2;
            }
            else if (p[from] == '/' &&
                    ((from + 2 < p.length &&
                      p[from+1] == '.' && p[from+2] == '/') ||
                     (from + 2 == p.length &&
                      p[from+1] == '.'))) {
                // skip over /./
                from += 1;
            }
            else {
                p[to++] = p[from];
            }
        }
        if (to > 0 && p[to-1] == '/') {
            to -= 1;
        }
        path = new String(p, 0, to);
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private String path;
}
