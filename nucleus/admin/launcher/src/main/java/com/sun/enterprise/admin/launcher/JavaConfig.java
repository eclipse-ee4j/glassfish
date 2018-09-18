/*
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

package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.glassfish.GFLauncherUtils;
import static com.sun.enterprise.admin.launcher.GFLauncherConstants.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author bnevins
 */
class JavaConfig {

    JavaConfig(Map<String, String> map) {
        this.map = map;
    }

    Map<String, String> getMap() {
        return map;
    }

    String getJavaHome() {
        return map.get("java-home");
    }

    List<File> getEnvClasspath() {
        if(useEnvClasspath()) {
            String s = System.getenv("CLASSPATH");
            s = stripQuotes(s);
            return GFLauncherUtils.stringToFiles(s);
        }
        else {
            return new ArrayList<File>();
        }
    }

    List<File> getPrefixClasspath() {
        String cp = map.get("classpath-prefix");

        if(GFLauncherUtils.ok(cp)) {
            return GFLauncherUtils.stringToFiles(cp);
        }
        else {
            return new ArrayList<File>();
        }
    }

    String getNativeLibraryPrefix() {
        String s = map.get(NATIVE_LIB_PREFIX);

        if(!GFLauncherUtils.ok(s))
            s = "";

        return s;
    }


    List<File> getSuffixClasspath() {
        String cp = map.get("classpath-suffix");

        if(GFLauncherUtils.ok(cp)) {
            return GFLauncherUtils.stringToFiles(cp);
        }
        else {
            return new ArrayList<File>();
        }
    }

    String getNativeLibrarySuffix() {
        String s = map.get(NATIVE_LIB_SUFFIX);

        if(!GFLauncherUtils.ok(s))
            s = "";

        return s;
    }

    List<File> getSystemClasspath() {
        String cp = map.get("system-classpath");

        if(GFLauncherUtils.ok(cp)) {
            return GFLauncherUtils.stringToFiles(cp);
        }
        else {
            return new ArrayList<File>();
        }
    }

    List<String> getDebugOptions() {
        // we MUST break this up into the total number of -X commands (currently 2),
        // Since our final command line is a List<String>, we can't have 2
        // options in one String -- the JVM will ignore the second option...
        // sample "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9999"
        List<String> empty = Collections.emptyList();
        String s = map.get("debug-options");

        if(!GFLauncherUtils.ok(s)) {
            return empty;
        }
        String[] ss = s.split(" ");

        if(ss.length <= 0) {
            return empty;
        }
        return Arrays.asList(ss);
    }

    boolean isDebugEnabled() {
        return Boolean.parseBoolean(map.get("debug-enabled"));
    }


    private boolean useEnvClasspath() {
        String s = map.get("env-classpath-ignored");

        // the default is true for *ignoring* which means
        // the default is *false* for using (yikes!)
        // If there is no value -- return false
        // else use the opposite of whatever the value is

        if(s == null || s.length() <= 0)
            return false;

        return !Boolean.parseBoolean(s);
    }

    private String stripQuotes(String s) {
        // IT 7500
        // if the CLASSPATH has "C:/foo goo" with actual double-quotes
        // the server will not start.
        // It is not allowed to have a classpath filename that contains quote characters.
        // Here we just mindlessly remove such characters.
        // It looks inefficient but it is incredibly rare for the CLASSPATH to be enabled
        // and for there to be an embedded quote character especially since we give
        // a SEVERE error message everytime.

        if(!hasQuotes(s))
            return s;

        String s2 = stripChar(s, "'");
        s2 = stripChar(s2, "\"");
        GFLauncherLogger.severe(GFLauncherLogger.NO_QUOTES_ALLOWED, s, s2);

        return s2;
    }

    private boolean hasQuotes(String s) {
        if(s == null)
            return false;

        if(s.indexOf('\'') >= 0)
            return true;

        return s.indexOf('"') >= 0;
    }

    private String stripChar(String s, String c) {
        String[] ss = s.split(c);

        StringBuilder sb = new StringBuilder();

        for(String s2 : ss)
            sb.append(s2);

        return sb.toString();
    }

    private Map<String, String> map;
}
/*
 * Sample java-config from a V2 domain.xml
 *  <java-config
        classpath-suffix=""
        debug-enabled="false"
        debug-options="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009"
        env-classpath-ignored="true"
        java-home="${com.sun.aas.javaRoot}"
        javac-options="-g"
        rmic-options="-iiop -poa -alwaysgenerate -keepgenerated -g"
        system-classpath="">
 * */
