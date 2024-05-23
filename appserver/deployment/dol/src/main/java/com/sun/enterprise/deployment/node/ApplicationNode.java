/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.ApplicationTagNames;
import com.sun.enterprise.deployment.xml.ConcurrencyTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.api.deployment.archive.EarArchiveType;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_SERVICE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MANAGED_EXECUTOR;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MANAGED_SCHEDULED_EXECUTOR;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MANAGED_THREAD_FACTORY;

/**
 * This class is responsible for loading and saving XML elements
 *
 * @author  Jerome Dochez
 * @version
 */
@Service
public class ApplicationNode extends AbstractBundleNode<Application> {

   /**
    * The public ID.
    */
    private static final String PUBLIC_DTD_ID = "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN";
    private static final String PUBLIC_DTD_ID_12 = "-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN";
    /**
     * The system ID.
     */
    private static final String SYSTEM_ID = "http://java.sun.com/dtd/application_1_3.dtd";
    private static final String SYSTEM_ID_12 = "http://java.sun.com/dtd/application_1_2.dtd";

    private static final String SCHEMA_ID_14 = "application_1_4.xsd";

    private static final String SCHEMA_ID_15 = "application_5.xsd";
    private static final String SCHEMA_ID_16 = "application_6.xsd";
    private static final String SCHEMA_ID_17 = "application_7.xsd";
    private static final String SCHEMA_ID_18 = "application_8.xsd";
    private static final String SCHEMA_ID_19 = "application_9.xsd";
    private static final String SCHEMA_ID = "application_10.xsd";

    public static final String SPEC_VERSION = "10";

    private static final List<String> systemIDs = initSystemIDs();

    // The XML tag associated with this Node
    private static final XMLElement tag = new XMLElement(ApplicationTagNames.APPLICATION);

    private final static List<String> initSystemIDs() {
        List<String> systemIDs = new ArrayList<>();
        systemIDs.add(SCHEMA_ID);
        systemIDs.add(SCHEMA_ID_14);
        systemIDs.add(SCHEMA_ID_15);
        systemIDs.add(SCHEMA_ID_16);
        systemIDs.add(SCHEMA_ID_17);
        systemIDs.add(SCHEMA_ID_18);
        systemIDs.add(SCHEMA_ID_19);
        return Collections.unmodifiableList(systemIDs);
    }

