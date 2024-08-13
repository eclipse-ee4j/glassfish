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

import java.util.concurrent.atomic.AtomicLong;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.grizzly.http.server.filecache.FileCache;

/**
 * File cache statistics
 *
 * @author Alexey Stashok
 */
@AMXMetadata(type="file-cache-mon", group="monitoring")
@ManagedObject
@Description("File Cache Statistics")
public class FileCacheStatsProvider implements StatsProvider {

    private final String name;

    protected final CountStatisticImpl hitsCount = new CountStatisticImpl("HitsCount", "count" , "Number of cache lookup hits");
    protected final CountStatisticImpl missesCount = new CountStatisticImpl("MissesCount", "count", "Number of cache lookup misses");
    protected final CountStatisticImpl infoHitsCount = new CountStatisticImpl("InfoHitsCount", "count", "Number of hits on cached file info");
    protected final CountStatisticImpl infoMissesCount = new CountStatisticImpl("InfoMissesCount", "count", "Number of misses on cached file info");
    protected final CountStatisticImpl contentHitsCount = new CountStatisticImpl("ContentHitsCount", "count", "Number of hits on cached file content");
    protected final CountStatisticImpl contentMissesCount = new CountStatisticImpl("ContentMissesCount", "count", "Number of misses on cached file content");

    protected final CountStatisticImpl openCacheEntriesCount = new CountStatisticImpl("OpenCacheEntriesCount", "count", "Number of current open cache entries");
    protected final AtomicLong heapSize = new AtomicLong();
    protected final AtomicLong mappedMemorySize = new AtomicLong();

    protected final AtomicLong maxHeapSize = new AtomicLong();
    protected final AtomicLong maxMappedMemorySize = new AtomicLong();

    protected volatile FileCache fileCache;

    public FileCacheStatsProvider(String name) {
        this.name = name;
    }

    @Override
    public Object getStatsObject() {
        return fileCache;
    }

    @Override
    public void setStatsObject(Object object) {
        if (object instanceof FileCache) {
            fileCache = (FileCache) object;
        } else {
            fileCache = null;
        }
    }

    @ManagedAttribute(id = "hits")
    @Description("Number of cache lookup hits")
    public CountStatistic getHitsCount() {
        return hitsCount;
    }

    @ManagedAttribute(id = "misses")
    @Description("Number of cache lookup misses")
    public CountStatistic getMissesCount() {
        return missesCount;
    }

    @ManagedAttribute(id = "infohits")
    @Description("Number of hits on cached file info")
    public CountStatistic getInfoHitsCount() {
        return infoHitsCount;
    }

    @ManagedAttribute(id = "infomisses")
    @Description("Number of misses on cached file info")
    public CountStatistic getInfoMissesCount() {
        return infoMissesCount;
    }

    @ManagedAttribute(id = "contenthits")
    @Description("Number of hits on cached file content")
    public CountStatistic getContentHitsCount() {
        return contentHitsCount;
    }

    @ManagedAttribute(id = "contentmisses")
    @Description("Number of misses on cached file content")
    public CountStatistic getContentMissesCount() {
        return contentMissesCount;
    }

    @ManagedAttribute(id = "opencacheentries")
    @Description("Number of current open cache entries")
    public CountStatistic getOpenCacheEntriesCount() {
        return openCacheEntriesCount;
    }

    @ManagedAttribute(id = "heapsize")
    @Description("Current cache size in bytes")
    public CountStatistic getHeapSize() {
        final CountStatisticImpl stats = new CountStatisticImpl("HeapSize",
                "byte(s)", "Current cache size in bytes");
        stats.setCount(heapSize.get());
        return stats;
    }

    @ManagedAttribute(id = "maxheapsize")
    @Description("Maximum heap space used for cache")
    public CountStatistic getMaxHeapSize() {
        final CountStatisticImpl stats = new CountStatisticImpl("MaxHeapSize",
                "byte(s)", "Maximum heap space used for cache");
        stats.setCount(maxHeapSize.get());
        return stats;
    }

    @ManagedAttribute(id = "mappedmemorysize")
    @Description("Size of mapped memory used for caching")
    public CountStatistic getMappedMemorySize() {
        final CountStatisticImpl stats = new CountStatisticImpl(
                "MappedMemorySize", "byte(s)",
                "Size of mapped memory used for caching");
        stats.setCount(mappedMemorySize.get());
        return stats;
    }

    @ManagedAttribute(id = "maxmappedmemorysize")
    @Description("Maximum memory map size used for caching")
    public CountStatistic getMaxMappedMemorySize() {
        final CountStatisticImpl stats = new CountStatisticImpl(
                "MaxMappedMemorySize", "byte(s)",
                "Maximum memory map size used for caching");
        stats.setCount(maxMappedMemorySize.get());
        return stats;
    }

    @ProbeListener("glassfish:kernel:file-cache:countHitEvent")
    public void countHitEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        if (name.equals(fileCacheName)) {
            hitsCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:countMissEvent")
    public void countMissEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        if (name.equals(fileCacheName)) {
            missesCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:countInfoHitEvent")
    public void countInfoHitEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        if (name.equals(fileCacheName)) {
            infoHitsCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:countInfoMissEvent")
    public void countInfoMissEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        if (name.equals(fileCacheName)) {
            infoMissesCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:countContentHitEvent")
    public void countContentHitEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        if (name.equals(fileCacheName)) {
            contentHitsCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:countContentMissEvent")
    public void countContentMissEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        if (name.equals(fileCacheName)) {
            contentMissesCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:incOpenCacheEntriesEvent")
    public void incOpenCacheEntriesEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        if (name.equals(fileCacheName)) {
            openCacheEntriesCount.increment();
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:decOpenCacheEntriesEvent")
    public void decOpenCacheEntriesEvent(@ProbeParam("fileCacheName") String fileCacheName) {
        if (name.equals(fileCacheName)) {
            openCacheEntriesCount.decrement();
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:addHeapSizeEvent")
    public void addHeapSizeEvent(@ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {
        if (name.equals(fileCacheName)) {
            final long newSize = heapSize.addAndGet(size);
            while (true) {
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
    }

    @ProbeListener("glassfish:kernel:file-cache:subHeapSizeEvent")
    public void subHeapSizeEvent(@ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {
        if (name.equals(fileCacheName)) {
            heapSize.addAndGet(-size);
        }
    }

    @ProbeListener("glassfish:kernel:file-cache:addMappedMemorySizeEvent")
    public void addMappedMemorySizeEvent(
            @ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {
        if (name.equals(fileCacheName)) {
            final long newSize = mappedMemorySize.addAndGet(size);
            while (true) {
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
    }

    @ProbeListener("glassfish:kernel:file-cache:subMappedMemorySizeEvent")
    public void subMappedMemorySizeEvent(
            @ProbeParam("fileCacheName") String fileCacheName,
            @ProbeParam("size") long size) {
        if (name.equals(fileCacheName)) {
            mappedMemorySize.addAndGet(-size);
        }
    }
}
