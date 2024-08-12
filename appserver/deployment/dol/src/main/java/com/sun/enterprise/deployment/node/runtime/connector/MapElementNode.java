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

package com.sun.enterprise.deployment.node.runtime.connector;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.connector.MapElement;
import com.sun.enterprise.deployment.runtime.connector.Principal;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This node handles the role-map runtime deployment descriptors
 *
 * @author  Jerome Dochez
 * @version
 */
public class MapElementNode extends RuntimeDescriptorNode {

    public MapElementNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.PRINCIPAL), PrincipalNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.BACKEND_PRINCIPAL), PrincipalNode.class);
    }


    /**
     * Adds a new DOL descriptor instance to the descriptor instance associated with
     * this XMLNode
     *
     * @param newDescriptor the new descriptor
     */
    @Override
    public void addDescriptor(Object newDescriptor) {
        MapElement descriptor = (MapElement) getDescriptor();
        if (descriptor == null) {
            throw new RuntimeException("Cannot set info on null descriptor");
        }
        if (newDescriptor instanceof Principal) {
            Principal principal = (Principal) newDescriptor;
            if (principal.getValue(Principal.CREDENTIAL) == null) {
                descriptor.addPrincipal(principal);
            } else {
                descriptor.setBackendPrincipal(true);
                descriptor.setAttributeValue(MapElement.BACKEND_PRINCIPAL, Principal.USER_NAME,
                    principal.getValue(Principal.USER_NAME));
                descriptor.setAttributeValue(MapElement.BACKEND_PRINCIPAL, Principal.PASSWORD,
                    principal.getValue(Principal.PASSWORD));
                descriptor.setAttributeValue(MapElement.BACKEND_PRINCIPAL, Principal.CREDENTIAL,
                    principal.getValue(Principal.CREDENTIAL));

            }
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName name for the descriptor
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, MapElement descriptor) {
        Node mapElementNode = super.writeDescriptor(parent, nodeName, descriptor);
        PrincipalNode pn = new PrincipalNode();
        Principal[] principals = descriptor.getPrincipal();
        for (Principal principal : principals) {
            pn.writeDescriptor(mapElementNode, RuntimeTagNames.PRINCIPAL, principal);
        }
        // backend-principal
        if (descriptor.isBackendPrincipal()) {
            Element backend = appendChild(mapElementNode, RuntimeTagNames.BACKEND_PRINCIPAL);
            setAttribute(backend, RuntimeTagNames.USER_NAME,
                descriptor.getAttributeValue(MapElement.BACKEND_PRINCIPAL, Principal.USER_NAME));
            setAttribute(backend, RuntimeTagNames.PASSWORD,
                descriptor.getAttributeValue(MapElement.BACKEND_PRINCIPAL, Principal.PASSWORD));
            setAttribute(backend, RuntimeTagNames.CREDENTIAL,
                descriptor.getAttributeValue(MapElement.BACKEND_PRINCIPAL, Principal.CREDENTIAL));
        }

        return mapElementNode;
    }
}
