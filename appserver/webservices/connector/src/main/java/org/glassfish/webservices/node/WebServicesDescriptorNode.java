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

package org.glassfish.webservices.node;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.node.AbstractBundleNode;
import com.sun.enterprise.deployment.node.SaxParserHandler;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import java.util.Map;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.webservices.connector.LogUtils;

/**
 * Root node for web services deployment descriptor
 *
 * @author  Kenneth Saks
 * @version
 */
@Service
public class WebServicesDescriptorNode extends AbstractBundleNode<BundleDescriptor> {
    public final static XMLElement ROOT_ELEMENT =
        new XMLElement(WebServicesTagNames.WEB_SERVICES);

    public final static String SCHEMA_ID = "jakartaee_web_services_2_0.xsd";
    public final static String SCHEMA_ID_12 = "javaee_web_services_1_2.xsd";
    public final static String SCHEMA_ID_13 = "javaee_web_services_1_3.xsd";
    public final static String SCHEMA_ID_14 = "javaee_web_services_1_4.xsd";

    public final static String SPEC_VERSION = "2.0";
    private final static List<String> systemIDs = initSystemIDs();
    private static final Logger logger = LogUtils.getLogger();

    private static List<String> initSystemIDs() {
        List<String> sysIDs = new ArrayList<String>();
        sysIDs.add(SCHEMA_ID);
        sysIDs.add(SCHEMA_ID_12);
        sysIDs.add(SCHEMA_ID_13);
        sysIDs.add(SCHEMA_ID_14);
        return Collections.unmodifiableList(sysIDs);

    }

    private BundleDescriptor bundleDescriptor;

    public WebServicesDescriptorNode(BundleDescriptor descriptor) {
        bundleDescriptor = descriptor;
        registerElementHandler(new XMLElement(WebServicesTagNames.WEB_SERVICE),
                               WebServiceNode.class);
        SaxParserHandler.registerBundleNode(this, WebServicesTagNames.WEB_SERVICES);
    }

    public WebServicesDescriptorNode() {
        this(null);
    }

    @Override
    public String registerBundle(Map<String, String> publicIDToSystemIDMapping) {
        return ROOT_ELEMENT.getQName();
    }

    @Override
    public Map<String, Class> registerRuntimeBundle(Map<String, String> publicIDToSystemIDMapping, Map<String, List<Class>> versionUpgrades) {
        return Collections.EMPTY_MAP;
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
     * @return the XML tag associated with this XMLNode
     */
    @Override
    protected XMLElement getXMLRootTag() {
        return ROOT_ELEMENT;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.VERSION.equals(element.getQName())) {
            bundleDescriptor.getWebServices().setSpecVersion(value);
        } else super.setElementValue(element, value);
    }

    /**
     * Adds  a new DOL descriptor instance to the descriptor
     * instance associated with this XMLNode
     *
     * @param descriptor the new descriptor
     */
    @Override
    public void addDescriptor(Object descriptor) {
        WebServicesDescriptor webServicesDesc =
            bundleDescriptor.getWebServices();
        WebService webService = (WebService) descriptor;
        webServicesDesc.addWebService(webService);

        for(Iterator iter = webService.getEndpoints().iterator();
            iter.hasNext();) {
            WebServiceEndpoint next = (WebServiceEndpoint) iter.next();
            if( !next.resolveComponentLink() ) {
                logger.log(Level.INFO, LogUtils.WS_COMP_LINK_NOT_VALID,
                        new Object[]{next.getEndpointName(), next.getLinkName()});
            }
        }

    }

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    @Override
    public BundleDescriptor getDescriptor() {
        return bundleDescriptor;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, BundleDescriptor descriptor) {
        if (parent instanceof Document) {
            Node topNode = super.writeDescriptor(parent, descriptor);
            WebServicesDescriptor webServicesDesc = descriptor.getWebServices();
            WebServiceNode wsNode = new WebServiceNode();
            for(WebService next : webServicesDesc.getWebServices()) {
                wsNode.writeDescriptor(topNode, WebServicesTagNames.WEB_SERVICE,
                                       next);
            }
        }
        return parent;
    }

    /**
     * @return the default spec version level this node complies to
     */
    @Override
    public String getSpecVersion() {
        return SPEC_VERSION;
    }

}

