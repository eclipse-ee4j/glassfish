/*
 * Copyright (c) 2023, 2024 Eclipse Foundation and/or its affiliates.
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

import jakarta.security.jacc.PrincipalMapper;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import org.glassfish.deployment.common.SecurityRoleMapper;
import org.glassfish.deployment.common.SecurityRoleMapperFactory;
import org.glassfish.internal.api.Globals;

public class GlassFishPrincipalMapper implements PrincipalMapper {

    private final SecurityRoleMapper roleMapper;

    private final Map<String, Set<String>> groupToRoles;
    private final Map<String, Set<String>> callerToRoles;
    private final boolean oneToOneMapping;
    private final boolean anyAuthenticatedUserRoleMapped;


    public GlassFishPrincipalMapper(String contextId) {
        roleMapper =
            Globals.get(SecurityRoleMapperFactory.class)
                   .getRoleMapper(contextId);

        groupToRoles = roleMapper.getGroupToRolesMapping();
        callerToRoles = roleMapper.getCallerToRolesMapping();
        oneToOneMapping =
            groupToRoles.isEmpty() &&
            callerToRoles.isEmpty() &&
            roleMapper.isDefaultPrincipalToRoleMapping();


        // Jakarta Authorization spec 3.2 states:
        //
        // "For the any "authenticated user role", "**", and unless an application specific mapping has
        // been established for this role,
        // the provider must ensure that all permissions added to the role are granted to any
        // authenticated user."
        //
        // Here we check for the "unless" part mentioned above. If we're dealing with the "**" role here
        // and groups is not
        // empty, then there's an application specific mapping and "**" maps only to those groups, not
        // to any authenticated user.
        anyAuthenticatedUserRoleMapped =
            groupToRoles.values()
                        .stream()
                        .flatMap(roles -> roles.stream())
                        .anyMatch(role -> role.equals("**"));
    }

    @Override
    public Principal getCallerPrincipal(Subject subject) {
        return roleMapper.getCallerPrincipal(subject);
    }

    @Override
    public Set<String> getMappedRoles(Subject subject) {
        // Check for groups that have been mapped to roles
        Set<String> mappedRoles = mapGroupsToRoles(roleMapper.getGroups(subject));

        Principal callerPrincipal = getCallerPrincipal(subject);

        // Check if the caller principal has been mapped to roles
        if (callerPrincipal != null && callerToRoles.containsKey(callerPrincipal.getName())) {
            mappedRoles = new HashSet<>(mappedRoles);
            mappedRoles.addAll(callerToRoles.get(callerPrincipal.getName()));
        }

        return mappedRoles;
    }

    @Override
    public boolean isAnyAuthenticatedUserRoleMapped() {
        return anyAuthenticatedUserRoleMapped;
    }

    private Set<String> mapGroupsToRoles(Set<String> groups) {
        if (oneToOneMapping) {
            // There is no mapping used, groups directly represent roles.
            return groups;
        }

        Set<String> roles = new HashSet<>();

        for (String group : groups) {
            if (groupToRoles.containsKey(group)) {
                roles.addAll(groupToRoles.get(group));
            }
        }

        return roles;
    }

}
