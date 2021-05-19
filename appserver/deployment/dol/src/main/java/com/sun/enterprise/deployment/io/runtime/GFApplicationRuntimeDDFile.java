/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.io.runtime;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.hk2.api.PerLookup;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFileFor;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.node.RootXMLNode;
import com.sun.enterprise.deployment.node.runtime.application.gf.GFApplicationRuntimeNode;
import com.sun.enterprise.deployment.EarType;
import org.jvnet.hk2.annotations.Service;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for handling the XML configuration information
 * for the Glassfish Application Container
 */
@ConfigurationDeploymentDescriptorFileFor(EarType.ARCHIVE_TYPE)
@PerLookup
@Service
public class GFApplicationRuntimeDDFile extends ConfigurationDeploymentDescriptorFile {
    /**
     * @return the location of the DeploymentDescriptor file for a
     * particular type of J2EE Archive
     */
    public String getDeploymentDescriptorPath() {
        return DescriptorConstants.GF_APPLICATION_JAR_ENTRY;
    }

    /**
     * @return a RootXMLNode responsible for handling the deployment
     * descriptors associated with this J2EE module
     *
     * @param the descriptor for which we need the node
     */
    public RootXMLNode getRootXMLNode(Descriptor descriptor) {

        if (descriptor instanceof Application) {
            return new GFApplicationRuntimeNode((Application) descriptor);
        }
        return null;
    }

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
                               final Map<String, List<Class>> versionUpgrades) {
        rootNodesMap.put(GFApplicationRuntimeNode.registerBundle(publicIDToDTDMap), GFApplicationRuntimeNode.class);
    }
}
