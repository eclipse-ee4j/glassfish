/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

import com.sun.ejb.codegen.EjbOptionalIntfGenerator;
import com.sun.ejb.spi.container.OptionalLocalInterfaceProvider;
import com.sun.enterprise.container.common.spi.JavaEEInterceptorBuilder;
import com.sun.enterprise.container.common.spi.JavaEEInterceptorBuilderFactory;
import com.sun.enterprise.container.common.spi.util.InterceptorInfo;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Service;

import static com.sun.ejb.EJBUtils.getGeneratedOptionalInterfaceName;
import static com.sun.logging.LogDomains.CORE_LOGGER;

@Service
public class JavaEEInterceptorBuilderFactoryImpl implements JavaEEInterceptorBuilderFactory {

    private static final Logger LOG = LogDomains.getLogger(JavaEEInterceptorBuilderImpl.class, CORE_LOGGER, false);

    @Override
    public JavaEEInterceptorBuilder createBuilder(InterceptorInfo info) throws Exception {
        Class<?> targetObjectClass = info.getTargetClass();

        // Create an interface with all public methods of the target class
        // in order to create a dynamic proxy
        String subClassInterfaceName = getGeneratedOptionalInterfaceName(targetObjectClass.getName());

        EjbOptionalIntfGenerator interfaceGenerator = new EjbOptionalIntfGenerator();
        interfaceGenerator.generateOptionalLocalInterface(targetObjectClass, subClassInterfaceName);
        Class<?> subClassInterface = interfaceGenerator.loadClass(subClassInterfaceName, targetObjectClass);

        String beanSubClassName = subClassInterfaceName + "__Bean__";

        // Generate a sub-class of the application's class. Use an instance of this subclass
        // as the actual object passed back to the application. The sub-class instance
        // delegates all public methods to the dyanamic proxy, which calls the
        // InvocationHandler.
        interfaceGenerator.generateOptionalLocalInterfaceSubClass(targetObjectClass, beanSubClassName, subClassInterface);

        Class<?> subClass = interfaceGenerator.loadClass(beanSubClassName, targetObjectClass);

        // TODO do interceptor builder once per managed bean
        InterceptorManager interceptorManager =
            new InterceptorManager(LOG, targetObjectClass.getClassLoader(), targetObjectClass.getName(), info);

        return new JavaEEInterceptorBuilderImpl(info, interceptorManager, interfaceGenerator, subClassInterface, subClass);
    }

    /**
     * Tests if a given object is a client proxy associated with an interceptor invoker.
     */
    @Override
    public boolean isClientProxy(Object obj) {
        return OptionalLocalInterfaceProvider.class.isAssignableFrom(obj.getClass());
    }

}
