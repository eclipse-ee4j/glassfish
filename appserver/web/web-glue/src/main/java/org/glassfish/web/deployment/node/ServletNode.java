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

import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.node.*;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.web.EnvironmentEntry;
import com.sun.enterprise.deployment.web.InitializationParameter;
import com.sun.enterprise.deployment.web.MultipartConfig;
import org.glassfish.web.deployment.descriptor.MultipartConfigDescriptor;
import org.glassfish.web.deployment.descriptor.WebComponentDescriptorImpl;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;

/**
 * This node is responsible for handling the servlet xml sub tree
 *
 * @author  Jerome Dochez
 * @version
 */
public class ServletNode extends DisplayableComponentNode<WebComponentDescriptor> {

    private final static XMLElement tag =
        new XMLElement(WebTagNames.SERVLET);

    private WebComponentDescriptor descriptor;

    /** Creates new ServletNode */
    public ServletNode() {
        super();
        registerElementHandler(new XMLElement(WebTagNames.ROLE_REFERENCE), SecurityRoleRefNode.class);
        registerElementHandler(new XMLElement(WebTagNames.INIT_PARAM), InitParamNode.class);
        registerElementHandler(new XMLElement(WebTagNames.RUNAS_SPECIFIED_IDENTITY),
                                                             RunAsNode.class, "setRunAsIdentity");
        registerElementHandler(new XMLElement(WebTagNames.MULTIPART_CONFIG), MultipartConfigNode.class);

    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
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
     * Adds  a new DOL descriptor instance to the descriptor instance associated with
     * this XMLNode
     *
     * @param newDescriptor the new descriptor
     */
    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof RoleReference) {
            if (DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
                DOLUtils.getDefaultLogger().fine("Adding security role ref " + newDescriptor);
            }
            descriptor.addSecurityRoleReference(
                        (RoleReference) newDescriptor);
        } else if (newDescriptor instanceof EnvironmentEntry) {
            if (DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
                DOLUtils.getDefaultLogger().fine("Adding init-param " + newDescriptor);
            }
            descriptor.addInitializationParameter(
                        (InitializationParameter) newDescriptor);
        } else if (newDescriptor instanceof MultipartConfig) {
            descriptor.setMultipartConfig((MultipartConfig)newDescriptor);
        } else super.addDescriptor(newDescriptor);
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(WebTagNames.NAME, "setName");
        table.put(WebTagNames.SERVLET_NAME, "setCanonicalName");
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
        if (WebTagNames.SERVLET_CLASS.equals(element.getQName())) {
            descriptor.setServlet(true);
            descriptor.setWebComponentImplementation(value);
        } else if (WebTagNames.JSP_FILENAME.equals(element.getQName())) {
            descriptor.setServlet(false);
            descriptor.setWebComponentImplementation(value);
        } else if (WebTagNames.LOAD_ON_STARTUP.equals(element.getQName())) {
            if (value.trim().equals("")) {
                descriptor.setLoadOnStartUp(Integer.MAX_VALUE);
            } else {
                descriptor.setLoadOnStartUp(Integer.valueOf(value));
            }
        } else if (WebTagNames.ENABLED.equals(element.getQName())) {
            descriptor.setEnabled(Boolean.parseBoolean(value));
        } else if (WebTagNames.ASYNC_SUPPORTED.equals(element.getQName())) {
            descriptor.setAsyncSupported(Boolean.valueOf(value));
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, WebComponentDescriptor descriptor) {

        Node myNode = super.writeDescriptor(parent, descriptor);
        appendTextChild(myNode, WebTagNames.SERVLET_NAME, descriptor.getCanonicalName());
        if (descriptor.isServlet()) {
            appendTextChild(myNode, WebTagNames.SERVLET_CLASS, descriptor.getWebComponentImplementation());
        } else {
            appendTextChild(myNode, WebTagNames.JSP_FILENAME, descriptor.getWebComponentImplementation());
        }

        // init-param*
        WebCommonNode.addInitParam(myNode, WebTagNames.INIT_PARAM, descriptor.getInitializationParameters());

        if (descriptor.getLoadOnStartUp()!=null) {
            appendTextChild(myNode, WebTagNames.LOAD_ON_STARTUP, String.valueOf(descriptor.getLoadOnStartUp()));
        }

        appendTextChild(myNode, WebTagNames.ENABLED, String.valueOf(descriptor.isEnabled()));
        if (descriptor.isAsyncSupported() != null) {
            appendTextChild(myNode, WebTagNames.ASYNC_SUPPORTED, String.valueOf(descriptor.isAsyncSupported()));
        }

        // run-as
        RunAsIdentityDescriptor runAs = descriptor.getRunAsIdentity();
        if (runAs!=null) {
            RunAsNode runAsNode = new RunAsNode();
            runAsNode.writeDescriptor(myNode, WebTagNames.RUNAS_SPECIFIED_IDENTITY, runAs);
        }

        // sercurity-role-ref*
        Enumeration roleRefs = descriptor.getSecurityRoleReferences();
        SecurityRoleRefNode roleRefNode = new SecurityRoleRefNode();
        while (roleRefs.hasMoreElements()) {
            roleRefNode.writeDescriptor(myNode, WebTagNames.ROLE_REFERENCE,
                            (RoleReference) roleRefs.nextElement());
        }

        // multipart-config
        MultipartConfigDescriptor multipartConfigDesc =
                (MultipartConfigDescriptor)descriptor.getMultipartConfig();
        if (multipartConfigDesc != null) {
            MultipartConfigNode multipartConfigNode = new MultipartConfigNode();
            multipartConfigNode.writeDescriptor(myNode, WebTagNames.MULTIPART_CONFIG,
                    multipartConfigDesc);
        }

        return myNode;
    }
}
