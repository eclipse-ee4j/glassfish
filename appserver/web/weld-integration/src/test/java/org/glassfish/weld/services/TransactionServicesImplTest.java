/*
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

package org.glassfish.weld.services;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

import static jakarta.transaction.Status.*;


import org.easymock.EasyMockSupport;

public class TransactionServicesImplTest {
  @Test
  public void testisTransactionActive() throws Exception {
    EasyMockSupport mockSupport = new EasyMockSupport();
    ServiceLocator serviceLocator = mockSupport.createMock( ServiceLocator.class );
    JavaEETransactionManager transactionManager = mockSupport.createMock( JavaEETransactionManager.class );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_ACTIVE );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_MARKED_ROLLBACK );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_PREPARED );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_UNKNOWN );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_PREPARING );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_COMMITTING );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_ROLLING_BACK );

    doTestIsNotTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_COMMITTED );

    doTestIsNotTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_ROLLEDBACK );

    doTestIsNotTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_NO_TRANSACTION );
  }

  private void doTestIsTransactionActive( EasyMockSupport mockSupport,
                                          ServiceLocator serviceLocator,
                                          JavaEETransactionManager transactionManager,
                                          int expectedStatus ) throws Exception {

    expect( serviceLocator.getService(JavaEETransactionManager.class) ).andReturn( transactionManager );
    expect( transactionManager.getStatus() ).andReturn( expectedStatus );
    mockSupport.replayAll();

    TransactionServicesImpl transactionServices = new TransactionServicesImpl( serviceLocator );
    assertTrue( transactionServices.isTransactionActive() );

    mockSupport.verifyAll();
    mockSupport.resetAll();
  }

  private void doTestIsNotTransactionActive( EasyMockSupport mockSupport,
                                          ServiceLocator serviceLocator,
                                          JavaEETransactionManager transactionManager,
                                          int expectedStatus ) throws Exception {

    expect( serviceLocator.getService(JavaEETransactionManager.class) ).andReturn( transactionManager );
    expect( transactionManager.getStatus() ).andReturn( expectedStatus );
    mockSupport.replayAll();

    TransactionServicesImpl transactionServices = new TransactionServicesImpl( serviceLocator );
    assertFalse( transactionServices.isTransactionActive() );

    mockSupport.verifyAll();
    mockSupport.resetAll();
  }
}
