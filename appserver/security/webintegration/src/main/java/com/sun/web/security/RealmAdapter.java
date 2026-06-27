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

package com.sun.web.security;

import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.security.AppCNonceCacheMap;
import com.sun.enterprise.security.CNonceCacheFactory;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.WebSecurityDeployerProbeProvider;
import com.sun.enterprise.security.auth.login.DistinguishedPrincipalCredential;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.ee.authentication.jakarta.AuthMessagePolicy;
import com.sun.enterprise.security.ee.authentication.jakarta.ConfigDomainParser;
import com.sun.enterprise.security.ee.authentication.jakarta.callback.ServerContainerCallbackHandler;
import com.sun.enterprise.security.ee.authorization.GlassFishAuthorizationService;
import com.sun.enterprise.security.ee.authorization.GlassFishAuthorizationService.Access;
import com.sun.enterprise.security.ee.web.integration.WebPrincipal;
import com.sun.enterprise.security.ee.web.integration.WebSecurityManager;
import com.sun.enterprise.security.ee.web.integration.WebSecurityManagerFactory;
import com.sun.enterprise.security.integration.RealmInitializer;
import com.sun.web.security.realmadapter.AuthenticatorProxy;
import com.sun.web.security.realmadapter.CatalinaRealmToLoginModule;
import com.sun.web.security.realmadapter.FormInfo;
import com.sun.web.security.realmadapter.HttpRedirectGenerator;
import com.sun.web.security.realmadapter.SubjectUtils;
import com.sun.web.security.realmadapter.wrappers.HttpRequestWrapper;
import com.sun.web.security.realmadapter.wrappers.HttpResponseWrapper;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.config.ServerAuthContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.RealmBase;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.epicyro.config.helper.Caller;
import org.glassfish.epicyro.config.helper.HttpServletConstants;
import org.glassfish.epicyro.config.servlet.HttpMessageInfo;
import org.glassfish.epicyro.services.BaseAuthenticationService;
import org.glassfish.epicyro.services.DefaultAuthenticationService;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.security.common.Group;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.security.ee.authentication.jakarta.AuthMessagePolicy.WEB_BUNDLE;
import static com.sun.enterprise.security.ee.authorization.AuthorizationUtil.getContextID;
import static com.sun.enterprise.security.ee.authorization.GlassFishAuthorizationService.Access.DENIED;
import static com.sun.enterprise.security.ee.authorization.GlassFishAuthorizationService.Access.PERMITTED_WITH_SSL;
import static com.sun.enterprise.util.Utility.isAnyNull;
import static com.sun.enterprise.util.Utility.isEmpty;
import static com.sun.web.security.WebSecurityResourceBundle.BUNDLE_NAME;
import static com.sun.web.security.WebSecurityResourceBundle.MSG_INVALID_REQUEST;
import static com.sun.web.security.WebSecurityResourceBundle.MSG_NO_WEB_SECURITY_MGR;
import static com.sun.web.security.realmadapter.AuthenticatorProxy.PROXY_AUTH_TYPE;
import static com.sun.web.security.realmadapter.SubjectUtils.copySubject;
import static com.sun.web.security.realmadapter.SubjectUtils.getGlassFishCallerPrincipal;
import static com.sun.web.security.realmadapter.SubjectUtils.reuseSessionSubject;
import static com.sun.web.security.realmadapter.SubjectUtils.toSubject;
import static com.sun.web.security.realmadapter.SubjectUtils.toSubjectCredential;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.apache.catalina.ContainerEvent.AFTER_AUTHENTICATION;
import static org.apache.catalina.ContainerEvent.AFTER_LOGOUT;
import static org.apache.catalina.ContainerEvent.AFTER_POST_AUTHENTICATION;
import static org.apache.catalina.ContainerEvent.BEFORE_AUTHENTICATION;
import static org.apache.catalina.ContainerEvent.BEFORE_LOGOUT;
import static org.apache.catalina.ContainerEvent.BEFORE_POST_AUTHENTICATION;
import static org.apache.catalina.Globals.WRAPPED_REQUEST;
import static org.apache.catalina.Globals.WRAPPED_RESPONSE;
import static org.glassfish.epicyro.config.helper.HttpServletConstants.IS_MANDATORY;
import static org.glassfish.epicyro.config.helper.HttpServletConstants.POLICY_CONTEXT;
import static org.glassfish.epicyro.config.helper.HttpServletConstants.REGISTER_SESSION;

/**
 * This is the realm adapter used to authenticate users and authorize access to web resources. The authenticate method
 * is called by Tomcat to authenticate users. The hasRole method is called by Tomcat during the authorization process.
 *
 * @author Harpreet Singh
 * @author JeanFrancois Arcand
 */
@Service
@PerLookup
public final class RealmAdapter extends RealmBase implements RealmInitializer, PostConstruct {

    public static final String SECURITY_CONTEXT = "SecurityContext";
    public static final String BASIC = "BASIC";
    public static final String FORM = "FORM";
    private static final String MSG_FORBIDDEN = "Access to the requested resource has been denied";

    private static final Logger LOG = Logger.getLogger(RealmAdapter.class.getName(), BUNDLE_NAME);
    private static final ResourceBundle resourceBundle = LOG.getResourceBundle();

