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

import org.glassfish.deployment.common.ModuleDescriptor;
import com.sun.enterprise.deployment.xml.ApplicationTagNames;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This node is responsible for handling the module xml fragment from
 * application.xml files
 *
 * @author  Jerome Dochez
 * @version
 */
public class ModuleNode extends DeploymentDescriptorNode {

    @Override
    protected Object createDescriptor() {
        return new ModuleDescriptor();
    }


   /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(ApplicationTagNames.ALTERNATIVE_DD, "setAlternateDescriptor");
        table.put(ApplicationTagNames.CONTEXT_ROOT, "setContextRoot");
        return table;
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        ModuleDescriptor descriptor = (ModuleDescriptor) getDescriptor();
         if (element.getQName().equals(ApplicationTagNames.WEB_URI)) {
            descriptor.setModuleType(DOLUtils.warType());
            descriptor.setArchiveUri(value);
        } else if (element.getQName().equals(ApplicationTagNames.EJB)) {
            descriptor.setModuleType(DOLUtils.ejbType());
            descriptor.setArchiveUri(value);
        } else if (element.getQName().equals(ApplicationTagNames.CONNECTOR)) {
            descriptor.setModuleType(DOLUtils.rarType());
            descriptor.setArchiveUri(value);
        } else if (element.getQName().equals(ApplicationTagNames.APPLICATION_CLIENT)) {
            descriptor.setModuleType(DOLUtils.carType());
            descriptor.setArchiveUri(value);
        } else if (element.getQName().equals(ApplicationTagNames.WEB)) {
            descriptor.setModuleType(DOLUtils.warType());
        } else super.setElementValue(element, value);
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, ModuleDescriptor descriptor) {

        Node module = appendChild(parent, nodeName);
        if (DOLUtils.warType().equals(descriptor.getModuleType())) {
            Node modType = appendChild(module, ApplicationTagNames.WEB);
            appendTextChild(modType, ApplicationTagNames.WEB_URI, descriptor.getArchiveUri());
            forceAppendTextChild(modType, ApplicationTagNames.CONTEXT_ROOT, descriptor.getContextRoot());

        } else {
            // default initialization if ejb...
            String type = ApplicationTagNames.EJB;
            if (DOLUtils.carType().equals(descriptor.getModuleType())) {
                type = ApplicationTagNames.APPLICATION_CLIENT;
            } else if (DOLUtils.rarType().equals(descriptor.getModuleType())) {
                type = ApplicationTagNames.CONNECTOR;
            }
            appendTextChild(module, type, descriptor.getArchiveUri());
        }
        appendTextChild(module,ApplicationTagNames.ALTERNATIVE_DD, descriptor.getAlternateDescriptor());
        return module;
    }
}
