/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.deployment;

/**
 * MetaData associated with a Deployer. This is used by the deployment layers to identify the special requirements of
 * the Deployer.
 *
 * Supported Requirements : invalidatesClassLoader Deployer can load classes that need to be reloaded for the
 * application to run successfully hence requiring the class loader to be flushed and reinitialized between the prepare
 * and load phase. componentAPIs Components can use APIs that are defined outside of the component's bundle. These
 * component's APIs (eg. Jakarta EE APIs) must be imported by the application class loader before any application code is
 * loaded.
 */
public class MetaData {

    private static final Class<?>[] empty = new Class[0];

    private final boolean invalidatesCL;
    private final Class<?>[] requires;
    private final Class<?>[] provides;

    /**
     * Constructor for the Deployer's metadata
     *
     * @param invalidatesClassLoader If true, invalidates the class loader used during the deployment's prepare phase
     */
    public MetaData(boolean invalidatesClassLoader, Class<?>[] provides, Class<?>[] requires) {
        this.invalidatesCL = invalidatesClassLoader;
        this.provides = provides;
        this.requires = requires;
    }

    /**
     * Returns whether or not the class loader is invalidated by the Deployer's propare phase.
     *
     * @return true if the class loader is invalid after the Deployer's prepare phase call.
     */
    public boolean invalidatesClassLoader() {
        return invalidatesCL;
    }

    /**
     * Returns the list of types of metadata this deployer will provide to the deployement context upon the successful
     * completion of the prepare method.
     *
     * @return list of metadata type;
     */
    public Class<?>[] provides() {
        if (provides == null) {
            return empty;
        }
        return provides;
    }

    /**
     * Returns the list of types of metadata this deployer will optionally require to run successfully the prepare method.
     * If no provider provides the type, the metadata will not be available. If any provider provides the type,
     * that provider will be loaded before the provider that requires it.
     *
     * @return list of types of optionally required metadata
     */
    public Class<?>[] requires() {
        if (requires == null) {
            return empty;
        }
        return requires;
    }
}
