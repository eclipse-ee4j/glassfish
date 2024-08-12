/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans;

import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;

import org.glassfish.api.Param;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * This config bean will define parameters for Managed jobs A Managed job is a commands which is annotated with
 * either @ManagedJob,@Progress or running with --detach
 *
 * @author Bhakti Mehta
 */
@Configured
public interface ManagedJobConfig extends DomainExtension, PropertyBag, Payload {

    /**
     * Gets the value of inMemoryRetentionPeriod property
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "1h")
    @Pattern(regexp = "[1-9]\\d*([hms]|[HMS])", message = "{invalid.time.period.specified}", payload = ManagedJobConfig.class)
    String getInMemoryRetentionPeriod();

    /**
     * Sets the value of the inMemoryRetentionPeriod property.
     *
     * @param value allowed object is {@link String }
     */
    @Param(name = "inmemoryretentionperiod", optional = true)
    void setInMemoryRetentionPeriod(String value) throws PropertyVetoException;

    /**
     * Gets the value of jobRetentionPeriod
     *
     * @return
     */
    @Attribute(defaultValue = "24h")
    @Pattern(regexp = "[1-9]\\d*([hms]|[HMS])", message = "{invalid.time.period.specified}", payload = ManagedJobConfig.class)
    String getJobRetentionPeriod();

    /**
     * Sets the value of the jobRetentionPeriod property.
     *
     * @param value allowed object is {@link String }
     */
    @Param(name = "jobretentionperiod", optional = true)
    void setJobRetentionPeriod(String value) throws PropertyVetoException;

    /**
     * Gets the value of persistingEnabled property
     *
     * @return
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    boolean getPersistingEnabled();

    /**
     * Sets the value of the persistingenabled property.
     *
     * @param value allowed object is {@link String }
     */
    void setPersistingEnabled(boolean value) throws PropertyVetoException;

    /**
     * Gets the value of pollInterval property
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "20m")
    @Pattern(regexp = "[1-9]\\d*([hms]|[HMS])", message = "{invalid.time.period.specified}", payload = ManagedJobConfig.class)
    String getPollInterval();

    /**
     * Sets the value of the pollInterval property.
     *
     * @param value allowed object is {@link String }
     */
    @Param(name = "pollinterval", optional = true)
    void setPollInterval(String value) throws PropertyVetoException;

    /**
     * Gets the value of initialDelay
     *
     * @return
     */
    @Attribute(defaultValue = "20m")
    @Pattern(regexp = "[1-9]\\d*([hms]|[HMS])", message = "{invalid.time.period.specified}", payload = ManagedJobConfig.class)
    String getInitialDelay();

    /**
     * Sets the value of the initialDelay property.
     *
     * @param value allowed object is {@link String }
     */
    @Param(name = "initialdelay", optional = true)
    void setInitialDelay(String value) throws PropertyVetoException;

}
