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

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.node.runtime.GFAppClientRuntimeNode;

import org.glassfish.deployment.common.Descriptor;

/**
 * This class is responsible for handling the XML configuration information
 * for the Glassfish Application Client Container
 */
public class GFAppClientRuntimeDDFile extends ConfigurationDeploymentDescriptorFile<ApplicationClientDescriptor> {

    @Override
    public String getDeploymentDescriptorPath() {
        return DescriptorConstants.GF_APP_CLIENT_JAR_ENTRY;
    }


    @Override
    public GFAppClientRuntimeNode getRootXMLNode(Descriptor descriptor) {
        if (descriptor instanceof ApplicationClientDescriptor) {
            return new GFAppClientRuntimeNode((ApplicationClientDescriptor) descriptor);
        }
        return null;
    }
}
