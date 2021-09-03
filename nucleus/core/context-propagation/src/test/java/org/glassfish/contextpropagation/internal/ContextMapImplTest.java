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

import java.util.EnumSet;
import java.util.Iterator;

import org.glassfish.contextpropagation.ContextMap;
import org.glassfish.contextpropagation.ContextViewFactory;
import org.glassfish.contextpropagation.InsufficientCredentialException;
import org.glassfish.contextpropagation.Location;
import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.View;
import org.glassfish.contextpropagation.ViewCapable;
import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
import org.glassfish.contextpropagation.adaptors.MockLoggerAdapter;
import org.glassfish.contextpropagation.internal.Entry.ContextType;
import org.glassfish.contextpropagation.internal.Utils.AccessControlledMapFinder;
import org.glassfish.contextpropagation.spi.ContextMapHelper;
import org.glassfish.contextpropagation.wireadapters.glassfish.DefaultWireAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.glassfish.contextpropagation.adaptors.BootstrapUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContextMapImplTest {

    private static Entry DUMMY_ENTRY;
    private static EnumSet<PropagationMode> PROP_MODES = PropagationMode.defaultSet();
    private static ContextMap cm;
    private static AccessControlledMap acMap;
    private static AccessControlledMap savedMap;
    static AccessControlledMapFinder mapFinder = new AccessControlledMapFinder() {

        @Override
        protected AccessControlledMap getMapIfItExists() {
            AccessControlledMap map = super.getMapIfItExists();
            return map == null ? acMap : map;
        }
    };

    @BeforeAll
    public static void setupClass() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
        DUMMY_ENTRY = new Entry("dummy", PropagationMode.defaultSet(), ContextType.STRING).init(true, true);
        savedMap = new AccessControlledMap();
        setStaticField(Utils.class, "mapFinder", mapFinder);
        cm = Utils.getScopeAwareContextMap();
        savedMap.simpleMap.put("key", DUMMY_ENTRY);
        savedMap.simpleMap.put("removeMe", DUMMY_ENTRY);
        Entry entry = new Entry(new Location(new ViewImpl(Location.KEY)) {
        }, PropagationMode.defaultSet(), ContextType.VIEW_CAPABLE).init(true, true);
        savedMap.simpleMap.put(Location.KEY, entry);
    }


    @BeforeEach
    public void setup() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
        acMap = null;
    }


    @Test
    public void testGet() throws InsufficientCredentialException {
        assertNull(cm.get("key"));
        acMap = savedMap;
        assertEquals("dummy", cm.get("key"));
    }


    @Test
    public void testPutString() throws InsufficientCredentialException {
        String key = "a String";
        String origContext = "string";
        cm.put(key, origContext, PROP_MODES);
        checkPut(key, origContext);
        assertEquals(origContext, cm.put(key, "new string", PROP_MODES));
    }


    protected void checkPut(String key, Object origContext) throws InsufficientCredentialException {
        assertNotNull(mapFinder.getMapIfItExists());
        assertEquals(origContext, mapFinder.getMapIfItExists().get(key));
        assertSame(PROP_MODES, mapFinder.getMapIfItExists().getPropagationModes(key));
    }


    @Test
    public void testPutNumber() throws InsufficientCredentialException {
        String key = "a long";
        long origContext = 1L;
        cm.put(key, origContext, PROP_MODES);
        checkPut(key, origContext);
        assertThat(cm.put(key, 2L, PROP_MODES), equalTo(origContext));
    }


    @Test
    public void testPutBoolean() throws InsufficientCredentialException {
        String key = "a boolean";
        boolean origContext = true;
        cm.put(key, origContext, PROP_MODES);
        checkPut(key, origContext);
        assertThat(cm.put(key, false, PROP_MODES), equalTo(origContext));
    }


    @Test
    public void testCreateViewCapable() throws InsufficientCredentialException {
        acMap = savedMap;
        String prefix = "a view capable";
        ContextMapHelper.registerContextFactoryForPrefixNamed(prefix, new ContextViewFactory() {

            @Override
            public EnumSet<PropagationMode> getPropagationModes() {
                return PropagationMode.defaultSet();
            }


            @Override
            public ViewCapable createInstance(View view) {
                return new ViewCapable() {
                    /* dummy instance */};
            }
        });
        assertNotNull(cm.createViewCapable(prefix));
    }


    @Test
    public void testGetPropagationModes() throws InsufficientCredentialException {
        assertNull(cm.getPropagationModes("key"));
        acMap = savedMap;
        assertEquals(PropagationMode.defaultSet(), cm.getPropagationModes("key"));
    }


    @Test
    public void testRemove() throws InsufficientCredentialException {
        acMap = savedMap;
        assertNull(cm.remove("nonexistent"));
        assertNotNull(cm.remove("removeMe"));
    }


    @Test
    public void testPutCharacter() throws InsufficientCredentialException {
        String key = "a Character";
        char origContext = 'c';
        cm.put(key, origContext, PROP_MODES);
        checkPut(key, origContext);
        assertThat(cm.put(key, 'd', PROP_MODES), equalTo(origContext));
    }


    @Test
    public void testGetLocationNormalCase() {
        acMap = savedMap;
        Location location = cm.getLocation();
        assertNotNull(location);
    }


    @Test
    public void testIsEmpty() {
        assertTrue(cm.isEmpty());
        acMap = new AccessControlledMap();
        assertTrue(cm.isEmpty());
        acMap = savedMap;
        assertFalse(cm.isEmpty());
    }


    @Test
    public void testNames() {
        Iterator<?> iter = cm.names();
        assertNull(iter);
        acMap = savedMap;
        iter = cm.names();
        while (iter.hasNext()) {
            MockLoggerAdapter.debug((String) iter.next());
        }
    }

}
