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

package org.glassfish.cdi.transaction;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

/**
 * The CDI Portable Extension for @Transactional.
 */
public class TransactionalExtension implements Extension {

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent, final BeanManager beanManager) {
        // Register the interceptors so no beans.xml is needed
        AnnotatedType annotatedType = beanManager.createAnnotatedType(TransactionalInterceptorMandatory.class);
        beforeBeanDiscoveryEvent.addAnnotatedType(annotatedType);

        annotatedType = beanManager.createAnnotatedType(TransactionalInterceptorNever.class);
        beforeBeanDiscoveryEvent.addAnnotatedType(annotatedType);

        annotatedType = beanManager.createAnnotatedType(TransactionalInterceptorNotSupported.class);
        beforeBeanDiscoveryEvent.addAnnotatedType(annotatedType);

        annotatedType = beanManager.createAnnotatedType(TransactionalInterceptorRequired.class);
        beforeBeanDiscoveryEvent.addAnnotatedType(annotatedType);

        annotatedType = beanManager.createAnnotatedType(TransactionalInterceptorRequiresNew.class);
        beforeBeanDiscoveryEvent.addAnnotatedType(annotatedType);

        annotatedType = beanManager.createAnnotatedType(TransactionalInterceptorSupports.class);
        beforeBeanDiscoveryEvent.addAnnotatedType(annotatedType);
    }

}
