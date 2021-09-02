/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.contextpropagation;


public class ContextViewTest {


//  @BeforeClass
//  public static void setUpBeforeClass() throws Exception {
//    BootstrapUtils.bootstrap(new DefaultWireAdapter());
//  }
//
//  @Before
//  public void setUp() throws Exception {
//  }
//
//  private static class MyViewBasedContext implements ViewCapable {
//    private View view;
//    EnumSet<PropagationMode> propModes = PropagationMode.defaultSet();
//
//    private MyViewBasedContext(View aView) {
//      view = aView;
//    }
//
//    public void setFoo(String foo) {
//      view.put("foo", foo, propModes);
//    }
//
//    public String getFoo() {
//      return view.get("foo");
//    };
//
//    public void setLongValue(long value) {
//      view.put("long value", value, propModes);
//    }
//
//    public long getLongValue() {
//      return (Long) view.get("long value");
//    }
//
//  }
//
//  @Test
//  public void testContextViewExample() throws InsufficientCredentialException {
//    ContextViewFactory factory = new ContextViewFactory() {
//
//      @Override
//      public ViewCapable createInstance(final View view) {
//        return new MyViewBasedContext(view) ;
//      }
//
//      @Override
//      public EnumSet<PropagationMode> getPropagationModes() {
//        return PropagationMode.defaultSet();
//      }
//    };
//
//    // Define prefix and register factory -- done only once during server startup phase
//    String prefix = "my.prefix";
//    ContextMapHelper.registerContextFactoryForPrefixNamed(prefix, factory);
//
//    // Get a ContextMap
//    ContextMap wcMap = ContextMapHelper.getScopeAwareContextMap();
//
//    // Since this is a new ContextMap, get will create the vbContext with the registered factory
//    MyViewBasedContext mvbContext = wcMap.createViewCapable(prefix);
//    mvbContext.setFoo("foo value");
//    assertEquals("foo value", mvbContext.getFoo());
//    mvbContext.setLongValue(1);
//    assertEquals(1L, mvbContext.getLongValue());
//  }

}
