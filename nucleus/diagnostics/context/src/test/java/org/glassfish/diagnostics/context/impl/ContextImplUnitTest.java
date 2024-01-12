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

package org.glassfish.diagnostics.context.impl;

import java.util.EnumSet;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.glassfish.contextpropagation.Location;
import org.glassfish.contextpropagation.View;
import org.glassfish.diagnostics.context.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.glassfish.contextpropagation.PropagationMode.LOCAL;
import static org.glassfish.contextpropagation.PropagationMode.defaultSetOneway;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(EasyMockExtension.class)
public class ContextImplUnitTest {

    @Mock
    private View view;

    @Mock
    private Location location;

    /**
     * Test that the {@code location} field of {@code ContextImpl} uses the {@code Location} object
     * used at construction and that the {@code Location} returned from the {@code ContextImpl}
     * does not then change over the lifetime of the {@code ContextImpl}.
     */
    @Test
    public void testConstructorsLocation() {
        String locationId = "mockedLocationId";
        String origin = "mockedOrigin";

        expect(location.getLocationId()).andReturn(locationId);
        expect(location.getOrigin()).andReturn(origin);

        replay(view, location);

        Context context = new ContextImpl(view, location);

        Location location1 = context.getLocation();
        assertSame(location, location1,
            "Location from contextImpl.getLocation() should be the instance passed in on construction.");

        // On the face of is these next two assertions seem perfectly reasonable
        // but in reality they prove nothing regarding the behaviour of the
        // org.glassfish.diagnostics.context.impl code, but rather
        // verify that the  mocking framework is doing its job: the getLocationId
        // and getOrigin methods are overridden by the mock framework to
        // return the values above, they are not returning state from the
        // mockedLocation object itself.
        assertEquals(location1.getLocationId(), locationId,
            "LocationId from contextImpl.getLocation() should be the locationId value from the location used"
            + " when constructing the ContextImpl.");
        assertEquals(location1.getOrigin(), origin,
            "Origin from contextImpl.getOrigin() should be the origin value from the location used"
            + " when constructing the ContextImpl.");

        Location location2 = context.getLocation();
        assertSame(location, location2,
            "Location from contextImpl.getLocation() should still be the instance passed in on construction.");

        verify(view, location);
    }


    /**
     * Test that the put operations on an instance of {@link ContextImpl} delegate as expected
     * to the {@link View} object used in construction.
     */
    @Test
    public void testDelegationOfPut() {
        expect(view.put("KeyForString-Value1-true", "Value1", defaultSetOneway())).andReturn(null);
        expect(view.put("KeyForString-Value2-false", "Value2", EnumSet.of(LOCAL))).andReturn(null);
        expect(view.put("KeyForNumber-5-true", 5, defaultSetOneway())).andReturn(null);
        expect(view.put("KeyForNumber-7-false", 7, EnumSet.of(LOCAL))).andReturn(null);

        replay(view, location);

        Context context = new ContextImpl(view, location);

        context.put("KeyForString-Value1-true", "Value1", true);
        context.put("KeyForString-Value2-false", "Value2", false);

        context.put("KeyForNumber-5-true", 5, true);
        context.put("KeyForNumber-7-false", 7, false);

        verify(view, location);
    }

    /**
     * Test that the get operation on an instance of {@link  ContextImpl} delegates as expected
     * to the {@link View} object used in construction.
     */
    @Test
    public void testDelegationOfGet() {
        final String key = "testDelegationOfGet-Key1";
        final String expectedValue = "testDelegationOfGet-Value1";

        // We expect get to be called on the view, and we'll
        // instruct the mocking framework to return expectedValueOfKey1
        // so that we can also verify that contextImpl returns it.
        expect(view.get(key)).andReturn(expectedValue);

        replay(view, location);

        Context context = new ContextImpl(view, location);

        assertEquals(expectedValue, context.get(key),
            "Value returned from contextImpl.get(\"" + key + "\") is not the value expected.");

        verify(view, location);
    }
}
