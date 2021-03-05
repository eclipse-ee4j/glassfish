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

package com.sun.enterprise.security.jmac.config;

/**
 * This is used to do HttpServlet security for app server
 */
public class HttpServletConstants {
    public static final String AUTH_TYPE = "jakarta.servlet.http.authType";
    public static final String WEB_BUNDLE = "WEB_BUNDLE";
    public static final String POLICY_CONTEXT = "jakarta.security.jacc.PolicyContext";
    public static final String IS_MANDATORY = "jakarta.security.auth.message.MessagePolicy.isMandatory";
    public static final String REGISTER_SESSION = "jakarta.servlet.http.registerSession";
    @Deprecated
    public static final String REGISTER_WITH_AUTHENTICATOR = "com.sun.web.RealmAdapter.register";

    private HttpServletConstants() {
    }
}
