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

import org.junit.jupiter.api.extension.ExtendWith;

import mockit.integration.junit5.JMockitExtension;

//import mockit.Deencapsulation;
/*import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;*/



/**
 * Behavioral tests to check the ContextMapPropagator is properly driving the WireAdapter
 */
@ExtendWith(JMockitExtension.class)
public class ContextMapPropagatorTest {
//  ContextMapPropagator propagator;
//  ContextMap cm;
//  SimpleMap sm;
//  //@Mocked(realClassName="org.glassfish.contextpropagation.wireadapters.glassfish.DefaultWireAdapter")
//  final WireAdapter adapter = new DefaultWireAdapter();
//  Entry defaultEntry, rmiEntry, soapEntry;
//  final OutputStream out = new ByteArrayOutputStream();
//
//  @Before
//  public void setup() throws InsufficientCredentialException {
//    BootstrapUtils.bootstrap(adapter);
//    propagator = Utils.getScopeAwarePropagator();
//    cm = Utils.getScopeAwareContextMap();
//    EnumSet<PropagationMode> oneWayDefault = PropagationMode.defaultSet();
//    oneWayDefault.add(PropagationMode.ONEWAY);
//    cm.put("default", "default value", oneWayDefault); // Only sent in the request
//    cm.put("rmi", "rmi value", EnumSet.of(PropagationMode.RMI));
//    cm.put("soap", "soap value", EnumSet.of(PropagationMode.SOAP));
//    Utils.AccessControlledMapFinder acmFinder = Deencapsulation.getField(Utils.class, "mapFinder");
//    sm = acmFinder.getMapIfItExists().simpleMap;
//    defaultEntry = sm.getEntry("default");
//    rmiEntry = sm.getEntry("rmi");
//    soapEntry = sm.getEntry("soap");
//  }
//
//  @Test
//  public void testSendRequest() throws IOException, InsufficientCredentialException {
////    new Expectations() {
////      {
////        adapter.prepareToWriteTo(out);
////        adapter.write("default", defaultEntry);
////        adapter.write("rmi", rmiEntry);
////        adapter.flush();
////      }
////    };
//    propagator.sendRequest(out, PropagationMode.RMI);
//  }
//
//  @Ignore("jmockit fails without providing a reason")@Test // TODO re-evaluate this test
//  public void testSendRequestWithLocation() throws IOException, InsufficientCredentialException {
//    final Entry locationEntry = setupLocation();
////    new Expectations() {
////      {
////        adapter.prepareToWriteTo(out);
////        adapter.write(Location.KEY, locationEntry); // the order to location calls may have changed since we no longer write it first.
////        adapter.write("default", defaultEntry);
////        //adapter.write(Location.KEY + ".locationId", (Entry) any);
////        //adapter.write(Location.KEY + ".origin", (Entry) any);
////        adapter.write("rmi", rmiEntry);
////        adapter.flush();
////      }
////    };
//    propagator.sendRequest(out, PropagationMode.RMI);
//  }
//
//  protected Entry setupLocation() {
//    final Entry locationEntry = new Entry(new Location(new ViewImpl(Location.KEY)) {},
//        Location.PROP_MODES, ContextType.VIEW_CAPABLE).init(true, false);
//    sm.put(Location.KEY, locationEntry);
//    return locationEntry;
//  }
//
//  @Test
//  public void testSendResponse() throws IOException {
////    new Expectations() {
////      {
////        adapter.prepareToWriteTo(out);
////        // default is not expected because it has propagation mode ONEWAY
////        adapter.write("rmi", rmiEntry);
////        adapter.flush();
////      }
////    };
//    propagator.sendResponse(out, PropagationMode.RMI);
//  }
//
//  @Test
//  public void testSendResponseWithLocation() throws IOException {
//    setupLocation();
////    new Expectations() {
////      {
////        adapter.prepareToWriteTo(out);
////        // Location is not expected for responses
////        // default is not expected because it has propagation mode ONEWAY
////        adapter.write("rmi", rmiEntry);
////        adapter.flush();
////      }
////    };
//    propagator.sendResponse(out, PropagationMode.RMI);
//  }
//
//  final static InputStream NOOPInputStream = new InputStream() {
//    @Override public int read() throws IOException {
//      return 0;
//    }
//  };
//
//  @Test
//  public void testReceiveRequestBehavior() throws IOException, ClassNotFoundException {
//    checkReceiveBehavior("receiveRequest", NOOPInputStream);
//  }
//
//  protected void checkReceiveBehavior(String methodName, Object... args) throws IOException,
//  ClassNotFoundException {
//
////    new Expectations() {
////      {
////        adapter.prepareToReadFrom(NOOPInputStream);
////        adapter.readKey(); result = "default";
////        adapter.readEntry(); result = defaultEntry;
////        adapter.readKey(); result = "rmi";
////        adapter.readEntry(); result = rmiEntry;
////        adapter.readKey(); result = "soap";
////        adapter.readEntry(); result = soapEntry;
////        adapter.readKey(); result = null;
////      }
////    };
//    Deencapsulation.invoke(propagator, methodName, args);
//  }
//
//  @Test
//  public void testReceiveResponse() throws IOException, ClassNotFoundException {
//    checkReceiveBehavior("receiveResponse", NOOPInputStream, PropagationMode.SOAP);
//  }
//
//  @Test
//  public void testRestoreThreadContexts() throws InsufficientCredentialException, InterruptedException {
//    cm.put("local", "local context", EnumSet.of(PropagationMode.LOCAL)); // This one should not propagate since it is LOCAL
//    final AccessControlledMap acm = cm.getAccessControlledMap();
//    new TestableThread() {
//      @Override
//      public void runTest() throws Exception {
//        propagator.restoreThreadContexts(acm);
//        ContextMap newCM = Utils.getScopeAwareContextMap();
//        assertNotSame(cm, newCM);
//        assertNull(newCM.get("local"));
//        assertNotNull(newCM.get("default"));
//        assertNull(newCM.get("soap")); // Does not have the PropagationMode.THREAD
//        assertNull(newCM.get("rmi")); // Does not have the PropagationMode.THREAD
//      }
//    }.startJoinAndCheckForFailures();
//  }
//
//  public void testUseWireAdapter(final WireAdapter wa) throws IOException {
//    assertTrue(wa != adapter);
//    propagator.useWireAdapter(wa);
////    new NonStrictExpectations() {{
////      wa.prepareToWriteTo((OutputStream) withNotNull()); times = 1;
////    }};
//    propagator.sendRequest(out, PropagationMode.RMI);
//  }
//
//  @Test public void clearPropagatedEntries() throws InsufficientCredentialException {
//    assertEquals(defaultEntry.value, cm.get("default"));
//    assertEquals(rmiEntry.value, cm.get("rmi"));
//    assertEquals(soapEntry.value, cm.get("soap"));
//    Deencapsulation.invoke(propagator, "clearPropagatedEntries", PropagationMode.ONEWAY, sm);
//    assertNull(cm.get("default")); // The only ONEWAY item
//    assertNotNull(cm.get("rmi"));
//    assertNotNull(cm.get("soap"));
//  }

}
