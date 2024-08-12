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

package com.sun.enterprise.v3.services.impl.monitor;

import com.sun.enterprise.v3.services.impl.monitor.stats.FileCacheStatsProvider;

import org.glassfish.grizzly.http.server.filecache.FileCache;
import org.glassfish.grizzly.http.server.filecache.FileCacheEntry;
import org.glassfish.grizzly.http.server.filecache.FileCacheProbe;

/**
 *
 * @author oleksiys
 */
public class FileCacheMonitor implements FileCacheProbe {
    private final GrizzlyMonitoring grizzlyMonitoring;
    private final String monitoringId;

    public FileCacheMonitor(GrizzlyMonitoring grizzlyMonitoring,
            String monitoringId, FileCache config) {
        this.grizzlyMonitoring = grizzlyMonitoring;
        this.monitoringId = monitoringId;

        if (grizzlyMonitoring != null) {
            final FileCacheStatsProvider statsProvider =
                    grizzlyMonitoring.getFileCacheStatsProvider(monitoringId);
            if (statsProvider != null) {
                statsProvider.setStatsObject(config);
            }

//            statsProvider.reset();
        }
    }

    @Override
    public void onEntryAddedEvent(final FileCache fileCache, final FileCacheEntry entry) {
        grizzlyMonitoring.getFileCacheProbeProvider().incOpenCacheEntriesEvent(monitoringId);
        switch (entry.type) {
            case HEAP: {
                grizzlyMonitoring.getFileCacheProbeProvider().addHeapSizeEvent(monitoringId, entry.getFileSize(false));
                break;
            }
            case MAPPED: {
                grizzlyMonitoring.getFileCacheProbeProvider().addMappedMemorySizeEvent(monitoringId, entry.getFileSize(false));
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected type: " + entry.type);
            }
        }

    }

    @Override
    public void onEntryRemovedEvent(final FileCache fileCache, final FileCacheEntry entry) {
        grizzlyMonitoring.getFileCacheProbeProvider().decOpenCacheEntriesEvent(monitoringId);
        switch (entry.type) {
            case HEAP: {
                grizzlyMonitoring.getFileCacheProbeProvider().subHeapSizeEvent(monitoringId, entry.getFileSize(false));
                break;
            }
            case MAPPED: {
                grizzlyMonitoring.getFileCacheProbeProvider().subMappedMemorySizeEvent(monitoringId, entry.getFileSize(false));
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected type: " + entry.type);
            }
        }
    }

    @Override
    public void onEntryHitEvent(final FileCache fileCache, final FileCacheEntry entry) {
        grizzlyMonitoring.getFileCacheProbeProvider().countHitEvent(monitoringId);

        switch (entry.type) {
            case HEAP: {
                grizzlyMonitoring.getFileCacheProbeProvider().countInfoHitEvent(monitoringId);
                break;
            }
            case MAPPED: {
                grizzlyMonitoring.getFileCacheProbeProvider().countContentHitEvent(monitoringId);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected type: " + entry.type);
            }
        }
    }

    @Override
    public void onEntryMissedEvent(final FileCache fileCache, final String host, final String requestURI) {
        grizzlyMonitoring.getFileCacheProbeProvider().countMissEvent(monitoringId);
    }

    @Override
    public void onErrorEvent(final FileCache fileCache, final Throwable error) {
    }
}
