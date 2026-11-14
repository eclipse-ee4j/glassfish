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

package org.glassfish.orb.http.fory.grpc.glassfish;

import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import javax.lang.model.SourceVersion;

import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.orb.http.fory.grpc.ForyGeneratedRuntime;
import org.glassfish.orb.http.fory.grpc.ForyGeneratedSchemaAdapter;
import org.glassfish.orb.http.fory.grpc.ForyGeneratedServiceRegistry;
import org.glassfish.orb.http.fory.grpc.ForyGrpcCatalog;
import org.glassfish.orb.http.fory.grpc.ForyGrpcSkeleton;
import org.glassfish.orb.http.fory.grpc.ForyIdlGenerator;
import org.glassfish.orb.http.fory.grpc.ForyRuntimeModelGenerator;
import org.glassfish.orb.http.fory.grpc.ForyTypeIds;
import org.glassfish.orb.http.fory.grpc.ForyViewMethods;
import org.glassfish.orb.http.protocol.Protocol;

/**
 * The remote business views of the deployed beans, as Fory IDL documents and
 * as the routes that serve them.
 *
 * <p>Everything published is built from what is deployed at that moment into
 * one immutable snapshot (the bindings, each carrying the route to its bean,
 * and the documents), and swapped in whole. A call that arrives during a
 * rebuild is served from the old snapshot, never from a half-built one.
 *
 * <p>A rebuild happens when something asks for a path that is not published
 * and either the deployment changed since the last one ({@link #markStale},
 * {@link #drop}) or the last one is more than {@link #MIN_REBUILD_INTERVAL_NANOS}
 * old, and also when something asks for a path that is published while the
 * deployment is known to have changed, so that a redeployment under the same
 * names is not served with the old bindings. The interval is what keeps a caller
 * who is not known yet from making the server build the whole index for every
 * request it sends. A view that was already built is reused for as long as its
 * application stays deployed, so a rebuild costs one compile only for a view it
 * has not seen.
 *
 * <p>Rebuilds are one at a time, and nothing that serves a call or reports a
 * deployment event waits for one for long: a request that finds a rebuild
 * running is answered from the snapshot that is published (a request that
 * knows the deployment changed waits a bounded time first), and {@link #drop}
 * never waits.
 *
 * <p>Each view is published completely or not at all: its document and every
 * one of its methods, or nothing and a warning.
 */
final class ForyEjbIndex {

