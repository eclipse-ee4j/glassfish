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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InjectionTarget;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;

import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.TagNames.ENVIRONMENT_PROPERTY_NAME;
import static com.sun.enterprise.deployment.xml.TagNames.ENVIRONMENT_PROPERTY_TYPE;
import static com.sun.enterprise.deployment.xml.TagNames.ENVIRONMENT_PROPERTY_VALUE;
import static com.sun.enterprise.deployment.xml.TagNames.INJECTION_TARGET;
import static com.sun.enterprise.deployment.xml.TagNames.LOOKUP_NAME;
import static com.sun.enterprise.deployment.xml.TagNames.MAPPED_NAME;

/**
 * This node is responsible for handling all env-entry related xml tags
 *
 * @author  Jerome Dochez
 * @version
 */
public class EnvEntryNode extends DeploymentDescriptorNode<EnvironmentProperty> {
    private static final Logger LOG = System.getLogger(EnvEntryNode.class.getName());

    public EnvEntryNode() {
        registerElementHandler(new XMLElement(INJECTION_TARGET), InjectionTargetNode.class, "addInjectionTarget");
    }


    @Override
    public EnvironmentProperty createDescriptor() {
        return new EnvironmentProperty();
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(ENVIRONMENT_PROPERTY_NAME, "setName");
        table.put(ENVIRONMENT_PROPERTY_VALUE, "setValue");
        table.put(ENVIRONMENT_PROPERTY_TYPE, "setType");
        table.put(MAPPED_NAME, "setMappedName");
        table.put(LOOKUP_NAME, "setLookupName");
        return table;
    }

    @Override
    public boolean endElement(XMLElement element) {
        LOG.log(Level.TRACE, "endElement(element={0}); this={1}", element, this);
        if (!getDescriptor().getValue().isEmpty() && getDescriptor().hasLookupName()) {
            throw new IllegalArgumentException(
                "Cannot specify both the env-entry-value and lookup-name elements for env-entry element: "
                    + getDescriptor());
        }
        return super.endElement(element);
    }

    @Override
    public void addDescriptor(Object newDescriptor) {
        if (getDescriptor().hasContent()) {
            super.addDescriptor(newDescriptor);
        } else {
            // Don't add it to DOL.  The env-entry only exists
            // at runtime if it has been assigned a value.
        }
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, EnvironmentProperty envProp) {
        Node envEntryNode = super.writeDescriptor(parent, nodeName, envProp);
        writeLocalizedDescriptions(envEntryNode, envProp);
        appendTextChild(envEntryNode, ENVIRONMENT_PROPERTY_NAME, envProp.getName());
        appendTextChild(envEntryNode, ENVIRONMENT_PROPERTY_TYPE, envProp.getType());
        appendTextChild(envEntryNode, ENVIRONMENT_PROPERTY_VALUE, envProp.getValue());
        appendTextChild(envEntryNode, MAPPED_NAME, envProp.getMappedName());
        if (envProp.isInjectable()) {
            InjectionTargetNode ijNode = new InjectionTargetNode();
            for (InjectionTarget target : envProp.getInjectionTargets()) {
                ijNode.writeDescriptor(envEntryNode, INJECTION_TARGET, target);
            }
        }
        appendTextChild(envEntryNode, LOOKUP_NAME, envProp.getLookupName());
        return envEntryNode;
    }
}
