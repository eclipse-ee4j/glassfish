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

package org.glassfish.webservices.config;

import com.sun.enterprise.config.serverbeans.ApplicationExtension;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.beans.PropertyVetoException;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * This specifies configuration for a web service end point. This web service
 * end point could be JAXRPC or JSR-109 web service. It contains configuration
 * about Monitoring, Transformation rules and Monitoring Log
 */

/* @XmlType(name = "", propOrder = {
    "registryLocation",
    "transformationRule"
}) */

@Configured
public interface WebServiceEndpoint extends ApplicationExtension {

    /**
     * Gets the value of the name property.
     *
     * fully qualified web service name. Format:
     * |ModuleName|#|EndpointName|, if the web service endpoint belongs to an
     * application. (Parent of this element is j2ee-application).
     * |EndpointName|, if the web service endpoint belongs to stand alone
     * ejb-module or web-module (Parent of this element is either ejb-module
     * or web-module).
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @NotNull
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the monitoring property.
     *
     * Monitoring level for this web service.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="OFF")
    public String getMonitoring();

    /**
     * Sets the value of the monitoring property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMonitoring(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxHistorySize property.
     *
     * Maximum number of monitoring records stored in history for this end point
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="25")
    @Min(value=1)
    public String getMaxHistorySize();

    /**
     * Sets the value of the maxHistorySize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxHistorySize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the jbiEnabled property.
     *
     * If true, it enables the visibility of this endoint as a service in JBI.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false",dataType=Boolean.class)
    public String getJbiEnabled();

    /**
     * Sets the value of the jbiEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setJbiEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the registryLocation property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the registryLocation property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegistryLocation().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link RegistryLocation }
     */
    @Element
    public List<RegistryLocation> getRegistryLocation();

    /**
     * Gets the value of the transformationRule property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transformationRule property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransformationRule().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link TransformationRule }
     */
    @Element
    public List<TransformationRule> getTransformationRule();

}
