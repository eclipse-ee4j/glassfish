/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import jakarta.enterprise.inject.spi.Interceptor;

import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class EjbInterceptorTest {

    @Test
    void testGetSetInterceptor() throws Exception {
        EjbInterceptor ejbInterceptor = new EjbInterceptor();
        assertNull(ejbInterceptor.getInterceptor());

        EasyMockSupport mockSupport = new EasyMockSupport();
        Interceptor<?> interceptorMock = mockSupport.createMock(Interceptor.class);

        ejbInterceptor.setInterceptor(interceptorMock);
        assertSame(interceptorMock, ejbInterceptor.getInterceptor());
    }

}
