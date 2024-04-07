/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.security.Principal;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.deployment.common.SecurityRoleMapper;
import org.glassfish.deployment.common.SecurityRoleMapperFactory;
import org.glassfish.exousia.modules.locked.AuthorizationRoleMapper;
import org.glassfish.internal.api.Globals;

/**
 * Glassfish role mapper
 *
 * <p>
 * NB: mapper only supports disjunctive (as apposed to conjunctive principal 2 role mappings. IOW,
 * there is no way to require 2 or more principals to be in a Role.
 *
 * @author monzillo
 */
public class GlassfishRoleMapper implements AuthorizationRoleMapper {

    private static final Logger defaultLogger = Logger.getLogger(GlassfishRoleMapper.class.getName());
    private final Logger logger;

    public GlassfishRoleMapper(Logger logger) {
        if (logger == null) {
            this.logger = defaultLogger;
        } else {
            this.logger = logger;
        }
    }

    private SecurityRoleMapper getInternalMapper(String contextId) {
        SecurityRoleMapperFactory factory = Globals.get(SecurityRoleMapperFactory.class);

        if (factory == null) {
            String msg = "RoleMapper.factory.lookup.failed";
            logger.log(Level.SEVERE, msg);
            throw new SecurityException(msg);
        }

        SecurityRoleMapper securityRoleMapper = factory.getRoleMapper(contextId);

        if (securityRoleMapper == null) {
            String msg = "RoleMapper.mapper.lookup.failed";
            logger.log(Level.SEVERE, msg);
            throw new SecurityException(msg);
        }

        return securityRoleMapper;
    }

    private Set<String> getDeclaredRoles(SecurityRoleMapper srm) {

        // default role mapping does not implement srm.getRoles() properly
        // until that is fixed, must throw UnsupportedOperation exception

        if (true) {
            String msg = "RoleMapper.unable.to.get.roles";
            logger.log(Level.SEVERE, msg);
            throw new UnsupportedOperationException(msg);
        }

        HashSet<String> roleNameSet = null;
        Iterator<String> it = srm.getRoles();
        while (it.hasNext()) {
            if (roleNameSet == null) {
                roleNameSet = new HashSet<>();
            }
            roleNameSet.add(it.next());
        }
        return roleNameSet;
    }

    private Set<Principal> getPrincipalsInRole(SecurityRoleMapper securityRoleMapper, String roleName) throws SecurityException, UnsupportedOperationException {
        Map<String, Subject> roleMap = securityRoleMapper.getRoleToSubjectMapping();
        if (roleMap == null) {
            return null;
        }

        Subject subject = roleMap.get(roleName);
        if (subject == null) {
            return null;
        }

        return subject.getPrincipals();
    }

    public boolean arePrincipalsInRole(SecurityRoleMapper srm, Principal[] principals, String roleName) throws SecurityException {
        if (principals == null || principals.length == 0) {
            return false;
        }

        Set<Principal> rolePrincipals = getPrincipalsInRole(srm, roleName);
        if (rolePrincipals == null || rolePrincipals.isEmpty()) {
            return false;
        }

        for (Principal principal : principals) {
            if (rolePrincipals.contains(principal)) {
                return true;
            }
        }

        return false;
    }

    // public methods follow
    public Set<String> getDeclaredRoles(String contextId) {
        return getDeclaredRoles(getInternalMapper(contextId));
    }

    public boolean isSubjectInRole(String contextId, Subject subject, String roleName) throws SecurityException {
        return arePrincipalsInRole(contextId, toArray(subject.getPrincipals()), roleName);
    }

    public boolean arePrincipalsInRole(String contextId, Principal[] principals, String roleName) throws SecurityException {
        return arePrincipalsInRole(getInternalMapper(contextId), principals, roleName);
    }

    public Set<String> getRolesOfSubject(String contextId, Subject subject) throws SecurityException, UnsupportedOperationException {
        return getRolesOfPrincipals(contextId, toArray(subject.getPrincipals()));
    }

    public Set<String> getRolesOfPrincipals(String contextId, Principal[] principals) throws SecurityException, UnsupportedOperationException {
        if (principals.length == 0) {
            return null;
        }

        SecurityRoleMapper securityRoleMapper = getInternalMapper(contextId);
        Set<String> roleNames = getDeclaredRoles(securityRoleMapper);

        HashSet<String> roles = new HashSet<>();
        Iterator<String> it = roleNames.iterator();
        while (it.hasNext()) {
            String roleName = it.next();
            Set<Principal> principalsInRole = getPrincipalsInRole(securityRoleMapper, roleName);
            if (principalsInRole != null) {
                for (Principal p : principals) {
                    if (principalsInRole.contains(p)) {
                        roles.add(roleName);
                        break;
                    }
                }
            }
        }

        return roles;
    }

    public BitSet getRolesOfSubject(String contextId, String[] roles, Subject subject) throws SecurityException, UnsupportedOperationException {
        return getRolesOfPrincipals(contextId, roles, toArray(subject.getPrincipals()));
    }

    private Principal[] toArray(Set<Principal> principals) {
        Principal[] list = new Principal[principals.size()];
        int i = 0;
        for (Object obj : principals) {
            if (obj instanceof Principal) {
                list[i] = (Principal) obj;
            }
        }

        return list;
    }

    public BitSet getRolesOfPrincipals(String contextId, String[] roles, Principal[] principals) throws SecurityException, UnsupportedOperationException {
        if (principals.length == 0 || roles == null || roles.length == 0) {
            return null;
        }

        BitSet roleSet = new BitSet(roles.length);
        SecurityRoleMapper srm = getInternalMapper(contextId);
        for (int i = 0; i < roles.length; i++) {
            roleSet.set(i, arePrincipalsInRole(srm, principals, roles[i]));
        }
        return roleSet;
    }

    @Override
    public Set<Principal> getPrincipalsInRole(String contextId, String roleName) throws SecurityException, UnsupportedOperationException {
        return getPrincipalsInRole(getInternalMapper(contextId), roleName);
    }
}
