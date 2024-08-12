/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.ejb.security.application;

import com.sun.ejb.EjbInvocation;
import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityManager;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
import com.sun.enterprise.security.ee.authorization.AuthorizationUtil;
import com.sun.enterprise.security.ee.authorization.cache.PermissionCache;
import com.sun.enterprise.security.ee.authorization.cache.PermissionCacheFactory;
import com.sun.logging.LogDomains;

import jakarta.security.jacc.EJBMethodPermission;
import jakarta.security.jacc.PolicyContext;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.deployment.common.SecurityRoleMapperFactory;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.security.factory.EJBSecurityManagerFactory;
import org.glassfish.exousia.AuthorizationService;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.security.common.Role;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toSet;
import static org.glassfish.ejb.security.application.GlassFishToExousiaConverter.convertEJBMethodPermissions;
import static org.glassfish.ejb.security.application.GlassFishToExousiaConverter.getSecurityRoleRefsFromBundle;
import static org.glassfish.exousia.permissions.RolesToPermissionsTransformer.createEnterpriseBeansRoleRefPermission;

/**
 * This class is used by the Enterprise Beans server to manage security. All the container object only call into this object for managing
 * security. This class cannot be subclassed.
 *
 * <p/>
 * An instance of this class should be created per deployment unit.
 *
 * @author Harpreet Singh, monzillo
 */
public final class EJBSecurityManager implements SecurityManager {

    private static final Logger _logger = LogDomains.getLogger(EJBSecurityManager.class, LogDomains.EJB_LOGGER);

    // We use two protection domain caches until we decide how to
    // set the applicationCodeSource in the protection domain of system apps.
    //
    // Protection domains in managerProtectionDomainCache have the (privileged) managerCodeSource
    // of the EJBSecurityManager class. The protection domain used in pre-dispatch
    // authorization decisions MUST not be constructed using a privileged
    // applicationCodeSource (or else all pre-distpatch access decisions will be granted).

    private PermissionCache uncheckedMethodPermissionCache;

    private final EjbSecurityProbeProvider probeProvider = new EjbSecurityProbeProvider();

    private final EjbDescriptor deploymentDescriptor;
    private final InvocationManager invocationManager;
    private final EJBSecurityManagerFactory ejbSecurityManagerFactory;
    private final SecurityRoleMapperFactory roleMapperFactory;
    private final RunAsIdentityDescriptor runAs;
    private final AuthorizationService authorizationService;

    private static volatile EjbSecurityStatsProvider ejbStatsProvider;

    /**
     * ContextId id is the same as an application name.
     * This will be used to get a PolicyConfiguration object per application.
     */
    private final String contextId;
    private String codebase;
    private final String ejbName;
    private final String realmName;
    private final AppServerAuditManager auditManager;


    public EJBSecurityManager(EjbDescriptor ejbDescriptor, InvocationManager invMgr, EJBSecurityManagerFactory ejbSecurityManagerFactory) throws Exception {
        this.deploymentDescriptor = ejbDescriptor;
        this.invocationManager = invMgr;
        this.ejbSecurityManagerFactory = ejbSecurityManagerFactory;
        roleMapperFactory = AuthorizationUtil.getRoleMapperFactory();

        runAs = getRunAs(deploymentDescriptor);

        setEnterpriseBeansStatsProvider();

        contextId = getContextID(deploymentDescriptor);
        roleMapperFactory.setAppNameForContext(deploymentDescriptor.getApplication().getRegistrationName(), contextId);
        ejbName = deploymentDescriptor.getName();
        realmName = getRealmName(deploymentDescriptor);

        _logger.log(Level.FINE, () -> "Jakarta Authorization: Context id (id under which all EJB's in application will be created) = " + contextId);
        _logger.log(Level.FINE, () -> "Codebase (module id for ejb " + ejbName + ") = " + codebase);

        // Create and initialize the unchecked permission cache.
        uncheckedMethodPermissionCache = PermissionCacheFactory.createPermissionCache(
            contextId,
            EJBMethodPermission.class,
            ejbName);

        auditManager = ejbSecurityManagerFactory.getAuditManager();

        authorizationService = new AuthorizationService(
            getContextID(ejbDescriptor),
            () -> SecurityContext.getCurrent().getSubject(),
            () -> new GlassFishPrincipalMapper(contextId));

        authorizationService.addPermissionsToPolicy(
            convertEJBMethodPermissions(ejbDescriptor, contextId));

        authorizationService.addPermissionsToPolicy(createEnterpriseBeansRoleRefPermission(
            ejbDescriptor.getEjbBundleDescriptor()
                         .getRoles()
                         .stream()
                         .map(Role::getName)
                         .collect(toSet()),

            getSecurityRoleRefsFromBundle(ejbDescriptor)));

    }

