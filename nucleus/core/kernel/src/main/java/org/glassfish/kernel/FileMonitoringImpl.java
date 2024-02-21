/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.kernel;

import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.api.admin.FileMonitoring;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Jerome Dochez
 */
@Service
public class FileMonitoringImpl implements FileMonitoring, PostConstruct {

    @Inject
    ExecutorService executor;

    @Inject
    ScheduledExecutorService scheduledExecutor;

    @Inject
    Events events;

    final Map<File, List<FileChangeListener>> listeners = new HashMap<File, List<FileChangeListener>>();
    final Map<File, Long> monitored = new HashMap<File, Long>();

    public void postConstruct() {
        final ScheduledFuture<?> future = scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                if (monitored.isEmpty()) {
                    return;
                }
                // check our list of monitored files for any changes
                Set<File> monitoredFiles = new HashSet<File>();
                monitoredFiles.addAll(listeners.keySet());
                for (File file : monitoredFiles) {
                    if (!file.exists()) {
                        removed(file);
                        listeners.remove(file);
                        monitored.remove(file);
                    } else
                    if (file.lastModified()!=monitored.get(file)) {
                        // file has changed
                        monitored.put(file, file.lastModified());
                        changed(file);
                    }
                }

            }
        }, 0, 500, TimeUnit.MILLISECONDS);
        events.register(event -> {
            if (event.is(EventTypes.PREPARE_SHUTDOWN)) {
                System.out.println("FileMonitoring shutdown");
                future.cancel(false);
            }
        });
    }

    public synchronized void monitors(File file, FileChangeListener listener) {

        if (monitored.containsKey(file)) {
            listeners.get(file).add(listener);
        } else {
            List<FileChangeListener> list = new ArrayList<FileChangeListener>();
            list.add(listener);
            listeners.put(file, list);
            monitored.put(file, file.lastModified());
        }
    }

    public synchronized void fileModified(File file) {
        monitored.put(file, 0L);
    }

    private void removed(final File file) {
        for (final FileChangeListener listener : listeners.get(file)) {
            executor.submit(new Runnable() {
                public void run() {
                    listener.deleted(file);
                }
            });
        }

    }

    private void changed(final File file) {
        for (final FileChangeListener listener : listeners.get(file)) {
            executor.submit(new Runnable() {
                public void run() {
                    listener.changed(file);
                }
            });
        }
    }
}
