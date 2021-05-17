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

package com.sun.enterprise.deployment.io.runtime;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.node.RootXMLNode;
import com.sun.enterprise.deployment.node.ws.WLDescriptorConstants;
import com.sun.enterprise.deployment.node.ws.WLWebServicesDescriptorNode;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

import java.util.Vector;
import java.io.OutputStream;
import java.io.IOException;

/**
 * This class is responsible for handling the WebLogic webservices deployment descriptor.
 * This file weblogic-webservices.xml complements JSR-109 defined webservices.xml
 * to define extra configuration.
 *
 * @author Rama Pulavarthi
 */
public class WLSWebServicesDeploymentDescriptorFile extends ConfigurationDeploymentDescriptorFile {
    private String descriptorPath;

    public WLSWebServicesDeploymentDescriptorFile(RootDeploymentDescriptor desc) {
        if (desc instanceof WebServicesDescriptor) {
            descriptorPath = (((WebServicesDescriptor)desc).getBundleDescriptor().getModuleType().equals(DOLUtils.warType())) ?
                WLDescriptorConstants.WL_WEB_WEBSERVICES_JAR_ENTRY : WLDescriptorConstants.WL_EJB_WEBSERVICES_JAR_ENTRY;
        } else if (desc instanceof WebBundleDescriptor) {
            descriptorPath = WLDescriptorConstants.WL_WEB_WEBSERVICES_JAR_ENTRY;
        } else if (desc instanceof EjbBundleDescriptor) {
            descriptorPath = WLDescriptorConstants.WL_EJB_WEBSERVICES_JAR_ENTRY;
        }
    }

    @Override
    public String getDeploymentDescriptorPath() {
        return descriptorPath;
    }

    public static Vector getAllDescriptorPaths() {
        Vector allDescPaths = new Vector();
        allDescPaths.add(WLDescriptorConstants.WL_WEB_WEBSERVICES_JAR_ENTRY);
        allDescPaths.add(WLDescriptorConstants.WL_EJB_WEBSERVICES_JAR_ENTRY);

        return allDescPaths;
    }

    @Override
    public RootXMLNode getRootXMLNode(Descriptor descriptor) {
        if (descriptor instanceof WebServicesDescriptor) {
            return new WLWebServicesDescriptorNode((WebServicesDescriptor) descriptor);
        }
        return null;
    }

    /**
     * writes the descriptor to an output stream
     *
     * @param descriptor the descriptor
     * @param os the output stream
     */
    @Override
    public void write(Descriptor descriptor, OutputStream os) throws IOException {
        if (descriptor instanceof BundleDescriptor) {
            BundleDescriptor bundleDesc = (BundleDescriptor)descriptor;
            if (bundleDesc.hasWebServices()) {
                super.write(bundleDesc.getWebServices(), os);
            }
        }
    }

  /**
   * Return whether this configuration file can be validated.
   * @return whether this configuration file can be validated.
   */
  public boolean isValidating() {
    return true;
  }
}
