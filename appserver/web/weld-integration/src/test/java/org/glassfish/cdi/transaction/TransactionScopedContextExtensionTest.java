/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;

import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

/**
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class TransactionScopedContextExtensionTest {
    @Test
    public void testafterBeanDiscovery() {
        EasyMockSupport mockSupport = new EasyMockSupport();
        AfterBeanDiscovery event = mockSupport.createMock(AfterBeanDiscovery.class);
        BeanManager beanManager = mockSupport.createMock(BeanManager.class);
        AnnotatedType<TransactionScopedCDIEventHelperImpl> annotatedType
            = (AnnotatedType<TransactionScopedCDIEventHelperImpl>) mockSupport.createMock(AnnotatedType.class);
        InjectionTargetFactory injectionTargetFactory = mockSupport.createMock(InjectionTargetFactory.class);
        InjectionTarget injectionTarget = mockSupport.createMock(InjectionTarget.class);

        event.addContext(isA(TransactionScopedContextImpl.class));
        expect(beanManager.createAnnotatedType(TransactionScopedCDIEventHelperImpl.class)).andReturn(annotatedType);
        expect(beanManager.getInjectionTargetFactory(annotatedType)).andReturn(injectionTargetFactory);
        expect(injectionTargetFactory.createInjectionTarget(anyObject(Bean.class))).andReturn(injectionTarget);
        event.addBean(anyObject(Bean.class));
        mockSupport.replayAll();

        TransactionScopedContextExtension transactionScopedContextExtension = new TransactionScopedContextExtension();
        transactionScopedContextExtension.afterBeanDiscovery(event, beanManager);

        mockSupport.verifyAll();
        mockSupport.resetAll();
    }
}
