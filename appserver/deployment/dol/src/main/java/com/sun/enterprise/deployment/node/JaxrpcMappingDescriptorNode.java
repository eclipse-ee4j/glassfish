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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.JaxrpcMappingDescriptor;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.jvnet.hk2.annotations.Service;
import org.xml.sax.Attributes;

import java.util.*;

/**
 * Root node for jaxrpc mapping deployment descriptor
 *
 * @author  Kenneth Saks
 * @version
 */
@Service
public class JaxrpcMappingDescriptorNode extends AbstractBundleNode

{

    public final static XMLElement ROOT_ELEMENT =
        new XMLElement(WebServicesTagNames.JAXRPC_MAPPING_FILE_ROOT);

    public final static String SCHEMA_ID = "j2ee_jaxrpc_mapping_1_1.xsd";
    private final static List<String> systemIDs = initSystemIDs();

    private static final Set complexElements = initComplexElements();
    private JaxrpcMappingDescriptor descriptor=null;
    private String javaPackage=null;

    // true if mapping file contains more than just package->namespace mappings.
    private boolean complexMapping=false;

    private static Set initComplexElements() {
        Set complexElements = new HashSet();
        complexElements.add(WebServicesTagNames.JAVA_XML_TYPE_MAPPING);
        complexElements.add(WebServicesTagNames.EXCEPTION_MAPPING);
        complexElements.add(WebServicesTagNames.SERVICE_INTERFACE_MAPPING);
        complexElements.add
            (WebServicesTagNames.SERVICE_ENDPOINT_INTERFACE_MAPPING);
        return Collections.unmodifiableSet(complexElements);
    }

    private static List<String> initSystemIDs() {
        ArrayList<String> systemIDs = new ArrayList<String>();
        systemIDs.add(SCHEMA_ID);
        return Collections.unmodifiableList(systemIDs);
    }

    public JaxrpcMappingDescriptorNode() {
        descriptor = new JaxrpcMappingDescriptor();
        SaxParserHandler.registerBundleNode(this, WebServicesTagNames.JAXRPC_MAPPING_FILE_ROOT);
    }

    @Override
    public String registerBundle(Map<String, String> publicIDToSystemIDMapping) {
        return ROOT_ELEMENT.getQName();
    }

    @Override
    public Map<String, Class> registerRuntimeBundle(Map<String, String> publicIDToSystemIDMapping, final Map<String, List<Class>> versionUpgrades) {
        return Collections.EMPTY_MAP;
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return ROOT_ELEMENT;
    }

    /**
     * @return the DOCTYPE of the XML file
     */
    public String getDocType() {
        return null;
    }

    /**
     * @return the SystemID of the XML file
     */
    public String getSystemID() {
        return SCHEMA_ID;
    }

    /**
     * @return the list of SystemID of the XML schema supported
     */
    public List<String> getSystemIDs() {
        return systemIDs;
    }

        /**
     * @return the complete URL for J2EE schemas
     */
    protected String getSchemaURL() {
       return WebServicesTagNames.IBM_NAMESPACE + "/" + getSystemID();
    }

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {
        return descriptor;
    }

    public void startElement(XMLElement element, Attributes attributes) {
        if( complexMapping ) {
            // NOTE : we don't call super.startElement in this case because
            // we don't need to process any of the attributes
            return;
        } else if( complexElements.contains(element.getQName()) ) {
            complexMapping = true;
            descriptor.setIsSimpleMapping(false);
            // NOTE : we don't call super.startElement in this case because
            // we don't need to process any of the attributes
        } else {
            super.startElement(element, attributes);
        }
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (complexMapping) {
            // We only gather namespace->package mapping. In exhaustive(complex)
            // mapping case, it's enough to just capture the fact that we
            // have complex mapping info.  The actual processing of the elements
            // will be done by mapping file modeler during deployment
            return;
        } else if(WebServicesTagNames.PACKAGE_TYPE.equals(element.getQName())) {
            javaPackage = value;
        } else if(WebServicesTagNames.NAMESPACE_URI.equals(element.getQName())){
            descriptor.addMapping(javaPackage, value);
            javaPackage = null;
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * @return the default spec version level this node complies to
     */
    public String getSpecVersion() {
        return "1.1";
    }


}
