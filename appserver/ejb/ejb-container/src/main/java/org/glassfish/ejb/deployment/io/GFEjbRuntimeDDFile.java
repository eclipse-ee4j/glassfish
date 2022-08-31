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

package org.glassfish.ejb.deployment.io;

import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFileFor;
import com.sun.enterprise.deployment.io.DescriptorConstants;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.node.runtime.GFEjbBundleRuntimeNode;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.util.DOLUtils.scatteredWarType;
import static com.sun.enterprise.deployment.util.DOLUtils.warType;

/**
 * This class is responsible for handling the XML configuration information
 * for the Glassfish EJB Container
 */
@ConfigurationDeploymentDescriptorFileFor(EjbContainerUtil.EJB_CONTAINER_NAME)
@Service
@PerLookup
public class GFEjbRuntimeDDFile extends ConfigurationDeploymentDescriptorFile<EjbBundleDescriptorImpl> {

    @Override
    public String getDeploymentDescriptorPath() {
        return warType().equals(getArchiveType()) || scatteredWarType().equals(getArchiveType())
            ? DescriptorConstants.GF_EJB_IN_WAR_ENTRY
            : DescriptorConstants.GF_EJB_JAR_ENTRY;
    }


    @Override
    public GFEjbBundleRuntimeNode getRootXMLNode(Descriptor descriptor) {
        if (descriptor instanceof EjbBundleDescriptorImpl) {
            return new GFEjbBundleRuntimeNode((EjbBundleDescriptorImpl) descriptor);
        }
        return null;
    }
}
