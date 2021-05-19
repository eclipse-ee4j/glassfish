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

package org.glassfish.config.support;

/**
 * Shared constants for config-beans packages
 *
 * @author Byron Nevins
 */

public class Constants {
    static final String SERVERS = "servers";
    static final String SERVER = "server";
    static final String CLUSTERS = "clusters";
    static final String CLUSTER = "cluster";
    static final String REF = "ref";
    static final String SERVER_REF = "server-ref";
    static final String CONFIG = "config";
    static final String CONFIGS = "configs";
    static final String CONFIG_REF = "config-ref";
    static final String NAME = "name";
    public static final String NAME_REGEX = "[A-Za-z0-9_][A-Za-z0-9\\-_\\./;#]*";
    public static final String NAME_SERVER_REGEX = "[A-Za-z0-9_][A-Za-z0-9\\-_\\.;]*";
    public static final String NAME_APP_REGEX = "[A-Za-z0-9_][A-Za-z0-9\\-_\\./;:#]*";
    public static final String DEFAULT_MAX_POOL_SIZE = "32";
    public static final String DEFAULT_STEADY_POOL_SIZE = "8";
}
