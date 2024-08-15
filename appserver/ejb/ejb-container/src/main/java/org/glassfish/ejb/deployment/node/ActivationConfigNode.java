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

package org.glassfish.ejb.deployment.node;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.ActivationConfigDescriptor;
import org.w3c.dom.Node;

/**
 * This class is responsible for handling the activation config elements.
 *
 * @author Kenneth Saks
 * @version
 */
public class ActivationConfigNode extends DeploymentDescriptorNode<ActivationConfigDescriptor> {

    private ActivationConfigDescriptor descriptor;
    private String propertyName = null;

    public ActivationConfigNode() {
        super();
        registerElementHandler(new XMLElement(EjbTagNames.ACTIVATION_CONFIG),
                               ActivationConfigNode.class,
                               "setActivationConfigDescriptor");
    }

    @Override
    public ActivationConfigDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new ActivationConfigDescriptor();
        return descriptor;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (EjbTagNames.ACTIVATION_CONFIG_PROPERTY_NAME.equals
            (element.getQName())) {
            propertyName = value;
        } else if(EjbTagNames.ACTIVATION_CONFIG_PROPERTY_VALUE.equals
                  (element.getQName())) {
            EnvironmentProperty prop =
                new EnvironmentProperty(propertyName, value, "");
            descriptor.getActivationConfig().add(prop);
            propertyName = null;
        }
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName,
                                ActivationConfigDescriptor descriptor) {

        Node activationConfigNode = null;
        Set activationConfig = descriptor.getActivationConfig();

        // ActionConfig must have at least one ActionConfigProperty
        // and ActionConfigProperty must have a pair of name/value
        // so filter out the entries with blank name or value
        Set activationConfig2 = new HashSet();
        for(Iterator iter = activationConfig.iterator(); iter.hasNext();) {
            EnvironmentProperty next = (EnvironmentProperty) iter.next();
            if ( ! next.getName().trim().equals("") &&
                 ! next.getValue().trim().equals("")) {
                activationConfig2.add(next);
            }
        }

        if( activationConfig2.size() > 0 ) {
            activationConfigNode =
                appendChild(parent, nodeName);
            writeLocalizedDescriptions(activationConfigNode, descriptor);

            for(Iterator iter = activationConfig2.iterator(); iter.hasNext();) {
                Node activationConfigPropertyNode =
                    appendChild(activationConfigNode,
                                EjbTagNames.ACTIVATION_CONFIG_PROPERTY);
                EnvironmentProperty next = (EnvironmentProperty) iter.next();
                appendTextChild(activationConfigPropertyNode,
                                EjbTagNames.ACTIVATION_CONFIG_PROPERTY_NAME,
                                next.getName());
                appendTextChild(activationConfigPropertyNode,
                                EjbTagNames.ACTIVATION_CONFIG_PROPERTY_VALUE,
                                next.getValue());
            }
        }
        return activationConfigNode;
    }
}
