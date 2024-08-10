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
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.glassfish.web.deployment.runtime.SessionConfig;
import org.w3c.dom.Node;

/**
* superclass node for WebProperty container
*
* @author Jerome Dochez
*/

public class SessionConfigNode extends RuntimeDescriptorNode<SessionConfig> {

    protected SessionConfig descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public SessionConfig getDescriptor() {
        if (descriptor==null) {
            descriptor = new SessionConfig();
        }
        return descriptor;
    }

    /**
     * Initialize the child handlers
     */
    public SessionConfigNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.SESSION_MANAGER),
                               SessionManagerNode.class, "setSessionManager");
        registerElementHandler(new XMLElement(RuntimeTagNames.SESSION_PROPERTIES),
                               SessionPropertiesNode.class, "setSessionProperties");
        registerElementHandler(new XMLElement(RuntimeTagNames.COOKIE_PROPERTIES),
                               CookiePropertiesNode.class, "setCookieProperties");
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, SessionConfig descriptor) {
        Node sessionConfig = super.writeDescriptor(parent, nodeName, descriptor);

        // session-manager?
        if (descriptor.getSessionManager()!=null) {
            SessionManagerNode smn = new SessionManagerNode();
            smn.writeDescriptor(sessionConfig, RuntimeTagNames.SESSION_MANAGER, descriptor.getSessionManager());
        }

        // session-properties?
        if (descriptor.getSessionProperties()!=null) {
            WebPropertyNode wpn = new WebPropertyNode();
            Node sessionProps = appendChild(sessionConfig, RuntimeTagNames.SESSION_PROPERTIES);
            wpn.writeDescriptor(sessionProps, RuntimeTagNames.PROPERTY, descriptor.getSessionProperties().getWebProperty());
        }

        // cookie-properties?
        if (descriptor.getCookieProperties()!=null) {
            WebPropertyNode wpn = new WebPropertyNode();
            Node cookieProps = appendChild(sessionConfig, RuntimeTagNames.COOKIE_PROPERTIES);
            wpn.writeDescriptor(cookieProps, RuntimeTagNames.PROPERTY, descriptor.getCookieProperties().getWebProperty());
        }

        return sessionConfig;
    }
}
