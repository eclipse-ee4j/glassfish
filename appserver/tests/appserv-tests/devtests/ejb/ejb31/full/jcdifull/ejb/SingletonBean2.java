/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import jakarta.ejb.*;
import jakarta.annotation.*;
import jakarta.interceptor.*;

import jakarta.inject.Inject;

import javax.naming.InitialContext;

import jakarta.enterprise.inject.spi.BeanManager;

import jakarta.enterprise.event.Event;

import java.lang.reflect.Method;

import org.jboss.weld.examples.translator.*;

@Singleton
@Startup
public class SingletonBean2 {

    private final Event<SomeEvent> someEvent;

    @Inject
    public SingletonBean2(Event<SomeEvent> se) {
        someEvent = se;
        System.out.println("In SingletonBean2 someEvent = " + someEvent);

    }

    @Inject
    Foo foo;

    @EJB
    private StatelessLocal statelessEE;

    @Inject
    private TranslatorController tc;

    @Inject
    StatelessLocal2 sl2;

    @Resource
    private BeanManager beanManagerInject;

    @Resource
    SessionContext sesCtx;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean2::init()");
        if (beanManagerInject == null) {
            throw new EJBException("BeanManager is null");
        }
        System.out.println("Bean manager inject = " + beanManagerInject);
        testIMCreateDestroyMO();
        testSingletonWithInjectionConstructor();

        System.out.println("Sending some event...");
        someEvent.fire(new SomeEvent(2));
    }

    public void hello() {
        System.out.println("In SingletonBean2::hello() " + foo);
        statelessEE.hello();

        BeanManager beanMgr = (BeanManager) sesCtx.lookup("java:comp/BeanManager");

        System.out.println("Successfully retrieved bean manager " + beanMgr + " for CDI enabled app");

    }

    @Schedule(second = "*/10", minute = "*", hour = "*")
    private void timeout() {
        System.out.println("In SingletonBean::timeout() " + foo);

        System.out.println("tc.getText() = " + tc.getText());

        statelessEE.hello();
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }

    private void testIMCreateDestroyMO() {
        try {
            // Test InjectionManager managed bean functionality
            Object injectionMgr = new InitialContext().lookup("com.sun.enterprise.container.common.spi.util.InjectionManager");
            Method createManagedMethod = injectionMgr.getClass().getMethod("createManagedObject", java.lang.Class.class);

            FooNonManagedBean nonF = (FooNonManagedBean) createManagedMethod.invoke(injectionMgr, FooNonManagedBean.class);
            System.out.println("FooNonManagedBean = " + nonF);
            nonF.hello();
            
            Method destroyManagedMethod = injectionMgr.getClass().getMethod("destroyManagedObject", java.lang.Object.class);
            destroyManagedMethod.invoke(injectionMgr, nonF);

        } catch (Exception e) {
            throw new EJBException(e);
        }

    }

    private void testSingletonWithInjectionConstructor() {
        try {
            SingletonBeanA b = (SingletonBeanA) sesCtx.lookup("java:module/SingletonBeanA");
            if (b.getBar() == null) {
                throw new Exception("Bar is null");
            }

        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

}
