/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

/**
 * ConnectorNode.java. This class is responsible for encapsulating all information specific to the Connector DTD
 *
 * Created on February 1, 2002, 3:07 PM
 */
package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.node.AbstractBundleNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.SaxParserHandler;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.XMLNode;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Node;


/**
 * The top connector node class
 * @author Sheetal Vartak
 * @version
 */
@Service
public class ConnectorNode extends AbstractBundleNode<ConnectorDescriptor> {

    // Descriptor class we are using
    private ConnectorDescriptor descriptor;
    public static final String VERSION_10 = "1.0";
    public static final String VERSION_15 = "1.5";
    public static final String VERSION_16 = "1.6";
    public static final String VERSION_17 = "1.7";
    public static final String VERSION_20 = "2.0";

    private String specVersion;

    //connector1.0
    public static final String PUBLIC_DTD_ID_10 = "-//Sun Microsystems, Inc.//DTD Connector 1.0//EN";
    public static final String SYSTEM_ID_10 = "http://java.sun.com/dtd/connector_1_0.dtd";

    //connector1.5
    public final static String PUBLIC_DTD_ID_15 = "-//Sun Microsystems, Inc.//DTD Connector 1.5//EN";
    public final  static String SYSTEM_ID_15 = "http://java.sun.com/dtd/connector_1_5.dtd";
    public final static String SCHEMA_ID_15 = "connector_1_5.xsd";

    //connector1.6
    public final static String PUBLIC_DTD_ID_16 = "-//Sun Microsystems, Inc.//DTD Connector 1.6//EN";
    public final  static String SYSTEM_ID_16 = "http://java.sun.com/dtd/connector_1_6.dtd" ;
    public final static String SCHEMA_ID_16 = "connector_1_6.xsd";

    public final static String PUBLIC_DTD_ID = PUBLIC_DTD_ID_16;
    public final static String SYSTEM_ID = SYSTEM_ID_16;
    public final static String SCHEMA_ID_17 = "connector_1_7.xsd";

    //connector2.0
    public final static String SCHEMA_ID_20 = "connector_2_0.xsd";

    public final static String SCHEMA_ID = SCHEMA_ID_20;
    public final static String SPEC_VERSION = VERSION_20;

    private final static List<String> systemIDs = initSystemIDs();

    private static final  XMLElement TAG = new XMLElement(ConnectorTagNames.CONNECTOR);

    private static List<String> initSystemIDs() {
        List<String> systemIDs = new ArrayList<>();
        systemIDs.add(SCHEMA_ID);
        return Collections.unmodifiableList(systemIDs);
    }

    /**
     * register this node as a root node capable of loading entire DD files
     *
     * @param publicIDToDTD is a mapping between xml Public-ID to DTD
     * @return the doctype tag name
     */
    @Override
    public String registerBundle(Map<String,String> publicIDToDTD) {
        publicIDToDTD.put(PUBLIC_DTD_ID, SYSTEM_ID);
        publicIDToDTD.put(PUBLIC_DTD_ID_10, SYSTEM_ID_10);
        return TAG.getQName();
    }


    @Override
    public Map<String, Class<?>> registerRuntimeBundle(final Map<String, String> publicIDToDTD,
        final Map<String, List<Class<?>>> versionUpgrades) {
        final Map<String, Class<?>> result = new HashMap<>();
        result.put(
            com.sun.enterprise.deployment.node.runtime.connector.ConnectorNode.registerBundle(publicIDToDTD),
            com.sun.enterprise.deployment.node.runtime.connector.ConnectorNode.class);
        return result;
    }


    public ConnectorNode() {
        super();
        registerElementHandler(new XMLElement(ConnectorTagNames.LICENSE), LicenseNode.class, "setLicenseDescriptor");
        SaxParserHandler.registerBundleNode(this, ConnectorTagNames.CONNECTOR);
    }


