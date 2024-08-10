/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.JndiNameEnvironment;

import jakarta.enterprise.inject.spi.AnnotatedType;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.jboss.weld.injection.spi.InjectionContext;
import org.jboss.weld.injection.spi.InjectionServices;

/**
 * The InjectionServices for a non-module bda (library or rar). A non-module bda has no associated bundle so we cannot
 * reuse the InjectionServicesImpl for injecting into a bean that's resides in an application library (not WEB-INF/lib)
 * or rar
 *
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class NonModuleInjectionServices implements InjectionServices {

    private InjectionManager injectionManager;

    public NonModuleInjectionServices(InjectionManager injectionMgr) {
        injectionManager = injectionMgr;
    }

    @Override
    public <T> void aroundInject(InjectionContext<T> injectionContext) {
        try {
            ServiceLocator serviceLocator = Globals.getDefaultHabitat();
            ComponentEnvManager compEnvManager = serviceLocator.getService(ComponentEnvManager.class);

            JndiNameEnvironment componentEnv = compEnvManager.getCurrentJndiNameEnvironment();

            Object target = injectionContext.getTarget();
            String targetClass = target.getClass().getName();

            if (componentEnv == null) {
                //throw new IllegalStateException("No valid EE environment for injection of " + targetClass);
                System.err.println("No valid EE environment for injection of " + targetClass);
                injectionContext.proceed();
                return;
            }

            injectionManager.injectInstance(target, componentEnv, false);
            injectionContext.proceed();

        } catch (InjectionException ie) {
            throw new IllegalStateException(ie.getMessage(), ie);
        }
    }

    @Override
    public <T> void registerInjectionTarget(jakarta.enterprise.inject.spi.InjectionTarget<T> injectionTarget,
            AnnotatedType<T> annotatedType) {
    }

    @Override
    public void cleanup() {
    }

}
