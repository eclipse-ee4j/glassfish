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

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;

import org.glassfish.web.deployment.descriptor.CookieConfigDescriptor;
import org.glassfish.web.deployment.descriptor.SessionConfigDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This class is responsible for handling session-config xml node.
 *
 * @author Shing Wai Chan
 */
public class SessionConfigNode extends DeploymentDescriptorNode {
    private SessionConfigDescriptor descriptor;

    public SessionConfigNode() {
        super();
        registerElementHandler(new XMLElement(WebTagNames.COOKIE_CONFIG),
                CookieConfigNode.class, "setCookieConfig");
    }

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
   @Override
    public SessionConfigDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new SessionConfigDescriptor();
        }
        return descriptor;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (WebTagNames.SESSION_TIMEOUT.equals(element.getQName())) {
            // if the session out value is already set
            // which means there are multiple session-config elements
            // throw an exception
            if (descriptor.getSessionTimeout() !=
                SessionConfigDescriptor.SESSION_TIMEOUT_DEFAULT) {
                throw new RuntimeException(
                    "Has more than one session-config element!");
            }
            descriptor.setSessionTimeout(Integer.parseInt(value.trim()));
        } else if (WebTagNames.TRACKING_MODE.equals(element.getQName())) {
            descriptor.addTrackingMode(value);
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, SessionConfigDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        if (descriptor.getSessionTimeout() != descriptor.SESSION_TIMEOUT_DEFAULT) {
            appendTextChild(myNode, WebTagNames.SESSION_TIMEOUT,
                    String.valueOf(descriptor.getSessionTimeout()));
        }
        CookieConfigDescriptor cookieConfigDesc = (CookieConfigDescriptor)descriptor.getCookieConfig();
        if (cookieConfigDesc != null) {
            CookieConfigNode cookieConfigNode = new CookieConfigNode();
            cookieConfigNode.writeDescriptor(myNode, WebTagNames.COOKIE_CONFIG,
                cookieConfigDesc);
        }

        if (descriptor.getTrackingModes().size() > 0) {
            for (Enum tmEnum : descriptor.getTrackingModes()) {
                appendTextChild(myNode, WebTagNames.TRACKING_MODE, tmEnum.name());
            }
        }
        return myNode;
    }
}
