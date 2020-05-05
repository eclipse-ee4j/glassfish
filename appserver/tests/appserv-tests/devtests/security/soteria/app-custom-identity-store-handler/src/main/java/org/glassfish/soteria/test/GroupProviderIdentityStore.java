/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.soteria.test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static org.glassfish.soteria.Utils.unmodifiableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition;
import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition.LdapSearchScope;
import javax.security.enterprise.identitystore.IdentityStore.ValidationType;
import static javax.security.enterprise.identitystore.LdapIdentityStoreDefinition.LdapSearchScope.SUBTREE;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;

/**
 *
 */
@LdapIdentityStoreDefinition(
        url = "ldap://localhost:33389/",
        callerBaseDn = "ou=caller,dc=jsr375,dc=net",
        callerSearchScope = LdapSearchScope.SUBTREE,
        groupSearchBase = "ou=group,dc=jsr375,dc=net",
        useForExpression = "#{'VALIDATE'}",
        groupSearchScopeExpression = "${configBean.searchScopeOneLevel}"
)
@ApplicationScoped
public class GroupProviderIdentityStore implements IdentityStore {

    private Map<String, Set<String>> groupsPerCaller;

    @PostConstruct
    public void init() {
        groupsPerCaller = new HashMap<>();

        groupsPerCaller.put("rudy", new HashSet<>(asList("foo", "bar")));
        groupsPerCaller.put("will", new HashSet<>(asList("foo", "bar", "baz")));
        groupsPerCaller.put("arjan", new HashSet<>(asList("foo", "baz")));
        groupsPerCaller.put("reza", new HashSet<>(asList("baz")));

    }

    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        Set<String> result = groupsPerCaller.get(validationResult.getCallerPrincipal().getName());
        if (result == null) {
            result = emptySet();
        }

        return result;
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return unmodifiableSet(PROVIDE_GROUPS);
    }
}
