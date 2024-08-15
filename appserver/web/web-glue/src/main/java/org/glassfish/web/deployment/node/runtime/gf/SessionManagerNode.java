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

package org.glassfish.web.deployment.node.runtime.gf;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.glassfish.web.deployment.runtime.SessionManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
* superclass node for WebProperty container
*
* @author Jerome Dochez
*/
public class SessionManagerNode extends RuntimeDescriptorNode<SessionManager> {

    /**
     * Initialize the child handlers
     */
    public SessionManagerNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.MANAGER_PROPERTIES),
                ManagerPropertiesNode.class, "setManagerProperties");
        registerElementHandler(new XMLElement(RuntimeTagNames.STORE_PROPERTIES),
                StorePropertiesNode.class, "setStoreProperties");
    }

    protected SessionManager descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public SessionManager getDescriptor() {
        if (descriptor==null) {
            descriptor = new SessionManager();
        }
        return descriptor;
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        RuntimeDescriptor descriptor = getDescriptor();
        if (element.getQName().equals(RuntimeTagNames.PERSISTENCE_TYPE)) {
            descriptor.setAttributeValue(SessionManager.PERSISTENCE_TYPE, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, SessionManager descriptor) {
        Element sessionMgr = (Element) super.writeDescriptor(parent, nodeName, descriptor);

        // manager-properties?
        if (descriptor.getManagerProperties() != null) {
            WebPropertyNode wpn = new WebPropertyNode();
            Node mgrProps = appendChild(sessionMgr, RuntimeTagNames.MANAGER_PROPERTIES);
            wpn.writeDescriptor(mgrProps, RuntimeTagNames.PROPERTY, descriptor.getManagerProperties().getWebProperty());
        }

        // store-properties?
        if (descriptor.getStoreProperties() != null) {
            WebPropertyNode wpn = new WebPropertyNode();
            Node storeProps = appendChild(sessionMgr, RuntimeTagNames.STORE_PROPERTIES);
            wpn.writeDescriptor(storeProps, RuntimeTagNames.PROPERTY, descriptor.getStoreProperties().getWebProperty());
        }

        // persistence-type?
        setAttribute(sessionMgr, RuntimeTagNames.PERSISTENCE_TYPE,
            descriptor.getAttributeValue(SessionManager.PERSISTENCE_TYPE));

        return sessionMgr;
    }

}
