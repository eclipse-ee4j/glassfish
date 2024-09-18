/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import java.net.URI;
import java.util.List;

/**
 * This interface allows us to customize various aspects of this class.
 * e.g., what should be used as location string while installing bundles,
 * what should be installed from a given directory, etc.
 */
interface BundleProvisionerCustomizer {
    /**
     * @param jar jar to be installed as bundle
     * @return Location that should be used while installing this jar as a bundle
     */
    String makeLocation(Jar jar);

    /**
     * Is this jar managed by us?
     *
     * @param jar
     * @return
     */
    boolean isManaged(Jar jar);

    /**
     * Return list of locations from where bundles are installed.
     *
     * @return
     */
    List<URI> getAutoInstallLocations();

    /**
     * Return list of locations from where bundles are started. This must be a subset of what is returned by
     * {@link #getAutoInstallLocations()}
     *
     * @return
     */
    List<URI> getAutoStartLocations();

    /**
     * Options used in Bundle.start().
     *
     * @return
     */
    int getStartOptions();

    /**
     * @param jar
     * @return start level of this bundle. -1 if not known
     */
    Integer getStartLevel(Jar jar);
}