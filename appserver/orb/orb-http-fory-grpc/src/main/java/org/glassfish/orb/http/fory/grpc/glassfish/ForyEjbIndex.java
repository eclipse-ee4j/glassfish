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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

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
 * old. The second condition is what keeps a caller who is not known yet from
 * making the server build the whole index for every request it sends. A view
 * that was already built is reused for as long as its application stays
 * deployed, so a rebuild costs one compile only for a view it has not seen.
 *
 * <p>Each view is published completely or not at all: its document and every
 * one of its methods, or nothing and a warning.
 */
final class ForyEjbIndex {

    /** The least time between two rebuilds that nothing but a miss asked for. */
    static final long MIN_REBUILD_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1);

    private static final Logger LOG = System.getLogger(ForyEjbIndex.class.getName());

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    /** One remote business view of one deployed bean. */
    record DeployedView(String app, String module, String bean, String viewName, ClassLoader loader,
                        boolean stateful) {
    }

    record ForyRoute(String app, String module, String bean, String view) {
    }

    private record ViewKey(String app, String module, String bean, String viewName, ClassLoader loader) {
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

    /** Bumped by every rebuild and every drop. */
    private volatile long generation;

    /** Whether the deployment changed since the last rebuild; initially, nothing was built. */
    private volatile boolean stale = true;

    // Guarded by rebuilding.
    private long lastRebuild;

    private Map<ViewKey, Published> built = Map.of();

    ForyEjbIndex(ApplicationRegistry applications) {
        this(() -> deployedViews(applications), System::nanoTime);
    }

    ForyEjbIndex(Supplier<List<DeployedView>> deployed, LongSupplier clock) {
        this.deployed = deployed;
        this.clock = clock;
        foryRegistry.onMiss(this::refreshIfDue);
        catalog.onMiss(this::refreshIfDue);
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
     * An application was unloaded: forget everything at once, so that nothing
     * this index holds keeps the application's classes (and the models and
     * runtimes built over them) alive, and so that a redeployed application
     * is not served with the signatures of the one it replaced.
     */
    void drop() {
        rebuilding.lock();
        try {
            built = Map.of();
            foryRegistry.clear();
            catalog.clear();
            stale = true;
            generation++;
        } finally {
            rebuilding.unlock();
        }
    }

    /** Rebuilds now. */
    void refresh() {
        rebuilding.lock();
        try {
            rebuild();
        } finally {
            rebuilding.unlock();
        }
    }

    /** The hook for a path that is not published; see the class comment. */
    private void refreshIfDue() {
        long seen = generation;
        rebuilding.lock();
        try {
            if (generation != seen && !stale) {
                return; // another request rebuilt while this one waited
            }
            if (!stale && clock.getAsLong() - lastRebuild < MIN_REBUILD_INTERVAL_NANOS) {
                return;
            }
            rebuild();
        } finally {
            rebuilding.unlock();
        }
    }

    // Called with the lock held.
    private void rebuild() {
        boolean retryFailures = stale;
        stale = false;
        lastRebuild = clock.getAsLong();
        List<DeployedView> views;
        try {
            views = deployed.get();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "cannot read the deployed applications: " + e);
            return;
        }
        Map<ViewKey, Published> next = new LinkedHashMap<>();
        for (DeployedView view : views) {
            ViewKey key = new ViewKey(view.app(), view.module(), view.bean(), view.viewName(), view.loader());
            Published published = built.get(key);
            if (published == null || published.failed() && retryFailures) {
                published = publish(view);
            }
            next.put(key, published);
        }
        refuseViewsThatShareAService(next);

        Map<String, ForyGeneratedServiceRegistry.Binding> bindings = new HashMap<>();
        Map<String, String> documents = new LinkedHashMap<>();
        for (Published published : next.values()) {
            for (Entry entry : published.entries()) {
                bindings.put(entry.wirePath(), entry.binding());
            }
            if (published.idl() != null) {
                documents.put(published.idlPath(), published.idl());
            }
        }
        foryRegistry.replaceAll(bindings);
        catalog.replaceAll(documents);
        built = next;
        generation++;
        lastRebuild = clock.getAsLong();
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
        for (Method method : Arrays.stream(view.getMethods())
                .filter(m -> m.getDeclaringClass() != Object.class)
                .sorted(Comparator.comparing(Method::getName))
                .toList()) {
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
            } else if (c <= 0xFF) {
                out.append('_').append(HEX[c >>> 4]).append(HEX[c & 0x0F]);
            } else {
                out.append("_u").append(HEX[c >>> 12]).append(HEX[(c >>> 8) & 0x0F])
                        .append(HEX[(c >>> 4) & 0x0F]).append(HEX[c & 0x0F]);
            }
        }
        return out.toString();
    }

    private static String pathPart(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Reads the remote business views of the applications deployed now. */
    private static List<DeployedView> deployedViews(ApplicationRegistry applications) {
        List<DeployedView> result = new ArrayList<>();
        for (String name : applications.getAllApplicationNames()) {
            ApplicationInfo info = applications.get(name);
            if (info == null) {
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
                                viewName, loader, stateful));
                    }
                }
            }
        }
        return result;
    }
}
