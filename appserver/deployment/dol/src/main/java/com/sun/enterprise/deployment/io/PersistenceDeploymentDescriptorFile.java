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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.core.*;
import com.sun.enterprise.deployment.node.PersistenceNode;
import com.sun.enterprise.deployment.node.RootXMLNode;
import org.glassfish.deployment.common.Descriptor;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceDeploymentDescriptorFile extends DeploymentDescriptorFile {
    public String getDeploymentDescriptorPath() {
        return DescriptorConstants.PERSISTENCE_DD_ENTRY;
    }

    public RootXMLNode getRootXMLNode(Descriptor descriptor) {
        // This method is called from SaxParserHandler.startElement() method
        // as well as DeploymentDescriptorFile.getDefaultSchemaSource().
        // When it is called from former method, descriptor is non-null,
        // but when it is called later method, descriptor is null.
        if(descriptor==null ||
                descriptor instanceof Application ||
                descriptor instanceof ApplicationClientDescriptor ||
                descriptor instanceof EjbBundleDescriptor ||
                descriptor instanceof WebBundleDescriptor) {
            return new PersistenceNode(new PersistenceUnitsDescriptor());
        } else {
            throw new IllegalArgumentException(descriptor.getClass().getName()+
                    "is not allowed to contain persistence.xml file");
        }
    }

}
