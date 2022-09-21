/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EarType;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFileFor;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.node.runtime.RuntimeBundleNode;
import com.sun.enterprise.deployment.node.runtime.application.wls.WeblogicApplicationNode;

import java.util.List;
import java.util.Map;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * This class is responsible for handling the XML configuration information
 * for the WebLogic Application Container
 *
 */
@ConfigurationDeploymentDescriptorFileFor(EarType.ARCHIVE_TYPE)
@PerLookup
@Service
public class WLSApplicationRuntimeDDFile extends ConfigurationDeploymentDescriptorFile<Application> {

    @Override
    public String getDeploymentDescriptorPath() {
        return DescriptorConstants.WLS_APPLICATION_JAR_ENTRY;
    }


    @Override
    public RuntimeBundleNode<Application> getRootXMLNode(Descriptor descriptor) {
        if (descriptor instanceof Application) {
            Application application = (Application) descriptor;
            RuntimeBundleNode<Application> node = (RuntimeBundleNode<Application>) application
                .getRootNode(getDeploymentDescriptorPath());
            if (node == null) {
                node = new WeblogicApplicationNode(application);
                application.addRootNode(getDeploymentDescriptorPath(), node);
            }
            return node;
        }
        return new WeblogicApplicationNode();
    }

    /**
     * Register the root node for this runtime deployment descriptor file
     * in the root nodes map, and also in the dtd map which will be used for
     * dtd validation.
     *
     * @param rootNodesMap the map for storing all the root nodes
     * @param publicIDToDTD the map for storing public id to dtd mapping
     * @param versionUpgrades The list of upgrades from older versions
     */
    @Override
    public void registerBundle(
        Map<String, Class<?>> rootNodesMap,
        Map<String, String> publicIDToDTD,
        Map<String, List<Class<?>>> versionUpgrades
    ) {
        String bundle = WeblogicApplicationNode.registerBundle(publicIDToDTD, versionUpgrades);
        rootNodesMap.put(bundle, WeblogicApplicationNode.class);
    }


    /**
     * @return whether this configuration file can be validated.
     */
    @Override
    public boolean isValidating() {
        return true;
    }
}