    public static String getContextID(EjbDescriptor ejbDescriptor) {
        return AuthorizationUtil.getContextID(ejbDescriptor.getEjbBundleDescriptor());
    }

    /**
     * This method is called by the EJB container to decide whether or not a method specified in the Invocation should be allowed.
     *
     * @param componentInvocation invocation object that contains all the details of the invocation.
     * @return A boolean value indicating if the client should be allowed to invoke the EJB.
     */
    @Override
    public boolean authorize(ComponentInvocation componentInvocation) {
        if (!(componentInvocation instanceof EjbInvocation)) {
            return false;
        }

        EjbInvocation ejbInvocation = (EjbInvocation) componentInvocation; //FIXME: Param type should be EjbInvocation
        if (ejbInvocation.getAuth() != null) {
            return ejbInvocation.getAuth().booleanValue();
        }

        SecurityContext securityContext = SecurityContext.getCurrent();

        boolean authorized = false;
        try {
            authorized = authorizationService.checkBeanMethodPermission(
                ejbName,
                ejbInvocation.getMethodInterface(),
                ejbInvocation.method,
                securityContext.getPrincipalSet());
        } catch (Throwable t) {
            _logger.log(SEVERE, "Unexpected exception manipulating policy context", t);
            authorized = false;
        }

        ejbInvocation.setAuth(authorized);

        doAuditAuthorize(securityContext, ejbInvocation, authorized);

        if (authorized && ejbInvocation.isWebService && !ejbInvocation.isPreInvokeDone()) {
            preInvoke(ejbInvocation);
        }

        return authorized;
    }

    /**
     * This method returns a boolean value indicating whether or not the caller is in the specified role.
     *
     * @param role role name in the form of java.lang.String
     * @return A boolean true/false depending on whether or not the caller has the specified role.
     */
    @Override
    public boolean isCallerInRole(String role) {
        if (_logger.isLoggable(FINE)) {
            _logger.entering("EJBSecurityManager", "isCallerInRole", role);
        }

        SecurityContext securityContext = getSecurityContext();
        Set<Principal> principalSet = securityContext != null ? securityContext.getPrincipalSet() : null;

        return authorizationService.checkBeanRoleRefPermission(ejbName, role, principalSet);
    }

    /**
     * This method is used by MDB Container - Invocation Manager to setup the run-as identity information.
     *
     * <p>
     * It has to be coupled with the postSetRunAsIdentity method. This method is called for EJB/MDB Containers
     */
    @Override
    public void preInvoke(ComponentInvocation invocation) {
        // Optimization to avoid the expensive call
        if (runAs == null) {
            invocation.setPreInvokeDone(true);
            return;
        }

        boolean isWebService = false;
        if (invocation instanceof EjbInvocation) {
            isWebService = ((EjbInvocation) invocation).isWebService;
        }

        // If it is not a webservice or successful authorization
        // and preInvoke is not call before
        if ((!isWebService || (invocation.getAuth() != null && invocation.getAuth())) && !invocation.isPreInvokeDone()) {
            invocation.setOldSecurityContext(SecurityContext.getCurrent());
            loginForRunAs();
            invocation.setPreInvokeDone(true);
        }
    }

    /**
     * This method is similar to the runMethod, except it keeps the semantics same as the one in reflection. On failure, if the
     * exception is caused due to reflection, it returns the InvocationTargetException. This method is called from the containers for
     * ejbTimeout, WebService and MDBs.
     *
     * @param bean the object on which this method is to be invoked in this case the ejb,
     * @param beanClassMethod, the bean class method to be invoked
     * @param methodParameters the parameters for the method,
     *
     * @return Object, the result of the execution of the method.
     */
    @Override
    public Object invoke(Object bean, Method beanClassMethod, Object[] methodParameters) throws Throwable {
        // Need to execute within the target bean's policy context.
        // see CR 6331550
        return authorizationService.invokeBeanMethod(bean, beanClassMethod, methodParameters);
    }

    /**
     * This method is used by Message Driven Bean Container to remove the run-as identity information that was set up using the
     * preSetRunAsIdentity method
     */
    @Override
    public void postInvoke(ComponentInvocation invocation) {
        if (runAs != null && invocation.isPreInvokeDone()) {
            SecurityContext.setCurrent((SecurityContext) invocation.getOldSecurityContext());
        }
    }

    /**
     * This will return the subject associated with the current call. If the run as subject is in effect. It will return that
     * subject. This is done to support the Authorization specification which says if the runas principal is in effect, that
     * principal should be used for making a component call.
     *
     * @return Subject the current subject. Null if this is not the run-as case
     */
    @Override
    public Subject getCurrentSubject() {
        // just get the security context will return the empt subject
        // of the default securityContext when appropriate.
        return SecurityContext.getCurrent().getSubject();
    }

