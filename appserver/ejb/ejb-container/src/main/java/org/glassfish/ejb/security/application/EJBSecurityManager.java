/*
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

package org.glassfish.ejb.security.application;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.security.common.Role;

import com.sun.ejb.EjbInvocation;
import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.SecurityManager;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.authorize.PolicyContextHandlerImpl;
import com.sun.enterprise.security.common.AppservAccessController;
import com.sun.enterprise.security.ee.CachedPermission;
import com.sun.enterprise.security.ee.CachedPermissionImpl;
import com.sun.enterprise.security.ee.PermissionCache;
import com.sun.enterprise.security.ee.PermissionCacheFactory;
import com.sun.enterprise.security.ee.SecurityUtil;
import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
import com.sun.logging.LogDomains;

import jakarta.security.jacc.EJBMethodPermission;
import jakarta.security.jacc.EJBRoleRefPermission;
import jakarta.security.jacc.PolicyConfiguration;
import jakarta.security.jacc.PolicyConfigurationFactory;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;

/**
 * This class is used by the EJB server to manage security. All the container object only call into this object for managing
 * security. This class cannot be subclassed.
 * <p/>
 * An instance of this class should be created per deployment unit.
 *
 * @author Harpreet Singh, monzillo
 */
public final class EJBSecurityManager/*extends SecurityManagerFactory*/ implements SecurityManager {

    private static final Logger _logger = LogDomains.getLogger(EJBSecurityManager.class, LogDomains.EJB_LOGGER);

    private AppServerAuditManager auditManager;

    private static final PolicyContextHandlerImpl pcHandlerImpl = (PolicyContextHandlerImpl) PolicyContextHandlerImpl.getInstance();

    private final SecurityRoleMapperFactory roleMapperFactory;

    private final EjbDescriptor deploymentDescriptor;
    // Objects required for Run-AS
    private final RunAsIdentityDescriptor runAs;

    // jacc related
    private static PolicyConfigurationFactory policyConfigurationFactory;
    private String ejbName;
    // contextId id is the same as an appname. This will be used to get
    // a PolicyConfiguration object per application.
    private String contextId;
    private String codebase;
    private CodeSource codesource;
    private String realmName;

    // we use two protection domain caches until we decide how to
    // set the codesource in the protection domain of system apps.
    // PD's in protectionDomainCache have the (privileged) codesource
    // of the EJBSecurityManager class. The PD used in pre-dispatch
    // authorization decisions MUST not be constructed using a privileged
    // codesource (or else all pre-distpatch access decisions will be granted).
    private final Map cacheProtectionDomain = Collections.synchronizedMap(new WeakHashMap());
    private final Map protectionDomainCache = Collections.synchronizedMap(new WeakHashMap());

    private final Map accessControlContextCache = Collections.synchronizedMap(new WeakHashMap());

    private PermissionCache uncheckedMethodPermissionCache = null;

    private final Policy policy;

    private static final CodeSource managerCodeSource = EJBSecurityManager.class.getProtectionDomain().getCodeSource();

    private final InvocationManager invocationManager;
    private final EJBSecurityManagerFactory ejbSecurityManagerFactory;
    private final EjbSecurityProbeProvider probeProvider = new EjbSecurityProbeProvider();
    private static volatile EjbSecurityStatsProvider ejbStatsProvider;

    public EJBSecurityManager(EjbDescriptor ejbDescriptor, InvocationManager invMgr, EJBSecurityManagerFactory ejbSecurityManagerFactory) throws Exception {
        this.deploymentDescriptor = ejbDescriptor;
        this.invocationManager = invMgr;
        this.ejbSecurityManagerFactory = ejbSecurityManagerFactory;
        roleMapperFactory = SecurityUtil.getRoleMapperFactory();

        // get the default policy
        policy = Policy.getPolicy();


        boolean runas = !(deploymentDescriptor.getUsesCallerIdentity());
        if (runas) {
            runAs = deploymentDescriptor.getRunAsIdentity();

            // Note: runAs may be null even when runas==true if this EJB
            // is an MDB.
            if (runAs != null) {
                if (_logger.isLoggable(FINE)) {
                    _logger.log(FINE, deploymentDescriptor.getEjbClassName() + " will run-as: " + runAs.getPrincipal() + " ("
                        + runAs.getRoleName() + ")");
                }
            }
        } else {
            runAs = null;
        }

        initialize();
    }

