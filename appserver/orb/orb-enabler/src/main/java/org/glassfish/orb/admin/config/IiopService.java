/*
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

package org.glassfish.orb.admin.config;

import com.sun.enterprise.config.serverbeans.SslClientConfig;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.ConfigExtension;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;


/* @XmlType(name = "", propOrder = {
    "orb",
    "sslClientConfig",
    "iiopListener"
}) */

@Configured
//@CustomConfiguration(baseConfigurationFileName = "iiop-module-conf.xml")
//@HasCustomizationTokens
public interface IiopService extends ConfigBeanProxy, ConfigExtension   {


    /**
     * Gets the value of the clientAuthenticationRequired property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false",dataType=Boolean.class)
    String getClientAuthenticationRequired();

    /**
     * Sets the value of the clientAuthenticationRequired property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setClientAuthenticationRequired(String value) throws PropertyVetoException;

    /**
     * Gets the value of the orb property.
     *
     * @return possible object is
     *         {@link Orb }
     */
    @Element(required=true)
    Orb getOrb();

    /**
     * Sets the value of the orb property.
     *
     * @param value allowed object is
     *              {@link Orb }
     */
    void setOrb(Orb value) throws PropertyVetoException;

    /**
     * Gets the value of the sslClientConfig property.
     *
     * @return possible object is
     *         {@link SslClientConfig }
     */
    @Element
    SslClientConfig getSslClientConfig();

    /**
     * Sets the value of the sslClientConfig property.
     *
     * @param value allowed object is
     *              {@link SslClientConfig }
     */
    void setSslClientConfig(SslClientConfig value) throws PropertyVetoException;

    /**
     * Gets the value of the iiopListener property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the iiopListener property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIiopListener().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link IiopListener }
     */
    @Element
    List<IiopListener> getIiopListener();
}
