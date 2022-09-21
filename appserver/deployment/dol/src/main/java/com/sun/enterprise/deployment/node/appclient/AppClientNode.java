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

package com.sun.enterprise.deployment.node.appclient;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.node.AbstractBundleNode;
import com.sun.enterprise.deployment.node.DataSourceDefinitionNode;
import com.sun.enterprise.deployment.node.EjbLocalReferenceNode;
import com.sun.enterprise.deployment.node.EjbReferenceNode;
import com.sun.enterprise.deployment.node.EntityManagerFactoryReferenceNode;
import com.sun.enterprise.deployment.node.EnvEntryNode;
import com.sun.enterprise.deployment.node.JMSConnectionFactoryDefinitionNode;
import com.sun.enterprise.deployment.node.JMSDestinationDefinitionNode;
import com.sun.enterprise.deployment.node.JndiEnvRefNode;
import com.sun.enterprise.deployment.node.LifecycleCallbackNode;
import com.sun.enterprise.deployment.node.MailSessionNode;
import com.sun.enterprise.deployment.node.MessageDestinationNode;
import com.sun.enterprise.deployment.node.MessageDestinationRefNode;
import com.sun.enterprise.deployment.node.ResourceEnvRefNode;
import com.sun.enterprise.deployment.node.ResourceRefNode;
import com.sun.enterprise.deployment.node.SaxParserHandler;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.AppClientRuntimeNode;
import com.sun.enterprise.deployment.node.runtime.GFAppClientRuntimeNode;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.ApplicationClientTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.deployment.common.JavaEEResourceType;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Node;

/**
 * This class is responsible for handling app clients
 *
 * @author  Sheetal Vartak
 * @version
 */

@Service
public class AppClientNode extends AbstractBundleNode<ApplicationClientDescriptor> {

    //app client 1.2
    private static final String PUBLIC_DTD_ID_12 = "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.2//EN";
    private static final String SYSTEM_ID_12 = "http://java.sun.com/dtd/application-client_1_2.dtd";

    //app client 1.3
    private static final String PUBLIC_DTD_ID = "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.3//EN";
    private static final String SYSTEM_ID = "http://java.sun.com/dtd/application-client_1_3.dtd";

    private static final String SCHEMA_ID_14 = "application-client_1_4.xsd";

    private static final String SCHEMA_ID_15 = "application-client_5.xsd";
    private static final String SCHEMA_ID_16 = "application-client_6.xsd";
    private static final String SCHEMA_ID_17 = "application-client_7.xsd";
    private static final String SCHEMA_ID_18 = "application-client_8.xsd";
    private static final String SCHEMA_ID = "application-client_9.xsd";
    public static final String SPEC_VERSION = "9";
    private final static List<String> systemIDs = initSystemIDs();

    private static final XMLElement tag = new XMLElement(ApplicationClientTagNames.APPLICATION_CLIENT_TAG);

    private static List<String> initSystemIDs() {
        final ArrayList<String> systemIDs = new ArrayList<>();
        systemIDs.add(SCHEMA_ID);
        systemIDs.add(SCHEMA_ID_14);
        systemIDs.add(SCHEMA_ID_15);
        systemIDs.add(SCHEMA_ID_16);
        systemIDs.add(SCHEMA_ID_17);
        systemIDs.add(SCHEMA_ID_18);
        return Collections.unmodifiableList(systemIDs);
    }