    public boolean getUsesCallerIdentity() {
        return (runAs == null);
    }

    public void loadPolicyConfiguration(EjbDescriptor eDescriptor) throws Exception {

        boolean inService = getPolicyFactory().inService(contextId);

        // only load the policy configuration if it isn't already in service.
        // Consequently, all things that deploy modules (as apposed to
        // loading already deployed modules) must make sure pre-exiting
        // pc is either in deleted or open state before this method
        // is called. Note that policy statements are not
        // removed to allow multiple EJB's to be represented by same pc.

        if (!inService) {
            // translate the deployment descriptor to configure the policy rules
            convertEJBMethodPermissions(eDescriptor, contextId);
            convertEJBRoleReferences(eDescriptor, contextId);
            if (_logger.isLoggable(FINE)) {
                _logger.fine("JACC: policy translated for policy context:" + contextId);
            }
        }
    }

    public static String getContextID(EjbDescriptor ejbDesc) {
        return SecurityUtil.getContextID(ejbDesc.getEjbBundleDescriptor());
    }

    private void initialize() throws Exception {
        if (ejbStatsProvider == null) {
            synchronized (EjbSecurityStatsProvider.class) {
                if (ejbStatsProvider == null) {
                    ejbStatsProvider = new EjbSecurityStatsProvider();
                    StatsProviderManager.register("security", PluginPoint.SERVER, "security/ejb", ejbStatsProvider);
                }
            }
        }

        contextId = getContextID(deploymentDescriptor);
        String appName = deploymentDescriptor.getApplication().getRegistrationName();
        roleMapperFactory.setAppNameForContext(appName, contextId);
        codesource = getApplicationCodeSource(contextId);
        ejbName = deploymentDescriptor.getName();

        realmName = deploymentDescriptor.getApplication().getRealm();

        if (realmName == null) {
            for (EjbIORConfigurationDescriptor iorConfig : deploymentDescriptor.getIORConfigurationDescriptors()) {
                // There should be at most one element in the loop from
                // definition of dtd
                realmName = iorConfig.getRealmName();
            }
        }

        _logger.log(Level.FINE, () -> "JACC: Context id (id under which all EJB's in application will be created) = " + contextId);
        _logger.log(Level.FINE, () -> "Codebase (module id for ejb " + ejbName + ") = " + codebase);

        loadPolicyConfiguration(deploymentDescriptor);

        // create and initialize the unchecked permission cache.
        uncheckedMethodPermissionCache = PermissionCacheFactory.createPermissionCache(contextId, codesource,
            EJBMethodPermission.class, ejbName);

        auditManager = ejbSecurityManagerFactory.getAuditManager();
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

    // obtains PolicyConfigurationFactory once for class
    private static PolicyConfigurationFactory getPolicyFactory() throws PolicyContextException {
        synchronized (EJBSecurityManager.class) {
            if (policyConfigurationFactory == null) {
                try {
                    policyConfigurationFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
                } catch (ClassNotFoundException cnfe) {
                    _logger.severe("jaccfactory.notfound");
                    throw new PolicyContextException(cnfe);
                } catch (PolicyContextException pce) {
                    _logger.severe("jaccfactory.notfound");
                    throw pce;
                }
            }
        }
        return policyConfigurationFactory;
    }


    /**
     * This method converts ejb role references to jacc permission objects and adds them to the policy configuration object It gets
     * the list of role references from the ejb descriptor. For each such role reference, create a EJBRoleRefPermission and add it to
     * the PolicyConfiguration object.
     *
     * @param ejbDescriptor the ejb descriptor
     * @param pcid, the policy context identifier
     */
    private static void convertEJBRoleReferences(EjbDescriptor ejbDescriptor, String contextId) throws PolicyContextException {
        PolicyConfiguration policyConfiguration = getPolicyFactory().getPolicyConfiguration(contextId, false);

        // Get the set of roles declared
        Set<Role> declaredRoles = ejbDescriptor.getEjbBundleDescriptor().getRoles();
        Role anyAuthUserRole = new Role("**");
        boolean rolesetContainsAnyAuthUserRole = declaredRoles.contains(anyAuthUserRole);
        List<Role> roles = new ArrayList<Role>();
        String ejbName = ejbDescriptor.getName();
        for (RoleReference roleRef : ejbDescriptor.getRoleReferences()) {
            String rolename = roleRef.getRoleName();
            EJBRoleRefPermission ejbRoleRefPermission = new EJBRoleRefPermission(ejbName, rolename);
            String rolelink = roleRef.getSecurityRoleLink().getName();

            roles.add(new Role(rolename));
            policyConfiguration.addToRole(rolelink, ejbRoleRefPermission);

            _logger.log(Level.FINE, () -> "JACC: Converting role-ref -> " + roleRef.toString() + " to permission with name(" + ejbRoleRefPermission.getName()
                + ") and actions (" + ejbRoleRefPermission.getActions() + ")" + "mapped to role (" + rolelink + ")");
        }

        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE,
                "JACC: Converting role-ref: Going through the list of roles not present in RoleRef elements and creating EJBRoleRefPermissions ");
        }

