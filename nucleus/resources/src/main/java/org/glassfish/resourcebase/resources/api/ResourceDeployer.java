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

package org.glassfish.resourcebase.resources.api;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;

import java.util.Collection;

import org.jvnet.hk2.annotations.Contract;

/**
 * Interface to be implemented by different resource types (eg. jms-resource)
 * to deploy/undeploy a resource to the server's runtime naming context.
 * <p/>
 * The methods can potentially be called concurrently, therefore implementation
 * need to do synchronization if necessary.
 *
 * @param <D> Deployable resource descriptor type.
 */
@Contract
public interface ResourceDeployer<D> {

    /**
     * Indicates whether a particular resource deployer can handle the resource in question
     *
     * @param resource resource that need to be handled
     * @return boolean
     */
    boolean handles(Object resource);


    /**
     * A deployer can indicate whether a particular resource can be deployed before
     * application deployment
     * <p>
     * Used in case of application-scoped-resources, eg. Embedded RAR resources are created after
     * application (that has embedded .rar) deployment.
     *
     * @param postApplicationDeployment post-application-deployment
     * @param allResources resources collection in which the resource being validated is present.
     * @param resource resource to be validated
     * @return boolean
     */
    default boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        return handles(resource) && !postApplicationDeployment;
    }

    /**
     * Indicates whether the resource deployer can handle
     * transparent-dynamic-reconfiguration of resource
     *
     * @return boolean indicating whether transparent-dynamic-reconfiguration is supported.
     */
    default boolean supportsDynamicReconfiguration() {
        return false;
    }

    /**
     * List of classes which need to be proxied for dynamic-reconfiguration
     *
     * @return list of classes
     */
    default Class<?>[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }


    void validatePreservedResource(Application oldApp, Application newApp, Resource resource, Resources allResources)
        throws ResourceConflictException;


    /**
     * Deploy the resource into the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @throws Exception thrown if fail
     */
    void deployResource(D resource) throws Exception;

    /**
     * Deploy the resource into the server's runtime naming context
     * This API is used in cases where the "config" bean is not
     * yet persisted in domain.xml and is part of the "config" transaction.
     *
     * @param resource a resource object (eg. JmsResource)
     * @param applicationName application-name
     * @param moduleName module-name
     * @throws Exception thrown if fail
     */
    void deployResource(D resource, String applicationName, String moduleName) throws Exception;

    /**
     * Undeploy the resource from the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @throws Exception thrown if fail
     */
    void undeployResource(D resource) throws Exception;

    /**
     * Undeploy the resource from the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @param applicationName application-name
     * @param moduleName module-name
     * @throws Exception thrown if fail
     */
    void undeployResource(D resource, String applicationName, String moduleName) throws Exception;

    /**
     * Redeploy the resource into the server's runtime naming context
     *
     * @param resource a resource object
     * @throws Exception thrown if fail
     */
    default void redeployResource(D resource) throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }

    /**
     * Enable the resource in the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @throws Exception thrown if fail
     */
    default void enableResource(D resource) throws Exception {
        deployResource(resource);
    }

    /**
     * Disable the resource in the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @throws Exception thrown if fail
     */
    default void disableResource(D resource) throws Exception {
        undeployResource(resource);
    }
}
