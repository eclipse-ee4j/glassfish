/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.serverbeans;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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

import static org.glassfish.config.support.Constants.NAME_REGEX;

/**
 * Profilers could be one of {@code jprobe}, {@code optimizeit}, {@code hprof}, {@code wily}
 * and so on. {@code Jvm-options} and property elements are used to record the settings
 * needed to get a particular profiler going. A server instance is tied to a particular
 * profiler, by the profiler element in {@code java-config} . Changing the profiler
 * will require a server restart.
 *
 * <p>The adminstrative graphical interfaces, could list multiple supported profilers
 * (incomplete at this point) and will populate {@code server.xml} appropriately.
 */
@Configured
public interface Profiler extends ConfigBeanProxy, PropertyBag, JvmOptionBag {

    /**
     * Gets the value of the {@code name} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute // bizarre case of having a name, but it's not a key; it's a singleton
    @NotNull
    @Pattern(regexp = NAME_REGEX, message = "Pattern: " + NAME_REGEX)
    String getName();

    /**
     * Sets the value of the {@code name} property.
     *
     * @param name allowed object is {@link String}
     */
    void setName(String name) throws PropertyVetoException;

    /**
     * Gets the value of the {@code classpath} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getClasspath();

    /**
     * Sets the value of the {@code classpath} property.
     *
     * @param classpath allowed object is {@link String}
     */
    void setClasspath(String classpath) throws PropertyVetoException;

    /**
     * Gets the value of the {@code nativeLibraryPath} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getNativeLibraryPath();

    /**
     * Sets the value of the {@code nativeLibraryPath} property.
     *
     * @param nativeLibraryPath allowed object is {@link String}
     */
    void setNativeLibraryPath(String nativeLibraryPath) throws PropertyVetoException;

    /**
     * Gets the value of the {@code enabled} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the {@code enabled} property.
     *
     * @param enabled allowed object is {@link String}
     */
    void setEnabled(String enabled) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
