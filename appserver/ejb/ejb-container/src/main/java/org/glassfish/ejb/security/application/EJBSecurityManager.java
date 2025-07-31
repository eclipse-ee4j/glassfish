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
import com.sun.enterprise.security.common.AppservAccessController;
import com.sun.enterprise.security.ee.SecurityUtil;
import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
import com.sun.enterprise.security.ee.authorize.cache.PermissionCache;
import com.sun.enterprise.security.ee.authorize.cache.PermissionCacheFactory;
import com.sun.logging.LogDomains;

import jakarta.security.jacc.EJBMethodPermission;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;

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

import static java.lang.System.getSecurityManager;
import static java.util.Collections.synchronizedMap;
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

    private final Map<Set<Principal>, ProtectionDomain> applicationProtectionDomainCache = synchronizedMap(new WeakHashMap<>());
    private final Map<Set<Principal>, ProtectionDomain>  managerProtectionDomainCache = synchronizedMap(new WeakHashMap<>());
    private final Map<Set<Principal>, AccessControlContext> accessControlContextCache = synchronizedMap(new WeakHashMap<>());

    private PermissionCache uncheckedMethodPermissionCache;

    private static final CodeSource managerCodeSource = EJBSecurityManager.class.getProtectionDomain().getCodeSource();

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
    private final CodeSource applicationCodeSource; // contrast to managerCodeSource above
    private String codebase;
    private final String ejbName;
    private final String realmName;
    private final AppServerAuditManager auditManager;


    public EJBSecurityManager(EjbDescriptor ejbDescriptor, InvocationManager invMgr, EJBSecurityManagerFactory ejbSecurityManagerFactory) throws Exception {
        this.deploymentDescriptor = ejbDescriptor;
        this.invocationManager = invMgr;
        this.ejbSecurityManagerFactory = ejbSecurityManagerFactory;
        roleMapperFactory = SecurityUtil.getRoleMapperFactory();

        runAs = getRunAs(deploymentDescriptor);

        setEnterpriseBeansStatsProvider();

        contextId = getContextID(deploymentDescriptor);
        roleMapperFactory.setAppNameForContext(deploymentDescriptor.getApplication().getRegistrationName(), contextId);
        applicationCodeSource = getApplicationCodeSource(contextId);
        ejbName = deploymentDescriptor.getName();
        realmName = getRealmName(deploymentDescriptor);

        _logger.log(Level.FINE, () -> "JACC: Context id (id under which all EJB's in application will be created) = " + contextId);
        _logger.log(Level.FINE, () -> "Codebase (module id for ejb " + ejbName + ") = " + codebase);

        // Create and initialize the unchecked permission cache.
        uncheckedMethodPermissionCache = PermissionCacheFactory.createPermissionCache(
            contextId, applicationCodeSource,
            EJBMethodPermission.class, ejbName);

        auditManager = ejbSecurityManagerFactory.getAuditManager();

        authorizationService = new AuthorizationService(
            getContextID(ejbDescriptor),
            () -> SecurityContext.getCurrent().getSubject(),
            null);

        authorizationService.setProtectionDomainCreator(principalSet -> getCachedProtectionDomain(principalSet, true));

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
        return SecurityUtil.getContextID(ejbDescriptor.getEjbBundleDescriptor());
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
     * @param beanClassMethod, the bean class method to be invoked
     * @param isLocal, true if this invocation is through the local EJB view
     * @param bean the object on which this method is to be invoked in this case the ejb,
     * @param methodParameters the parameters for the method,
     * @param c, the container instance can be a null value, where in the container will be queried to find its security manager.
     * @return Object, the result of the execution of the method.
     */
    @Override
    public Object invoke(Method beanClassMethod, boolean isLocal, Object bean, Object[] methodParameters) throws Throwable {

        // Optimization.  Skip doAsPrivileged call if this is a local
        // invocation and the target ejb uses caller identity or the
        // System Security Manager is disabled.
        // Still need to execute it within the target bean's policy context.
        // see CR 6331550
        if ((isLocal && getUsesCallerIdentity())) {
            return authorizationService.invokeBeanMethod(bean, beanClassMethod, methodParameters);
        }

        PrivilegedExceptionAction<Object> pea = new PrivilegedExceptionAction<>() {
            @Override
            public Object run() throws Exception {
                return beanClassMethod.invoke(bean, methodParameters);
            }
        };

        try {
            return doAsPrivileged(pea);
        } catch (PrivilegedActionException pae) {
            throw pae.getCause();
        }
    }

    /**
     * This method is used by Message Driven Bean Container to remove the run-as identity information that was set up using the
     * preSetRunAsIdentity method
     */
    @Override
    public void postInvoke(ComponentInvocation invocation) {
        if (runAs != null && invocation.isPreInvokeDone()) {
            AppservAccessController.doPrivileged(new PrivilegedAction<>() {
                @Override
                public Object run() {
                    SecurityContext.setCurrent((SecurityContext) invocation.getOldSecurityContext());
                    return null;
                }
            });
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

    /* This method is used by SecurityUtil runMethod to run the
     * action as the subject encapsulated in the current
     * SecurityContext.
     */
    @Override
    public Object doAsPrivileged(PrivilegedExceptionAction<Object> privilegedAction) throws Throwable {
        AccessControlContext accessControlContext = getCachedAccessControlContext(SecurityContext.getCurrent());

        return authorizationService.runInScope(() -> AccessController.doPrivileged(privilegedAction, accessControlContext));
    }




    // ### Private methods

    private boolean getUsesCallerIdentity() {
        return runAs == null;
    }

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

    private ProtectionDomain getCachedProtectionDomain(Set<Principal> principalSet, boolean useApplicationCodeSource) {
        ProtectionDomain protectionDomain = null;
        Principal[] principals = null;


        // Need to use the application codeSource for permission evaluations
        // as the manager applicationCodeSource is granted all permissions in server.policy.

        // The manager applicationCodeSource needs to be used for doPrivileged to allow system
        // apps to have all permissions, but we either need to revert to
        // real doAsPrivileged, or find a way to distinguish system apps.

        CodeSource currentCodeSource = null;

        if (useApplicationCodeSource) {
            protectionDomain = applicationProtectionDomainCache.get(principalSet);
            currentCodeSource = applicationCodeSource;
        } else {
            protectionDomain = managerProtectionDomainCache.get(principalSet);
            currentCodeSource = managerCodeSource;
        }

        if (protectionDomain == null) {
            principals = principalSet == null ? null : principalSet.toArray(new Principal[principalSet.size()]);

            protectionDomain = new ProtectionDomain(currentCodeSource, null, null, principals);

            // Form a new key set so that it does not share with others
            Set<Principal> newKeySet = (principalSet != null) ? new HashSet<>(principalSet) : new HashSet<>();

            if (useApplicationCodeSource) {
                applicationProtectionDomainCache.put(newKeySet, protectionDomain);
            } else {
                managerProtectionDomainCache.put(newKeySet, protectionDomain);
            }

            _logger.fine(() -> "Authorization: new ProtectionDomain added to cache");

        }

        if (_logger.isLoggable(FINE)) {
            if (principalSet == null) {
                _logger.fine("Authorization: returning cached ProtectionDomain PrincipalSet: null");
            } else {
                StringBuilder principalBuilder = null;
                principals = principalSet.toArray(new Principal[principalSet.size()]);
                for (int i = 0; i < principals.length; i++) {
                    if (i == 0) {
                        principalBuilder = new StringBuilder(principals[i].toString());
                    } else {
                        principalBuilder.append(" " + principals[i].toString());
                    }
                }

                _logger.fine("Authorization: returning cached ProtectionDomain - CodeSource: (" + currentCodeSource + ") PrincipalSet: " + principalBuilder);
            }
        }

        return protectionDomain;
    }

    private AccessControlContext getCachedAccessControlContext(SecurityContext securityContext) throws Exception {
        Set<Principal> principalSet = securityContext.getPrincipalSet();

        AccessControlContext accessControlContext = accessControlContextCache.get(principalSet);

        if (accessControlContext == null) {
            ProtectionDomain[] protectionDomainArray = new ProtectionDomain[1];
            protectionDomainArray[0] = getCachedProtectionDomain(principalSet, false);

            try {
                if (principalSet != null) {

                    Subject subject = securityContext.getSubject();

                    accessControlContext = AccessController.doPrivileged(new PrivilegedExceptionAction<>() {
                        @Override
                        public AccessControlContext run() throws Exception {
                            return new AccessControlContext(new AccessControlContext(protectionDomainArray), new SubjectDomainCombiner(subject));
                        }
                    });
                } else {
                    accessControlContext = new AccessControlContext(protectionDomainArray);
                }

                // Form a new key set so that it does not share with
                // applicationProtectionDomainCache and managerProtectionDomainCache
                if (principalSet != null) {
                    accessControlContextCache.put(new HashSet<>(principalSet), accessControlContext);
                }

                _logger.fine("Authorization: new AccessControlContext added to cache");

            } catch (Exception e) {
                _logger.log(SEVERE, "java_security.security_context_exception", e);
                accessControlContext = null;
                throw e;
            }
        }

        return accessControlContext;
    }



    /**
     * Logs in a principal for run-as. This method is called if the run-as principal is required. The user has already logged in -
     * now it needs to change to the new principal. In order that all the correct permissions work - this method logs the new
     * principal with no password -generating valid credentials.
     */
    private void loginForRunAs() {
        AppservAccessController.doPrivileged(new PrivilegedAction<>() {
            @Override
            public Object run() {
                LoginContextDriver.loginPrincipal(runAs.getPrincipal(), realmName);
                return null;
            }
        });
    }

    @Override
    public void resetPolicyContext() {

    }

    private SecurityContext getSecurityContext() {
        if (runAs == null) {
            return SecurityContext.getCurrent();
        }

        // Return the principal associated with the old security context
        ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();

        if (componentInvocation == null) {
            throw new InvocationException(); // 4646060
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
        if (!Boolean.FALSE.equals(deploymentDescriptor.getUsesCallerIdentity())) {
            // true or null disable runAs
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
