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

package com.sun.enterprise.deployment.node;

import java.util.Map;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling all env-entry related xml tags
 *
 * @author  Jerome Dochez
 * @version
 */
public class EnvEntryNode extends DeploymentDescriptorNode<EnvironmentProperty> {

    private EnvironmentProperty envProp;
    private boolean setValueCalled = false;

    public EnvEntryNode() {
        super();
        registerElementHandler(new XMLElement(TagNames.INJECTION_TARGET),
                                InjectionTargetNode.class, "addInjectionTarget");
    }

    @Override
    public EnvironmentProperty getDescriptor() {
        if (envProp == null) envProp = new EnvironmentProperty();
        return envProp;
    }

    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(TagNames.ENVIRONMENT_PROPERTY_NAME, "setName");
        table.put(TagNames.ENVIRONMENT_PROPERTY_VALUE, "setValue");
        table.put(TagNames.ENVIRONMENT_PROPERTY_TYPE, "setType");
        table.put(TagNames.MAPPED_NAME, "setMappedName");
        table.put(TagNames.LOOKUP_NAME, "setLookupName");
        return table;
    }

    @Override
    public boolean endElement(XMLElement element) {
        if (TagNames.ENVIRONMENT_PROPERTY_NAME.equals(element.getQName())) {
            // name element is always right before value, so initialize
            // setValueCalled to false when it is processed.
            setValueCalled = false;
        } else if( TagNames.ENVIRONMENT_PROPERTY_VALUE.equals
                   (element.getQName()) ) {
            setValueCalled = true;
        } else if (TagNames.LOOKUP_NAME.equals(element.getQName())) {
            if (setValueCalled) {
                throw new IllegalArgumentException(localStrings.getLocalString( "enterprise.deployment.node.invalidenventry", "Cannot specify both the env-entry-value and lookup-name elements for env-entry element {0}", new Object[] {envProp.getName()}));
            }
        }
        return super.endElement(element);
    }

    @Override
    public void addDescriptor(Object newDescriptor) {
        if( setValueCalled ) {
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
        appendTextChild(envEntryNode, TagNames.ENVIRONMENT_PROPERTY_NAME, envProp.getName());
        appendTextChild(envEntryNode, TagNames.ENVIRONMENT_PROPERTY_TYPE, envProp.getType());
        appendTextChild(envEntryNode, TagNames.ENVIRONMENT_PROPERTY_VALUE, envProp.getValue());
        appendTextChild(envEntryNode, TagNames.MAPPED_NAME, envProp.getMappedName());
        if( envProp.isInjectable() ) {
            InjectionTargetNode ijNode = new InjectionTargetNode();
            for (InjectionTarget target : envProp.getInjectionTargets()) {
                ijNode.writeDescriptor(envEntryNode, TagNames.INJECTION_TARGET, target);
            }
        }
        appendTextChild(envEntryNode, TagNames.LOOKUP_NAME, envProp.getLookupName());
        return envEntryNode;
    }
}
