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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;

import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.jvnet.hk2.annotations.Service;

/**
 * Resolves a bean's four-part name to the id the container knows it by.
 *
 * <p>The container is addressed entirely by {@code ejbId}:
 * {@code EjbContainerUtil} offers {@code getContainer(long)},
 * {@code getDescriptor(long)} and {@code getClassLoader(long)}, and nothing
 * keyed by name. The IIOP path never needs the mapping, because the id travels
 * inside the object key of a reference the server itself minted. A client that
 * builds a locator from a name rather than from a lookup holds no such
 * reference, so the mapping has to exist somewhere - here.
 *
 * <p>Built lazily from {@link ApplicationRegistry} rather than from deployment
 * events. Events are the more obvious mechanism and the wrong one: their
 * ordering relative to the first invocation is not guaranteed, and an index
 * that is merely usually populated fails as a 404 indistinguishable from a
 * misspelled bean name. A miss here refreshes and retries, so the worst case
 * is a slow first call rather than a wrong answer.
 *
 * <p>The refresh on a miss is rate limited. A miss is something any caller can
 * cause, including one that has not authenticated, and a rebuild walks every
 * deployed application; without a limit a stream of requests for a bean that
 * does not exist is a stream of full rebuilds. Inside
 * {@link #MIN_REFRESH_INTERVAL_NANOS} of the last rebuild a miss therefore
 * rebuilds only if the registry no longer holds the very applications the index
 * was built from - one was deployed, undeployed or redeployed - which is
 * checked by identity and costs a walk of the application names, not of their
 * beans. So a bean deployed a moment after a miss is not a 404 for the next
 * second.
 *
 * <p>A rebuild is published whole. Readers see either the previous index or
 * the new one, never the empty map a clear-then-fill would show in between.
 */
@Service
@Singleton
public class EjbNameIndex {

    private static final Logger LOG = System.getLogger(EjbNameIndex.class.getName());

    /** How long after a rebuild a miss is answered without rebuilding again. */
    static final long MIN_REFRESH_INTERVAL_NANOS = 1_000_000_000L;

    @Inject
    private ApplicationRegistry applications;

    private final LongSupplier clock;
    private final long minRefreshIntervalNanos;

    private volatile Snapshot current = Snapshot.EMPTY;

    /** Guarded by {@code this}: when the last rebuild finished, valid once {@code rebuilt} is set. */
    private long lastRebuildNanos;
    private boolean rebuilt;
    private int rebuilds;

    public EjbNameIndex() {
        this(null, System::nanoTime, MIN_REFRESH_INTERVAL_NANOS);
    }

    /** For tests: a registry, a clock and a refresh interval of their own. */
    EjbNameIndex(ApplicationRegistry applications, LongSupplier clock, long minRefreshIntervalNanos) {
        this.applications = applications;
        this.clock = clock;
        this.minRefreshIntervalNanos = minRefreshIntervalNanos;
    }

    /**
     * @param appName the application name
     * @param moduleName the module within it, or null if the caller does not know it
     * @param beanName the bean's ejb-name
     * @return the container's id for that bean, or null if there is no such
     *         bean, or if the name does not single one out
     */
    public Long lookup(String appName, String moduleName, String beanName) {
        Long id = find(appName, moduleName, beanName);
        if (id != null) {
            return id;
        }
        refreshIfDue();
        return find(appName, moduleName, beanName);
    }

    /**
     * Exact match first, then module-agnostic.
     *
     * <p>The fallback is not laziness. A client reaching a bean by name may
     * have got that name from {@code java:global/<module>/<bean>}, where the
     * application and the module are the same word, or from
     * {@code java:global/<app>/<module>/<bean>}, where they are not - and for
     * a standalone jar the module name the deployment layer assigns is not
     * necessarily either of them. Requiring the caller to have guessed it
     * turns a deployed bean into a 404, which is what happened the first time
     * this ran against a real server.
     *
     * <p>The fallback only answers when exactly one bean of that name exists in
     * the application. When two modules each have one, the module-less key is
     * ambiguous and resolves to nothing: guessing would run the call on
     * whichever bean happened to be indexed first, and a caller that named the
     * wrong module would be answered by the other module's bean.
     */
    private Long find(String appName, String moduleName, String beanName) {
        Snapshot snapshot = current;
        String exact = key(appName, moduleName, beanName);
        if (snapshot.ambiguous().contains(exact)) {
            // Only a module-less key can be ambiguous, and this caller named
            // no module.
            return null;
        }
        Long id = snapshot.ids().get(exact);
        if (id != null) {
            return id;
        }
        String unqualified = key(appName, null, beanName);
        return snapshot.ambiguous().contains(unqualified) ? null : snapshot.ids().get(unqualified);
    }

    /**
     * A miss: rebuild unless the index was rebuilt a moment ago and the
     * deployed applications are the same ones it was built from.
     */
    private synchronized void refreshIfDue() {
        if (rebuilt && clock.getAsLong() - lastRebuildNanos < minRefreshIntervalNanos
                && sameApplications(current.applications(), deployed().values())) {
            return;
        }
        rebuild();
    }

