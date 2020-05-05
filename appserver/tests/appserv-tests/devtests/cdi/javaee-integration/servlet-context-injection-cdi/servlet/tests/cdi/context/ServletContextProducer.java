/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package tests.cdi.context;

//JJS: With cdi 1.1 the ServletContext is produced by Weld (2.0) and so producing one creates an ambiguous dependency.
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.enterprise.event.Observes;
//import jakarta.enterprise.inject.Produces;
//import jakarta.enterprise.inject.spi.BeanManager;
//import jakarta.inject.Inject;
//import jakarta.servlet.ServletContext;
//import jakarta.servlet.ServletContextEvent;
//
//import tests.cdi.artifacts.Destroyed;
//import tests.cdi.artifacts.Initialized;
//
//@ApplicationScoped
public class ServletContextProducer {
//    private ServletContext servletContext;
//
////    @Inject
////    private BeanManager beanManager;
//
//    protected void contextInitialized(
//            @Observes @Initialized ServletContextEvent e) {
//        System.out.println("Servlet context initialized with event -" + e);
//        servletContext = e.getServletContext();
////        servletContext.setAttribute(BeanManager.class.getName(), beanManager);
//    }
//
//    protected void contextDestroyed(@Observes @Destroyed ServletContextEvent e) {
//        System.out.println("Servlet context destroyed with event #0" + e);
//        servletContext = null;
//    }
//
//    @Produces
//    @ApplicationScoped
//    public ServletContext getServletContext() {
//        return servletContext;
//    }
}
