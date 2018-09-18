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


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.credential.RememberMeCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.RememberMeIdentityStore;

import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;

@ApplicationScoped
public class TestRememberMeIdentityStore implements RememberMeIdentityStore {

    private final Map<String, CredentialValidationResult> identities = new ConcurrentHashMap<>();

    @Override
    public CredentialValidationResult validate(RememberMeCredential credential) {
        if (identities.containsKey(credential.getToken())) {
            return identities.get(credential.getToken());
        }

        return INVALID_RESULT;
    }

    @Override
    public String generateLoginToken(CallerPrincipal callerPrincipal, Set<String> groups) {
        String token = UUID.randomUUID().toString();

        // NOTE: FOR EXAMPLE ONLY. AS TOKENKEY WOULD EFFECTIVELY BECOME THE REPLACEMENT PASSWORD
        // IT SHOULD NORMALLY NOT BE STORED DIRECTLY BUT EG USING STRONG HASHING
        identities.put(token, new CredentialValidationResult(callerPrincipal, groups));

        return token;
    }

    @Override
    public void removeLoginToken(String token) {
        identities.remove(token);
    }

}
