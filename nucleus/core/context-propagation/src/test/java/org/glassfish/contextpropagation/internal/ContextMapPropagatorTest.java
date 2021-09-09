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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

import org.glassfish.contextpropagation.ContextMap;
import org.glassfish.contextpropagation.Location;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
import org.glassfish.contextpropagation.adaptors.TestableThread;
import org.glassfish.contextpropagation.internal.Entry.ContextType;
import org.glassfish.contextpropagation.internal.Utils.AccessControlledMapFinder;
import org.glassfish.contextpropagation.spi.ContextMapPropagator;
import org.glassfish.contextpropagation.wireadapters.WireAdapter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit5.JMockitExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Behavioral tests to check the ContextMapPropagator is properly driving the WireAdapter
 */
@ExtendWith(JMockitExtension.class)
public class ContextMapPropagatorTest {

    @Mocked
    private WireAdapter adapter;
    private ContextMapPropagator propagator;
    private ContextMap contextMap;
    private SimpleMap simpleMap;
    private Entry defaultEntry;
    private Entry rmiEntry;
    private Entry soapEntry;
    private final OutputStream out = new ByteArrayOutputStream();

    private static final InputStream NOOP_INPUT_STREAM = new InputStream() {

        @Override
        public int read() throws IOException {
            return 0;
        }
    };

    @BeforeEach
    public void setup() throws Exception {
        BootstrapUtils.bootstrap(adapter);
        propagator = Utils.getScopeAwarePropagator();
        propagator.useWireAdapter(adapter);
        contextMap = Utils.getScopeAwareContextMap();
        EnumSet<PropagationMode> oneWayDefault = PropagationMode.defaultSet();
        oneWayDefault.add(PropagationMode.ONEWAY);
        // Only sent in the request
        contextMap.put("default", "default value", oneWayDefault);
        contextMap.put("rmi", "rmi value", EnumSet.of(PropagationMode.RMI));
        contextMap.put("soap", "soap value", EnumSet.of(PropagationMode.SOAP));
        AccessControlledMapFinder acmFinder = new AccessControlledMapFinder();
        simpleMap = acmFinder.getMapAndCreateIfNeeded().simpleMap;
        defaultEntry = simpleMap.getEntry("default");
        rmiEntry = simpleMap.getEntry("rmi");
        soapEntry = simpleMap.getEntry("soap");
    }

    @AfterAll
    public static void reset() {
        BootstrapUtils.reset();
    }

    @Test
    public void testSendRequest() throws Exception {
        new Expectations() {

            {
                adapter.prepareToWriteTo(out);
                adapter.write("default", defaultEntry);
                adapter.write("rmi", rmiEntry);
                adapter.flush();
            }
        };
        propagator.sendRequest(out, PropagationMode.RMI);
    }


    @Test
    @Disabled("Causes ConcurrentModificationException at SimpleMap.findNext:162")
    public void testSendRequestWithLocation() throws Exception {
        final Entry locationEntry = createLocationEntry();
        new Expectations() {

            {
                adapter.prepareToWriteTo(out);
                adapter.write(Location.KEY, locationEntry);
                // the order to location calls may have changed since we no longer write it first.
                adapter.write("default", defaultEntry);
                adapter.write(Location.KEY + ".locationId", (Entry) any);
                adapter.write(Location.KEY + ".origin", (Entry) any);
                adapter.write("rmi", rmiEntry);
                adapter.flush();
            }
        };
        propagator.sendRequest(out, PropagationMode.RMI);
    }


    @Test
    public void testSendResponse() throws IOException {
        new Expectations() {

            {
                adapter.prepareToWriteTo(out);
                // default is not expected because it has propagation mode ONEWAY
                adapter.write("rmi", rmiEntry);
                adapter.flush();
            }
        };
        propagator.sendResponse(out, PropagationMode.RMI);
    }


    @Test
    public void testSendResponseWithLocation() throws IOException {
        createLocationEntry();
        new Expectations() {

            {
                adapter.prepareToWriteTo(out);
                // Location is not expected for responses
                // default is not expected because it has propagation mode ONEWAY
                adapter.write("rmi", rmiEntry);
                adapter.flush();
            }
        };
        propagator.sendResponse(out, PropagationMode.RMI);
    }


    @Test
    @Disabled("Incompatible Jmockit 1.49 and JaCoCo 0.8.7, also readEntry after 'soap' still fails even without jacoco")
    public void testReceiveRequestBehavior() throws Exception {
        new Expectations() {

            {
                adapter.prepareToReadFrom(NOOP_INPUT_STREAM);
                adapter.readKey();
                result = "default";
                adapter.readEntry();
                result = defaultEntry;
                adapter.readKey();
                result = "rmi";
                adapter.readEntry();
                result = rmiEntry;
                adapter.readKey();
                result = "soap";
                adapter.readEntry();
                result = soapEntry;
                adapter.readKey();
                result = null;
            }
        };
        propagator.receiveRequest(NOOP_INPUT_STREAM);
    }


    @Test
    @Disabled("Incompatible Jmockit 1.49 and JaCoCo 0.8.7, also readEntry after 'soap' still fails even without jacoco")
    public void testReceiveResponse() throws Exception {
        new Expectations() {

            {
                adapter.prepareToReadFrom(NOOP_INPUT_STREAM);
                adapter.readKey();
                result = "default";

                adapter.readEntry();
                result = defaultEntry;

                adapter.readKey();
                result = "rmi";

                adapter.readEntry();
                result = rmiEntry;

                adapter.readKey();
                result = "soap";

                adapter.readEntry();
                result = soapEntry;

                adapter.readKey();
                result = null;
            }
        };
        propagator.receiveResponse(NOOP_INPUT_STREAM, PropagationMode.SOAP);
    }


    @Test
    public void testRestoreThreadContexts() throws Exception {
        contextMap.put("local", "local context", EnumSet.of(PropagationMode.LOCAL));
        final AccessControlledMap acm = contextMap.getAccessControlledMap();
        new TestableThread() {

            @Override
            public void runTest() throws Exception {
                propagator.restoreThreadContexts(acm);
                ContextMap newCM = Utils.getScopeAwareContextMap();
                assertAll(
                    () -> assertNotSame(contextMap, newCM),
                    () -> assertNull(newCM.get("local"), "This one should not propagate since it is LOCAL"),
                    () -> assertNotNull(newCM.get("default")),
                    () -> assertNull(newCM.get("soap")),
                    () -> assertNull(newCM.get("rmi"))
                );
            }
        }.startJoinAndCheckForFailures();
    }


    @Test
    public void testUseWireAdapter() throws IOException {
        new Expectations() {

            {
                adapter.prepareToWriteTo(withInstanceOf(OutputStream.class));
                times = 1;
            }
        };
        propagator.sendRequest(out, PropagationMode.RMI);
    }


    /**
     * Create the entry and put it into the simpleMap ({@link SimpleMap})
     */
    private Entry createLocationEntry() {
        final Entry locationEntry = new Entry(new Location(new ViewImpl(Location.KEY)) {
        }, Location.PROP_MODES, ContextType.VIEW_CAPABLE).init(true, false);
        simpleMap.put(Location.KEY, locationEntry);
        return locationEntry;
    }
}
