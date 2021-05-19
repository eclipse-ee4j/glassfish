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

package org.glassfish.ejb.deployment.node.runtime;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.glassfish.ejb.deployment.descriptor.ActivationConfigDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;
import org.w3c.dom.Node;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

/**
 * This class is responsible for handling the activation config elements.
 *
 * @author Qingqing Ouyang
 * @version
 */
public class ActivationConfigNode extends DeploymentDescriptorNode<ActivationConfigDescriptor> {

    private ActivationConfigDescriptor descriptor = null;
    private String propertyName = null;

    public ActivationConfigNode() {
        super();
        registerElementHandler(
                new XMLElement(RuntimeTagNames.ACTIVATION_CONFIG),
                ActivationConfigNode.class,
                "setRuntimeActivationConfigDescriptor");
    }

    @Override
    public ActivationConfigDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = ((EjbMessageBeanDescriptor) getParentNode().getDescriptor()).getRuntimeActivationConfigDescriptor();
        }
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        return super.getDispatchTable();
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (RuntimeTagNames.ACTIVATION_CONFIG_PROPERTY_NAME.equals
                (element.getQName())) {
            propertyName = value;
        } else if(RuntimeTagNames.ACTIVATION_CONFIG_PROPERTY_VALUE.equals
                (element.getQName())) {
            EnvironmentProperty prop =
                new EnvironmentProperty(propertyName, value, "");
            descriptor.getActivationConfig().add(prop);
            propertyName = null;
        }
        else super.setElementValue(element, value);
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName,
                                ActivationConfigDescriptor descriptor) {

        Node activationConfigNode = null;
        Set activationConfig = descriptor.getActivationConfig();
        if( activationConfig.size() > 0 ) {
            activationConfigNode = appendChild(parent, nodeName);
            for(Iterator iter = activationConfig.iterator(); iter.hasNext();) {
                Node activationConfigPropertyNode =
                    appendChild(activationConfigNode,
                                RuntimeTagNames.ACTIVATION_CONFIG_PROPERTY);
                EnvironmentProperty next = (EnvironmentProperty) iter.next();
                appendTextChild(activationConfigPropertyNode,
                        RuntimeTagNames.ACTIVATION_CONFIG_PROPERTY_NAME,
                        next.getName());
                appendTextChild(activationConfigPropertyNode,
                        RuntimeTagNames.ACTIVATION_CONFIG_PROPERTY_VALUE,
                        next.getValue());
            }
        }

        return activationConfigNode;
    }
}
