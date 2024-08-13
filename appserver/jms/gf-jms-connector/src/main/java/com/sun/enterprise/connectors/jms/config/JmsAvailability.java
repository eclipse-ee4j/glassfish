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

package com.sun.enterprise.connectors.jms.config;

import com.sun.enterprise.config.serverbeans.AvailabilityServiceExtension;

import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface JmsAvailability extends ConfigExtension, PropertyBag, AvailabilityServiceExtension {

    String PATTERN_BROKER = "(masterbroker|shareddb)";
    String PATTERN_MESSAGE_STORE_TYPE = "(file|jdbc)";

    /**
     * Gets the value of the availabilityEnabled property.
     *
     * This boolean flag controls whether the MQ cluster associated with the
     * application server cluster is HA enabled or not. If this attribute is
     * "false", then the MQ cluster pointed to by the jms-service element is
     * considered non-HA (Conventional MQ cluster). JMS Messages are not
     * persisted to a highly availablestore. If this attribute is "true" the
     * MQ cluster pointed to by the jms-service element is a HA (enhanced)
     * cluster and the MQ cluster uses the database pointed to by jdbcurl to save persistent JMS messages and
     * other broker cluster configuration information. Individual applications
     * will not be able to control or override MQ cluster availability levels.
     * They inherit the availability attribute defined in this element.
     * If this attribute is missing, availability is turned off by default
     * [i.e. the MQ cluster associated with the AS cluster would behave as a
     * non-HA cluster]
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false",dataType=Boolean.class)
    String getAvailabilityEnabled();

    /**
     * Sets the value of the availabilityEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setAvailabilityEnabled(String value) throws PropertyVetoException;

    /**
      * Gets the value of the Config Store type property
      *
      * This attribute specifies whether to use a master broker or a Shared Database
      * for conventional MQ clusters
      * This is a no-op for Enhanced clusters
      *
      * @return possible object is
      *         {@link String }
      */

    @Attribute (defaultValue="masterbroker")
    @Pattern(regexp = PATTERN_BROKER, message = "Valid values: " + PATTERN_BROKER)
    String getConfigStoreType();


    /**
     * Sets the value of the Config Store type property.
     *
     * @param value allowed object is
     *              {@link String }
     */

    void setConfigStoreType(String value);

    /**
      * Gets the value of the Message Store type property
      *
      * This attribute specifies where messages need to be stored by MQ.
      * The options are file based or Database based storage
      * This is only relevent for conventional MQ clusters
      * This is a no-op for enhanced clusters
      *
      * @return possible object is
      *         {@link String }
      */

    @Attribute(defaultValue = "file")
    @Pattern(regexp = PATTERN_MESSAGE_STORE_TYPE, message = "Valid values: " + PATTERN_MESSAGE_STORE_TYPE)
    String getMessageStoreType();


    /**
     * Sets the value of the Message store type property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMessageStoreType(String value);



    /**
     * Gets the value of the DB Vendor property.
     *
     * This is the DB Vendor Name for the DB used by the MQ broker cluster
     * for use in saving persistent JMS messages and other broker
     * cluster configuration information.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getDbVendor();

    /**
     * Sets the value of the DB Vendor property.
     *
     * @param value allowed object is
     *            {@link String }
     */
    void setDbVendor(String value) throws PropertyVetoException;

    /**
     * Gets the value of the DB User Name property.
     * This is the DB user Name for the DB used by the MQ broker cluster
     * for use in saving persistent JMS messages and other broker
     * cluster configuration information.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getDbUsername();

    /**
     * Sets the value of the DB UserName property.
     *
     * @param value allowed object is
     *            {@link String }
     */
    void setDbUsername(String value) throws PropertyVetoException;

    /**
     * Gets the value of the DB Password property.
     *
     * This is the DB Password for the DB used by the MQ broker cluster
     * for use in saving persistent JMS messages and other broker
     * cluster configuration information.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getDbPassword();

    /**
     * Sets the value of the DB password property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setDbPassword(String value) throws PropertyVetoException;

    /**
     * Gets the value of the JDBC URL property.
     *
     * This is the JDBC URL used by the MQ broker
     * cluster for use in saving persistent JMS messages and other broker
     * cluster configuration information.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getDbUrl();

    /**
     * Sets the value of the JDBC URL property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setDbUrl(String value) throws PropertyVetoException;

    /**
     * Gets the value of the MQ Store pool name property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getMqStorePoolName();

    /**
     * Sets the value of the MQ store pool name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMqStorePoolName(String value) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}

