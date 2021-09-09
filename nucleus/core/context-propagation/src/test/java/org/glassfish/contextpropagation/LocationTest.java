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

package org.glassfish.contextpropagation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.glassfish.contextpropagation.adaptors.BootstrapUtils;
import org.glassfish.contextpropagation.adaptors.MockLoggerAdapter;
import org.glassfish.contextpropagation.adaptors.TestableThread;
import org.glassfish.contextpropagation.internal.ViewImpl;
import org.glassfish.contextpropagation.spi.ContextMapHelper;
import org.glassfish.contextpropagation.wireadapters.glassfish.DefaultWireAdapter;
import org.glassfish.tests.utils.ReflectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocationTest {

    @BeforeAll
    public static void setupClass() {
        BootstrapUtils.bootstrap(new DefaultWireAdapter());
    }


    @Test
    public void testGetOrigin() {
        Location location = new Location(new ViewImpl("prefix") {
        });
        assertEquals("guid", location.getOrigin());
        ReflectionUtils.setField(location, "origin", "non-null origin");
        assertEquals("non-null origin", location.getOrigin());
    }


    @Test
    public void testGetLocationId() {
        Location location = new Location(new ViewImpl("prefix") {
        });
        assertEquals("[0]", location.getLocationId());
    }


    @Test
    public void testContextToPropagateAndContextAdded() {
        Location location = new Location(new ViewImpl("prefix") {
        });
        Location locationToPropagate = (Location) location.contextToPropagate();
        assertEquals(location, locationToPropagate);
        Location propagatedLocation = new Location(new ViewImpl("prefix") {
        });
        View view = ReflectionUtils.getField(location, "view");
        ReflectionUtils.setField(propagatedLocation, "view", view);
        propagatedLocation.contextAdded();
        assertEquals("[0, 1]", propagatedLocation.getLocationId());
    }


    @Test
    @Disabled("Causes ConcurrentModificationException in SimpleMap.findNext (access to iterator)")
    public void testMultiplePropagations() throws Exception {
        ContextMap wcMap = ContextMapHelper.getScopeAwareContextMap();
        Location location = wcMap.getLocation();
        assertEquals("guid", location.getOrigin());
        assertEquals("[0]", location.getLocationId());
        // TODO NOW make sure the location is created if this is the origin of the request.
        for (int i = 1; i <= 3; i++) {
            mimicPropagation("[0, " + i + "]");
        }
    }


    private static void mimicPropagation(final String expectedLocationId) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ContextMapHelper.getScopeAwarePropagator().sendRequest(bos, PropagationMode.SOAP);

        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        new TestableThread() {

            @Override
            protected void runTest() throws Exception {
                ContextMapHelper.getScopeAwarePropagator().receiveRequest(bis);

                ContextMap wcMap = ContextMapHelper.getScopeAwareContextMap();
                Location location = wcMap.getLocation();
                MockLoggerAdapter.debug(location.getLocationId());
                assertEquals(expectedLocationId, location.getLocationId());
            }
        }.startJoinAndCheckForFailures();
    }
}
