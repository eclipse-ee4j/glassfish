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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * This node is responsible for handling WebService runtime info
 *
 * @author  Kenneth Saks
 * @version
 */
public class WebServiceRuntimeNode extends DeploymentDescriptorNode {

    private WebService descriptor;

    public Object getDescriptor() {
        return descriptor;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */

    public void setElementValue(XMLElement element, String value) {
        if (WebServicesTagNames.WEB_SERVICE_DESCRIPTION_NAME.equals
            (element.getQName())) {
            BundleDescriptor parent = (BundleDescriptor)getParentNode().getDescriptor();
            WebServicesDescriptor webServices = parent.getWebServices();
            descriptor = webServices.getWebServiceByName(value);
        } else if( WebServicesTagNames.CLIENT_WSDL_PUBLISH_URL.equals
                   (element.getQName()) ) {
            if( descriptor == null ) {
                DOLUtils.getDefaultLogger().info
                    ("Warning : WebService descriptor is null for "
                     + "final wsdl url=" + value);
                return;
            }
            try {
                URL url = new URL(value);
                descriptor.setClientPublishUrl(url);
            } catch(MalformedURLException mue) {
                DOLUtils.getDefaultLogger().log(Level.INFO,
                  "Warning : Invalid final wsdl url=" + value, mue);
            }
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for the descriptor
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                WebService webService) {
        Node webServiceNode =
            super.writeDescriptor(parent, nodeName, webService);

        appendTextChild(webServiceNode,
                        WebServicesTagNames.WEB_SERVICE_DESCRIPTION_NAME,
                        webService.getName());

        if( webService.hasClientPublishUrl() ) {
            URL url = webService.getClientPublishUrl();
            appendTextChild(webServiceNode,
                            WebServicesTagNames.CLIENT_WSDL_PUBLISH_URL,
                            url.toExternalForm());
        }

        return webServiceNode;
    }

    /**
     * writes all the runtime information for the web services for a given
     * bundle descriptor
     *
     * @param parent node to add the runtime xml info
     * @param the bundle descriptor
     */
    public void writeWebServiceRuntimeInfo(Node parent,
                                           BundleDescriptor bundle) {
        WebServicesDescriptor webServices = bundle.getWebServices();
        if( webServices != null ) {
            for(Iterator iter = webServices.getWebServices().iterator();
                iter.hasNext();) {
                WebService next = (WebService) iter.next();
                if( next.hasClientPublishUrl() ) {
                    writeDescriptor
                        (parent, WebServicesTagNames.WEB_SERVICE, next);
                }
            }
        }
    }

}
