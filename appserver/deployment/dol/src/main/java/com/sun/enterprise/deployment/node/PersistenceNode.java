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

import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.deployment.xml.PersistenceTagNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jvnet.hk2.annotations.Service;

/**
 * Represents the top level node, i.e. persistence node in persistence.xsd.
 * Since this is a top level node, it extends {@link AbstractBundleNode}. This class
 * registers a handler {@link PersistenceNode} which is responsible for reading
 * the persistence-unit elements.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class PersistenceNode extends AbstractBundleNode {

    public final static String SCHEMA_NS =
            "http://java.sun.com/xml/ns/persistence"; // NOI18N

    public final static String SCHEMA_ID_1_0 = "persistence_1_0.xsd"; // NOI18N

    public final static String SCHEMA_ID = "persistence_2_0.xsd"; // NOI18N

    private final static List<String> systemIDs = initSystemIDs();

    // The XML tag associated with this Node
    public final static XMLElement ROOT_ELEMENT = new XMLElement(
            PersistenceTagNames.PERSISTENCE);

    private PersistenceUnitsDescriptor persistenceUnitsDescriptor;

    private static final String SPEC_VERSION = "2.0";

    private static List<String> initSystemIDs() {
        List<String> systemIDs = new ArrayList<String>();
        systemIDs.add(SCHEMA_ID);
        systemIDs.add(SCHEMA_ID_1_0);
        return Collections.unmodifiableList(systemIDs);
    }

    /**
     * This is the default constructor which is also called from other
     * constructors of this class. Inside this constructor, we clear the
     * handlers set up by super classes' constructors because they are
     * not applicable in the context of PersistenceNode because
     * unlike standard Java EE schemas, persistence.xsd does not include
     * javaee_5.xsd for things like description, version etc.
     */
    public PersistenceNode() {
        // clear all the handlers set up by super classes.
        if (handlers != null) handlers.clear();
        registerElementHandler(
                new XMLElement(PersistenceTagNames.PERSISTENCE_UNIT),
                PersistenceUnitNode.class);
        SaxParserHandler.registerBundleNode(this, PersistenceTagNames.PERSISTENCE);
    }

    public PersistenceNode(PersistenceUnitsDescriptor persistenceUnitsDescriptor) {
        this();
        this.persistenceUnitsDescriptor = persistenceUnitsDescriptor;
    }

    @Override
    public PersistenceUnitsDescriptor getDescriptor() {
        return persistenceUnitsDescriptor;
    }

    // This method is called when parser has parsed one <persistence-unit>
    @Override
    public void addDescriptor(Object descriptor) {
        final PersistenceUnitDescriptor pud = PersistenceUnitDescriptor.class.cast(descriptor);
        getDescriptor().addPersistenceUnitDescriptor(pud);
    }

    @Override
    public String registerBundle(Map<String, String> publicIDToSystemIDMapping) {
        return ROOT_ELEMENT.getQName();
    }

    @Override
    public Map<String, Class> registerRuntimeBundle(Map<String, String> publicIDToSystemIDMapping, final Map<String, List<Class>> versionUpgrades) {
        return Collections.EMPTY_MAP;
    }

    public String getDocType() {
        return null;
    }

    public String getSystemID() {
        return SCHEMA_ID;
    }

    public String getNameSpace() {
        return SCHEMA_NS;
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return ROOT_ELEMENT;
    }

    public List<String> getSystemIDs() {
        return systemIDs;
    }

    public String getSpecVersion() {
        return SPEC_VERSION;
    }

    // Currently this method is commented because
    // it is not needed now as we are not writing out
    // persistence.xml file to generated directory. When we decide to
    // write out a "full" persistence.xml file, we will need this method.
    // When you uncomment, you need to check the actual logic as well.
//    @Override public Node writeDescriptor(Node parent, Descriptor descriptor) {
//        if(descriptor instanceof PersistenceUnitDescriptor) {
//            Element child = getOwnerDocument(parent).createElementNS(TagNames.PERSISTENCE_XML_NAMESPACE, PersistenceTagNames.ENTITY_MANAGER);
//            Element bundleNode = (Element)parent.appendChild(child);
//            bundleNode.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", TagNames.PERSISTENCE_XML_NAMESPACE);
//            bundleNode.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", W3C_XML_SCHEMA_INSTANCE);
//
//            String schemaLocation = TagNames.PERSISTENCE_XML_NAMESPACE + " " + getSchemaURL();
//            String clientSchemaLocation = PersistenceUnitDescriptor.class.cast(descriptor).getSchemaLocation();
//            if (clientSchemaLocation!=null) {
//                schemaLocation = schemaLocation + " " + clientSchemaLocation;
//            }
//            bundleNode.setAttributeNS(W3C_XML_SCHEMA_INSTANCE, SCHEMA_LOCATION_TAG, schemaLocation);
//            TODO: Code for all other elements for this node goes here.
//            return bundleNode;
//        } else {
//            return super.writeDescriptor(parent, descriptor);
//        }
//    }
}
