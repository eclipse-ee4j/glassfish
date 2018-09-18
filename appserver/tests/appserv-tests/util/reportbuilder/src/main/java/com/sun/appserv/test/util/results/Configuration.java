/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.test.util.results;

public class Configuration {
    private String os;
    private String jdkVersion;
    private String machineName;

    public Configuration() {
    }

    public Configuration(final String os, final String jdkVersion, final String machineName) {
        this.os = os;
        this.jdkVersion = jdkVersion;
        this.machineName = machineName;
    }

    @Override
    public String toString() {
        return "Configuration{" +
            "jdkVersion='" + jdkVersion + '\'' +
            ", os='" + os + '\'' +
            ", machineName='" + machineName + '\'' +
            '}';
    }

    public String toHtml() {
        return "<table>"
            + "<tr><th colspan=2>Configuration Information</th></tr>"
            + ReportHandler.row(null, "td", "Machine Name", machineName)
            + ReportHandler.row(null, "td", "OS", os)
            + ReportHandler.row(null, "td", "JDK Version", jdkVersion)
            + "</table>";
    }
}
