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

package com.sun.enterprise.connectors.jms.config;

import com.sun.enterprise.config.modularity.annotation.CustomConfiguration;
import com.sun.enterprise.config.modularity.annotation.HasCustomizationTokens;
import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.api.admin.config.Container;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.PropertyDesc;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.beans.PropertyVetoException;
import java.util.List;

/**
 * The jms-service element specifies information about the bundled/built-in
 * JMS service that is managed by Application Server
 */

/* @XmlType(name = "", propOrder = {
    "jmsHost",
    "property"
}) */

@Configured
@HasCustomizationTokens
@CustomConfiguration(baseConfigurationFileName = "jms-module-conf.xml")
public interface JmsService extends ConfigExtension, PropertyBag, Container {

    /**
     * Gets the value of the initTimeoutInSeconds property.
     *
     * specifies the time server instance will wait at start up, for its
     * corresponding JMS service instance to respond. If there is no response
     * within the specifies timeout period, application server startup is
     * aborted. Default value of 60 seconds
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="60")
    @Min(value=1)
    String getInitTimeoutInSeconds();

    /**
     * Sets the value of the initTimeoutInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setInitTimeoutInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the type property.
     *
     * Type of JMS service
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    @Pattern(regexp="(LOCAL|EMBEDDED|REMOTE)")
    String getType();

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the startArgs property.
     *
     * specifies the arguments that will be supplied to start up corresponding
     * JMS service instance.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getStartArgs();

    /**
     * Sets the value of the startArgs property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setStartArgs(String value) throws PropertyVetoException;

    /**
     * Gets the value of the defaultJmsHost property.
     *
     * Reference to a jms-host that to be started when type of jms-service
     * is LOCAL.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getDefaultJmsHost();

    /**
     * Sets the value of the defaultJmsHost property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setDefaultJmsHost(String value) throws PropertyVetoException;

     @Attribute
    String getMasterBroker();

    /**
     * Sets the value of the MasterBroker property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMasterBroker(String value) throws PropertyVetoException;

    /**
     * Gets the value of the reconnectIntervalInSeconds property.
     *
     * Interval between reconnect attempts, in seconds. An integer.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="5")
    @Min(value=1)
    String getReconnectIntervalInSeconds();

    /**
     * Sets the value of the reconnectIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setReconnectIntervalInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the reconnectAttempts property.
     *
     * Total number of attempts to reconnect. An integer.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="3")
    String getReconnectAttempts();

    /**
     * Sets the value of the reconnectAttempts property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setReconnectAttempts(String value) throws PropertyVetoException;

    /**
     * Gets the value of the reconnectEnabled property.
     *
     * Causes reconnect feature to be enabled (true) or disabled (false).
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    String getReconnectEnabled();

    /**
     * Sets the value of the reconnectEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setReconnectEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the addresslistBehavior property.
     *
     * Determines broker selection from imqAddressList.
     * random
     *      causes selection to be performed randomly
     * priority
     *      causes selection to be performed sequentially
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="random")
    @Pattern(regexp="(random|priority)")
    String getAddresslistBehavior();

    /**
     * Sets the value of the addresslistBehavior property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setAddresslistBehavior(String value) throws PropertyVetoException;

    /**
     * Gets the value of the addresslistIterations property.
     *
     * Number of times reconnect logic should iterate imqAddressList.
     * This property will not be used if the addresslist-behavior is "random".
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="3")
    @Min(value=-1)
    @Max(value=Integer.MAX_VALUE)
    String getAddresslistIterations();

    /**
     * Sets the value of the addresslistIterations property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setAddresslistIterations(String value) throws PropertyVetoException;

    /**
     * Gets the value of the mqScheme property.
     *
     * Scheme for establishing connection with broker. For e.g. scheme can be
     * specified as "http" for connecting to MQ broker over http. Default: "mq"
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @Pattern(regexp="(mq||http)")
    String getMqScheme();

    /**
     * Sets the value of the mqScheme property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMqScheme(String value) throws PropertyVetoException;

    /**
     * Gets the value of the mqService property.
     *
     * Type of broker service. If a broker supports ssl, then the type of
     * service can be "ssljms". If nothing is specified, MQ will assume
     * that service is "jms".
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @Pattern(regexp="(ssljms||jms)")
    String getMqService();

    /**
     * Sets the value of the mqService property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setMqService(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jmsHost property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jmsHost property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJmsHost().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link JmsHost }
     */
    @Element
    List<JmsHost> getJmsHost();

     /**
        Properties.
     */
@PropertiesDesc(
    props={
        @PropertyDesc(name="instance-name", defaultValue="imqbroker",
            description="The full Sun GlassFish Message Queue broker instance name"),

        @PropertyDesc(name="instance-name-suffix", defaultValue="xxxxxxxxxxxxxxxxxx",
            description="A suffix to add to the full Message Queue broker instance name. The suffix is separated " +
                "from the instance name by an underscore character (_). For example, if the instance name is 'imqbroker', " +
                "appending the suffix 'xyz' changes the instance name to 'imqbroker_xyz'"),

        @PropertyDesc(name="append-version", defaultValue="",
            description="If true, appends the major and minor version numbers, preceded by underscore characters (_), " +
                "to the full Message Queue broker instance name. For example, if the instance name is 'imqbroker', " +
                "appending the version numbers changes the instance name to imqbroker_8_0"),

        @PropertyDesc(name="user-name", defaultValue="xxxxxxxxxxxxxxxxxx",
            description="Specifies the user name for creating the JMS connection. Needed only if the default " +
                "username/password of guest/guest is not available in the broker"),

        @PropertyDesc(name="password", defaultValue="xxxxxxxxxxxxxxxxxx",
            description="Specifies the password for creating the JMS connection. Needed only if the default " +
                "username/password of guest/guest is not available in the broker")
    }
    )
    @Element
    List<Property> getProperty();
}
