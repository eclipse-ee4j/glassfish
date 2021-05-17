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

package org.glassfish.webservices.io;

import com.sun.enterprise.deployment.BundleDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.webservices.node.WebServicesDescriptorNode;
import com.sun.enterprise.deployment.node.RootXMLNode;


import java.util.Vector;

/**
 * This class is responsible for handling the
 * JSR 109 webservices deployment descriptor
 *
 * @author Kenneth Saks
 */
public class WebServicesDeploymentDescriptorFile extends DeploymentDescriptorFile {

    private String descriptorPath;

    public WebServicesDeploymentDescriptorFile(RootDeploymentDescriptor desc) {
        descriptorPath = ( desc instanceof WebBundleDescriptor ) ?
            DescriptorConstants.WEB_WEBSERVICES_JAR_ENTRY : DescriptorConstants.EJB_WEBSERVICES_JAR_ENTRY;
    }

    /**
     * @return the location of the DeploymentDescriptor file for a
     * particular type of J2EE Archive
     */
    public String getDeploymentDescriptorPath() {
        return descriptorPath;
    }

    public static Vector getAllDescriptorPaths() {
        Vector allDescPaths = new Vector();
        allDescPaths.add(DescriptorConstants.WEB_WEBSERVICES_JAR_ENTRY);
        allDescPaths.add(DescriptorConstants.EJB_WEBSERVICES_JAR_ENTRY);

        return allDescPaths;
    }

    /**
     * @return a RootXMLNode responsible for handling the deployment
     * descriptors associated with this J2EE module
     *
     * @param the descriptor for which we need the node
     */
    public RootXMLNode getRootXMLNode(Descriptor descriptor) {
        if( descriptor instanceof BundleDescriptor ) {
            return new WebServicesDescriptorNode((BundleDescriptor) descriptor);
        }
        return null;
    }
}
