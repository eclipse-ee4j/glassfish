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

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.net.URLPattern;
import org.glassfish.web.deployment.descriptor.WebResourceCollectionImpl;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.glassfish.web.LogFacade;
import org.w3c.dom.Node;

import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * This nodes handles the web-collection xml tag element
 *
 * @author  Jerome Dochez
 * @version
 */
public class WebResourceCollectionNode extends DeploymentDescriptorNode<WebResourceCollectionImpl>  {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    private WebResourceCollectionImpl descriptor;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public WebResourceCollectionImpl getDescriptor() {
        if (descriptor==null) {
            descriptor = new WebResourceCollectionImpl();
        }
        return descriptor;
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    @Override
    protected XMLElement getXMLRootTag() {
        return new XMLElement(WebTagNames.WEB_RESOURCE_COLLECTION);
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
        table.put(WebTagNames.WEB_RESOURCE_NAME, "setName");
        table.put(WebTagNames.HTTP_METHOD, "addHttpMethod");
        table.put(WebTagNames.HTTP_METHOD_OMISSION, "addHttpMethodOmission");
        return table;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (WebTagNames.URL_PATTERN.equals(element.getQName())) {
            if (!URLPattern.isValid(value)) {
                // try trimming url (in case DD uses extra
                // whitespace for aligning)
                String trimmedUrl = value.trim();

                // If URL Pattern does not start with "/" then
                // prepend it (for Servlet2.2 Web apps)
                Object parent = getParentNode().getParentNode().getDescriptor();
                if (parent instanceof WebBundleDescriptor &&
                        ((WebBundleDescriptor) parent).getSpecVersion().equals("2.2")) {
                    if(!trimmedUrl.startsWith("/") &&
                            !trimmedUrl.startsWith("*.")) {
                        trimmedUrl = "/" + trimmedUrl;
                    }
                }

                if (URLPattern.isValid(trimmedUrl)) {
                    // warn user with error message if url included \r or \n
                    if (URLPattern.containsCRorLF(value)) {
                        DOLUtils.getDefaultLogger().log(Level.WARNING,
                                "enterprise.deployment.backend.urlcontainscrlf",
                                new Object[] { value });
                    }
                    value = trimmedUrl;
                } else {
                    throw new IllegalArgumentException(
                            MessageFormat.format(
                                    rb.getString(LogFacade.ENTERPRISE_DEPLOYMENT_INVALID_URL_PATTERN), value));
                }
            }
            descriptor.addUrlPattern(value);
        } else super.setElementValue(element, value);
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
    public Node writeDescriptor(Node parent, String nodeName, WebResourceCollectionImpl descriptor) {
        Node myNode = appendChild(parent, nodeName);
        appendTextChild(myNode, WebTagNames.WEB_RESOURCE_NAME, descriptor.getName());
        writeLocalizedDescriptions(myNode, descriptor);

        // url-pattern*
        for (String urlPattern: descriptor.getUrlPatterns()) {
            appendTextChild(myNode, WebTagNames.URL_PATTERN, urlPattern);
        }

        // http-method*
        for (String httpMethod: descriptor.getHttpMethods()) {
            appendTextChild(myNode, WebTagNames.HTTP_METHOD, httpMethod);
        }

        // http-method-omission*
        for (String httpMethodOmission: descriptor.getHttpMethodOmissions()) {
            appendTextChild(myNode, WebTagNames.HTTP_METHOD_OMISSION, httpMethodOmission);
        }
        return myNode;
    }
}
