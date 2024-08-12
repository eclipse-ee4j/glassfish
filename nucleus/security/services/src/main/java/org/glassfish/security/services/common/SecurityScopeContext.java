/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.common;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ServiceHandle;
import org.jvnet.hk2.annotations.Service;

/**
 * The security context used to enable the scoping of security service instances.
 */
@Service
@Singleton
public class SecurityScopeContext implements Context<SecurityScope> {
    private final HashMap<String, HashMap<ActiveDescriptor<?>, Object>> contexts =
        new HashMap<>();

    @Inject
    private StateManager manager;

    @Override
    public Class<? extends Annotation> getScope() {
        return SecurityScope.class;
    }

    @Override
    public <T> T findOrCreate(ActiveDescriptor<T> activeDescriptor, ServiceHandle<?> root) {
        HashMap<ActiveDescriptor<?>, Object> mappings = getCurrentContext();

        Object retVal = mappings.get(activeDescriptor);
        if (retVal == null) {
            retVal = activeDescriptor.create(root);

            mappings.put(activeDescriptor, retVal);
        }
        return (T) retVal;
    }

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        HashMap<ActiveDescriptor<?>, Object> mappings = getCurrentContext();

        return mappings.containsKey(descriptor);
    }

    @Override
    public boolean isActive() {
        return manager.getCurrent() != null;
    }

    private HashMap<ActiveDescriptor<?>, Object> getCurrentContext() {
        if (manager.getCurrent() == null) {
            throw new IllegalStateException("Not In Active State");
        }

        HashMap<ActiveDescriptor<?>, Object> retVal = contexts.get(manager.getCurrent());
        if (retVal == null) {
            retVal = new HashMap<>();

            contexts.put(manager.getCurrent(), retVal);
        }

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#supportsNullCreation()
     */
    @Override
    public boolean supportsNullCreation() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Context#shutdown()
     */
    @Override
    public void shutdown() {
        // Do nothing

    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        // TODO Auto-generated method stub

    }
}
