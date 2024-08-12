/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.security.ee.web.integration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jvnet.hk2.annotations.Contract;

/**
 * Web specific Programmatic Login An implementation of this will be injected into
 * com.sun.appserv.security.api.ProgrammaticLogin
 */
@Contract
public interface WebProgrammaticLogin {

    /**
     * Login and set up principal in request and session. This implements programmatic login for servlets.
     *
     * <P>
     * Due to a number of bugs in RI the security context is not shared between web container and ejb container. In order
     * for an identity established by programmatic login to be known to both containers, it needs to be set not only in the
     * security context but also in the current request and, if applicable, the session object. If a session does not exist
     * this method does not create one.
     *
     * <P>
     * See bugs 4646134, 4688449 and other referenced bugs for more background.
     *
     * <P>
     * Note also that this login does not hook up into SSO.
     *
     * @param user User name to login.
     * @param password User password.
     * @param request HTTP request object provided by caller application. It should be an instance of HttpRequestFacade.
     * @param response HTTP response object provided by called application. It should be an instance of HttpServletResponse.
     * This is not used currently.
     * @param realm the realm name to be authenticated to. If the realm is null, authentication takes place in default realm
     * @returns A Boolean object; true if login succeeded, false otherwise.
     * @see com.sun.enterprise.security.ee.authentication.ProgrammaticLogin
     * @throws Exception on login failure.
     *
     */
    Boolean login(String user, char[] password, String realm, HttpServletRequest request, HttpServletResponse response);

    /**
     * Logout and remove principal in request and session.
     *
     * @param request HTTP request object provided by caller application. It should be an instance of HttpRequestFacade.
     * @param response HTTP response object provided by called application. It should be an instance of HttpServletResponse.
     * This is not used currently.
     * @returns A Boolean object; true if login succeeded, false otherwise.
     * @see com.sun.enterprise.security.ee.authentication.ProgrammaticLogin
     * @throws Exception any exception encountered during logout operation
     */
    Boolean logout(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
