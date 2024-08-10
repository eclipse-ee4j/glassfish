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
 * FilterMappingNode.java
 *
 * Created on February 26, 2002, 9:21 PM
 */

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.net.URLPattern;

import jakarta.servlet.DispatcherType;

import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.descriptor.ServletFilterMappingDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;


/**
 * This node handles all information relative to servlet-mapping xml tag
 *
 * @author  Jerome Dochez
 * @version
 */
public class FilterMappingNode extends DeploymentDescriptorNode<ServletFilterMappingDescriptor> {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    private ServletFilterMappingDescriptor descriptor;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public ServletFilterMappingDescriptor getDescriptor() {

       if (descriptor==null) {
            descriptor = new ServletFilterMappingDescriptor();
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
        table.put(WebTagNames.FILTER_NAME, "setName");
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
        if (WebTagNames.SERVLET_NAME.equals(element.getQName())) {
            descriptor.addServletName(value);
        } else if (WebTagNames.URL_PATTERN.equals(element.getQName())) {
            if (!URLPattern.isValid(value)) {
                // try trimming url (in case DD uses extra
                // whitespace for aligning)
                String trimmedUrl = value.trim();

                // If URL Pattern does not start with "/" then
                // prepend it (for Servlet2.2 Web apps)
                Object parent = getParentNode().getDescriptor();
                if (parent instanceof WebBundleDescriptor &&
                        ((WebBundleDescriptor) parent).getSpecVersion().equals("2.2")) {
                    if(!trimmedUrl.startsWith("/") &&
                            !trimmedUrl.startsWith("*.")) {
                        trimmedUrl = "/" + trimmedUrl;
                    }
                }

                if (URLPattern.isValid(trimmedUrl)) {
                    // warn user if url included \r or \n
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
            descriptor.addURLPattern(value);
        } else if (WebTagNames.DISPATCHER.equals(element.getQName())) {
            descriptor.addDispatcher(value);
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
    public Node writeDescriptor(Node parent, String nodeName, ServletFilterMappingDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        appendTextChild(myNode, WebTagNames.FILTER_NAME, descriptor.getName());
        for (String servletName : descriptor.getServletNames()) {
            appendTextChild(myNode, WebTagNames.SERVLET_NAME, servletName);
        }

        for (String urlPattern : descriptor.getUrlPatterns()) {
            appendTextChild(myNode, WebTagNames.URL_PATTERN, urlPattern);
        }

        for (DispatcherType dispatcherType : descriptor.getDispatchers()) {
            appendTextChild(myNode, WebTagNames.DISPATCHER, dispatcherType.name());
        }
        return myNode;
    }
}
