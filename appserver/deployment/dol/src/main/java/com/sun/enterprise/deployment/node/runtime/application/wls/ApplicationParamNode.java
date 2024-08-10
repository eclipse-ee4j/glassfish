/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node.runtime.application.wls;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.runtime.application.wls.ApplicationParam;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling application-param.
 *
 */
public class ApplicationParamNode extends DeploymentDescriptorNode {

    /**
     * all sub-implementation of this class can use a dispatch table to map
     * xml element to method name on the descriptor class for setting
     * the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(RuntimeTagNames.PARAM_NAME, "setName");
        table.put(RuntimeTagNames.PARAM_VALUE, "setValue");
        return table;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, EnvironmentProperty descriptor) {
        Node myNode = appendChild(parent, nodeName);

        writeLocalizedDescriptions(myNode, descriptor);
        appendTextChild(myNode, RuntimeTagNames.PARAM_NAME, descriptor.getName());
        appendTextChild(myNode, RuntimeTagNames.PARAM_VALUE, descriptor.getValue());
        return myNode;
    }

    /**
     * write all occurrences of the descriptor corresponding to the current
     * node from the parent descriptor to an JAXP DOM node and return it
     *
     * This API will be invoked by the parent node when the parent node
     * writes out a mix of statically and dynamically registered sub nodes.
     *
     * This method should be overriden by the sub classes if it
     * needs to be called by the parent node.
     *
     * @param parent node in the DOM tree
     * @param nodeName the name of the node
     * @param parentDesc parent descriptor of the descriptor to be written
     * @return the JAXP DOM node
     */
    @Override
    public Node writeDescriptors(Node parent, String nodeName, Descriptor parentDesc) {
        if (parentDesc instanceof Application) {
            Application application = (Application)parentDesc;
            // application-param*
            Set<ApplicationParam> applicationParams =
                application.getApplicationParams();
            for (ApplicationParam appParam : applicationParams) {
                writeDescriptor(parent, nodeName, (EnvironmentProperty)appParam);
            }
        }
        return parent;
    }
}
