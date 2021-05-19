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

package org.glassfish.ejb.deployment.io;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.node.runtime.GFEjbBundleRuntimeNode;
import org.glassfish.hk2.api.PerLookup;

import org.jvnet.hk2.annotations.Service;

import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFileFor;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.node.RootXMLNode;
import com.sun.enterprise.deployment.util.DOLUtils;

/**
 * This class is responsible for handling the XML configuration information
 * for the Glassfish EJB Container
 */
@ConfigurationDeploymentDescriptorFileFor(EjbContainerUtil.EJB_CONTAINER_NAME)
@Service
@PerLookup
public class GFEjbRuntimeDDFile extends ConfigurationDeploymentDescriptorFile {
    /**
     * @return the location of the DeploymentDescriptor file for a
     *         particular type of J2EE Archive
     */
    public String getDeploymentDescriptorPath() {
        return DOLUtils.warType().equals(getArchiveType()) ?
                DescriptorConstants.GF_EJB_IN_WAR_ENTRY : DescriptorConstants.GF_EJB_JAR_ENTRY;
    }

    /**
     * @param descriptor the descriptor for which we need the node
     * @return a RootXMLNode responsible for handling the deployment
     *         descriptors associated with this J2EE module
     */
    public RootXMLNode<EjbBundleDescriptorImpl> getRootXMLNode(Descriptor descriptor) {
        if (descriptor instanceof EjbBundleDescriptorImpl) {
            return new GFEjbBundleRuntimeNode((EjbBundleDescriptorImpl) descriptor);
        }
        return null;
    }
}
