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

import com.sun.enterprise.config.serverbeans.customvalidators.JavaClassName;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
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
 * An audit-module specifies an optional plug-in module which implements audit capabilities.
 */
@Configured
@RestRedirects({
        @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-audit-module"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-audit-module")
})
public interface AuditModule extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the {@code name} property.
     *
     * <p>Defines the name of this realm.
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
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
     * Gets the value of the {@code classname} property.
     *
     * <p>Defines the java class which implements this audit module.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    @JavaClassName
    String getClassname();

    /**
     * Sets the value of the {@code classname} property.
     *
     * @param classname allowed object is {@link String}
     */
    void setClassname(String classname) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
