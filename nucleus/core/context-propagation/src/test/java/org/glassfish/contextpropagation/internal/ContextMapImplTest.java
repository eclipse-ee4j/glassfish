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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.glassfish.tests.utils.Utils.getStaticField;
import static org.glassfish.tests.utils.Utils.setStaticField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContextMapImplTest {

    private static final Entry DUMMY_ENTRY
        = new Entry("dummy", PropagationMode.defaultSet(), ContextType.STRING).init(true, true);
    private static final EnumSet<PropagationMode> PROP_MODES = PropagationMode.defaultSet();
    private static ContextMap contextMap;
    private static AccessControlledMap acMapForFinder;
    private static AccessControlledMap acMapCustom;
    private static AccessControlledMapFinder originalFinder;
    private static AccessControlledMapFinder mapFinder = new AccessControlledMapFinder() {

        @Override
        protected AccessControlledMap getMapIfItExists() {
            AccessControlledMap map = super.getMapIfItExists();
            return map == null ? acMapForFinder : map;
        }
    };

    @BeforeAll
    public static void setupClass() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
        originalFinder = getStaticField(Utils.class, "mapFinder");
        setStaticField(Utils.class, "mapFinder", mapFinder);

        contextMap = Utils.getScopeAwareContextMap();

        acMapCustom = new AccessControlledMap();
        acMapCustom.simpleMap.put("key", DUMMY_ENTRY);
        acMapCustom.simpleMap.put("removeMe", DUMMY_ENTRY);

        Entry entry = new Entry(new Location(new ViewImpl(Location.KEY)) {
        }, PropagationMode.defaultSet(), ContextType.VIEW_CAPABLE).init(true, true);
        acMapCustom.simpleMap.put(Location.KEY, entry);
    }


    @BeforeEach
    public void setup() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
        acMapForFinder = null;
    }

    @AfterAll
    public static void reset() {
        BootstrapUtils.reset();
        setStaticField(Utils.class, "mapFinder", originalFinder);
    }

    @Test
    public void testGet() throws InsufficientCredentialException {
        assertNull(contextMap.get("key"));
        acMapForFinder = acMapCustom;
        assertEquals("dummy", contextMap.get("key"));
    }


    @Test
    public void testPutString() throws InsufficientCredentialException {
        String key = "a String";
        String origContext = "string";
        contextMap.put(key, origContext, PROP_MODES);
        checkPut(key, origContext);
        assertEquals(origContext, contextMap.put(key, "new string", PROP_MODES));
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
        contextMap.put(key, origContext, PROP_MODES);
        checkPut(key, origContext);
        assertThat(contextMap.put(key, 2L, PROP_MODES), equalTo(origContext));
    }


    @Test
    public void testPutBoolean() throws InsufficientCredentialException {
        String key = "a boolean";
        boolean origContext = true;
        contextMap.put(key, origContext, PROP_MODES);
        checkPut(key, origContext);
        assertThat(contextMap.put(key, false, PROP_MODES), equalTo(origContext));
    }


    @Test
    public void testCreateViewCapable() throws InsufficientCredentialException {
        acMapForFinder = acMapCustom;
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
        assertNotNull(contextMap.createViewCapable(prefix));
    }


    @Test
    public void testGetPropagationModes() throws InsufficientCredentialException {
        assertNull(contextMap.getPropagationModes("key"));
        acMapForFinder = acMapCustom;
        assertEquals(PropagationMode.defaultSet(), contextMap.getPropagationModes("key"));
    }


    @Test
    public void testRemove() throws InsufficientCredentialException {
        acMapForFinder = acMapCustom;
        assertNull(contextMap.remove("nonexistent"));
        assertNotNull(contextMap.remove("removeMe"));
    }


    @Test
    public void testPutCharacter() throws InsufficientCredentialException {
        String key = "a Character";
        char origContext = 'c';
        contextMap.put(key, origContext, PROP_MODES);
        checkPut(key, origContext);
        assertThat(contextMap.put(key, 'd', PROP_MODES), equalTo(origContext));
    }


    @Test
    public void testGetLocationNormalCase() {
        acMapForFinder = acMapCustom;
        Location location = contextMap.getLocation();
        assertNotNull(location);
    }


    @Test
    public void testIsEmpty() {
        assertTrue(contextMap.isEmpty());
        acMapForFinder = new AccessControlledMap();
        assertTrue(contextMap.isEmpty());
        acMapForFinder = acMapCustom;
        assertFalse(contextMap.isEmpty());
    }


    @Test
    public void testNames() {
        Iterator<?> iter = contextMap.names();
        assertNull(iter);
        acMapForFinder = acMapCustom;
        iter = contextMap.names();
        while (iter.hasNext()) {
            MockLoggerAdapter.debug((String) iter.next());
        }
    }

}