    /**
     * This method returns the Client Principal who initiated the current Invocation.
     *
     * @return A Principal object of the client who made this invocation. or null if the SecurityContext has not been established by
     * the client.
     */
    @Override
    public Principal getCallerPrincipal() {
        SecurityContext securityContext = getSecurityContext();
        if (securityContext == null) {
            return SecurityContext.getDefaultCallerPrincipal();
        }

        return securityContext.getCallerPrincipal();
    }

    @Override
    public void destroy() {
        try {
            authorizationService.refresh();

            /*
             * All enterprise beans of module share same policy context, but each has its
             * own permission cache, which must be unregistered from factory to
             * avoid leaks.
             */
            PermissionCacheFactory.removePermissionCache(uncheckedMethodPermissionCache);
            uncheckedMethodPermissionCache = null;
            roleMapperFactory.removeAppNameForContext(contextId);

        } catch (IllegalStateException e) {
            _logger.log(WARNING, "ejbsm.could_not_delete", e);
        }

        probeProvider.securityManagerDestructionStartedEvent(ejbName);
        ejbSecurityManagerFactory.getManager(contextId, ejbName, true);
        probeProvider.securityManagerDestructionEndedEvent(ejbName);

        probeProvider.securityManagerDestructionEvent(ejbName);
    }





    // ### Private methods

    private static CodeSource getApplicationCodeSource(String contextId) throws Exception {
        CodeSource result = null;
        String archiveURI = "file:///" + contextId.replace(' ', '_');
        try {
            URI uri = null;
            try {
                uri = new URI(archiveURI);
                if (uri != null) {
                    result = new CodeSource(uri.toURL(), (Certificate[]) null);
                }
            } catch (URISyntaxException use) {
                // manually create the URL
                _logger.log(SEVERE, "JACC_createurierror", use);
                throw new RuntimeException(use);
            }

        } catch (MalformedURLException mue) {
            // should never come here.
            _logger.log(SEVERE, "JACC_ejbsm.codesourceerror", mue);
            throw new RuntimeException(mue);
        }

        return result;
    }

    /**
     * Logs in a principal for run-as. This method is called if the run-as principal is required. The user has already logged in -
     * now it needs to change to the new principal. In order that all the correct permissions work - this method logs the new
     * principal with no password -generating valid credentials.
     */
    private void loginForRunAs() {
        LoginContextDriver.loginPrincipal(runAs.getPrincipal(), realmName);
    }

    @Override
    public void resetPolicyContext() {
        PolicyContext.setContextID(null);
    }

    private SecurityContext getSecurityContext() {
        if (runAs == null) {
            return SecurityContext.getCurrent();
        }

        // Return the principal associated with the old security context
        ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();

        if (componentInvocation == null) {
            throw new InvocationException();
        }

        return (SecurityContext) componentInvocation.getOldSecurityContext();
    }

    private String getRealmName(EjbDescriptor deploymentDescriptor) {
        String realmName = deploymentDescriptor.getApplication().getRealm();

        if (realmName == null) {
            for (EjbIORConfigurationDescriptor iorConfig : deploymentDescriptor.getIORConfigurationDescriptors()) {
                // There should be at most one element in the loop from
                // definition of dtd
                realmName = iorConfig.getRealmName();
            }
        }

        return realmName;
    }

    private RunAsIdentityDescriptor getRunAs(EjbDescriptor deploymentDescriptor) {
        if (deploymentDescriptor.getUsesCallerIdentity()) {
            return null;
        }

        RunAsIdentityDescriptor runAs = deploymentDescriptor.getRunAsIdentity();

        // Note: runAs may be null even when runas==true if this Enterprise Bean
        // is an MDB.
        if (runAs != null) {
            if (_logger.isLoggable(FINE)) {
                _logger.log(FINE,
                    deploymentDescriptor.getEjbClassName() + " will run-as: " + runAs.getPrincipal() +
                    " (" + runAs.getRoleName() + ")");
            }
        }

        return runAs;

    }

    private void setEnterpriseBeansStatsProvider() {
        if (ejbStatsProvider == null) {
            synchronized (EjbSecurityStatsProvider.class) {
                if (ejbStatsProvider == null) {
                    ejbStatsProvider = new EjbSecurityStatsProvider();
                    StatsProviderManager.register("security", PluginPoint.SERVER, "security/ejb", ejbStatsProvider);
                }
            }
        }
    }

    private void doAuditAuthorize(SecurityContext securityContext, EjbInvocation ejbInvocation, boolean authorized) {
        if (auditManager.isAuditOn()) {
            String caller = securityContext.getCallerPrincipal().getName();
            auditManager.ejbInvocation(caller, ejbName, ejbInvocation.method.toString(), authorized);

            _logger.fine(() -> " (Caller) = " + caller);
        }
    }

}
