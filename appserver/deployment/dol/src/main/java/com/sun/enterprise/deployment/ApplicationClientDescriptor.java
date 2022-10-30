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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.node.appclient.AppClientNode;
import com.sun.enterprise.deployment.runtime.JavaWebStartAccessDescriptor;
import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;
import com.sun.enterprise.deployment.util.AppClientTracerVisitor;
import com.sun.enterprise.deployment.util.AppClientValidator;
import com.sun.enterprise.deployment.util.AppClientVisitor;
import com.sun.enterprise.deployment.util.ComponentPostVisitor;
import com.sun.enterprise.deployment.util.ComponentVisitor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.DescriptorVisitor;
import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * I represent all the deployment information about
 * an application client [{0}].
 *
 * @author Danny Coward
 */
public class ApplicationClientDescriptor extends CommonResourceBundleDescriptor
    implements WritableJndiNameEnvironment, ResourceReferenceContainer, EjbReferenceContainer,
    ResourceEnvReferenceContainer, ServiceReferenceContainer, MessageDestinationReferenceContainer {

    private static final long serialVersionUID = 1L;
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ApplicationClientDescriptor.class);

    private Set<EnvironmentProperty> environmentProperties;
    private Set<EjbReferenceDescriptor> ejbReferences;
    private Set<ResourceEnvReferenceDescriptor> resourceEnvReferences;
    private Set<MessageDestinationReferenceDescriptor> messageDestReferences;
    private Set<ResourceReferenceDescriptor> resourceReferences;
    private Set<ServiceReferenceDescriptor> serviceReferences;
    private final Set<EntityManagerFactoryReferenceDescriptor> entityManagerFactoryReferences = new HashSet<>();
    private final Set<EntityManagerReferenceDescriptor> entityManagerReferences = new HashSet<>();
    private final Set<LifecycleCallbackDescriptor> postConstructDescs = new HashSet<>();
    private final Set<LifecycleCallbackDescriptor> preDestroyDescs = new HashSet<>();
    private String mainClassName;
    private String callbackHandler;
    private JavaWebStartAccessDescriptor jwsAccessDescriptor;

    /**
     * @return true if there is runtime information in this
     * object that must be saved.
     */
    public boolean hasRuntimeInformation() {
        for (NamedDescriptor next : this.getNamedDescriptors()) {
            if (!next.getJndiName().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the default version of the deployment descriptor
     * loaded by this descriptor
     */
    @Override
    public String getDefaultSpecVersion() {
        return AppClientNode.SPEC_VERSION;
    }

    @Override
    public boolean isEmpty() {
        return mainClassName == null;
    }


    /**
     * @return the fq Java clasname of this application client [{0}].
     */
    public String getMainClassName() {
        if (this.mainClassName == null) {
            this.mainClassName = "";
        }
        return this.mainClassName;
    }

    /**
     * Sets the main classname of this app client.
     */
    public void setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName;

    }

    /**
     * Get the classname of the callback handler.
     */
    public String getCallbackHandler() {
        return callbackHandler;
    }

    /**
     * Set the classname of the callback handler.
     */
    public void setCallbackHandler(String handler) {
        callbackHandler = handler;
    }

    /**
     * @return the set of named descriptors I reference.
     */
    public Collection<NamedDescriptor> getNamedDescriptors() {
        return super.getNamedDescriptorsFrom(this);
    }

    /**
     * @return the set of named reference pairs I reference.
     */
    public Vector<NamedReferencePair> getNamedReferencePairs() {
        return super.getNamedReferencePairsFrom(this);
    }

    /**
     * Returns the set of environment properties of this app client.
     */
    @Override
    public Set<EnvironmentProperty> getEnvironmentProperties() {
        if (this.environmentProperties == null) {
            this.environmentProperties = new OrderedSet<>();
        }
        this.environmentProperties = new OrderedSet<>(this.environmentProperties);
        return this.environmentProperties;
    }

    /**
     * Returns the environment property object searching on the supplied key.
     * throws an illegal argument exception if no such environment property exists.
     */
    @Override
    public EnvironmentProperty getEnvironmentPropertyByName(String name) {
        for (EnvironmentProperty ev : this.getEnvironmentProperties()) {
            if (ev.getName().equals(name)) {
                return ev;
            }
        }
        throw new IllegalArgumentException(
            localStrings.getLocalString("enterprise.deployment.exceptionappclienthasnoenvpropertybyname",
                "This application client [{0}] has no environment property by the name of [{1}]",
                new Object[] {getName(), name}));
    }


    /**
     * Adds an environment property to this application client [{0}].
     */
    @Override
    public void addEnvironmentProperty(EnvironmentProperty environmentProperty) {
        this.getEnvironmentProperties().add(environmentProperty);
    }

    /**
     * Remove the given environment property
     */
    @Override
    public void removeEnvironmentProperty(EnvironmentProperty environmentProperty) {
        this.getEnvironmentProperties().remove(environmentProperty);
    }

    /**
     * Return the set of references to ejbs that I have.
     */
    @Override
    public Set<EjbReferenceDescriptor> getEjbReferenceDescriptors() {
        if (this.ejbReferences == null) {
            this.ejbReferences = new OrderedSet<>();
        }
        this.ejbReferences = new OrderedSet<>(this.ejbReferences);
        return this.ejbReferences;
    }


    @Override
    public void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        this.getEjbReferenceDescriptors().add(ejbReference);
        ejbReference.setReferringBundleDescriptor(this);

    }


    @Override
    public void removeEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        this.getEjbReferenceDescriptors().remove(ejbReference);
        ejbReference.setReferringBundleDescriptor(null);

    }

    @Override
    public Set<LifecycleCallbackDescriptor> getPostConstructDescriptors() {
        return postConstructDescs;
    }

    @Override
    public void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc) {
        String className = postConstructDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : getPostConstructDescriptors()) {
            if ( (next.getLifecycleCallbackClass() != null) &&
                next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPostConstructDescriptors().add(postConstructDesc);
        }
    }

    @Override
    public LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className) {
        return getPostConstructDescriptorByClass(className, this);
    }

    @Override
    public Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors() {
        return preDestroyDescs;
    }


    @Override
    public void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
        String className = preDestroyDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : getPreDestroyDescriptors()) {
            if ((next.getLifecycleCallbackClass() != null) && next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPreDestroyDescriptors().add(preDestroyDesc);
        }
    }


    @Override
    public LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className) {
        return getPreDestroyDescriptorByClass(className, this);
    }

    @Override
    public InjectionInfo getInjectionInfoByClass(Class clazz) {
        return getInjectionInfoByClass(clazz, this);
    }

    @Override
    public Set<ServiceReferenceDescriptor> getServiceReferenceDescriptors() {
        if (this.serviceReferences == null) {
            this.serviceReferences = new OrderedSet<>();
        }
        this.serviceReferences = new OrderedSet<>(this.serviceReferences);
        return this.serviceReferences;
    }

    @Override
    public void addServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        serviceRef.setBundleDescriptor(this);
        this.getServiceReferenceDescriptors().add(serviceRef);

    }

    @Override
    public void removeServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        this.getServiceReferenceDescriptors().remove(serviceRef);

    }

    /**
     * Looks up an service reference with the given name.
     * Throws an IllegalArgumentException if it is not found.
     */
    @Override
    public ServiceReferenceDescriptor getServiceReferenceByName(String name) {
        for (ServiceReferenceDescriptor srd : this.getServiceReferenceDescriptors()) {
            if (srd.getName().equals(name)) {
                return srd;
            }
        }
        throw new IllegalArgumentException(
            localStrings.getLocalString("enterprise.deployment.exceptionappclienthasnoservicerefbyname",
                "This application client [{0}] has no service reference by the name of [{1}]",
                new Object[] {getName(), name}));
    }


    @Override
    public Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors() {
        if (this.messageDestReferences == null) {
            this.messageDestReferences = new OrderedSet<>();
        }
        this.messageDestReferences = new OrderedSet<>(this.messageDestReferences);
        return this.messageDestReferences;
    }

    @Override
    public void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor messageDestRef) {
        messageDestRef.setReferringBundleDescriptor(this);
        this.getMessageDestinationReferenceDescriptors().add(messageDestRef);

    }

    @Override
    public void removeMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef) {
        this.getMessageDestinationReferenceDescriptors().remove(msgDestRef);

    }

    /**
     * Looks up an message destination reference with the given name.
     * Throws an IllegalArgumentException if it is not found.
     */
    @Override
    public MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name) {
        for (MessageDestinationReferenceDescriptor mdr : this.getMessageDestinationReferenceDescriptors()) {
            if (mdr.getName().equals(name)) {
                return mdr;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString("exceptionappclienthasnomsgdestrefbyname",
            "This application client [{0}] has no message destination reference by the name of [{1}]",
            new Object[] {getName(), name}));
    }


    /**
     * Return the set of resource environment references this ejb declares.
     */
    @Override
    public Set<ResourceEnvReferenceDescriptor> getResourceEnvReferenceDescriptors() {
        if (this.resourceEnvReferences == null) {
            this.resourceEnvReferences = new OrderedSet<>();
        }
        return this.resourceEnvReferences = new OrderedSet<>(this.resourceEnvReferences);
    }

    @Override
    public void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        this.getResourceEnvReferenceDescriptors().add(resourceEnvReference);

    }

    @Override
    public void removeResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        this.getResourceEnvReferenceDescriptors().remove(resourceEnvReference);

    }

    /**
     * Looks up an ejb reference with the given name. Throws an IllegalArgumentException
     * if it is not found.
     */
    public EjbReferenceDescriptor getEjbReferenceByName(String name) {
        for (EjbReferenceDescriptor ejr : this.getEjbReferenceDescriptors()) {
            if (ejr.getName().equals(name)) {
                return ejr;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString(
            "exceptionappclienthasnoejbrefbyname",
            "This application client [{0}] has no ejb reference by the name of [{1}]",
            new Object[] {getName(), name}));
    }

    @Override
    public Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors() {
        return entityManagerFactoryReferences;
    }

    /**
     * Return the entity manager factory reference descriptor corresponding to
     * the given name.
     */
    @Override
    public EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name) {
        for (EntityManagerFactoryReferenceDescriptor next : getEntityManagerFactoryReferenceDescriptors()) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString(
            "exceptionappclienthasnoentitymgrfactoryrefbyname",
            "This application client [{0}] has no entity manager factory reference by the name of [{1}]",
            new Object[] {getName(), name}));
    }

    @Override
    public void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor reference) {
        reference.setReferringBundleDescriptor(this);
        this.getEntityManagerFactoryReferenceDescriptors().add(reference);

    }

    @Override
    public Set<EntityManagerReferenceDescriptor> getEntityManagerReferenceDescriptors() {
        return entityManagerReferences;
    }

    /**
     * Return the entity manager factory reference descriptor corresponding to
     * the given name.
     */
    @Override
    public EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name) {
        throw new IllegalArgumentException(localStrings.getLocalString(
            "exceptionappclienthasnoentitymgrrefbyname",
            "This application client [{0}] has no entity manager reference by the name of [{1}]",
            new Object[] {getName(), name}));
    }

    @Override
    public void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor reference) {
        reference.setReferringBundleDescriptor(this);
        this.getEntityManagerReferenceDescriptors().add(reference);
    }

    @Override
    public List<InjectionCapable> getInjectableResourcesByClass(String className) {
        return getInjectableResourcesByClass(className, this);
    }

    /**
     * Looks up an ejb reference with the given name. Throws an IllegalArgumentException
     * if it is not found.
     */
    @Override
    public EjbReferenceDescriptor getEjbReference(String name) {
        for (EjbReferenceDescriptor ejr : this.getEjbReferenceDescriptors()) {
            if (ejr.getName().equals(name)) {
                return ejr;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString(
            "exceptionappclienthasnoejbrefbyname",
            "This application client [{0}] has no ejb reference by the name of [{1}]",
            new Object[] {getName(), name}));
    }

    /**
     * Return a resource environment reference by the same name or throw an IllegalArgumentException.
     */
    @Override
    public ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name) {
        for (ResourceEnvReferenceDescriptor jdr : this.getResourceEnvReferenceDescriptors()) {
            if (jdr.getName().equals(name)) {
                return jdr;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString(
            "enterprise.deployment.exceptionappclienthasnoesourceenvrefbyname",
            "This application client [{0}] has no resource environment reference by the name of [{1}]",
            new Object[] {getName(), name}));
    }

    /**
     * Return the set of references to resources that I have.
     */
    @Override
    public Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors() {
        if (this.resourceReferences == null) {
            this.resourceReferences = new OrderedSet<>();
        }
        this.resourceReferences = new OrderedSet<>(this.resourceReferences);
        return this.resourceReferences;
    }

    /**
     * Looks up a reference to a resource by its name (getName()).
     * @throws an IllegalArgumentException if no such descriptor is found.
     */
    @Override
    public ResourceReferenceDescriptor getResourceReferenceByName(String name) {
        for (ResourceReferenceDescriptor rr : this.getResourceReferenceDescriptors()) {
            if (rr.getName().equals(name)) {
                return rr;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString(
            "exceptionappclienthasnoresourcerefbyname",
            "This application client [{0}] has no resource reference by the name of [{1}]",
            new Object[] {getName(), name}));
    }

    /**
     * Adds a reference to a resource.
     */

    @Override
    public void addResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        this.getResourceReferenceDescriptors().add(resourceReference);

    }

    /**
     * Removes the given resource reference from this app client.
     */
    @Override
    public void removeResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        this.getResourceReferenceDescriptors().remove(resourceReference);

    }

    /**
     * @return a set of class names that need to have full annotation processing
     */
    public Set<String> getComponentClassNames() {
        Set<String> set = new HashSet<>();
        set.add(getMainClassName());
        return set;
    }


    /**
     * @return true if this bundle descriptor defines web service clients
     */
    @Override
    public boolean hasWebServiceClients() {
        return !getServiceReferenceDescriptors().isEmpty();
    }

    /**
     * @return true if this bundle descriptor defines web services
     */
    @Override
    public boolean hasWebServices() {
        return false;
    }

    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Application Client Descriptor");
        toStringBuffer.append("\n ");
        super.print(toStringBuffer);
        toStringBuffer.append("\n environmentProperties ").append(environmentProperties);
        toStringBuffer.append("\n ejbReferences ");
        if (ejbReferences != null) {
            printDescriptorSet(ejbReferences, toStringBuffer);
        }
        toStringBuffer.append("\n resourceEnvReferences ");
        if (resourceEnvReferences != null) {
            printDescriptorSet(resourceEnvReferences, toStringBuffer);
        }
        toStringBuffer.append("\n messageDestReferences ");
        if (messageDestReferences != null) {
            printDescriptorSet(messageDestReferences, toStringBuffer);
        }
        toStringBuffer.append("\n resourceReferences ");
        if (resourceReferences != null) {
            printDescriptorSet(resourceReferences, toStringBuffer);
        }
        toStringBuffer.append("\n serviceReferences ");
        if (serviceReferences != null) {
            printDescriptorSet(serviceReferences, toStringBuffer);
        }
        toStringBuffer.append("\n mainClassName ").append(mainClassName);
    }
    private void printDescriptorSet(Set descSet, StringBuffer sbuf){
        for (Object obj : descSet) {
            if(obj instanceof Descriptor) {
                ((Descriptor)obj).print(sbuf);
            } else {
                sbuf.append(obj);
            }
        }
    }

    /**
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     *
     * @param aVisitor a visitor to traverse the descriptors
     */
    @Override
    public void visit(DescriptorVisitor aVisitor) {
        if (aVisitor instanceof AppClientVisitor || aVisitor instanceof ComponentPostVisitor) {
            visit((ComponentVisitor) aVisitor);
        } else {
            super.visit(aVisitor);
        }
    }

    /**
     * @return the module type for this bundle descriptor
     */
    @Override
    public ArchiveType getModuleType() {
        return DOLUtils.carType();
    }

    /**
     * @return the visitor for this bundle descriptor
     */
    @Override
    public ComponentVisitor getBundleVisitor() {
        return new AppClientValidator();
    }

    /**
     * @return the tracer visitor for this descriptor
     */
    @Override
    public DescriptorVisitor getTracerVisitor() {
        return new AppClientTracerVisitor();
    }

    public JavaWebStartAccessDescriptor getJavaWebStartAccessDescriptor() {
        if (jwsAccessDescriptor == null) {
            jwsAccessDescriptor = new JavaWebStartAccessDescriptor();
            jwsAccessDescriptor.setBundleDescriptor(this);
        }
        return jwsAccessDescriptor;
    }

    public void setJavaWebStartAccessDescriptor(JavaWebStartAccessDescriptor descr) {
        descr.setBundleDescriptor(this);
        jwsAccessDescriptor = descr;

    }

    /**
     * This method is used to find out the precise list of PUs that are
     * referenced by the appclient. An appclient can not use container
     * managed EM as there is no support for JTA in our ACC, so this method
     * only returns the list of PUs referenced via @PersistenceUnit or
     * <persistence-unit-ref>.
     *
     * @return list of PU that are actually referenced by the appclient.
     */
    @Override
    public Collection<? extends PersistenceUnitDescriptor> findReferencedPUs() {
        return findReferencedPUsViaPURefs(this);
    }

    @Override
    public Set<ResourceDescriptor> getResourceDescriptors(JavaEEResourceType type) {
        switch(type) {
            case CFD:
                throw new UnsupportedOperationException(localStrings.getLocalString(
                    "enterprise.deployment.exceptionappclientnotsupportconnectionfactorydefinition",
                    "The application client [{0}] do not support connection factory definitions",
                    new Object[] {getName()}));
            case AODD:
                throw new UnsupportedOperationException(localStrings.getLocalString(
                    "enterprise.deployment.exceptionappclientnotsupportadministeredobjectdefinition",
                    "The application client [{0}] do not support administered object definitions",
                    new Object[] {getName()}));
            default:
                return super.getResourceDescriptors(type);
        }
    }

    @Override
    public void addResourceDescriptor(ResourceDescriptor descriptor) {
        if (descriptor.getResourceType().equals(JavaEEResourceType.CFD)) {
            throw new UnsupportedOperationException(localStrings.getLocalString(
                "enterprise.deployment.exceptionappclientnotsupportconnectionfactorydefinition",
                "The application client [{0}] do not support connection factory definitions",
                new Object[] {getName()}));
        } else if (descriptor.getResourceType().equals(JavaEEResourceType.AODD)) {
            throw new UnsupportedOperationException(localStrings.getLocalString(
                "enterprise.deployment.exceptionappclientnotsupportadministeredobjectdefinition",
                "The application client [{0}] do not support administered object definitions",
                new Object[] {getName()}));

        } else {
            super.addResourceDescriptor(descriptor);
        }

    }

    @Override
    public void removeResourceDescriptor(ResourceDescriptor descriptor) {
        if (descriptor.getResourceType().equals(JavaEEResourceType.CFD)) {
            throw new UnsupportedOperationException(localStrings.getLocalString(
                "enterprise.deployment.exceptionappclientnotsupportconnectionfactorydefinition",
                "The application client [{0}] do not support connection factory definitions",
                new Object[] {getName()}));

        } else if (descriptor.getResourceType().equals(JavaEEResourceType.AODD)) {
            throw new UnsupportedOperationException(localStrings.getLocalString(
                "enterprise.deployment.exceptionappclientnotsupportadministeredobjectdefinition",
                "The application client [{0}] do not support administered object definitions",
                new Object[] {getName()}));
        } else {
            super.removeResourceDescriptor(descriptor);
        }
    }
}
