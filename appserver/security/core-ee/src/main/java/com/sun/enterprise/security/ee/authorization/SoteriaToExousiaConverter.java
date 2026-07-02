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

import jakarta.security.jacc.WebResourcePermission;
import jakarta.servlet.ServletContext;

import java.security.Permission;
import java.security.Permissions;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.exousia.permissions.JakartaPermissions;
import org.glassfish.exousia.permissions.RestResourcePermission;
import org.glassfish.soteria.rest.RestConstraintsStore;
import org.glassfish.soteria.rest.RestConstraintsStore.RestConstraint;

/**
 * This class converts from staged permissions by Soteria to Exousia JakartaPermissions.
 *
 * <p>
 * Soteria stages permissions discovered from Jakarta REST endpoints in the ServletContext.
 * Using its accessor for these, we retrieve them and creata an Exousia native {@link JakartaPermissions}
 * instance out of those.
 *
 */
public class SoteriaToExousiaConverter {

    private static final String ESCAPED_COLON = "%3A";

    public static Set<String> getRESTServletPathBases(ServletContext servletContext) {
        Set<String> restBaseUrls = new HashSet<String>();

        if (RestConstraintsStore.hasConstraints(servletContext)) {
            restBaseUrls.addAll(RestConstraintsStore.getConstraints(servletContext).keySet());
        }

        return restBaseUrls;
    }

    public static JakartaPermissions getStagedPermissionsFromContext(ServletContext servletContext) {
        JakartaPermissions jakartaPermissions = new JakartaPermissions();

        if (RestConstraintsStore.hasConstraints(servletContext)) {

            for (var constraints : RestConstraintsStore.getConstraints(servletContext).values()) {
                for (var constraint : constraints) {

                    Permission permission = toPermission(constraint);

                    var securityConstraint = constraint.securityConstraint();

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
    public static String toStagedUrlPatternName(String path) {
        if (containsTemplate(path)) {
            throw new IllegalArgumentException(
                "URI templates are not supported for JACC staging yet: " + path);
        }

        if (path.equals("/")) {
            return "";
        }

        return path.replace(":", ESCAPED_COLON);
    }

    private static Permission toPermission(RestConstraint constraint) {
        String path = constraint.fullTemplatePath();

        if (containsTemplate(path)) {
            return new RestResourcePermission(path, constraint.httpMethod());
        }

        return new WebResourcePermission(
            toStagedUrlPatternName(path),
            constraint.httpMethod());
    }


    private static boolean containsTemplate(String path) {
        return path.indexOf('{') >= 0 || path.indexOf('}') >= 0;
    }


}
