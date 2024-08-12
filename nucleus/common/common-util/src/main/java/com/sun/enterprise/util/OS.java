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

/*
 * OS.java
 *
 * Created on December 8, 2001, 5:48 PM
 */

package com.sun.enterprise.util;

import java.io.File;
import java.util.Locale;

/**
 *
 * @author  bnevins
 * @version
 */
public class OS
{

    private OS()
    {
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isWindows()
    {
        return File.separatorChar == '\\';
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isUNIX()
    {
        return File.separatorChar == '/';
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isUnix()
    {
        // convenience method...
        return isUNIX();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isSun()
    {
        return isName("sun");
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isSolaris10()
    {
        return isSun() && isVersion("5.10");
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isSunSparc()
    {
        return isName("sun") && isArch("sparc");
    }
    ///////////////////////////////////////////////////////////////////////////

    public static boolean isSunX86()
    {
        return isName("sun") && isArch("x86");
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isLinux()
    {
        return isName("linux");
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isDarwin()
    {
        return isName("Mac OS X");
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isWindowsForSure()
    {
        return isName("windows") && isWindows();
    }
    ///////////////////////////////////////////////////////////////////////////
    //  There are 10 known Linux versions!
    ///////////////////////////////////////////////////////////////////////////

    public static boolean isDebianLinux()
    {
        return isLinux() && new File("/etc/debian_version").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isFedoraLinux()
    {
        return isLinux() && new File("/etc/fedora-release").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isGentooLinux()
    {
        return isLinux() && new File("/etc/gentoo-release").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isKnoppixLinux()
    {
        return isLinux() && new File("/etc/knoppix_version").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isMandrakeLinux()
    {
        return isLinux() && new File("/etc/mandrake-release").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isMandrivaLinux()
    {
        return isLinux() && new File("/etc/mandriva-release").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isRedHatLinux()
    {
        return isLinux() && new File("/etc/redhat-release").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isSlackwareLinux()
    {
        return isLinux() && new File("/etc/slackware-version").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isSuSELinux()
    {
        return isLinux() && new File("/etc/SuSE-release").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isUbuntuLinux()
    {
        return isLinux() && new File("/etc/lsb-release").exists();
    }

    ///////////////////////////////////////////////////////////////////////////

    public static boolean isAix()
    {
        return isName("AIX");
    }

    ///////////////////////////////////////////////////////////////////////////

    private static boolean isArch(String name)
    {
        String archname = System.getProperty("os.arch");

        if(archname == null || archname.length() <= 0)
            return false;

        // case insensitive compare...
        archname= archname.toLowerCase(Locale.getDefault());
        name= name.toLowerCase(Locale.getDefault());

        if(archname.indexOf(name) >= 0)
            return true;

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////

    private static boolean isName(String name)
    {
        String osname = System.getProperty("os.name");

        if(osname == null || osname.length() <= 0)
            return false;

        // case insensitive compare...
        osname        = osname.toLowerCase(Locale.getDefault());
        name        = name.toLowerCase(Locale.getDefault());

        if(osname.indexOf(name) >= 0)
            return true;

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////

    private static boolean isVersion(String version)
    {
        String osversion = System.getProperty("os.version");

        if(osversion == null || osversion.length() <= 0 || version == null || version.length() <= 0 )
            return false;

        if(osversion.equals(version))
            return true;

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static final String WINDOWS_BATCH_FILE_EXTENSION = ".bat";

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        System.out.println("os.version = "                  + System.getProperty("os.version"));
        System.out.println("os.name = "                     + System.getProperty("os.name"));
        System.out.println("os.arch = "                     + System.getProperty("os.arch"));
        System.out.println("isUNIX() returned: "            + isUNIX());
        System.out.println("isWindows() returned: "         + isWindows());
        System.out.println("isWindowsForSure() returned: "  + isWindowsForSure());
        System.out.println("isSun() returned: "             + isSun());
        System.out.println("isLinux() returned: "           + isLinux());
        System.out.println("isDebianLinux() returned: "     + isDebianLinux());
        System.out.println("isFedoraLinux() returned: "     + isFedoraLinux());
        System.out.println("isGentooLinux() returned: "     + isGentooLinux());
        System.out.println("isKnoppixLinux() returned: "    + isKnoppixLinux());
        System.out.println("isMandrakeLinux() returned: "   + isMandrakeLinux());
        System.out.println("isMandrivaLinux() returned: "   + isMandrivaLinux());
        System.out.println("isRedHatLinux() returned: "     + isRedHatLinux());
        System.out.println("isSlackwareLinux() returned: "  + isSlackwareLinux());
        System.out.println("isSuSELinux() returned: "       + isSuSELinux());
        System.out.println("isUbuntuLinux() returned: "     + isUbuntuLinux());
        System.out.println("isSunX86() returned: "          + isSunX86());
        System.out.println("isSunSparc() returned: "        + isSunSparc());
        System.out.println("isDarwin() returned: "          + isDarwin());
        System.out.println("isSolaris10() returned: "       + isSolaris10());
        System.out.println("isAix() returned: "             + isAix());
    }
}
