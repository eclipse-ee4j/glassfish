/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.io;

import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFileFor;
import com.sun.enterprise.deployment.io.DescriptorConstants;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.node.runtime.EjbBundleRuntimeNode;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.util.DOLUtils.scatteredWarType;
import static com.sun.enterprise.deployment.util.DOLUtils.warType;


/**
 * This class is responsible for handling the XML configuration information
 * for the SunOne AppServer Ejb Container
 *
 * @author Jerome Dochez
 */
@ConfigurationDeploymentDescriptorFileFor(EjbContainerUtil.EJB_CONTAINER_NAME)
@Service
@PerLookup
public class EjbRuntimeDDFile extends ConfigurationDeploymentDescriptorFile<EjbBundleDescriptorImpl>  {

    /**
     * @return the location of the DeploymentDescriptor file for a
     *         particular type of J2EE Archive
     */
    @Override
    public String getDeploymentDescriptorPath() {
        return warType().equals(getArchiveType()) || scatteredWarType().equals(getArchiveType())
            ? DescriptorConstants.S1AS_EJB_IN_WAR_ENTRY
            : DescriptorConstants.S1AS_EJB_JAR_ENTRY;
    }

    /**
     * @param descriptor the descriptor for which we need the node
     * @return a RootXMLNode responsible for handling the deployment
     *         descriptors associated with this J2EE module
     */
    @Override
    public EjbBundleRuntimeNode getRootXMLNode(Descriptor descriptor) {
        if (descriptor instanceof EjbBundleDescriptorImpl) {
            return new EjbBundleRuntimeNode((EjbBundleDescriptorImpl) descriptor);
        }
        return null;
    }
}
