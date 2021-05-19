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
import com.sun.enterprise.deployment.node.XMLNode;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.net.URLPattern;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.xml.WebTagNames;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * This node is responsible for handling servlet-mapping subtree node
 *
 * @author  Jerome Dochez
 * @version
 */
public class ServletMappingNode extends DeploymentDescriptorNode {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    private String servletName;
    private String urlPattern;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Object getDescriptor() {
        return null;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (WebTagNames.SERVLET_NAME.equals(element.getQName())) {
            servletName = value;
        }
        if (WebTagNames.URL_PATTERN.equals(element.getQName())) {
            if (!URLPattern.isValid(value)) {
                // try trimming url (in case DD uses extra
                // whitespace for aligning)
                String trimmedUrl = value.trim();
                if ("\"\"".equals(trimmedUrl)) {
                    trimmedUrl = "";
                }

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

            urlPattern = value;

            XMLNode  parentNode = getParentNode();
            if (parentNode instanceof WebCommonNode) {
                ((WebCommonNode) parentNode).addServletMapping(servletName,
                urlPattern);
            } else {
                DOLUtils.getDefaultLogger().log(Level.SEVERE, "enterprise.deployment.backend.addDescriptorFailure",
                    new Object[]{getXMLRootTag() , "servlet-mapping"});
            }

        }
    }

}
