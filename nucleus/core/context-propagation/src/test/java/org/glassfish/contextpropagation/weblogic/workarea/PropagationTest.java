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

package org.glassfish.contextpropagation.weblogic.workarea;

public class PropagationTest {
//  public static WorkContextMap wcMap;
//
//  @BeforeClass
//  public static void setup() throws PropertyReadOnlyException, IOException {
//    wcMap = WorkContextHelper.getWorkContextHelper().getWorkContextMap();
//    wcMap.put("long", PrimitiveContextFactory.create(1L));
//    wcMap.put("string", PrimitiveContextFactory.create("string"));
//    wcMap.put("ascii", PrimitiveContextFactory.createASCII("ascii"));
//    wcMap.put("serializable", PrimitiveContextFactory.createMutable(new MySerializable()));
//    wcMap.put("workcontext", new MyWorkContext());
//  }
//
//  @Test
//  public void testRequestPropagation() throws IOException, InterruptedException {
//    final byte[] bytes = serialize();
//    //MockLoggerAdaper.debug(Utils.toString(bytes));
//    new TestableThread() {
//      @Override public void runTest() throws Exception {
//        final WorkContextMap map = WorkContextHelper.getWorkContextHelper().getWorkContextMap();
//        assertTrue(map.isEmpty());
//        deserialize(bytes);
//        Set<?> expectedSet = map2Set(wcMap);
//        Set<?> resultSet = map2Set(map);
//        assertEquals(expectedSet, resultSet);
//      }
//
//      @SuppressWarnings("serial")
//      private HashSet<?> map2Set(final WorkContextMap map) {
//        return new HashSet<Object>() {{
//          Iterator<?> it = map.iterator();
//          while (it.hasNext()) {
//            add(it.next());
//          }
//        }};
//      }
//    }.startJoinAndCheckForFailures();
//    MockLoggerAdapter.debug(Utils.toString(bytes));
//  }
//
//  @Ignore("there seems to be a problem with jmockit")
//  @Test public void fromGlassfish() throws InsufficientCredentialException, IOException, InterruptedException {
//    WorkContextMapInterceptor interceptor = WorkContextHelper.getWorkContextHelper().createInterceptor();
//    //byte[] gBytes = getFirstBytes();
//    byte[] bytes = WLSWireAdapterTest.toWLSBytes();
//    //MockLoggerAdaper.debug(">>>" + Utils.toString(gBytes) + "<<<");
//    //MockLoggerAdaper.debug(">>>" + Utils.toString(bytes) + "<<<");
//    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//    WorkContextInput wci = new WorkContextInputAdapter(new ObjectInputStream(bais));
//    interceptor.receiveRequest(wci);
//    WorkContextMap map = (WorkContextMap) interceptor;
//    @SuppressWarnings("unchecked")
//    Iterator<String> keys = map.keys();
//    while(keys.hasNext()) {
//      String key = keys.next();
//      MockLoggerAdapter.debug(key + ": " + map.get(key));
//    }
//    assertEquals("ascii", ((PrimitiveWorkContext) map.get("ascii")).get());
//    assertEquals("string", ((PrimitiveWorkContext)map.get("string")).get());
//    assertEquals(1L, ((PrimitiveWorkContext)map.get("one")).get());
//    assertEquals(MyWorkContext.class, map.get("workcontext").getClass());
//    SerializableWorkContext swc = (SerializableWorkContext) map.get("serializable");
//    MockLoggerAdapter.debug("Serializable contents: " + swc.get().getClass() + swc.get());
//    Set<?> set = (Set<?>) swc.get();
//    assertEquals(1,  set.size());
//  }
//
//  public static byte[] serialize() throws IOException {
//    WorkContextMapInterceptor wcInterceptor = WorkContextHelper.getWorkContextHelper().getLocalInterceptor();
//    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//    ObjectOutput oo = new ObjectOutputStream(baos);
//    WorkContextOutput wco = new WorkContextOutputAdapter(oo);
//    wcInterceptor.sendRequest(wco, PropagationMode.RMI);
//    oo.flush();
//    return baos.toByteArray();
//  }
//
//  public static void deserialize(byte[] bytes) {
//    try {
//      //WorkContextHelper.getWorkContextHelper().
//      WorkContextMapInterceptor wcInterceptor = WorkContextHelper.getWorkContextHelper().getInterceptor();
//      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//      ObjectInput oi = new ObjectInputStream(bais);
//      WorkContextInput wci = new WorkContextInputAdapter(oi);
//      wcInterceptor.receiveRequest(wci);
//    } catch (IOException ioe) {
//      throw new RuntimeException(ioe);
//    }
//  }


}
