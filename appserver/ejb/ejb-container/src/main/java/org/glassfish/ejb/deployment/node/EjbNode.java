/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.ejb.deployment.node;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.node.AdministeredObjectDefinitionNode;
import com.sun.enterprise.deployment.node.ConnectionFactoryDefinitionNode;
import com.sun.enterprise.deployment.node.ContextServiceDefinitionNode;
import com.sun.enterprise.deployment.node.DataSourceDefinitionNode;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.EjbLocalReferenceNode;
import com.sun.enterprise.deployment.node.EjbReferenceNode;
import com.sun.enterprise.deployment.node.EntityManagerFactoryReferenceNode;
import com.sun.enterprise.deployment.node.EntityManagerReferenceNode;
import com.sun.enterprise.deployment.node.EnvEntryNode;
import com.sun.enterprise.deployment.node.JMSConnectionFactoryDefinitionNode;
import com.sun.enterprise.deployment.node.JMSDestinationDefinitionNode;
import com.sun.enterprise.deployment.node.JndiEnvRefNode;
import com.sun.enterprise.deployment.node.MailSessionNode;
import com.sun.enterprise.deployment.node.ManagedExecutorDefinitionNode;
import com.sun.enterprise.deployment.node.ManagedScheduledExecutorDefinitionNode;
import com.sun.enterprise.deployment.node.ManagedThreadFactoryDefinitionNode;
import com.sun.enterprise.deployment.node.MessageDestinationRefNode;
import com.sun.enterprise.deployment.node.ResourceEnvRefNode;
import com.sun.enterprise.deployment.node.ResourceRefNode;
import com.sun.enterprise.deployment.node.SecurityRoleRefNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.CONTEXT_SERVICE;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MANAGED_EXECUTOR;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MANAGED_SCHEDULED_EXECUTOR;
import static com.sun.enterprise.deployment.xml.ConcurrencyTagNames.MANAGED_THREAD_FACTORY;
import static com.sun.enterprise.deployment.xml.TagNames.ADMINISTERED_OBJECT;
import static com.sun.enterprise.deployment.xml.TagNames.CONNECTION_FACTORY;
import static com.sun.enterprise.deployment.xml.TagNames.DATA_SOURCE;
import static com.sun.enterprise.deployment.xml.TagNames.EJB_LOCAL_REFERENCE;
import static com.sun.enterprise.deployment.xml.TagNames.EJB_REFERENCE;
import static com.sun.enterprise.deployment.xml.TagNames.ENVIRONMENT_PROPERTY;
import static com.sun.enterprise.deployment.xml.TagNames.JMS_CONNECTION_FACTORY;
import static com.sun.enterprise.deployment.xml.TagNames.JMS_DESTINATION;
import static com.sun.enterprise.deployment.xml.TagNames.MAIL_SESSION;
import static com.sun.enterprise.deployment.xml.TagNames.MESSAGE_DESTINATION_REFERENCE;
import static com.sun.enterprise.deployment.xml.TagNames.PERSISTENCE_CONTEXT_REF;
import static com.sun.enterprise.deployment.xml.TagNames.PERSISTENCE_UNIT_REF;
import static com.sun.enterprise.deployment.xml.TagNames.RESOURCE_ENV_REFERENCE;
import static com.sun.enterprise.deployment.xml.TagNames.RESOURCE_REFERENCE;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_REF;
import static org.glassfish.ejb.deployment.EjbTagNames.SECURITY_IDENTITY;

/**
 * This class is responsible for handling all common information shared by all types of enterprise
 * beans (MDB, session, entity)
 *
 * @author Jerome Dochez
 * @version
 */
public abstract class EjbNode<S extends EjbDescriptor> extends DisplayableComponentNode<S> {

