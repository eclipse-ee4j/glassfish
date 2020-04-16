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
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;

import java.util.HashSet;

import javax.enterprise.context.RequestScoped;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.credential.CallerOnlyCredential;
import javax.security.enterprise.credential.Credential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequestScoped
public class TestAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {

        if (httpMessageContext.isAuthenticationRequest()) {

            Credential credential = httpMessageContext.getAuthParameters().getCredential();
            if (!(credential instanceof CallerOnlyCredential)) {
                throw new IllegalStateException("This authentication mechanism requires a programmatically provided CallerOnlyCredential");
            }

            CallerOnlyCredential callerOnlyCredential = (CallerOnlyCredential) credential;

            if ("reza".equals(callerOnlyCredential.getCaller())) {
                return httpMessageContext.notifyContainerAboutLogin("reza", new HashSet<>(asList("foo", "bar")));
            }

            if ("rezax".equals(callerOnlyCredential.getCaller())) {
                throw new AuthenticationException();
            }

            return SEND_FAILURE;

        }

        return httpMessageContext.doNothing();
    }

}
