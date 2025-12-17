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

package com.sun.enterprise.security.ee.authentication;

import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.UsernamePasswordStore;
import com.sun.enterprise.security.auth.login.LoginCallbackHandler;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.common.SecurityConstants;
import com.sun.enterprise.security.ee.web.integration.WebProgrammaticLogin;
import com.sun.logging.LogDomains;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.callback.CallbackHandler;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.security.common.Util.isEmbeddedServer;

/**
 * Implement programmatic login.
 *
 * <P>
 * This class allows deployed applications to supply a name and password directly to the security service. This info
 * will be used to attempt to login to the current realm. If authentication succeeds, a security context is established
 * as this user.
 *
 * <P>
 * This allows applications to programmatically handle authentication. The use of this mechanism is not recommended
 * since it bypasses the standard J2EE mechanisms and places all burden on the application developer.
 *
 * <P>
 * Invoking this method requires the permission ProgrammaticLoginPermission with the method name being invoked.
 *
 * <P>
 * There are two forms of the login method, one which includes the HTTP request and response objects for use by servlets
 * and one which can be used by EJBs.
 *
 *
 */
@Service
@PerLookup
public class ProgrammaticLogin {

    private static final Logger logger = LogDomains.getLogger(ProgrammaticLogin.class, LogDomains.SECURITY_LOGGER);

    private static CallbackHandler handler = new LoginCallbackHandler(false);

    private WebProgrammaticLogin webProgrammaticLogin;

    public ProgrammaticLogin() {
        if (SecurityServicesUtil.getInstance() != null) {
            resolveWebProgrammaticLogin();
        }
    }

    /**
     * Password should be used as a char[]
     */
    public Boolean login(final String user, final String password, final String realm, boolean errors) throws Exception {
        return login(user, password.toCharArray(), realm, errors);
    }

    /**
     * Attempt to login.
     *
     * <P>
     * Upon successful return from this method the SecurityContext will be set in the name of the given user as its Subject.
     *
     * <p>
     * On client side, realm and errors parameters will be ignored and the actual login will not occur until we actually
     * access a resource requiring a login. And a java.rmi.AccessException with COBRA NO_PERMISSION will occur when actual
     * login is failed.
     *
     * <P>
     * This method is intented primarily for EJBs wishing to do programmatic login. If servlet code used this method the
     * established identity will be propagated to EJB calls but will not be used for web container manager authorization. In
     * general servlets should use the servlet-specific version of login instead.
     *
     * @param user User name.
     * @param password Password for user.
     * @param realm the realm name in which the user should be logged in.
     * @param errors errors=true, propagate any exception encountered to the user errors=false, no exceptions are
     * propagated.
     * @return Boolean containing true or false to indicate success or failure of login.
     * @throws Exception any exception encountered during Login.
     */
    public Boolean login(final String user, final char[] password, final String realm, boolean errors) throws Exception {
        Boolean authenticated = null;
        try {
            // try to login.
            // if realm is null, LCD will log into the default realm
            if (SecurityServicesUtil.getInstance() != null && SecurityServicesUtil.getInstance().isServer() || isEmbeddedServer()) {
                LoginContextDriver.login(user, password, realm);
            } else {
                int type = SecurityConstants.USERNAME_PASSWORD;

                // should not set realm here
                // Bugfix# 6387278. The UsernamePasswordStore
                // abstracts the thread-local/global details
                UsernamePasswordStore.set(user, password);

                try {
                    LoginContextDriver.doClientLogin(type, handler);
                } finally {
                    // For security, if thread-local no need to
                    // save the username/password state
                    UsernamePasswordStore.resetThreadLocalOnly();
                }
            }

            authenticated = true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "prog.login.failed", e);
            if (errors == true) { // propagate the exception ahead
                throw e;
            }
            authenticated = false;
        }