    /**
     * Drops the entries that name one bean, for a caller that found its
     * container gone. The index is not rebuilt: the next miss does that, within
     * the rate limit, because a different set of applications is registered by
     * then.
     *
     * @param ejbId the id that no longer has a container
     */
    synchronized void forget(long ejbId) {
        Snapshot snapshot = current;
        Map<String, Long> kept = new HashMap<>(snapshot.ids());
        kept.values().removeIf(id -> id == ejbId);
        current = new Snapshot(Map.copyOf(kept), snapshot.ambiguous(), snapshot.applications());
    }

    /**
     * The deployed applications, copied while the registry's own monitor is
     * held: its {@code add} and {@code remove} are synchronized on it, and its
     * name set is the live key set of a plain map, so iterating that set while a
     * deployment adds to it fails.
     */
    private Map<String, ApplicationInfo> deployed() {
        Map<String, ApplicationInfo> copy = new LinkedHashMap<>();
        synchronized (applications) {
            for (String name : new ArrayList<>(applications.getAllApplicationNames())) {
                ApplicationInfo info = applications.get(name);
                if (info != null) {
                    copy.put(name, info);
                }
            }
        }
        return copy;
    }

    private static boolean sameApplications(Set<ApplicationInfo> built, Collection<ApplicationInfo> now) {
        if (built.size() != now.size()) {
            return false;
        }
        for (ApplicationInfo info : now) {
            if (!built.contains(info)) {
                return false;
            }
        }
        return true;
    }

    /** @return how many times the index has been rebuilt from the registry, for tests */
    synchronized int rebuilds() {
        return rebuilds;
    }

    /**
     * Rebuilds the index from what is deployed right now, whatever the last
     * rebuild was. For a caller that knows the index is stale: a bean it named
     * is gone.
     *
     * <p>Says what it found at debug level. A miss here presents as a 404 on
     * an invocation, which is indistinguishable from a bean that was never
     * deployed and from a name spelled wrong, so the way to tell the three
     * apart from the outside is for this to say what it saw.
     */
    public synchronized void refresh() {
        rebuild();
    }

    private void rebuild() {
        Entries entries = new Entries();
        Map<String, ApplicationInfo> deployed = deployed();
        entries.applications.addAll(deployed.values());
        rebuilds++;
        for (Map.Entry<String, ApplicationInfo> entry : deployed.entrySet()) {
            String name = entry.getKey();
            ApplicationInfo info = entry.getValue();
            Application application = info.getMetaData(Application.class);
            if (application == null) {
                LOG.log(Level.DEBUG, "application {0} carries no Application metadata", name);
                continue;
            }
            int before = entries.size();
            index(application, entries);
            LOG.log(Level.DEBUG, "indexed {0} bean key(s) for application {1} (registration name {2})",
                    entries.size() - before, name, application.getRegistrationName());
        }
        publish(entries);
        LOG.log(Level.DEBUG, "EJB name index rebuilt: {0}", current.ids().keySet());
    }

    /** Swaps the whole index in one write. */
    synchronized void publish(Entries entries) {
        current = entries.build();
        lastRebuildNanos = clock.getAsLong();
        rebuilt = true;
    }

    private void index(Application application, Entries into) {
        var bundles = application.getBundleDescriptors(EjbBundleDescriptor.class);
        LOG.log(Level.DEBUG, "application {0} has {1} EJB bundle(s)",
                application.getRegistrationName(), bundles.size());
        for (EjbBundleDescriptor bundle : bundles) {
            String moduleName = bundle.getModuleDescriptor().getModuleName();
            for (EjbDescriptor ejb : bundle.getEjbs()) {
                into.add(application.getRegistrationName(), moduleName, ejb.getName(), ejb.getUniqueId());
            }
        }
    }

    private static String key(String appName, String moduleName, String beanName) {
        return appName + '/' + (moduleName == null ? "*" : moduleName) + '/' + beanName;
    }

    /** @return how many keys are currently indexed, for diagnostics */
    public int size() {
        return current.ids().size();
    }

    /** What an index holds: the keys that name one bean, and the module-less keys that name several. */
    private record Snapshot(Map<String, Long> ids, Set<String> ambiguous, Set<ApplicationInfo> applications) {
        static final Snapshot EMPTY = new Snapshot(Map.of(), Set.of(), Set.of());
    }

    /** The beans of one rebuild, collected before anything is published. */
    static final class Entries {

        private final Map<String, Long> ids = new HashMap<>();
        private final Set<String> ambiguous = new HashSet<>();
        private final Set<ApplicationInfo> applications = Collections.newSetFromMap(new IdentityHashMap<>());

        /**
         * Records a bean under its module, and under the application alone so a
         * client that knows only the application and the bean is still served.
         * When a second bean of the same name turns up in the application, the
         * module-less key stops naming either.
         */
        void add(String appName, String moduleName, String beanName, long ejbId) {
            String exact = key(appName, moduleName, beanName);
            Long replaced = ids.put(exact, ejbId);
            if (replaced != null && replaced != ejbId) {
                // The same name now means a different bean: neither is singled out.
                ambiguous.add(exact);
            }
            String unqualified = key(appName, null, beanName);
            Long first = ids.putIfAbsent(unqualified, ejbId);
            if (first != null && first != ejbId) {
                ambiguous.add(unqualified);
            }
        }

        int size() {
            return ids.size();
        }

        private Snapshot build() {
            return new Snapshot(Map.copyOf(ids), Set.copyOf(ambiguous), applications);
        }
    }
}
