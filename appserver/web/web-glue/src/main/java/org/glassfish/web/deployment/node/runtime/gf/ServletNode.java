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
 * ServletNode.java
 *
 * Created on March 7, 2002, 2:30 PM
 */

package org.glassfish.web.deployment.node.runtime.gf;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.WebServiceEndpointRuntimeNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import org.glassfish.web.deployment.descriptor.WebComponentDescriptorImpl;
import org.w3c.dom.Node;

/**
 * This node is handling all runtime deployment descriptors
 * relative to servlets
 *
 * @author  Jerome Dochez
 * @version
 */
public class ServletNode extends DeploymentDescriptorNode<WebComponentDescriptor> {

    protected WebComponentDescriptor descriptor;

    public ServletNode() {
        registerElementHandler(new XMLElement
            (WebServicesTagNames.WEB_SERVICE_ENDPOINT),
                               WebServiceEndpointRuntimeNode.class);
    }

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public WebComponentDescriptor getDescriptor() {
        if (descriptor==null) {
            descriptor = new WebComponentDescriptorImpl();
        }
        return descriptor;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (RuntimeTagNames.SERVLET_NAME.equals(element.getQName())) {
            Object parentDesc = getParentNode().getDescriptor();
            if (parentDesc instanceof WebBundleDescriptor) {
                descriptor = ((WebBundleDescriptor) parentDesc).getWebComponentByCanonicalName(value);
            }
        } else if (RuntimeTagNames.PRINCIPAL_NAME.equals(element.getQName())) {
            if (descriptor!=null && descriptor.getRunAsIdentity()!=null) {
                descriptor.getRunAsIdentity().setPrincipal(value);
            }
        } else super.setElementValue(element, value);
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, WebComponentDescriptor descriptor) {
        WebServicesDescriptor webServices =
            descriptor.getWebBundleDescriptor().getWebServices();

        // only write servlet runtime elements if there is a runas identity
        // or the servlet is exposed as a web service
        if ( (descriptor.getRunAsIdentity() != null) ||
             webServices.hasEndpointsImplementedBy(descriptor) ) {
            Node servletNode =  appendChild(parent, nodeName);
            appendTextChild(servletNode, RuntimeTagNames.SERVLET_NAME, descriptor.getCanonicalName());

            if( descriptor.getRunAsIdentity() != null ) {
                appendTextChild(servletNode, RuntimeTagNames.PRINCIPAL_NAME,
                                descriptor.getRunAsIdentity().getPrincipal());
            }

            WebServiceEndpointRuntimeNode wsRuntime =
                new WebServiceEndpointRuntimeNode();
            wsRuntime.writeWebServiceEndpointInfo(servletNode, descriptor);

            return servletNode;
        }
        return null;
    }
}
