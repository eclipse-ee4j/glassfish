/*
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

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;


/**
 * Configuration for ejb timer service
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface EjbTimerService extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the minimumDeliveryIntervalInMillis property.
     *
     * It is the minimum number of milliseconds allowed before the next timer
     * expiration for a particular timer can occur. It guards  against extremely
     * small timer increments that can overload the server.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="1000")
    @Min(value=1)
    String getMinimumDeliveryIntervalInMillis();

    /**
     * Sets the value of the minimumDeliveryIntervalInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMinimumDeliveryIntervalInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxRedeliveries property.
     *
     * It is the maximum number of times the ejb timer service will attempt to
     * redeliver a timer expiration due to exception or rollback.
     * The minimum value is 1, per the ejb specification.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="1")
    @Min(value=1)
    String getMaxRedeliveries();

    /**
     * Sets the value of the maxRedeliveries property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMaxRedeliveries(String value) throws PropertyVetoException;

    /**
     * Gets the value of the timerDatasource property.
     *
     * overrides cmp-resource (jdbc/__TimerPool) specified in sun-ejb-jar.xml of
     * (__ejb_container_timer_app) of the timer service system application.
     * By default this is set to jdbc/__TimerPool, but can be overridden for the
     * cluster or server instance, if they choose to.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getTimerDatasource();

    /**
     * Sets the value of the timerDatasource property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setTimerDatasource(String value) throws PropertyVetoException;

    /**
     * Gets the value of the redeliveryIntervalInternalInMillis property.
     *
     * It is the number of milliseconds the ejb timer service will wait after a
     * failed ejbTimeout delivery before attempting a redelivery.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="5000")
    @Min(value=1)
    String getRedeliveryIntervalInternalInMillis();

    /**
     * Sets the value of the redeliveryIntervalInternalInMillis property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setRedeliveryIntervalInternalInMillis(String value) throws PropertyVetoException;

    /**
        Properties as per {@link PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
