/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.config.serverbeans.SecureAdmin;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.security.Principal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.glassfish.common.util.admin.AdminAuthenticator.AuthenticatorType;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.LocalPassword;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Handles callbacks for admin authentication other than user-provided username and password, such as the local
 * password, a limited-use token, a ReST token.
 * <p>
 * Note that some of the information the callback handler stores is really for the use of the admin LoginModule. But
 * because we don't control how the login module is instantiated or initialized - but we do control that for the
 * callback handler - we can put that information here. This callback handler sets the info in the callback, which is
 * then available to the LoginModule.
 *
 * @author tjquinn
 */
public class AdminCallbackHandler implements CallbackHandler {

    public static final String COOKIE_REST_TOKEN = "gfresttoken";
    public static final String HEADER_X_AUTH_TOKEN = "X-Auth-Token";

    private static final Level PROGRESS_LEVEL = Level.FINE;

    private static final Logger logger = GenericAdminAuthenticator.ADMSEC_LOGGER;

    private final Request request;

    private Map<String, String> headers;

    private static final String BASIC = "Basic ";

    private final Principal clientPrincipal;
    private final String originHost;

    private final PasswordAuthentication passwordAuthentication;

    private final String specialAdminIndicator;
    private final String token;
    private final String defaultAdminUsername;
    private final LocalPassword localPassword;
    private final ServiceLocator serviceLocator;

    public AdminCallbackHandler(final ServiceLocator serviceLocator, final Request request, final String alternateHostName,
            final String defaultAdminUsername, final LocalPassword localPassword) throws IOException {
        this.serviceLocator = serviceLocator;
        this.request = request;
        this.defaultAdminUsername = defaultAdminUsername;
        this.localPassword = localPassword;
        clientPrincipal = request.getUserPrincipal();
        originHost = alternateHostName != null ? alternateHostName : request.getRemoteHost();
        passwordAuthentication = basicAuth();
        specialAdminIndicator = specialAdminIndicator();
        token = token();

    }

    ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    private static Map<String, String> headers(final Request req) {
        final Map<String, String> result = new HashMap<>();
        for (String headerName : req.getHeaderNames()) {
            result.put(headerName(headerName), req.getHeader(headerName));
        }
        return result;
    }

    //    private List<String> headers(final String headerName) {
    //        return headers.get(headerName);
    //    }

    private static String headerName(final String headerName) {
        return headerName.toLowerCase(Locale.ENGLISH);
    }

    private synchronized Map<String, String> headers() {
        if (headers == null) {
            headers = headers(request);
        }
        return headers;
    }

    private String header(final String headerName) {
        //        final List<String> matches = headers(headerName);
        //        if (matches != null && matches.size() > 0) {
        //            return matches.get(0);
        //        }
        //        return null;
        return headers().get(headerName(headerName));
    }

    private PasswordAuthentication basicAuth() throws IOException {
        final String authHeader = header("Authorization");
        if (authHeader == null) {
            logger.log(PROGRESS_LEVEL, "No Authorization header found; preparing default with username {0} and empty password",
                    defaultAdminUsername);
            return new PasswordAuthentication(defaultAdminUsername, new char[0]);
        }

        String enc = authHeader.substring(BASIC.length());
        String dec = new String(Base64.getDecoder().decode(enc), UTF_8);
        int i = dec.indexOf(':');
        if (i < 0) {
            logger.log(PROGRESS_LEVEL,
                    "Authorization header contained no : to separate the username from the password; proceeding with an empty username and empty password");
            return new PasswordAuthentication("", new char[0]);
        }
        final char[] password = dec.substring(i + 1).toCharArray();
        String username = dec.substring(0, i);
        if (username.isEmpty() && !localPassword.isLocalPassword(new String(password))) {
            logger.log(PROGRESS_LEVEL,
                    "Authorization header contained no username and the password is not the local password, so continue with the default username {0}",
                    defaultAdminUsername);
            username = defaultAdminUsername;
        }
        logger.log(PROGRESS_LEVEL, "basicAuth processing returning PasswordAuthentication with username {0}", username);
        return new PasswordAuthentication(username, password);

    }

    private String specialAdminIndicator() {
        return header(SecureAdmin.ADMIN_INDICATOR_HEADER_NAME);
    }

    private String token() {
        return header(SecureAdmin.ADMIN_ONE_TIME_AUTH_TOKEN_HEADER_NAME);
    }

    private String restToken() {
        final Cookie[] cookies = request.getCookies();
        String result = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_REST_TOKEN.equals(cookie.getName())) {
                    result = cookie.getValue();
                }
            }
        }

        if (result == null) {
            result = request.getHeader(HEADER_X_AUTH_TOKEN);
        }
        return result;
    }

    public String getRemoteHost() {
        return originHost;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback cb : callbacks) {
            if (cb instanceof NameCallback) {
                ((NameCallback) cb).setName(passwordAuthentication.getUserName());
            } else if (cb instanceof PasswordCallback) {
                ((PasswordCallback) cb).setPassword(passwordAuthentication.getPassword());
            } else if (cb instanceof TextInputCallback) {
                final TextInputCallback ticb = (TextInputCallback) cb;
                final String prompt = ticb.getPrompt();
                if (AuthenticatorType.ADMIN_INDICATOR.name().equals(prompt)) {
                    ticb.setText(specialAdminIndicator());
                } else if (AuthenticatorType.ADMIN_TOKEN.name().equals(prompt)) {
                    ticb.setText(token());
                } else if (AuthenticatorType.REMOTE_HOST.name().equals(prompt)) {
                    ticb.setText(remoteHost());
                } else if (AuthenticatorType.REST_TOKEN.name().equals(prompt)) {
                    ticb.setText(restToken());
                } else if (AuthenticatorType.REMOTE_ADDR.name().equals(prompt)) {
                    ticb.setText(remoteAddr());
                }
            } else if (cb instanceof AdminLoginModule.PrincipalCallback) {
                ((AdminLoginModule.PrincipalCallback) cb).setPrincipal(clientPrincipal);
            }
        }
    }

    PasswordAuthentication pw() {
        return passwordAuthentication;
    }

    Principal clientPrincipal() {
        return clientPrincipal;
    }

    String tkn() {
        return token;
    }

    String remoteHost() {
        return originHost;
    }

    String adminIndicator() {
        return specialAdminIndicator;
    }

    String remoteAddr() {
        return request.getRemoteAddr();
    }

}
