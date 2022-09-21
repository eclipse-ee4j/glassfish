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

package org.glassfish.webservices.node;

import com.sun.enterprise.deployment.RespectBinding;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node does xml marshalling to/from web service respect-binding elements
 *
 * @author Bhakti Mehta
 */
public class RespectBindingNode extends DisplayableComponentNode<RespectBinding> {

    private final static XMLElement TAG = new XMLElement(WebServicesTagNames.RESPECT_BINDING);

    @Override
    protected XMLElement getXMLRootTag() {
        return TAG;
    }


    @Override
    protected RespectBinding createDescriptor() {
        return new RespectBinding();
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(WebServicesTagNames.RESPECT_BINDING_ENABLED, "setEnabled");
        return table;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        String qname = element.getQName();
        RespectBinding rb = getDescriptor();
        if (WebServicesTagNames.RESPECT_BINDING_ENABLED.equals(qname)) {
            rb.setEnabled(Boolean.valueOf(value));
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, RespectBinding rb) {
        Node wshNode = super.writeDescriptor(parent, nodeName, rb);
        writeDisplayableComponentInfo(wshNode, rb);
        appendTextChild(wshNode, WebServicesTagNames.RESPECT_BINDING_ENABLED, Boolean.toString(rb.isEnabled()));
        return wshNode;
    }
}
