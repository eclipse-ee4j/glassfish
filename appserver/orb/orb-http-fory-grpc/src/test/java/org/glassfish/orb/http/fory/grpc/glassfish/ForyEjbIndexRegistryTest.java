/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */


package org.glassfish.orb.http.fory.grpc.glassfish;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reading the deployed applications from the registry: an application that is
 * not loaded is not read. {@code APPLICATION_UNLOADED} is sent from inside the
 * application's unload, after it was marked as not loaded and before the
 * registry lets go of it, so this is what keeps it from being published again.
 */
class ForyEjbIndexRegistryTest {

    /** An application that says whether it is loaded, and counts being asked for its metadata. */
    private static final class Info extends ApplicationInfo {
        private final boolean loaded;
        private final AtomicInteger metaDataReads = new AtomicInteger();

        Info(String name, boolean loaded) {
            super(null, null, name);
            this.loaded = loaded;
        }

        @Override
        public boolean isLoaded() {
            return loaded;
        }

        @Override
        public <T> T getMetaData(Class<T> type) {
            metaDataReads.incrementAndGet();
            return null;
        }
    }

    @Test
    void anApplicationThatIsNotLoadedIsNotRead() {
        ApplicationRegistry registry = new ApplicationRegistry();
        Info unloading = new Info("unloading", false);
        Info running = new Info("running", true);
        registry.add("unloading", unloading);
        registry.add("running", running);

        List<ForyEjbIndex.DeployedView> views = new ArrayList<>(ForyEjbIndex.deployedViews(registry));

        assertEquals(List.of(), views);
        assertEquals(0, unloading.metaDataReads.get(), "an application that is not loaded was read");
        assertEquals(1, running.metaDataReads.get(), "an application that is loaded was not read");
    }
}
