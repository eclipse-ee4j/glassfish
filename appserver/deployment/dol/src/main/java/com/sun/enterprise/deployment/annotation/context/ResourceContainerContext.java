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

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;

import java.util.Set;

import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * This interface provides an abstraction for handle resource references.
 *
 * @Author Shing Wai Chan
 */
public interface ResourceContainerContext extends ServiceReferenceContainerContext {

    /**
     * Add a ejb reference.
     *
     * @param the ejb reference
     */
    void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference);

    /**
     * Looks up an ejb reference with the given name.
     * Return null if it is not found.
     *
     * @param the name of the ejb-reference
     */
    EjbReferenceDescriptor getEjbReference(String name);

    /**
     * Add a resource reference
     *
     * @param the resource reference
     */
    void addResourceReferenceDescriptor(ResourceReferenceDescriptor resReference);

    /**
     * Looks up an resource reference with the given name.
     * Return null if it is not found.
     *
     * @param the name of the resource-reference
     */
    ResourceReferenceDescriptor getResourceReference(String name);

    /**
     * Add a message-destination-ref
     *
     * @param the msgDestRef
     */
    void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef);

    /**
     * Looks up a message-destination-ref with the given name.
     * Return null if it is not found.
     *
     * @param the name of the message-destination-ref
     */
    MessageDestinationReferenceDescriptor getMessageDestinationReference(String name);

    /**
     * Add a resource-env-ref
     *
     * @param the resourceEnvRef
     */
    void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvRef);

    /**
     * Looks up a resource-env-ref with the given name.
     * Return null if it is not found.
     *
     * @param the name of the resource-env-ref
     */
    ResourceEnvReferenceDescriptor getResourceEnvReference(String name);

    /**
     * Add an env-entry
     *
     * @param the env-entry
     */
    void addEnvEntryDescriptor(EnvironmentProperty envEntry);

    /**
     * Looks up an env-entry with the given name.
     * Return null if it is not found.
     *
     * @param the name of the env-entry
     */
    EnvironmentProperty getEnvEntry(String name);

    void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor emfRefDesc);

    /**
     * Looks up an entity manager factory reference with the given name.
     * Return null if it is not found.
     *
     * @param the name of the emf reference
     */
    EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReference(String name);

    void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor emRefDesc);

    /**
     * Looks up an entity manager reference with the given name.
     * Return null if it is not found.
     *
     * @param the name of the emf reference
     */
    EntityManagerReferenceDescriptor getEntityManagerReference(String name);

    /**
     * @param postConstructDesc
     */
    void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc);

    /**
     * Look up an post-construct LifecycleCallbackDescriptor with the
     * given name. Return null if it is not found
     *
     * @param className
     */
    LifecycleCallbackDescriptor getPostConstruct(String className);

    /**
     * @param preDestroyDesc
     */
    void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc);

    /**
     * Look up an pre-destroy LifecycleCallbackDescriptor with the
     * given name. Return null if it is not found
     *
     * @param className
     */
    LifecycleCallbackDescriptor getPreDestroy(String className);

    /**
     * Adds the specified descriptor to the receiver.
     *
     * @param desc ResourceDescriptor to add.
     */
    void addResourceDescriptor(ResourceDescriptor desc);

    /**
     * get all descriptors based on the type
     *
     * @return writeable {@link Set} of {@link ResourceDescriptor}
     */
    Set<ResourceDescriptor> getResourceDescriptors(JavaEEResourceType type);

    void addManagedBean(ManagedBeanDescriptor managedBeanDesc);
}
