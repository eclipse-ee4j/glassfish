/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.glassfish.bootstrap.cfg;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public final class BootstrapKeys {
    // bootstrap properties = bsp
    public static final String PLATFORM_PROPERTY_KEY = "GlassFish_Platform";
    // bundle containing module startup, bsp
    public static final String GF_KERNEL = "org.glassfish.core.kernel";
    // bsp
    public static final String ORIGINAL_CP     = "-startup-classpath";
    public static final String ORIGINAL_CN     = "-startup-classname";
    public static final String ORIGINAL_ARGS   = "-startup-args";
    public static final String ARG_SEP         = ",,,";

    public static final String INSTANCE_ROOT_PROP_NAME = "com.sun.aas.instanceRoot";
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot";
    public static final String INSTALL_ROOT_URI_PROP_NAME = "com.sun.aas.installRootURI";
    public static final String INSTANCE_ROOT_URI_PROP_NAME = "com.sun.aas.instanceRootURI";
    public static final String HK2_CACHE_DIR = "com.sun.enterprise.hk2.cacheDir";
    public static final String INHABITANTS_CACHE = "inhabitants";
    public static final String BUILDER_NAME_PROPERTY = "GlassFish.BUILDER_NAME";
    public static final String NO_FORCED_SHUTDOWN = "--noforcedshutdown";

    private BootstrapKeys() {
    }
}
