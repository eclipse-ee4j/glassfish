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

package com.sun.enterprise.glassfish.bootstrap.osgi;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class Constants {
    static final String BUNDLEIDS_FILENAME = "glassfish.bundleids";
    static final String PROVISIONING_OPTIONS_FILENAME = "provisioning.properties";
    static final String PROVISIONING_OPTIONS_PREFIX = "glassfish.osgi";
    /**
     * The property name for the auto processor's auto-install property.
     */
    static final String AUTO_INSTALL_PROP = PROVISIONING_OPTIONS_PREFIX + ".auto.install";
    /**
     * The property name for the auto processor's auto-start property.
     */
    static final String AUTO_START_PROP = PROVISIONING_OPTIONS_PREFIX + ".auto.start";
    /**
     * The property name for auto processor's auto-start options property
     * The value of this property is the integer argument to Bundle.start()
     */
    static final String AUTO_START_OPTIONS_PROP = PROVISIONING_OPTIONS_PREFIX + ".auto.start.options";
    /**
     * Prefix for the property name to specify bundle's start level
     */
    static final String AUTO_START_LEVEL_PROP = PROVISIONING_OPTIONS_PREFIX + ".auto.start.level";
    /**
     * The property name for final start level of framework
     */
    static final String FINAL_START_LEVEL_PROP = PROVISIONING_OPTIONS_PREFIX + ".start.level.final";
    /**
     * The property name to configure if bundles should be provisioned on demand.
     */
    static final String ONDEMAND_BUNDLE_PROVISIONING = "glassfish.osgi.ondemand";

    static final String FILE_SCHEME = "file";
}