    @Override
    public ConnectorDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = (ConnectorDescriptor) DescriptorFactory.getDescriptor(getXMLPath());
        }
        return descriptor;
    }


    @Override
    protected boolean setAttributeValue(XMLElement elementName,
        XMLElement attributeName, String value) {
        getDescriptor();
        if (descriptor == null) {
            throw new RuntimeException("Trying to set values on a null descriptor");
        }
        // the version attribute value is the spec version we use
        // and it's only available from schema based xml
        if (attributeName.getQName().equals(ConnectorTagNames.VERSION)) {
            descriptor.setSpecVersion(value);
            specVersion = value;
            return true;
        } else if (attributeName.getQName().equals(TagNames.ID)) {
            // we do not support id attribute for the moment
            return true;
        }

        return false;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        getDescriptor();
        if (descriptor == null) {
            throw new RuntimeException("Trying to set values on a null descriptor");
        }
        if (ConnectorTagNames.SPEC_VERSION.equals(element.getQName())) {
            descriptor.setSpecVersion(value);
            specVersion = value;
            // the version element value is the resourve adapter version
            // and it's only available from dtd based xml
        } else if (ConnectorTagNames.VERSION.equals(element.getQName())) {
            descriptor.setResourceAdapterVersion(value);
        } else if (TagNames.MODULE_NAME.equals(element.getQName())) {
            ConnectorDescriptor bundleDesc = getDescriptor();
            bundleDesc.getModuleDescriptor().setModuleName(value);
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public boolean handlesElement(XMLElement element) {
        if (ConnectorTagNames.RESOURCE_ADAPTER.equals(element.getQName())) {
            return false;
        }
        return super.handlesElement(element);
    }


    @Override
    public XMLNode<?> getHandlerFor(XMLElement element) {
        if (ConnectorTagNames.RESOURCE_ADAPTER.equals(element.getQName())) {
            // For resourceadapter tag, we need to find out what version of DTD we are handling
            // in order to correctly read/write the XML file
            if (VERSION_10.equals(specVersion)) {
                OutBoundRANode outboundRANode = new OutBoundRANode(element);
                outboundRANode.setParentNode(this);
                outboundRANode.createConDefDescriptorFor10();
                return outboundRANode;
            }
            RANode raNode = new RANode(element);
            raNode.setParentNode(this);
            return raNode;
        }
        return super.getHandlerFor(element);
    }


    @Override
    protected XMLElement getXMLRootTag() {
        return TAG;
    }


    @Override
    public void addDescriptor(Object newDescriptor) {
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(ConnectorTagNames.VENDOR_NAME, "setVendorName");
        table.put(ConnectorTagNames.EIS_TYPE, "setEisType");

        // support for 1.0 DTD and 1.5 schema and not 1.5 DTD
        table.put(ConnectorTagNames.RESOURCEADAPTER_VERSION, "setResourceAdapterVersion");
        table.put(ConnectorTagNames.REQUIRED_WORK_CONTEXT, "addRequiredWorkContext");

        return table;
    }


    /**
     * @return null
     */
    @Override
    public String getDocType() {
        return null;
    }


    @Override
    public String getSystemID() {
        return SCHEMA_ID;
    }


    @Override
    public List<String> getSystemIDs() {
        return systemIDs;
    }


    @Override
    public Node writeDescriptor(Node parent, ConnectorDescriptor conDesc) {
        conDesc.setSpecVersion(VERSION_17);
        Node connectorNode = super.writeDescriptor(parent, conDesc);
        appendTextChild(connectorNode, ConnectorTagNames.VENDOR_NAME, conDesc.getVendorName());
        appendTextChild(connectorNode, ConnectorTagNames.EIS_TYPE, conDesc.getEisType());
        appendTextChild(connectorNode, ConnectorTagNames.RESOURCEADAPTER_VERSION, conDesc.getResourceAdapterVersion());

        Iterator<String> requiredInflowContexts = conDesc.getRequiredWorkContexts().iterator();

        for (; requiredInflowContexts.hasNext();) {
            String className = requiredInflowContexts.next();
            appendTextChild(connectorNode, ConnectorTagNames.REQUIRED_WORK_CONTEXT, className);
        }

        // license info
        LicenseNode licenseNode = new LicenseNode();
        connectorNode = licenseNode.writeDescriptor(connectorNode, conDesc);

        // resource adapter node
        RANode raNode = new RANode();
        connectorNode = raNode.writeDescriptor(connectorNode, conDesc);
        return connectorNode;
    }


    /**
     * @return the default spec version level this node complies to
     */
    @Override
    public String getSpecVersion() {
        return SPEC_VERSION;
    }


    /**
     * @return the schema URL
     */
    @Override
    protected String getSchemaURL() {
        return TagNames.JAKARTAEE_NAMESPACE + "/" + getSystemID();
    }
}
