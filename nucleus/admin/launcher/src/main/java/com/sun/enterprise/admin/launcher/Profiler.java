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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sun.enterprise.universal.glassfish.GFLauncherUtils.ok;
import static com.sun.enterprise.universal.glassfish.GFLauncherUtils.stringToFiles;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * This class wraps the profiler element in java-config Note that the V2 dtd says that there can be generic property
 * elements in the profiler element. I don't know why anyone would use them -- but if they do I turn it into a "-D"
 * System Property
 *
 * @author Byron Nevins
 */
public class Profiler {

    Map<String, String> config;
    private boolean enabled;
    List<String> jvmOptions;

    Profiler(Map<String, String> config, List<String> jvmOptions, Map<String, String> sysProps) {
        this.config = config;
        enabled = Boolean.parseBoolean(this.config.get("enabled"));
        this.jvmOptions = jvmOptions;
        jvmOptions.addAll(getPropertiesAsJvmOptions(sysProps));
    }

    List<String> getJvmOptions() {
        if (!enabled) {
            return emptyList();
        }
        return jvmOptions;
    }

    Map<String, String> getConfig() {
        if (!enabled) {
            return emptyMap();
        }
        return config;
    }

    List<File> getClasspath() {
        if (!enabled) {
            return emptyList();
        }

        String cp = config.get("classpath");

        if (ok(cp)) {
            return stringToFiles(cp);
        }

        return emptyList();
    }

    List<File> getNativePath() {
        if (!enabled) {
            return emptyList();
        }

        String cp = config.get("native-library-path");

        if (ok(cp)) {
            return stringToFiles(cp);
        }

        return emptyList();
    }

    boolean isEnabled() {
        return enabled;
    }

    private List<String> getPropertiesAsJvmOptions(Map<String, String> props) {
        List<String> list = new ArrayList<>();
        Set<Map.Entry<String, String>> entries = props.entrySet();

        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();

            if (value != null) {
                list.add("-D" + name + "=" + value);
            } else {
                list.add("-D" + name);
            }
        }

        return list;
    }

}
