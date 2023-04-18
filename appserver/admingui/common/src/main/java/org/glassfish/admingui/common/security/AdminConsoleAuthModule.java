/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation. All rights reserved.
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

package org.glassfish.admingui.common.security;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.security.SecurityServicesUtil;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.RestResponse;
import org.glassfish.admingui.common.util.RestUtil;
import org.glassfish.common.util.InputValidationUtil;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import static com.sun.logging.LogCleanerUtil.neutralizeForLog;

/**
 * This class is responsible for providing the Authentication support needed by the admin console to both access the
 * admin console pages as well as invoke REST requests.
 */
public class AdminConsoleAuthModule implements ServerAuthModule {
    // public static final String TOKEN_ADMIN_LISTENER_PORT = "${ADMIN_LISTENER_PORT}";

    private CallbackHandler handler = null;

    private String restURL = null;

    private String loginPage = null;

    private String loginErrorPage = null;

    private static final Class[] SUPPORTED_MESSAGE_TYPES = new Class[] { HttpServletRequest.class, HttpServletResponse.class };

    private static final String SAVED_SUBJECT = "Saved_Subject";

    private static final String USER_NAME = "userName";

    private static final String ORIG_REQUEST_PATH = "origRequestPath";

    private static final String RESPONSE_TYPE = "application/json";

    /**
     * The Session key for the REST Server Name.
     */
    public static final String REST_SERVER_NAME = "serverName";

    /**
     * The Session key for the REST Server Port.
     */
    public static final String REST_SERVER_PORT = "serverPort";

    /**
     * The Session key for the REST authentication token.
     */
    public static final String REST_TOKEN = "__rTkn__";

    private static final Logger logger = GuiUtil.getLogger();

