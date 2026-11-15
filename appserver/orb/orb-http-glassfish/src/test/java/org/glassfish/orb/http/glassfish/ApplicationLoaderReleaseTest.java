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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.glassfish.api.event.EventListener;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.internal.deployment.Deployment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Releasing the codecs' per-loader state when an application is unloaded, over
 * an event built by hand and a recording stand-in for the release.
 *
 * <p>What this does not cover: the real deployment sending the event from
 * inside {@code ApplicationInfo.unload}, HK2 instantiating the listener, and the
 * codecs' own release (tested in the protocol and Fory modules).
 */
class ApplicationLoaderReleaseTest {

    private final List<ClassLoader> released = new ArrayList<>();
    private final ApplicationLoaderRelease listener = new ApplicationLoaderRelease(released::add);

    private static ClassLoader loader() {
        return new URLClassLoader(new URL[0], null);
    }

    private static ModuleInfo module(ClassLoader moduleLoader, Set<ClassLoader> others) {
        return new ModuleInfo(null, "module", List.of(), null) {
            @Override
            public ClassLoader getModuleClassLoader() {
                return moduleLoader;
            }

            @Override
            public Set<ClassLoader> getClassLoaders() {
                return others;
            }
        };
    }

    @Test
    @DisplayName("an unloaded application's loader is released")
    void theApplicationLoaderIsReleased() {
        ClassLoader app = loader();
        ApplicationInfo info = new ApplicationInfo(null, null, "app");
        info.setAppClassLoader(app);

        listener.event(new EventListener.Event<>(Deployment.APPLICATION_UNLOADED, info));

        assertEquals(1, released.size(), "released: " + released);
        assertSame(app, released.get(0));
    }

    @Test
    @DisplayName("the loaders of its modules are released too, each once")
    void theModuleLoadersAreReleased() {
        ClassLoader app = loader();
        ClassLoader ejbModule = loader();
        ClassLoader extra = loader();
        ApplicationInfo info = new ApplicationInfo(null, null, "app");
        info.setAppClassLoader(app);
        // The module loader is also among the module's loaders, as it is after a load.
        info.addModule(module(ejbModule, Set.of(ejbModule, extra)));

        listener.event(new EventListener.Event<>(Deployment.APPLICATION_UNLOADED, info));

        assertEquals(3, released.size(), "released: " + released);
        assertTrue(released.containsAll(List.of(app, ejbModule, extra)), "released: " + released);
    }

    @Test
    @DisplayName("a module whose loaders were already cleaned does not break the release")
    void cleanedModulesAreSkipped() {
        ClassLoader app = loader();
        ApplicationInfo info = new ApplicationInfo(null, null, "app");
        info.setAppClassLoader(app);
        info.addModule(module(null, null));

        listener.event(new EventListener.Event<>(Deployment.APPLICATION_UNLOADED, info));

        assertEquals(List.of(app), released);
    }

    @Test
    @DisplayName("other deployment events, and an unload with no application, release nothing")
    void otherEventsAreIgnored() {
        ApplicationInfo info = new ApplicationInfo(null, null, "app");
        info.setAppClassLoader(loader());

        listener.event(new EventListener.Event<>(Deployment.APPLICATION_STARTED, info));
        listener.event(new EventListener.Event<>(Deployment.UNDEPLOYMENT_SUCCESS, info));
        listener.event(new EventListener.Event<>(Deployment.APPLICATION_UNLOADED, null));

        assertTrue(released.isEmpty(), "released: " + released);
    }

    @Test
    @DisplayName("a release that fails does not reach the deployment, and the other loaders are still tried")
    void aFailureDoesNotEscape() {
        ClassLoader app = loader();
        ClassLoader ejbModule = loader();
        List<ClassLoader> tried = new ArrayList<>();
        ApplicationLoaderRelease failing = new ApplicationLoaderRelease(l -> {
            tried.add(l);
            throw new IllegalStateException("cannot release");
        });
        ApplicationInfo info = new ApplicationInfo(null, null, "app");
        info.setAppClassLoader(app);
        info.addModule(module(ejbModule, null));

        failing.event(new EventListener.Event<>(Deployment.APPLICATION_UNLOADED, info));

        assertEquals(2, tried.size(), "tried: " + tried);
        assertTrue(tried.containsAll(List.of(app, ejbModule)), "tried: " + tried);
    }
}
