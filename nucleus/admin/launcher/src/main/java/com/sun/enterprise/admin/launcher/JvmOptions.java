/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.enterprise.util.StringUtils.ok;

/**
 *
 * @author bnevins
 */
class JvmOptions {

    Map<String, String> sysProps = new LinkedHashMap<>();
    Map<String, String> xxProps = new LinkedHashMap<>();
    Map<String, String> xProps = new LinkedHashMap<>();
    Map<String, String> longProps = new LinkedHashMap<>();
    Map<String, String> plainProps = new LinkedHashMap<>();
    int osgiPort = -1;

    JvmOptions(List<String> options) throws GFLauncherException {
        // We get them from domain.xml as a list of Strings
        // -Dx=y -Dxx -XXfoo -XXgoo=zzz -client -server
        // Issue 4434 -- we might get a jvm-option like this:
        // <jvm-options>"-xxxxxx"</jvm-options> notice the literal double-quotes

        for (String s : options) {
            s = StringUtils.removeEnclosingQuotes(s);

            if (s.startsWith("-D")) {
                addSysProp(s);
            } else if (s.startsWith("-XX")) {
                addXxProp(s);
            } else if (s.startsWith("-X")) {
                addXProp(s);
            } else if (s.startsWith("--")) {
                addLongProp(s);
            } else if (s.startsWith("-")) {
                addPlainProp(s);
            } else {
                throw new GFLauncherException("UnknownJvmOptionFormat", s);
            }
        }

        filter();
        setOsgiPort();
    }

    @Override
    public String toString() {
        List<String> options = toList();
        StringBuilder sb = new StringBuilder();
        for (String option : options) {
            sb.append(option).append('\n');
        }

        return sb.toString();
    }

    List<String> toList() {
        List<String> options = new ArrayList<>();

        for (Map.Entry<String, String> entry : xxProps.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                options.add("-XX" + entry.getKey() + "=" + value);
            } else {
                options.add("-XX" + entry.getKey());
            }
        }