    /** Creates new EjbNode */
    public EjbNode() {
        super();
        registerElementHandler(new XMLElement(ENVIRONMENT_PROPERTY), EnvEntryNode.class, "addEnvironmentProperty");
        registerElementHandler(new XMLElement(EJB_REFERENCE), EjbReferenceNode.class);
        registerElementHandler(new XMLElement(EJB_LOCAL_REFERENCE), EjbLocalReferenceNode.class);
        JndiEnvRefNode<?> serviceRefNode = serviceLocator.getService(JndiEnvRefNode.class, SERVICE_REF);
        if (serviceRefNode != null) {
            registerElementHandler(new XMLElement(SERVICE_REF), serviceRefNode.getClass(), "addServiceReferenceDescriptor");
        }
        registerElementHandler(new XMLElement(RESOURCE_REFERENCE), ResourceRefNode.class, "addResourceReferenceDescriptor");
        registerElementHandler(new XMLElement(DATA_SOURCE), DataSourceDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(MAIL_SESSION), MailSessionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(CONNECTION_FACTORY), ConnectionFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(ADMINISTERED_OBJECT), AdministeredObjectDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(JMS_CONNECTION_FACTORY), JMSConnectionFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(JMS_DESTINATION), JMSDestinationDefinitionNode.class, "addResourceDescriptor");

        registerElementHandler(new XMLElement(SECURITY_IDENTITY), SecurityIdentityNode.class);
        registerElementHandler(new XMLElement(RESOURCE_ENV_REFERENCE), ResourceEnvRefNode.class, "addResourceEnvReferenceDescriptor");
        registerElementHandler(new XMLElement(MESSAGE_DESTINATION_REFERENCE), MessageDestinationRefNode.class);
        registerElementHandler(new XMLElement(PERSISTENCE_CONTEXT_REF), EntityManagerReferenceNode.class, "addEntityManagerReferenceDescriptor");
        registerElementHandler(new XMLElement(PERSISTENCE_UNIT_REF), EntityManagerFactoryReferenceNode.class, "addEntityManagerFactoryReferenceDescriptor");

        // Use special method for overrides because more than one schedule can be specified on a single method
        registerElementHandler(new XMLElement(EjbTagNames.TIMER), ScheduledTimerNode.class, "addScheduledTimerDescriptorFromDD");

        registerElementHandler(new XMLElement(MANAGED_EXECUTOR), ManagedExecutorDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(MANAGED_THREAD_FACTORY), ManagedThreadFactoryDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(MANAGED_SCHEDULED_EXECUTOR), ManagedScheduledExecutorDefinitionNode.class, "addResourceDescriptor");
        registerElementHandler(new XMLElement(CONTEXT_SERVICE), ContextServiceDefinitionNode.class, "addResourceDescriptor");
    }

    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof EjbReference) {
            if (DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
                DOLUtils.getDefaultLogger().fine("Adding ejb ref " + newDescriptor);
            }
            getEjbDescriptor().addEjbReferenceDescriptor((EjbReferenceDescriptor) newDescriptor);
        } else if (newDescriptor instanceof RunAsIdentityDescriptor) {
            if (DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
                DOLUtils.getDefaultLogger().fine("Adding security-identity" + newDescriptor);
            }
            getEjbDescriptor().setUsesCallerIdentity(false);
            getEjbDescriptor().setRunAsIdentity((RunAsIdentityDescriptor) newDescriptor);
        } else if (newDescriptor instanceof MessageDestinationReferenceDescriptor) {
            MessageDestinationReferenceDescriptor msgDestRef = (MessageDestinationReferenceDescriptor) newDescriptor;
            EjbBundleDescriptorImpl ejbBundle = (EjbBundleDescriptorImpl) getParentNode().getDescriptor();
            // EjbBundle might not be set yet on EjbDescriptor, so set it
            // explicitly here.
            msgDestRef.setReferringBundleDescriptor(ejbBundle);
            getEjbDescriptor().addMessageDestinationReferenceDescriptor(msgDestRef);
        } else {
            super.addDescriptor(newDescriptor);
        }
    }

    @Override
    public S getDescriptor() {
        return getEjbDescriptor();
    }

    public abstract S getEjbDescriptor();

    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.EJB_NAME, "setName");
        table.put(EjbTagNames.EJB_CLASS, "setEjbClassName");
        table.put(TagNames.MAPPED_NAME, "setMappedName");
        return table;
    }

    /**
     * write the common descriptor info to a DOM tree and return it
     *
     * @param ejbNode parent node for the DOM tree
     * @param descriptor the descriptor to write
     */
    protected void writeCommonHeaderEjbDescriptor(Node ejbNode, EjbDescriptor descriptor) {
        appendTextChild(ejbNode, TagNames.EJB_NAME, descriptor.getName());
        appendTextChild(ejbNode, TagNames.MAPPED_NAME, descriptor.getMappedName());
    }


    /**
     * write the security identity information about an EJB
     *
     * @param parent node for the DOM tree
     * @param descriptor the EJB descriptor the security information to be retrieved
     */
    protected void writeSecurityIdentityDescriptor(Node parent, EjbDescriptor descriptor) {
        if (descriptor.getUsesCallerIdentity() == null && descriptor.getRunAsIdentity() == null) {
            return;
        }
        SecurityIdentityNode.writeSecureIdentity(parent, SECURITY_IDENTITY, descriptor);
    }

    /**
     * write the security role references to the DOM Tree
     *
     * @param parentNode for the DOM tree
     * @param refs iterator over the RoleReference descriptors to write
     */
    protected void writeRoleReferenceDescriptors(Node parentNode, Iterator<RoleReference> refs) {
        SecurityRoleRefNode node = new SecurityRoleRefNode();
        while (refs.hasNext()) {
            RoleReference roleRef = refs.next();
            node.writeDescriptor(parentNode, TagNames.ROLE_REFERENCE, roleRef);
        }
    }


    protected static void writeAroundInvokeDescriptors(Node parentNode,
        Iterator<LifecycleCallbackDescriptor> aroundInvokeDescs) {
        if (aroundInvokeDescs == null || !aroundInvokeDescs.hasNext()) {
            return;
        }

        AroundInvokeNode subNode = new AroundInvokeNode();
        while (aroundInvokeDescs.hasNext()) {
            LifecycleCallbackDescriptor next = aroundInvokeDescs.next();
            subNode.writeDescriptor(parentNode, EjbTagNames.AROUND_INVOKE_METHOD, next);
        }
    }


    protected static void writeAroundTimeoutDescriptors(Node parentNode,
        Iterator<LifecycleCallbackDescriptor> aroundTimeoutDescs) {
        if (aroundTimeoutDescs == null || !aroundTimeoutDescs.hasNext()) {
            return;
        }

        AroundTimeoutNode subNode = new AroundTimeoutNode();
        while (aroundTimeoutDescs.hasNext()) {
            LifecycleCallbackDescriptor next = aroundTimeoutDescs.next();
            subNode.writeDescriptor(parentNode, EjbTagNames.AROUND_TIMEOUT_METHOD, next);
        }
    }
}