    // The DOL Descriptor we are working for
    private Application descriptor;

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
        Map<String, List<Class<?>>> versionUpgrades) {
        final Map<String, Class<?>> result = new HashMap<>();
        for (ConfigurationDeploymentDescriptorFile<?> confDD : DOLUtils
            .getConfigurationDeploymentDescriptorFiles(serviceLocator, EarArchiveType.ARCHIVE_TYPE)) {
            confDD.registerBundle(result, publicIDToDTD, versionUpgrades);
        }
        return result;
    }

    @Override
    public Collection<String> elementsAllowingEmptyValue() {
        final Set<String> result = new HashSet<>();
        result.add(ApplicationTagNames.LIBRARY_DIRECTORY);
        result.add(ConcurrencyTagNames.QUALIFIER);
        return result;
    }

    @Override
    protected String topLevelTagName() {
        return ApplicationTagNames.APPLICATION_NAME;
    }

    @Override
    protected String topLevelTagValue(Application descriptor) {
        return descriptor.getAppName();
    }

    /** Creates new ApplicationNode */
    public ApplicationNode() {
        super();
        registerElementHandler(new XMLElement(ApplicationTagNames.MODULE), ModuleNode.class, "addModule");
        registerElementHandler(new XMLElement(TagNames.ROLE), SecurityRoleNode.class, "addAppRole");
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
        registerElementHandler(new XMLElement(TagNames.PERSISTENCE_CONTEXT_REF), EntityManagerReferenceNode.class, "addEntityManagerReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.PERSISTENCE_UNIT_REF), EntityManagerFactoryReferenceNode.class, "addEntityManagerFactoryReferenceDescriptor");
        registerElementHandler(new XMLElement(TagNames.MESSAGE_DESTINATION), MessageDestinationNode.class, "addMessageDestination");
        registerElementHandler(new XMLElement(TagNames.DATA_SOURCE), DataSourceDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(TagNames.MAIL_SESSION), MailSessionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(TagNames.CONNECTION_FACTORY), ConnectionFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(TagNames.ADMINISTERED_OBJECT), AdministeredObjectDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(TagNames.JMS_CONNECTION_FACTORY), JMSConnectionFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(TagNames.JMS_DESTINATION), JMSDestinationDefinitionNode.class, "addResourceDescriptor");

        registerElementHandler(new XMLElement(MANAGED_EXECUTOR), ManagedExecutorDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(MANAGED_THREAD_FACTORY), ManagedThreadFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(MANAGED_SCHEDULED_EXECUTOR), ManagedScheduledExecutorDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(CONTEXT_SERVICE), ContextServiceDefinitionNode.class, "addResourceDescriptor");

        SaxParserHandler.registerBundleNode(this, ApplicationTagNames.APPLICATION);
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
    }

   /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        Application application = getDescriptor();
        if (element.getQName().equals(
            ApplicationTagNames.LIBRARY_DIRECTORY)) {
            application.setLibraryDirectory(value);
        } else if(element.getQName().equals(
            ApplicationTagNames.APPLICATION_NAME)) {
            application.setAppName(value);
        } else if (element.getQName().equals(
            ApplicationTagNames.INITIALIZE_IN_ORDER)) {
            application.setInitializeInOrder(Boolean.valueOf(value));
        } else {
            super.setElementValue(element, value);
        }
    }


    /**
     * Adds  a new DOL descriptor instance to the descriptor instance associated with
     * this XMLNode
     *
     * @param newDescriptor the new descriptor
     */
    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof BundleDescriptor) {
            if (DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
                DOLUtils.getDefaultLogger().fine("In  " + toString() + " adding descriptor " + newDescriptor);
            }
            descriptor.addBundleDescriptor((BundleDescriptor) newDescriptor);
        } else if (newDescriptor instanceof EjbReferenceDescriptor) {
            descriptor.addEjbReferenceDescriptor((EjbReferenceDescriptor) newDescriptor);
        }
    }

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    @Override
    public Application getDescriptor() {
        if (descriptor == null) {
            descriptor = Application.createApplication();
        }
        return descriptor;
    }

    /**
     * @return the DOCTYPE  of the XML file
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
     * @param parent parent node
     * @param application  to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, Application application) {
        Node appNode = super.writeDescriptor(parent, application);

        // initialize-in-order
        appendTextChild(appNode, ApplicationTagNames.INITIALIZE_IN_ORDER, String.valueOf(application.isInitializeInOrder()));

        // module
        ModuleNode moduleNode = new ModuleNode();
        for (ModuleDescriptor md :  application.getModules()) {
            moduleNode.writeDescriptor(appNode, ApplicationTagNames.MODULE, md);
        }

        // security-role*
        // this information is not written out since it's already included
        // in the sub module deployment descriptors


        // library-directory
        if (application.getLibraryDirectoryRawValue() != null) {
            appendTextChild(appNode, ApplicationTagNames.LIBRARY_DIRECTORY,
                application.getLibraryDirectoryRawValue());
        }

       // env-entry*
        writeEnvEntryDescriptors(appNode, application.getEnvironmentProperties().iterator());

        // ejb-ref * and ejb-local-ref*
        writeEjbReferenceDescriptors(appNode, application.getEjbReferenceDescriptors().iterator());

        // service-ref*
        writeServiceReferenceDescriptors(appNode, application.getServiceReferenceDescriptors().iterator());

        // resource-ref*
        writeResourceRefDescriptors(appNode, application.getResourceReferenceDescriptors().iterator());

        // resource-env-ref*
        writeResourceEnvRefDescriptors(appNode, application.getResourceEnvReferenceDescriptors().iterator());

        // message-destination-ref*
        writeMessageDestinationRefDescriptors(appNode, application.getMessageDestinationReferenceDescriptors().iterator());

        // persistence-context-ref*
        writeEntityManagerReferenceDescriptors(appNode, application.getEntityManagerReferenceDescriptors().iterator());

        // persistence-unit-ref*
        writeEntityManagerFactoryReferenceDescriptors(appNode, application.getEntityManagerFactoryReferenceDescriptors().iterator());

        // message-destination*
        writeMessageDestinations(appNode, application.getMessageDestinations().iterator());

        // all descriptors (includes DSD, MSD, JMSCFD, JMSDD,AOD, CFD)*
        writeResourceDescriptors(appNode, application.getAllResourcesDescriptors().iterator());

        return appNode;
    }

    /**
     * @return the default spec version level this node complies to
     */
    @Override
    public String getSpecVersion() {
        return SPEC_VERSION;
    }

}