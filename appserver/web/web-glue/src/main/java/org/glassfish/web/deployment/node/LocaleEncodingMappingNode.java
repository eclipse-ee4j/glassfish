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

import com.sun.enterprise.deployment.LocaleEncodingMappingDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;

import java.util.Map;

import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 */
public class LocaleEncodingMappingNode extends DeploymentDescriptorNode<LocaleEncodingMappingDescriptor> {

    protected LocaleEncodingMappingDescriptor descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public LocaleEncodingMappingDescriptor getDescriptor() {
        if (descriptor==null) {
            descriptor = new LocaleEncodingMappingDescriptor();
        }
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(WebTagNames.LOCALE, "setLocale");
        table.put(WebTagNames.ENCODING, "setEncoding");
        return table;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName node name for the root element of this xml fragment
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, LocaleEncodingMappingDescriptor descriptor) {
        Node envEntryNode = super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(envEntryNode, WebTagNames.LOCALE, descriptor.getLocale());
        appendTextChild(envEntryNode, WebTagNames.ENCODING, descriptor.getEncoding());
        return envEntryNode;
    }
}
