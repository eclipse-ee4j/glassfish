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

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.IdentityStoreHandler;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;
import static org.glassfish.soteria.cdi.CdiUtils.getBeanReferencesByType;

/**
 *
 */
@Alternative
@Priority(APPLICATION)
@ApplicationScoped
public class CustomIdentityStoreHandler implements IdentityStoreHandler {

    private List<IdentityStore> validatingIdentityStores;
    private List<IdentityStore> groupProvidingIdentityStores;

    @PostConstruct
    public void init() {
        List<IdentityStore> identityStores = getBeanReferencesByType(IdentityStore.class, false);

        validatingIdentityStores = identityStores.stream()
                .filter(i -> i.validationTypes().contains(VALIDATE))
                .sorted(comparing(IdentityStore::priority))
                .collect(toList());

        groupProvidingIdentityStores = identityStores.stream()
                .filter(i -> i.validationTypes().contains(PROVIDE_GROUPS))
                .sorted(comparing(IdentityStore::priority))
                .collect(toList());
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        CredentialValidationResult validationResult = null;
        IdentityStore identityStore = null;

        // Check all stores and stop when one marks it as invalid.
        for (IdentityStore authenticationIdentityStore : validatingIdentityStores) {
            CredentialValidationResult temp = authenticationIdentityStore.validate(credential);
            switch (temp.getStatus()) {

                case NOT_VALIDATED:
                    // Don't do anything
                    break;
                case INVALID:
                    validationResult = temp;
                    break;
                case VALID:
                    validationResult = temp;
                    identityStore = authenticationIdentityStore;
                    break;
                default:
                    throw new IllegalArgumentException("Value not supported " + temp.getStatus());
            }
            if (validationResult != null && validationResult.getStatus() == CredentialValidationResult.Status.INVALID) {
                break;
            }
        }

        if (validationResult == null) {
            // No authentication store at all
            return INVALID_RESULT;
        }

        if (validationResult.getStatus() != VALID) {
            // No store validated (authenticated), no need to continue
            return validationResult;
        }

        CallerPrincipal callerPrincipal = validationResult.getCallerPrincipal();

        Set<String> groups = new HashSet<>();
        if (identityStore.validationTypes().contains(PROVIDE_GROUPS)) {
            groups.addAll(validationResult.getCallerGroups());
        }

        // Ask all stores that were configured for authorization to get the groups for the
        // authenticated caller
        for (IdentityStore authorizationIdentityStore : groupProvidingIdentityStores) {
            groups.addAll(authorizationIdentityStore.getCallerGroups(validationResult));
        }

        return new CredentialValidationResult(callerPrincipal, groups);

    }
}
