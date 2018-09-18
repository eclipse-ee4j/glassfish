/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package admin.monitoring;

import java.io.*;

/**
 * @author Byron Nevins
 */
class Constants {
    static final String DOMAIN_NAME = "mon-domain";
    static final String CLUSTER_NAME = "mon-cluster";
    static final String CLUSTERED_INSTANCE_NAME1 = "clustered-i1";
    static final String CLUSTERED_INSTANCE_NAME2 = "clustered-i2";
    static final String STAND_ALONE_INSTANCE_NAME = "standalone-i3";
    static final String NO_DATA = "No monitoring data to report.";
    final static String HIGH = "HIGH";
    final static String LOW = "LOW";
    final static String OFF = "OFF";
    final static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    final static File installDir = new File(System.getenv("S1AS_HOME"));

    // this is annoying!!!
    final static String STAR = isWindows ? "\"*\"" : "*";
    final static String SERVERDOTSTAR = isWindows ? "\"server.*\"" : "server.*";

    final static String[] INSTANCES = new String[]{
        CLUSTERED_INSTANCE_NAME1,
        CLUSTERED_INSTANCE_NAME2,
        STAND_ALONE_INSTANCE_NAME
    };
    final static String[] CONFIG_NAMES = new String[]{
        CLUSTER_NAME,
        STAND_ALONE_INSTANCE_NAME,
        "server"
    };
    final static String[] LEVELS = new String[]{
        OFF, LOW, HIGH
    };

    final static String MON_CATEGORIES[] = new String[]{
        "http-service",
        "connector-connection-pool",
        "connector-service",
        "deployment",
        "ejb-container",
        "jdbc-connection-pool",
        "jersey",
        "jms-service",
        "jpa",
        "jvm",
        "orb",
        "security",
        "thread-pool",
        "transaction-service",
        "web-container",
        "web-services-container"
    };
    final static File RESOURCES_DIR = new File("resources").getAbsoluteFile();
    final static File BUILT_RESOURCES_DIR = new File("apps").getAbsoluteFile();
}
