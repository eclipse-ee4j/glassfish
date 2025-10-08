/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.glassfish.test.security.retranslate.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.auth.message.callback.PasswordValidationCallback;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PrincipalMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import static jakarta.security.jacc.PolicyContext.PRINCIPAL_MAPPER;

@ApplicationScoped
public class ApiKeyMechanism implements HttpAuthenticationMechanism {

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request,
                                                HttpServletResponse response,
                                                HttpMessageContext context) {

        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null || apiKey.isBlank()) {
            // No credentials here, let other mechanisms run or fall through to challenge
            return context.doNothing();
        }

        PasswordValidationCallback passwordValidation =
            new PasswordValidationCallback(
                context.getClientSubject(), "javajoe", apiKey.toCharArray());

        boolean authenticated = false;
        try {
            context.getHandler().handle(new Callback[] { passwordValidation });

            authenticated = passwordValidation.getResult();
        } catch (IOException | UnsupportedCallbackException e) {
            e.printStackTrace();
        }

        if (authenticated) {
            PrincipalMapper mapper = PolicyContext.get(PRINCIPAL_MAPPER);
            Principal callerPrincipal = mapper.getCallerPrincipal(context.getClientSubject());
            Set<String> roles = mapper.getMappedRoles(context.getClientSubject());

            return context.notifyContainerAboutLogin(
                callerPrincipal, roles);
        }

        // Tell the client how to authenticate (header name + 401)
        response.setHeader("X-API-Key", "realm=\"jakarta\"");
        return context.responseUnauthorized();
    }
}
