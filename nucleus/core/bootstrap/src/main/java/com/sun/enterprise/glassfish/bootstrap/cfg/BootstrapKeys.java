/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

    /** bundle containing module startup */
    public static final String GF_KERNEL = "org.glassfish.core.kernel";

    public static final String ASADMIN_MP = "-asadmin-modulepath";
    public static final String ASADMIN_CP = "-asadmin-classpath";
    public static final String ASADMIN_CN = "-asadmin-classname";
    public static final String ASADMIN_ARGS = "-asadmin-args";

    public static final String ORIGINAL_MP = "-startup-modulepath";
    public static final String ORIGINAL_CP = "-startup-classpath";
    public static final String ORIGINAL_CN = "-startup-classname";
    public static final String ORIGINAL_ARGS = "-startup-args";
    public static final String ARG_SEP = ",,,";

    public static final String INSTALL_ROOT_URI_PROP_NAME = "com.sun.aas.installRootURI";
    public static final String INSTANCE_ROOT_URI_PROP_NAME = "com.sun.aas.instanceRootURI";
    public static final String HK2_CACHE_DIR = "com.sun.enterprise.hk2.cacheDir";
    public static final String INHABITANTS_CACHE = "inhabitants";
    public static final String BUILDER_NAME_PROPERTY = "GlassFish.BUILDER_NAME";
    public static final String NO_FORCED_SHUTDOWN = "--noforcedshutdown";

    public static final String BUNDLEIDS_FILENAME = "glassfish.bundleids";
    public static final String PROVISIONING_OPTIONS_FILENAME = "provisioning.properties";
    public static final String PROVISIONING_OPTIONS_PREFIX = "glassfish.osgi";
    /**
     * The property name for the auto processor's auto-install property.
     */
    public static final String AUTO_INSTALL_PROP = PROVISIONING_OPTIONS_PREFIX + ".auto.install";
    /**
     * The property name for the auto processor's auto-start property.
     */
    public static final String AUTO_START_PROP = PROVISIONING_OPTIONS_PREFIX + ".auto.start";
    /**
     * The property name for auto processor's auto-start options property
     * The value of this property is the integer argument to Bundle.start()
     */
    public static final String AUTO_START_OPTIONS_PROP = PROVISIONING_OPTIONS_PREFIX + ".auto.start.options";
    /**
     * Prefix for the property name to specify bundle's start level
     */
    public static final String AUTO_START_LEVEL_PROP = PROVISIONING_OPTIONS_PREFIX + ".auto.start.level";
    /**
     * The property name for final start level of framework
     */
    public static final String FINAL_START_LEVEL_PROP = PROVISIONING_OPTIONS_PREFIX + ".start.level.final";
    /**
     * The property name to configure if bundles should be provisioned on demand.
     */
    public static final String ONDEMAND_BUNDLE_PROVISIONING = "glassfish.osgi.ondemand";

    public static final String FILE_SCHEME = "file";

    public static final String AUTO_DELETE = "org.glassfish.embeddable.autoDelete";

    // Following constants were copy-pasted from com.sun.enterprise.module.bootstrap.StartupContext
    // to avoid dependency on HK2 jar files.
    public final static String TIME_ZERO_NAME = "__time_zero";
    public final static String STARTUP_MODULE_NAME = "hk2.startup.context.mainModule";
    public final static String STARTUP_MODULESTARTUP_NAME = "hk2.startup.context.moduleStartup";


    private BootstrapKeys() {
    }
}
