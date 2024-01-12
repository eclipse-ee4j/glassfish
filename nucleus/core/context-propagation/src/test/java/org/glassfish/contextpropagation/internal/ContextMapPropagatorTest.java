/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.contextpropagation.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.glassfish.contextpropagation.ContextMap;
import org.glassfish.contextpropagation.Location;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
import org.glassfish.contextpropagation.adaptors.TestableThread;
import org.glassfish.contextpropagation.internal.Utils.AccessControlledMapFinder;
import org.glassfish.contextpropagation.spi.ContextMapPropagator;
import org.glassfish.contextpropagation.wireadapters.WireAdapter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.glassfish.contextpropagation.Location.KEY;
import static org.glassfish.contextpropagation.Location.PROP_MODES;
import static org.glassfish.contextpropagation.PropagationMode.RMI;
import static org.glassfish.contextpropagation.PropagationMode.SOAP;
import static org.glassfish.contextpropagation.PropagationMode.defaultSetOneway;
import static org.glassfish.contextpropagation.internal.Entry.ContextType.VIEW_CAPABLE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Behavioral tests to check the {@link ContextMapPropagator} is properly driving the {@link WireAdapter}.
 */
@ExtendWith(EasyMockExtension.class)
public class ContextMapPropagatorTest {

    @Mock
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
        public int read() {
            return 0;
        }
    };

    @BeforeEach
    public void setup() throws Exception {
        BootstrapUtils.bootstrap(adapter);
        propagator = Utils.getScopeAwarePropagator();
        propagator.useWireAdapter(adapter);
        contextMap = Utils.getScopeAwareContextMap();
        // Only sent in the request
        contextMap.put("default", "default value", defaultSetOneway());
        contextMap.put("rmi", "rmi value", EnumSet.of(RMI));
        contextMap.put("soap", "soap value", EnumSet.of(SOAP));
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
        adapter.prepareToWriteTo(out);
        expectLastCall().andVoid();

        adapter.write("default", defaultEntry);
        expectLastCall().andVoid();
        adapter.write("rmi", rmiEntry);
        expectLastCall().andVoid();

        adapter.flush();
        expectLastCall().andVoid();

        replay(adapter);

        propagator.sendRequest(out, RMI);

        verify(adapter);
    }


    @Test
    @Disabled("Causes ConcurrentModificationException at SimpleMap.findNext:162")
    public void testSendRequestWithLocation() throws Exception {
        final Entry locationEntry = createLocationEntry();

        adapter.prepareToWriteTo(out);
        expectLastCall().andVoid();

        adapter.write(KEY, locationEntry);
        expectLastCall().andVoid();
        // the order to location calls may have changed since we no longer write it first.
        adapter.write("default", defaultEntry);
        expectLastCall().andVoid();
        adapter.write(eq(KEY + ".locationId"), anyObject(Entry.class));
        expectLastCall().andVoid();
        adapter.write(eq(KEY + ".origin"), anyObject(Entry.class));
        expectLastCall().andVoid();
        adapter.write("rmi", rmiEntry);
        expectLastCall().andVoid();

        adapter.flush();
        expectLastCall().andVoid();

        replay(adapter);

        propagator.sendRequest(out, RMI);

        verify(adapter);
    }


    @Test
    public void testSendResponse() throws IOException {
        adapter.prepareToWriteTo(out);
        expectLastCall().andVoid();

        // "default" is not expected because it has propagation mode ONEWAY
        adapter.write("rmi", rmiEntry);
        expectLastCall().andVoid();

        adapter.flush();
        expectLastCall().andVoid();

        replay(adapter);

        propagator.sendResponse(out, RMI);

        verify(adapter);
    }


    @Test
    public void testSendResponseWithLocation() throws IOException {
        createLocationEntry();

        adapter.prepareToWriteTo(out);
        expectLastCall().andVoid();

        // Location is not expected for responses.
        // "default" is not expected because it has propagation mode ONEWAY
        adapter.write("rmi", rmiEntry);
        expectLastCall().andVoid();

        adapter.flush();
        expectLastCall().andVoid();

        replay(adapter);

        propagator.sendResponse(out, RMI);

        verify(adapter);
    }


    @Test
    public void testReceiveRequestBehavior() throws Exception {
        adapter.prepareToReadFrom(NOOP_INPUT_STREAM);
        expectLastCall().andVoid();

        expect(adapter.readKey()).andReturn("default");
        expect(adapter.readEntry()).andReturn(defaultEntry);
        expect(adapter.readKey()).andReturn("rmi");
        expect(adapter.readEntry()).andReturn(rmiEntry);
        expect(adapter.readKey()).andReturn("soap");
        expect(adapter.readEntry()).andReturn(soapEntry);
        expect(adapter.readKey()).andReturn(null);

        replay(adapter);

        propagator.receiveRequest(NOOP_INPUT_STREAM);

        verify(adapter);
    }


    @Test
    public void testReceiveResponse() throws Exception {
        adapter.prepareToReadFrom(NOOP_INPUT_STREAM);
        expectLastCall().andVoid();

        expect(adapter.readKey()).andReturn("default");
        expect(adapter.readEntry()).andReturn(defaultEntry);
        expect(adapter.readKey()).andReturn("rmi");
        expect(adapter.readEntry()).andReturn(rmiEntry);
        expect(adapter.readKey()).andReturn("soap");
        expect(adapter.readEntry()).andReturn(soapEntry);
        expect(adapter.readKey()).andReturn(null);

        replay(adapter);

        propagator.receiveResponse(NOOP_INPUT_STREAM, SOAP);

        verify(adapter);
    }


    @Test
    public void testRestoreThreadContexts() throws Exception {
        contextMap.put("local", "local context", EnumSet.of(PropagationMode.LOCAL));
        final AccessControlledMap accessControlledMap = contextMap.getAccessControlledMap();
        new TestableThread() {

            @Override
            public void runTest() {
                propagator.restoreThreadContexts(accessControlledMap);
                ContextMap newContextMap = Utils.getScopeAwareContextMap();
                assertAll(
                    () -> assertNotSame(contextMap, newContextMap),
                    () -> assertNull(newContextMap.get("local"), "This one should not propagate since it is LOCAL"),
                    () -> assertNotNull(newContextMap.get("default")),
                    () -> assertNull(newContextMap.get("soap")),
                    () -> assertNull(newContextMap.get("rmi"))
                );
            }
        }.startJoinAndCheckForFailures();
    }


    @Test
    public void testUseWireAdapter() throws IOException {
        adapter.prepareToWriteTo(EasyMock.isA(OutputStream.class));
        expectLastCall().andVoid();

        adapter.write(EasyMock.anyString(), anyObject(Entry.class));
        expectLastCall().andVoid().times(2);

        adapter.flush();
        expectLastCall().andVoid();

        replay(adapter);

        propagator.sendRequest(out, RMI);

        verify(adapter);
    }


    /**
     * Create the entry and put it into the simpleMap ({@link SimpleMap})
     */
    private Entry createLocationEntry() {
        final Entry locationEntry = new Entry(new Location(new ViewImpl(KEY)) { }, PROP_MODES, VIEW_CAPABLE).init(true, false);
        simpleMap.put(KEY, locationEntry);
        return locationEntry;
    }
}
