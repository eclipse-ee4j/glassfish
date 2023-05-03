/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;

import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * Fake container for http service configuration
 *
 * User: Jerome Dochez
 * Date: May 13, 2008
 * Time: 11:55:01 AM
 */
@Service
public class NetworkListenersContainer implements ConfigListener {

    @Inject
    NetworkListeners httpService;

    volatile boolean received=false;

    @Override
    public synchronized UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        if (received) {
            // I am already happy
        }
        return ConfigSupport.sortAndDispatch(events, new Changed() {

            @Override
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> tClass, T t) {
                if (type == TYPE.ADD) {
                    received = true;
                }

                // we did not deal with it, so it is unprocsseed
                return new NotProcessed("unimplemented by NetworkListenersContainer");
                // System.out.println("Event type : " + type + " class " + tClass +" -> " + t);
            }
        });
    }
}
