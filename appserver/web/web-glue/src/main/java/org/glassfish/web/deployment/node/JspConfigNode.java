/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.JspConfigDefinitionDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;

import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;

import org.glassfish.web.deployment.descriptor.JspGroupDescriptor;
import org.glassfish.web.deployment.descriptor.TagLibConfigurationDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This node represents the <jsp-config> element in a web application.
 */
public class JspConfigNode extends DeploymentDescriptorNode<JspConfigDefinitionDescriptor> {

    public JspConfigNode() {
        super();
        registerElementHandler(new XMLElement(WebTagNames.TAGLIB), TagLibNode.class, "addTagLib");
        registerElementHandler(new XMLElement(WebTagNames.JSP_GROUP), JspGroupNode.class, "addJspGroup");
    }

    private JspConfigDefinitionDescriptor descriptor;

    @Override
    public JspConfigDefinitionDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new JspConfigDefinitionDescriptor();
        }
        return descriptor;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, JspConfigDefinitionDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        TagLibNode lNode = new TagLibNode();
        for (TaglibDescriptor desc : descriptor.getTaglibs()) {
            lNode.writeDescriptor(myNode, WebTagNames.TAGLIB, (TagLibConfigurationDescriptor) desc);
        }
        JspGroupNode jspGroup = new JspGroupNode();
        for (JspPropertyGroupDescriptor desc : descriptor.getJspPropertyGroups()) {
            jspGroup.writeDescriptor(myNode, WebTagNames.JSP_GROUP, (JspGroupDescriptor) desc);
        }

        return myNode;
    }
}
