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

import com.sun.enterprise.deployment.AuthMechanism;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.Map;

/**
 * This node is responsible for handling the Connector DTD related auth-mechanism XML tag
 *
 * @author  Sheetal Vartak
 * @version
 */
public class AuthMechNode extends DeploymentDescriptorNode {

    private AuthMechanism auth = null;

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(ConnectorTagNames.CREDENTIAL_INTF, "setCredentialInterface");
        table.put(ConnectorTagNames.AUTH_MECH_TYPE, "setAuthMechVal");
        return table;
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Object getDescriptor() {
        if (auth == null) {
            auth = (AuthMechanism) DescriptorFactory.getDescriptor(getXMLPath());
        }
        return auth;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, Descriptor descriptor) {
        if (!(descriptor instanceof OutboundResourceAdapter) && !(descriptor instanceof ConnectorDescriptor)) {
            throw new IllegalArgumentException(
                getClass() + " cannot handle descriptors of type " + descriptor.getClass());
        }

        Iterator authMechs = null;

        if (descriptor instanceof ConnectorDescriptor) {
            authMechs = ((ConnectorDescriptor) descriptor).getAuthMechanisms().iterator();
        } else if (descriptor instanceof OutboundResourceAdapter) {
            authMechs = ((OutboundResourceAdapter) descriptor).getAuthMechanisms().iterator();
        }

        // auth mechanism info
        if (authMechs != null) {
            for (; authMechs.hasNext();) {
                AuthMechanism auth = (AuthMechanism) authMechs.next();
                Node authNode = appendChild(parent, ConnectorTagNames.AUTH_MECHANISM);
                appendTextChild(authNode, TagNames.DESCRIPTION, auth.getDescription());
                appendTextChild(authNode, ConnectorTagNames.AUTH_MECH_TYPE, auth.getAuthMechType());
                appendTextChild(authNode, ConnectorTagNames.CREDENTIAL_INTF, auth.getCredentialInterface());
            }
        }
        return parent;
    }
}
