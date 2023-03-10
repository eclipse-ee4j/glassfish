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

import java.util.List;
import java.util.Set;

import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * Objects implementing this interface allow their
 * environment properties, ejb references and resource
 * references to be read.
 *
 * @author Danny Coward
 */
public interface JndiNameEnvironment {

    /**
     * Return a set of environment properties.
     *
     * @return java.util.Set of {@link EnvironmentProperty} objects
     */
    Set<EnvironmentProperty> getEnvironmentProperties();

    /**
     * @param name
     * @return the environment property object found by the supplied key.
     * @throws IllegalArgumentException if no such environment property exists.
     */
    EnvironmentProperty getEnvironmentPropertyByName(String name) throws IllegalArgumentException;

    /**
     * Return a set of ejb reference descriptors.
     *
     * @return java.util.Set of EjbReferenceDescriptor objects
     */
    Set<EjbReferenceDescriptor> getEjbReferenceDescriptors();

    /**
     * Return a set of service reference descriptors.
     *
     * @return java.util.Set of ServiceReferenceDescriptor objects
     */
    Set<ServiceReferenceDescriptor> getServiceReferenceDescriptors();

    /**
     * Return the Service reference descriptor corresponding to the given name.
     *
     * @param name
     * @return ServiceReferenceDescriptor object
     */
    ServiceReferenceDescriptor getServiceReferenceByName(String name);

    /**
     * Return a set of resource reference descriptors.
     *
     * @return java.util.Set of ResourceReferenceDescriptor objects
     */
    Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors();

    /**
     * Return a set of resource environment reference descriptors.
     *
     * @return java.util.Set of ResourceEnvReferenceDescriptor objects
     */
    Set<ResourceEnvReferenceDescriptor> getResourceEnvReferenceDescriptors();

    /**
     * Return the resource environment reference descriptor corresponding to the given name.
     *
     * @param name
     * @return ResourceEnvReferenceDescriptor object
     */
    ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name);

    /**
     * Return a set of message destination reference descriptors.
     *
     * @return java.util.Set of MessageDestinationReferenceDescriptor objects
     */
    Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors();

    /**
     * Return the message destination reference descriptor corresponding to the given name.
     *
     * @param name
     * @return MessageDestinationReferenceDescriptor object
     */
    MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name);

    /**
     * Return a set of post-construct descriptors.
     *
     * @return java.util.Set of LifecycleCallbackDescriptor post-construct objects
     */
    Set<LifecycleCallbackDescriptor> getPostConstructDescriptors();

    /**
     * Return the post-construct descriptor corresponding to
     * the given name.
     *
     * @param className
     * @return LifecycleCallbackDescriptor post-construct object
     */
    LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className);

    /**
     * Return a set of pre-destroy descriptors.
     *
     * @return java.util.Set of LifecycleCallbackDescriptor pre-destroy objects
     */
    Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors();

    /**
     * Return a set of descriptors based on the type.
     *
     * @param type
     * @return writeable {@link Set} of descriptor objects
     */
    Set<ResourceDescriptor> getResourceDescriptors(JavaEEResourceType type);

    /**
     * Return a set of descriptors based on the class value.
     *
     * @param givenClass
     * @return java.util.Set of descriptor objects
     */
    Set<ResourceDescriptor> getAllResourcesDescriptors(Class<?> givenClass);

    /**
     * Return a set of descriptors.
     *
     * @return java.util.Set of descriptor objects
     */
    Set<ResourceDescriptor> getAllResourcesDescriptors();

    /**
     * Return the pre-destroy descriptor corresponding to the given name.
     *
     * @param className
     * @return LifecycleCallbackDescriptor pre-destroy object
     */
    LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className);

    /**
     * @return a set of entity manager factory reference descriptors.
     */
    Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors();

    /**
     * @param name
     * @return the entity manager factory reference descriptor corresponding to the given name.
     */
    EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name);

    /**
     * @return a set of entity manager reference descriptors.
     */
    Set<EntityManagerReferenceDescriptor> getEntityManagerReferenceDescriptors();

    /**
     * @param name
     * @return the entity manager reference descriptor corresponding to the given name.
     * @throws IllegalArgumentException if no such reference exists.
     */
    EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name);

    /**
     * @param className
     * @return list of resources injectable by the class name.
     */
    List<InjectionCapable> getInjectableResourcesByClass(String className);

    /**
     * @param clazz
     * @return {@link InjectionInfo} to be used in management of object's lifecycle.
     */
    InjectionInfo getInjectionInfoByClass(Class<?> clazz);
}
