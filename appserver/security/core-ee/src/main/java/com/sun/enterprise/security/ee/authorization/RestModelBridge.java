/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package com.sun.enterprise.security.ee.authorization;

import jakarta.annotation.Priority;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.FeatureContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.glassfish.jersey.servlet.WebConfig;

import static com.sun.enterprise.util.Utility.isBlank;
import static com.sun.enterprise.util.Utility.isOneOf;
import static jakarta.ws.rs.RuntimeType.SERVER;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.glassfish.jersey.server.monitoring.ApplicationEvent.Type.INITIALIZATION_APP_FINISHED;
import static org.glassfish.jersey.servlet.WebConfig.ConfigType.FilterConfig;

/**
 * Captures the deployed Jakarta REST resource model of every Jersey application in a web module, and publishes it --
 * together with the servlet mount points it is reachable on -- as a registry in the ServletContext.
 *
 * <p>
 * This bridges the two universes that have to be joined for REST authorization: Jersey's model knows resource templates
 * relative to the <em>application</em>, while the servlet layer knows the <em>mount point</em> (the
 * {@code @ApplicationPath} value, or the {@code web.xml} servlet mapping). Neither can produce a context-relative path
 * alone. {@link RestApplication} pairs them.
 *
 * <p>
 * One WAR can host several REST applications, and one REST application can be mounted on several paths. The registry
 * therefore holds one entry per application, each carrying <em>all</em> of that application's mappings.
 *
 */
@Priority(100)
@ConstrainedTo(SERVER)
public final class RestModelBridge implements ForcedAutoDiscoverable {

    /**
     * ServletContext attribute holding a {@code Map<String, RestApplication>} keyed by the servlet or filter registration name of
     * each Jersey application in this web module.
     */
    public static final String REGISTRY_ATTRIBUTE = "org.glassfish.security.rest.registry";


    @Override
    public void configure(FeatureContext context) {
        if (context.getConfiguration().getRuntimeType() != SERVER) {
            return;
        }

        if (context.getConfiguration().isRegistered(CapturingListener.class)) {
            return;
        }

        context.register(CapturingListener.class);
    }

    /**
     * Reads the registry of REST applications published for a web module.
     *
     * @param servletContext The context of the web module to read the registry from.
     * @return The registry, keyed by registration name; empty when no Jersey application published to this context.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, RestApplication> getRestApplicationRegistry(ServletContext servletContext) {
        if (servletContext == null) {
            return Map.of();
        }

        Object registry = servletContext.getAttribute(REGISTRY_ATTRIBUTE);
        if (registry instanceof Map) {
            return unmodifiableMap((Map<String, RestApplication>) registry);
        }

        return Map.of();
    }


    /**
     * Captures the model once per REST application, at the end of that application's initialization. For eagerly loaded
     * Jersey servlets -- which is what {@code @ApplicationPath} deployments are, since Jersey registers them with a
     * load-on-startup value -- this happens during deployment, before the first request.
     */
    @ConstrainedTo(SERVER)
    public static final class CapturingListener implements ApplicationEventListener {

        /**
         * Bound unconditionally by Jersey's servlet integration, and the authoritative source for the registration name of this
         * very component, the context it is deployed in, and whether it is a servlet or a filter. Null outside servlet
         * deployments.
         */
        @Context
        private WebConfig webConfig;

        /**
         * Fallback context source, in case only this binding resolves.
         */
        @Context
        private ServletContext injectedServletContext;

        @Override
        public void onEvent(ApplicationEvent event) {
            if (event.getType() != INITIALIZATION_APP_FINISHED) {
                return;
            }

            ServletContext servletContext = getServletContext();
            if (servletContext == null) {
                return;
            }

            Class<?> applicationClass = event.getResourceConfig().getApplication().getClass();
            boolean filterDeployment = webConfig != null && webConfig.getConfigType() == FilterConfig;

            // Prefer the registration name Jersey itself was configured with. Without WebConfig,
            // fall back to the Application class name, which is what Jersey's servlet container
            // initializer names @ApplicationPath registrations after anyway.
            String componentName = webConfig != null ? webConfig.getName() : applicationClass.getName();

            restApplicationRegistry(servletContext)
                .put(componentName,
                    new RestApplication(
                        componentName,
                        filterDeployment,
                        applicationClass,
                        getMappings(servletContext, componentName, filterDeployment),
                        ResourceMethodSelector.of(event.getResourceModel())));
        }

