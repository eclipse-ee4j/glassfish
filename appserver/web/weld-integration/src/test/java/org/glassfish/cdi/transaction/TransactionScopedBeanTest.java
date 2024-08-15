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

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import java.util.concurrent.ConcurrentHashMap;

import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class TransactionScopedBeanTest {

    @Test
    public void testAllMethods() {
        LocalBean localBean = new LocalBean();
        EasyMockSupport mockSupport = new EasyMockSupport();
        Contextual<LocalBean> contextual = (Contextual<LocalBean>) mockSupport.createMock(Contextual.class);
        CreationalContext<LocalBean> creationalContext = (CreationalContext<LocalBean>) mockSupport.createMock(CreationalContext.class);
        TransactionScopedContextImpl transactionScopedContext = mockSupport.createMock(TransactionScopedContextImpl.class);
        transactionScopedContext.beansPerTransaction = new ConcurrentHashMap<>();

        // test getContextualInstance
        TransactionScopedBean<LocalBean> transactionScopedBean = getTransactionScopedBean(mockSupport, localBean, contextual,
                creationalContext, transactionScopedContext);
        assertSame(localBean, transactionScopedBean.getContextualInstance());
        // test afterCompletion
        contextual.destroy(localBean, creationalContext);
        mockSupport.replayAll();

        transactionScopedBean.afterCompletion(0);

        mockSupport.verifyAll();
        mockSupport.resetAll();
    }

    private class LocalBean {

    }

    public static <T> TransactionScopedBean<T> getTransactionScopedBean(EasyMockSupport mockSupport, T localBean, Contextual<T> contextual,
            CreationalContext<T> creationalContext, TransactionScopedContextImpl transactionScopedContext) {
        expect(contextual.create(creationalContext)).andReturn(localBean);
        mockSupport.replayAll();

        TransactionScopedBean<T> transactionScopedBean = new TransactionScopedBean<>(contextual, creationalContext,
                transactionScopedContext);
        assertSame(localBean, transactionScopedBean.getContextualInstance());

        mockSupport.verifyAll();
        mockSupport.resetAll();
        return transactionScopedBean;
    }
}
