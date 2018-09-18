/*
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

package com.sun.enterprise.deployment;

import org.junit.Test;

import static junit.framework.Assert.assertNull;

import static junit.framework.Assert.assertSame;
import static org.easymock.EasyMock.*;

import org.easymock.EasyMockSupport;

import javax.enterprise.inject.spi.Interceptor;

public class EjbInterceptorTest {

  @Test
  public void testGetSetInterceptor() throws Exception {
    EjbInterceptor ejbInterceptor = new EjbInterceptor();
    assertNull( ejbInterceptor.getInterceptor() );

    EasyMockSupport mockSupport = new EasyMockSupport();
    Interceptor interceptor = mockSupport.createMock( Interceptor.class );

    ejbInterceptor.setInterceptor( interceptor );
    assertSame( interceptor, ejbInterceptor.getInterceptor() );
  }

}
