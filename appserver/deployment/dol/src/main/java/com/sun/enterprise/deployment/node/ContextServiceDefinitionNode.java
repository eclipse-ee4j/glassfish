/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2024 Payara Foundation and/or its affiliates
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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.ContextServiceDefinitionDescriptor;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;

import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_SERVICE_CLEARED;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_SERVICE_PROPAGATED;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_SERVICE_UNCHANGED;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.QUALIFIER;
import static com.sun.enterprise.deployment.xml.TagNames.NAME;
import static com.sun.enterprise.deployment.xml.TagNames.RESOURCE_PROPERTY;

public class ContextServiceDefinitionNode extends DeploymentDescriptorNode<ContextServiceDefinitionDescriptor> {

    private static final Logger LOG = System.getLogger(ContextServiceDefinitionNode.class.getName());

    public ContextServiceDefinitionNode() {
        registerElementHandler(new XMLElement(RESOURCE_PROPERTY), ResourcePropertyNode.class,
            "addContextServiceExecutorDescriptor");
    }


    @Override
    public ContextServiceDefinitionDescriptor createDescriptor() {
        return new ContextServiceDefinitionDescriptor();
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> map = super.getDispatchTable();
        map.put(NAME, "setName");
        map.put(CONTEXT_SERVICE_PROPAGATED, "addPropagated");
        map.put(CONTEXT_SERVICE_CLEARED, "addCleared");
        map.put(CONTEXT_SERVICE_UNCHANGED, "addUnchanged");
        return map;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        String qname = element.getQName();
        ContextServiceDefinitionDescriptor descriptor = getDescriptor();
        if (QUALIFIER.equals(qname)) {
            try {
                descriptor.addQualifier(Class.forName(value, false, Thread.currentThread().getContextClassLoader()));
            } catch (ClassNotFoundException e) {
                LOG.log(Level.WARNING, "Ignoring unresolvable qualifier " + value, e);
            }
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, ContextServiceDefinitionDescriptor descriptor) {
        Node node = appendChild(parent, nodeName);
        appendTextChild(node, NAME, descriptor.getName());
        for (Class<?> qualifier : descriptor.getQualifiers()) {
            appendTextChild(node, QUALIFIER, qualifier.getCanonicalName());
        }
        for (String cleared : descriptor.getCleared()) {
            appendTextChild(node, CONTEXT_SERVICE_CLEARED, cleared);
        }
        for (String propagated : descriptor.getPropagated()) {
            appendTextChild(node, CONTEXT_SERVICE_PROPAGATED, propagated);
        }
        for (String unchanged : descriptor.getUnchanged()) {
            appendTextChild(node, CONTEXT_SERVICE_UNCHANGED, unchanged);
        }
        return ResourcePropertyNode.write(node, descriptor);
    }
}
