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

import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.orb.http.fory.grpc.ForyGeneratedRuntime;
import org.glassfish.orb.http.fory.grpc.ForyGeneratedSchemaAdapter;
import org.glassfish.orb.http.fory.grpc.ForyGeneratedServiceRegistry;
import org.glassfish.orb.http.fory.grpc.ForyGrpcCatalog;
import org.glassfish.orb.http.fory.grpc.ForyGrpcSkeleton;
import org.glassfish.orb.http.fory.grpc.ForyIdlGenerator;
import org.glassfish.orb.http.protocol.Protocol;
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
 */
@Service
@Singleton
public class EjbNameIndex {

    private static final Logger LOG = System.getLogger(EjbNameIndex.class.getName());

    @Inject
    private ApplicationRegistry applications;

    private final Map<String, Long> byName = new ConcurrentHashMap<>();
    private final Map<String, ForyRoute> foryRoutes = new ConcurrentHashMap<>();

    public ForyGeneratedServiceRegistry foryRegistry() {
        ForyGeneratedServiceRegistry registry = new ForyGeneratedServiceRegistry();
        foryRoutes.clear();
        for (String name : applications.getAllApplicationNames()) {
            ApplicationInfo info = applications.get(name);
            if (info == null) continue;
            Application application = info.getMetaData(Application.class);
            if (application == null) continue;
            for (EjbBundleDescriptor bundle : application.getBundleDescriptors(EjbBundleDescriptor.class)) {
                String module = bundle.getModuleDescriptor().getModuleName();
                for (EjbDescriptor ejb : bundle.getEjbs()) {
                    Set<String> views = ejb.getRemoteBusinessClassNames();
                    if (views == null) continue;
                    ClassLoader loader = EjbContainerUtilImpl.getInstance().getClassLoader(ejb.getUniqueId());
                    for (String viewName : views) {
                        try {
                            Class<?> view = Class.forName(viewName, false, loader);
                            String service = "glassfish." + safeName(application.getRegistrationName()) + '.'
                                    + safeName(module) + '.' + safeName(ejb.getName()) + '_'
                                    + safeName(view.getSimpleName());
                            // Validate the complete contract before publishing any
                            // route. The IDL generator is the source of truth for
                            // method ordering, overload rejection and portable
                            // types; keeping this check here prevents a partially
                            // registered service whose wire type ids differ from
                            // the advertised .fdl document.
                            ForyIdlGenerator.generate(
                                    "glassfish." + safeName(application.getRegistrationName()) + '.'
                                            + safeName(module),
                                    safeName(ejb.getName()) + '_' + safeName(view.getSimpleName()), view);
                            ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of(service, view);
                            int id = 1000;
                            for (java.lang.reflect.Method method : java.util.Arrays.stream(view.getMethods())
                                    .filter(m -> m.getDeclaringClass() != Object.class)
                                    .sorted(java.util.Comparator.comparing(java.lang.reflect.Method::getName))
                                    .toList()) {
                                if (method.getDeclaringClass() == Object.class || method.getParameterCount() > 1) continue;
                                String wire = Protocol.CONTEXT_PATH + "/fory/" + service + '/'
                                        + Character.toUpperCase(method.getName().charAt(0)) + method.getName().substring(1);
                                var models = org.glassfish.orb.http.fory.grpc.ForyRuntimeModelGenerator.unary(
                                        service, method.getName(), method.getParameterCount() == 0 ? void.class : method.getParameterTypes()[0], method.getReturnType());
                                registry.register(wire, '/' + service + '/' + method.getName(), skeleton, models,
                                        new ForyGeneratedRuntime(models, id++, id++),
                                        ForyGeneratedSchemaAdapter.unary(models.request(), models.response()));
                                foryRoutes.put(wire, new ForyRoute(application.getRegistrationName(), module,
                                        ejb.getName(), viewName));
                            }
                        } catch (ReflectiveOperationException | IllegalArgumentException e) {
                            LOG.log(Level.WARNING, "cannot build Fory route for " + viewName + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
        return registry;
    }

    public ForyRoute foryRoute(String path) {
        return foryRoutes.get(path);
    }

    public record ForyRoute(String app, String module, String bean, String view) {
    }

    /**
     * @param appName the application name
     * @param moduleName the module within it, or null if the caller does not know it
     * @param beanName the bean's ejb-name
     * @return the container's id for that bean, or null if there is no such bean
     */
    public Long lookup(String appName, String moduleName, String beanName) {
        Long id = find(appName, moduleName, beanName);
        if (id != null) {
            return id;
        }
        refresh();
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
     * <p>The ambiguous key is only ever populated when one bean of that name
     * exists in the application, so the fallback cannot silently pick the
     * wrong one.
     */
    private Long find(String appName, String moduleName, String beanName) {
        Long id = byName.get(key(appName, moduleName, beanName));
        return id != null ? id : byName.get(key(appName, null, beanName));
    }

    /**
     * Rebuilds the index from what is deployed right now.
     *
     * <p>Logs what it found. A miss here presents as a 404 on an invocation,
     * which is indistinguishable from a bean that was never deployed and from
     * a name spelled wrong, so the only way to tell the three apart from the
     * outside is for this to say what it saw.
     */
    public void refresh() {
        Map<String, Long> rebuilt = new ConcurrentHashMap<>();
        for (String name : applications.getAllApplicationNames()) {
            ApplicationInfo info = applications.get(name);
            if (info == null) {
                LOG.log(Level.DEBUG, "application {0} is registered but has no info", name);
                continue;
            }
            Application application = info.getMetaData(Application.class);
            if (application == null) {
                LOG.log(Level.DEBUG, "application {0} carries no Application metadata", name);
                continue;
            }
            int before = rebuilt.size();
            index(application, rebuilt);
            LOG.log(Level.INFO, "indexed {0} bean key(s) for application {1} (registration name {2})",
                    rebuilt.size() - before, name, application.getRegistrationName());
        }
        byName.clear();
        byName.putAll(rebuilt);
        LOG.log(Level.INFO, "EJB name index rebuilt: {0}", rebuilt.keySet());
    }

    /** Builds the deploy-time Fory IDL catalog from remote business views. */
    public Map<String, String> foryIdl() {
        Map<String, String> result = new LinkedHashMap<>();
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
                    for (String viewName : views) {
                        try {
                            Class<?> view = Class.forName(viewName, false, loader);
                            String path = Protocol.CONTEXT_PATH + ForyGrpcCatalog.PREFIX
                                    + pathPart(application.getRegistrationName()) + '/'
                                    + pathPart(module) + '/'
                                    + pathPart(ejb.getName()) + '/'
                                    + pathPart(viewName) + ".fdl";
                            result.put(path, ForyIdlGenerator.generate(
                                    "glassfish." + safeName(application.getRegistrationName()) + '.'
                                            + safeName(module),
                                    safeName(ejb.getName()) + '_' + safeName(view.getSimpleName()), view));
                        } catch (ReflectiveOperationException | IllegalArgumentException e) {
                            LOG.log(Level.WARNING, "cannot generate Fory IDL for "
                                    + application.getRegistrationName() + '/' + module + '/' + ejb.getName()
                                    + '/' + viewName + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
        return result;
    }

    private static String safeName(String value) {
        return value == null ? "application" : value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String pathPart(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private void index(Application application, Map<String, Long> into) {
        var bundles = application.getBundleDescriptors(EjbBundleDescriptor.class);
        LOG.log(Level.INFO, "application {0} has {1} EJB bundle(s)",
                application.getRegistrationName(), bundles.size());
        for (EjbBundleDescriptor bundle : bundles) {
            String moduleName = bundle.getModuleDescriptor().getModuleName();
            for (EjbDescriptor ejb : bundle.getEjbs()) {
                into.put(key(application.getRegistrationName(), moduleName, ejb.getName()),
                        ejb.getUniqueId());
                // Also index without the module, so a client that knows only
                // the application and the bean is still served when the name is
                // unambiguous. putIfAbsent rather than put: on a collision the
                // ambiguous key should resolve to nothing in particular rather
                // than to whichever bean happened to be indexed last.
                into.putIfAbsent(key(application.getRegistrationName(), null, ejb.getName()),
                        ejb.getUniqueId());
            }
        }
    }

    private static String key(String appName, String moduleName, String beanName) {
        return appName + '/' + (moduleName == null ? "*" : moduleName) + '/' + beanName;
    }

    /** @return how many beans are currently indexed, for diagnostics */
    public int size() {
        return byName.size();
    }
}
