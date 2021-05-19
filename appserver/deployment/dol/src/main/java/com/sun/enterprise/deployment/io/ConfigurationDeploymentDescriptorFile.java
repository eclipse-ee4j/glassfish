/*
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

package com.sun.enterprise.deployment.io;

import java.util.List;
import java.util.Map;

import org.jvnet.hk2.annotations.Contract;

/**
 * This class is responsible for handling the XML configuration information
 * for the J2EE Reference Implementation runtime descriptors.
 *
 * @author Jerome Dochez
 */
@Contract
public abstract class ConfigurationDeploymentDescriptorFile extends DeploymentDescriptorFile {

    /**
     * Register the root node for this runtime deployment descriptor file
     * in the root nodes map, and also in the dtd map which will be used for
     * dtd validation.
     *
     * @param rootNodesMap the map for storing all the root nodes
     * @param publicIDToDTDMap the map for storing public id to dtd mapping
     * @param versionUpgrades The list of upgrades from older versions
     */
    public void registerBundle(final Map<String, Class> rootNodesMap,
                               final Map<String, String> publicIDToDTDMap,
                               final Map<String, List<Class>> versionUpgrades) {}

  /**
   * Return whether this configuration file can be validated.
   * @return whether this configuration file can be validated.
   */
  public boolean isValidating() {
    return false;
  }
}
