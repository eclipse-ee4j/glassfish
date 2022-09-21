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

package com.sun.enterprise.deployment.node.ws;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.ws.ReliabilityConfig;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.ACKNOWLEDGEMENT_INTERVAL;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.BASE_RETRANSMISSION_INTERVAL;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.BUFFER_RETRY_COUNT;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.BUFFER_RETRY_DELAY;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.INACTIVITY_TIMEOUT;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.RELIABILITY_CONFIG;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.RETRANSMISSION_EXPONENTIAL_BACKOFF;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.SEQUENCE_EXPIRATION;

/**
 * This node represents reliability-config in weblogic-webservices.xml
 *
 * @author Rama Pulavarthi
 */
public class ReliabilityConfigNode extends DeploymentDescriptorNode<ReliabilityConfig> {

    private final XMLElement tag = new XMLElement(WLWebServicesTagNames.RELIABILITY_CONFIG);
    private final ReliabilityConfig rmConfig = new ReliabilityConfig();


    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
    }


    @Override
    public ReliabilityConfig getDescriptor() {
        return rmConfig;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(INACTIVITY_TIMEOUT, "setInactivityTimeout");
        table.put(BASE_RETRANSMISSION_INTERVAL, "setBaseRetransmissionInterval");
        table.put(RETRANSMISSION_EXPONENTIAL_BACKOFF, "setRetransmissionExponentialBackoff");
        table.put(ACKNOWLEDGEMENT_INTERVAL, "setAcknowledgementInterval");
        table.put(SEQUENCE_EXPIRATION, "setSequenceExpiration");
        table.put(BUFFER_RETRY_COUNT, "setBufferRetryCount");
        table.put(BUFFER_RETRY_DELAY, "setBufferRetryDelay");
        return table;
    }


    @Override
    public Node writeDescriptor(Node parent, ReliabilityConfig descriptor) {
        if (descriptor != null) {
            Document doc = getOwnerDocument(parent);
            Element reliablityConfig = doc.createElement(RELIABILITY_CONFIG);
            addElementIfNonNull(doc, reliablityConfig, INACTIVITY_TIMEOUT, descriptor.getInactivityTimeout());
            addElementIfNonNull(doc, reliablityConfig, BASE_RETRANSMISSION_INTERVAL, descriptor.getBaseRetransmissionInterval());
            addElementIfNonNull(doc, reliablityConfig, RETRANSMISSION_EXPONENTIAL_BACKOFF, descriptor.getRetransmissionExponentialBackoff());
            addElementIfNonNull(doc, reliablityConfig, ACKNOWLEDGEMENT_INTERVAL, descriptor.getAcknowledgementInterval());
            addElementIfNonNull(doc, reliablityConfig, SEQUENCE_EXPIRATION, descriptor.getSequenceExpiration());
            addElementIfNonNull(doc, reliablityConfig, BUFFER_RETRY_COUNT, descriptor.getBufferRetryCount());
            addElementIfNonNull(doc, reliablityConfig, BUFFER_RETRY_DELAY, descriptor.getBufferRetryDelay());
            parent.appendChild(reliablityConfig);
            return reliablityConfig;
        }
        return null;
    }


    private void addElementIfNonNull(Document doc, Node parentNode, String tagName, String value) {
        if (value != null) {
            Element element = doc.createElement(tagName);
            element.appendChild(doc.createTextNode(value));
            parentNode.appendChild(element);
        }
    }
}
