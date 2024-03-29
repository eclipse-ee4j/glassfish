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

package org.glassfish.contextpropagation.wireadapters.wls;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.EnumSet;

import org.glassfish.contextpropagation.ContextMap;
import org.glassfish.contextpropagation.InsufficientCredentialException;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.SerializableContextFactory;
import org.glassfish.contextpropagation.SerializableContextFactory.WLSContext;
import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
import org.glassfish.contextpropagation.adaptors.MockLoggerAdapter;
import org.glassfish.contextpropagation.internal.AccessControlledMap;
import org.glassfish.contextpropagation.internal.Entry;
import org.glassfish.contextpropagation.internal.Utils;
import org.glassfish.contextpropagation.internal.Utils.PrivilegedWireAdapterAccessor;
import org.glassfish.contextpropagation.spi.ContextMapHelper;
import org.glassfish.contextpropagation.spi.ContextMapPropagator;
import org.glassfish.contextpropagation.weblogic.workarea.PropagationTest;
import org.glassfish.contextpropagation.weblogic.workarea.PropertyReadOnlyException;
import org.glassfish.contextpropagation.wireadapters.AbstractWireAdapter;
import org.glassfish.contextpropagation.wireadapters.Catalog;
import org.glassfish.contextpropagation.wireadapters.WireAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.glassfish.tests.utils.ReflectionUtils.getField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class WLSWireAdapterTest {

    private WLSWireAdapter adapter;

    @BeforeAll
    public static void setupClass() {
        WireAdapter.HELPER.registerContextFactoryForClass(MyWLSContext.class,
            "org.glassfish.contextpropagation.weblogic.workarea.MyContext", new SerializableContextFactory() {

                @Override
                public WLSContext createInstance() {
                    return new MyWLSContext();
                }
            });
    }


    @BeforeEach
    public void before() {
        BootstrapUtils.bootstrap(new WLSWireAdapter());
        adapter = new WLSWireAdapter();
    }


    @AfterEach
    public void after() {
        BootstrapUtils.reset();
    }


    @Test
    public void testFromWLS() throws Exception {
        PropagationTest.setup();
        byte[] bytes = PropagationTest.serialize();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        WireAdapter adapter = new WLSWireAdapter();
        adapter.prepareToReadFrom(bais);
        for (String key = adapter.readKey(); key != null; key = adapter.readKey()) {
            adapter.readEntry();
        }
    }


    @Test
    public void testWithCatalog() throws PropertyReadOnlyException, IOException, InsufficientCredentialException {
        BootstrapUtils.populateMap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ContextMapPropagator propagator = Utils.getScopeAwarePropagator();
        WLSWireAdapter adapter = new WLSWireAdapter();
        propagator.useWireAdapter(adapter);
        propagator.sendRequest(baos, PropagationMode.RMI);
        Catalog catalog = getField(adapter, "catalog", AbstractWireAdapter.class);
        BootstrapUtils.reset();
        adapter = new WLSWireAdapter();
        BootstrapUtils.bootstrap(adapter);
        byte[] bytes = baos.toByteArray();
        MockLoggerAdapter.debug(Utils.toString(bytes));
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        propagator.useWireAdapter(adapter);
        propagator.receiveRequest(bais);
        Catalog newCatalog = getField(adapter, "wlsCatalog");
        assertEquals(catalog, newCatalog);
        // MockLoggerAdapter.debug("start: " + Deencapsulation.getField(newCatalog, "start") + ",
        // end: " + Deencapsulation.getField(newCatalog, "end"));
    }


    @Test
    public void testResilientWithBadSerializableInsertFirst() throws Exception {
        badSerializable(true);
    }


    @Test
    public void testResilientWithBadSerializableInsertLast() throws Exception {
        badSerializable(false);
    }


    private void badSerializable(boolean insertFirst) throws Exception {
        ContextMap wcMap = ContextMapHelper.getScopeAwareContextMap();
        String key = "faulty serializable";
        @SuppressWarnings("serial")
        Serializable faultySerializable = new Serializable() {

            @SuppressWarnings("unused")
            transient String s = "";

            private void writeObject(ObjectOutputStream out) throws IOException {
                out.writeLong(1L);
                out.writeUTF("a string");
            }


            private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
                MockLoggerAdapter.debug("*******");
                in.readFully(new byte[25]); // expected to fail since we should be reading a long
                                            // and produce stack traces
                MockLoggerAdapter.debug(" -- done");
            }
        };
        Method method = wcMap.getClass().getDeclaredMethod("putSerializable", String.class, Serializable.class, EnumSet.class);
        method.setAccessible(true);
        if (insertFirst) {
            method.invoke(wcMap, key, faultySerializable, PropagationMode.defaultSet());
        }
        BootstrapUtils.populateMap();
        if (!insertFirst) {
            method.invoke(wcMap, key, faultySerializable, PropagationMode.defaultSet());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ContextMapPropagator propagator = Utils.getScopeAwarePropagator();
        WLSWireAdapter adapter = new WLSWireAdapter();
        propagator.useWireAdapter(adapter);
        propagator.sendRequest(baos, PropagationMode.RMI);
        Catalog catalog = getField(adapter, "catalog", AbstractWireAdapter.class);
        BootstrapUtils.reset();
        adapter = new WLSWireAdapter();
        BootstrapUtils.bootstrap(adapter);
        byte[] bytes = baos.toByteArray();
        MockLoggerAdapter.debug(Utils.toString(bytes));
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        propagator.useWireAdapter(adapter);
        propagator.receiveRequest(bais);
        Catalog newCatalog = getField(adapter, "wlsCatalog");
        assertNull(wcMap.get(key));
        // Check that the catalog is read since the faulty context is read before it.
        assertEquals(catalog, newCatalog);
    }


    @Test
    public void markReset() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.write("Some data".getBytes());
        oos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        bais.mark(100);
        bais.read();
        bais.reset();
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(bais));
        ois.mark(100);
        ois.read();
        ois.skip(5); // It can skip but cannot reset
        assertEquals(false, ois.markSupported());
        // ObjectOutputStream does not support reset even if we give it a BufferedInputStream
        assertThrows(IOException.class, ois::reset);
    }


    @Test
    public void testResilientWithBadWorkContext() throws IOException, InsufficientCredentialException {
        ContextMap wcMap = ContextMapHelper.getScopeAwareContextMap();
        AccessControlledMap acm = ((PrivilegedWireAdapterAccessor) wcMap).getAccessControlledMap(true);
        String key = "bad work context";
        WLSContext ctx = new WLSContext() {

            @Override
            public void writeContext(ObjectOutput out) throws IOException {
                out.writeUTF("a string");
            }


            @Override
            public void readContext(ObjectInput in) throws IOException {
                in.readLong(); // Expected to fail
            }
        };
        acm.put(key, Entry.createOpaqueEntryInstance(ctx, PropagationMode.defaultSet(), ctx.getClass().getName())
            .init(true, false));
        BootstrapUtils.populateMap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ContextMapPropagator propagator = Utils.getScopeAwarePropagator();
        WLSWireAdapter adapter = new WLSWireAdapter();
        propagator.useWireAdapter(adapter);
        propagator.sendRequest(baos, PropagationMode.RMI);
        Catalog catalog = getField(adapter, "catalog", AbstractWireAdapter.class);
        BootstrapUtils.reset();
        adapter = new WLSWireAdapter();
        BootstrapUtils.bootstrap(adapter);
        byte[] bytes = baos.toByteArray();
        MockLoggerAdapter.debug(Utils.toString(bytes));
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        propagator.useWireAdapter(adapter);
        propagator.receiveRequest(bais);
        Catalog newCatalog = getField(adapter, "wlsCatalog");
        assertEquals(catalog, newCatalog);
        assertNull(wcMap.get(key));
    }


    @Test
    public void convertPropagationMode() {
        // LOCAL, WORK, RMI, TRANSACTION, JMS_QUEUE, JMS_TOPIC, SOAP, MIME_HEADER, ONEWAY
        checkPropModeConversion(EnumSet.of(PropagationMode.LOCAL),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.LOCAL);
        checkPropModeConversion(EnumSet.of(PropagationMode.THREAD),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.WORK);
        checkPropModeConversion(EnumSet.of(PropagationMode.RMI),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.RMI);
        checkPropModeConversion(EnumSet.of(PropagationMode.TRANSACTION),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.TRANSACTION);
        checkPropModeConversion(EnumSet.of(PropagationMode.JMS_QUEUE),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.JMS_QUEUE);
        checkPropModeConversion(EnumSet.of(PropagationMode.JMS_TOPIC),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.JMS_TOPIC);
        checkPropModeConversion(EnumSet.of(PropagationMode.SOAP),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.SOAP);
        checkPropModeConversion(EnumSet.of(PropagationMode.MIME_HEADER),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.MIME_HEADER);
        checkPropModeConversion(EnumSet.of(PropagationMode.ONEWAY),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.ONEWAY);
        /*
         * Glassfish's default includes all of the WLS default plus THREAD (which
         * is equivalent to the WLS propagation mode WORK)
         */
        checkPropModeConversion(PropagationMode.defaultSet(),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.DEFAULT
                + org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.WORK);
        checkPropModeConversion(PropagationMode.defaultSet(),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.GLOBAL
                + org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.WORK);
    }


    private void checkPropModeConversion(EnumSet<PropagationMode> propagationModes, int expectedWLSPropMode) {
        assertEquals(expectedWLSPropMode, WLSWireAdapter.toWlsPropagationMode(propagationModes));
    }


    @Test
    public void toPropagationMode() {
        checkReversePropModeConversion(EnumSet.of(PropagationMode.LOCAL),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.LOCAL);
        checkReversePropModeConversion(EnumSet.of(PropagationMode.THREAD),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.WORK);
        checkReversePropModeConversion(EnumSet.of(PropagationMode.RMI),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.RMI);
        checkReversePropModeConversion(EnumSet.of(PropagationMode.TRANSACTION),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.TRANSACTION);
        checkReversePropModeConversion(EnumSet.of(PropagationMode.JMS_QUEUE),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.JMS_QUEUE);
        checkReversePropModeConversion(EnumSet.of(PropagationMode.JMS_TOPIC),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.JMS_TOPIC);
        checkReversePropModeConversion(EnumSet.of(PropagationMode.SOAP),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.SOAP);
        checkReversePropModeConversion(EnumSet.of(PropagationMode.MIME_HEADER),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.MIME_HEADER);
        checkReversePropModeConversion(EnumSet.of(PropagationMode.ONEWAY),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.ONEWAY);
        checkReversePropModeConversion(PropagationMode.defaultSet(),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.DEFAULT
                + org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.WORK);
        checkReversePropModeConversion(PropagationMode.defaultSet(),
            org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.GLOBAL
                + org.glassfish.contextpropagation.weblogic.workarea.PropagationMode.WORK);
    }


    private void checkReversePropModeConversion(EnumSet<PropagationMode> modes, int mode) {
        assertEquals(modes, WLSWireAdapter.toPropagationMode(mode));

    }


    void foo(Object o) {
        System.out.println(o);
    }


    @Test
    public void testFoo() {
        foo(1);
    }


    @Test
    public void testStreams() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.write("foo".getBytes());
        oos.write("bar".getBytes());
        oos.flush();
        MockLoggerAdapter.debug(Utils.toString(baos.toByteArray()));

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.write("foo".getBytes());
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ObjectOutputStream oos2 = new ObjectOutputStream(baos2);
        oos2.write("bar".getBytes());
        oos2.flush();
        oos.write(baos2.toByteArray());
        oos.flush();
        MockLoggerAdapter.debug(Utils.toString(baos.toByteArray()));

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.write("foo".getBytes());
        baos2 = new ByteArrayOutputStream();
        oos2 = new ObjectOutputStream(baos2);
        oos2.write("bar".getBytes());
        oos2.flush();
        oos.flush();
        baos.write(baos2.toByteArray());
        MockLoggerAdapter.debug(Utils.toString(baos.toByteArray()));

        baos = new ByteArrayOutputStream();
        baos.write("foo".getBytes());
        MockLoggerAdapter.debug(Utils.toString(baos.toByteArray()));

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.write("foo".getBytes());
        baos2 = new ByteArrayOutputStream();
        oos2 = new ObjectOutputStream(baos2);
        oos2.write("bar".getBytes());
        oos2.flush();
        byte[] bytes = baos2.toByteArray();
        oos.write(bytes, 6, bytes.length - 6);
        oos.flush();
        MockLoggerAdapter.debug(Utils.toString(baos.toByteArray()));
    }
}
