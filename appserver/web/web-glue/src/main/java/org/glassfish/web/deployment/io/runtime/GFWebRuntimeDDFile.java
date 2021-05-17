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

package org.glassfish.web.deployment.io.runtime;

import java.util.List;
import java.util.Map;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFileFor;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.node.RootXMLNode;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.web.WarType;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.deployment.node.runtime.gf.GFWebBundleRuntimeNode;

import org.jvnet.hk2.annotations.Service;

/**
 * This class is responsible for handling the XML configuration information
 * for the Glassfish Web Container
 */
@ConfigurationDeploymentDescriptorFileFor(WarType.ARCHIVE_TYPE)
@Service
@PerLookup
public class GFWebRuntimeDDFile extends ConfigurationDeploymentDescriptorFile {

    /**
     * @return the location of the DeploymentDescriptor file for a
     * particular type of J2EE Archive
     */
    @Override
    public String getDeploymentDescriptorPath() {
        return DescriptorConstants.GF_WEB_JAR_ENTRY;
    }

    /**
     * @return a RootXMLNode responsible for handling the deployment
     * descriptors associated with this J2EE module
     *
     * @param descriptor the descriptor for which we need the node
     */
    @Override
    public RootXMLNode getRootXMLNode(Descriptor descriptor) {

        if (descriptor instanceof WebBundleDescriptorImpl) {
            return new GFWebBundleRuntimeNode((WebBundleDescriptorImpl) descriptor);
        }
        return null;
    }

    @Override
    public void registerBundle(final Map<String, Class> registerMap,
                               final Map<String, String> publicIDToDTD,
                               final Map<String, List<Class>> versionUpgrades) {

        registerMap.put(GFWebBundleRuntimeNode.registerBundle(publicIDToDTD,
                                                              versionUpgrades),
                GFWebBundleRuntimeNode.class);
    }
}
