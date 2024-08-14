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

import com.sun.enterprise.deployment.node.ApplicationNode;
import com.sun.enterprise.deployment.node.RootXMLNode;

import org.glassfish.deployment.common.Descriptor;

/**
 * This class is responsible for handling J2EE applications
 * DeploymentDescriptor files
 *
 * @author Jerome Dochez
 */
public class ApplicationDeploymentDescriptorFile extends DeploymentDescriptorFile {

    public final static String DESC_PATH = "META-INF/application.xml";

    /** Creates a new instance of ApplicationDeploymentDescriptorFile */
    public ApplicationDeploymentDescriptorFile() {
    }

    /**
     * @return the location of the DeploymentDescriptor file for a
     * particular type of J2EE Archive
     */
    public String getDeploymentDescriptorPath() {
        return DescriptorConstants.APPLICATION_JAR_ENTRY;
    }

    /**
     * @return a RootXMLNode responsible for handling the deployment
     * descriptors associated with this J2EE module
     *
     * @param the descriptor for which we need the node
     */
    public RootXMLNode getRootXMLNode(Descriptor descriptor) {
        return new ApplicationNode();
    }

}
