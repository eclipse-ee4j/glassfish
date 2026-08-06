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
package com.sun.enterprise.security.ee.authorization;

import com.sun.enterprise.security.ee.authorization.ResourceMethodSelector.ResourceEndpoint;
import com.sun.enterprise.security.ee.authorization.RestModelBridge.RestApplication;

import jakarta.security.jacc.WebResourcePermission;
import jakarta.servlet.ServletContext;

import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.glassfish.exousia.permissions.JakartaPermissions;
import org.glassfish.exousia.permissions.RestResourceMethodSelector;
import org.glassfish.exousia.permissions.RestResourcePermission;

import static com.sun.enterprise.security.ee.authorization.RestModelBridge.getRestApplicationRegistry;
import static com.sun.enterprise.util.Utility.isBlank;
import static java.util.stream.Collectors.joining;
import static org.glassfish.soteria.rest.introspection.ResourceSecurityConstraintResolver.resolveSecurityConstraintForResource;

/**
 * This class inspects the available REST applications and provides information such as the bases and all
 * the security constraints for resource methods.
 */
public class RestIntrospector {

    private static final String ESCAPED_COLON = "%3A";

    public static Set<String> getRestServletPathBases(ServletContext servletContext) {
        Set<String> restBaseUrls = new LinkedHashSet<>();

        for (RestApplication restApplication : getRestApplicationRegistry(servletContext).values()) {
            restBaseUrls.addAll(restApplication.bases());
        }

        return restBaseUrls;
    }

    public static JakartaPermissions createPermissionsForRestApplications(ServletContext servletContext) {
        JakartaPermissions jakartaPermissions = new JakartaPermissions();

        for (RestApplication restApplication : getRestApplicationRegistry(servletContext).values()) {
            RestResourceMethodSelector methodSelector = new RestResourceMethodSelectorImpl(restApplication.selector());
            List<ResourceEndpoint> endpoints = restApplication.selector().getResourceEndpoints();

            for (String base : restApplication.bases()) {
                for (ResourceEndpoint endpoint : endpoints) {
                    if (endpoint.isExtended()) {
                        continue;
                    }

                    var securityConstraint =
                        resolveSecurityConstraintForResource(
                            endpoint.getResourceClass(),
                            endpoint.getJavaMethod());

                    if (securityConstraint == null) {
                        continue;
                    }

                    Permission permission = toPermission(base, endpoint, methodSelector);

                    switch (securityConstraint.type()) {
                        case DENY_ALL:
                            jakartaPermissions.getExcluded().add(permission);
                            break;

                        case PERMIT_ALL:
                            jakartaPermissions.getUnchecked().add(permission);
                            break;

                        case ROLES_ALLOWED:
                            for (String role : securityConstraint.roles()) {
                                jakartaPermissions.getPerRole().computeIfAbsent(role, e -> new Permissions()).add(permission);
                            }
                            break;

                        default:
                            throw new IllegalStateException("Unknown access rule type: " + securityConstraint.type());
                    }
                }
            }
        }

        return jakartaPermissions;
    }

    /**
     * Derives a context-relative, unqualified URL pattern name suitable for
     * staging as the name argument of WebResourcePermission.
     *
     * This is not the final qualified URLPatternSpec. The normal constraints
     * transformer will create the qualified form later.
     */
    private static String toStagedUrlPatternName(String path) {
        if (containsTemplate(path)) {
            throw new IllegalArgumentException(
                "URI templates are not supported here: " + path);
        }

        if (path.equals("/")) {
            return "";
        }

        return path.replace(":", ESCAPED_COLON);
    }

    private static Permission toPermission(String base, ResourceEndpoint endpoint, RestResourceMethodSelector methodSelector) {
        String templatePath = endpoint.getTemplatePath();

        if (containsTemplate(templatePath)) {
            return new RestResourcePermission(base, templatePath, endpoint.getHttpMethod(), methodSelector);
        }

        return new WebResourcePermission(
            toStagedUrlPatternName(joinPath(base, templatePath)),
            endpoint.getHttpMethod());
    }

    private static boolean containsTemplate(String path) {
        return path.indexOf('{') >= 0 || path.indexOf('}') >= 0;
    }

    private static String joinPath(String... parts) {
        List<String> normalizedParts = new ArrayList<>();

        for (String part : parts) {
            String normalized = normalizePathPart(part);

            if (!normalized.isEmpty()) {
                normalizedParts.add(normalized);
            }
        }

        if (normalizedParts.isEmpty()) {
            return "/";
        }

        return "/" + normalizedParts.stream().collect(joining("/"));
    }

    /**
     * Strips leading and trailing slashes. In Jakarta REST, leading slashes
     * in @Path values are ignored for absolutizing.
     */
    private static String normalizePathPart(String part) {
        if (isBlank(part)) {
            return "";
        }

        String result = part.trim();

        while (result.startsWith("/")) {
            result = result.substring(1);
        }

        while (result.endsWith("/") && !result.isEmpty()) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

}
