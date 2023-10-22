/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.api.monitoring;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;

import org.glassfish.api.admin.config.Named;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;


/**
 * Default monitoring configuration for containers.
 *
 * <p>Containers can provide their configuration through the {@code ContainerMonitoring} interface.
 *
 * @author Nandini Ektare
 * @author Byron Nevins (3.1+)
 */
@Configured
public interface ContainerMonitoring extends ConfigBeanProxy, Named {

    String LEVEL_OFF = "OFF";

    String LEVEL_LOW = "LOW";

    String LEVEL_HIGH = "HIGH";

    /**
     * The monitoring {@code level} of this monitoring item.
     *
     * @return String with values {@code HIGH}/{@code LOW}/{@code OFF}
     */
    @Attribute(defaultValue = "OFF")
    @NotNull String getLevel();

    /**
     * Set the level of this monitoring module.
     *
     * @param level new monitoring level
     */

    void setLevel(String level) throws PropertyVetoException;
}
