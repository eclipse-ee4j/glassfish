/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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
package com.sun.enterprise.security.ee.authorization;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
import com.sun.enterprise.security.ee.authorization.cache.CachedPermission;
import com.sun.enterprise.security.ee.authorization.cache.CachedPermissionImpl;
import com.sun.enterprise.security.ee.authorization.cache.PermissionCache;
import com.sun.enterprise.security.ee.authorization.cache.PermissionCacheFactory;
import com.sun.enterprise.security.ee.web.integration.LogUtils;
import com.sun.enterprise.security.ee.web.integration.SecurityRoleMapperFactoryGen;
import com.sun.enterprise.security.ee.web.integration.WebPrincipal;

import jakarta.security.jacc.PolicyConfigurationFactory;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;
import jakarta.security.jacc.PolicyFactory;
import jakarta.security.jacc.WebResourcePermission;
import jakarta.security.jacc.WebUserDataPermission;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Permission;
import java.security.Principal;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.exousia.AuthorizationService;

import static com.sun.enterprise.security.ee.authorization.GlassFishToExousiaConverter.getConstraintsFromBundle;
import static com.sun.enterprise.security.ee.authorization.GlassFishToExousiaConverter.getSecurityRoleRefsFromBundle;
import static com.sun.enterprise.security.ee.authorization.cache.PermissionCacheFactory.createPermissionCache;
import static java.util.logging.Level.FINE;
import static java.util.stream.Collectors.toSet;

/**
 * The class provides support for Jakarta Authorization. This class is a companion class of
 * EJBSecurityManager.
 *
 * All the security decisions required to allow access to a resource are defined in that class.
 *
 */
public class GlassFishAuthorizationService {

    private static final Logger logger = LogUtils.getLogger();

    /**
     * Request path. Copied from org.apache.catalina.Globals; Required to break dependence on WebTier of Security Module
     */
    public static final String CONSTRAINT_URI = "org.apache.catalina.CONSTRAINT_URI";

    private static final String RESOURCE = "hasResourcePermission";
    private static final String USERDATA = "hasUserDataPermission";
    private static final String EMPTY_STRING = "";

    // The context ID associated with this instance. This is the name of the application
    private final String contextId;

    private static final WebResourcePermission allResources = new WebResourcePermission("/*", (String) null);
    private static final WebUserDataPermission allConnections = new WebUserDataPermission("/*", null);
    private static Permission[] protoPerms = { allResources, allConnections };

    // permissions tied to unchecked permission cache, and used
    // to determine if the effective policy is grant all
    // WebUserData and WebResource permisions.
    private CachedPermission allResourcesCachedPermission;

    private CachedPermission allConnectionsCachedPermission;

    // Unchecked permission cache
    private PermissionCache uncheckedPermissionCache;

    private static Set<Principal> defaultPrincipalSet = SecurityContext.getDefaultSecurityContext().getPrincipalSet();

    private final boolean register;

    private final ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<>();
    private final AuthorizationService exousiaAuthorizationService;

    public GlassFishAuthorizationService(WebBundleDescriptor webBundleDescriptor, boolean register) throws PolicyContextException {
        this.register = register;
        this.contextId = AuthorizationUtil.getContextID(webBundleDescriptor);

        String appName = webBundleDescriptor.getApplication().getRegistrationName();
        SecurityRoleMapperFactoryGen.getSecurityRoleMapperFactory().setAppNameForContext(appName, contextId);

        initPermissionCache();

        webBundleDescriptor.getContextParameters()
                           .stream()
                           .filter(param -> param.getName().equals(PolicyConfigurationFactory.FACTORY_NAME))
                           .findAny()
                           .map(param -> loadFactory(webBundleDescriptor, param.getValue()))
                           .ifPresent(clazz -> installPolicyConfigurationFactory(webBundleDescriptor, clazz));

        webBundleDescriptor.getContextParameters()
                           .stream()
                           .filter(param -> param.getName().equals(PolicyFactory.FACTORY_NAME))
                           .findAny()
                           .map(param -> loadFactory(webBundleDescriptor, param.getValue()))
                           .ifPresent(clazz -> installPolicyFactory(webBundleDescriptor, clazz));

        exousiaAuthorizationService = new AuthorizationService(
            contextId,
            () -> SecurityContext.getCurrent().getSubject(),
            () -> new GlassFishPrincipalMapper(contextId));

        exousiaAuthorizationService.setConstrainedUriRequestAttribute(CONSTRAINT_URI);
        exousiaAuthorizationService.setRequestSupplier(contextId,
            () -> currentRequest.get());

        exousiaAuthorizationService.addConstraintsToPolicy(
            getConstraintsFromBundle(webBundleDescriptor),
            webBundleDescriptor.getRoles()
               .stream()
               .map(e -> e.getName())
               .collect(toSet()),
            webBundleDescriptor.isDenyUncoveredHttpMethods(),
            getSecurityRoleRefsFromBundle(webBundleDescriptor));
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
        return exousiaAuthorizationService.checkWebResourcePermission(httpServletRequest, (Subject) null);
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
            isGranted = exousiaAuthorizationService.checkWebUserDataPermission(httpServletRequest);
        } else {
            isGranted = exousiaAuthorizationService.checkWebUserDataPermission(uri, httpMethod, httpServletRequest.isSecure());
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

            isGranted = exousiaAuthorizationService.checkWebUserDataPermission(uri, httpMethod, true, defaultPrincipalSet);

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

        boolean isGranted = exousiaAuthorizationService.checkWebResourcePermission(httpServletRequest);

        if (logger.isLoggable(FINE)) {
            logger.log(FINE, "[Web-Security] hasResource isGranted: {0}", isGranted);
            logger.log(FINE, "[Web-Security] hasResource perm: {0}", getUriMinusContextPath(httpServletRequest));
        }

        recordWebInvocation(httpServletRequest, RESOURCE, isGranted);

        return isGranted;
    }

