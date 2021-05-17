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

package com.sun.enterprise.v3.services.impl.monitor.stats;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedObject;

/**
 * Server wide File cache statistics
 *
 * @author Amy Roh
 */
@AMXMetadata(type="file-cache-mon", group="monitoring")
@ManagedObject
@Description("File Cache Statistics")
public class FileCacheStatsProviderGlobal extends FileCacheStatsProvider {

    public FileCacheStatsProviderGlobal(String name) {
        super(name);
    }

    @ProbeListener("glassfish:kernel:file-cache:countHitEvent")
    public void countHitEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        hitsCount.increment();
    }

    @ProbeListener("glassfish:kernel:file-cache:countMissEvent")
    public void countMissEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        missesCount.increment();
    }

    @ProbeListener("glassfish:kernel:file-cache:countInfoHitEvent")
    public void countInfoHitEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        infoHitsCount.increment();
    }

    @ProbeListener("glassfish:kernel:file-cache:countInfoMissEvent")
    public void countInfoMissEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        infoMissesCount.increment();
    }

    @ProbeListener("glassfish:kernel:file-cache:countContentHitEvent")
    public void countContentHitEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        contentHitsCount.increment();
    }

    @ProbeListener("glassfish:kernel:file-cache:countContentMissEvent")
    public void countContentMissEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        contentMissesCount.increment();
    }

    @ProbeListener("glassfish:kernel:file-cache:incOpenCacheEntriesEvent")
    public void incOpenCacheEntriesEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        openCacheEntriesCount.increment();
    }

    @ProbeListener("glassfish:kernel:file-cache:decOpenCacheEntriesEvent")
    public void decOpenCacheEntriesEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        openCacheEntriesCount.decrement();
    }

    @ProbeListener("glassfish:kernel:file-cache:addHeapSizeEvent")
    public void addHeapSizeEvent(@ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {
        final long newSize = heapSize.addAndGet(size);
        for(;;) {
            final long maxSize = maxHeapSize.get();
            if (newSize > maxSize) {
                if (maxHeapSize.compareAndSet(maxSize, newSize)) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:subHeapSizeEvent")
    public void subHeapSizeEvent(@ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {
        heapSize.addAndGet(-size);
    }

    @ProbeListener("glassfish:kernel:file-cache:addMappedMemorySizeEvent")
    public void addMappedMemorySizeEvent(
            @ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {
        final long newSize = mappedMemorySize.addAndGet(size);
        for(;;) {
            final long maxMemSize = maxMappedMemorySize.get();
            if (newSize > maxMemSize) {
                if (maxMappedMemorySize.compareAndSet(maxMemSize, newSize)) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:subMappedMemorySizeEvent")
    public void subMappedMemorySizeEvent(
            @ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {
        mappedMemorySize.addAndGet(-size);
    }
}
