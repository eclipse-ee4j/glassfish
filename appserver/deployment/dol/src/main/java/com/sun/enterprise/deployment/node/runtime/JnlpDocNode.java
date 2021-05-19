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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.XMLNode;
import com.sun.enterprise.deployment.runtime.JavaWebStartAccessDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

/**
 * Represents the jnlp-doc node under java-web-start-access.
 * We need this node in order to support the <jnlp-doc href="path-to-custom-JNLP-doc"/>
 * notation.  The DTD file describes the href attribute although the doc has not
 * historically mentioned it.  Instead the doc has said to place the path to
 * the custom JNLP as the text value of the <jnlp-doc> element.
 *
 * @author tjquinn
 */
public class JnlpDocNode extends DeploymentDescriptorNode<JavaWebStartAccessDescriptor> {

    protected JavaWebStartAccessDescriptor descriptor;

    public JnlpDocNode() {
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public JavaWebStartAccessDescriptor getDescriptor() {
        if (descriptor == null) {
            XMLNode parentNode = getParentNode();
            if (parentNode != null) {
                Object parentDescriptor = parentNode.getDescriptor();
                if (parentDescriptor != null && (parentDescriptor instanceof JavaWebStartAccessDescriptor)) {
                    descriptor = (JavaWebStartAccessDescriptor) parentDescriptor;
                }
            }
        }
        return descriptor;
    }


    @Override
    protected boolean setAttributeValue(XMLElement elementName, XMLElement attributeName, String value) {
        if (attributeName.getQName().equals("href")) {
            getDescriptor().setJnlpDocument(value);
            return true;
        } else {
            return super.setAttributeValue(elementName, attributeName, value);
        }
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        if (element.getQName().equals(RuntimeTagNames.JNLP_DOC)) {
            getDescriptor().setJnlpDocument(value);
        }
    }

}
