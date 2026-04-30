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

import jakarta.servlet.ServletContext;

import java.security.Permissions;

import org.glassfish.exousia.permissions.JakartaPermissions;
import org.glassfish.soteria.rest.RestPermissions;

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

    public static JakartaPermissions getStagedPermissionsFromContext(ServletContext servletContext) {
        JakartaPermissions jakartaPermissions = new JakartaPermissions();

        if (servletContext != null && RestPermissions.hasPermissions(servletContext)) {

            var excluded = RestPermissions.getExcluded(servletContext);
            if (excluded != null) {
                excluded.elementsAsStream()
                        .forEach(e -> jakartaPermissions.getExcluded().add(e));
            }

            var unchecked = RestPermissions.getUnchecked(servletContext);
            if (unchecked != null) {
                unchecked.elementsAsStream()
                         .forEach(e -> jakartaPermissions.getUnchecked().add(e));
            }

            var perRole = RestPermissions.getPerRole(servletContext);
            if (perRole != null) {
                perRole.entrySet().stream()
                       .forEach(e -> jakartaPermissions.getPerRole().put(e.getKey(), copy(e.getValue())));
            }
        }

        return jakartaPermissions;
    }

    private static Permissions copy(Permissions source) {
        Permissions target = new Permissions();

        if (source != null) {
            source.elementsAsStream()
                  .forEach(target::add);
        }

        return target;
    }
}
