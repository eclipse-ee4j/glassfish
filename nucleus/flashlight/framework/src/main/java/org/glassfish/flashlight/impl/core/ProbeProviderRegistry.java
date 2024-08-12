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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.glassfish.flashlight.FlashlightUtils;

/**
 * @author Mahesh Kannan
 * @author Byron Nevins
 * Date: Jun 25, 2008
 *
 * Byron Nevins says:  Note this class is used in one and only one place in all of
 * GF --> FlashlightProbeProviderFactory.java
 * Apparently it is used exclusively for making sure that 2 probes with the same name are not allowed?!?
 * look at the putIfAbsent for clues
 * This class has no business having any public members and should be moved to the only user's package
 *
 * I'm suspicious.  Maybe it is called by reflection from somewhere?!?
 */
public class ProbeProviderRegistry {
    private ProbeProviderRegistry() {
    }

    public static ProbeProviderRegistry getInstance() {
        return _me;
    }

    public static void cleanup() {
        _me = new ProbeProviderRegistry();
    }

    public Collection<FlashlightProbeProvider> getAllProbeProviders() {
        return Collections.unmodifiableCollection(providerMap.values());
    }

    public FlashlightProbeProvider getProbeProvider(String moduleProviderName, String moduleName, String probeProviderName) {
        if(probeProviderName == null)
            probeProviderName = "";

        return providerMap.get(FlashlightUtils.makeName(moduleProviderName, moduleName, probeProviderName));
    }

    public FlashlightProbeProvider getProbeProvider(Class clz) {
        return classProviderMap.get(clz);
    }

    public FlashlightProbeProvider getProbeProvider(FlashlightProbeProvider fpp) {
        return providerMap.get(FlashlightUtils.makeName(fpp));
    }

    public FlashlightProbeProvider registerProbeProvider(FlashlightProbeProvider provider, Class clz) {

        String qname = FlashlightUtils.makeName(provider);

        // if there is an entry in the map for qname already -- it is an error
        // ConcurrentMap allows us to check and put with thread-safety!
        // putIfAbsent returns null iff there was no value already in there.

        if (providerMap.putIfAbsent(qname, provider) != null) {
            throw new IllegalStateException("Provider already mapped " + qname);
        }

        if (classProviderMap.putIfAbsent(clz, provider) != null) {
            throw new IllegalStateException("Provider already mapped " + qname);
        }

        return provider;
    }

    public void unregisterProbeProvider(Object provider) {
        FlashlightProbeProvider fpp = classProviderMap.remove(provider.getClass());
        String qname = FlashlightUtils.makeName(fpp);
        providerMap.remove(qname);
    }

    private static ProbeProviderRegistry _me =
            new ProbeProviderRegistry();

    private ConcurrentMap<String, FlashlightProbeProvider> providerMap =
            new ConcurrentHashMap<String, FlashlightProbeProvider>();
    private ConcurrentMap<Class, FlashlightProbeProvider> classProviderMap =
            new ConcurrentHashMap<Class, FlashlightProbeProvider>();
}