    private static final String SERVER_AUTH_CONTEXT = "__jakarta.security.auth.message.ServerAuthContext";
    private static final String MESSAGE_INFO = "__jakarta.security.auth.message.MessageInfo";
    private static final WebSecurityDeployerProbeProvider websecurityProbeProvider = new WebSecurityDeployerProbeProvider();

    private WebBundleDescriptor webBundleDescriptor;
    private HashMap<String, String> runAsPrincipals;
    private String realmName;  // required for realm-per-app login

    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "J2EE-RI-RealmAdapter";

    /**
     * The context Id value needed for Jakarta Authorization
     */
    private String contextId;
    private Container virtualServer;

    /**
     * A <code>WebSecurityManager</code> object associated with a CONTEXT_ID
     */
    protected volatile WebSecurityManager webSecurityManager;

    protected boolean isCurrentURIincluded = false;

    private final static SecurityConstraint[] emptyConstraints = new SecurityConstraint[] {};
    /**
     * the default provider id for system apps if one has been established. the default provider for system apps is
     * established by defining a system property.
     */

    private String moduleID;

    private BaseAuthenticationService authenticationService;

    @Inject
    private ServerContext serverContext;

    @Inject
    private Provider<AppCNonceCacheMap> appCNonceCacheMapProvider;

    @Inject
    private Provider<CNonceCacheFactory> cNonceCacheFactoryProvider;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private NetworkConfig networkConfig;

    /**
     * The factory used for creating <code>WebSecurityManager</code> object.
     */
    @Inject
    protected WebSecurityManagerFactory webSecurityManagerFactory;

    private CatalinaRealmToLoginModule catalinaRealmToLoginModule;
    private HttpRedirectGenerator httpRedirectGenerator;
    private FormInfo formInfo;

    /**
     * ThreadLocal object to keep track of the reentrancy status of each thread. It contains a byte[] object whose single
     * element is either 0 (initial value or no reentrancy), or 1 (current thread is reentrant). When a thread exits the
     * implies method, byte[0] is always reset to 0.
     */
    private static ThreadLocal<byte[]> reentrancyStatus =
        ThreadLocal.withInitial(() ->  new byte[] { 0 });


    @Override
    public void postConstruct() {
        httpRedirectGenerator = new HttpRedirectGenerator(networkConfig.getNetworkListeners());
    }

    @Override
    public void initializeRealm(Object descriptor, String initialRealmName) {
        this.webBundleDescriptor = (WebBundleDescriptor) descriptor;

        realmName = findRealmName(initialRealmName);
        contextId = getContextID(webBundleDescriptor);
        String appName = webBundleDescriptor.getApplication().getAppName();
        moduleID = webBundleDescriptor.getModuleID();

        // For authentication, the Catalina native authentication mechanisms (called Authenticators there) call
        // into a Catalina native identity store (called "Catalina" Realm there).
        //
        // In GlassFish we route these to a LoginModule, which typically routes to a GlassFish Realm.
        //
        // E.g. org.apache.catalina.authenticator.BasicAuthenticator -> Catalina Realm (RealmAdapter) ->
        //      (via CatalinaRealmToLoginModule and LoginContextDriver) -> LDAPLoginModule -> LDAPRealm
        //
        // This flow is orthogonal to Jakarta Security / Jakarta Authentication and separately supported here.
        catalinaRealmToLoginModule =
            new CatalinaRealmToLoginModule(
                realmName, appName, moduleID, appCNonceCacheMapProvider, cNonceCacheFactoryProvider, () -> getWebSecurityManager(false));

        formInfo = new FormInfo(() -> (Context) getContainer());

        collectRunAsPrincipals();
    }

    /**
     * Sets the virtual server on which the web module (with which this RealmAdapter is associated with) has been deployed.
     *
     * @param container The virtual server
     */
    @Override
    public void setVirtualServer(Object container) {
        this.virtualServer = (Container) container;
    }

