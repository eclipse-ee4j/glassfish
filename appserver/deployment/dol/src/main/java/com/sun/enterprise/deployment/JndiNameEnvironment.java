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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.JavaEEResourceType;
import java.util.List;
import java.util.Set;

    /**
    * Objects implementing this interface allow their
    * environment properties, ejb references and resource
    * references to be read.
    *
    *@author Danny Coward
    */

public interface JndiNameEnvironment {

    /**
     * Return a set of environment properties.
     *
     * @return java.util.Set of EnvironmentProperty objects
     */
    public Set getEnvironmentProperties();


    /**
     * Return the env-entry with the given name
     *
     * @return EnvironmentProperty descriptor
     */
    public EnvironmentProperty getEnvironmentPropertyByName(String name);

    /**
     * Return a set of ejb reference descriptors.
     *
     * @return java.util.Set of EjbReferenceDescriptor objects
     */
    public Set getEjbReferenceDescriptors();

/**
     * Return a set of service reference descriptors.
     *
     * @return java.util.Set of ServiceReferenceDescriptor objects
     */

    public Set getServiceReferenceDescriptors();


    /**
     * Return the Service reference descriptor corresponding to
     * the given name.
     *
     * @return ServiceReferenceDescriptor object
     */
    public ServiceReferenceDescriptor getServiceReferenceByName(String name);

    /**
     * Return a set of resource reference descriptors.
     *
     * @return java.util.Set of ResourceReferenceDescriptor objects
     */

    public Set getResourceReferenceDescriptors();


    /**
     * Return a set of resource environment reference descriptors.
     *
     * @return java.util.Set of ResourceEnvReferenceDescriptor objects
     */

    public Set getResourceEnvReferenceDescriptors();


    /**
     * Return the resource environment reference descriptor corresponding to
     * the given name.
     *
     * @return ResourceEnvReferenceDescriptor object
     */
    public ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name);

    /**
     * Return a set of message destination reference descriptors.
     *
     * @return java.util.Set of MessageDestinationReferenceDescriptor objects
     */

    public Set getMessageDestinationReferenceDescriptors();


    /**
     * Return the message destination reference descriptor corresponding to
     * the given name.
     *
     * @return MessageDestinationReferenceDescriptor object
     */
    public MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name);

    /**
     * Return a set of post-construct descriptors.
     *
     * @return java.util.Set of LifecycleCallbackDescriptor post-construct objects
     */
    public Set<LifecycleCallbackDescriptor> getPostConstructDescriptors();

    /**
     * Return the post-construct descriptor corresponding to
     * the given name.
     *
     * @return LifecycleCallbackDescriptor post-construct object
     */
    public LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className);

    /**
     * Return a set of pre-destroy descriptors.
     *
     * @return java.util.Set of LifecycleCallbackDescriptor pre-destroy objects
     */
    public Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors();

    /**
     * Return a set of descriptors based on the type.
     *
     * @return java.util.Set of descriptor objects
     */
    public Set<ResourceDescriptor> getResourceDescriptors(JavaEEResourceType type);

    /**
     * Return a set of descriptors based on the class value.
     *
     * @return java.util.Set of descriptor objects
     */
    public Set<ResourceDescriptor> getAllResourcesDescriptors(Class givenClass);

    /**
     * Return a set of descriptors.
     *
     * @return java.util.Set of descriptor objects
     */
    public Set<ResourceDescriptor> getAllResourcesDescriptors();

    /**
     * Return the pre-destroy descriptor corresponding to
     * the given name.
     *
     * @return LifecycleCallbackDescriptor pre-destroy object
     */
    public LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className);

    /**
     * Return a set of entity manager factory reference descriptors.
     */
    public Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors();

    /**
     * Return the entity manager factory reference descriptor corresponding to
     * the given name.
     */
    public EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name);


    /**
     * Return a set of entity manager reference descriptors.
     */
    public Set<EntityManagerReferenceDescriptor> getEntityManagerReferenceDescriptors();

    /**
     * Return the entity manager reference descriptor corresponding to
     * the given name.
     */
    public EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name);

    /**
     *
     */
    public List<InjectionCapable> getInjectableResourcesByClass(String className);
    public InjectionInfo getInjectionInfoByClass(Class clazz);
}
