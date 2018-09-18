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

package com.sun.enterprise.deployment.node.ws;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.ws.ReliabilityConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This node represents reliability-config in weblogic-webservices.xml
 *
 * @author Rama Pulavarthi
 */
public class ReliabilityConfigNode extends DeploymentDescriptorNode {
    private final XMLElement tag =
            new XMLElement(WLWebServicesTagNames.RELIABILITY_CONFIG);

    ReliabilityConfig rmConfig = new ReliabilityConfig();
    /*
    public ReliabilityConfigNode(WebServiceEndpoint endpoint) {
        this.endpoint = endpoint;
    }
     */
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    public Object getDescriptor() {
        return rmConfig;
    }

    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WLWebServicesTagNames.INACTIVITY_TIMEOUT, "setInactivityTimeout");
        table.put(WLWebServicesTagNames.BASE_RETRANSMISSION_INTERVAL, "setBaseRetransmissionInterval");
        table.put(WLWebServicesTagNames.RETRANSMISSION_EXPONENTIAL_BACKOFF, "setRetransmissionExponentialBackoff");
        table.put(WLWebServicesTagNames.ACKNOWLEDGEMENT_INTERVAL, "setAcknowledgementInterval");
        table.put(WLWebServicesTagNames.SEQUENCE_EXPIRATION, "setSequenceExpiration");
        table.put(WLWebServicesTagNames.BUFFER_RETRY_COUNT, "setBufferRetryCount");
        table.put(WLWebServicesTagNames.BUFFER_RETRY_DELAY, "setBufferRetryDelay");
        return table;
    }

    public Node writeDescriptor(Node parent, ReliabilityConfig descriptor) {
        if (descriptor != null) {
            Document doc = getOwnerDocument(parent);
            Element reliablityConfig = doc.createElement(WLWebServicesTagNames.RELIABILITY_CONFIG);
            addElementIfNonNull(doc,reliablityConfig, WLWebServicesTagNames.INACTIVITY_TIMEOUT, descriptor.getInactivityTimeout());
            addElementIfNonNull(doc,reliablityConfig, WLWebServicesTagNames.BASE_RETRANSMISSION_INTERVAL, descriptor.getBaseRetransmissionInterval());
            addElementIfNonNull(doc,reliablityConfig, WLWebServicesTagNames.RETRANSMISSION_EXPONENTIAL_BACKOFF, descriptor.getRetransmissionExponentialBackoff());
            addElementIfNonNull(doc,reliablityConfig, WLWebServicesTagNames.ACKNOWLEDGEMENT_INTERVAL, descriptor.getAcknowledgementInterval());
            addElementIfNonNull(doc,reliablityConfig, WLWebServicesTagNames.SEQUENCE_EXPIRATION, descriptor.getSequenceExpiration());
            addElementIfNonNull(doc,reliablityConfig, WLWebServicesTagNames.BUFFER_RETRY_COUNT, descriptor.getBufferRetryCount());
            addElementIfNonNull(doc,reliablityConfig, WLWebServicesTagNames.BUFFER_RETRY_DELAY, descriptor.getBufferRetryDelay());
            parent.appendChild(reliablityConfig);
            return reliablityConfig;
        }
        return null;
    }

    private void addElementIfNonNull(Document doc, Node parentNode, String tagName, String value) {
        if (value != null) {
            Element tag = doc.createElement(tagName);
            tag.appendChild(doc.createTextNode(value));
            parentNode.appendChild(tag);
        }
    }
}
