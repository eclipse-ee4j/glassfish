/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.deployment;


/**
 * Interface for descriptors managing collections of {@link ConnectorConfigProperty}.
 */
public interface ConnectorConfigPropertySetDescriptor {

    /**
     * @return Set of {@link ConnectorConfigProperty} elements
     */
    OrderedSet<ConnectorConfigProperty> getConfigProperties();


    /**
     * @param configProperty to add
     */
    void addConfigProperty(ConnectorConfigProperty configProperty);


    /**
     * @param configProperty to remove
     */
    void removeConfigProperty(ConnectorConfigProperty configProperty);
}
