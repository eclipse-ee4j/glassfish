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

package org.glassfish.orb.admin.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

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
 * Orb Configuration properties
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface Orb extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the useThreadPoolIds property.
     * Specifies a comma-separated list of thread-pool ids.
     *
     * This would refer to the thread-pool-id(s) defined in the thread-pool
     * sub-element of thread-pool-config element in domain.xml. These would be
     * the threadpool(s) used by the ORB. More than one thread-pool-id(s) could
     * be specified by using commas to separate the names
     * e.g. orb-thread-pool-1, orb-thread-pool-2
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    String getUseThreadPoolIds();

    /**
     * Sets the value of the useThreadPoolIds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setUseThreadPoolIds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the messageFragmentSize property.
     *
     * GIOPv1.2 messages larger than this will get fragmented.
     * Minimum value is 128.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="1024")
    @Min(value=128)
    String getMessageFragmentSize();

    /**
     * Sets the value of the messageFragmentSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMessageFragmentSize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxConnections property.
     *
     * Maximum number of incoming connections, on all listeners
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="1024",dataType=Integer.class)
    @Min(value=0)
    String getMaxConnections();

    /**
     * Sets the value of the maxConnections property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMaxConnections(String value) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @Override
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
