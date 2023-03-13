/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.EntityManagerFactoryReference;
import com.sun.enterprise.deployment.types.EntityManagerReference;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;
import com.sun.enterprise.deployment.util.ComponentVisitor;
import com.sun.enterprise.deployment.util.DOLUtils;

import java.lang.System.Logger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.security.common.Role;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * I represent all the configurable deployment information contained in an EJB JAR.
 *
 * @author Danny Coward
 * @author David Matejcek
 */
public abstract class EjbBundleDescriptor extends CommonResourceBundleDescriptor
    implements WritableJndiNameEnvironment, EjbReferenceContainer, ResourceEnvReferenceContainer,
    ResourceReferenceContainer, ServiceReferenceContainer, MessageDestinationReferenceContainer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = DOLUtils.getLogger();

    private Boolean disableNonportableJndiNames;

    private final Map<String, EjbApplicationExceptionInfo> applicationExceptions = new HashMap<>();
    private final Set<EjbDescriptor> ejbs = new HashSet<>();
    /** EJB module level dependencies */
    private final Set<EjbReferenceDescriptor> ejbReferences = new HashSet<>();
    private final Set<EntityManagerFactoryReferenceDescriptor> entityManagerFactoryReferences = new HashSet<>();
    private final Set<EntityManagerReferenceDescriptor> entityManagerReferences = new HashSet<>();
    private final Set<EnvironmentProperty> environmentProperties = new HashSet<>();
    /** All interceptor classes defined within this ejb module, keyed by interceptor class name. */
    private final Map<String, EjbInterceptor> interceptors = new HashMap<>();
    private final Set<MessageDestinationReferenceDescriptor> messageDestReferences = new HashSet<>();
    private final Set<ResourceEnvReferenceDescriptor> resourceEnvReferences = new HashSet<>();
    private final Set<ResourceReferenceDescriptor> resourceReferences = new HashSet<>();
    private final Set<ServiceReferenceDescriptor> serviceReferences = new HashSet<>();

    /**
     * Creates a dummy {@link EjbDescriptor} instance of the given name.
     *
     * @param ejbName
     * @return {@link EjbDescriptor}, never null.
     */
    protected abstract EjbDescriptor createDummyEjbDescriptor(String ejbName);


    @Override
    public ArchiveType getModuleType() {
        return DOLUtils.ejbType();
    }


    /**
     * @return value of the disable-nonportable-jndi-names element. May be null.
     */
    public Boolean getDisableNonportableJndiNames() {
        return disableNonportableJndiNames;
    }


    /**
     * @param disable value of the disable-nonportable-jndi-names element.May be null.
     */
    public void setDisableNonportableJndiNames(String disable) {
        disableNonportableJndiNames = Boolean.valueOf(disable);
    }


    /**
     * @return always new {@link HashMap} with mappings of the class name
     *         and the {@link EjbApplicationExceptionInfo}.
     */
    public Map<String, EjbApplicationExceptionInfo> getApplicationExceptions() {
        return new HashMap<>(applicationExceptions);
    }


    /**
     * Adds the mapping of the class name and the exception info.
     *
     * @param appExc
     */
    // Reflection in EjbBundleNode
    public void addApplicationException(EjbApplicationExceptionInfo appExc) {
        applicationExceptions.put(appExc.getExceptionClassName(), appExc);
    }


    @Override
    public boolean isEmpty() {
        return ejbs.isEmpty();
    }


    /**
     * @return unmodifiable set of ejb descriptors.
     */
    public Set<? extends EjbDescriptor> getEjbs() {
        return Collections.unmodifiableSet(ejbs);
    }


    /**
     * @param name
     * @return true if I have an ejb descriptor by that name.
     */
    public boolean hasEjbByName(String name) {
        for (EjbDescriptor ejb : ejbs) {
            if (ejb.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param name name of the {@link EjbDescriptor}
     * @return an ejb descriptor that I have by the same name, otherwise throws
     *          an IllegalArgumentException
     */
    public EjbDescriptor getEjbByName(String name) {
        return getEjbByName(name, false);
    }

    /**
     * Returns an ejb descriptor of the given name.
     *
     * @param name name of the {@link EjbDescriptor}
     * @param isCreateDummy
     * @return {@link EjbDescriptor} found by the name OR a dummy {@link EjbDescriptor}
     *         if requested.
     * @throws IllegalArgumentException if isCreateDummy is false and we don't have such EJB
     */
    public EjbDescriptor getEjbByName(String name, boolean isCreateDummy) {
        for (EjbDescriptor next : ejbs) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        if (!isCreateDummy) {
            throw new IllegalArgumentException("Referencing error: this bundle has no bean of name: " + name);
        }

        // there could be cases where the annotation defines the ejb component
        // and the ejb-jar.xml just uses it
        // we have to create a dummy version of the ejb descriptor in this
        // case as we process xml before annotations.
        EjbDescriptor dummyEjbDesc = createDummyEjbDescriptor(name);
        addEjb(dummyEjbDesc);
        return dummyEjbDesc;
    }


    /**
     * @param className {@link EjbDescriptor#getEjbClassName()}
     * @return all ejb descriptors that has a given class name.
     *         It returns an empty list if no ejb is found.
     */
    public EjbDescriptor[] getEjbByClassName(String className) {
        List<EjbDescriptor> ejbList = new ArrayList<>();
        for (EjbDescriptor ejb : ejbs) {
            if (className.equals(ejb.getEjbClassName())) {
                ejbList.add(ejb);
            }
        }
        return ejbList.toArray(EjbDescriptor[]::new);
    }


    /**
     * @param className
     * @return all ejb descriptors that have a given class name as the web service endpoint
     *         interface. It returns an empty list if no ejb is found.
     */
    public EjbDescriptor[] getEjbBySEIName(String className) {
        ArrayList<EjbDescriptor> ejbList = new ArrayList<>();
        for (EjbDescriptor ejb : ejbs) {
            if (className.equals(ejb.getWebServiceEndpointInterfaceName())) {
                ejbList.add(ejb);
            }
        }
        return ejbList.toArray(EjbDescriptor[]::new);
    }


    /**
     * @return set of service-ref from ejbs contained in this bundle this bundle or empty set
     *         if none
     */
    public Set<ServiceReferenceDescriptor> getEjbServiceReferenceDescriptors() {
        Set<ServiceReferenceDescriptor> serviceRefs = new OrderedSet<>();
        for (EjbDescriptor ejb : ejbs) {
            serviceRefs.addAll(ejb.getServiceReferenceDescriptors());
        }
        return serviceRefs;
    }


    /**
     * @return true if this bundle descriptor defines web service clients
     */
    @Override
    public boolean hasWebServiceClients() {
        for (EjbDescriptor next : ejbs) {
            Collection<ServiceReferenceDescriptor> serviceRefs = next.getServiceReferenceDescriptors();
            if (!serviceRefs.isEmpty()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Add the given ejb descriptor to my (uses equals).
     *
     * @param ejbDescriptor
     */
    public void addEjb(EjbDescriptor ejbDescriptor) {
        ejbDescriptor.setEjbBundleDescriptor(this);
        ejbs.add(ejbDescriptor);
    }


    /**
     * Remove the given ejb descriptor from my (uses equals).
     *
     * @param ejbDescriptor
     */
    public void removeEjb(EjbDescriptor ejbDescriptor) {
        ejbDescriptor.setEjbBundleDescriptor(null);
        ejbs.remove(ejbDescriptor);
    }


    /**
     * Returns the generated XML directory feturn the set of ejb references this ejb declares.
     */
    @Override
    public Set<EjbReferenceDescriptor> getEjbReferenceDescriptors() {
        return ejbReferences;
    }


    @Override
    public EjbReferenceDescriptor getEjbReference(String name) {
        for (EjbReferenceDescriptor reference : ejbReferences) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        throw new IllegalArgumentException(
            MessageFormat.format("This ejb jar [{0}] has no ejb reference by the name of [{1}] ", getName(), name));
    }


    @Override
    public void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        ejbReferences.add(ejbReference);
        ejbReference.setReferringBundleDescriptor(this);
    }


    @Override
    public void removeEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        ejbReferences.remove(ejbReference);
    }


    @Override
    public Collection<? extends PersistenceUnitDescriptor> findReferencedPUs() {
        Collection<PersistenceUnitDescriptor> persistenceUnits = new HashSet<>();
        // Iterate through all the ejbs
        for (EjbDescriptor ejb : ejbs) {
            persistenceUnits.addAll(findReferencedPUsViaPURefs(ejb));
            persistenceUnits.addAll(findReferencedPUsViaPCRefs(ejb));
        }

        // Add bundle level artifacts added by e.g. CDDI
        for (EntityManagerFactoryReference emfRef : getEntityManagerFactoryReferenceDescriptors()) {
            persistenceUnits.add(findReferencedPUViaEMFRef(emfRef));
        }

        for (EntityManagerReference emRef : getEntityManagerReferenceDescriptors()) {
            persistenceUnits.add(findReferencedPUViaEMRef(emRef));
        }
        return persistenceUnits;
    }


    @Override
    public Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors() {
        return entityManagerFactoryReferences;
    }


    @Override
    public EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name) {
        for (EntityManagerFactoryReferenceDescriptor reference : getEntityManagerFactoryReferenceDescriptors()) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        throw new IllegalArgumentException("No entity manager factory reference of name " + name);
    }


    @Override
    public void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor reference) {
        reference.setReferringBundleDescriptor(this);
        entityManagerFactoryReferences.add(reference);
    }


    @Override
    public Set<EntityManagerReferenceDescriptor> getEntityManagerReferenceDescriptors() {
        return entityManagerReferences;
    }


    @Override
    public EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name) {
        for (EntityManagerReferenceDescriptor reference : entityManagerReferences) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        throw new IllegalArgumentException("No entity manager reference of name " + name);
    }


    @Override
    public void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor reference) {
        reference.setReferringBundleDescriptor(this);
        entityManagerReferences.add(reference);
    }


    @Override
    public Set<EnvironmentProperty> getEnvironmentProperties() {
        return environmentProperties;
    }


    @Override
    public EnvironmentProperty getEnvironmentPropertyByName(String name) {
        for (EnvironmentProperty ev : environmentProperties) {
            if (ev.getName().equals(name)) {
                return ev;
            }
        }
        throw new IllegalArgumentException("no env-entry of name " + name);
    }


    @Override
    public void addEnvironmentProperty(EnvironmentProperty environmentProperty) {
        environmentProperties.add(environmentProperty);
    }


    @Override
    public void removeEnvironmentProperty(EnvironmentProperty environmentProperty) {
        environmentProperties.remove(environmentProperty);

    }


    /**
     * @return new {@link Set} with all our {@link EjbInterceptor}s
     */
    public Set<EjbInterceptor> getInterceptors() {
        return new HashSet<>(interceptors.values());
    }


    /**
     * @param className
     * @return {@link EjbInterceptor} or null
     */
    public EjbInterceptor getInterceptorByClassName(String className) {
        return interceptors.get(className);
    }


    /**
     * Adds the interceptor. If there already is another interceptor with the same
     * {@link EjbInterceptor#getInterceptorClassName()}, the call is ignored.
     *
     * @param interceptor
     */
    public void addInterceptor(EjbInterceptor interceptor) {
        EjbInterceptor ic = getInterceptorByClassName(interceptor.getInterceptorClassName());
        if (ic == null) {
            interceptor.setEjbBundleDescriptor(this);
            interceptors.put(interceptor.getInterceptorClassName(), interceptor);
        }
    }


    @Override
    public Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors() {
        return messageDestReferences;
    }


    @Override
    public MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name) {
        for (MessageDestinationReferenceDescriptor mdr : messageDestReferences) {
            if (mdr.getName().equals(name)) {
                return mdr;
            }
        }
        throw new IllegalArgumentException("No message destination ref of name " + name);
    }


    @Override
    public void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor messageDestRef) {
        messageDestRef.setReferringBundleDescriptor(this);
        messageDestReferences.add(messageDestRef);
    }


    @Override
    public void removeMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef) {
        messageDestReferences.remove(msgDestRef);
    }


    @Override
    public Set<ResourceEnvReferenceDescriptor> getResourceEnvReferenceDescriptors() {
        return resourceEnvReferences;
    }


    @Override
    public ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name) {
        for (ResourceEnvReferenceDescriptor element : resourceEnvReferences) {
            if (element.getName().equals(name)) {
                return element;
            }
        }
        throw new IllegalArgumentException("No resource env ref of name " + name);
    }


    @Override
    public void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        resourceEnvReferences.add(resourceEnvReference);
    }


    @Override
    public void removeResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        resourceEnvReferences.remove(resourceEnvReference);
    }


    @Override
    public Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors() {
        return resourceReferences;
    }


    @Override
    public ResourceReferenceDescriptor getResourceReferenceByName(String name) {
        for (ResourceReferenceDescriptor reference : resourceReferences) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        throw new IllegalArgumentException("No resource ref of name " + name);
    }


    @Override
    public void addResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        resourceReferences.add(resourceReference);
    }


    @Override
    public void removeResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        resourceReferences.remove(resourceReference);
    }


    @Override
    public Set<ServiceReferenceDescriptor> getServiceReferenceDescriptors() {
        return serviceReferences;
    }


    @Override
    public ServiceReferenceDescriptor getServiceReferenceByName(String name) {
        for (ServiceReferenceDescriptor reference : getServiceReferenceDescriptors()) {
            if (reference.getName().equals(name)) {
                return reference;
            }
        }
        throw new IllegalArgumentException("No service ref of name " + name);
    }


    @Override
    public void addServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        serviceRef.setBundleDescriptor(this);
        serviceReferences.add(serviceRef);
    }


    @Override
    public void removeServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        serviceReferences.remove(serviceRef);
    }


    @Override
    public void removeRole(Role role) {
        LOG.log(DEBUG, "removeRole(role={0})", role);
        if (getRoles().contains(role)) {
            for (EjbDescriptor ejb : ejbs) {
                ejb.removeRole(role);
            }
            super.removeRole(role);
        }
    }


    /**
     * @return null if not overriden
     */
    @Override
    public ComponentVisitor getBundleVisitor() {
        return null;
    }


    @Override
    public List<InjectionCapable> getInjectableResourcesByClass(String className) {
        return getInjectableResourcesByClass(className, this);
    }


    @Override
    public InjectionInfo getInjectionInfoByClass(Class clazz) {
        return getInjectionInfoByClass(clazz, this);
    }


    /**
     * Always empty immutable {@link Set}.
     */
    @Override
    public Set<LifecycleCallbackDescriptor> getPostConstructDescriptors() {
        return Collections.emptySet();
    }


    /**
     * Ignored.
     */
    @Override
    public void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc) {
        // no-op
    }


    /**
     * Always null.
     */
    @Override
    public LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className) {
        return null;
    }


    /**
     * Always empty immutable {@link Set}.
     */
    @Override
    public Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors() {
        return Collections.emptySet();
    }


    /**
     * Ignored.
     */
    @Override
    public void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
        // no-op
    }


    /**
     * Always null.
     */
    @Override
    public LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className) {
        return null;
    }
}
