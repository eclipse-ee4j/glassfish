/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.node.runtime.connector;

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.node.runtime.common.RuntimeNameValuePairNode;
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import org.glassfish.api.naming.SimpleJndiName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This node handles the resource-adapter runtime deployment descriptors
 *
 * @author  Jerome Dochez
 * @version
 */
public class ResourceAdapterNode extends RuntimeDescriptorNode<RuntimeDescriptor> {

    protected RuntimeDescriptor descriptor;

    /**
     * Initialize the child handlers
     */
    public ResourceAdapterNode() {
        // we do not care about our standard DDS handles
        handlers = null;

        registerElementHandler(new XMLElement(RuntimeTagNames.PROPERTY), RuntimeNameValuePairNode.class,
            "addPropertyElement");
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public RuntimeDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = super.getDescriptor();
            descriptor.setValue(ResourceAdapter.MAX_POOL_SIZE, "32");
            descriptor.setValue(ResourceAdapter.STEADY_POOL_SIZE, "4");
            descriptor.setValue(ResourceAdapter.MAX_WAIT_TIME_IN_MILLIS, "10000");
            descriptor.setValue(ResourceAdapter.IDLE_TIMEOUT_IN_SECONDS, "1000");

        }
        return descriptor;
    }


    /**
     * parsed an attribute of an element
     *
     * @return true if the attribute was processed
     */
    @Override
    protected boolean setAttributeValue(XMLElement elementName, XMLElement attributeName, String value) {
        getDescriptor();
        if (descriptor == null) {
            throw new RuntimeException("Trying to set values on a null descriptor");
        }
        if (attributeName.getQName().equals(RuntimeTagNames.JNDI_NAME)) {
            descriptor.setAttributeValue(ResourceAdapter.JNDI_NAME, SimpleJndiName.of(value));
            return true;
        }
        if (attributeName.getQName().equals(RuntimeTagNames.MAX_POOL_SIZE)) {
            descriptor.setAttributeValue(ResourceAdapter.MAX_POOL_SIZE, value);
            return true;
        }
        if (attributeName.getQName().equals(RuntimeTagNames.STEADY_POOL_SIZE)) {
            descriptor.setAttributeValue(ResourceAdapter.STEADY_POOL_SIZE, value);
            return true;
        }
        if (attributeName.getQName().equals(RuntimeTagNames.MAX_WAIT_TIME_IN_MILLIS)) {
            descriptor.setAttributeValue(ResourceAdapter.MAX_WAIT_TIME_IN_MILLIS, value);
            return true;
        }
        if (attributeName.getQName().equals(RuntimeTagNames.IDLE_TIMEOUT_IN_SECONDS)) {
            descriptor.setAttributeValue(ResourceAdapter.IDLE_TIMEOUT_IN_SECONDS, value);
            return true;
        }
        return false;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName name for the descriptor
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, ResourceAdapter descriptor) {
        Element raNode = (Element) super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(raNode, TagNames.DESCRIPTION, descriptor.getDescription());
        SimpleJndiName jndiName = (SimpleJndiName) descriptor.getValue(ResourceAdapter.JNDI_NAME);
        if (jndiName != null) {
            setAttribute(raNode, RuntimeTagNames.JNDI_NAME, jndiName.toString());
        }
        setAttribute(raNode, RuntimeTagNames.MAX_POOL_SIZE,
            (String) descriptor.getValue(ResourceAdapter.MAX_POOL_SIZE));
        setAttribute(raNode, RuntimeTagNames.STEADY_POOL_SIZE,
            (String) descriptor.getValue(ResourceAdapter.STEADY_POOL_SIZE));
        setAttribute(raNode, RuntimeTagNames.MAX_WAIT_TIME_IN_MILLIS,
            (String) descriptor.getValue(ResourceAdapter.MAX_WAIT_TIME_IN_MILLIS));
        setAttribute(raNode, RuntimeTagNames.IDLE_TIMEOUT_IN_SECONDS,
            (String) descriptor.getValue(ResourceAdapter.IDLE_TIMEOUT_IN_SECONDS));

        // properties...
        NameValuePairDescriptor[] properties = descriptor.getPropertyElement();
        if (properties != null && properties.length > 0) {
            RuntimeNameValuePairNode subNode = new RuntimeNameValuePairNode();
            for (NameValuePairDescriptor element : properties) {
                subNode.writeDescriptor(raNode, RuntimeTagNames.PROPERTY, element);
            }
        }

        return raNode;
    }
}
