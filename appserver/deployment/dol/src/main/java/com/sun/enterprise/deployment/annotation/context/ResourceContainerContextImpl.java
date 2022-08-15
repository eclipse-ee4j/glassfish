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

package com.sun.enterprise.deployment.annotation.context;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.WritableJndiNameEnvironment;
import com.sun.enterprise.deployment.core.ResourceDescriptor;
import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.HandlerChainContainer;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;

import java.util.Set;

import org.glassfish.apf.context.AnnotationContext;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * This provides an abstraction for handle resource references.
 *
 * @author Shing Wai Chan
 */
public class ResourceContainerContextImpl extends AnnotationContext
    implements ResourceContainerContext, ComponentContext, ServiceReferenceContainerContext, HandlerContext {

    protected Descriptor descriptor;
    protected String componentClassName;

    public ResourceContainerContextImpl() {
    }

    public ResourceContainerContextImpl(Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Add a reference to an ejb.
     *
     * @param ejbReference the ejb reference
     */
    @Override
    public void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        getEjbReferenceContainer().addEjbReferenceDescriptor(ejbReference);
    }

    /**
     * Looks up an ejb reference with the given name.
     * Return null if it is not found.
     *
     * @param the name of the ejb-reference
     */
    @Override
    public EjbReferenceDescriptor getEjbReference(String name) {
        try {
            return getEjbReferenceContainer().getEjbReference(name);
            // annotation has a corresponding ejb-local-ref/ejb-ref
            // in xml.  Just add annotation info and continue.
            // This logic might change depending on overriding rules
            // and order in which annotations are read w.r.t. to xml.
            // E.g. sparse overriding in xml or loading annotations
            // first.
        } catch (IllegalArgumentException e) {
            // DOL API is (unfortunately) defined to return
            // IllegalStateException if name doesn't exist.

            Application app = getAppFromDescriptor();

            if (app != null) {
                try {
                    // Check for java:app/java:global dependencies at app-level
                    EjbReferenceDescriptor ejbRef = app.getEjbReferenceByName(name);
                     // Make sure it's added to the container context.
                    addEjbReferenceDescriptor(ejbRef);
                } catch (IllegalArgumentException ee) {
                }
            }
        }
        return null;
    }


    protected EjbReferenceContainer getEjbReferenceContainer() {
        return (EjbReferenceContainer) descriptor;
    }


    @Override
    public void addResourceReferenceDescriptor(ResourceReferenceDescriptor resReference) {
        getResourceReferenceContainer().addResourceReferenceDescriptor(resReference);
    }


    /**
     * Looks up an resource reference with the given name.
     * Return null if it is not found.
     *
     * @param the name of the resource-reference
     */
    @Override
    public ResourceReferenceDescriptor getResourceReference(String name) {
        try {
            return getResourceReferenceContainer().getResourceReferenceByName(name);
            // annotation has a corresponding resource-ref
            // in xml.  Just add annotation info and continue.
            // This logic might change depending on overriding rules
            // and order in which annotations are read w.r.t. to xml.
            // E.g. sparse overriding in xml or loading annotations
            // first.
        } catch(IllegalArgumentException e) {
            // DOL API is (unfortunately) defined to return
            // IllegalStateException if name doesn't exist.

            Application app = getAppFromDescriptor();
            if (app != null) {
                try {
                    // Check for java:app/java:global dependencies at app-level
                    ResourceReferenceDescriptor resourceRef = app.getResourceReferenceByName(name);
                    // Make sure it's added to the container context.
                    addResourceReferenceDescriptor(resourceRef);
                } catch (IllegalArgumentException ee) {
                }
            }
        }
        return null;
    }


    protected ResourceReferenceContainer getResourceReferenceContainer() {
        return (ResourceReferenceContainer) descriptor;
    }


    @Override
    public void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestReference) {
        getMessageDestinationReferenceContainer().addMessageDestinationReferenceDescriptor(msgDestReference);
    }


    @Override
    public MessageDestinationReferenceDescriptor getMessageDestinationReference(String name) {
        try {
            return getMessageDestinationReferenceContainer().getMessageDestinationReferenceByName(name);
            // annotation has a corresponding message-destination-ref
            // in xml. Just add annotation info and continue.
            // This logic might change depending on overriding rules
            // and order in which annotations are read w.r.t. to xml.
            // E.g. sparse overriding in xml or loading annotations
            // first.
        } catch (IllegalArgumentException e) {
            // DOL API is (unfortunately) defined to return
            // IllegalStateException if name doesn't exist.
            return null;
        }
    }


    protected MessageDestinationReferenceContainer getMessageDestinationReferenceContainer() {
        return (MessageDestinationReferenceContainer) descriptor;
    }


    @Override
    public void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        getResourceEnvReferenceContainer().addResourceEnvReferenceDescriptor(resourceEnvReference);
    }


    @Override
    public ResourceEnvReferenceDescriptor getResourceEnvReference(String name) {
        try {
            return getResourceEnvReferenceContainer().getResourceEnvReferenceByName(name);
            // annotation has a corresponding resource-env-ref
            // in xml. Just add annotation info and continue.
            // This logic might change depending on overriding rules
            // and order in which annotations are read w.r.t. to xml.
            // E.g. sparse overriding in xml or loading annotations
            // first.
        } catch (IllegalArgumentException e) {
            // DOL API is (unfortunately) defined to return
            // IllegalStateException if name doesn't exist.
            Application app = getAppFromDescriptor();
            if (app != null) {
                try {
                    // Check for java:app/java:global dependencies at app-level
                    ResourceEnvReferenceDescriptor resourceEnvRef = app.getResourceEnvReferenceByName(name);
                    // Make sure it's added to the container context.
                    addResourceEnvReferenceDescriptor(resourceEnvRef);
                } catch (IllegalArgumentException ee) {
                }
            }
        }
        return null;
    }


    protected WritableJndiNameEnvironment getResourceEnvReferenceContainer() {
        return getDescriptorAsWritableJndiNameEnvironment();
    }


    @Override
    public void addEnvEntryDescriptor(EnvironmentProperty envEntry) {
        getEnvEntryContainer().addEnvironmentProperty(envEntry);
    }


    @Override
    public EnvironmentProperty getEnvEntry(String name) {
        try {
            return getEnvEntryContainer().getEnvironmentPropertyByName(name);
            // annotation has a corresponding env-entry
            // in xml.  Just add annotation info and continue.
            // This logic might change depending on overriding rules
            // and order in which annotations are read w.r.t. to xml.
            // E.g. sparse overriding in xml or loading annotations
            // first.
        } catch (IllegalArgumentException e) {
            // DOL API is (unfortunately) defined to return
            // IllegalStateException if name doesn't exist.

            Application app = getAppFromDescriptor();

            if (app != null) {
                try {
                    // Check for java:app/java:global dependencies at app-level
                    EnvironmentProperty envEntry = app.getEnvironmentPropertyByName(name);
                    // Make sure it's added to the container context.
                    addEnvEntryDescriptor(envEntry);
                } catch (IllegalArgumentException ee) {
                }
            }

        }
        return null;

    }


    protected WritableJndiNameEnvironment getEnvEntryContainer() {
        return getDescriptorAsWritableJndiNameEnvironment();
    }


    @Override
    public void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor emfRefDesc) {
        getEmfRefContainer().addEntityManagerFactoryReferenceDescriptor(emfRefDesc);

    }


    @Override
    public EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReference(String name) {
        try {
            return getEmfRefContainer().getEntityManagerFactoryReferenceByName(name);
            // annotation has a corresponding entry
            // in xml.  Just add annotation info and continue.
            // This logic might change depending on overriding rules
            // and order in which annotations are read w.r.t. to xml.
            // E.g. sparse overriding in xml or loading annotations
            // first.
        } catch (IllegalArgumentException e) {
            // DOL API is (unfortunately) defined to return
            // IllegalStateException if name doesn't exist.

            Application app = getAppFromDescriptor();
            if (app != null) {
                try {
                    // Check for java:app/java:global dependencies at app-level
                    EntityManagerFactoryReferenceDescriptor emfRefDesc = app.getEntityManagerFactoryReferenceByName(name);
                    // Make sure it's added to the container context.
                    addEntityManagerFactoryReferenceDescriptor(emfRefDesc);
                } catch (IllegalArgumentException ee) {
                }
            }
        }
        return null;
    }


    protected WritableJndiNameEnvironment getEmfRefContainer() {
        return getDescriptorAsWritableJndiNameEnvironment();
    }


    @Override
    public void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor emRefDesc) {
        getEmRefContainer().addEntityManagerReferenceDescriptor(emRefDesc);
    }


    @Override
    public EntityManagerReferenceDescriptor getEntityManagerReference(String name) {
        try {
            return getEmRefContainer().getEntityManagerReferenceByName(name);
            // annotation has a corresponding entry
            // in xml. Just add annotation info and continue.
            // This logic might change depending on overriding rules
            // and order in which annotations are read w.r.t. to xml.
            // E.g. sparse overriding in xml or loading annotations
            // first.
        } catch (IllegalArgumentException e) {
            // DOL API is (unfortunately) defined to return
            // IllegalStateException if name doesn't exist.

            Application app = getAppFromDescriptor();

            if (app != null) {
                try {
                    // Check for java:app/java:global dependencies at app-level
                    EntityManagerReferenceDescriptor emRefDesc = app.getEntityManagerReferenceByName(name);
                    // Make sure it's added to the container context.
                    addEntityManagerReferenceDescriptor(emRefDesc);
                } catch (IllegalArgumentException ee) {
                }
            }
        }

        return null;

    }


    protected WritableJndiNameEnvironment getEmRefContainer() {
        return getDescriptorAsWritableJndiNameEnvironment();
    }


    /**
     * @param postConstructDesc
     */
    @Override
    public void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc) {
        getPostConstructContainer().addPostConstructDescriptor(postConstructDesc);
    }


    /**
     * Look up an post-construct LifecycleCallbackDescriptor with the
     * given name. Return null if it is not found
     *
     * @param className
     */
    @Override
    public LifecycleCallbackDescriptor getPostConstruct(String className) {
        LifecycleCallbackDescriptor postConstructDesc = getPostConstructContainer()
            .getPostConstructDescriptorByClass(className);
        return postConstructDesc;
    }


    protected WritableJndiNameEnvironment getPostConstructContainer() {
        return getDescriptorAsWritableJndiNameEnvironment();
    }


    /**
     * @param preDestroyDesc
     */
    @Override
    public void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
        getPreDestroyContainer().addPreDestroyDescriptor(preDestroyDesc);
    }


    /**
     * Look up an pre-destroy LifecycleCallbackDescriptor with the
     * given name. Return null if it is not found
     *
     * @param className
     */
    @Override
    public LifecycleCallbackDescriptor getPreDestroy(String className) {
        LifecycleCallbackDescriptor preDestroyDesc = getPreDestroyContainer().getPreDestroyDescriptorByClass(className);
        return preDestroyDesc;
    }


    protected WritableJndiNameEnvironment getDataSourceDefinitionContainer(){
        return getDescriptorAsWritableJndiNameEnvironment();
    }

    /**
     * Adds the descriptor to the receiver.
     * @param desc Descriptor to add.
     */
    @Override
    public void addResourceDescriptor(ResourceDescriptor desc) {
        getDataSourceDefinitionContainer().addResourceDescriptor(desc);
    }

    /**
     * get all Descriptor descriptors based on the type
     * @return Descriptor descriptors
     */
    @Override
    public Set<ResourceDescriptor> getResourceDescriptors(JavaEEResourceType type) {
        return getDataSourceDefinitionContainer().getResourceDescriptors(type);
    }


    protected WritableJndiNameEnvironment getMailSessionContainer() {
        return getDescriptorAsWritableJndiNameEnvironment();
    }

    protected WritableJndiNameEnvironment getConnectionFactoryDefinitionContainer(){
        return getDescriptorAsWritableJndiNameEnvironment();
    }

    protected WritableJndiNameEnvironment getAdministeredObjectDefinitionContainer(){
        return getDescriptorAsWritableJndiNameEnvironment();
    }

    protected WritableJndiNameEnvironment getJMSConnectionFactoryDefinitionContainer(){
        return getDescriptorAsWritableJndiNameEnvironment();
    }

    protected WritableJndiNameEnvironment getJMSDestinationDefinitionContainer(){
        return getDescriptorAsWritableJndiNameEnvironment();
    }

    protected WritableJndiNameEnvironment getPreDestroyContainer() {
        return getDescriptorAsWritableJndiNameEnvironment();
    }

    @Override
    public String getComponentClassName() {
        return componentClassName;
    }


    @Override
    public HandlerChainContainer[] getHandlerChainContainers(boolean serviceSideHandlerChain, Class<?> declaringClass) {
        // by default return null; appropriate contextx should override this
        return null;
    }

    @Override
    public ServiceReferenceContainer[] getServiceRefContainers() {
        // by default we return our descriptor;
        ServiceReferenceContainer[] containers = new ServiceReferenceContainer[1];
        containers[0] = (ServiceReferenceContainer) descriptor;
        return containers;
    }

    @Override
    public void addManagedBean(ManagedBeanDescriptor managedBeanDesc) {
        BundleDescriptor bundleDesc = (BundleDescriptor) ((BundleDescriptor) descriptor).getModuleDescriptor()
            .getDescriptor();
        bundleDesc.addManagedBean(managedBeanDesc);
    }


    public Application getAppFromDescriptor() {
        if (descriptor instanceof BundleDescriptor) {
            BundleDescriptor bundle = (BundleDescriptor) descriptor;
            return bundle.getApplication();
        } else if (descriptor instanceof EjbDescriptor) {
            return ((EjbDescriptor) descriptor).getApplication();
        } else {
            return null;
        }
    }


    private WritableJndiNameEnvironment getDescriptorAsWritableJndiNameEnvironment() {
        return (WritableJndiNameEnvironment) this.descriptor;
    }
}
