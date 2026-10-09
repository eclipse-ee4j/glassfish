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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
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
import org.glassfish.orb.http.fory.grpc.ForyRuntimeModelGenerator;
import org.glassfish.orb.http.fory.grpc.ForyTypeIds;
import org.glassfish.orb.http.protocol.Protocol;

/**
 * The remote business views of the deployed beans, as Fory IDL documents and
 * as the routes that serve them.
 *
 * <p>Read from {@link ApplicationRegistry} on demand, for the reason the
 * endpoint's own name index is: a bean deployed after the endpoint started has
 * to be reachable without restarting the server.
 */
final class ForyEjbIndex {

    private static final Logger LOG = System.getLogger(ForyEjbIndex.class.getName());

    private final ApplicationRegistry applications;

    private final Map<String, ForyRoute> foryRoutes = new ConcurrentHashMap<>();

    /** The registry the endpoint serves from; {@link #refreshForyRegistry} fills it. */
    private final ForyGeneratedServiceRegistry foryRegistry = new ForyGeneratedServiceRegistry();

    ForyEjbIndex(ApplicationRegistry applications) {
        this.applications = applications;
    }

    ForyGeneratedServiceRegistry foryRegistry() {
        return foryRegistry;
    }

    /**
     * Rebuilds the Fory routes from the applications deployed now, so that a
     * bean deployed after the endpoint started can still be reached.
     *
     * @return the registry, filled in
     */
    ForyGeneratedServiceRegistry refreshForyRegistry() {
        ForyGeneratedServiceRegistry registry = foryRegistry;
        registry.clear();
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
                            String idlPackage = "glassfish." + safeName(application.getRegistrationName())
                                    + '.' + safeName(module);
                            ForyIdlGenerator.generate(idlPackage,
                                    safeName(ejb.getName()) + '_' + safeName(view.getSimpleName()), view);
                            ForyGrpcSkeleton skeleton = ForyGrpcSkeleton.of(service, view);
                            for (Method method : Arrays.stream(view.getMethods())
                                    .filter(m -> m.getDeclaringClass() != Object.class)
                                    .sorted(Comparator.comparing(Method::getName))
                                    .toList()) {
                                if (method.getDeclaringClass() == Object.class || method.getParameterCount() > 1) continue;
                                String wire = Protocol.CONTEXT_PATH + "/fory/" + service + '/'
                                        + Character.toUpperCase(method.getName().charAt(0)) + method.getName().substring(1);
                                var models = ForyRuntimeModelGenerator.unary(
                                        service, method.getName(), method.getParameterCount() == 0 ? void.class : method.getParameterTypes()[0], method.getReturnType());
                                // The ids come from the names in the published IDL, so the
                                // document a client generated from and the runtime agree.
                                String rpcName = Character.toUpperCase(method.getName().charAt(0))
                                        + method.getName().substring(1);
                                registry.register(wire, '/' + service + '/' + method.getName(), skeleton, models,
                                        new ForyGeneratedRuntime(models,
                                                ForyTypeIds.of(idlPackage, rpcName + "Request"),
                                                ForyTypeIds.of(idlPackage, rpcName + "Response")),
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

    ForyRoute foryRoute(String path) {
        return foryRoutes.get(path);
    }

    record ForyRoute(String app, String module, String bean, String view) {
    }

    /** Builds the deploy-time Fory IDL catalog from remote business views. */
    Map<String, String> foryIdl() {
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
}