    public AppClientNode() {
        registerElementHandler(new XMLElement(TagNames.ENVIRONMENT_PROPERTY), EnvEntryNode.class, "addEnvironmentProperty");
        registerElementHandler(new XMLElement(TagNames.EJB_REFERENCE), EjbReferenceNode.class);
        registerElementHandler(new XMLElement(TagNames.EJB_LOCAL_REFERENCE), EjbLocalReferenceNode.class);
        JndiEnvRefNode<?> serviceRefNode = serviceLocator.getService(JndiEnvRefNode.class, WebServicesTagNames.SERVICE_REF);
        if (serviceRefNode != null) {
            registerElementHandler(new XMLElement(WebServicesTagNames.SERVICE_REF), serviceRefNode.getClass(),"addServiceReferenceDescriptor");
        }
        registerElementHandler(new XMLElement(TagNames.RESOURCE_REFERENCE), ResourceRefNode.class, "addResourceReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.RESOURCE_ENV_REFERENCE), ResourceEnvRefNode.class, "addResourceEnvReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.MESSAGE_DESTINATION_REFERENCE), MessageDestinationRefNode.class, "addMessageDestinationReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.PERSISTENCE_UNIT_REF), EntityManagerFactoryReferenceNode.class, "addEntityManagerFactoryReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.MESSAGE_DESTINATION), MessageDestinationNode.class, "addMessageDestination");
        registerElementHandler(new XMLElement(TagNames.POST_CONSTRUCT), LifecycleCallbackNode.class, "addPostConstructDescriptor");
        registerElementHandler(new XMLElement(TagNames.PRE_DESTROY), LifecycleCallbackNode.class, "addPreDestroyDescriptor");
        registerElementHandler(new XMLElement(TagNames.DATA_SOURCE), DataSourceDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(TagNames.MAIL_SESSION), MailSessionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(TagNames.JMS_CONNECTION_FACTORY), JMSConnectionFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(TagNames.JMS_DESTINATION), JMSDestinationDefinitionNode.class, "addResourceDescriptor");

        SaxParserHandler.registerBundleNode(this, ApplicationClientTagNames.APPLICATION_CLIENT_TAG);
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
    public Map<String, Class<?>> registerRuntimeBundle(final Map<String, String> publicIDToDTD,
        final Map<String, List<Class<?>>> versionUpgrades) {
        final Map<String, Class<?>> result = new HashMap<>();
        result.put(AppClientRuntimeNode.registerBundle(publicIDToDTD), AppClientRuntimeNode.class);
        result.put(GFAppClientRuntimeNode.registerBundle(publicIDToDTD), GFAppClientRuntimeNode.class);
        return result;
    }

    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof EjbReferenceDescriptor) {
            DOLUtils.getDefaultLogger().fine("Adding ejb ref " + newDescriptor);
            getDescriptor().addEjbReferenceDescriptor((EjbReferenceDescriptor) newDescriptor);
        } else {
            super.addDescriptor(newDescriptor);
        }
    }

    @Override
    protected ApplicationClientDescriptor createDescriptor() {
        return new ApplicationClientDescriptor();
    }

    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(ApplicationClientTagNames.CALLBACK_HANDLER, "setCallbackHandler");
        return table;
    }

    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
    }

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
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.MODULE_NAME.equals(element.getQName())) {
            getDescriptor().getModuleDescriptor().setModuleName(value);
        } else {
            super.setElementValue(element, value);
        }
    }

    @Override
    public Node writeDescriptor(Node parent, ApplicationClientDescriptor appclientDesc) {
        Node appclientNode = super.writeDescriptor(parent, appclientDesc);

        // env-entry*
        writeEnvEntryDescriptors(appclientNode, appclientDesc.getEnvironmentProperties().iterator());

        // ejb-ref * and ejb-local-ref*
        writeEjbReferenceDescriptors(appclientNode, appclientDesc.getEjbReferenceDescriptors().iterator());

        // service-ref*
        writeServiceReferenceDescriptors(appclientNode, appclientDesc.getServiceReferenceDescriptors().iterator());

        // resource-ref*
        writeResourceRefDescriptors(appclientNode, appclientDesc.getResourceReferenceDescriptors().iterator());

        // resource-env-ref*
        writeResourceEnvRefDescriptors(appclientNode, appclientDesc.getResourceEnvReferenceDescriptors().iterator());

        // message-destination-ref*
        writeMessageDestinationRefDescriptors(appclientNode, appclientDesc.getMessageDestinationReferenceDescriptors().iterator());

        // persistence-unit-ref*
        writeEntityManagerFactoryReferenceDescriptors(appclientNode, appclientDesc.getEntityManagerFactoryReferenceDescriptors().iterator());

        // post-construct
        writeLifeCycleCallbackDescriptors(appclientNode, TagNames.POST_CONSTRUCT, appclientDesc.getPostConstructDescriptors());

        // pre-destroy
        writeLifeCycleCallbackDescriptors(appclientNode, TagNames.PRE_DESTROY, appclientDesc.getPreDestroyDescriptors());

        // datasource-definition*
        writeResourceDescriptors(appclientNode, appclientDesc.getResourceDescriptors(JavaEEResourceType.DSD).iterator());

        // mail-session*
        writeResourceDescriptors(appclientNode, appclientDesc.getResourceDescriptors(JavaEEResourceType.MSD).iterator());

        // jms-connection-factory-definition*
        writeResourceDescriptors(appclientNode, appclientDesc.getResourceDescriptors(JavaEEResourceType.JMSCFDD).iterator());

        // jms-destination-definition*
        writeResourceDescriptors(appclientNode, appclientDesc.getResourceDescriptors(JavaEEResourceType.JMSDD).iterator());

        appendTextChild(appclientNode, ApplicationClientTagNames.CALLBACK_HANDLER, appclientDesc.getCallbackHandler());

        // message-destination*
        writeMessageDestinations(appclientNode, appclientDesc.getMessageDestinations().iterator());

        return appclientNode;

    }

    @Override
    public String getSpecVersion() {
        return SPEC_VERSION;
    }

}
