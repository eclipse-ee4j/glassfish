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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.node.*;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.TagNames;
import org.glassfish.web.WarType;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

import java.util.*;

/**
 * This node is responsible for handling the web-app xml tree
 *
 * @author  Jerome Dochez
 * @version
 */
public class WebBundleNode extends WebCommonNode<WebBundleDescriptorImpl> {

    public final static XMLElement tag = new XMLElement(WebTagNames.WEB_BUNDLE);

    /**
     * The public ID for my documents.
     */
    public final static String PUBLIC_DTD_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public final static String PUBLIC_DTD_ID_12 = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    /**
     * The system ID of my documents.
     */
    public final static String SYSTEM_ID = "http://java.sun.com/dtd/web-app_2_3.dtd";
    public final static String SYSTEM_ID_12 = "http://java.sun.com/dtd/web-app_2_2.dtd";

    public final static String SCHEMA_ID_24 = "web-app_2_4.xsd";
    public final static String SCHEMA_ID_25 = "web-app_2_5.xsd";
    public final static String SCHEMA_ID_30 = "web-app_3_0.xsd";
    public final static String SCHEMA_ID_31 = "web-app_3_1.xsd";
    public final static String SCHEMA_ID_40 = "web-app_4_0.xsd";
    public final static String SCHEMA_ID = "web-app_5_0.xsd";
    private final static List<String> systemIDs = initSystemIDs();


    private static List<String> initSystemIDs() {
        List<String> systemIDs = new ArrayList<String>();
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
     public Map<String,Class> registerRuntimeBundle(final Map<String,String> publicIDToDTD, Map<String, List<Class>> versionUpgrades) {
        final Map<String,Class> result = new HashMap<String,Class>();
        for (ConfigurationDeploymentDescriptorFile wddFile :
                DOLUtils.getConfigurationDeploymentDescriptorFiles(
                        habitat, WarType.ARCHIVE_TYPE)) {

            wddFile.registerBundle(result, publicIDToDTD, versionUpgrades);
        }

        return result;
    }

    @Override
    public Collection<String> elementsAllowingEmptyValue() {
        final Set<String> result = new HashSet<String>();
        result.add(WebTagNames.LOAD_ON_STARTUP);
        return result;
    }

    @Override
    public Collection<String> elementsPreservingWhiteSpace() {
        final Set<String> result = new HashSet<String>();
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
        } else {
            return super.endElement(element);
        }
    }

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    @Override
    public WebBundleDescriptorImpl getDescriptor() {
        if (descriptor==null) {
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

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent,
        WebBundleDescriptorImpl webBundleDesc) {

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
