/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * This specifies configuration for a web service endpoint.
 *
 * <p>This web service endpoint could be JAX-RPC or JSR-109 web service.
 * It contains configuration about Monitoring, Transformation rules and Monitoring Log.
 */

@Configured
public interface WebServiceEndpoint extends ApplicationExtension {

    /**
     * Gets the value of the {@code name} property.
     *
     * <p>Fully qualified web service name.
     *
     * <p>Format:
     * <ul>
     * <li><em>|ModuleName|#|EndpointName|</em> - if the web service endpoint belongs to an
     * application. (Parent of this element is j2ee-application).</li>
     * <li><em>|EndpointName|</em> - if the web service endpoint belongs to stand alone
     * ejb-module or web-module (Parent of this element is either ejb-module
     * or web-module).</li>
     * </ul>
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
    @NotNull
    String getName();

    /**
     * Sets the value of the {@code name} property.
     *
     * @param value allowed object is {@link String}
     */
    void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the {@code monitoring} property.
     *
     * <p>Monitoring level for this web service.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "OFF")
    String getMonitoring();

    /**
     * Sets the value of the {@code monitoring} property.
     *
     * @param value allowed object is {@link String}
     */
    void setMonitoring(String value) throws PropertyVetoException;

    /**
     * Gets the value of the {@code maxHistorySize} property.
     *
     * <p>Maximum number of monitoring records stored in history for this end point.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "25")
    @Min(value=1)
    String getMaxHistorySize();

    /**
     * Sets the value of the {@code maxHistorySize} property.
     *
     * @param value allowed object is {@link String}
     */
    void setMaxHistorySize(String value) throws PropertyVetoException;

    /**
     * Gets the value of the {@code registryLocation} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for the
     * {@code registryLocation} property.
     *
     * <p>For example, to add a new item, do as follows:
     * <pre>
     *    getRegistryLocation().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link RegistryLocation}
     */
    @Element
    List<RegistryLocation> getRegistryLocation();

    /**
     * Gets the value of the {@code transformationRule} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the transformationRule property.
     *
     * <p>For example, to add a new item, do as follows:
     * <pre>
     *    getTransformationRule().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link TransformationRule}
     */
    @Element
    List<TransformationRule> getTransformationRule();
}
