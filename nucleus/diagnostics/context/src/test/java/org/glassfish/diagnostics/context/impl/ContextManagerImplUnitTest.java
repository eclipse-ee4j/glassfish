/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.diagnostics.context.impl;

import org.glassfish.contextpropagation.ContextMap;
import org.glassfish.contextpropagation.ContextViewFactory;
import org.glassfish.contextpropagation.spi.ContextMapHelper;
import org.glassfish.diagnostics.context.Context;
import org.glassfish.diagnostics.context.ContextManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class ContextManagerImplUnitTest {

 /**
  * Verify that ContextManagerImpl initialization registers a
  * ContextViewFactory with the ContextMapHelper.
  *
  * This test assumes only that initialization takes place by the time
  * the first new ContextManagerImpl has been created.
  */
  @Test
  @Ignore
  public void testViewFactoryRegistration()
  {
    new MockUp<ContextMapHelper>(){
      @Mock
      public void registerContextFactoryForPrefixNamed(
        String prefixName, ContextViewFactory factory)
      {
        Assert.assertEquals(prefixName, ContextManager.WORK_CONTEXT_KEY);
        Assert.assertTrue("org.glassfish.diagnostics.context.impl.ContextManagerImpl$DiagnosticContextViewFactory".equals(factory.getClass().getName()));
      }
    };

    ContextManagerImpl cmi = new ContextManagerImpl();
  }

 /**
  * Verify the expected delegation to ContextMap by
  * ContextManagerImpl on invocation of getContext.
  */
  @Test
  @Ignore
  public void testGetContextUseOfContextMap_new(
    @Mocked final ContextMap mockedContextMap)
  throws Exception
  {
    new Expectations(){

      // We expect ContextManagerImpl to call getScopeAwareContextMap, but
      // we also need that method to return a ContextMap instance so 
      // we tell the mocking framework to return an instance. 
      ContextMapHelper expectationsRefContextMapHelper;
      {
        expectationsRefContextMapHelper.getScopeAwareContextMap(); returns(mockedContextMap,null,null);
      }

      // We expect ContextManagerImpl to then go ahead and use the
      // ContextMap - in particular to call get (from which we deliberately
      // return null) and the createViewCapable (from which we return null
      // which is in practice an exceptional condition (which will result
      // in a WARNING log message) but does fine for this test.
      ContextMap expectationsRefContextMap = mockedContextMap;
      {
        expectationsRefContextMap.get(ContextManager.WORK_CONTEXT_KEY); returns(null,null,null);
        expectationsRefContextMap.createViewCapable(ContextManager.WORK_CONTEXT_KEY); returns(null,null,null);
      }
    };

    ContextManagerImpl cmi = new ContextManagerImpl();
    Context ci = cmi.getContext();
  }

}
