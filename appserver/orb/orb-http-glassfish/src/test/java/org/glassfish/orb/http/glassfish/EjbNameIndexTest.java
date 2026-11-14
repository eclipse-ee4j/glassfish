/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.orb.http.glassfish;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The name index over a registry that is not the server's: one that counts how
 * often it is walked, and a clock the test moves by hand.
 *
 * <p>What this does not cover: an index built from real deployed applications
 * (the descriptors need a running deployment), and the swap of a rebuilt index
 * under concurrent readers, which has no deterministic hook.
 */
class EjbNameIndexTest {

    private static final long INTERVAL = 1_000;

    private final Map<String, ApplicationInfo> apps = new HashMap<>();
    private final AtomicLong now = new AtomicLong(5_000);
    private final EjbNameIndex index = new EjbNameIndex(new ApplicationRegistry() {
        @Override
        public Set<String> getAllApplicationNames() {
            // The live key set, as the real registry's is.
            return apps.keySet();
        }

        @Override
        public ApplicationInfo get(String name) {
            return apps.get(name);
        }
    }, now::get, INTERVAL);

    private static ApplicationInfo application(String name) {
        return new ApplicationInfo(null, null, name);
    }

    @Test
    @DisplayName("a stream of lookups for a bean that does not exist rebuilds the index once, not once per lookup")
    void missesDoNotRebuildEachTime() {
        for (int i = 0; i < 100; i++) {
            assertNull(index.lookup("app", "module", "NoSuchBean"));
        }

        assertEquals(1, index.rebuilds());
    }

    @Test
    @DisplayName("a miss inside the interval rebuilds when an application was deployed since")
    void aDeploymentInsideTheIntervalIsSeen() {
        assertNull(index.lookup("app", "module", "NoSuchBean"));
        assertEquals(1, index.rebuilds());

        apps.put("late", application("late"));
        assertNull(index.lookup("app", "module", "NoSuchBean"));
        assertEquals(2, index.rebuilds());

        // The same applications as the index was built from: no further rebuild.
        assertNull(index.lookup("app", "module", "NoSuchBean"));
        assertEquals(2, index.rebuilds());
    }

    @Test
    @DisplayName("a miss inside the interval rebuilds when an application was redeployed under the same name")
    void aRedeploymentInsideTheIntervalIsSeen() {
        apps.put("app", application("app"));
        assertNull(index.lookup("app", "module", "NoSuchBean"));
        assertEquals(1, index.rebuilds());

        apps.put("app", application("app"));
        assertNull(index.lookup("app", "module", "NoSuchBean"));

        assertEquals(2, index.rebuilds());
    }

    @Test
    @DisplayName("a miss inside the interval rebuilds when an application was undeployed")
    void anUndeploymentInsideTheIntervalIsSeen() {
        apps.put("app", application("app"));
        assertNull(index.lookup("app", "module", "NoSuchBean"));

        apps.remove("app");
        assertNull(index.lookup("app", "module", "NoSuchBean"));

        assertEquals(2, index.rebuilds());
    }

    @Test
    @DisplayName("a deployment that races a rebuild does not fail it: the names are copied before they are read")
    void aConcurrentDeploymentDoesNotBreakARebuild() {
        apps.put("a", application("a"));
        apps.put("b", application("b"));
        ApplicationRegistry racing = new ApplicationRegistry() {
            private boolean added;

            @Override
            public Set<String> getAllApplicationNames() {
                return apps.keySet();
            }

            @Override
            public ApplicationInfo get(String name) {
                if (!added) {
                    // What a deployment on another thread does to the live name set.
                    added = true;
                    apps.put("c", application("c"));
                }
                return apps.get(name);
            }
        };

        new EjbNameIndex(racing, now::get, INTERVAL).refresh();
    }

    @Test
    @DisplayName("a container that has gone drops only its own entries, and costs no rebuild")
    void forgettingAnIdDropsOnlyItsEntries() {
        EjbNameIndex.Entries entries = new EjbNameIndex.Entries();
        entries.add("app", "ejb1", "Counter", 11);
        entries.add("app", "ejb1", "Other", 13);
        index.publish(entries);

        index.forget(11);

        assertNull(index.lookup("app", "ejb1", "Counter"));
        assertEquals(13L, index.lookup("app", "ejb1", "Other"));
        assertEquals(0, index.rebuilds());
    }

    @Test
    @DisplayName("the same name registered twice with different ids names neither")
    void aNameRegisteredTwiceIsAmbiguous() {
        EjbNameIndex.Entries entries = new EjbNameIndex.Entries();
        entries.add("app", "ejb1", "Counter", 11);
        entries.add("app", "ejb1", "Counter", 12);
        index.publish(entries);

        assertNull(index.lookup("app", "ejb1", "Counter"));
    }

    // Characterization test: it passes without the change under test and pins
    // behaviour that is meant to stay.
    @Test
    @DisplayName("a miss after the interval rebuilds again, so a bean deployed since is found")
    void aMissAfterTheIntervalRebuildsAgain() {
        assertNull(index.lookup("app", "module", "NoSuchBean"));
        assertEquals(1, index.rebuilds());

        now.addAndGet(INTERVAL);
        assertNull(index.lookup("app", "module", "NoSuchBean"));

        assertEquals(2, index.rebuilds());
    }

    // Characterization test: it passes without the change under test and pins
    // behaviour that is meant to stay.
    @Test
    @DisplayName("a forced refresh rebuilds whatever the interval says")
    void aForcedRefreshAlwaysRebuilds() {
        index.refresh();
        index.refresh();

        assertEquals(2, index.rebuilds());
    }

    // Characterization test: it passes without the change under test and pins
    // behaviour that is meant to stay.
    @Test
    @DisplayName("a bean is found by its module, and without one when its name is unique in the application")
    void aBeanIsFound() {
        EjbNameIndex.Entries entries = new EjbNameIndex.Entries();
        entries.add("app", "ejb1", "Counter", 11);
        index.publish(entries);

        assertEquals(11L, index.lookup("app", "ejb1", "Counter"));
        assertEquals(11L, index.lookup("app", null, "Counter"));
        // A module name the client guessed wrong still reaches the only bean of that name.
        assertEquals(11L, index.lookup("app", "guessed", "Counter"));
        assertEquals(0, index.rebuilds());
    }

    @Test
    @DisplayName("two modules with a bean of the same name: each is reached by its module, and the module-less name refuses")
    void aSharedNameIsAmbiguousWithoutAModule() {
        EjbNameIndex.Entries entries = new EjbNameIndex.Entries();
        entries.add("app", "ejb1", "Counter", 11);
        entries.add("app", "ejb2", "Counter", 12);
        entries.add("app", "ejb1", "Other", 13);
        index.publish(entries);

        assertEquals(11L, index.lookup("app", "ejb1", "Counter"));
        assertEquals(12L, index.lookup("app", "ejb2", "Counter"));
        // Neither module was named, or a third one was: no bean is singled out.
        assertNull(index.lookup("app", null, "Counter"));
        assertNull(index.lookup("app", "ejb3", "Counter"));
        // An unrelated bean is not affected.
        assertEquals(13L, index.lookup("app", "guessed", "Other"));
    }

    // Characterization test: it passes without the change under test and pins
    // behaviour that is meant to stay.
    @Test
    @DisplayName("an application's beans are not reachable through another application's name")
    void applicationsAreSeparate() {
        EjbNameIndex.Entries entries = new EjbNameIndex.Entries();
        entries.add("app", "ejb1", "Counter", 11);
        index.publish(entries);

        assertNull(index.lookup("other", "ejb1", "Counter"));
    }
}
