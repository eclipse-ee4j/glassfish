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

import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
import org.glassfish.contextpropagation.internal.Utils.AccessControlledMapFinder;
import org.glassfish.contextpropagation.wireadapters.glassfish.DefaultWireAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AccessControlledMapFinderTest {

    private static AccessControlledMapFinder mapFinder = new AccessControlledMapFinder();

    @BeforeEach
    public void setup() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
        mapFinder.getMapAndCreateIfNeeded();
    }


    @Test
    public void testGetMapIfItExistsButDoesnt() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
        assertNull(mapFinder.getMapIfItExists());
    }


    @Test
    public void testGetMapIfItExistsWhenItDoes() {
        assertNotNull(mapFinder.getMapIfItExists());
    }


    @Test
    public void testCreateMapIfItExistsButDoesnt() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
        assertNull(mapFinder.getMapIfItExists());
        assertNotNull(mapFinder.getMapAndCreateIfNeeded());
    }


    @Test
    public void testCreateMapIfItExistsWhenItDoes() {
        assertEquals(mapFinder.getMapIfItExists(), mapFinder.getMapAndCreateIfNeeded());
    }

}
