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
import com.sun.enterprise.deployment.util.DOLUtils;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

/**
 * This class represents nodes that are not supported.
 * Basically it consumes all sub nodes of the unsupported node and ignores them.
 *
 * @author Rama Pulavarthi
 */
public class WLUnSupportedNode extends DeploymentDescriptorNode<Descriptor> {

    private final XMLElement tag;

    public WLUnSupportedNode(XMLElement tag) {
        this.tag = tag;
    }


    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        DOLUtils.getDefaultLogger()
            .warning("Unsupported configuration " + element.getQName() + " in weblogic-webservices.xml");
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, Descriptor descriptor) {
        // This node does not preserve the original node information
        // TODO, Fix to write it back.
        return parent;
    }


    @Override
    public Descriptor getDescriptor() {
        return null;
    }


    @Override
    public void addDescriptor(Object descriptor) {
    }
}
