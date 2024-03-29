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

package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.LicenseDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;

import org.w3c.dom.Node;


/**
 * This node is responsible for handling license subtree.
 */
public class LicenseNode extends DeploymentDescriptorNode<LicenseDescriptor> {

    public static Node writeLicenseDescriptor(Node parent, LicenseDescriptor licenseDesc) {
        if (licenseDesc != null) {
            Node licenseNode = appendChild(parent, ConnectorTagNames.LICENSE);
            writeLocalizedDescriptions(licenseNode, licenseDesc);
            appendTextChild(licenseNode, ConnectorTagNames.LICENSE_REQUIRED, licenseDesc.getLicenseRequiredValue());
        }
        return parent;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(ConnectorTagNames.LICENSE_REQUIRED, "setLicenseRequired");
        return table;
    }
}
