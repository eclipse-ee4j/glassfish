/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.serverbeans;

import java.util.List;

import org.glassfish.api.admin.config.ApplicationName;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * Element describing the {@code system-applications} in {@code domain.xml}
 */
@Configured
public interface SystemApplications extends Applications {

    /**
     * Gets the value of the {@code system-applications} property. Objects of the
     * following type(s) are allowed in the list: {@link Application}.
     */
    @Element("*")
    List<ApplicationName> getModules();
}
