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

import com.sun.enterprise.deployment.JaxrpcMappingDescriptor;
import com.sun.enterprise.deployment.node.JaxrpcMappingDescriptorNode;
import com.sun.enterprise.deployment.node.RootXMLNode;

/**
 * This class is responsible for handling the
 * JSR 109 jaxrpc mapping deployment descriptor
 *
 * @author Kenneth Saks
 */
public class JaxrpcMappingDeploymentDescriptorFile extends
    DeploymentDescriptorFile<JaxrpcMappingDescriptor> {

    String mappingFilePath = null;

    public JaxrpcMappingDeploymentDescriptorFile() {
    }


    /**
     * @return the location of the DeploymentDescriptor file for a
     * particular type of J2EE Archive
     */
    public String getDeploymentDescriptorPath() {
        // writing not supported.  always copied from input jar.
        return mappingFilePath;
    }

    /**
     * Sets the mapping file location in the source archive
     */
    public void setDeploymentDescriptorPath(String path) {
        this.mappingFilePath = path;
    }

    /**
     * @return a RootXMLNode responsible for handling the deployment
     * descriptors associated with this J2EE module
     *
     * @param descriptor ignored
     */
    public RootXMLNode getRootXMLNode(JaxrpcMappingDescriptor descriptor) {
        return new JaxrpcMappingDescriptorNode();
    }
}
