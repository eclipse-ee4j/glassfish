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

package com.sun.enterprise.v3.services.impl.monitor.probes;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 * Probe provider interface for file-cache related events.
 *
 * @author Alexey Stashok
 */
@ProbeProvider (moduleProviderName="glassfish", moduleName="kernel", probeProviderName="file-cache")
public class FileCacheProbeProvider {
    @Probe(name="countHitEvent")
    public void countHitEvent(@ProbeParam("fileCacheName") String fileCacheName) {}

    @Probe(name="countMissEvent")
    public void countMissEvent(@ProbeParam("fileCacheName") String fileCacheName) {}

    @Probe(name="countInfoHitEvent")
    public void countInfoHitEvent(@ProbeParam("fileCacheName") String fileCacheName) {}

    @Probe(name="countInfoMissEvent")
    public void countInfoMissEvent(@ProbeParam("fileCacheName") String fileCacheName) {}

    @Probe(name="countContentHitEvent")
    public void countContentHitEvent(@ProbeParam("fileCacheName") String fileCacheName) {}

    @Probe(name="countContentMissEvent")
    public void countContentMissEvent(@ProbeParam("fileCacheName") String fileCacheName) {}

    @Probe(name="incOpenCacheEntriesEvent")
    public void incOpenCacheEntriesEvent(@ProbeParam("fileCacheName") String fileCacheName) {}

    @Probe(name="decOpenCacheEntriesEvent")
    public void decOpenCacheEntriesEvent(@ProbeParam("fileCacheName") String fileCacheName) {}

    @Probe(name="addHeapSizeEvent")
    public void addHeapSizeEvent(@ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {}

    @Probe(name="subHeapSizeEvent")
    public void subHeapSizeEvent(@ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {}

    @Probe(name="addMappedMemorySizeEvent")
    public void addMappedMemorySizeEvent(
            @ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {}

    @Probe(name="subMappedMemorySizeEvent")
    public void subMappedMemorySizeEvent(
            @ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {}
}
