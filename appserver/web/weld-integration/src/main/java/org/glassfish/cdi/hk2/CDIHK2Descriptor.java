/*
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
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;

/**
 * This is an HK2 Descriptor that is backed by a CDI bean
 *
 * @author jwells
 *
 */
public class CDIHK2Descriptor<T> extends AbstractActiveDescriptor<T> {
    private transient BeanManager manager;
    private transient Bean<T> bean;
    private transient Type requiredType ;

    public CDIHK2Descriptor() {
    }

    private static Set<Annotation> fixQualifiers(Bean<?> bean) {
        Set<Annotation> beanQualifiers = bean.getQualifiers();
        Set<Annotation> retVal = new HashSet<>();

        for (Annotation beanQualifier : beanQualifiers) {
            if (Any.class.equals(beanQualifier.annotationType())) {
                continue;
            }

            if (Default.class.equals(beanQualifier.annotationType())) {
                continue;
            }

            retVal.add(beanQualifier);
        }

        return retVal;
    }

    private static Class<? extends Annotation> fixScope(Bean<?> bean) {
        if (bean.getScope() == null || Dependent.class.equals(bean.getScope())) {
            return PerLookup.class;
        }

        if (Singleton.class.equals(bean.getScope())) {
            return Singleton.class;
        }

        return CDIScope.class;
    }

    // @SuppressWarnings("unchecked")
    public CDIHK2Descriptor(BeanManager manager, Bean<T> bean, Type requiredType) {
        super(
            bean.getTypes(),
            fixScope(bean),
            bean.getName(),
            fixQualifiers(bean),
            DescriptorType.CLASS,
            DescriptorVisibility.NORMAL, 0,
            null, null, null,
            new HashMap<>());

        this.manager = manager;
        this.bean = bean;
        this.requiredType = requiredType;
    }

    @Override
    public String getImplementation() {
        return bean.getBeanClass().getName();
    }

    @Override
    public Type getImplementationType() {
        return bean.getBeanClass().getGenericSuperclass();
    }

    @Override
    public Class<?> getImplementationClass() {
        return bean.getBeanClass();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create(ServiceHandle<?> root) {
        return (T) manager.getReference(bean, requiredType, manager.createCreationalContext(bean));
    }

}