        for (Role declaredRole : declaredRoles) {
            _logger.log(FINE, () -> "JACC: Converting role-ref: Looking at Role =  " + declaredRole.getName());

            if (!roles.contains(declaredRole)) {
                String roleName = declaredRole.getName();
                EJBRoleRefPermission ejbRoleRefPermission = new EJBRoleRefPermission(ejbName, roleName);

                policyConfiguration.addToRole(roleName, ejbRoleRefPermission);
                    _logger.log(FINE, () -> "JACC: Converting role-ref: Role =  " + declaredRole.getName() + " is added as a permission with name("
                        + ejbRoleRefPermission.getName() + ") and actions (" + ejbRoleRefPermission.getActions() + ")" + "mapped to role (" + roleName + ")");
            }
        }

        /**
         * JACC MR8 add EJBRoleRefPermission for the any authenticated user role '**'
         */
        if ((!roles.contains(anyAuthUserRole)) && !rolesetContainsAnyAuthUserRole) {
            String rolename = anyAuthUserRole.getName();
            EJBRoleRefPermission ejbrr = new EJBRoleRefPermission(ejbName, rolename);
            policyConfiguration.addToRole(rolename, ejbrr);
                _logger.log(FINE, () -> "JACC: Converting role-ref: Adding any authenticated user role-ref " + " to permission with name("
                    + ejbrr.getName() + ") and actions (" + ejbrr.getActions() + ")" + "mapped to role (" + rolename + ")");
        }
    }



    /**
     * This method converts the deployment descriptor in two phases.
     *
     * <p>
     * Phase 1: gets a map representing the methodPermission elements exactly as they
     * occured for the ejb in the dd. The map is keyed by method-permission element and each method-permission is mapped to a list of
     * method elements representing the method elements of the method permision element. Each method element is converted to a
     * corresponding EJBMethodPermission and added, based on its associated method-permission, to the policy configuration object.
     *
     * <p>
     * phase 2: configures additional EJBMethodPermission policy statements for the purpose of optimizing Permissions.implies
     * matching by the policy provider. This phase also configures unchecked policy statements for any uncovered methods. This method
     * gets the list of method descriptors for the ejb from the EjbDescriptor object. For each method descriptor, it will get a list
     * of MethodPermission objects that signify the method permissions for the Method and convert each to a corresponding
     * EJBMethodPermission to be added to the policy configuration object.
     *
     * @param ejbDescriptor the ejb descriptor for this EJB.
     * @param pcid, the policy context identifier.
     */
    private static void convertEJBMethodPermissions(EjbDescriptor ejbDescriptor, String contextId) throws PolicyContextException {
        PolicyConfiguration policyConfiguration = getPolicyFactory().getPolicyConfiguration(contextId, false);

        String ejbName = ejbDescriptor.getName();

        Permissions uncheckedPermissions = null;
        Permissions excludedPermissions = null;
        HashMap<String, Permissions> rolePermissionsTable = null;

        EJBMethodPermission ejbMethodPermission = null;

        // phase 1
        Map<MethodPermission, ArrayList<MethodDescriptor>> methodPermissionsFromDD = ejbDescriptor.getMethodPermissionsFromDD();
        if (methodPermissionsFromDD != null) {
            for (var methodPermissionFromDD : methodPermissionsFromDD.entrySet()) {

                MethodPermission methodPermission = methodPermissionFromDD.getKey();

                for (MethodDescriptor methodDescriptor : methodPermissionFromDD.getValue()) {

                    String methodName = methodDescriptor.getName();
                    String methodInterface = methodDescriptor.getEjbClassSymbol();
                    String methodParameters[] = methodDescriptor.getStyle() == 3 ? methodDescriptor.getParameterClassNames() : null;

                    ejbMethodPermission = new EJBMethodPermission(ejbName, methodName.equals("*") ? null : methodName, methodInterface, methodParameters);

                    rolePermissionsTable = addToRolePermissionsTable(rolePermissionsTable, methodPermission, ejbMethodPermission);
                    uncheckedPermissions = addToUncheckedPermissions(uncheckedPermissions, methodPermission, ejbMethodPermission);
                    excludedPermissions = addToExcludedPermissions(excludedPermissions, methodPermission, ejbMethodPermission);
                }
            }
        }

        // phase 2 - configures additional perms:
        //      . to optimize performance of Permissions.implies
        //      . to cause any uncovered methods to be unchecked

        for (MethodDescriptor methodDescriptor : ejbDescriptor.getMethodDescriptors()) {

            Method method = methodDescriptor.getMethod(ejbDescriptor);
            String methodInterface = methodDescriptor.getEjbClassSymbol();

            if (method == null) {
                continue;
            }

            if (methodInterface == null || methodInterface.equals("")) {
                _logger.log(SEVERE, "method_descriptor_not_defined",
                    new Object[] { ejbName, methodDescriptor.getName(), methodDescriptor.getParameterClassNames() });

                continue;
            }

            ejbMethodPermission = new EJBMethodPermission(ejbName, methodInterface, method);

            for (MethodPermission methodPermission : ejbDescriptor.getMethodPermissionsFor(methodDescriptor)) {
                rolePermissionsTable = addToRolePermissionsTable(rolePermissionsTable, methodPermission, ejbMethodPermission);
                uncheckedPermissions = addToUncheckedPermissions(uncheckedPermissions, methodPermission, ejbMethodPermission);
                excludedPermissions = addToExcludedPermissions(excludedPermissions, methodPermission, ejbMethodPermission);
            }
        }

        if (uncheckedPermissions != null) {
            policyConfiguration.addToUncheckedPolicy(uncheckedPermissions);
        }

        if (excludedPermissions != null) {
            policyConfiguration.addToExcludedPolicy(excludedPermissions);
        }

        if (rolePermissionsTable != null) {
            for (var entry : rolePermissionsTable.entrySet()) {
                policyConfiguration.addToRole(entry.getKey(), entry.getValue());
            }
        }
    }

    // utility to collect role permisisions in table of collections
    private static HashMap<String, Permissions> addToRolePermissionsTable(HashMap<String, Permissions> table, MethodPermission methodPermission, EJBMethodPermission ejbMethodPermission) {
        if (methodPermission.isRoleBased()) {
            if (table == null) {
                table = new HashMap<String, Permissions>();
            }

            String roleName = methodPermission.getRole().getName();

            table.computeIfAbsent(roleName, e -> new Permissions())
                 .add(ejbMethodPermission);

            _logger.log(FINE, () -> "JACC DD conversion: EJBMethodPermission ->(" + ejbMethodPermission.getName() + " " + ejbMethodPermission.getActions()
                    + ")protected by role -> " + roleName);
        }

        return table;
    }

    // utility to collect unchecked permissions in collection
    private static Permissions addToUncheckedPermissions(Permissions permissions, MethodPermission methodPermission, EJBMethodPermission ejbMethodPermission) {
        if (methodPermission.isUnchecked()) {
            if (permissions == null) {
                permissions = new Permissions();
            }

            permissions.add(ejbMethodPermission);
            _logger.log(FINE, () -> "JACC DD conversion: EJBMethodPermission ->(" + ejbMethodPermission.getName() + " " + ejbMethodPermission.getActions() + ") is (unchecked)");
        }
        return permissions;
    }

    // utility to collect excluded permissions in collection
    private static Permissions addToExcludedPermissions(Permissions permissions, MethodPermission methodPermission, EJBMethodPermission ejbMethodPermission) {
        if (methodPermission.isExcluded()) {
            if (permissions == null) {
                permissions = new Permissions();
            }

            permissions.add(ejbMethodPermission);
            _logger.log(FINE, () -> "JACC DD conversion: EJBMethodPermission ->(" + ejbMethodPermission.getName() + " " + ejbMethodPermission.getActions() + ") is (excluded)");
        }

        return permissions;
    }

    private ProtectionDomain getCachedProtectionDomain(Set principalSet, boolean applicationCodeSource) {
        ProtectionDomain prdm = null;
        Principal[] principals = null;

        /* Need to use the application codeSource for permission evaluations
        * as the manager codesource is granted all permissions in server.policy.
        * The manager codesource needs to be used for doPrivileged to allow system
        * apps to have all permissions, but we either need to revert to
        * real doAsPrivileged, or find a way to distinguish system apps.
        */

        CodeSource cs = null;

        if (applicationCodeSource) {
            prdm = (ProtectionDomain) cacheProtectionDomain.get(principalSet);
            cs = codesource;
        } else {
            prdm = (ProtectionDomain) protectionDomainCache.get(principalSet);
            cs = managerCodeSource;
        }

        if (prdm == null) {

            principals = (principalSet == null ? null : (Principal[]) principalSet.toArray(new Principal[principalSet.size()]));

            prdm = new ProtectionDomain(cs, null, null, principals);

            // form a new key set so that it does not share with others
            Set newKeySet = ((principalSet != null) ? new HashSet(principalSet) : new HashSet());

            if (applicationCodeSource) {
                cacheProtectionDomain.put(newKeySet, prdm);
            } else {
                // form a new key set so that it does not share with others
                protectionDomainCache.put(newKeySet, prdm);
            }

            if (_logger.isLoggable(FINE)) {
                _logger.fine("JACC: new ProtectionDomain added to cache");
            }

        }

        if (_logger.isLoggable(FINE)) {
            if (principalSet == null) {
                _logger.fine("JACC: returning cached ProtectionDomain PrincipalSet: null");
            } else {
                StringBuffer pBuf = null;
                principals = (Principal[]) principalSet.toArray(new Principal[principalSet.size()]);
                for (int i = 0; i < principals.length; i++) {
                    if (i == 0)
                        pBuf = new StringBuffer(principals[i].toString());
                    else
                        pBuf.append(" " + principals[i].toString());
                }
                _logger.fine("JACC: returning cached ProtectionDomain - CodeSource: (" + cs + ") PrincipalSet: " + pBuf);
            }
        }

        return prdm;
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

        EjbInvocation inv = (EjbInvocation) componentInvocation; //FIXME: Param type should be EjbInvocation
        if (inv.getAuth() != null) {
            return inv.getAuth().booleanValue();
        }

        boolean ret = false;

        CachedPermission cp = null;
        Permission ejbmp = null;

        if (inv.invocationInfo == null || inv.invocationInfo.cachedPermission == null) {
            ejbmp = new EJBMethodPermission(ejbName, inv.getMethodInterface(), inv.method);
            cp = new CachedPermissionImpl(uncheckedMethodPermissionCache, ejbmp);
            if (inv.invocationInfo != null) {
                inv.invocationInfo.cachedPermission = cp;
                if (_logger.isLoggable(FINE)) {
                    _logger.fine("JACC: permission initialized in InvocationInfo: EJBMethodPermission (Name) = " + ejbmp.getName()
                        + " (Action) = " + ejbmp.getActions());
                }
            }
        } else {
            cp = inv.invocationInfo.cachedPermission;
            ejbmp = cp.getPermission();
        }

        String caller = null;
        SecurityContext sc = null;

        pcHandlerImpl.getHandlerData().setInvocation(inv);
        ret = cp.checkPermission();

        if (!ret) {

            sc = SecurityContext.getCurrent();
            Set principalSet = sc.getPrincipalSet();
            ProtectionDomain prdm = getCachedProtectionDomain(principalSet, true);
            try {
                // set the policy context in the TLS.
                String oldContextId = setPolicyContext(this.contextId);
                try {
                    ret = policy.implies(prdm, ejbmp);
                } catch (SecurityException se) {
                    _logger.log(SEVERE, "jacc_access_exception", se);
                    ret = false;
                } catch (Throwable t) {
                    _logger.log(SEVERE, "jacc_access_exception", t);
                    ret = false;
                } finally {
                    resetPolicyContext(oldContextId, this.contextId);
                }

            } catch (Throwable t) {
                _logger.log(SEVERE, "jacc_policy_context_exception", t);
                ret = false;
            }
        }

        inv.setAuth((ret) ? Boolean.TRUE : Boolean.FALSE);

        if (auditManager.isAuditOn()) {
            if (sc == null) {
                sc = SecurityContext.getCurrent();
            }
            caller = sc.getCallerPrincipal().getName();
            auditManager.ejbInvocation(caller, ejbName, inv.method.toString(), ret);
        }

        if (ret && inv.isWebService && !inv.isPreInvokeDone()) {
            preInvoke(inv);
        }

        if (_logger.isLoggable(FINE)) {
            _logger.fine("JACC: Access Control Decision Result: " + ret + " EJBMethodPermission (Name) = " + ejbmp.getName()
                + " (Action) = " + ejbmp.getActions() + " (Caller) = " + caller);
        }

        return ret;
    }

    /**
     * This method is used by MDB Container - Invocation Manager to setup the run-as identity information. It has to be coupled with
     * the postSetRunAsIdentity method. This method is called for EJB/MDB Containers
     */
    @Override
    public void preInvoke(ComponentInvocation inv) {

        //Optimization to avoid the expensive call

        if (runAs == null) {
            inv.setPreInvokeDone(true);
            return;
        }

        boolean isWebService = false;
        if (inv instanceof EjbInvocation) {
            isWebService = ((EjbInvocation) inv).isWebService;
        }

        // if it is not a webservice or successful authorization
        // and preInvoke is not call before
        if ((!isWebService || (inv.getAuth() != null && inv.getAuth().booleanValue())) && !inv.isPreInvokeDone()) {
            inv.setOldSecurityContext(SecurityContext.getCurrent());
            loginForRunAs();
            inv.setPreInvokeDone(true);
        }
    }

    /**
     * This method is used by Message Driven Bean Container to remove the run-as identity information that was set up using the
     * preSetRunAsIdentity method
     */
    @Override
    public void postInvoke(ComponentInvocation inv) {
        if (runAs != null && inv.isPreInvokeDone()) {
            final ComponentInvocation finv = inv;
            AppservAccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public Object run() {
                    SecurityContext.setCurrent((SecurityContext) finv.getOldSecurityContext());
                    return null;
                }
            });
        }
    }

    /**
     * Logs in a principal for run-as. This method is called if the run-as principal is required. The user has already logged in -
     * now it needs to change to the new principal. In order that all the correct permissions work - this method logs the new
     * principal with no password -generating valid credentials.
     */
    private void loginForRunAs() {
        AppservAccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                LoginContextDriver.loginPrincipal(runAs.getPrincipal(), realmName);
                return null;
            }
        });
    }

    /**
     * This method returns a boolean value indicating whether or not the caller is in the specified role.
     *
     * @param role role name in the form of java.lang.String
     * @return A boolean true/false depending on whether or not the caller has the specified role.
     */
    @Override
    public boolean isCallerInRole(String role) {
        /* In case of Run As - Should check isCallerInRole with
        * respect to the old security context.
        */

        boolean isCallerInRole = false;

        if (_logger.isLoggable(FINE)) {
            _logger.entering("EJBSecurityManager", "isCallerInRole", role);

        }
        EJBRoleRefPermission ejbRoleRefPermission = new EJBRoleRefPermission(ejbName, role);

        SecurityContext securityContext;
        if (runAs != null) {
            securityContext = (SecurityContext) invocationManager.getCurrentInvocation().getOldSecurityContext();
        } else {
            securityContext = SecurityContext.getCurrent();
        }

        Set<Principal> principalSet = (securityContext != null) ? securityContext.getPrincipalSet() : null;
        ProtectionDomain protectionDomain = getCachedProtectionDomain(principalSet, true);

        String oldContextId = null;
        try {
            // set the policy context in the TLS.
            oldContextId = setPolicyContext(contextId);
            isCallerInRole = policy.implies(protectionDomain, ejbRoleRefPermission);
        } catch (Throwable t) {
            _logger.log(SEVERE, "jacc_is_caller_in_role_exception", t);
            isCallerInRole = false;
        } finally {
            try {
                resetPolicyContext(oldContextId, contextId);
            } catch (Throwable ex) {
                _logger.log(SEVERE, "jacc_policy_context_exception", ex);
                isCallerInRole = false;
            }
        }

        if (_logger.isLoggable(FINE)) {
            _logger.fine("JACC: isCallerInRole Result: " + isCallerInRole + " EJBRoleRefPermission (Name) = " + ejbRoleRefPermission.getName() + " (Action) = "
                + ejbRoleRefPermission.getActions() + " (Codesource) = " + protectionDomain.getCodeSource());
        }

        return isCallerInRole;
    }

    /**
     * This method returns the Client Principal who initiated the current Invocation.
     *
     * @return A Principal object of the client who made this invocation. or null if the SecurityContext has not been established by
     * the client.
     */
    @Override
    public Principal getCallerPrincipal() {
        SecurityContext securityContext = null;
        if (runAs != null) { // Run As
            // return the principal associated with the old security context
            ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();

            if (componentInvocation == null) {
                throw new InvocationException(); // 4646060
            }
            securityContext = (SecurityContext) componentInvocation.getOldSecurityContext();

        } else {
            // lets optimize a little. no need to look up oldsecctx
            // its the same as the new one
            securityContext = SecurityContext.getCurrent();
        }

        if (securityContext == null) {
            return SecurityContext.getDefaultCallerPrincipal();
        }

        return securityContext.getCallerPrincipal();
    }

    @Override
    public void destroy() {
        try {
            boolean wasInService = getPolicyFactory().inService(this.contextId);
            if (wasInService) {
                policy.refresh();
            }
            /*
             * all ejbs of module share same policy context, but each has its
             * own permission cache, which must be unregistered from factory to
             * avoid leak.
             */
            PermissionCacheFactory.removePermissionCache(uncheckedMethodPermissionCache);
            uncheckedMethodPermissionCache = null;
            roleMapperFactory.removeAppNameForContext(this.contextId);

        } catch (PolicyContextException pce) {
            _logger.log(Level.WARNING, "ejbsm.could_not_delete", pce);
        }

        probeProvider.securityManagerDestructionStartedEvent(ejbName);
        ejbSecurityManagerFactory.getManager(contextId, ejbName, true);
        probeProvider.securityManagerDestructionEndedEvent(ejbName);

        probeProvider.securityManagerDestructionEvent(ejbName);
    }

    /**
     * This will return the subject associated with the current call. If the run as subject is in effect. It will return that
     * subject. This is done to support the JACC specification which says if the runas principal is in effect, that principal should
     * be used for making a component call.
     *
     * @return Subject the current subject. Null if this is not the run-as case
     */
    @Override
    public Subject getCurrentSubject() {
        // just get the security context will return the empt subject
        // of the default securityContext when appropriate.
        return SecurityContext.getCurrent().getSubject();
    }

    /* This method is used by SecurityUtil runMethod to run the
     * action as the subject encapsulated in the current
     * SecurityContext.
     */
    @Override
    public Object doAsPrivileged(PrivilegedExceptionAction pea) throws Throwable {

        SecurityContext sc = SecurityContext.getCurrent();
        Set principalSet = sc.getPrincipalSet();
        AccessControlContext acc = (AccessControlContext) accessControlContextCache.get(principalSet);

        if (acc == null) {
            final ProtectionDomain[] pdArray = new ProtectionDomain[1];
            pdArray[0] = getCachedProtectionDomain(principalSet, false);
            try {
                if (principalSet != null) {

                    final Subject s = sc.getSubject();

                    acc = (AccessControlContext) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        @Override
                        public Object run() throws Exception {
                            return new AccessControlContext(new AccessControlContext(pdArray), new SubjectDomainCombiner(s));
                        }
                    });
                } else {
                    acc = new AccessControlContext(pdArray);
                }

                // form a new key set so that it does not share with
                // cacheProtectionDomain and protectionDomainCache
                if (principalSet != null) {
                    accessControlContextCache.put(new HashSet(principalSet), acc);
                }

                _logger.fine("JACC: new AccessControlContext added to cache");

            } catch (Exception e) {
                _logger.log(SEVERE, "java_security.security_context_exception", e);
                acc = null;
                throw e;
            }
        }

        Object rvalue = null;
        String oldContextId = setPolicyContext(this.contextId);
        if (_logger.isLoggable(FINE)) {
            _logger.fine("JACC: doAsPrivileged contextId(" + this.contextId + ")");
        }

        try {
            rvalue = AccessController.doPrivileged(pea, acc);
        } finally {
            resetPolicyContext(oldContextId, this.contextId);
        }
        return rvalue;
    }

    /**
     * Runs a business method of an EJB within the bean's policy context. The original policy context is restored after method
     * execution. This method should only be used by com.sun.enterprise.security.SecurityUtil.
     *
     * @param beanClassMethod the EJB business method
     * @param obj the EJB bean instance
     * @param oa parameters passed to beanClassMethod
     * @return return value from beanClassMethod
     * @throws java.lang.reflect.InvocationTargetException if the underlying method throws an exception
     * @throws Throwable other throwables in other cases
     */
    public Object runMethod(Method beanClassMethod, Object obj, Object[] oa) throws Throwable {
        String oldCtxID = setPolicyContext(this.contextId);
        Object ret = null;
        try {
            ret = beanClassMethod.invoke(obj, oa);
        } finally {
            resetPolicyContext(oldCtxID, this.contextId);
        }
        return ret;
    }

    private static void resetPolicyContext(final String newV, String oldV) throws Throwable {
        if (newV != null && (oldV == null || !oldV.equals(newV))) {

            if (_logger.isLoggable(FINE)) {
                _logger.fine("JACC: Changing Policy Context ID: oldV = " + oldV + " newV = " + newV);
            }
            try {
                AppservAccessController.doPrivileged(new PrivilegedExceptionAction() {
                    @Override
                    public Object run() throws Exception {
                        PolicyContext.setContextID(newV);
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                Throwable cause = pae.getCause();
                if (cause instanceof java.security.AccessControlException) {
                    _logger.log(SEVERE, "jacc_policy_context_security_exception", cause);
                } else {
                    _logger.log(SEVERE, "jacc_policy_context_exception", cause);
                }
                throw cause;
            }
        }
    }

    private static String setPolicyContext(String newV) throws Throwable {
        String oldV = PolicyContext.getContextID();
        resetPolicyContext(newV, oldV);
        return oldV;
    }

    /**
     * This method is similiar to the runMethod, except it keeps the semantics same as the one in reflection. On failure, if the
     * exception is caused due to reflection, it returns the InvocationTargetException. This method is called from the containers for
     * ejbTimeout, WebService and MDBs.
     *
     * @param beanClassMethod, the bean class method to be invoked
     * @param isLocal, true if this invocation is through the local EJB view
     * @param o the object on which this method is to be invoked in this case the ejb,
     * @param oa the parameters for the method,
     * @param c, the container instance can be a null value, where in the container will be queried to find its security manager.
     * @return Object, the result of the execution of the method.
     */
    @Override
    public Object invoke(Method beanClassMethod, boolean isLocal, Object o, Object[] oa) throws Throwable {

        final Method meth = beanClassMethod;
        final Object obj = o;
        final Object[] objArr = oa;
        Object ret = null;

        // Optimization.  Skip doAsPrivileged call if this is a local
        // invocation and the target ejb uses caller identity or the
        // System Security Manager is disabled.
        // Still need to execute it within the target bean's policy context.
        // see CR 6331550
        if ((isLocal && this.getUsesCallerIdentity()) || System.getSecurityManager() == null) {
            ret = this.runMethod(meth, obj, objArr);
        } else {

            PrivilegedExceptionAction pea = new PrivilegedExceptionAction() {
                @Override
                public Object run() throws Exception {
                    return meth.invoke(obj, objArr);
                }
            };

            try {
                ret = this.doAsPrivileged(pea);
            } catch (PrivilegedActionException pae) {
                Throwable cause = pae.getCause();
                throw cause;
            }
        }
        return ret;
    }

    @Override
    public void resetPolicyContext() {
        if (System.getSecurityManager() == null) {
            ((PolicyContextHandlerImpl) PolicyContextHandlerImpl.getInstance()).reset();
            PolicyContext.setContextID(null);
            return;
        }

        try {
            AppservAccessController.doPrivileged(new PrivilegedExceptionAction() {
                @Override
                public Object run() throws Exception {
                    ((PolicyContextHandlerImpl) PolicyContextHandlerImpl.getInstance()).reset();
                    PolicyContext.setContextID(null);
                    return null;
                }
            });
        } catch (PrivilegedActionException pae) {
            Throwable cause = pae.getCause();
            if (cause instanceof java.security.AccessControlException) {
                _logger.log(SEVERE, "jacc_policy_context_security_exception", cause);
            } else {
                _logger.log(SEVERE, "jacc_policy_context_exception", cause);
            }
            throw new RuntimeException(cause);
        }
    }

}
