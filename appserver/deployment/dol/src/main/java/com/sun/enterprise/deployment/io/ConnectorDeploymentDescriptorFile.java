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

import com.sun.enterprise.deployment.node.RootXMLNode;
import com.sun.enterprise.deployment.node.connector.ConnectorNode;

import org.glassfish.deployment.common.Descriptor;


/**
 * This class is responsible for handling JCA DeploymentDescriptor files
 *
 * @author Jerome Dochez
 */
public class ConnectorDeploymentDescriptorFile extends DeploymentDescriptorFile {

    public final static String DESC_PATH = "META-INF/ra.xml";

    public final static String VERSION_10 = "1.0";
    public final static String VERSION_15 = "1.5";
    public final static String VERSION_16 = "1.6";

    /** Creates a new instance of ConnectorDeploymentDescriptorFile */
    public ConnectorDeploymentDescriptorFile() {
    }

    /**
     * @return the location of the DeploymentDescriptor file for a
     * particular type of J2EE Archive
     */
    @Override
    public String getDeploymentDescriptorPath() {
        return DESC_PATH;
    }

    /**
     * @return a RootXMLNode responsible for handling the deployment
     * descriptors associated with this J2EE module
     *
     * @param the descriptor for which we need the node
     */
    @Override
    public RootXMLNode getRootXMLNode(Descriptor descriptor) {
        return new ConnectorNode();
    }
}