        for (Map.Entry<String, String> entry : xProps.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                options.add("-X" + entry.getKey() + "=" + value);
            } else {
                options.add("-X" + entry.getKey());
            }
        }

        for (Map.Entry<String, String> entry : longProps.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                options.add("--" + entry.getKey() + "=" + value);
            } else {
                options.add("--" + entry.getKey());
            }
        }

        for (Map.Entry<String, String> entry : plainProps.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                options.add("-" + entry.getKey() + "=" + value);
            } else {
                options.add("-" + entry.getKey());
            }
        }

        for (Map.Entry<String, String> entry : sysProps.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                options.add("-D" + entry.getKey() + "=" + value);
            } else {
                options.add("-D" + entry.getKey());
            }
        }

        return postProcessOrdering(options);
    }

    Map<String, String> getCombinedMap() {
        // used for resolving tokens
        Map<String, String> all = new HashMap<>(plainProps);
        all.putAll(xProps);
        all.putAll(xxProps);
        all.putAll(sysProps);
        return all;
    }

    int getOsgiPort() {
        return osgiPort;
    }

    private void addLongProp(String s) {
        s = s.substring(2);
        CompoundNameValue nv = new CompoundNameValue(s);
        longProps.put(nv.name, nv.value);
    }

    private void addPlainProp(String s) {
        s = s.substring(1);
        NameValue nv = new NameValue(s);
        plainProps.put(nv.name, nv.value);
    }

    private void addSysProp(String s) {
        s = s.substring(2);
        NameValue nv = new NameValue(s);
        sysProps.put(nv.name, nv.value);
    }

    private void addXProp(String s) {
        s = s.substring(2);
        NameValue nv = new NameValue(s);
        xProps.put(nv.name, nv.value);
    }

    private void addXxProp(String s) {
        s = s.substring(3);
        NameValue nv = new NameValue(s);
        xxProps.put(nv.name, nv.value);
    }

    private List<String> postProcessOrdering(List<String> unsorted) {
        /*
         * (1) JVM has one known order dependency. If these 3 are here, then unlock MUST appear first in the list
         * -XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:LogFile=D:/as/domains/domain1/logs/jvm.log
         *
         * June 2012 http://java.net/jira/browse/GLASSFISH-18777 JFR needs UnlockCommercialFeatures -- it is also
         * order-dependent. New algorithm -- put -XX:+Unlock* first
         *
         * (2) TODO Get the name of the instance early. We no longer send in the instanceRoot as an arg so -- ????
         */

        // go through the list hunting for the magic string. If such a string is
        // found then move it to the top. In June 2012 I changed this to a less
        // efficient but much more robust and simple algorithm...

        List<String> sorted = new ArrayList<>(unsorted.size());

        for (String s : unsorted) {
            if (hasMagic(s)) {
                sorted.add(s);
            }
        }

        for (String s : unsorted) {
            if (!hasMagic(s)) {
                sorted.add(s);
            }
        }

        return sorted;
    }

    private boolean hasMagic(String s) {
        final String magic = "-XX:+Unlock";
        return s != null && s.startsWith(magic);
    }


    /**
     * Filters out unwanted properties and filters in interested properties that may need to be
     * present by default in certain environments (OS, vm.vendor)
     * <p>
     * bnevins September 2009 There may be System Properties from V2 that cause havoc. E.g. the
     * MBean Server sys prop from V2 will be removed by upgrade code in the server but the server
     * will blow up before it starts with a CNFE! We need to remove it carefully. I.e. the user may
     * want to set up their own MBean Server Factory so we just check to see if the value is
     * identical to the V2 class...
     */
    private void filter() {
        // there is only one forbidden sys prop now so no need yet for fancy
        // data structures to contain the one key/value

        // I have seen these 2 values:
        // com.sun.enterprise.admin.server.core.jmx.AppServerMBeanServerBuilder
        // com.sun.enterprise.ee.admin.AppServerMBeanServerBuilder

        final String key = "javax.management.builder.initial";
        final String forbiddenStart = "com.sun.enterprise";
        final String forbiddenEnd = "AppServerMBeanServerBuilder";

        String val = sysProps.get(key);

        if (val != null && val.startsWith(forbiddenStart) && val.endsWith(forbiddenEnd)) {
            sysProps.remove(key);
        }

        if (OS.isDarwin() && System.getProperty("java.vm.vendor").equals("Apple Inc.")) {
            // on Mac OS, unless the property is specified in the domain.xml, we add
            // the -d32 flag to start the JVM in 32 bits mode
            Pattern pattern = Pattern.compile("d\\d+");
            boolean settingPresent = false;
            for (String propName : plainProps.keySet()) {
                Matcher m = pattern.matcher(propName);
                if (m.matches()) {
                    settingPresent = true;
                }
            }
            if (!settingPresent) {
                addPlainProp("-d32");
            }
        }
    }

    private void setOsgiPort() {
        String s = sysProps.get("osgi.shell.telnet.port");

        // not configured
        if (!ok(s)) {
            return;
        }

        try {
            osgiPort = Integer.parseInt(s);
        } catch (Exception e) {
            // already handled -- it is already set to -1
        }
    }

    // NameValue assumes that for a <name>=<value> pair, the <name> is always
    // the portion of the string before the first equals sign. This also allows the <value> to
    // contain one or more <key>=<value> pairs (if required).
    private static class NameValue {

        NameValue(String s) {
            int index = s.indexOf("=");

            if (index < 0) {
                name = s;
            } else {
                name = s.substring(0, index);
                if (index + 1 < s.length()) {
                    value = s.substring(index + 1);
                }
            }
        }

        private String name;
        private String value;
    }

    // For Java 9, options must also be supported of the forms:
    //   <option>=<module[/package]>=<value>
    //   <option> <module[/package]>=<value>
    // These forms are now handled by CompoundNameValue, which combines <option> and <module[/package]>
    // into a single <name> key.
    private static class CompoundNameValue {

        CompoundNameValue(String s) {
            int index = Math.min(s.indexOf("="), s.indexOf(" "));
            if (index < 0) {
                name = s;
            } else {
                int index2 = s.indexOf("=", index + 1);
                if (index2 >= 0) {
                    // There is a later equals sign, which implies <option>=<module>=<value>
                    // If the original match was a space, change it to an equals character instead
                    // so that this is treated as a single argument in the output string.
                    if (s.charAt(index) == ' ') {
                        s = s.replaceFirst(" ", "=");
                    }
                    index = index2;
                }
                name = s.substring(0, index);
                if (index + 1 < s.length()) {
                    value = s.substring(index + 1);
                }
            }
        }

        private String name;
        private String value;
    }
}
