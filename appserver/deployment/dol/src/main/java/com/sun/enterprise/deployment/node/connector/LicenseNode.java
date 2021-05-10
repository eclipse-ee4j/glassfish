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

package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.LicenseDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import org.w3c.dom.Node;

import java.util.Map;


/**
 * This node is responsible for handling license subtree.
 */
public class LicenseNode extends DeploymentDescriptorNode {

    /**
     * all sub-implementation of this class can use a dispatch table to
     * map xml element to method name on the descriptor class for
     * setting the element value.
     *
     * @return the map with the element name as a key, the setter method
     *         as a value
     */
    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(ConnectorTagNames.LICENSE_REQUIRED, "setLicenseRequired");
        return table;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, ConnectorDescriptor descriptor) {
        LicenseDescriptor licenseDesc = descriptor.getLicenseDescriptor();
        if (licenseDesc != null) {
            Node licenseNode = appendChild(parent, ConnectorTagNames.LICENSE);
            writeLocalizedDescriptions(licenseNode, licenseDesc);
            appendTextChild(licenseNode, ConnectorTagNames.LICENSE_REQUIRED, licenseDesc.getLicenseRequiredValue());
        }
        return parent;
    }
}
