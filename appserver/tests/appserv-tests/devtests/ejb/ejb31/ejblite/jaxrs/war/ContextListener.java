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

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.annotation.PostConstruct;
import javax.naming.InitialContext;

import java.lang.reflect.Method;

@WebListener
public class ContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {

        System.out.println("In ContextListener::contextInitialized");

        try {
            Object jaxrsEjbGlue = new InitialContext().lookup("java:org.glassfish.ejb.container.interceptor_binding_spi");
            System.out.println("jaxrsEjbGlue = " + jaxrsEjbGlue);
            Method m = jaxrsEjbGlue.getClass().getMethod("registerInterceptor", java.lang.Object.class);
            System.out.println("register interceptor method = " + m);

            m.invoke(jaxrsEjbGlue, new com.sun.jersey.JerseyInterceptor());

            // Test InjectionManager managed bean functionality
            Object injectionMgr = new InitialContext().lookup("com.sun.enterprise.container.common.spi.util.InjectionManager");
            Method createManagedMethod = injectionMgr.getClass().getMethod("createManagedObject", java.lang.Class.class);
            System.out.println("create managed object method = " + createManagedMethod);

            FooNonManagedBean nonF = (FooNonManagedBean) createManagedMethod.invoke(injectionMgr, FooNonManagedBean.class);
            System.out.println("FooNonManagedBean = " + nonF);
            nonF.hello();
            destroyManagedMethod.invoke(injectionMgr, nonF);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @PostConstruct
    public void pc() {
        System.out.println("In ContextListener::postConstruct");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("In ContextListener::contextDestroyed");
    }

}
