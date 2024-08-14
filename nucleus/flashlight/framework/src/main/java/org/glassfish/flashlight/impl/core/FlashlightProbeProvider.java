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

import com.sun.enterprise.util.ObjectAnalyzer;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.api.monitoring.ProbeInfo;
import org.glassfish.api.monitoring.ProbeProviderInfo;
import org.glassfish.flashlight.provider.FlashlightProbe;

/**
 * Byron Nevins, October 2009
 * This class implements a very public interface.
 * I changed it to do some minimal error checking.
 * It throws RuntimeException because it is too late to change the signature
 * for the interface.
 *
 *
 * @author Mahesh Kannan
 * @author Byron Nevins
 */
public class FlashlightProbeProvider implements ProbeProviderInfo{
    /**
     * GUARANTEED to have all 3 names valid -- or at least not null and not empty
     * @param moduleProviderName
     * @param moduleName
     * @param probeProviderName
     * @param providerClazz
     * @throws RuntimeException if parameters are null or empty
     */
     public FlashlightProbeProvider(String moduleProviderName, String moduleName,
                                    String probeProviderName, Class providerClazz) {

        if(!ok(moduleProviderName) || !ok(moduleName) || !ok(providerClazz))
            throw new RuntimeException(CTOR_ERROR);

        this.moduleProviderName = moduleProviderName;
        this.moduleName = moduleName;
        this.providerClazz = providerClazz;

        if(probeProviderName == null)
            this.probeProviderName = providerClazz.getName();
        else
            this.probeProviderName = probeProviderName;

    }

        public String toString() {
                return ObjectAnalyzer.toString(this);
        }
    public Class getProviderClass() {
        return providerClazz;
    }
    public String getModuleProviderName() {
        return moduleProviderName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getProbeProviderName() {
        return probeProviderName;
    }

    public void addProbe(FlashlightProbe probe) {
        probes.put(probe.getProbeDesc(), probe);
    }

    public FlashlightProbe getProbe(String probeDescriptor) {
        return (probes.get(probeDescriptor));
    }

    public Collection<FlashlightProbe> getProbes() {
        return probes.values();
    }

    public ProbeInfo[] getProbesInfo() {
        // confusing -- the *map* of the probes is named "probes"
        Collection<FlashlightProbe> fprobes = getProbes();
        ProbeInfo[] infos = new ProbeInfo[fprobes.size()];

        int i = 0;
        for(FlashlightProbe fprobe : fprobes) {
            infos[i++] = (ProbeInfo) fprobe;
        }
        return infos;
    }

    public boolean isDTraceInstrumented() {
        return dtraceIsInstrumented;
    }
    public void setDTraceInstrumented(boolean b) {
        dtraceIsInstrumented = b;
    }

    // note that it is IMPOSSIBLE for an object instance to have null variables --
    // they are final and checked at instantiation time...
    // we are NOT checking the probes --
    public boolean namesEqual(Object o) {
        if(o == null)
            return false;

        if( ! (o instanceof FlashlightProbeProvider))
            return false;

        FlashlightProbeProvider fpp = (FlashlightProbeProvider) o;

        return
            fpp.moduleName.equals(moduleName) &&
            fpp.moduleProviderName.equals(moduleProviderName)  &&
            fpp.probeProviderName.equals(probeProviderName) &&
            fpp.providerClazz == providerClazz;
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private static boolean ok(Class clazz) {
        return clazz != null;
    }

    private boolean dtraceIsInstrumented;
    private final String moduleProviderName;
    private final String moduleName;
    private final String probeProviderName;
    private final Class providerClazz;
    private ConcurrentHashMap<String, FlashlightProbe> probes = new ConcurrentHashMap<String, FlashlightProbe>();
    private static final String CTOR_ERROR = "ProbeProviderInfo constructor -- you must supply valid arguments";
}
