/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.deployment.ConnectorDescriptor;

import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapter;

import org.jvnet.hk2.annotations.Contract;


/**
 * Interface class for different types (1.0 and 1.5 complient) resource
 * adapter abstraction classes.
 * Contains methods for setup(initialization), destroy and creation of MCF.
 *
 * @author Srikanth P and Binod PG
 */

@Contract
public interface ActiveResourceAdapter {

    /**
     * initializes the active (runtime) RAR
     * @param ra resource-adapter bean
     * @param cd connector-descriptor
     * @param moduleName rar-name
     * @param loader classloader for the RAR
     * @throws ConnectorRuntimeException when unable to initialize the runtime RA
     */
    void init(ResourceAdapter ra, ConnectorDescriptor cd, String moduleName, ClassLoader loader)
            throws ConnectorRuntimeException;

    /**
     * initializes the resource adapter bean and the resources, pools
     *
     * @throws ConnectorRuntimeException This exception is thrown if the
     *                                   setup/initialization fails.
     */
    void setup() throws ConnectorRuntimeException;

    /**
     * uninitializes the resource adapter.
     */
    void destroy();

    /**
     * Returns the Connector descriptor which represents/holds ra.xml
     *
     * @return ConnectorDescriptor Representation of ra.xml.
     */
    ConnectorDescriptor getDescriptor();

    /**
     * Indicates whether a particular implementation of ActiveRA can handle the RAR in question.
     * @param desc ConnectorDescriptor
     * @param moduleName resource adapter name
     * @return boolean indiating whether a ActiveRA can handle the RAR
     */
    boolean handles(ConnectorDescriptor desc, String moduleName);

    /**
     * Creates managed Connection factories corresponding to one pool.
     * This should be implemented in the ActiveJmsResourceAdapter, for
     * jms resources, has been implemented to perform xa resource recovery
     * in mq clusters, not supported for any other code path.
     *
     * @param ccp Connector connection pool which contains the pool properties
     *            and ra.xml values pertaining to managed connection factory
     *            class. These values are used in MCF creation.
     * @param loader Classloader used to managed connection factory class.
     * @return ManagedConnectionFactory created managed connection factories
     */
    ManagedConnectionFactory[] createManagedConnectionFactories
            (ConnectorConnectionPool ccp, ClassLoader loader);

    /**
     * Creates managed Connection factory instance.
     *
     * @param ccp    Connector connection pool which contains the pool properties
     *               and ra.xml values pertaining to managed connection factory
     *               class. These values are used in MCF creation.
     * @param loader Classloader used to managed connection factory class.
     * @return ManagedConnectionFactory created managed connection factory
     *         instance
     */
    ManagedConnectionFactory createManagedConnectionFactory
            (ConnectorConnectionPool ccp, ClassLoader loader);

    /**
     * Returns the class loader that is used to load the RAR.
     *
     * @return <code>ClassLoader</code> object.
     */
    ClassLoader getClassLoader();

    /**
     * Returns the module Name of the RAR
     *
     * @return A <code>String</code> representing the name of the
     *         connector module
     */
    String getModuleName();

    /**
     * returns the resource-adapter bean
     * @return resource-adapter bean
     */
    ResourceAdapter getResourceAdapter();
}
