/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.web.integration;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.runtime.common.PrincipalNameDescriptor;
import com.sun.enterprise.deployment.runtime.common.SecurityRoleMapping;
import com.sun.enterprise.deployment.runtime.common.wls.SecurityRoleAssignment;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.ee.SecurityUtil;
import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
import com.sun.enterprise.security.ee.authorize.PolicyContextHandlerImpl;
import com.sun.enterprise.security.ee.authorize.cache.CachedPermission;
import com.sun.enterprise.security.ee.authorize.cache.CachedPermissionImpl;
import com.sun.enterprise.security.ee.authorize.cache.PermissionCache;
import com.sun.enterprise.security.ee.authorize.cache.PermissionCacheFactory;

import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;
import jakarta.security.jacc.WebResourcePermission;
import jakarta.security.jacc.WebUserDataPermission;
import jakarta.servlet.http.HttpServletRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.exousia.AuthorizationService;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.UserNameAndPassword;

import static com.sun.enterprise.security.ee.authorize.PolicyContextHandlerImpl.HTTP_SERVLET_REQUEST;
import static com.sun.enterprise.security.ee.authorize.cache.PermissionCacheFactory.createPermissionCache;
import static com.sun.enterprise.security.ee.web.integration.GlassFishToExousiaConverter.getConstraintsFromBundle;
import static com.sun.enterprise.security.ee.web.integration.GlassFishToExousiaConverter.getSecurityRoleRefsFromBundle;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.stream.Collectors.toSet;
import static org.glassfish.api.web.Constants.ADMIN_VS;

/**
 * The class implements the JSR 115 - JavaTM Authorization Contract for Containers. This class is a companion class of
 * EJBSecurityManager.
 *
 * All the security decisions required to allow access to a resource are defined in that class.
 *
 * @author Jean-Francois Arcand
 * @author Harpreet Singh.
 */
public class WebSecurityManager {
    private static final Logger logger = LogUtils.getLogger();

    /**
     * Request path. Copied from org.apache.catalina.Globals; Required to break dependence on WebTier of Security Module
     */
    public static final String CONSTRAINT_URI = "org.apache.catalina.CONSTRAINT_URI";

    private static final String RESOURCE = "hasResourcePermission";
    private static final String USERDATA = "hasUserDataPermission";
    private static final String EMPTY_STRING = "";

    // The context ID associated with this instance. This is the name
    // of the application
    private final String contextId;
    private String codebase;

    protected CodeSource codesource;

    private static final WebResourcePermission allResources = new WebResourcePermission("/*", (String) null);

    private static final WebUserDataPermission allConnections = new WebUserDataPermission("/*", null);

    private static Permission[] protoPerms = { allResources, allConnections };

    // permissions tied to unchecked permission cache, and used
    // to determine if the effective policy is grant all
    // WebUserData and WebResource permisions.
    private CachedPermission allResourcesCachedPermission;

    private CachedPermission allConnectionsCachedPermission;

    // unchecked permission cache
    private PermissionCache uncheckedPermissionCache;

    private static Set<Principal> defaultPrincipalSet = SecurityContext.getDefaultSecurityContext().getPrincipalSet();

    private final WebSecurityManagerFactory webSecurityManagerFactory;
    private final ServerContext serverContext;

    // WebBundledescriptor
    private final WebBundleDescriptor webBundleDescriptor;

    private boolean register = true;

    AuthorizationService authorizationService;

    WebSecurityManager(WebBundleDescriptor webBundleDescriptor, ServerContext serverContext, WebSecurityManagerFactory webSecurityManagerFactory, boolean register) throws PolicyContextException {
        this.register = register;
        this.webBundleDescriptor = webBundleDescriptor;
        this.contextId = getContextID(webBundleDescriptor);
        this.serverContext = serverContext;
        this.webSecurityManagerFactory = webSecurityManagerFactory;

        String appName = webBundleDescriptor.getApplication().getRegistrationName();
        SecurityRoleMapperFactoryGen.getSecurityRoleMapperFactory().setAppNameForContext(appName, contextId);

        initialise(appName);

        authorizationService = new AuthorizationService(
            getContextID(webBundleDescriptor),
            () -> SecurityContext.getCurrent().getSubject(),
            null);

        authorizationService.setConstrainedUriRequestAttribute(CONSTRAINT_URI);
        authorizationService.setRequestSupplier(
            () -> (HttpServletRequest) webSecurityManagerFactory.pcHandlerImpl.getHandlerData().get(HTTP_SERVLET_REQUEST));

        authorizationService.addConstraintsToPolicy(
            getConstraintsFromBundle(webBundleDescriptor),
            webBundleDescriptor.getRoles()
               .stream()
               .map(e -> e.getName())
               .collect(toSet()),
            webBundleDescriptor.isDenyUncoveredHttpMethods(),
            getSecurityRoleRefsFromBundle(webBundleDescriptor));
    }

