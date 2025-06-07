/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.io.SmartFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * Package private static utility methods
 * @author bnevins
 */
public final class GFLauncherUtils {

    private GFLauncherUtils() {
        // all static methods
    }

    public static boolean ok(String s) {
        return s != null && !s.isEmpty();
    }

    public static boolean safeExists(File f) {
        return f != null && f.exists();
    }

    public static boolean safeIsDirectory(File f) {
        return f != null && f.isDirectory();
    }

    public static File getInstallDir() {
        try {
            return resolveInstallDir();
        } catch (IOException | URISyntaxException e) {
            throw new Error("Cannot resolve install root!", e);
        }
    }

    public static boolean isWindows() {
        String osname = System.getProperty("os.name");

        if (osname == null || osname.length() <= 0) {
            return false;
        }

        // case insensitive compare...
        osname = osname.toLowerCase(Locale.ENGLISH);

        if (osname.indexOf("windows") >= 0) {
            return true;
        }

        return false;
    }

    public static String replace(String s, String token, String replace) {
        if (s == null || s.isEmpty() || token == null || token.isEmpty()) {
            return s;
        }

        int index = s.indexOf(token);

        if (index < 0) {
            return s;
        }

        int tokenLength = token.length();
        String ret = s.substring(0, index);
        ret += replace;
        ret += s.substring(index + tokenLength);

        return ret;
    }

    /**
     * Makes an educated guess on whether an arbitrary string is a relative path.
     * If the string really is a path, it should be 100% accurate.
     * If it is an arbitrary string like, say, "hello/./world", then it will say
     * that it is a relative path.
     * @param path the path to check
     * @return true if the path is probably relative
     */
    public static boolean isRelativePath(String path) {
        if (!ok(path)) {
            return false;
        } else if (path.startsWith(".")) {
            return true;
        } else if (path.indexOf("/.") >= 0) {
            return true;
        } else if (path.indexOf("\\.") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convert a classpath like string, e.g. c:/a;c:/b, and convert to List of File
     * @param cp The classpath-like string
     * @return the list of File
     */
    public static List<File> stringToFiles(String cp) {
        List<File> list = new ArrayList<>();

        if (ok(cp)) {
            cp = SmartFile.sanitizePaths(cp);
            String[] ss = cp.split(File.pathSeparator);

            for (String s : ss) {
                list.add(new File(s));
            }
        }

        return list;
    }

    public static String fileListToPathString(List<File> files) {
        StringBuilder sb = new StringBuilder();
        boolean firstFile = true;

        for (File f : files) {
            if(firstFile) {
                firstFile = false;
            }
            else {
                sb.append(File.pathSeparatorChar);
            }
            // let's use forward slashes for neatness...
            sb.append(f.getPath().replace('\\', '/'));
        }
        return sb.toString();
    }

    private static File resolveInstallDir() throws IOException, URISyntaxException {
        // Property first, because it could be already set by this JVM and we respect it.
        String sys = System.getProperty(INSTALL_ROOT.getSystemPropertyName());
        if (sys != null) {
            return toValidDirectory(sys);
        }
        String env = System.getenv(INSTALL_ROOT.getEnvName());
        if (env != null) {
            return toValidDirectory(env);
        }
        // Expectation: we are running the program using usual directory design of GlassFish.
        // That means that the common-util jar is in modules directory.
        return new File(GFLauncherUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI())
            .getCanonicalFile().getParentFile().getParentFile();
    }

    private static File toValidDirectory(String value) throws IOException {
        File file = new File(value).toPath().normalize().toFile();
        if (!file.isAbsolute()) {
            throw new IOException("The path to install root is not an absolute path: " + file);
        }
        File canonical = file.getCanonicalFile();
        if (!canonical.isDirectory()) {
            throw new IOException("The path to install root is not a directory: " + canonical);
        }
        return canonical;
    }
}