    @Override
    public void updateWebSecurityManager() {
        if (webSecurityManager == null) {
            webSecurityManager = getWebSecurityManager(true);
        }

        if (webSecurityManager != null) {
            try {
                webSecurityManager.release();
                webSecurityManager.destroy();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            webSecurityManager = webSecurityManagerFactory.createManager(null, webBundleDescriptor, true, serverContext);
            LOG.log(FINE, "WebSecurityManager for {0} has been updated", contextId);
        }
    }

    /**
     * Set the run-as principal into the SecurityContext when needed.
     *
     * <P>
     * This method will attempt to obtain the name of the servlet from the ComponentInvocation. Note that there may not be
     * one since this gets called also during internal processing (not clear..) not just part of servlet requests. However,
     * if it is not a servlet request there is no need (or possibility) to have a run-as setting so no further action is
     * taken.
     *
     * <P>
     * If the servlet name is present the runAsPrincipals cache is checked to find the run-as principal to use (if any). If
     * one is set, the SecurityContext is switched to this principal.
     *
     * @param componentInvocation The invocation object to process.
     *
     */
    public void preSetRunAsIdentity(ComponentInvocation componentInvocation) {
        // Optimization to avoid the expensive call to getServletName
        // for cases with no run-as descriptors
        if (isEmpty(runAsPrincipals)) {
            return;
        }

        String servletName = getServletName(componentInvocation);
        if (servletName == null) {
            return;
        }

        String runAs = runAsPrincipals.get(servletName);

        if (runAs != null) {
            // The existing SecurityContext is saved - however, this seems
            // meaningless - see bug 4757733. For now, keep it unchanged
            // in case there are some dependencies elsewhere in RI.
            componentInvocation.setOldSecurityContext(getSecurityContext());

            // Set the run-as principal into SecurityContext
            loginForRunAs(runAs);

            LOG.log(FINE, "The run-as principal for servlet {0} set to {1}", new Object[] {servletName, runAs});
        }
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public String getRealmName() {
        return realmName;
    }

    /**
     * Return <tt>true</tt> if Jakarta Authentication is available.
     *
     * @return <tt>true</tt> if Jakarta Authentication is available.
     */
    @Override
    public boolean isSecurityExtensionEnabled(final ServletContext context) {
        if (authenticationService == null) {
            initAuthenticationService(context);
        }

        try {
            return authenticationService.getServerAuthConfig() != null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @returns null
     *          <ol>
     *          <li>if there are no security constraints defined on any of the web resources within
     *          the context
     *          <li>or if the target is a form login related page or target.
     *          </ol>
     *          otherwise return an empty array of SecurityConstraint.
     */
    @Override
    public SecurityConstraint[] findSecurityConstraints(HttpRequest request, Context context) {
        return findSecurityConstraints(context);
    }

    /**
     * @returns null
     *          <ol>
     *          <li>if there are no security constraints defined on any of the web resources within
     *          the context
     *          <li>or if the target is a form login related page or target.
     *          </ol>
     *          otherwise return an empty array of SecurityConstraint.
     */
    @Override
    public SecurityConstraint[] findSecurityConstraints(String requestPathMB, String httpMethod, Context context) {
       return findSecurityConstraints(context);
    }

    @Override
    public boolean hasUserDataPermission(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints) throws IOException {
        return hasUserDataPermission(request, response, constraints, null, null);
    }

    @Override
    public boolean hasUserDataPermission(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints, String uri, String method) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (httpServletRequest.getServletPath() == null) {
            request.setServletPath(getResourceName(httpServletRequest.getRequestURI(), httpServletRequest.getContextPath()));
        }

        LOG.log(FINE, "hasUserDataPermission called for principal {0} and context path {1}",
            new Object[] {httpServletRequest.getUserPrincipal(), httpServletRequest.getContextPath()});

        if (request.getRequest().isSecure()) {
            LOG.log(FINE, "request.getRequest().isSecure(): {0}", request.getRequest().isSecure());
            return true;
        }

        GlassFishAuthorizationService authorizationService = getAuthorizationService();
        if (authorizationService == null) {
            return false;
        }

        Access access = DENIED;
        try {
            access = authorizationService.hasUserDataPermission(httpServletRequest, uri, method);
        } catch (IllegalArgumentException e) {
            // End the request after getting IllegalArgumentException while checking
            // user data permission
            LOG.log(WARNING, MSG_INVALID_REQUEST, e);
            ((HttpServletResponse) response.getResponse()).sendError(SC_BAD_REQUEST, resourceBundle.getString(MSG_INVALID_REQUEST));
            return false;
        }

        // Only redirect if we are sure the user will be granted.
        // See bug 4947698

        // This method will return:
        // PERMITTED - if granted
        // DENIED - if not granted
        // PERMITTED_WITH_SSL - if the current transport is not granted, but a redirection can occur
        // so the grand will succeed.
        if (access == PERMITTED_WITH_SSL) {
            LOG.log(FINE, "Redirecting using SSL");
            return httpRedirectGenerator.redirect(request, response);
        }

        if (access == DENIED) {
            ((HttpServletResponse) response.getResponse()).sendError(SC_FORBIDDEN, "Access to the requested resource has been denied");
            return false;
        }

        return true;
    }

    @Override
    public int preAuthenticateCheck(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints, boolean disableProxyCaching, boolean securePagesWithPragma, boolean ssoEnabled) throws IOException {
        boolean isGranted = false;

        try {
            if (!isRequestAuthenticated(request)) {
                SecurityContext.setUnauthenticatedContext();
            }
            if (isJakartaAuthenticationEnabled()) {
                return AUTHENTICATE_NEEDED;
            }

            isGranted = invokeAuthorizationService(request, response, constraints);
        } catch (IOException iex) {
            throw iex;
        } catch (Throwable ex) {
            LOG.log(SEVERE, "Authentication passed, but authorization failed.", ex);
            ((HttpServletResponse) response.getResponse()).sendError(SC_SERVICE_UNAVAILABLE);
            response.setDetailMessage(MSG_FORBIDDEN);

            return AUTHENTICATED_NOT_AUTHORIZED;
        }

        if (isGranted) {
            if (isRequestAuthenticated(request)) {
                disableProxyCaching(request, response, disableProxyCaching, securePagesWithPragma);
                if (ssoEnabled) {
                    HttpServletRequest httpServletRequest = (HttpServletRequest) request.getRequest();
                    if (!getAuthorizationService().permitAll(httpServletRequest)) {
                        // create a session for protected sso association
                        httpServletRequest.getSession(true);
                    }
                }
            }

            return AUTHENTICATE_NOT_NEEDED;
        }

        if (isRequestAuthenticated(request)) {
            ((HttpServletResponse) response.getResponse()).sendError(SC_FORBIDDEN);
            response.setDetailMessage(MSG_FORBIDDEN);
            return AUTHENTICATED_NOT_AUTHORIZED;
        }

        disableProxyCaching(request, response, disableProxyCaching, securePagesWithPragma);

        return AUTHENTICATE_NEEDED;
    }

    @Override
    public boolean invokeAuthenticateDelegate(HttpRequest request, HttpResponse response, Context context, Authenticator authenticator, boolean calledFromAuthenticate) throws IOException {
        LoginConfig config = context.getLoginConfig();

        if (isJakartaAuthenticationEnabled()) {
            final SecurityContext securityContext = SecurityContext.getCurrent();
            // Jakarta Authentication is enabled for this application
            try {
                context.fireContainerEvent(BEFORE_AUTHENTICATION, null);
                RequestFacade requestFacade = (RequestFacade) request.getRequest();
                securityContext.setSessionPrincipal(requestFacade.getRequestPrincipal());
                return validate(request, response, config, authenticator, calledFromAuthenticate);
            } finally {
                securityContext.setSessionPrincipal(null);
                context.fireContainerEvent(AFTER_AUTHENTICATION, null);
            }
        }

        // Jakarta Authentication is not enabled. Use the current authenticator.
        return ((AuthenticatorBase) authenticator).authenticate(request, response, config);
    }

    @Override
    public Principal authenticate(HttpRequest request, String username, char[] password) {
        return catalinaRealmToLoginModule.authenticate(request, username, password);
    }

    @Override
    public Principal authenticate(HttpServletRequest httpServletRequest) {
        return catalinaRealmToLoginModule.authenticate(httpServletRequest);
    }

    @Override
    public Principal authenticate(HttpRequest request, X509Certificate certificates[]) {
        return catalinaRealmToLoginModule.authenticate(request, certificates);
    }

    @Override
    public boolean hasResourcePermission(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints, Context context) throws IOException {
        boolean isGranted = false;

        try {
            isGranted = invokeAuthorizationService(request, response, constraints);
        } catch (IOException iex) {
            throw iex;
        } catch (Throwable ex) {
            LOG.log(SEVERE, "Authentication passed, but authorization failed.", ex);
            ((HttpServletResponse) response.getResponse()).sendError(SC_SERVICE_UNAVAILABLE);
            response.setDetailMessage(MSG_FORBIDDEN);

            return isGranted;
        }

        if (isGranted) {
            return isGranted;
        }

        ((HttpServletResponse) response.getResponse()).sendError(SC_FORBIDDEN);
        response.setDetailMessage(MSG_FORBIDDEN);

        // invoking secureResponse
        invokePostAuthenticateDelegate(request, response, context);

        return isGranted;
    }

    @Override
    public boolean invokePostAuthenticateDelegate(HttpRequest request, HttpResponse response, Context context) throws IOException {
        boolean result = false;
        ServerAuthContext serverAuthContext = null;
        try {
            if (authenticationService != null) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request.getRequest();
                MessageInfo messageInfo = (MessageInfo) httpServletRequest.getAttribute(MESSAGE_INFO);
                if (messageInfo != null) {
                    // Jakarta Authentication is enabled for this application
                    serverAuthContext = (ServerAuthContext) messageInfo.getMap().get(SERVER_AUTH_CONTEXT);
                    if (serverAuthContext != null) {
                        try {
                            context.fireContainerEvent(BEFORE_POST_AUTHENTICATION, null);
                            AuthStatus authStatus = serverAuthContext.secureResponse(messageInfo, null); // null serviceSubject
                            result = AuthStatus.SUCCESS.equals(authStatus);
                        } finally {
                            context.fireContainerEvent(AFTER_POST_AUTHENTICATION, null);
                        }
                    }
                }
            }
        } catch (AuthException ex) {
            throw new IOException(ex);
        } finally {
            if (authenticationService != null && serverAuthContext != null) {
                if (request instanceof HttpRequestWrapper) {
                    request.removeNote(WRAPPED_REQUEST);
                }
                if (response instanceof HttpResponseWrapper) {
                    request.removeNote(WRAPPED_RESPONSE);
                }
            }
        }

        return result;
    }

    @Override
    public boolean hasRole(HttpRequest request, HttpResponse response, Principal principal, String role) {
        GlassFishAuthorizationService authorizationService = getAuthorizationService();
        if (authorizationService == null) {
            return false;
        }

        // Add HttpResponse and HttpResponse to the parameters, and remove
        // instance variable currentRequest from this class. References to
        // this.currentRequest are also removed from other methods.
        String servletName = getCanonicalName(request);

        boolean isGranted = authorizationService.hasRoleRefPermission(servletName, role, principal);

        LOG.log(FINE, "Checking if servlet {0} with principal {1} has role {2} isGranted: {3}",
            new Object[] {servletName, principal, role, isGranted});

        return isGranted;
    }

    @Override
    public void logout(HttpRequest httpRequest) {
        boolean securityExtensionEnabled = isSecurityExtensionEnabled(httpRequest.getRequest().getServletContext());
        byte[] alreadyCalled = reentrancyStatus.get();

        if (securityExtensionEnabled && authenticationService != null && alreadyCalled[0] == 0) {
            alreadyCalled[0] = 1;

            MessageInfo messageInfo = (MessageInfo) httpRequest.getRequest().getAttribute(MESSAGE_INFO);
            if (messageInfo == null) {
                messageInfo =
                    new HttpMessageInfo(
                        (HttpServletRequest) httpRequest.getRequest(),
                        (HttpServletResponse) httpRequest.getResponse().getResponse());
            }

            messageInfo.getMap().put(IS_MANDATORY, TRUE.toString());

            try {
                ServerAuthContext serverAuthContext = authenticationService.getServerAuthContext(messageInfo, null);
                if (serverAuthContext != null) {
                    /*
                     * Check for the default/server-generated/unauthenticated security context.
                     */
                    SecurityContext securityContext = SecurityContext.getCurrent();
                    Subject subject = securityContext.didServerGenerateCredentials() ? new Subject() : securityContext.getSubject();

                    if (subject == null) {
                        subject = new Subject();
                    }
                    if (subject.isReadOnly()) {
                        LOG.log(WARNING, "Read-only subject found during logout processing");
                    }

                    try {
                        httpRequest.getContext().fireContainerEvent(BEFORE_LOGOUT, null);
                        serverAuthContext.cleanSubject(messageInfo, subject);
                    } finally {
                        httpRequest.getContext().fireContainerEvent(AFTER_LOGOUT, null);
                    }
                }
            } catch (AuthException ex) {
                throw new RuntimeException(ex);
            } finally {
                doLogout(httpRequest, true);
                alreadyCalled[0] = 0;
            }
        } else {
            doLogout(httpRequest, alreadyCalled[0] == 1);
        }
    }

    @Override
    public void logout() {
        setSecurityContext(null);

        // Sets the security context for Jakarta Authorization
        WebSecurityManager webSecurityManager = getWebSecurityManager(false);
        if (webSecurityManager != null) {
            webSecurityManager.onLogout();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (authenticationService != null) {
            authenticationService.disable();
        }
    }

    /**
     * Attempts to restore old SecurityContext (but fails).
     *
     * <P>
     * In theory this method seems to attempt to check if a run-as principal was set by preSetRunAsIdentity() (based on the
     * indirect assumption that if the servlet in the given invocation has a run-as this must've been the case). If so, it
     * retrieves the oldSecurityContext from the invocation object and set it in the SecurityContext.
     *
     * <P>
     * The problem is that the invocation object is not the same object as was passed in to preSetRunAsIdentity() so it will
     * never contain the right info - see bug 4757733.
     *
     * <P>
     * In practice it means this method only ever sets the SecurityContext to null (if run-as matched) or does nothing. In
     * particular note the implication that it <i>will</i> be set to null after a run-as invocation completes. This behavior
     * will be retained for the time being for consistency with RI. It must be fixed later.
     *
     * @param inv The invocation object to process.
     *
     */
    public void postSetRunAsIdentity(ComponentInvocation inv) {
        // Optimization to avoid the expensivce call to getServletName
        // for cases with no run-as descriptors

        if (runAsPrincipals != null && runAsPrincipals.isEmpty()) {
            return;
        }

        String servletName = this.getServletName(inv);
        if (servletName == null) {
            return;
        }

        String runAs = runAsPrincipals.get(servletName);
        if (runAs != null) {
            setSecurityContext((SecurityContext) inv.getOldSecurityContext()); // always null

        }
    }

    @Override
    protected char[] getPassword(String username) {
        throw new IllegalStateException("Should not reach here");
    }

    @Override
    protected Principal getPrincipal(String username) {
        throw new IllegalStateException("Should not reach here");
    }

    public WebBundleDescriptor getWebDescriptor() {
        return webBundleDescriptor;
    }

    public boolean hasRole(String servletName, Principal principal, String role) {
        GlassFishAuthorizationService authorizationService = getAuthorizationService();
        if (authorizationService == null) {
            return false;
        }

        return authorizationService.hasRoleRefPermission(servletName, role, principal);
    }

    /**
     * This method is added to create a Principal based on the username only. Hercules stores the username as part of
     * authentication failover and needs to create a Principal based on username only.
     *
     * @param username
     * @return Principal for the user username HERCULES:add
     */
    public Principal createFailOveredPrincipal(String username) {
        LOG.log(FINEST, "createFailOveredPrincipal(username={0})", username);
        loginForRunAs(username);

        // set the appropriate security context
        SecurityContext securityContext = SecurityContext.getCurrent();
        LOG.log(FINE, "Security context is {0}", securityContext);

        Principal principal = new WebPrincipal(username, (char[]) null, securityContext);
        LOG.log(INFO, "Principal created for FailOvered user {0}", principal);

        return principal;
    }



    // ### Private methods

    private SecurityConstraint[] findSecurityConstraints(Context context) {
        ServletContext servletContext = context.getServletContext();

        if (authenticationService == null) {
            initAuthenticationService(servletContext);
        }

        GlassFishAuthorizationService authorizationService = getAuthorizationService(false);
        if (authorizationService == null) {
            return emptyConstraints;
        }

        if (authorizationService.hasNoConstrainedResources() && !isSecurityExtensionEnabled(servletContext)) {
            return null;
        }

        return emptyConstraints;
    }

    private boolean isRequestAuthenticated(HttpRequest httpRequest) {
        return ((HttpServletRequest) httpRequest).getUserPrincipal() != null;
    }

    private boolean isJakartaAuthenticationEnabled() throws IOException {
        try {
            return authenticationService != null && authenticationService.getServerAuthConfig() != null;

        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Invokes the GlassFish AuthorizationService (a thin wrapper over the Exousia AuthorizationService)
     * to perform the Jakarta Authorization access control check.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param constraints Security constraint we are enforcing
     * @return <code>true</code> if permission is granted, or <code>false</code> otherwise.
     *
     * @exception IOException if an input/output error occurs
     */
    private boolean invokeAuthorizationService(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints) throws IOException {
        if (formInfo.isRequestFormPage(request)) {
            // TODO: this is very specific for a single authentication mechanism. Architecturally, RealmAdapter
            // should not have knowledge of specific authentication mechanisms.
            return true;
        }

        setServletPath(request);
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        LOG.log(FINE, () -> "[Web-Security] [ hasResourcePermission ]" +
            " Principal: " + httpServletRequest.getUserPrincipal() +
            " ContextPath: " + httpServletRequest.getContextPath());

        GlassFishAuthorizationService authorizationService = getAuthorizationService();
        if (authorizationService == null) {
            return false;
        }

        return authorizationService.hasResourcePermission(httpServletRequest);
    }

    private boolean validate(HttpRequest request, HttpResponse response, LoginConfig config, Authenticator authenticator, boolean calledFromAuthenticate) throws IOException {
        /*
         * Create a request facade such that if the request was received at the root context, and the root context is mapped to
         * a default-web-module, the default-web-module mapping is masked from the application code to which the request facade
         * is being passed. For example, the request.facade's getContextPath() method will return "/", rather than the context
         * root of the default-web-module, in this case.
         */
        HttpServletRequest httpServletRequest = (HttpServletRequest) request.getRequest(true);
        HttpServletResponse httpServletResponse = (HttpServletResponse) response.getResponse();

        Subject subject = new Subject();

        MessageInfo messageInfo = new HttpMessageInfo(httpServletRequest, httpServletResponse);
        boolean isRequestValidated = false;
        boolean isMandatory = true;
        try {
            isMandatory = !getAuthorizationService().permitAll(httpServletRequest);

            // Issue - 9578 - produce user challenge if call originates from HttpRequest.authenticate
            if (isMandatory || calledFromAuthenticate) {
                messageInfo.getMap().put(IS_MANDATORY, TRUE.toString());
            }

            // TODO: call validateRequest

            ServerAuthContext serverAuthContext = authenticationService.getServerAuthContext(messageInfo, null); // null serviceSubject
            if (serverAuthContext == null) {
                throw new AuthException("null ServerAuthContext");
            }
            AuthStatus authStatus = serverAuthContext.validateRequest(messageInfo, subject, null); // null serviceSubject
            isRequestValidated = AuthStatus.SUCCESS.equals(authStatus);

            if (isRequestValidated) { // cache it only if validateRequest = true
                messageInfo.getMap().put(SERVER_AUTH_CONTEXT, serverAuthContext);
                httpServletRequest.setAttribute(MESSAGE_INFO, messageInfo);
            }
        } catch (AuthException ae) {
            LOG.log(SEVERE, "Jakarta Authentication: http msg authentication fail", ae);
            httpServletResponse.setStatus(SC_INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            LOG.log(SEVERE , "Jakarta Authentication: Exception during validateRequest", e);
            httpServletResponse.sendError(SC_INTERNAL_SERVER_ERROR);
        }

        if (isRequestValidated) {
            Caller caller = SubjectUtils.getCaller(subject);

            // Must have a caller to establish non-default security context
            if (caller != null) {

                // Convert Epicyro representation of the Caller Principal / Groups to the existing
                // GlassFish one. A future version of this code may use the Epicyro one everywhere directly.
                subject = new Subject();

                // See if there's a Subject stored in the session that contain all relevant principals and
                // credentials for reuse, and the caller has indicated to take these.
                Subject sessionSubject = reuseSessionSubject(caller);
                if (sessionSubject != null) {
                    // Copy principals, public credentials and private credentials from the Subject that lives in
                    // the session to the receiving Subject.
                    copySubject(subject, sessionSubject);
                } else {
                    Principal glassFishCallerPrincipal = getGlassFishCallerPrincipal(caller, realmName);

                    toSubject(subject, glassFishCallerPrincipal);
                    DistinguishedPrincipalCredential distinguishedPrincipal = new DistinguishedPrincipalCredential(glassFishCallerPrincipal);

                    // Credentials don't serialize, so for now, also add to the subject principals
                    // For next version, see if we can only use principals
                    toSubject(subject, distinguishedPrincipal);
                    toSubjectCredential(subject, distinguishedPrincipal);

                    for (String group : caller.getGroups()) {
                        toSubject(subject, new Group(group));
                    }

                    if (!glassFishCallerPrincipal.equals(SecurityContext.getDefaultCallerPrincipal())) {

                        // Give native GlassFish (realms, mostly) opportunity to add groups
                        LoginContextDriver.jmacLogin(subject, glassFishCallerPrincipal, realmName);

                        SecurityContext securityContext = new SecurityContext(subject);
                        SecurityContext.setCurrent(securityContext);

                        // XXX assuming no null principal here
                        Principal principal = securityContext.getCallerPrincipal();
                        WebPrincipal webPrincipal = new WebPrincipal(principal, securityContext);
                        try {
                            String authType = getAuthType(messageInfo);
                            if (authType == null && config != null && config.getAuthMethod() != null) {
                                authType = config.getAuthMethod();
                            }

                            if (shouldRegister(messageInfo)) {
                                // Sets webPrincipal for the session and request
                                new AuthenticatorProxy(authenticator, webPrincipal, authType)
                                        .authenticate(request, response, config);
                            } else {
                                // Sets webPrincipal for the request only
                                request.setAuthType(authType == null ? PROXY_AUTH_TYPE : authType);
                                request.setUserPrincipal(webPrincipal);
                            }
                        } catch (LifecycleException le) {
                            LOG.log(SEVERE, "Unable to register session", le);

                        }

                    } else {
                        // GLASSFISH-20930.Set null for the case when SAM does not
                        // indicate that it needs the session
                        if (getCallerPrincipal(messageInfo) != null) {
                            request.setUserPrincipal(null);
                            request.setAuthType(null);
                        }

                        if (isMandatory) {
                            isRequestValidated = false;
                        }
                    }
                }
            }

            if (isRequestValidated) {
                HttpServletRequest newRequest = (HttpServletRequest) messageInfo.getRequestMessage();
                if (newRequest != httpServletRequest) {
                    request.setNote(WRAPPED_REQUEST, new HttpRequestWrapper(request, newRequest));
                }

                HttpServletResponse newResponse = (HttpServletResponse) messageInfo.getResponseMessage();
                if (newResponse != httpServletResponse) {
                    request.setNote(WRAPPED_RESPONSE, new HttpResponseWrapper(response, newResponse));
                }
            }

        }

        return isRequestValidated;
    }

    private void doLogout(HttpRequest request, boolean extensionEnabled) {
        Context context = request.getContext();
        Authenticator authenticator = null;
        if (context != null) {
            authenticator = context.getAuthenticator();
        }
        if (authenticator == null) {
            throw new RuntimeException("Context or Authenticator is null");
        }
        try {
            if (extensionEnabled) {
                AuthenticatorProxy proxy = new AuthenticatorProxy(authenticator, null, null);
                proxy.logout(request);
            } else {
                authenticator.logout(request);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        logout();
    }

    private void setServletPath(HttpRequest request) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (httpServletRequest.getServletPath() == null) {
            request.setServletPath(getResourceName(httpServletRequest.getRequestURI(), httpServletRequest.getContextPath()));
        }
    }

    /**
     * Obtain servlet name from invocation.
     *
     * <P>
     * In order to obtain the servlet name one of the following must be true: 1. The instanceName of the ComponentInvocation
     * is not null 2. The ComponentInvocation contains a 'class' of type HttpServlet, which contains a valid ServletConfig
     * object. This method returns the value returned by getServletName() on the ServletConfig.
     *
     * <P>
     * If the above is not met, null is returned.
     *
     * @param componentInvocation The invocation object to process.
     * @return Servlet name or null.
     *
     */
    private String getServletName(ComponentInvocation componentInvocation) {
        String servletName = componentInvocation.getInstanceName();
        if (servletName != null) {
            return servletName;
        }

        Object invocationInstance = componentInvocation.getInstance();

        if (invocationInstance instanceof HttpServlet) {
            HttpServlet thisServlet = (HttpServlet) invocationInstance;
            ServletConfig servletConfig = thisServlet.getServletConfig();

            if (servletConfig != null) {
                return thisServlet.getServletName();
            }
        }

        return null;
    }

    // pass in HttpServletResponse instead of saving it as instance variable
    // private String getCanonicalName(){
    private String getCanonicalName(HttpRequest currentRequest) {
        return currentRequest.getWrapper().getServletName();
    }

    private String getResourceName(String uri, String contextPath) {
        if (contextPath.length() < uri.length()) {
            return uri.substring(contextPath.length());
        }
        return "";
    }

    private String findRealmName(String initialRealmName) {
        String candidateRealmName = webBundleDescriptor.getApplication().getRealm();
        LoginConfiguration loginConfig = webBundleDescriptor.getLoginConfiguration();

        if (candidateRealmName == null && loginConfig != null) {
            candidateRealmName = loginConfig.getRealmName();
        }

        if (initialRealmName != null && isEmpty(candidateRealmName)) {
            candidateRealmName = initialRealmName;
        }

        return candidateRealmName;
    }

    private void loginForRunAs(String principal) {
        LoginContextDriver.loginPrincipal(principal, realmName);
    }

    private SecurityContext getSecurityContext() {
        return SecurityContext.getCurrent();
    }

    private void setSecurityContext(SecurityContext sc) {
        SecurityContext.setCurrent(sc);
    }

    /**
     * This must be invoked after virtualServer is set.
     */
    private String getAppContextID(final ServletContext servletContext) {
        if (!servletContext.getVirtualServerName().equals(this.virtualServer.getName())) {
            LOG.log(WARNING, "Virtual server name from ServletContext: {0} differs from name from virtual.getName(): {1}",
                    new Object[] { servletContext.getVirtualServerName(), virtualServer.getName() });
        }
        if (!servletContext.getContextPath().equals(webBundleDescriptor.getContextRoot())) {
            LOG.log(WARNING, "Context path from ServletContext: {0} differs from path from bundle: {1}",
                    new Object[] { servletContext.getContextPath(), webBundleDescriptor.getContextRoot() });
        }

        return servletContext.getVirtualServerName() + " " + servletContext.getContextPath();
    }

    private String getAuthType(MessageInfo messageInfo) {
        return (String) messageInfo.getMap().get(HttpServletConstants.AUTH_TYPE);
    }

    private Principal getCallerPrincipal(MessageInfo messageInfo) {
        return ((HttpServletRequest) messageInfo.getRequestMessage()).getUserPrincipal();
    }

    private boolean shouldRegister(MessageInfo messageInfo) {
        return mapEntryToBoolean(REGISTER_SESSION, messageInfo.getMap());
    }

    private boolean mapEntryToBoolean(String propertyName, Map<String, Object> map) {
        if (!map.containsKey(propertyName)) {
            return false;
        }

        if (map.get(propertyName) instanceof String string) {
            return Boolean.parseBoolean(string);
        }

        return false;
    }

    // TODO: reexamine this after TP2
    private synchronized void initAuthenticationService(final ServletContext servletContext) {
        if (this.authenticationService != null) {
            return;
        }

        try {
            this.authenticationService = createAuthenticationService(servletContext);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * This must be invoked after virtualServer is set.
     * @throws IOException
     */
    private BaseAuthenticationService createAuthenticationService(final ServletContext servletContext) throws IOException {
        Map<String, Object> properties = new HashMap<>();

        String policyContextId = getContextID(webBundleDescriptor);
        if (policyContextId != null) {
           properties.put(POLICY_CONTEXT, policyContextId);
        }

        // "authModuleId" (HttpServletSecurityProvider) is a GlassFish proprietary mechanism where a
        // Jakarta Authentication module gets assigned an ID in the proprietary config of GlassFish (domain.xml).
        // This ID is then used in glassfish-web.xml to indicate that a war wants to use that authentication module.
        String authModuleId =
            AuthMessagePolicy.getProviderID(
                AuthMessagePolicy.getSunWebApp(Map.of(
                        WEB_BUNDLE, webBundleDescriptor)));

        if (authModuleId != null) {
            properties.put("authModuleId", authModuleId);
        }

        String appContextId = getAppContextID(servletContext);

        return new DefaultAuthenticationService(
            appContextId,
            properties,
            new ConfigDomainParser(),
            new ServerContainerCallbackHandler(realmName));
    }

    /**
     * Utility method to get web security manager.
     * Will log warning if the manager is not found in the factory, and logNull is true.
     * <p>
     * Note: webSecurityManagerFactory can be null the very questionable SOAP code just
     * instantiates a RealmAdapter
     *
     * @param logNull
     * @return {@link WebSecurityManager} or null
     */
    private WebSecurityManager getWebSecurityManager(boolean logNull) {
        if (webSecurityManager == null && webSecurityManagerFactory != null) {
            synchronized (this) {
                webSecurityManager = webSecurityManagerFactory.getManager(contextId);
            }

            if (webSecurityManager == null && logNull) {
                LOG.log(WARNING, MSG_NO_WEB_SECURITY_MGR, contextId);
            }
        }

        return webSecurityManager;
    }

    private GlassFishAuthorizationService getAuthorizationService() {
        return getAuthorizationService(true);
    }

    private GlassFishAuthorizationService getAuthorizationService(boolean lognull) {
        WebSecurityManager webSecurityManager = getWebSecurityManager(lognull);
        if (webSecurityManager == null) {
            return null;
        }

        return webSecurityManager.getAuthorizationService();
    }

    private void collectRunAsPrincipals() {
        runAsPrincipals = new HashMap<>();

        for (WebComponentDescriptor componentDescriptor : webBundleDescriptor.getWebComponentDescriptors()) {
            RunAsIdentityDescriptor runAsDescriptor = componentDescriptor.getRunAsIdentity();

            if (runAsDescriptor != null) {
                String principal = runAsDescriptor.getPrincipal();
                String servlet = componentDescriptor.getCanonicalName();

                if (isAnyNull(principal, servlet)) {
                    LOG.warning("WEB8080: Null run-as principal or servlet, ignoring run-as element.");
                } else {
                    runAsPrincipals.put(servlet, principal);
                    LOG.log(FINE, "Servlet {0} will run-as: {1}", new Object[] {servlet, principal});
                }
            }
        }
    }

}
