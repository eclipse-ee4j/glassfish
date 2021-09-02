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

package org.glassfish.contextpropagation.internal;

//import mockit.Deencapsulation;



public class UtilsTest {
//  @BeforeClass
//  public static void setupClass() {
//    BootstrapUtils.bootstrap(new DefaultWireAdapter());
//  }
//
//  @Test
//  public void testGetScopeAwarePropagator() {
//    assertNotNull(Utils.getScopeAwarePropagator());
//  }
//
//  @Test
//  public void testGetScopeAwareContextMap() {
//    assertNotNull(Utils.getScopeAwarePropagator());
//  }
//
//  @Test
//  public void testRegisterContextFactoryForPrefixNamed() {
//    Utils.registerContextFactoryForPrefixNamed("prefix",
//        new ContextViewFactory() {
//           @Override
//          public EnumSet<PropagationMode> getPropagationModes() {
//            return PropagationMode.defaultSet();
//          }
//          @Override
//          public ViewCapable createInstance(View view) {
//            return new ViewCapable() {};
//          }
//        });
//    assertNotNull(Utils.getFactory("prefix"));
//  }
//
//  private static final Object CONTEXT_VIEW_FACTORY = new ContextViewFactory() {
//    @Override
//    public ViewCapable createInstance(View view) {
//       return null;
//    }
//    @Override
//    public EnumSet<PropagationMode> getPropagationModes() {
//      return null;
//    }
//  };
//
//  private static MessageID msgID = MessageID.WRITING_KEY; // We need a dummy MessageID
//  @Test(expected=IllegalArgumentException.class)
//  public void testValidateFactoryRegistrationArgsNullKey() {
//    Utils.validateFactoryRegistrationArgs(null, msgID, "context class name",
//        CONTEXT_VIEW_FACTORY, null);
//  }
//
//  @Test(expected=IllegalArgumentException.class)
//  public void testValidateFactoryRegistrationArgsNullContextClassName() {
//    Utils.validateFactoryRegistrationArgs("key", msgID, null,
//        CONTEXT_VIEW_FACTORY, null);
//  }
//
//  @Test(expected=IllegalArgumentException.class)
//  public void testValidateFactoryRegistrationArgsNullFactory() {
//    Utils.validateFactoryRegistrationArgs("key", msgID, "context class name",
//        null, null);
//  }
//
//  @Test
//  public void testValidateFactoryRegistration() {
//    Map<String, ?> map = Collections.emptyMap();
//    Utils.validateFactoryRegistrationArgs("key", msgID, "context class name",
//        CONTEXT_VIEW_FACTORY, map);
//  }
//  @Test(expected=IllegalArgumentException.class)
//  public void testValidateFactoryRegistrationNullKey() {
//    Map<String, ?> map = Collections.emptyMap();
//    Utils.validateFactoryRegistrationArgs(null, msgID, "context class name",
//        CONTEXT_VIEW_FACTORY, map);
//  }
//  @Test(expected=IllegalArgumentException.class)
//  public void testValidateFactoryRegistrationNullClassName() {
//    Map<String, ?> map = Collections.emptyMap();
//    Utils.validateFactoryRegistrationArgs("key", msgID, null,
//        CONTEXT_VIEW_FACTORY, map);
//  }
//  @Test(expected=IllegalArgumentException.class)
//  public void testValidateFactoryRegistrationNullFactory() {
//    Map<String, ?> map = Collections.emptyMap();
//    Utils.validateFactoryRegistrationArgs("key", msgID, "context class name",
//        null, map);
//  }
//  @Test(expected=IllegalArgumentException.class)
//  public void testValidateFactoryRegistrationNullMessageID() {
//    Map<String, ?> map = Collections.emptyMap();
//    Utils.validateFactoryRegistrationArgs("key", null, "context class name",
//        CONTEXT_VIEW_FACTORY, map);
//  }
//  @Test
//  public void testValidateFactoryRegistrationAlreadyRegistered() {
//    RecordingLoggerAdapter logger = new RecordingLoggerAdapter();
//    Deencapsulation.setField(ContextBootstrap.class, "loggerAdapter", logger);
//    Map<String, Object> map = new HashMap<String, Object>();
//    Utils.validateFactoryRegistrationArgs("key", msgID, "context class name",
//        CONTEXT_VIEW_FACTORY, map);
//    logger.verify(null, null, null, (Object[]) null);
//    map.put("context class name", "something");
//    Utils.validateFactoryRegistrationArgs("key", msgID, "context class name",
//        CONTEXT_VIEW_FACTORY, map);
//    logger.verify(Level.WARN, null, msgID, "context class name",
//        "something", CONTEXT_VIEW_FACTORY);
//  }

}