    /**
     * Return <code>true</code> if the specified servletName has the specified security role, within the context of the
     * WebRoleRefPermission; otherwise return <code>false</code>.
     *
     * @param principal servletName the resource's name.
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     *
     * @return true is the resource is granted, false if denied
     */
    public boolean hasRoleRefPermission(String servletName, String role, Principal callerPrincipal) {
        boolean isGranted = exousiaAuthorizationService.checkWebRoleRefPermission(
            servletName,
            role,
            getSecurityContext(callerPrincipal).getSubject());

        if (logger.isLoggable(FINE)) {
            logger.log(FINE, "[Web-Security] hasRoleRef perm: {0}", servletName + " " + role);
            logger.log(FINE, "[Web-Security] hasRoleRef isGranted: {0}", isGranted);
        }

        return isGranted;
    }

    public boolean linkPolicy(String linkedContextId, boolean lastInService) {
        return exousiaAuthorizationService.linkPolicy(linkedContextId, lastInService);
    }

    public static boolean linkPolicy(String contextId, String linkedContextId, boolean lastInService) {
        return AuthorizationService.linkPolicy(contextId, linkedContextId, lastInService);
    }

    public void commitPolicy() {
        exousiaAuthorizationService.commitPolicy();
    }

    public static void commitPolicy(String contextId) {
        AuthorizationService.commitPolicy(contextId);
    }

    public void refresh() {
        exousiaAuthorizationService.refresh();
    }

    public void deletePolicy() {
        exousiaAuthorizationService.deletePolicy();
    }

    public static void deletePolicy(String contextId) {
        AuthorizationService.deletePolicy(contextId);
    }

    public void setSecurityInfo(HttpServletRequest httpRequest) {
        if (httpRequest != null) {
            currentRequest.set(httpRequest);
        }

        AuthorizationService.setThreadContextId(contextId);
    }

    public void resetSecurityInfo() {
        currentRequest.remove();
        PolicyContext.setContextID(null);
    }

    public void release() throws PolicyContextException {
        exousiaAuthorizationService.removeStatementsFromPolicy(null);

        // Remove the handlers for policy contexts
        exousiaAuthorizationService.destroy();

        PermissionCacheFactory.removePermissionCache(uncheckedPermissionCache);
        uncheckedPermissionCache = null;
    }

    public void destroy() throws PolicyContextException {
        exousiaAuthorizationService.refresh();
        exousiaAuthorizationService.destroy();

        PermissionCacheFactory.removePermissionCache(uncheckedPermissionCache);
        uncheckedPermissionCache = null;
        SecurityRoleMapperFactoryGen.getSecurityRoleMapperFactory().removeAppNameForContext(contextId);
    }


    // ### Private methods

    private Class<?> loadFactory(WebBundleDescriptor webBundleDescriptor, String factoryClassName) {
        try {
            return
                webBundleDescriptor.getApplicationClassLoader()
                                   .loadClass(factoryClassName);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void installPolicyConfigurationFactory(WebBundleDescriptor webBundleDescriptor, Class<?> factoryClass) {
        ClassLoader existing = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(webBundleDescriptor.getApplicationClassLoader());
            AuthorizationService.installPolicyConfigurationFactory(factoryClass);
        } finally {
            Thread.currentThread().setContextClassLoader(existing);
        }
    }

    private void installPolicyFactory(WebBundleDescriptor webBundleDescriptor, Class<?> factoryClass) {
        ClassLoader existing = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(webBundleDescriptor.getApplicationClassLoader());
            AuthorizationService.installPolicyFactory(factoryClass);
        } finally {
            Thread.currentThread().setContextClassLoader(existing);
        }
    }

    private void initPermissionCache() {
        if (uncheckedPermissionCache == null) {
            if (register) {
                uncheckedPermissionCache = createPermissionCache(contextId, protoPerms, null);
                allResourcesCachedPermission = new CachedPermissionImpl(uncheckedPermissionCache, allResources);
                allConnectionsCachedPermission = new CachedPermissionImpl(uncheckedPermissionCache, allConnections);
            }
        } else {
            uncheckedPermissionCache.reset();
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

    private void recordWebInvocation(final HttpServletRequest httpServletRequest, final String type, final boolean isGranted) {
        AuditManager auditManager = SecurityServicesUtil.getInstance().getAuditManager();

        if (auditManager != null && auditManager.isAuditOn() && auditManager instanceof AppServerAuditManager appServerAuditManager) {
            Principal callerPrincipal = httpServletRequest.getUserPrincipal();
            String caller = callerPrincipal != null ? callerPrincipal.getName() : null;
            appServerAuditManager.webInvocation(caller, httpServletRequest, type, isGranted);
        }
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
