/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin.config;

import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * A configured element which has to have application type of name.
 *
 * @author Nandini Ektare
 */
@Configured
public interface ApplicationName extends ConfigBeanProxy, Payload {

    String NAME_APP_REGEX = "[\\p{L}\\p{N}_][\\p{L}\\p{N}\\-_./;:#]*";

    /**
     * Name of the configured object
     *
     * @return name of the configured object
     */
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = NAME_APP_REGEX, message = "{app.invalid.name}", payload = ApplicationName.class)
    String getName();

    void setName(String name) throws PropertyVetoException;
}