    /**
     * This method configures this AuthModule and makes sure all the information needed to continue is present.
     */
    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) throws AuthException {
        this.handler = handler;
        if (options != null) {
            this.loginPage = (String) options.get("loginPage");
            if (loginPage == null) {
                throw new AuthException(
                        "'loginPage' " + "must be supplied as a property in the provider-config " + "in the domain.xml file!");
            }
            this.loginErrorPage = (String) options.get("loginErrorPage");
            if (loginErrorPage == null) {
                throw new AuthException(
                        "'loginErrorPage' " + "must be supplied as a property in the provider-config " + "in the domain.xml file!");
            }
            ServiceLocator habitat = SecurityServicesUtil.getInstance().getHabitat();
            Domain domain = habitat.getService(Domain.class);
            NetworkListener adminListener = domain.getServerNamed("server").getConfig().getNetworkConfig()
                    .getNetworkListener("admin-listener");
            SecureAdmin secureAdmin = habitat.getService(SecureAdmin.class);

            final String host = adminListener.getAddress();
            // Save the REST URL we need to authenticate the user.
            this.restURL = (SecureAdmin.isEnabled(secureAdmin) ? "https://" : "http://")
                    + (host.equals("0.0.0.0") ? "localhost" : host) + ":" + adminListener.getPort() + "/management/sessions";
        }
    }

    /**
     *
     */
    @Override
    public Class[] getSupportedMessageTypes() {
        return SUPPORTED_MESSAGE_TYPES;
    }

    /**
     * This is where the validation happens...
     */
    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
        // Make sure we need to check...
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
        if (!isMandatory(messageInfo) && !request.getRequestURI().endsWith("/j_security_check")) {
            return AuthStatus.SUCCESS;
        }

        // See if we've already checked...
        HttpSession session = request.getSession(true);
        if (session == null) {
            return AuthStatus.FAILURE;
        }

        Subject savedClientSubject = (Subject) session.getAttribute(SAVED_SUBJECT);
        if (savedClientSubject != null) {
            // Copy all principals...
            clientSubject.getPrincipals().addAll(savedClientSubject.getPrincipals());
            clientSubject.getPublicCredentials().addAll(savedClientSubject.getPublicCredentials());
            clientSubject.getPrivateCredentials().addAll(savedClientSubject.getPrivateCredentials());
            return AuthStatus.SUCCESS;
        }

        // See if we've already calculated the serverName / serverPort
        if (session.getAttribute(REST_SERVER_NAME) == null) {
            // Save this for use later...
            URL url = null;
            try {
                url = new URL(restURL);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("Unable to parse REST URL: (" + restURL + ")", ex);
            }
            session.setAttribute(REST_SERVER_NAME, url.getHost());
            session.setAttribute(REST_SERVER_PORT, url.getPort());
        }

        // See if the username / password has been passed in...
        String username = request.getParameter("j_username");
        char[] password = request.getParameter("j_password") != null ? request.getParameter("j_password").toCharArray() : null;
        if ((username == null) || (password == null) || !request.getMethod().equalsIgnoreCase("post")) {
            // Not passed in, show the login page...
            String origPath = request.getRequestURI();
            String qs = request.getQueryString();
            if ((qs != null) && (!qs.isEmpty())) {
                origPath += "?" + qs;
            }
            session.setAttribute(ORIG_REQUEST_PATH, origPath);
            RequestDispatcher rd = request.getRequestDispatcher(loginPage);
            try {
                rd.forward(request, response);
            } catch (Exception ex) {
                AuthException ae = new AuthException();
                ae.initCause(ex);
                throw ae;
            }
            return AuthStatus.SEND_CONTINUE;
        }

        Client client2 = RestUtil.initialize(ClientBuilder.newBuilder()).build();
        WebTarget target = client2.target(restURL);
        target.register(HttpAuthenticationFeature.basic(username, new String(password)));
        MultivaluedMap payLoad = new MultivaluedHashMap();
        payLoad.putSingle("remoteHostName", request.getRemoteHost());

        Response resp = target.request(RESPONSE_TYPE).post(Entity.entity(payLoad, MediaType.APPLICATION_FORM_URLENCODED), Response.class);
        RestResponse restResp = RestResponse.getRestResponse(resp);
        Arrays.fill(password, ' ');
        // Check to see if successful..
        if (restResp.isSuccess()) {
            // Username and Password sent in... validate them!
            CallerPrincipalCallback cpCallback = new CallerPrincipalCallback(clientSubject, username);
            try {
                handler.handle(new Callback[] { /* pwdCallback, */cpCallback });
            } catch (Exception ex) {
                AuthException ae = new AuthException();
                ae.initCause(ex);
                throw ae;
            }

            request.changeSessionId();

            // Get the "extraProperties" section of the response...
            Object obj = restResp.getResponse().get("data");
            Map extraProperties = null;
            if ((obj != null) && (obj instanceof Map)) {
                obj = ((Map) obj).get("extraProperties");
                if ((obj != null) && (obj instanceof Map)) {
                    extraProperties = (Map) obj;
                }
            }

            // Save the Rest Token...
            if (extraProperties != null) {
                session.setAttribute(REST_TOKEN, extraProperties.get("token"));
            }

            // Save the Subject...
            session.setAttribute(SAVED_SUBJECT, clientSubject);

            // Save the userName
            session.setAttribute(USER_NAME, username);

            try {
                // Redirect...
                String origRequest = (String) session.getAttribute(ORIG_REQUEST_PATH);
                // Explicitly test for favicon.ico, as Firefox seems to ask for this on
                // every page
                if ((origRequest == null) || "/favicon.ico".equals(origRequest)) {
                    origRequest = "/index.jsf";
                }
                logger.log(Level.INFO, "Redirecting to {0}", neutralizeForLog(origRequest));
                if (InputValidationUtil.validateStringforCRLF(origRequest)) {
                    response.sendError(403, "Forbidden");
                }
                response.sendRedirect(response.encodeRedirectURL(InputValidationUtil.removeLinearWhiteSpaces(origRequest)));
            } catch (Exception ex) {
                AuthException ae = new AuthException();
                ae.initCause(ex);
                throw ae;
            }

            // Continue...
            return AuthStatus.SEND_CONTINUE;
        } else {
            int status = restResp.getResponseCode();
            if (status == 403) {
                request.setAttribute("errorText", GuiUtil.getMessage("alert.ConfigurationError"));
                request.setAttribute("messageText", GuiUtil.getMessage("alert.EnableSecureAdmin"));
            }

            RequestDispatcher rd = request.getRequestDispatcher(this.loginErrorPage);
            try {
                rd.forward(request, response);
            } catch (Exception ex) {
                AuthException ae = new AuthException();
                ae.initCause(ex);
                throw ae;
            }

            return AuthStatus.SEND_FAILURE;
        }
    }

    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        // FIXME: Cleanup...
    }

    private boolean isMandatory(MessageInfo messageInfo) {
        return Boolean.valueOf((String) messageInfo.getMap().get("jakarta.security.auth.message.MessagePolicy.isMandatory"));
    }
}
