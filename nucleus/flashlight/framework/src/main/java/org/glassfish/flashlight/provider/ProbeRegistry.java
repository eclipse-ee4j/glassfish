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

package org.glassfish.flashlight.provider;

import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.flashlight.impl.core.ProbeProviderRegistry;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 *         Date: Jul 20, 2008
 */
@Service
@Singleton
public class ProbeRegistry {

    private static volatile ProbeRegistry _me = new ProbeRegistry();

    private static ConcurrentHashMap<Integer, FlashlightProbe> probeMap =
        new ConcurrentHashMap<Integer, FlashlightProbe>();
    private static ConcurrentHashMap<String, FlashlightProbe> probeDesc2ProbeMap =
        new ConcurrentHashMap<String, FlashlightProbe>();

    public static ProbeRegistry getInstance() {
        return _me;
    }

    // bnevins -- todo this is a huge concurrency bug!
    // why is it even here?!?
    // @deprecated

    @Deprecated
    public static ProbeRegistry createInstance() {
        if (_me == null) {
            _me = new ProbeRegistry();
        }

        return _me;
    }

    public static void cleanup() {
        if (_me != null) {
            _me = new ProbeRegistry();
        }
        ProbeProviderRegistry.cleanup();
    }

    public void registerProbe(FlashlightProbe probe) {
        probeMap.put(probe.getId(), probe);
        probeDesc2ProbeMap.put(probe.getProbeDesc(), probe);
        //System.out.println("[FL]Registered probe : " + probe.getProbeStr());
    }

    public void unregisterProbe(FlashlightProbe probe) {
        probeDesc2ProbeMap.remove(probe.getProbeDesc());
        probeMap.remove(probe.getId());
    }

    public void unregisterProbe(int id) {
        probeMap.remove(id);
    }

    public FlashlightProbe getProbe(int id) {
        return probeMap.get(id);
    }

    public FlashlightProbe getProbe(String probeStr) {
        //System.out.println("[FL]Get probe : " + probeStr);
        return probeDesc2ProbeMap.get(probeStr);
    }

    public static FlashlightProbe getProbeById(int id) {
        return _me.getProbe(id);
    }

    public Collection<FlashlightProbe> getAllProbes() {
        Collection<FlashlightProbe> allProbes = probeMap.values();
        Collection<FlashlightProbe> visibleProbes = new ArrayList<FlashlightProbe>();
        for (FlashlightProbe probe : allProbes) {
            if (!probe.isHidden())
                visibleProbes.add(probe);
        }
        return visibleProbes;
    }

    public static void invokeProbe(int id, Object[] args) {
        FlashlightProbe probe = probeMap.get(id);
        if (probe != null) {
            probe.fireProbe(args);
        }
    }

    public static Object invokeProbeBefore(int id, Object[] args) {
        FlashlightProbe probe = probeMap.get(id);
        if (probe != null) {
            return probe.fireProbeBefore(args);
        }
        return null;
    }

    public static void invokeProbeAfter(Object returnValue, int id,
        Object states) {
        FlashlightProbe probe = probeMap.get(id);
        if (probe != null) {
            try {
                probe.fireProbeAfter(returnValue, (ArrayList<FlashlightProbe.ProbeInvokeState>)states);
            } catch (ClassCastException e) {
                // Make sure the state we got was really ok, internal error if that happens
            }
        }
    }

    public static void invokeProbeOnException(Object exceptionValue, int id,
        Object states) {
        FlashlightProbe probe = probeMap.get(id);
        if (probe != null) {
            try {
                probe.fireProbeOnException(exceptionValue, (ArrayList<FlashlightProbe.ProbeInvokeState>)states);
            } catch (ClassCastException e) {
                // Make sure the state we got was really ok, internal error if that happens
            }
        }
    }
}
