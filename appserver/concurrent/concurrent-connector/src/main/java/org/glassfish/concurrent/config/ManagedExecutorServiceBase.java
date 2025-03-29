/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.config;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;

import jakarta.validation.Payload;
import jakarta.validation.constraints.Min;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Concurrency managed executor service resource base definition
 */
@Configured
public interface ManagedExecutorServiceBase
    extends ConfigBeanProxy, Resource, BindableResource, Payload, ConcurrencyResource {

    /**
     * Gets the value of the threadPriority property.
     *
     * @return possible object is
     *         {@link String}
     */
    @Attribute(defaultValue = "" + Thread.NORM_PRIORITY, dataType = Integer.class)
    @Min(value = 0)
    String getThreadPriority();

    /**
     * Sets the value of the threadPriority property.
     *
     * @param value allowed object is
     *            {@link String}
     */
    void setThreadPriority(String value) throws PropertyVetoException;

    /**
     * Gets the value of the useVirtualThreads property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getVirtual();

    /**
     * Sets the value of the useVirtualThreads property.
     *
     * @param value allowed object is {@link String }
     */
    void setVirtual(String value) throws PropertyVetoException;

    /**
     * Gets the value of the longRunningTasks property.
     *
     * @return possible object is
     *         {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getLongRunningTasks();

    /**
     * Sets the value of the longRunningTasks property.
     *
     * @param value allowed object is
     *            {@link String}
     */
    void setLongRunningTasks(String value) throws PropertyVetoException;

    /**
     * Gets the value of the hungAfterSeconds property.
     *
     * @return possible object is
     *         {@link String}
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value = 0)
    String getHungAfterSeconds();

    /**
     * Sets the value of the hungAfterSeconds property.
     *
     * @param value allowed object is
     *            {@link String}
     */
    void setHungAfterSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the hungLoggerPrintOnce property.
     *
     * @return possible object is
     *         {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getHungLoggerPrintOnce();

    /**
     * Sets the value of the hungLoggerPrintOnce property.
     *
     * @param value allowed object is
     *            {@link String}
     */
    void setHungLoggerPrintOnce(String value) throws PropertyVetoException;

    /**
     * Gets the value of the hungLoggerInitialDelaySeconds property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "60", dataType = Long.class)
    @Min(value = 0)
    String getHungLoggerInitialDelaySeconds();

    /**
     * Sets the value of the hungLoggerInitialDelaySeconds property.
     *
     * @param value allowed object is {@link String}
     */
    void setHungLoggerInitialDelaySeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the hungLoggerIntervalSeconds property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "60", dataType = Long.class)
    @Min(value = 1)
    String getHungLoggerIntervalSeconds();

    /**
     * Sets the value of the hungLoggerIntervalSeconds property.
     *
     * @param value allowed object is {@link String}
     */
    void setHungLoggerIntervalSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the corePoolSize property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value = 0)
    String getCorePoolSize();

    /**
     * Sets the value of the coreSize property.
     *
     * @param value allowed object is {@link String}
     */
    void setCorePoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the keepAlivesSeconds property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "60", dataType = Integer.class)
    @Min(value = 0)
    String getKeepAliveSeconds();

    /**
     * Sets the value of the keepAliveSeconds property.
     *
     * @param value allowed object is {@link String}
     */
    void setKeepAliveSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the threadLifetimeSeconds property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "0", dataType = Integer.class)
    @Min(value = 0)
    String getThreadLifetimeSeconds();

    /**
     * Sets the value of the threadLifetimeSeconds property.
     *
     * @param value allowed object is {@link String}
     */
    void setThreadLifetimeSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the context property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "", dataType = String.class)
    String getContext();

    /**
     * Sets the value of the context property.
     *
     * @param value allowed object is {@link String}
     */
    void setContext(String value) throws PropertyVetoException;
}