        return authenticated;
    }

    /*
     * Use of the char[] as password is encouraged
     */
    public Boolean login(final String user, final String password) {
        return login(user, password.toCharArray());
    }

    /**
     * Attempt to login.
     *
     * <P>
     * Upon successful return from this method the SecurityContext will be set in the name of the given user as its Subject.
     *
     * <p>
     * On client side, the actual login will not occur until we actually access a resource requiring a login. And a
     * java.rmi.AccessException with COBRA NO_PERMISSION will occur when actual login is failed.
     *
     * <P>
     * This method is intented primarily for EJBs wishing to do programmatic login. If servlet code used this method the
     * established identity will be propagated to EJB calls but will not be used for web container manager authorization. In
     * general servlets should use the servlet-specific version of login instead.
     *
     * @param user User name.
     * @param password Password for user.
     * @return Boolean containing true or false to indicate success or failure of login.
     */
    public Boolean login(final String user, final char[] password) {
        // call login with realm-name = null and request for errors = false
        Boolean authenticated = null;
        try {
            authenticated = login(user, password, null, false);
        } catch (Exception e) {
            // sanity checking, will never come here
            authenticated = false;
        }
        return authenticated;
    }

    /*
     * Use of the char[] as password is encouraged
     */
    public Boolean login(final String user, final String password, final String realm, final HttpServletRequest request,
            final HttpServletResponse response, boolean errors) throws Exception {
        return login(user, password.toCharArray(), realm, request, response, errors);
    }

    /**
     * Attempt to login. This method is specific to servlets (and JSPs).
     *
     * <P>
     * Upon successful return from this method the SecurityContext will be set in the name of the given user as its Subject.
     * In addition, the principal stored in the request is set to the user name. If a session is available, its principal is
     * also set to the user provided.
     *
     * @returns Boolean containing true or false to indicate success or failure of login.
     * @param realm
     * @param errors
     * @param user User name.
     * @param password Password for user.
     * @param request HTTP request object provided by caller application. It should be an instance of HttpRequestFacade.
     * @param response HTTP response object provided by called application. It should be an instance of HttpServletResponse.
     * @throws Exception any exceptions encountered during login
     * @return Boolean indicating true for successful login and false otherwise
     */
    public Boolean login(final String user, final char[] password, final String realm, final HttpServletRequest request,
            final HttpServletResponse response, boolean errors) throws Exception {
        Boolean authenticated = null;
        try {
            // try to login.
            authenticated = webProgrammaticLogin.login(user, password, realm, request, response);
        } catch (Exception e) {
            if (errors == true) {
                throw e;
            }
            authenticated = false;
        }

        return authenticated;
    }

    /*
     * Use of char[] as password is encouraged
     */
    public Boolean login(final String user, final String password, final HttpServletRequest request, final HttpServletResponse response) {
        return login(user, password.toCharArray(), request, response);
    }

    /**
     * Attempt to login. This method is specific to servlets (and JSPs).
     *
     * <P>
     * Upon successful return from this method the SecurityContext will be set in the name of the given user as its Subject.
     * In addition, the principal stored in the request is set to the user name. If a session is available, its principal is
     * also set to the user provided.
     *
     * @param user User name.
     * @param password Password for user.
     * @param request HTTP request object provided by caller application. It should be an instance of HttpRequestFacade.
     * @param response HTTP response object provided by called application. It should be an instance of HttpServletResponse.
     * @return Boolean containing true or false to indicate success or failure of login.
     *
     */
    public Boolean login(final String user, final char[] password, final HttpServletRequest request, final HttpServletResponse response) {
        Boolean authenticated = null;
        try {
            // pass a null realmname and errors=false
            authenticated = login(user, password, null, request, response, false);
        } catch (Exception e) {
            // sanity check will never come here
            authenticated = false;
        }

        return authenticated;
    }

    /**
     * Attempt to logout.
     *
     * @returns Boolean containing true or false to indicate success or failure of logout.
     *
     */
    public Boolean logout() {
        Boolean loggedout = null;
        try {
            loggedout = logout(false);
        } catch (Exception e) {
            // sanity check will never come here
            loggedout = false;
        }
        return loggedout;
    }

    /**
     * Attempt to logout.
     *
     * @param errors, errors = true, the method will propagate the exceptions encountered while logging out, errors=false
     * will return a Boolean value of false indicating failure of logout
     * @return Boolean containing true or false to indicate success or failure of logout.
     * @throws Exception encountered while logging out, if errors==false
     *
     */
    public Boolean logout(boolean errors) throws Exception {
        Boolean loggedout = null;
        try {
            if (SecurityServicesUtil.getInstance() != null && SecurityServicesUtil.getInstance().isServer()) {
                LoginContextDriver.logout();
            } else {
                // Reset the username/password state on logout
                UsernamePasswordStore.reset();

                LoginContextDriver.doClientLogout();

                // If user try to access a protected resource after here
                // then it will prompt for password in appclient or
                // just fail in standalone client.
            }

            loggedout = true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "prog.logout.failed", e);
            if (errors) {
                throw e;
            }
            loggedout = false;
        }

        return loggedout;
    }

    /**
     * Attempt to logout. Also removes principal from request (and session if available).
     *
     * @returns Boolean containing true or false to indicate success or failure of logout.
     *
     */
    public Boolean logout(final HttpServletRequest request, final HttpServletResponse response) {
        Boolean loggedout = null;
        try {
            loggedout = logout(request, response, false);
        } catch (Exception e) {
            // Sanity check, will never come here
            loggedout = false;
        }
        return loggedout;
    }

    /**
     * Attempt to logout. Also removes principal from request (and session if available).
     *
     * @param errors, errors = true, the method will propagate the exceptions encountered while logging out, errors=false
     * will return a Boolean value of false indicating failure of logout
     *
     * @return Boolean containing true or false to indicate success or failure of logout.
     * @throws Exception, exception encountered while logging out and if errors == true
     */
    public Boolean logout(final HttpServletRequest request, final HttpServletResponse response, boolean errors) throws Exception {
        Boolean loggedout = null;
        try {
            loggedout = webProgrammaticLogin.logout(request, response);
        } catch (Exception e) {
            if (errors) {
                throw e;
            }
            loggedout = false;
        }

        return loggedout;
    }

    private void resolveWebProgrammaticLogin() {
        this.webProgrammaticLogin =
            SecurityServicesUtil.getInstance()
                                .getServiceLocator()
                                .getService(WebProgrammaticLogin.class);
    }

}
