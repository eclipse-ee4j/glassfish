/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.flashlight.impl.core;

import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.provider.ProbeRegistry;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 */
@Service
@Singleton
public class ProbeFactory {

    private static AtomicInteger counter = new AtomicInteger();

    static ProbeRegistry probeRegistry = ProbeRegistry.getInstance();

    public static FlashlightProbe createProbe(Class providerClazz,
        String moduleProviderName, String moduleName,
        String probeProviderName, String probeName, String[] paramNames,
        Class[] paramTypes, boolean self, boolean hidden) {

        return createProbe(providerClazz, moduleProviderName, moduleName, probeProviderName,
            probeName, paramNames, paramTypes, self, hidden,
            false, false, false, null); // not stateful, no profile names
    }

    public static FlashlightProbe createProbe(Class providerClazz,
        String moduleProviderName, String moduleName,
        String probeProviderName, String probeName, String[] paramNames,
        Class[] paramTypes, boolean self, boolean hidden,
        boolean stateful, boolean statefulReturn, boolean statefulException,
        String [] profileNames) {

        int id = counter.incrementAndGet();
        FlashlightProbe probe = new FlashlightProbe(id, providerClazz,
            moduleProviderName, moduleName, probeProviderName, probeName,
            paramNames, paramTypes, self, hidden, stateful, statefulReturn,
            statefulException, profileNames);

        probeRegistry.registerProbe(probe);
        return probe;
    }

}
