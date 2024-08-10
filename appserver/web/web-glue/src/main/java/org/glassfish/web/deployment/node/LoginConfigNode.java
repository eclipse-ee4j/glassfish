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
 * LoginConfigNode.java
 *
 * Created on March 5, 2002, 11:44 AM
 */

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;

import java.util.Map;

import org.glassfish.web.deployment.descriptor.LoginConfigurationImpl;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This node handles the login-config xml tag
 *
 * @author  Jerome Dochez
 * @version
 */
public class LoginConfigNode extends DeploymentDescriptorNode<LoginConfigurationImpl> {

    protected LoginConfigurationImpl descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public LoginConfigurationImpl getDescriptor() {
        if (descriptor==null) {
            descriptor = new LoginConfigurationImpl();
        }
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(WebTagNames.AUTH_METHOD, "setAuthenticationMethod");
        table.put(WebTagNames.REALM_NAME, "setRealmName");
        table.put(WebTagNames.FORM_LOGIN_PAGE, "setFormLoginPage");
        table.put(WebTagNames.FORM_ERROR_PAGE, "setFormErrorPage");
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
    public Node writeDescriptor(Node parent, String nodeName, LoginConfigurationImpl descriptor) {
        Node myNode = appendChild(parent, nodeName);
        appendTextChild(myNode, WebTagNames.AUTH_METHOD, descriptor.getAuthenticationMethod());
        appendTextChild(myNode, WebTagNames.REALM_NAME, descriptor.getRealmName());
        String loginPage = descriptor.getFormLoginPage();
        String errorPage =descriptor.getFormErrorPage();
        if (loginPage!=null && loginPage.length()>0 && errorPage !=null && errorPage.length()>0) {
            Node formNode = appendChild(myNode, WebTagNames.FORM_LOGIN_CONFIG);
            appendTextChild(formNode, WebTagNames.FORM_LOGIN_PAGE, loginPage);
            appendTextChild(formNode, WebTagNames.FORM_ERROR_PAGE, errorPage);
        }
        return myNode;
    }
}
