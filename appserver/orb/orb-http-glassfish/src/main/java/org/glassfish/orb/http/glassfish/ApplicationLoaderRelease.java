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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.orb.http.protocol.Marshallers;
import org.jvnet.hk2.annotations.Service;

/**
 * Lets the codecs go of an application's class loaders when the application is
 * unloaded.
 *
 * <p>The codecs keep per-loader state (discovery results and, for the Fory
 * codec, its readers and writers), and that state refers back to the loader it
 * is keyed by, so a weak key alone never frees it: without this every
 * undeployed or redeployed application would keep its class loader, and
 * everything the loader holds, alive. {@link Marshallers#release} documents the
 * container adapter as the caller; this is that call.
 *
 * <p>Registered for {@code APPLICATION_UNLOADED}, which is sent from inside the
 * application's own unload - before its loaders are cleaned - and so also when
 * an application is redeployed, whose old version is unloaded first. A loader
 * that is released while a call is still running on it is rebuilt by that
 * call, which is the documented behaviour of the release.
 */
@Service
@Singleton
public class ApplicationLoaderRelease implements EventListener, PostConstruct, PreDestroy {

    private static final Logger LOG = System.getLogger(ApplicationLoaderRelease.class.getName());

    @Inject
    private Events events;

    private final Consumer<ClassLoader> release;

    /** Used by the container. */
    public ApplicationLoaderRelease() {
        this(Marshallers::release);
    }

    /** For tests: what is done with each loader instead of releasing it from the codecs. */
    ApplicationLoaderRelease(Consumer<ClassLoader> release) {
        this.release = release;
    }

    @Override
    public void postConstruct() {
        events.register(this);
    }

    @Override
    public void preDestroy() {
        try {
            events.unregister(this);
        } catch (RuntimeException e) {
            LOG.log(Level.DEBUG, "could not unregister the class loader release", e);
        }
    }

    @Override
    public void event(Event<?> event) {
        if (!event.is(Deployment.APPLICATION_UNLOADED) || !(event.hook() instanceof ApplicationInfo info)) {
            return;
        }
        // Whatever goes wrong here must not reach the deployment that is
        // unloading: the application is being undeployed whether or not the
        // codecs could let go of it.
        try {
            Set<ClassLoader> loaders = Collections.newSetFromMap(new IdentityHashMap<>());
            add(loaders, info.getAppClassLoader());
            for (ModuleInfo module : info.getModuleInfos()) {
                add(loaders, module.getModuleClassLoader());
                Set<ClassLoader> more = module.getClassLoaders();
                if (more != null) {
                    for (ClassLoader loader : more) {
                        add(loaders, loader);
                    }
                }
            }
            for (ClassLoader loader : loaders) {
                try {
                    release.accept(loader);
                } catch (RuntimeException | LinkageError e) {
                    // One loader that cannot be let go of must not keep the others.
                    LOG.log(Level.WARNING, "could not release the codecs' state for a loader of application "
                            + info.getName(), e);
                }
            }
        } catch (RuntimeException | LinkageError e) {
            LOG.log(Level.WARNING, "could not release the codecs' state for application " + info.getName(), e);
        }
    }

    private static void add(Set<ClassLoader> into, ClassLoader loader) {
        if (loader != null) {
            into.add(loader);
        }
    }
}
