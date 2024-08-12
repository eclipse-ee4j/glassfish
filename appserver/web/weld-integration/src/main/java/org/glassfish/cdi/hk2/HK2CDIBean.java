/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.hk2;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;

import static java.util.Collections.emptySet;

/**
 * This is a CDI bean that is backed by an HK2 service
 *
 * @author jwells
 *
 */
public class HK2CDIBean<T> implements Bean<T> {
    private final ServiceLocator locator;
    private final ActiveDescriptor<T> descriptor;

    /* package */
    HK2CDIBean(ServiceLocator serviceLocator, ActiveDescriptor<T> descriptor) {
        this.locator = serviceLocator;
        this.descriptor = descriptor;
    }

    @Override
    public T create(CreationalContext<T> arg0) {
        ServiceHandle<T> serviceHandle = locator.getServiceHandle(descriptor);
        return serviceHandle.getService();
    }

    @Override
    public void destroy(T arg0, CreationalContext<T> arg1) {
        descriptor.dispose(arg0);
    }

    @Override
    public Class<?> getBeanClass() {
        return descriptor.getImplementationClass();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        if (descriptor.getQualifierAnnotations().isEmpty()) {
            return Set.of(Default.Literal.INSTANCE);
        }

        return descriptor.getQualifierAnnotations();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        Class<? extends Annotation> scope = descriptor.getScopeAnnotation();
        if (scope == null || scope.equals(PerLookup.class)) {
            scope = Dependent.class;
        }

        return scope;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return emptySet();
    }

    @Override
    public Set<Type> getTypes() {
        return descriptor.getContractTypes();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    public ActiveDescriptor<T> getHK2Descriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return "HK2CDIBean(" + descriptor + "," + System.identityHashCode(this) + ")";
    }
}
