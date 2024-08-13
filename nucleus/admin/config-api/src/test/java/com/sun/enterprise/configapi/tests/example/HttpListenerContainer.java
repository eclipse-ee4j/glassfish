/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests.example;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyChangeEvent;

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple container code that is interested in getting notification of injected model changes
 *
 * @author Jerome Dochez
 */
@Service
@PerLookup
public class HttpListenerContainer implements ConfigListener {

    @Inject
    @Named("http-listener-1")
    public NetworkListener httpListener;

    public volatile boolean received = false;

    @Override
    public synchronized UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        if (received) {
            // I am alredy happy
            return null;
        }
        assertTrue(events.length==1);
        String listenerName = ((NetworkListener) events[0].getSource()).getName();
        assertEquals("http-listener-1",listenerName);
        assertEquals("8989",events[0].getNewValue().toString());
        received = true;
        return null;
    }

}
