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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.glassfish.contextpropagation.adaptors.MockLoggerAdapter;
import org.glassfish.contextpropagation.adaptors.TestableThread;
import org.glassfish.contextpropagation.internal.Utils;
import org.glassfish.contextpropagation.weblogic.workarea.spi.WorkContextMapInterceptor;
import org.glassfish.contextpropagation.weblogic.workarea.utils.WorkContextInputAdapter;
import org.glassfish.contextpropagation.weblogic.workarea.utils.WorkContextOutputAdapter;
import org.glassfish.contextpropagation.wireadapters.wls.MySerializable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropagationTest {

    private static WorkContextMap wcMap;

    @BeforeAll
    public static void setup() throws Exception {
        wcMap = WorkContextHelper.getWorkContextHelper().getWorkContextMap();
        wcMap.put("long", PrimitiveContextFactory.create(1L));
        wcMap.put("string", PrimitiveContextFactory.create("string"));
        wcMap.put("ascii", PrimitiveContextFactory.createASCII("ascii"));
        wcMap.put("serializable", PrimitiveContextFactory.createMutable(new MySerializable()));
        wcMap.put("workcontext", new MyWorkContext());
    }


    @Test
    public void testRequestPropagation() throws IOException, InterruptedException {
        final byte[] bytes = serialize();
        // MockLoggerAdaper.debug(Utils.toString(bytes));
        new TestableThread() {

            @Override
            public void runTest() throws Exception {
                final WorkContextMap map = WorkContextHelper.getWorkContextHelper().getWorkContextMap();
                assertTrue(map.isEmpty());
                deserialize(bytes);
                Set<?> expectedSet = map2Set(wcMap);
                Set<?> resultSet = map2Set(map);
                assertEquals(expectedSet, resultSet);
            }


            @SuppressWarnings("serial")
            private HashSet<?> map2Set(final WorkContextMap map) {
                return new HashSet<>() {

                    {
                        Iterator<?> it = map.iterator();
                        while (it.hasNext()) {
                            add(it.next());
                        }
                    }
                };
            }
        }.startJoinAndCheckForFailures();
        MockLoggerAdapter.debug(Utils.toString(bytes));
    }


    public static byte[] serialize() throws IOException {
        WorkContextMapInterceptor wcInterceptor = WorkContextHelper.getWorkContextHelper().getLocalInterceptor();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(baos);
        WorkContextOutput wco = new WorkContextOutputAdapter(oo);
        wcInterceptor.sendRequest(wco, PropagationMode.RMI);
        oo.flush();
        return baos.toByteArray();
    }


    private static void deserialize(byte[] bytes) {
        try {
            WorkContextMapInterceptor wcInterceptor = WorkContextHelper.getWorkContextHelper().getInterceptor();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInput oi = new ObjectInputStream(bais);
            WorkContextInput wci = new WorkContextInputAdapter(oi);
            wcInterceptor.receiveRequest(wci);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}