        @Override
        public RequestEventListener onRequest(RequestEvent requestEvent) {
            return null; // Deployment time capture only.
        }

        private ServletContext getServletContext() {
            if (webConfig != null && webConfig.getServletContext() != null) {
                return webConfig.getServletContext();
            }
            return injectedServletContext;
        }

        private static Set<String> getMappings(ServletContext servletContext, String componentName, boolean filterDeployment) {
            if (filterDeployment) {
                FilterRegistration filterRegistration = servletContext.getFilterRegistration(componentName);
                if (filterRegistration == null) {
                    return Set.of();
                }

                return new LinkedHashSet<>(filterRegistration.getUrlPatternMappings());
            }

            ServletRegistration servletRegistration = servletContext.getServletRegistration(componentName);
            if (servletRegistration == null) {
                return Set.of();
            }

            return new LinkedHashSet<>(servletRegistration.getMappings());
        }

        @SuppressWarnings("unchecked")
        private static ConcurrentMap<String, RestApplication> restApplicationRegistry(ServletContext servletContext) {
            ConcurrentMap<String, RestApplication> registry = (ConcurrentMap<String, RestApplication>) servletContext.getAttribute(REGISTRY_ATTRIBUTE);
            if (registry != null) {
                return registry;
            }

            synchronized (RestModelBridge.class) {
                registry = (ConcurrentMap<String, RestApplication>) servletContext.getAttribute(REGISTRY_ATTRIBUTE);
                if (registry == null) {
                    registry = new ConcurrentHashMap<>();
                    servletContext.setAttribute(REGISTRY_ATTRIBUTE, registry);
                }
            }

            return registry;
        }
    }

    /**
     * One deployed Jakarta REST application, as reachable within one web module.
     *
     * @param componentName The servlet or filter registration name. For {@code @ApplicationPath} deployments this equals
     * the Application class name, because that is how Jersey's servlet container initializer names the registration. This
     * is the key a security valve can look up from an already mapped request.
     * @param filterDeployment True when the Jersey component is registered as a filter rather than a servlet.
     * @param applicationClass The {@code jakarta.ws.rs.core.Application} subclass. May be a {@code ResourceConfig} subtype
     * for programmatic deployments.
     * @param mappings The URL patterns this application is mounted on, in declaration order, for example {@code [/rest/*]}.
     * Empty when the registration could not be resolved; callers should then fall back to their own mount detection.
     * @param model The enhanced, deployed resource model, including model processor output such as the WADL resource and
     * the synthetic OPTIONS methods.
     * @param selector A request-independent matcher over that model.
     */
    public record RestApplication(
        String componentName,
        boolean filterDeployment,
        Class<?> applicationClass,
        Set<String> mappings,
        ResourceMethodSelector selector) {

        public RestApplication {
            mappings = unmodifiableSet(new LinkedHashSet<>(mappings));
        }

        /**
         * The mount points of this application as path prefixes, ready to be prepended to an application-relative template.
         * Derived from {@link #mappings()} through {@link RestModelBridge#toBase(String)}; mappings that cannot serve as a REST
         * base are skipped, so this can be empty even when {@code mappings()} is not.
         */
        public List<String> bases() {
            List<String> bases = new ArrayList<>();
            for (String mapping : mappings) {
                String base = toBase(mapping);
                if (base != null && !bases.contains(base)) {
                    bases.add(base);
                }
            }
            return bases;
        }

        /**
         * Converts a servlet URL pattern into the path prefix that REST resources of the mounted application are reachable
         * under.
         *
         * <p>
         * {@code /rest/*} becomes {@code /rest}, and both {@code /*} and {@code /} become the empty string, meaning the
         * application is mounted at the context root. Extension mappings and exact mappings return {@code null}: an extension
         * mapping has no prefix at all, and an exact mapping can only ever serve the application-relative root, which makes it
         * useless as a staging base.
         *
         * @param mapping The servlet or filter URL pattern to convert.
         * @return The path prefix, or {@code null} when the mapping cannot serve as a REST base.
         */
        private static String toBase(String mapping) {
            if (isBlank(mapping)) {
                return null;
            }

            String urlPattern = mapping.trim();

            if (isOneOf(urlPattern, "/", "/*")) {
                return "";
            }

            if (urlPattern.startsWith("*.")) {
                return null;
            }

            if (urlPattern.endsWith("/*")) {
                return urlPattern.substring(0, urlPattern.length() - 2);
            }

            return null;
        }
    }
}