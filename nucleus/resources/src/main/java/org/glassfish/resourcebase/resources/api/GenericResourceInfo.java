/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resourcebase.resources.api;

import java.io.Serializable;

import org.glassfish.api.naming.SimpleJndiName;

/**
 * Represents resource information.
 * A resource may be defined in "java:app" scope or "java:module" scope
 *
 * @author Jagadish Ramu
 */
public interface GenericResourceInfo extends Serializable {

    /**
     * Name of the resource
     *
     * @return String name
     */
    SimpleJndiName getName();

    /**
     * Application in which the resource is defined
     *
     * @return String application-name
     */
    String getApplicationName();

    /**
     * Module in which the resource is defined
     *
     * @return String module-name
     */
    String getModuleName();
}
