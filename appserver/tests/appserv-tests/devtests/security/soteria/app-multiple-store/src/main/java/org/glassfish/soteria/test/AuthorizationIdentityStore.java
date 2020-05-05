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
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;

/**
 *
 */
@RequestScoped
public class AuthorizationIdentityStore implements IdentityStore {

    private Map<String, Set<String>> authorization;

    @PostConstruct
    public void init() {
        authorization = new HashMap<>();

        authorization.put("reza", new HashSet<>(asList("foo", "bar")));
        authorization.put("alex", new HashSet<>(asList("foo", "foo", "kaz")));
        authorization.put("arjan", new HashSet<>(asList("foo", "foo")));

    }

    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        return authorization.get(validationResult.getCallerPrincipal().getName());
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return new HashSet<>(asList(PROVIDE_GROUPS));
    }

}
