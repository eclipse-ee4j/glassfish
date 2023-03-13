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
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;

import java.lang.System.Logger;
import java.util.ArrayList;
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
    private final Set<EnvironmentProperty> environmentProperties = new HashSet<>();

    /** All interceptor classes defined within this ejb module, keyed by interceptor class name. */
    private final Map<String, EjbInterceptor> interceptors = new HashMap<>();

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


    /**
     * @return new {@link Set} with all our {@link EjbInterceptor}s
     */
    public Set<EjbInterceptor> getInterceptors() {
        return new HashSet<>(interceptors.values());
    }


    /**
     * @return set of service-ref from ejbs contained in this bundle this bundle or empty set
     *         if none
     */
    public Set<ServiceReferenceDescriptor> getEjbServiceReferenceDescriptors() {
        Set<ServiceReferenceDescriptor> serviceRefs = new OrderedSet<>();
        for (EjbDescriptor next : ejbs) {
            serviceRefs.addAll(next.getServiceReferenceDescriptors());
        }
        return serviceRefs;
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
