/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.node.SaxParserHandler;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.ConcurrencyTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.api.deployment.archive.WarArchiveType;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling the web-app xml tree
 *
 * @author  Jerome Dochez
 */
public class WebBundleNode extends WebCommonNode<WebBundleDescriptorImpl> {

    private static final XMLElement tag = new XMLElement(WebTagNames.WEB_BUNDLE);

    /**
     * The public ID for my documents.
     */
    private static final String PUBLIC_DTD_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    private static final String PUBLIC_DTD_ID_12 = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    /**
     * The system ID of my documents.
     */
    private static final String SYSTEM_ID = "http://java.sun.com/dtd/web-app_2_3.dtd";
    private static final String SYSTEM_ID_12 = "http://java.sun.com/dtd/web-app_2_2.dtd";

    private static final String SCHEMA_ID_24 = "web-app_2_4.xsd";
    private static final String SCHEMA_ID_25 = "web-app_2_5.xsd";
    private static final String SCHEMA_ID_30 = "web-app_3_0.xsd";
    private static final String SCHEMA_ID_31 = "web-app_3_1.xsd";
    private static final String SCHEMA_ID_40 = "web-app_4_0.xsd";
    private static final String SCHEMA_ID = "web-app_5_0.xsd";
    private final static List<String> systemIDs = initSystemIDs();


    private static List<String> initSystemIDs() {
        List<String> systemIDs = new ArrayList<>();
        systemIDs.add(SCHEMA_ID);
        systemIDs.add(SCHEMA_ID_24);
        systemIDs.add(SCHEMA_ID_25);
        systemIDs.add(SCHEMA_ID_30);
        systemIDs.add(SCHEMA_ID_31);
        systemIDs.add(SCHEMA_ID_40);
        return Collections.unmodifiableList(systemIDs);
    }

   /**
    * register this node as a root node capable of loading entire DD files
    *
    * @param publicIDToDTD is a mapping between xml Public-ID to DTD
    * @return the doctype tag name
    */
    @Override
    public String registerBundle(Map<String, String> publicIDToDTD) {
        publicIDToDTD.put(PUBLIC_DTD_ID, SYSTEM_ID);
        publicIDToDTD.put(PUBLIC_DTD_ID_12, SYSTEM_ID_12);
        return tag.getQName();
    }


    @Override
    public Map<String, Class<?>> registerRuntimeBundle(
        final Map<String, String> publicIDToDTD,
        final Map<String, List<Class<?>>> versionUpgrades) {

        final Map<String, Class<?>> result = new HashMap<>();
        for (ConfigurationDeploymentDescriptorFile<?> wddFile : DOLUtils
            .getConfigurationDeploymentDescriptorFiles(serviceLocator, WarArchiveType.ARCHIVE_TYPE)) {
            wddFile.registerBundle(result, publicIDToDTD, versionUpgrades);
        }
        return result;
    }

    @Override
    public Collection<String> elementsAllowingEmptyValue() {
        return Set.of(
            WebTagNames.LOAD_ON_STARTUP,
            ConcurrencyTagNames.QUALIFIER);
    }

    @Override
    public Collection<String> elementsPreservingWhiteSpace() {
        final Set<String> result = new HashSet<>();
        result.add(WebTagNames.URL_PATTERN);
        return result;
    }



    /** Creates new WebBundleNode */
    public WebBundleNode()  {
        super();
        registerElementHandler(new XMLElement(WebTagNames.ABSOLUTE_ORDERING),
               AbsoluteOrderingNode.class, "setAbsoluteOrderingDescriptor");
        SaxParserHandler.registerBundleNode(this, WebTagNames.WEB_BUNDLE);
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.MODULE_NAME.equals(element.getQName())) {
            WebBundleDescriptor bundleDesc = getDescriptor();
            bundleDesc.getModuleDescriptor().setModuleName(value);
        } else if (WebTagNames.DEFAULT_CONTEXT_PATH.equals(element.getQName())) {
            WebBundleDescriptor bundleDesc = getDescriptor();
            bundleDesc.setContextRoot(value);
        } else if (WebTagNames.REQUEST_CHARACTER_ENCODING.equals(element.getQName())) {
            WebBundleDescriptor bundleDesc = getDescriptor();
            bundleDesc.setRequestCharacterEncoding(value);
        } else if (WebTagNames.RESPONSE_CHARACTER_ENCODING.equals(element.getQName())) {
            WebBundleDescriptor bundleDesc = getDescriptor();
            bundleDesc.setResponseCharacterEncoding(value);
        } else {
            super.setElementValue(element, value);
        }
    }

    @Override
    public boolean endElement(XMLElement element) {
        if (WebTagNames.DENY_UNCOVERED_HTTP_METHODS.equals(element.getQName())) {
            descriptor.setDenyUncoveredHttpMethods(true);
            return false;
        }
        return super.endElement(element);
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public WebBundleDescriptorImpl getDescriptor() {
        if (descriptor == null) {
            descriptor = new WebBundleDescriptorImpl();
        }
        return descriptor;
    }


    /**
     * @return the XML tag associated with this XMLNode
     */
    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    /**
     * @return the DOCTYPE of the XML file
     */
    @Override
    public String getDocType() {
        return null;
    }

    /**
     * @return the SystemID of the XML file
     */
    @Override
    public String getSystemID() {
        return SCHEMA_ID;
    }

    /**
     * @return the list of SystemID of the XML schema supported
     */
    @Override
    public List<String> getSystemIDs() {
        return systemIDs;
    }


    @Override
    public Node writeDescriptor(Node parent, WebBundleDescriptorImpl webBundleDesc) {
        Node jarNode = super.writeDescriptor(parent, webBundleDesc);
        if (webBundleDesc.isDenyUncoveredHttpMethods()) {
            appendChild(jarNode, WebTagNames.DENY_UNCOVERED_HTTP_METHODS);
        }
        if (webBundleDesc.getAbsoluteOrderingDescriptor() != null) {
            AbsoluteOrderingNode absOrderingNode = new AbsoluteOrderingNode();
            absOrderingNode.writeDescriptor(jarNode, WebTagNames.ABSOLUTE_ORDERING,
                webBundleDesc.getAbsoluteOrderingDescriptor());
        }
        return jarNode;
    }
}
