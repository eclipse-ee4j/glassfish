/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config.test.example;

import java.beans.PropertyVetoException;
import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;


/**
 * Configuration of EJB Container
 */

/* @XmlType(name = "", propOrder = {
    "ejbTimerService",
    "property"
}) */

@Configured
public interface SimpleConnector extends ConfigBeanProxy {
    int DEFAULT_THREAD_CORE_POOL_SIZE = 16;
    int DEFAULT_THREAD_MAX_POOL_SIZE = 32;
    long DEFAULT_THREAD_KEEP_ALIVE_SECONDS = 60;
    int DEFAULT_THREAD_QUEUE_CAPACITY = Integer.MAX_VALUE;
    boolean DEFAULT_ALLOW_CORE_THREAD_TIMEOUT = false;
    boolean DEFAULT_PRESTART_ALL_CORE_THREADS = false;

    /**
     * Gets the value of the steadyPoolSize property.
     *
     * (slsb,eb) number of bean instances normally maintained in pool.
     * When a pool is first created, it will be populated with size equal to
     * steady-pool-size. When an instance is removed from the pool, it is
     * replenished asynchronously, so that the pool size is at or above the
     * steady-pool-size. This addition will be in multiples of
     * pool-resize-quantity. When a bean is disassociated from a method
     * invocation, it is put back in the pool, subject to max-pool-size limit.
     * If the max pool size is exceeded the bean id destroyed immediately.
     * A pool cleaning thread, executes at an interval defined by
     * pool-idle-timeout-in-seconds. This thread reduces the pool size to
     * steady-pool-size, in steps defined by pool-resize-quantity. If the pool
     * is empty, the required object will be created and returned immediately.
     * This prevents threads from blocking till the pool is replenished by the
     * background thread. steady-pool-size must be greater than 1 and at most
     * equal to the max-pool-size.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="8080")
    String getPort();

    /**
     * Sets the value of the steadyPoolSize property
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPort(String value) throws PropertyVetoException;

    @Element
    EjbContainerAvailability getEjbContainerAvailability();

    void setEjbContainerAvailability(EjbContainerAvailability v);

    @Element
    WebContainerAvailability getWebContainerAvailability();

    void setWebContainerAvailability(WebContainerAvailability v);

    @Element("*")
    List<GenericContainer> getExtensions();

}



