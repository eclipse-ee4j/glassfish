/*
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

package org.glassfish.kernel.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * Simple implementation of the events dispatching facility.
 *
 * @author Jerome Dochez
 */
@Service
public class EventsImpl implements Events {

    @Inject
    ExecutorService executor;

    final static Logger logger = KernelLoggerInfo.getLogger();

    List<EventListener> listeners = Collections.synchronizedList(new ArrayList<EventListener>());

    @Override
    public synchronized void register(EventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void send(final Event event) {
        send(event, true);
    }

    @Override
    public void send(final Event event, boolean asynchronously) {

        List<EventListener> l = new ArrayList<EventListener>();
        l.addAll(listeners);
        for (final EventListener listener : l) {

            Method m =null;
            try {
                // check if the listener is interested with his event.
                m = listener.getClass().getMethod("event", Event.class);
            } catch (Throwable ex) {
                // We need to catch Throwable, otherwise we can server not to
                // shutdown when the following happens:
                // Assume a bundle which has registered a event listener
                // has been uninstalled without unregistering the listener.
                // listener.getClass() refers to a class of such an uninstalled
                // bundle. If framework has been refreshed, then the
                // classloader can't be used further to load any classes.
                // As a result, an exception like NoClassDefFoundError is thrown
                // from getMethod.
                logger.log(Level.SEVERE, KernelLoggerInfo.exceptionSendEvent, ex);
            }
            if (m!=null) {
                RestrictTo fooBar = m.getParameters()[0].getAnnotation(RestrictTo.class);
                if (fooBar!=null) {
                    EventTypes interested = EventTypes.create(fooBar.value());
                    if (!event.is(interested)) {
                        continue;
                    }
                }
            }

            if (asynchronously) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listener.event(event);
                        } catch(Throwable e) {
                            logger.log(Level.WARNING, KernelLoggerInfo.exceptionDispatchEvent, e);
                        }
                    }
                });
            } else {
                try {
                    listener.event(event);
                } catch (DeploymentException de) {
                    // when synchronous listener throws DeploymentException
                    // we re-throw the exception to abort the deployment
                    throw de;
                } catch (Throwable e) {
                    logger.log(Level.WARNING, KernelLoggerInfo.exceptionDispatchEvent, e);
                }
            }
        }
    }

    @Override
    public synchronized boolean unregister(EventListener listener) {
        return listeners.remove(listener);
    }
}