    /** The least time between two rebuilds that nothing but a miss asked for. */
    static final long MIN_REBUILD_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1);

    /** How long a request that knows the deployment changed waits for a rebuild that is running. */
    static final long STALE_WAIT_MILLIS = 200;

    private static final String ALL = "\0all";

    private static final Logger LOG = System.getLogger(ForyEjbIndex.class.getName());

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    /** One remote business view of one deployed bean. */
    record DeployedView(String app, String module, String bean, String viewName, ClassLoader loader,
                        boolean stateful, String registryName) {

        DeployedView(String app, String module, String bean, String viewName, ClassLoader loader,
                     boolean stateful) {
            this(app, module, bean, viewName, loader, stateful, app);
        }
    }

    record ForyRoute(String app, String module, String bean, String view) {
    }

    private record ViewKey(String registryName, String app, String module, String bean, String viewName,
                           ClassLoader loader) {
    }

    private record Entry(String wirePath, ForyGeneratedServiceRegistry.Binding binding) {
    }

    /**
     * What one view contributes. {@code failed} views contribute nothing and
     * are not asked again until the deployment changes; a view that is merely
     * not published (a stateful bean) has no entries and no document.
     */
    private record Published(String service, String idlPath, String idl, List<Entry> entries, boolean failed) {

        static final Published NOTHING = new Published(null, null, null, List.of(), false);
        static final Published FAILED = new Published(null, null, null, List.of(), true);
    }

    private final Supplier<List<DeployedView>> deployed;

    private final LongSupplier clock;

    private final ForyGeneratedServiceRegistry foryRegistry = new ForyGeneratedServiceRegistry();

    private final ForyGrpcCatalog catalog = new ForyGrpcCatalog();

    private final ReentrantLock rebuilding = new ReentrantLock();

    /** Applications whose views are to be forgotten; applied by whoever next holds the lock. */
    private final Set<String> dropped = ConcurrentHashMap.newKeySet();

    /** Bumped by every rebuild and every drop. */
    private volatile long generation;

    /** Whether the deployment changed since the last rebuild; initially, nothing was built. */
    private volatile boolean stale = true;

    // Guarded by rebuilding.
    private long lastRebuild;

    /** Whether the last attempt to read the deployment failed; guarded by rebuilding. */
    private boolean readFailing;

    private Map<ViewKey, Published> built = Map.of();

    ForyEjbIndex(ApplicationRegistry applications) {
        this(() -> deployedViews(applications), System::nanoTime);
    }

    ForyEjbIndex(Supplier<List<DeployedView>> deployed, LongSupplier clock) {
        this.deployed = deployed;
        this.clock = clock;
        foryRegistry.onMiss(() -> refreshIfDue(false));
        foryRegistry.onStaleHit(() -> refreshIfDue(true));
        foryRegistry.onStale(() -> stale);
        catalog.onMiss(() -> refreshIfDue(false));
    }

    ForyGeneratedServiceRegistry foryRegistry() {
        return foryRegistry;
    }

    ForyGrpcCatalog catalog() {
        return catalog;
    }

    /** Something deployed, started or changed: the next miss rebuilds, however recent the last rebuild was. */
    void markStale() {
        stale = true;
    }

    /**
     * An application is being unloaded: forget its views, so that nothing this
     * index holds keeps the application's classes (and the models and runtimes
     * built over them) alive.
     *
     * <p>Never waits. If a rebuild is running, the forgetting is left to run
     * right after it, which also removes what that rebuild publishes.
     *
     * @param registryName the name the application is registered under, or
     *        {@code null} for every application
     */
    void drop(String registryName) {
        dropped.add(registryName == null ? ALL : registryName);
        stale = true;
        applyPendingDrops();
    }

    /** Forgets every application's views; see {@link #drop(String)}. */
    void dropAll() {
        drop(null);
    }

    private void applyPendingDrops() {
        while (!dropped.isEmpty() && rebuilding.tryLock()) {
            try {
                Set<String> names = new HashSet<>(dropped);
                dropped.removeAll(names);
                if (names.contains(ALL)) {
                    built = Map.of();
                } else {
                    Map<ViewKey, Published> remaining = new LinkedHashMap<>(built);
                    remaining.keySet().removeIf(key -> names.contains(key.registryName()));
                    built = remaining;
                }
                publishSnapshot(built);
                generation++;
            } finally {
                rebuilding.unlock();
            }
        }
    }

    /** Rebuilds now, waiting for a rebuild that is running; for the start of the server. */
    void refresh() {
        rebuilding.lock();
        try {
            rebuild();
        } finally {
            rebuilding.unlock();
            applyPendingDrops();
        }
    }

    /**
     * The hook for a path that is not published, or is published while the
     * deployment has changed.
     *
     * @param hit whether the path is published. Only then does a request that
     *        knows the deployment changed wait (a bounded time) for a rebuild
     *        that is running, because it is about to be answered with a binding
     *        that may be the old one. A miss has nothing to wait for and is
     *        answered at once, so that a caller who asks for paths that do not
     *        exist cannot hold request threads.
     */
    private void refreshIfDue(boolean hit) {
        long seen = generation;
        try {
            boolean locked = stale && hit
                    ? rebuilding.tryLock(STALE_WAIT_MILLIS, TimeUnit.MILLISECONDS)
                    : rebuilding.tryLock();
            if (!locked) {
                return; // a rebuild is running: answer from what is published
            }
        } catch (InterruptedException e) {
            // The flag is set again for whoever is serving the request, which
            // stops there instead of going on with it.
            Thread.currentThread().interrupt();
            return;
        }
        try {
            if (generation != seen && !stale) {
                return; // another request rebuilt while this one waited
            }
            if (readFailing && clock.getAsLong() - lastRebuild < MIN_REBUILD_INTERVAL_NANOS) {
                return; // the registry could not be read a moment ago
            }
            if (!stale && clock.getAsLong() - lastRebuild < MIN_REBUILD_INTERVAL_NANOS) {
                return;
            }
            rebuild();
        } finally {
            // On every path, including the early returns: a drop that found the
            // lock taken left its work for whoever held it.
            rebuilding.unlock();
            applyPendingDrops();
        }
    }

    // Called with the lock held.
    private void rebuild() {
        boolean retryFailures = stale;
        // Cleared before the deployment is read, so that a change announced
        // while it is being read is not lost; put back if it cannot be read.
        stale = false;
        List<DeployedView> views;
        try {
            views = deployed.get();
        } catch (RuntimeException | LinkageError e) {
            stale = retryFailures || stale;
            // Tried again no sooner than the interval, and said once per run of failures.
            LOG.log(readFailing ? Level.DEBUG : Level.WARNING, "cannot read the deployed applications: " + e);
            readFailing = true;
            lastRebuild = clock.getAsLong();
            return;
        }
        readFailing = false;
        lastRebuild = clock.getAsLong();
        Map<ViewKey, Published> next = new LinkedHashMap<>();
        for (DeployedView view : views) {
            ViewKey key = new ViewKey(view.registryName(), view.app(), view.module(), view.bean(),
                    view.viewName(), view.loader());
            Published published = built.get(key);
            if (published == null || published.failed() && retryFailures) {
                published = publish(view);
            }
            next.put(key, published);
        }
        refuseViewsThatShareAService(next);
        publishSnapshot(next);
        built = next;
        generation++;
        lastRebuild = clock.getAsLong();
    }

    /**
     * Publishes what the views contribute. The documents are checked and
     * swapped in first, so that a document that cannot be published fails
     * before anything has changed, and the bindings, which cannot fail, follow.
     */
    private void publishSnapshot(Map<ViewKey, Published> all) {
        Map<String, ForyGeneratedServiceRegistry.Binding> bindings = new HashMap<>();
        Map<String, String> documents = new LinkedHashMap<>();
        for (Published published : all.values()) {
            for (Entry entry : published.entries()) {
                bindings.put(entry.wirePath(), entry.binding());
            }
            if (published.idl() != null) {
                documents.put(published.idlPath(), published.idl());
            }
        }
        catalog.replaceAll(documents);
        foryRegistry.replaceAll(bindings);
    }

    /**
     * Two views that would publish the same service (two interfaces of one
     * bean with the same simple name, from different packages) would be one
     * service for a client, and a call would reach whichever came last. Neither
     * is published.
     */
    private void refuseViewsThatShareAService(Map<ViewKey, Published> all) {
        Map<String, List<ViewKey>> byService = new HashMap<>();
        all.forEach((key, published) -> {
            if (published.service() != null) {
                byService.computeIfAbsent(published.service(), s -> new ArrayList<>()).add(key);
            }
        });
        byService.forEach((service, keys) -> {
            if (keys.size() > 1) {
                LOG.log(Level.WARNING, "not publishing " + keys.stream().map(ViewKey::viewName).toList()
                        + ": they would all be the Fory service " + service);
                keys.forEach(key -> all.put(key, Published.FAILED));
            }
        });
    }

    /** Never throws: a view that cannot be published is logged and left out. */
    private Published publish(DeployedView view) {
        try {
            return build(view);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            LOG.log(Level.WARNING, "cannot publish the Fory service for " + view.app() + '/' + view.module()
                    + '/' + view.bean() + '/' + view.viewName() + ": " + e);
            return Published.FAILED;
        }
    }

    private Published build(DeployedView deployedView) throws ReflectiveOperationException {
        String app = deployedView.app();
        String module = deployedView.module();
        String bean = deployedView.bean();
        if (app == null || module == null || bean == null) {
            throw new IllegalArgumentException("the application, module or bean has no name");
        }
        if (deployedView.loader() == null) {
            // Between the unload of the application's modules and its being
            // marked as not loaded; it is about to go, and is not news.
            LOG.log(Level.DEBUG, "not publishing " + bean + ": its class loader is gone");
            return Published.NOTHING;
        }
        if (deployedView.stateful()) {
            // Every call to a stateful bean needs a session, and this endpoint
            // has no way to be given one: publishing the view would only
            // publish something every call to which fails.
            LOG.log(Level.DEBUG, "not publishing the stateful bean " + bean);
            return Published.NOTHING;
        }
        Class<?> view = Class.forName(deployedView.viewName(), false, deployedView.loader());
        String packageName = packageName(app, module, bean, view);
        String serviceName = serviceSimpleName(view);
        String service = packageName + '.' + serviceName;

        // The document first: the IDL generator is the source of truth for
        // method ordering, overload rejection, portable types and names, and
        // it refuses before anything has been built.
        String idl = ForyIdlGenerator.generate(packageName, serviceName, view);
        ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of(service, view);
        ForyRoute route = new ForyRoute(app, module, bean, deployedView.viewName());

        // Every method is built into this list, and nothing is published
        // unless all of them were: a view with some of its methods served and
        // the rest missing would answer UNIMPLEMENTED for calls the document
        // advertises.
        List<Entry> entries = new ArrayList<>();
        for (Method method : ForyViewMethods.of(view)) {
            String rpcName = Character.toUpperCase(method.getName().charAt(0)) + method.getName().substring(1);
            Class<?> parameter = method.getParameterCount() == 0 ? void.class : method.getParameterTypes()[0];
            var models = ForyRuntimeModelGenerator.unary(packageName, method.getName(), parameter,
                    method.getReturnType());
            // The ids come from the names in the published IDL, so the
            // document a client generated from and the runtime agree.
            var binding = new ForyGeneratedServiceRegistry.Binding('/' + service + '/' + method.getName(), skeleton,
                    models,
                    new ForyGeneratedRuntime(models,
                            ForyTypeIds.of(packageName, rpcName + "Request"),
                            ForyTypeIds.of(packageName, rpcName + "Response")),
                    ForyGeneratedSchemaAdapter.unary(models.request(), models.response()), route);
            entries.add(new Entry(Protocol.CONTEXT_PATH + "/fory/" + service + '/' + rpcName, binding));
        }
        String idlPath = Protocol.CONTEXT_PATH + ForyGrpcCatalog.PREFIX
                + pathPart(app) + '/' + pathPart(module) + '/' + pathPart(bean) + '/'
                + pathPart(deployedView.viewName()) + ".fdl";
        return new Published(service, idlPath, idl, List.copyOf(entries), false);
    }

    /**
     * The name of the service a view is published as: the Java package of its
     * messages and the service itself.
     *
     * @return {@code glassfish.<app>.<module>.<bean>.<view>.<view>}, each part escaped
     */
    static String serviceName(String app, String module, String bean, Class<?> view) {
        return packageName(app, module, bean, view) + '.' + serviceSimpleName(view);
    }

    /**
     * The package of the messages of one view. It is one package for each view
     * and not for each module: the messages are named after the methods, so
     * two beans of a module with a method of the same name would otherwise
     * declare the same message, with the same type id, twice.
     */
    static String packageName(String app, String module, String bean, Class<?> view) {
        return "glassfish." + escape(app) + '.' + escape(module) + '.' + escape(bean) + '.'
                + escape(view.getSimpleName());
    }

    static String serviceSimpleName(Class<?> view) {
        return escape(view.getSimpleName());
    }

    /**
     * Turns any name into one made of letters, digits and underscores, and
     * does it reversibly, so that two different names never become the same
     * one: an underscore becomes two, any other character becomes an
     * underscore and its code in hexadecimal ({@code -} is {@code _2D}).
     * Replacing every such character with one underscore, as this once did,
     * made {@code a-b} and {@code a_b} the same service.
     */
    static String escape(String name) {
        StringBuilder out = new StringBuilder(name.length() + 8);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                out.append("__");
            } else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') {
                out.append(c);
            } else {
                escapeInto(out, c);
            }
        }
        // A name that would not be a Java identifier as it is - it starts with
        // a digit, or it is a keyword - has its first character escaped too, so
        // that 2fa-app and a module named default can be published.
        if (out.length() > 0 && (Character.isDigit(out.charAt(0)) || SourceVersion.isKeyword(out))) {
            char first = name.charAt(0);
            StringBuilder escaped = new StringBuilder(out.length() + 2);
            escapeInto(escaped, first);
            return escaped.append(out, 1, out.length()).toString();
        }
        return out.toString();
    }

    private static void escapeInto(StringBuilder out, char c) {
        if (c <= 0xFF) {
            out.append('_').append(HEX[c >>> 4]).append(HEX[c & 0x0F]);
        } else {
            out.append("_u").append(HEX[c >>> 12]).append(HEX[(c >>> 8) & 0x0F])
                    .append(HEX[(c >>> 4) & 0x0F]).append(HEX[c & 0x0F]);
        }
    }

    private static String pathPart(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Reads the remote business views of the applications deployed now. */
    static List<DeployedView> deployedViews(ApplicationRegistry applications) {
        List<DeployedView> result = new ArrayList<>();
        for (String name : applications.getAllApplicationNames()) {
            ApplicationInfo info = applications.get(name);
            // An application that is being unloaded is still registered until
            // its undeployment completes, and its classes are about to go.
            if (info == null || !info.isLoaded()) {
                continue;
            }
            Application application = info.getMetaData(Application.class);
            if (application == null) {
                continue;
            }
            for (EjbBundleDescriptor bundle : application.getBundleDescriptors(EjbBundleDescriptor.class)) {
                String module = bundle.getModuleDescriptor().getModuleName();
                for (EjbDescriptor ejb : bundle.getEjbs()) {
                    Set<String> views = ejb.getRemoteBusinessClassNames();
                    if (views == null) {
                        continue;
                    }
                    ClassLoader loader = EjbContainerUtilImpl.getInstance().getClassLoader(ejb.getUniqueId());
                    boolean stateful = ejb instanceof EjbSessionDescriptor session && session.isStateful();
                    for (String viewName : views) {
                        result.add(new DeployedView(application.getRegistrationName(), module, ejb.getName(),
                                viewName, loader, stateful, name));
                    }
                }
            }
        }
        return result;
    }
}
