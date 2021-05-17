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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.MessageDestinationDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This node is responsible for handling runtime descriptor
 * message-destination tag.
 *
 * @author  Kenneth Saks
 * @version
 */
public class MessageDestinationRuntimeNode extends DeploymentDescriptorNode {

    private MessageDestinationDescriptor descriptor;

    /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(RuntimeTagNames.JNDI_NAME, "setJndiName");
        return table;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (RuntimeTagNames.MESSAGE_DESTINATION_NAME.equals(element.getQName())) {
            // this is a hack but not much choice
            Object parentDesc = getParentNode().getDescriptor();

            if (parentDesc instanceof BundleDescriptor) {
                try {
                    descriptor = ((BundleDescriptor) parentDesc).
                        getMessageDestinationByName(value);
                } catch (IllegalArgumentException iae) {
                    DOLUtils.getDefaultLogger().warning(iae.getMessage());
                }
            }
        } else if (RuntimeTagNames.JNDI_NAME.equals(element.getQName())) {
            if (descriptor != null) {
                descriptor.setJndiName(value);
            }
        } else super.setElementValue(element, value);
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for the descriptor
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, MessageDestinationDescriptor msgDest) {
        String jndiName  = msgDest.getJndiName();
        Node msgDestNode = null;
        if( (jndiName != null) && (jndiName.length() > 0) ) {
            msgDestNode = super.writeDescriptor(parent, nodeName, msgDest);
            appendTextChild(msgDestNode,
                            RuntimeTagNames.MESSAGE_DESTINATION_NAME,
                            msgDest.getName());
            appendTextChild(msgDestNode, RuntimeTagNames.JNDI_NAME,
                            msgDest.getJndiName());
        }
        return msgDestNode;
    }

}
