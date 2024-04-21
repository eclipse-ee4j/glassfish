/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.glassfish.deployment.common.SecurityRoleMapper;
import org.glassfish.deployment.common.SecurityRoleMapperFactory;
import org.glassfish.exousia.modules.locked.AuthorizationRoleMapper;
import org.glassfish.internal.api.Globals;

/**
 * Glassfish role mapper
 *
 * @author monzillo
 */
public class GlassfishRoleMapper implements AuthorizationRoleMapper {

    public GlassfishRoleMapper() {
       // No-arg constructor for reflection used by Exousia 2.1.2 and newer
    }


    @Override
    public Set<Principal> getPrincipalsInRole(String contextId, String roleName) throws SecurityException {
        return getPrincipalsInRole(getInternalMapper(contextId), roleName);
    }

    private SecurityRoleMapper getInternalMapper(String contextId) {
        SecurityRoleMapperFactory factory = Globals.get(SecurityRoleMapperFactory.class);
        if (factory == null) {
            throw new SecurityException("SecurityRoleMapperFactory lookup failed - null.");
        }

        SecurityRoleMapper securityRoleMapper = factory.getRoleMapper(contextId);
        if (securityRoleMapper == null) {
            throw new SecurityException("Factory failed to provide a SecurityRoleMapper. Factory: " + factory);
        }

        return securityRoleMapper;
    }

    private Set<Principal> getPrincipalsInRole(SecurityRoleMapper securityRoleMapper, String roleName) throws SecurityException {
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
}
