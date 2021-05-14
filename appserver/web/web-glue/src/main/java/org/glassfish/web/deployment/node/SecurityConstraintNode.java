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

/*
 * SecurityConstraintNode.java
 *
 * Created on March 1, 2002, 2:24 PM
 */

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.web.WebResourceCollection;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;

import org.glassfish.web.deployment.descriptor.AuthorizationConstraintImpl;
import org.glassfish.web.deployment.descriptor.SecurityConstraintImpl;
import org.glassfish.web.deployment.descriptor.UserDataConstraintImpl;
import org.glassfish.web.deployment.descriptor.WebResourceCollectionImpl;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This node handles the security-contraint xml tag
 *
 * @author  Jerome Dochez
 * @version
 */
public class SecurityConstraintNode extends DeploymentDescriptorNode<SecurityConstraintImpl> {

    public SecurityConstraintNode() {
        super();
        registerElementHandler(new XMLElement(WebTagNames.USERDATA_CONSTRAINT), UserDataConstraintNode.class,
            "setUserDataConstraint");
        registerElementHandler(new XMLElement(WebTagNames.AUTH_CONSTRAINT), AuthConstraintNode.class,
            "setAuthorizationConstraint");
        registerElementHandler(new XMLElement(WebTagNames.WEB_RESOURCE_COLLECTION), WebResourceCollectionNode.class,
            "addWebResourceCollection");
    }

    protected SecurityConstraintImpl descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public SecurityConstraintImpl getDescriptor() {
        if (descriptor==null) {
            descriptor = new SecurityConstraintImpl();
        }
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map
     * xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a
     * value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.NAME, "setName");
        return table;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName node name for the root element of this xml fragment
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, SecurityConstraintImpl descriptor) {
        Node myNode = appendChild(parent, nodeName);
        appendTextChild(myNode, TagNames.NAME, descriptor.getName());

        // web-resource-collection+
        WebResourceCollectionNode wrcNode = new WebResourceCollectionNode();
        for (WebResourceCollection webResource: descriptor.getWebResourceCollections()) {
                wrcNode.writeDescriptor(myNode, WebTagNames.WEB_RESOURCE_COLLECTION,
                    (WebResourceCollectionImpl) webResource);
        }

        // auth-constaint?
        AuthorizationConstraintImpl aci = (AuthorizationConstraintImpl) descriptor.getAuthorizationConstraint();
        if (aci!=null) {
            AuthConstraintNode acNode = new AuthConstraintNode();
            acNode.writeDescriptor(myNode, WebTagNames.AUTH_CONSTRAINT, aci);
        }

        // user-data-constraint?
        UserDataConstraintImpl udci = (UserDataConstraintImpl) descriptor.getUserDataConstraint();
        if (udci!=null) {
            UserDataConstraintNode udcn = new UserDataConstraintNode();
            udcn.writeDescriptor(myNode, WebTagNames.USERDATA_CONSTRAINT, udci);
        }
        return myNode;
    }

}
