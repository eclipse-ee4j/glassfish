/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.spi.util;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;

import java.util.Collection;

import javax.naming.NamingException;

import org.glassfish.api.invocation.ApplicationEnvironment;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ComponentEnvManager {

    // FIXME: doesn't it?
    /**
     * @param componentId can be null, then method returns null.
     * @deprecated Remove once WebContainer sets JndiNameEnvironment on every "new ComponentInvocation()"
     */
    @Deprecated
    JndiNameEnvironment getJndiNameEnvironment(String componentId);

    /**
     * @deprecated Remove once WebContainer sets JndiNameEnvironment on every "new ComponentInvocation()"
     */
    @Deprecated
    JndiNameEnvironment getCurrentJndiNameEnvironment();

    String getComponentEnvId(JndiNameEnvironment env);

    String bindToComponentNamespace(JndiNameEnvironment env) throws NamingException;

    void addToComponentNamespace(
        JndiNameEnvironment origEnv,
        Collection<EnvironmentProperty> envProps,
        Collection<ResourceReferenceDescriptor> resRefs)
            throws NamingException;

    void unbindFromComponentNamespace(JndiNameEnvironment env) throws NamingException;

    /**
     * Returns the current application environment if not running in a specified
     * container
     *
     * @return The current application environment or null if we are not currently
     *         running as a specific application
     */
    ApplicationEnvironment getCurrentApplicationEnvironment();

}
