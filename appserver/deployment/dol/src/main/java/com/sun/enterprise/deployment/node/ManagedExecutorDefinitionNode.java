/*
 * Copyright (c) 2022-2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;

import java.util.Map;

import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MANAGED_EXECUTOR_CONTEXT_SERVICE_REF;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MANAGED_EXECUTOR_HUNG_TASK_THRESHOLD;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MANAGED_EXECUTOR_MAX_ASYNC;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.QUALIFIER;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.USE_VIRTUAL_THREADS;
import static com.sun.enterprise.deployment.xml.TagNames.NAME;
import static com.sun.enterprise.deployment.xml.TagNames.RESOURCE_PROPERTY;

public class ManagedExecutorDefinitionNode extends DeploymentDescriptorNode<ManagedExecutorDefinitionDescriptor> {


    public ManagedExecutorDefinitionNode() {
        registerElementHandler(new XMLElement(RESOURCE_PROPERTY), ResourcePropertyNode.class,
            "addManagedExecutorPropertyDescriptor");
    }


    @Override
    public ManagedExecutorDefinitionDescriptor createDescriptor() {
        return new ManagedExecutorDefinitionDescriptor();
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> map = super.getDispatchTable();
        map.put(NAME, "setName");
        map.put(QUALIFIER, "addQualifier");
        map.put(USE_VIRTUAL_THREADS, "setUseVirtualThreads");
        map.put(MANAGED_EXECUTOR_MAX_ASYNC, "setMaximumPoolSize");
        map.put(MANAGED_EXECUTOR_HUNG_TASK_THRESHOLD, "setHungAfterSeconds");
        map.put(MANAGED_EXECUTOR_CONTEXT_SERVICE_REF, "setContext");
        return map;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, ManagedExecutorDefinitionDescriptor descriptor) {
        Node node = appendChild(parent, nodeName);
        appendTextChild(node, NAME, descriptor.getName());
        for (String qualifier : descriptor.getQualifiers()) {
            appendTextChild(node, QUALIFIER, qualifier);
        }
        appendTextChild(node, USE_VIRTUAL_THREADS, descriptor.getUseVirtualThreads());
        appendTextChild(node, MANAGED_EXECUTOR_MAX_ASYNC, String.valueOf(descriptor.getMaximumPoolSize()));
        appendTextChild(node, MANAGED_EXECUTOR_HUNG_TASK_THRESHOLD, String.valueOf(descriptor.getHungAfterSeconds()));
        appendTextChild(node, MANAGED_EXECUTOR_CONTEXT_SERVICE_REF, descriptor.getContext());
        return ResourcePropertyNode.write(node, descriptor);
    }
}
