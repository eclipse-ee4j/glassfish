/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.MailSessionDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: naman mehta
 * Date: 16/4/12
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class MailSessionNode extends DeploymentDescriptorNode<MailSessionDescriptor> {

    public final static XMLElement tag = new XMLElement(TagNames.MAIL_SESSION);
    private MailSessionDescriptor descriptor = null;

    public MailSessionNode() {
        registerElementHandler(new XMLElement(TagNames.RESOURCE_PROPERTY), ResourcePropertyNode.class,
                "addMailSessionPropertyDescriptor");
    }

    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();

        table.put(TagNames.MAIL_SESSION_NAME, "setName");
        table.put(TagNames.MAIL_SESSION_STORE_PROTOCOL, "setStoreProtocol");
        table.put(TagNames.MAIL_SESSION_TRANSPORT_PROTOCOL, "setTransportProtocol");
        table.put(TagNames.MAIL_SESSION_HOST, "setHost");
        table.put(TagNames.MAIL_SESSION_USER, "setUser");
        table.put(TagNames.MAIL_SESSION_PASSWORD, "setPassword");
        table.put(TagNames.MAIL_SESSION_FROM, "setFrom");

        return table;
    }


    public Node writeDescriptor(Node parent, String nodeName, MailSessionDescriptor mailSessionDesc) {

        Node node = appendChild(parent, nodeName);
        appendTextChild(node, TagNames.MAIL_SESSION_NAME, mailSessionDesc.getName());
        appendTextChild(node, TagNames.MAIL_SESSION_STORE_PROTOCOL, mailSessionDesc.getStoreProtocol());
        appendTextChild(node, TagNames.MAIL_SESSION_TRANSPORT_PROTOCOL, mailSessionDesc.getTransportProtocol());
        appendTextChild(node, TagNames.MAIL_SESSION_HOST, mailSessionDesc.getHost());
        appendTextChild(node, TagNames.MAIL_SESSION_USER, mailSessionDesc.getUser());
        appendTextChild(node, TagNames.MAIL_SESSION_PASSWORD, mailSessionDesc.getPassword());
        appendTextChild(node, TagNames.MAIL_SESSION_FROM, mailSessionDesc.getFrom());

        ResourcePropertyNode propertyNode = new ResourcePropertyNode();
        propertyNode.writeDescriptor(node, mailSessionDesc);

        return node;
    }

    public MailSessionDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new MailSessionDescriptor();
        }
        return descriptor;
    }
}
