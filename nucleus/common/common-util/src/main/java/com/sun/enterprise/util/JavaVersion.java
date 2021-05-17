/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class has been copied from open installer
 * @author yamini
 */
public class JavaVersion {

    public static JavaVersion getVersion(String string) {
        // convert 1.7.0-b10 to 1.7.0.0.10
        if (string.matches(
                "[0-9]+\\.[0-9]+\\.[0-9]+-b[0-9]+")) {
          string = string.replace("-b", ".0.");
        }

        // convert 1.7.0_01-b10 to 1.7.0_01.10
        if (string.matches(
                "[0-9]+\\.[0-9]+\\.[0-9]+_[0-9]+-b[0-9]+")) {
          string = string.replace("-b", ".");
        }

        if (string.matches(
                "[0-9]+\\.[0-9]+\\.[0-9]+_[0-9]+-ea")) {
          string = string.replace("-ea", ".");
        }

        // and create the version
        final Matcher matcher = Pattern.
                compile("[0-9]+\\.[0-9]+[0-9_\\.\\-]+").
                matcher(string);

        if (matcher.find()) {
            return new JavaVersion(matcher.group());
        } else {
            return null;
        }
    }
    private long major;
    private long minor;
    private long micro;
    private long update;
    private long build;

    public JavaVersion(String string) {
        String[] split = string.split("[\\._\\-]+");

        if (split.length > 0) {
            major = Long.parseLong(split[0]);
        }
        if (split.length > 1) {
            minor = Long.parseLong(split[1]);
        }
        if (split.length > 2) {
            micro = Long.parseLong(split[2]);
        }
        if (split.length > 3) {
            update = Long.parseLong(split[3]);
        }
        if (split.length > 4) {
            build = Long.parseLong(split[4]);
        }
    }

    public boolean newerThan(JavaVersion version) {
        if (getMajor() > version.getMajor())
            return true;
        if (getMajor() < version.getMajor())
            return false;

        // majors are equal, so compare minors
        if (getMinor() > version.getMinor())
            return true;
        if (getMinor() < version.getMinor())
            return false;

        // minors are equal, so compare micros
        if (getMicro() > version.getMicro())
            return true;
        if (getMicro() < version.getMicro())
            return false;

        // micros are equal, so compare updates
        if (getUpdate() > version.getUpdate())
            return true;
        if (getUpdate() < version.getUpdate())
            return false;

        // updates are equal, so compare builds
        if (getBuild() > version.getBuild())
            return true;
        if (getBuild() < version.getBuild())
            return true;

        return false;
    }

    public boolean newerOrEquals(JavaVersion version) {
        return newerThan(version) || equals(version);
    }

    public boolean olderThan(JavaVersion version) {
        return !newerOrEquals(version);
    }

    public boolean olderOrEquals(JavaVersion version) {
        return !newerThan(version);
    }

    public long getMajor() {
        return major;
    }

    public long getMinor() {
        return minor;
    }

    public long getMicro() {
        return micro;
    }

    public long getUpdate() {
        return update;
    }

    public long getBuild() {
        return build;
    }

    public String toMinor() {
        return "" + major + "." + minor;
    }

    public String toJdkStyle() {
        return "" + major
                + "." + minor
                + "." + micro
                + (update != 0 ? "_" + (update < 10 ? "0" + update : update) : "");
    }
}
