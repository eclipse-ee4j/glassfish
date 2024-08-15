/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.services;

import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EJBContextImpl;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.weld.construction.api.AroundConstructCallback;
import org.jboss.weld.construction.api.ConstructionHandle;
import org.jboss.weld.exceptions.WeldException;

/**
 * This calls back into the ejb container to perform the around construct interception. When that's finished the ejb
 * itself is then created.
 */
public class CDIAroundConstructCallback<T> implements AroundConstructCallback<T> {
    private BaseContainer container;
    private EJBContextImpl ejbContext;

    // The AroundConstruct interceptor method can access the constructed instance using
    // InvocationContext.getTarget method after the InvocationContext.proceed completes.
    private final AtomicReference<T> target = new AtomicReference<>();

    private ConstructionHandle<T> handle;
    private Object[] parameters;

    public CDIAroundConstructCallback(BaseContainer container, EJBContextImpl ejbContext) {
        this.container = container;
        this.ejbContext = ejbContext;
    }

    @Override
    public T aroundConstruct(final ConstructionHandle<T> handle, AnnotatedConstructor<T> constructor, Object[] parameters, Map<String, Object> data) {
        this.handle = handle;
        this.parameters = parameters;
        T ejb;

        try {
            container.intercept(LifecycleCallbackDescriptor.CallbackType.AROUND_CONSTRUCT, ejbContext);

            // all the interceptors were invoked, call the constructor now
            if (target.get() == null) {
                ejb = handle.proceed(parameters, new HashMap<String, Object>());
                target.set(ejb);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new WeldException(e);
        }

        return target.get();
    }

    public T createEjb() {
        T instance = null;
        if (handle != null) {
            instance = handle.proceed(parameters, new HashMap<String, Object>());
        }
        target.set(instance);

        return instance;
    }
}