    // fix for CR 6155144
    // used to get the policy context id. Also used by the RealmAdapter
    public static String getContextID(WebBundleDescriptor webBundleDescriptor) {
        return SecurityUtil.getContextID(webBundleDescriptor);
    }

    /**
     * Returns true to indicate that a policy check was made and there were no constrained resources.
     *
     * when caching is disabled must always return false, which will ensure that policy is consulted to authorize each
     * request.
     */
    public boolean hasNoConstrainedResources() {
        boolean result = false;

        if (allResourcesCachedPermission != null && allConnectionsCachedPermission != null) {
            boolean x = allResourcesCachedPermission.checkPermission();
            boolean y = allConnectionsCachedPermission.checkPermission();
            result = x && y;
            if (result) {
                try {
                    AuthorizationService.setThreadContextId(contextId);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        }

        return result;
    }

    public boolean permitAll(HttpServletRequest httpServletRequest) {
        setSecurityInfo(httpServletRequest);
        return authorizationService.checkWebResourcePermission(httpServletRequest, null);
    }

    /**
     * if uri == null, determine if the connection characteristics of the request satisfy the applicable policy. If the uri is not
     * null, determine if the uri and Http method require a CONFIDENTIAL transport. The uri value does not include the context path,
     * and any colons occurring in the uri must be escaped.
     *
     * @return 1 if access is permitted (as is or without SSL). -1 if the the access will be permitted after a redirect to SSL.
     * return 0 if access will be denied independent of whether a redirect to SSL is done.
     *
     * Note: this method is not intended to be called if the request is secure. it checks whether the resource can be accessed over
     * the current connection type (which is presumed to be insecure), and if an insecure connection type is not permitted it checks
     * if the resource can be accessed via a confidential transport.
     *
     * If the request is secure, the second check is skipped, and the proper result is returned (but that is not the intended use
     * model).
     */
    public int hasUserDataPermission(HttpServletRequest httpServletRequest, String uri, String httpMethod) {
        setSecurityInfo(httpServletRequest);

        boolean isGranted = false;
        if (uri == null) {
            isGranted = authorizationService.checkWebUserDataPermission(httpServletRequest);
        } else {
            isGranted = authorizationService.checkWebUserDataPermission(uri, httpMethod, httpServletRequest.isSecure());
        }

        int result = 0;

        if (isGranted) {
            result = 1;
        }

        if (logger.isLoggable(FINE)) {
            logger.log(FINE, "[Web-Security] hasUserDataPermission isGranted: {0}", isGranted);
        }

        recordWebInvocation(httpServletRequest, USERDATA, isGranted);

        // Try to see if the caller would have access to the secure variant of this request.
        // If so, we can signal that a redirect is likely going to succeed.
        if (!isGranted && !httpServletRequest.isSecure()) {

            if (uri == null) {
                uri = getUriMinusContextPath(httpServletRequest);
                httpMethod = httpServletRequest.getMethod();
            }

            isGranted = authorizationService.checkWebUserDataPermission(uri, httpMethod, true, defaultPrincipalSet);

            if (isGranted) {
                result = -1;
            }
        }

        return result;
    }

    /**
     * Perform access control based on the <code>HttpServletRequest</code>. Return <code>true</code> if this constraint is satisfied
     * and processing should continue, or <code>false</code> otherwise.
     *
     * @return true is the resource is granted, false if denied
     */
    public boolean hasResourcePermission(HttpServletRequest httpServletRequest) {
        setSecurityInfo(httpServletRequest);
        SecurityContext.setCurrent(getSecurityContext(httpServletRequest.getUserPrincipal()));

        boolean isGranted = authorizationService.checkWebResourcePermission(httpServletRequest);

        if (logger.isLoggable(FINE)) {
            logger.log(FINE, "[Web-Security] hasResource isGranted: {0}", isGranted);
            logger.log(FINE, "[Web-Security] hasResource perm: {0}", getUriMinusContextPath(httpServletRequest));
        }

        recordWebInvocation(httpServletRequest, RESOURCE, isGranted);

        return isGranted;
    }

    /*
     * Return <code>true</code> if the specified servletName has the specified security role, within the context of the
     * WebRoleRefPermission; otherwise return <code>false</code>.
     *
     * @param principal servletName the resource's name.
     *
     * @param principal Principal for whom the role is to be checked
     *
     * @param role Security role to be checked
     *
     * @return true is the resource is granted, false if denied
     */
    public boolean hasRoleRefPermission(String servletName, String role, Principal callerPrincipal) {
        boolean isGranted = authorizationService.checkWebRoleRefPermission(
            servletName,
            role,
            getSecurityContext(callerPrincipal).getPrincipalSet());

        if (logger.isLoggable(FINE)) {
            logger.log(FINE, "[Web-Security] hasRoleRef perm: {0}", servletName + " " + role);
            logger.log(FINE, "[Web-Security] hasRoleRef isGranted: {0}", isGranted);
        }

        return isGranted;
    }

    public void onLogin(HttpServletRequest httpServletRequest) {
        setSecurityInfo(httpServletRequest);
    }

    public void onLogout() {
        resetSecurityInfo();
    }

    public boolean linkPolicy(String linkedContextId, boolean lastInService) {
        return authorizationService.linkPolicy(linkedContextId, lastInService);
    }

    public static boolean linkPolicy(String contextId, String linkedContextId, boolean lastInService) {
        return AuthorizationService.linkPolicy(contextId, linkedContextId, lastInService);
    }

    public void commitPolicy() {
        authorizationService.commitPolicy();
    }

    public static void commitPolicy(String contextId) {
        AuthorizationService.commitPolicy(contextId);
    }

    public void refresh() {
        authorizationService.refresh();
    }

    public void deletePolicy() {
        authorizationService.deletePolicy();
    }

    public static void deletePolicy(String contextId) {
        AuthorizationService.deletePolicy(contextId);
    }

    /**
     * Analogous to destroy, except does not remove links from Policy Context, and does not remove context_id from role mapper
     * factory. Used to support Policy Changes that occur via ServletContextListener.
     *
     * @throws PolicyContextException
     */
    public void release() throws PolicyContextException {
        authorizationService.removeStatementsFromPolicy(null);

        PermissionCacheFactory.removePermissionCache(uncheckedPermissionCache);
        uncheckedPermissionCache = null;
        webSecurityManagerFactory.getManager(contextId, true);
    }

    public void destroy() throws PolicyContextException {
        authorizationService.refresh();

        PermissionCacheFactory.removePermissionCache(uncheckedPermissionCache);
        uncheckedPermissionCache = null;
        SecurityRoleMapperFactoryGen.getSecurityRoleMapperFactory().removeAppNameForContext(contextId);
        webSecurityManagerFactory.getManager(contextId, true);
    }



    // ### Private methods ###


    private void initialise(String appName) throws PolicyContextException {
        codebase = removeSpaces(contextId);

        if (ADMIN_VS.equals(getVirtualServers(appName))) {
            handleAdminVirtualServer();
        }

        // Will require stuff in hash format for reference later on.
        codesource = createCodeSource();

        if (logger.isLoggable(FINE)) {
            logger.log(FINE, "[Web-Security] Context id (id under which  WEB component in application will be created) = {0}", contextId);
            logger.log(FINE, "[Web-Security] Codebase (module id for web component) {0}", codebase);
        }

        initPermissionCache();
    }

    private CodeSource createCodeSource() {
        try {
            try {
                if (logger.isLoggable(FINE)) {
                    logger.log(FINE, "[Web-Security] Creating a Codebase URI with = {0}", codebase);
                }
                return codesource = new CodeSource(new URL(new URI("file:///" + codebase).toString()), (Certificate[]) null);
            } catch (URISyntaxException use) {
                // manually create the URL
                logger.log(FINE, "[Web-Security] Error Creating URI ", use);
                throw new RuntimeException(use);
            }

        } catch (MalformedURLException mue) {
            logger.log(SEVERE, LogUtils.EJBSM_CODSOURCEERROR, mue);
            throw new RuntimeException(mue);
        }
    }

    private void initPermissionCache() {
        if (uncheckedPermissionCache == null) {
            if (register) {
                uncheckedPermissionCache = createPermissionCache(contextId, codesource, protoPerms, null);
                allResourcesCachedPermission = new CachedPermissionImpl(uncheckedPermissionCache, allResources);
                allConnectionsCachedPermission = new CachedPermissionImpl(uncheckedPermissionCache, allConnections);
            }
        } else {
            uncheckedPermissionCache.reset();
        }
    }

    private void handleAdminVirtualServer() {
        LoginConfiguration loginConfiguration = webBundleDescriptor.getLoginConfiguration();
        if (loginConfiguration == null) {
            return;
        }

        String realmName = loginConfiguration.getRealmName();
        SunWebApp sunDescriptor = webBundleDescriptor.getSunDescriptor();
        if (sunDescriptor == null) {
            return;
        }
        SecurityRoleMapping[] sunRoleMappings = sunDescriptor.getSecurityRoleMapping();
        if (sunRoleMappings != null) {
            for (SecurityRoleMapping roleMapping : sunRoleMappings) {
                for (PrincipalNameDescriptor principal : roleMapping.getPrincipalNames()) {
                    // we keep just a name here
                    webSecurityManagerFactory.putAdminPrincipal(realmName,
                        new UserNameAndPassword(principal.getName()));
                }
                for (String group : roleMapping.getGroupNames()) {
                    webSecurityManagerFactory.putAdminGroup(group, realmName, new Group(group));
                }
            }
        }

        SecurityRoleAssignment[] sunRoleAssignments = sunDescriptor.getSecurityRoleAssignments();
        if (sunRoleAssignments != null) {
            for (SecurityRoleAssignment roleAssignment : sunRoleAssignments) {
                List<String> principals = roleAssignment.getPrincipalNames();
                if (roleAssignment.isExternallyDefined()) {
                    webSecurityManagerFactory.putAdminGroup(roleAssignment.getRoleName(), realmName,
                        new Group(roleAssignment.getRoleName()));
                    continue;
                }
                for (String principal : principals) {
                    webSecurityManagerFactory.putAdminPrincipal(realmName, new UserNameAndPassword(principal));
                }
            }
        }
    }


    private void recordWebInvocation(final HttpServletRequest httpsr, final String type, final boolean isGranted) {
        AuditManager auditManager = SecurityServicesUtil.getInstance().getAuditManager();
        if (auditManager != null && auditManager.isAuditOn() && auditManager instanceof AppServerAuditManager) {
            final AppServerAuditManager appServerAuditManager = (AppServerAuditManager) auditManager;
            Principal prin = httpsr.getUserPrincipal();
            String user = prin != null ? prin.getName() : null;
            appServerAuditManager.webInvocation(user, httpsr, type, isGranted);
        }
    }

    /**
     * This is an private method for transforming principal into a SecurityContext
     *
     * @param principal expected to be a WebPrincipal
     * @return SecurityContext
     */
    private SecurityContext getSecurityContext(Principal principal) {
        SecurityContext securityContext = null;

        if (principal != null) {
            if (principal instanceof WebPrincipal) {
                WebPrincipal webPrincipal = (WebPrincipal) principal;
                securityContext = webPrincipal.getSecurityContext();
            } else {
                securityContext = new SecurityContext(principal.getName(), null);
            }
        }

        if (securityContext == null) {
            securityContext = SecurityContext.getDefaultSecurityContext();
        }

        return securityContext;
    }

    /**
     * This is a private method for policy context handler data info
     *
     * @param httpRequest
     */
    private void setSecurityInfo(HttpServletRequest httpRequest) {
        if (httpRequest != null) {
            webSecurityManagerFactory.pcHandlerImpl.getHandlerData().setHttpServletRequest(httpRequest);
        }
        AuthorizationService.setThreadContextId(contextId);
    }

    private void resetSecurityInfo() {
        PolicyContextHandlerImpl.getInstance().reset();
        PolicyContext.setContextID(null);
    }

    /**
     * Virtual servers are maintained in the reference contained in Server element. First, we need to find the server and then get
     * the virtual server from the correct reference
     *
     * @param applicationName Name of the app to get vs
     *
     * @return virtual servers as a string (separated by space or comma)
     */
    private String getVirtualServers(String applicationName) {
        Server server = serverContext.getDefaultServices().getService(Server.class);
        for (ApplicationRef appplicationRef : server.getApplicationRef()) {
            if (appplicationRef.getRef().equals(applicationName)) {
                return appplicationRef.getVirtualServers();
            }
        }

        return null;
    }

    private static String removeSpaces(String withSpaces) {
        return withSpaces.replace(' ', '_');
    }

    private static String getUriMinusContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri == null) {
            return EMPTY_STRING;
        }

        String contextPath = request.getContextPath();
        int contextLength = contextPath == null ? 0 : contextPath.length();

        if (contextLength > 0) {
            uri = uri.substring(contextLength);
        }

        if (uri.equals("/")) {
            return EMPTY_STRING;
        }

        // Encode all colons
        return uri.replaceAll(":", "%3A");
    }

}
