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

import com.sun.enterprise.deployment.AuthMechanism;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

/**
 * This node is responsible for handling the Connector DTD related auth-mechanism XML tag
 *
 * @author Sheetal Vartak
 */
public class AuthMechNode extends DeploymentDescriptorNode<AuthMechanism> {

    private AuthMechanism auth;

    public static Node writeAuthMechanisms(Node parent, Set<AuthMechanism> authMechanisms) {
        Iterator<AuthMechanism> authMechs = authMechanisms.iterator();
        while (authMechs.hasNext()) {
            AuthMechanism authMechanism = authMechs.next();
            Node authNode = appendChild(parent, ConnectorTagNames.AUTH_MECHANISM);
            appendTextChild(authNode, TagNames.DESCRIPTION, authMechanism.getDescription());
            appendTextChild(authNode, ConnectorTagNames.AUTH_MECH_TYPE, authMechanism.getAuthMechType());
            appendTextChild(authNode, ConnectorTagNames.CREDENTIAL_INTF, authMechanism.getCredentialInterface());
        }
        return parent;
    }


    @Override
    public AuthMechanism getDescriptor() {
        if (auth == null) {
            auth = DescriptorFactory.getDescriptor(getXMLPath());
        }
        return auth;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(ConnectorTagNames.CREDENTIAL_INTF, "setCredentialInterface");
        table.put(ConnectorTagNames.AUTH_MECH_TYPE, "setAuthMechVal");
        return table;
    }
}
