/*
 * Copyright (c) 2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.perf.benchmark;


/**
 * Configuration of several critical values affecting performance and stability.
 */
public final class BenchmarkLimits {

    public static final int LIMIT_JMH_THREADS = 500;
    public static final int LIMIT_POOL_HTTP_THREADS = 1000;
    // FIXME: 4 reproduces the JDBC Pool error - pool contains just unusable connections!
    public static final int LIMIT_HTTP_REQUEST_TIMEOUT = 10;

    public static final int LIMIT_DBSERVER_CONNECTION_COUNT = 500;
    public static final int LIMIT_POOL_JDBC = 300;
    public static final int LIMIT_POOL_EJB = 500;
    public static final boolean ENABLE_CONNECTION_VALIDATION = false;

    public static final int MEM_MAX_APP_OS = 4;
    public static final int MEM_MAX_APP_HEAP = 3;
    public static final int MEM_MAX_DB_OS = 4;
    public static final int MEM_MAX_DB_SHARED = 4;

    public static final String SERVER_LOG_LEVEL = "INFO";

    private BenchmarkLimits() {
        // hidden
    }
}
