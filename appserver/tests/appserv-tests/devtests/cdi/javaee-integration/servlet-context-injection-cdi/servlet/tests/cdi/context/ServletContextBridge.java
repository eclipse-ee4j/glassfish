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

import java.lang.annotation.Annotation;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import tests.cdi.artifacts.Destroyed;
import tests.cdi.artifacts.Initialized;

@WebListener
public class ServletContextBridge implements ServletContextListener {
    @Inject
    private BeanManager beanManager;

    public ServletContextBridge() {
    }

    /**
     * Servlet context initialized / destroyed events
     */

    public void contextDestroyed(final ServletContextEvent e) {
        fireEvent(e, DESTROYED);
    }

    public void contextInitialized(final ServletContextEvent e) {
        fireEvent(e, INITIALIZED);
    }

    private void fireEvent(final Object payload,
            final Annotation... qualifiers) {
        System.out.println("Firing event #0 with qualifiers #1" + payload
                + qualifiers);
        beanManager.fireEvent(payload, qualifiers);
    }

    private static final AnnotationLiteral<Destroyed> DESTROYED = new AnnotationLiteral<Destroyed>() {
        private static final long serialVersionUID = -1610281796509557441L;
    };

    private static final AnnotationLiteral<Initialized> INITIALIZED = new AnnotationLiteral<Initialized>() {
        private static final long serialVersionUID = -1610281796509557441L;
    };

}
