/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.ejb.config;

import jakarta.validation.constraints.Min;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.config.support.datatypes.NonNegativeInteger;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

@Configured
public interface MdbContainer extends ConfigBeanProxy, PropertyBag, ConfigExtension {

    /**
     * Gets the value of the steadyPoolSize property. Minimum and initial number of message driven beans in pool. An integer
     * in the range [0, max-pool-size].
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "1")
    @Min(value = 0)
    String getSteadyPoolSize();

    /**
     * Sets the value of the steadyPoolSize property.
     *
     * @param value allowed object is {@link String }
     */
    void setSteadyPoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the poolResizeQuantity property.
     *
     * Quantum of increase/decrease, when the size of pool grows/shrinks. An integer in the range [0, max-pool-size].
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "8")
    @Min(value = 0)
    String getPoolResizeQuantity();

    /**
     * Sets the value of the poolResizeQuantity property.
     *
     * @param value allowed object is {@link String }
     */
    void setPoolResizeQuantity(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxPoolSize property. maximum size, pool can grow to. A non-negative integer.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "32")
    @Min(value = 0)
    String getMaxPoolSize();

    /**
     * Sets the value of the maxPoolSize property.
     *
     * @param value allowed object is {@link String }
     */
    void setMaxPoolSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the idleTimeoutInSeconds property.
     *
     * Idle bean instance in pool becomes a candidate for deletion, when this timeout expires
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "600")
    @Min(value = 0)
    String getIdleTimeoutInSeconds();

    /**
     * Sets the value of the idleTimeoutInSeconds property.
     *
     * @param value allowed object is {@link String }
     */
    void setIdleTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Properties.
     */
    @Override
    @PropertiesDesc(props = {
        @PropertyDesc(
            name = "cmt-max-runtime-exceptions",
            defaultValue = "1",
            dataType = NonNegativeInteger.class,
            description =
                "Deprecated. Specifies the maximum number of RuntimeException occurrences allowed from a message-driven bean's " +
                "method when container-managed transactions are used") })
    @Element
    List<Property> getProperty();
}
