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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;

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
@SuppressWarnings("serial")
public class CDIHK2Descriptor<T> extends AbstractActiveDescriptor<T> {
    private transient BeanManager manager = null;
    private transient Bean<T> bean = null;
    private transient Type requiredType = null;
    
    public CDIHK2Descriptor() {
        super();
    }
    
    private static Set<Annotation> fixQualifiers(Bean<?> bean) {
        Set<Annotation> fromBean = bean.getQualifiers();
        Set<Annotation> retVal = new HashSet<Annotation>();
        
        for (Annotation beanQ : fromBean) {
            if (Any.class.equals(beanQ.annotationType())) continue;
            
            if (Default.class.equals(beanQ.annotationType())) continue;
            
            retVal.add(beanQ);
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
        super(bean.getTypes(),
                fixScope(bean),
                bean.getName(),
                fixQualifiers(bean),
                DescriptorType.CLASS,
                DescriptorVisibility.NORMAL,
                0,
                null,
                null,
                null,
                new HashMap<String, List<String>>());
                
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
        CreationalContext<T> cc = manager.createCreationalContext(bean);
        
        return (T) manager.getReference(bean, requiredType, cc);
    }

}
