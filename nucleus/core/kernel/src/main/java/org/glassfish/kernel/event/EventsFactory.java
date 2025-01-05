/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import org.glassfish.api.event.Events;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * HK2 factory for the {@link Events} implementations.
 */
@Service
public class EventsFactory implements Factory<Events> {

    private static final String BUNDLE_REFERENCE_CLASS_NAME = "org.osgi.framework.BundleReference";

    @Inject
    private ServiceLocator serviceLocator;

    @Override
    @Singleton
    public Events provide() {
        if (isOSGiEnv()) {
            return serviceLocator.createAndInitialize(OSGiAwareEventsImpl.class);
        }
        return serviceLocator.createAndInitialize(EventsImpl.class);
    }

    @Override
    public void dispose(Events events) {
        serviceLocator.preDestroy(events);
    }

    /**
     * Determine if we are operating in OSGi environment.
     *
     * <p>We do this by checking what class loader is used to this class.
     *
     * @return {@code true} if we are called in the context of OSGi framework, {@code false} otherwise
     */
    private boolean isOSGiEnv() {
        return isBundleReference(getClass().getClassLoader());
    }

    /**
     * Determines if the specified object is OSGi bundle reference.
     *
     * @param obj the object to check
     * @return {@code true} if the {@code obj} is OSGi bundle reference, {@code false} otherwise
     */
    private boolean isBundleReference(Object obj) {
        Queue<Class<?>> interfaces = new ArrayDeque<>();
        Class<?> c = obj.getClass();
        while (c != null && c != Object.class) {
            interfaces.addAll(Arrays.asList(c.getInterfaces()));
            while (!interfaces.isEmpty()) {
                Class<?> interfaceClass = interfaces.poll();
                if (BUNDLE_REFERENCE_CLASS_NAME.equals(interfaceClass.getName())) {
                    return true;
                }
                interfaces.addAll(Arrays.asList(interfaceClass.getInterfaces()));
            }
            c = c.getSuperclass();
        }
        return false;
    }
}
