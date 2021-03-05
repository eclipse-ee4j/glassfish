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

package com.sun.enterprise.security.jacc.provider;

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
import org.glassfish.internal.api.Globals;

/**
 * Glassfish role mapper NB: mapper only supports disjunctive (as apposed to conjunctive principal 2 role mappings. IOW,
 * there is no way to require 2 or more principals to be in a Role.
 *
 * @author monzillo
 */
public class GlassfishRoleMapper implements JACCRoleMapper {

    private static final Logger defaultLogger = Logger.getLogger(GlassfishRoleMapper.class.getName());
    private final Logger logger;

    public GlassfishRoleMapper(Logger logger) {
        if (logger == null) {
            this.logger = defaultLogger;
        } else {
            this.logger = logger;
        }
    }

    private SecurityRoleMapper getInternalMapper(String pcid) {

        SecurityRoleMapperFactory factory = Globals.get(SecurityRoleMapperFactory.class);
        // SecurityRoleMapperFactoryMgr.getFactory();

        if (factory == null) {
            String msg = "RoleMapper.factory.lookup.failed";
            logger.log(Level.SEVERE, msg);
            throw new SecurityException(msg);
        }

        SecurityRoleMapper srm = factory.getRoleMapper(pcid);

        if (srm == null) {
            String msg = "RoleMapper.mapper.lookup.failed";
            logger.log(Level.SEVERE, msg);
            throw new SecurityException(msg);
        }
        return srm;
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

    private Set<Principal> getPrincipalsInRole(SecurityRoleMapper srm, String roleName)
            throws SecurityException, UnsupportedOperationException {

        Map<String, Subject> roleMap = srm.getRoleToSubjectMapping();
        if (roleMap == null) {
            return null;
        }

        Subject s = roleMap.get(roleName);
        if (s == null) {
            return null;
        }

        return s.getPrincipals();
    }

    public boolean arePrincipalsInRole(SecurityRoleMapper srm, Principal[] principals, String roleName) throws SecurityException {

        if (principals == null || principals.length == 0) {
            return false;
        }

        Set<Principal> rolePrincipals = getPrincipalsInRole(srm, roleName);
        if (rolePrincipals == null || rolePrincipals.isEmpty()) {
            return false;
        }

        for (Principal p : principals) {
            if (rolePrincipals.contains(p)) {
                return true;
            }
        }
        return false;
    }

    // public methods follow
    @Override
    public Set<String> getDeclaredRoles(String pcid) {
        return getDeclaredRoles(getInternalMapper(pcid));
    }

    @Override
    public boolean isSubjectInRole(String pcid, Subject s, String roleName) throws SecurityException {
        return arePrincipalsInRole(pcid, toArray(s.getPrincipals()), roleName);
    }

    @Override
    public boolean arePrincipalsInRole(String pcid, Principal[] principals, String roleName) throws SecurityException {
        return arePrincipalsInRole(getInternalMapper(pcid), principals, roleName);
    }

    @Override
    public Set<String> getRolesOfSubject(String pcid, Subject s) throws SecurityException, UnsupportedOperationException {
        return getRolesOfPrincipals(pcid, toArray(s.getPrincipals()));
    }

    @Override
    public Set<String> getRolesOfPrincipals(String pcid, Principal[] principals) throws SecurityException, UnsupportedOperationException {

        if (principals.length == 0) {
            return null;
        }

        SecurityRoleMapper srm = getInternalMapper(pcid);
        Set<String> roleNames = getDeclaredRoles(srm);

        // Comment out for now to supress FindBugs warning, getDeclaredRoles(srm) always throw UnsupportedOperationException
        // currently so roleNames cannot be null, when getDeclaredRoles is fixed we can uncomment this
        // if (roleNames == null) {
        // return null;
        // }

        HashSet<String> roles = new HashSet<>();
        Iterator<String> it = roleNames.iterator();
        while (it.hasNext()) {
            String roleName = it.next();
            Set<Principal> pSet = getPrincipalsInRole(srm, roleName);
            if (pSet != null) {
                for (Principal p : principals) {
                    if (pSet.contains(p)) {
                        roles.add(roleName);
                        break;
                    }
                }
            }
        }

        return roles;
    }

    @Override
    public BitSet getRolesOfSubject(String pcid, String[] roles, Subject s) throws SecurityException, UnsupportedOperationException {
        return getRolesOfPrincipals(pcid, roles, toArray(s.getPrincipals()));
    }

    private Principal[] toArray(Set principals) {
        Principal[] list = new Principal[principals.size()];
        int i = 0;
        for (Object obj : principals) {
            if (obj instanceof Principal) {
                list[i] = (Principal) obj;
            }
        }
        return list;
    }

    @Override
    public BitSet getRolesOfPrincipals(String pcid, String[] roles, Principal[] principals)
            throws SecurityException, UnsupportedOperationException {
        if (principals.length == 0 || roles == null || roles.length == 0) {
            return null;
        }
        BitSet roleSet = new BitSet(roles.length);
        SecurityRoleMapper srm = getInternalMapper(pcid);
        for (int i = 0; i < roles.length; i++) {
            roleSet.set(i, arePrincipalsInRole(srm, principals, roles[i]));
        }
        return roleSet;
    }

    @Override
    public Set<Principal> getPrincipalsInRole(String pcid, String roleName) throws SecurityException, UnsupportedOperationException {
        return getPrincipalsInRole(getInternalMapper(pcid), roleName);
    }
}
