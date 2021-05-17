/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers.interceptors;

import com.sun.enterprise.container.common.spi.JavaEEInterceptorBuilder;
import com.sun.enterprise.container.common.spi.JavaEEInterceptorBuilderFactory;
import com.sun.enterprise.container.common.spi.util.InterceptorInfo;

import org.jvnet.hk2.annotations.Service;
import com.sun.ejb.EJBUtils;
import com.sun.ejb.codegen.EjbOptionalIntfGenerator;
import com.sun.ejb.spi.container.OptionalLocalInterfaceProvider;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;

/**
 *
 */
@Service
public class JavaEEInterceptorBuilderFactoryImpl implements JavaEEInterceptorBuilderFactory {


    private static Logger _logger = LogDomains.getLogger(JavaEEInterceptorBuilderImpl.class,
            LogDomains.CORE_LOGGER);

    public JavaEEInterceptorBuilder createBuilder(InterceptorInfo info) throws Exception {

        Class targetObjectClass = info.getTargetClass();

        // Create an interface with all public methods of the target class
        // in order to create a dynamic proxy
        String subClassIntfName = EJBUtils.getGeneratedOptionalInterfaceName(targetObjectClass.getName());

        EjbOptionalIntfGenerator gen = new EjbOptionalIntfGenerator(targetObjectClass.getClassLoader());
        gen.generateOptionalLocalInterface(targetObjectClass, subClassIntfName);
        Class subClassIntf = gen.loadClass(subClassIntfName);

        String beanSubClassName = subClassIntfName + "__Bean__";

        // Generate a sub-class of the application's class.  Use an instance of this subclass
        // as the actual object passed back to the application.  The sub-class instance
        // delegates all public methods to the dyanamic proxy, which calls the
        // InvocationHandler.
        gen.generateOptionalLocalInterfaceSubClass(
            targetObjectClass, beanSubClassName, subClassIntf);

        Class subClass = gen.loadClass(beanSubClassName);


        // TODO do interceptor builder once per managed bean
        InterceptorManager interceptorManager = new InterceptorManager(_logger,
                targetObjectClass.getClassLoader(), targetObjectClass.getName(),
                info);


        JavaEEInterceptorBuilderImpl builderImpl =
                new JavaEEInterceptorBuilderImpl(info, interceptorManager,
                        gen, subClassIntf, subClass);

        return builderImpl;

    }

    /**
      * Tests if a given object is a client proxy associated with an interceptor invoker.
      */
    public boolean isClientProxy(Object obj) {

        Class clazz = obj.getClass();

        return (OptionalLocalInterfaceProvider.class.isAssignableFrom(clazz));
    }


}


