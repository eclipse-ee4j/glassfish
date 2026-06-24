/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.web.security.realmadapter;

import java.io.IOException;
import java.security.Principal;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.deploy.LoginConfig;

public class AuthenticatorProxy extends AuthenticatorBase {

    public static final String PROXY_AUTH_TYPE = "PLUGGABLE_PROVIDER";

    private final AuthenticatorBase authBase;
    private final Principal principal;
    private final String authType;

    public AuthenticatorProxy(Authenticator authenticator, Principal p, String authType) throws LifecycleException {

        this.authBase = (AuthenticatorBase) authenticator;
        this.principal = p;
        this.authType = authType == null ? PROXY_AUTH_TYPE : authType;

        setCache(authBase.getCache());
        setContainer(authBase.getContainer());
        start(); // finds sso valve and sets its value in proxy
    }

    @Override
    public boolean getCache() {
        return authBase.getCache();
    }

    @Override
    public Container getContainer() {
        return authBase.getContainer();
    }

    @Override
    public boolean authenticate(HttpRequest request, HttpResponse response, LoginConfig config) throws IOException {
        if (cache) {
            getSession(request, true);
        }

        register(request, response, this.principal, this.authType, this.principal.getName(), null);
        return true;
    }

    @Override
    public String getAuthMethod() {
        return authType;
    }
}
