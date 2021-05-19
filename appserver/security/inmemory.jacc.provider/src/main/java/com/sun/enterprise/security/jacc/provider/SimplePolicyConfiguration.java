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

package com.sun.enterprise.security.jacc.provider;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanPermission;

import jakarta.security.jacc.EJBRoleRefPermission;
import jakarta.security.jacc.PolicyConfiguration;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;
import jakarta.security.jacc.PolicyContextHandler;
import jakarta.security.jacc.WebResourcePermission;
import jakarta.security.jacc.WebRoleRefPermission;
import jakarta.security.jacc.WebUserDataPermission;

/**
 * The methods of this interface are used by containers to create policy statements in a Policy provider.
 *
 * <p>
 * An object that implements the PolicyConfiguration interface provides the policy statement configuration interface for
 * a corresponding policy context within the corresponding Policy provider.
 *
 * @author monzillo
 */
public class SimplePolicyConfiguration implements PolicyConfiguration {

    public static final int OPEN_STATE = 0;
    public static final int INSERVICE_STATE = 2;
    public static final int DELETED_STATE = 3;
    private static final Permission setPolicyPermission = new java.security.SecurityPermission("setPolicy");
    private String contextId;
    private int state = OPEN_STATE;

    // Excluded permissions
    private PermissionCollection excludedPermissions;

    // Unchecked permissions
    private PermissionCollection uncheckedPermissions;

    // roleTbale mapps permissions and principals to roles.
    private ArrayList<Role> roleTable = null;

    // Lock on this PolicyConfiguration onject
    private ReentrantReadWriteLock pcLock = new ReentrantReadWriteLock(true);
    private Lock pcrLock = pcLock.readLock();
    private Lock pcwLock = pcLock.writeLock();

    static {
        // Register a role mapper
        try {
            String className = System.getProperty(JACCRoleMapper.CLASS_NAME);
            if (className != null || !PolicyContext.getHandlerKeys().contains(JACCRoleMapper.HANDLER_KEY)) {
                if (className == null) {
                    String packageName = SimplePolicyConfiguration.class.getPackage().getName();
                    className = packageName + "." + "GlassfishRoleMapper";
                }

                Constructor<?> constructor = Thread.currentThread().getContextClassLoader().loadClass(className)
                        .getConstructor(new Class[] { Logger.class });

                PolicyContext.registerHandler(JACCRoleMapper.HANDLER_KEY, new PolicyContextHandler() {

                    @Override
                    public Object getContext(String key, Object data) throws PolicyContextException {
                        if (key.equals(JACCRoleMapper.HANDLER_KEY)) {
                            try {
                                return constructor.newInstance(new Object[] { SharedState.getLogger() });
                            } catch (Throwable t) {
                                throw new PolicyContextException(t);
                            }
                        }
                        return null;
                    }

                    @Override
                    public String[] getKeys() throws PolicyContextException {
                        return new String[] { JACCRoleMapper.HANDLER_KEY };
                    }

                    @Override
                    public boolean supports(String key) throws PolicyContextException {
                        return key.equals(JACCRoleMapper.HANDLER_KEY);
                    }
                }, false);
            }
        } catch (Throwable t) {
            SharedState.getLogger().log(Level.SEVERE, "RoleMapper.registration.failed", t);
            throw new RuntimeException(t);
        }
    }

    /**
     * Creates a new instance of SimplePolicyConfiguration
     */
    protected SimplePolicyConfiguration(String contextID) {
        this.contextId = contextID;
    }

    // ### Public Policy Configuration Interfaces Start here ###

    /**
     * This method returns this object's policy context identifier.
     *
     * @return this object's policy context identifier.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the getContextID method signature. The exception thrown by the implementation class will be
     * encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public String getContextID() throws jakarta.security.jacc.PolicyContextException {
        return contextId;
    }

    /**
     * Used to add permissions to a named role in this PolicyConfiguration. If the named Role does not exist in the
     * PolicyConfiguration, it is created as a result of the call to this function.
     * <P>
     * It is the job of the Policy provider to ensure that all the permissions added to a role are granted to principals
     * "mapped to the role".
     * <P>
     *
     * @param roleName the name of the Role to which the permissions are to be added.
     * <P>
     * @param permissions the collection of permissions to be added to the role. The collection may be either a homogenous
     * or heterogenous collection.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the addToRole method signature. The exception thrown by the implementation class will be
     * encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void addToRole(String roleName, PermissionCollection permissions) throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        pcwLock.lock();
        try {
            assertStateIsOpen();
            if (roleName != null && permissions != null) {
                getRole(roleName).addPermissions(permissions);
            }
        } finally {
            pcwLock.unlock();
        }

    }

    /**
     * Used to add a single permission to a named role in this PolicyConfiguration. If the named Role does not exist in the
     * PolicyConfiguration, it is created as a result of the call to this function.
     * <P>
     * It is the job of the Policy provider to ensure that all the permissions added to a role are granted to principals
     * "mapped to the role".
     * <P>
     *
     * @param roleName the name of the Role to which the permission is to be added.
     * <P>
     * @param permission the permission to be added to the role.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the addToRole method signature. The exception thrown by the implementation class will be
     * encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void addToRole(String roleName, Permission permission) throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        pcwLock.lock();
        try {
            assertStateIsOpen();
            if (roleName != null && permission != null) {
                getRole(roleName).addPermission(permission);
            }
        } finally {
            pcwLock.unlock();
        }

    }

    /**
     * Used to add unchecked policy statements to this PolicyConfiguration.
     * <P>
     *
     * @param permissions the collection of permissions to be added as unchecked policy statements. The collection may be
     * either a homogenous or heterogenous collection.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the addToUncheckedPolicy method signature. The exception thrown by the implementation class
     * will be encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void addToUncheckedPolicy(PermissionCollection permissions) throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        pcwLock.lock();
        try {
            assertStateIsOpen();
            if (permissions != null) {
                for (Enumeration e = permissions.elements(); e.hasMoreElements();) {
                    this.getUncheckedPermissions().add((Permission) e.nextElement());
                }

            }
        } finally {
            pcwLock.unlock();
        }

    }

    /**
     * Used to add a single unchecked policy statement to this PolicyConfiguration.
     * <P>
     *
     * @param permission the permission to be added to the unchecked policy statements.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the addToUncheckedPolicy method signature. The exception thrown by the implementation class
     * will be encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void addToUncheckedPolicy(Permission permission) throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        pcwLock.lock();
        try {
            assertStateIsOpen();
            if (permission != null) {
                getUncheckedPermissions().add(permission);
            }

        } finally {
            pcwLock.unlock();
        }

    }

    /**
     * Used to add excluded policy statements to this PolicyConfiguration.
     * <P>
     *
     * @param permissions the collection of permissions to be added to the excluded policy statements. The collection may be
     * either a homogenous or heterogenous collection.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the addToExcludedPolicy method signature. The exception thrown by the implementation class will
     * be encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void addToExcludedPolicy(PermissionCollection permissions) throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        pcwLock.lock();
        try {
            assertStateIsOpen();
            if (permissions != null) {
                for (Enumeration e = permissions.elements(); e.hasMoreElements();) {
                    this.getExcludedPermissions().add((Permission) e.nextElement());
                }

            }
        } finally {
            pcwLock.unlock();
        }

    }

    /**
     * Used to add a single excluded policy statement to this PolicyConfiguration.
     * <P>
     *
     * @param permission the permission to be added to the excluded policy statements.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the addToExcludedPolicy method signature. The exception thrown by the implementation class will
     * be encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void addToExcludedPolicy(Permission permission) throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        pcwLock.lock();
        try {
            assertStateIsOpen();
            if (permission != null) {
                getExcludedPermissions().add(permission);
            }

        } finally {
            pcwLock.unlock();
        }

    }

    /**
     * Used to remove a role and all its permissions from this PolicyConfiguration.
     * <P>
     *
     * @param roleName the name of the Role to remove from this PolicyConfiguration.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the removeRole method signature. The exception thrown by the implementation class will be
     * encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void removeRole(String roleName) throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        pcwLock.lock();
        try {
            assertStateIsOpen();
            if (roleName != null && roleTable != null) {
                if (!roleTable.remove(new Role(roleName))) {
                    if (roleName.equals("*")) {
                        roleTable.clear();
                        roleTable = null;
                    }
                } else if (roleTable.isEmpty()) {
                    roleTable = null;
                }
            }

        } finally {
            pcwLock.unlock();
        }

    }

    /**
     * Used to remove any unchecked policy statements from this PolicyConfiguration.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the removeUncheckedPolicy method signature. The exception thrown by the implementation class
     * will be encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void removeUncheckedPolicy() throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        pcwLock.lock();
        try {
            assertStateIsOpen();
            uncheckedPermissions = null;
        } finally {
            pcwLock.unlock();
        }

    }

    /**
     * Used to remove any excluded policy statements from this PolicyConfiguration.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the removeExcludedPolicy method signature. The exception thrown by the implementation class
     * will be encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void removeExcludedPolicy() throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        pcwLock.lock();
        try {
            assertStateIsOpen();
            excludedPermissions = null;
        } finally {
            pcwLock.unlock();
        }

    }

    /**
     * Creates a relationship between this configuration and another such that they share the same principal-to-role
     * mappings. PolicyConfigurations are linked to apply a common principal-to-role mapping to multiple seperately
     * manageable PolicyConfigurations, as is required when an application is composed of multiple modules.
     * <P>
     * Note that the policy statements which comprise a role, or comprise the excluded or unchecked policy collections in a
     * PolicyConfiguration are unaffected by the configuration being linked to another.
     * <P>
     *
     * @param link a reference to a different PolicyConfiguration than this PolicyConfiguration.
     * <P>
     * The relationship formed by this method is symetric, transitive and idempotent. If the argument PolicyConfiguration
     * does not have a different Policy context identifier than this PolicyConfiguration no relationship is formed, and an
     * exception, as described below, is thrown.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" or "inService" when this method is called.
     *
     * @throws java.lang.IllegalArgumentException if called with an argument PolicyConfiguration whose Policy context is
     * equivalent to that of this PolicyConfiguration.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the linkConfiguration method signature. The exception thrown by the implementation class will
     * be encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void linkConfiguration(PolicyConfiguration link) throws jakarta.security.jacc.PolicyContextException {

        checkSetPolicyPermission();
        pcrLock.lock();
        try {
            assertStateIsOpen();
        } finally {
            pcrLock.unlock();
        }
        /*
         * if at this point, a simultaneous attempt is made to delete or commit this pc, we could end up with a corrupted link
         * table. Niether event is likely, but we should try to properly serialize those events.
         */

        SharedState.link(contextId, link.getContextID());
    }

    /**
     * Causes all policy statements to be deleted from this PolicyConfiguration and sets its internal state such that
     * calling any method, other than delete, getContextID, or inService on the PolicyConfiguration will be rejected and
     * cause an UnsupportedOperationException to be thrown.
     * <P>
     * This operation has no affect on any linked PolicyConfigurations other than removing any links involving the deleted
     * PolicyConfiguration.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the delete method signature. The exception thrown by the implementation class will be
     * encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void delete() throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        SharedState.removeLinks(contextId);

        /*
         * final state will be unlinked and deleted
         */

        pcwLock.lock();
        try {
            removePolicy();
        } finally {
            try {
                setState(DELETED_STATE);
            } finally {
                pcwLock.unlock();
            }
        }

    }

    /**
     * This method is used to set to "inService" the state of the policy context whose interface is this PolicyConfiguration
     * Object. Only those policy contexts whose state is "inService" will be included in the policy contexts processed by
     * the Policy.refresh method. A policy context whose state is "inService" may be returned to the "owpen" state by
     * calling the getPolicyConfiguration method of the PolicyConfiguration factory with the policy context identifier of
     * the policy context.
     * <P>
     * When the state of a policy context is "inService", calling any method other than commit, delete, getContextID, or
     * inService on its PolicyConfiguration Object will cause an UnsupportedOperationException to be thrown.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws java.lang.UnsupportedOperationException if the state of the policy context whose interface is this
     * PolicyConfiguration Object is "deleted" when this method is called.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the commit method signature. The exception thrown by the implementation class will be
     * encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public void commit() throws jakarta.security.jacc.PolicyContextException {
        checkSetPolicyPermission();
        boolean initRoles = false;
        pcwLock.lock();
        try {
            if (stateIs(DELETED_STATE)) {
                String msg = "pc.invalid_op_for_state_delete";
                throw new UnsupportedOperationException(msg);
            }

            if (stateIs(OPEN_STATE)) {
                if (roleTable != null) {
                    initRoles = true;
                }
                setState(INSERVICE_STATE);
            }

        } finally {
            pcwLock.unlock();
        }

        if (initRoles) {
            commitRoleMapping();
        }
    }

    /**
     * This method is used to determine if the policy context whose interface is this PolicyConfiguration Object is in the
     * "inService" state.
     *
     * @return true if the state of the associated policy context is "inService"; false otherwise.
     *
     * @throws java.lang.SecurityException if called by an AccessControlContext that has not been granted the "setPolicy"
     * SecurityPermission.
     *
     * @throws jakarta.security.jacc.PolicyContextException if the implementation throws a checked exception that has not
     * been accounted for by the inService method signature. The exception thrown by the implementation class will be
     * encapsulated (during construction) in the thrown PolicyContextException.
     */
    @Override
    public boolean inService() throws jakarta.security.jacc.PolicyContextException {
        pcrLock.lock();
        try {
            return stateIs(INSERVICE_STATE);
        } finally {
            pcrLock.unlock();
        }
    }

    // Internal Policy Configuration interfaces start here
    protected static SimplePolicyConfiguration getPolicyConfig(String pcid, boolean remove) throws PolicyContextException {

        SimplePolicyConfiguration simplePolicyConfiguration = SharedState.getConfig(pcid, remove);
        simplePolicyConfiguration.pcwLock.lock();
        try {
            if (remove) {
                simplePolicyConfiguration.removePolicy();
            }
            simplePolicyConfiguration.setState(OPEN_STATE);
        } finally {
            simplePolicyConfiguration.pcwLock.unlock();
        }

        return simplePolicyConfiguration;
    }

    protected static boolean inService(String pcid) throws jakarta.security.jacc.PolicyContextException {
        SimplePolicyConfiguration simplePolicyConfiguration = SharedState.lookupConfig(pcid);
        if (simplePolicyConfiguration == null) {
            return false;
        }

        return simplePolicyConfiguration.inService();
    }

    protected static void checkSetPolicyPermission() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(setPolicyPermission);
        }

    }

    private void setState(int stateValue) {
        this.state = stateValue;
    }

    private boolean stateIs(int stateValue) {
        return this.state == stateValue;
    }

    private void assertStateIsOpen() throws UnsupportedOperationException {
        if (!stateIs(OPEN_STATE)) {
            throw new UnsupportedOperationException("Operation invoked on closed or deleted PolicyConfiguration.");
        }
    }

    private void assertStateIsInService() throws UnsupportedOperationException {
        if (!stateIs(INSERVICE_STATE)) {
            throw new UnsupportedOperationException("Operation invoked on open or deleted PolicyConfiguration.");
        }
    }

    private PermissionCollection getUncheckedPermissions() {
        if (uncheckedPermissions == null) {
            uncheckedPermissions = new Permissions();
        }

        return uncheckedPermissions;
    }

    private PermissionCollection getExcludedPermissions() {
        if (excludedPermissions == null) {
            excludedPermissions = new Permissions();
        }

        return excludedPermissions;
    }

    private Role getRole(String roleName) {
        int index = -1;
        Role rvalue = new Role(roleName);

        if (roleTable == null) {
            roleTable = new ArrayList<>();
        } else {
            index = roleTable.indexOf(rvalue);
        }

        if (index < 0) {
            roleTable.add(rvalue);
        } else {
            rvalue = roleTable.get(index);
        }

        return rvalue;
    }

    private void removePolicy() {
        excludedPermissions = null;
        uncheckedPermissions = null;
        roleTable = null;
    }

    /**
     * Adds the principal-2-role mapping to the roles in the roleTable
     *
     * @throws jakarta.security.jacc.PolicyContextException
     */
    private void commitRoleMapping() throws PolicyContextException {
        JACCRoleMapper roleMapper = null;
        try {
            /**
             * NB: when running with a security manager, this method will call policy, to check if the policy module is authorized
             * to invoke the policy context handler.
             */
            roleMapper = (JACCRoleMapper) PolicyContext.getContext(JACCRoleMapper.HANDLER_KEY);

            if (roleMapper == null) {
                throw new PolicyContextException("RoleMapper.lookup.null");
            }
        } catch (Throwable t) {
            SharedState.getLogger().log(Level.SEVERE, "RoleMapper.lookup.failed", t);
            if (t instanceof PolicyContextException) {
                throw (PolicyContextException) t;
            }

            throw new PolicyContextException(t);
        }

        pcwLock.lock();
        try {
            if (roleTable != null) {
                for (Role role : roleTable) {
                    role.setPrincipals(roleMapper.getPrincipalsInRole(contextId, role.getName()));
                }

                /**
                 * JACC MR8 add handling for the any authenticated user role '**'
                 */
                Role anyAuthRole = new Role("**");
                int index = roleTable.indexOf(anyAuthRole);
                if (index != -1) {
                    anyAuthRole = roleTable.get(index);
                    anyAuthRole.determineAnyAuthenticatedUserRole();
                }
            }
        } finally {
            pcwLock.unlock();
        }
    }


    // ### Public Policy Enforcement Interfaces Start here ###


    /**
     * Evaluates the global policy and returns a PermissionCollection object specifying the set of permissions allowed for
     * code from the specified code source.
     *
     * @param codeSource the CodeSource associated with the caller. This encapsulates the original location of the code
     * (where the code came from) and the public key(s) of its signer.
     *
     * @return the set of permissions allowed for code from <i>codesource</i> according to the policy.The returned set of
     * permissions must be a new mutable instance and it must support heterogeneous Permission types.
     *
     */
    public static PermissionCollection getPermissions(PermissionCollection basePerms, CodeSource codesource) throws PolicyContextException {
        SimplePolicyConfiguration policyConfiguration = SharedState.getActiveConfig();
        return policyConfiguration == null ? basePerms : policyConfiguration.getPermissions(basePerms, (PermissionCollection) null, new Principal[0]);
    }

    /**
     * Evaluates the policy and returns a PermissionCollection object specifying the set of permissions allowed given the
     * characteristics of the protection domain.
     *
     * @param domain the ProtectionDomain associated with the caller.
     *
     * @return the set of permissions allowed for the <i>domain</i> according to the policy.The returned set of permissions
     * must be a new mutable instance and it must support heterogeneous Permission types.
     *
     * @see java.security.ProtectionDomain
     * @see java.security.SecureClassLoader
     * @since 1.4
     */
    public static PermissionCollection getPermissions(PermissionCollection basePerms, ProtectionDomain domain) throws PolicyContextException {
        SimplePolicyConfiguration policyConfiguration = SharedState.getActiveConfig();
        return policyConfiguration == null ? basePerms : policyConfiguration.getPermissions(basePerms, domain.getPermissions(), domain.getPrincipals());
    }

    /**
     * Evaluates the policy to determine whether the permissions is granted to the ProtectionDomain.
     *
     * @param domain the ProtectionDomain to test
     * @param permission the Permission object to be tested for implication.
     *
     * @return integer -1 if excluded, 0 if not implied, 1 if implied granted to this ProtectionDomain.
     *
     */
    public static int implies(ProtectionDomain domain, Permission p) throws PolicyContextException {
        SimplePolicyConfiguration policyConfiguration = SharedState.getActiveConfig();
        return policyConfiguration == null ? 0 : policyConfiguration.doImplies(domain, p);
    }



    // ###  Internal Policy Enforcement Interfaces Start here ###


    private boolean permissionIsExcluded(Permission testedPermission) {
        boolean isExcluded = false;

        if (hasExcludedPermissions()) {
            if (!getExcludedPermissions().implies(testedPermission)) {
                /*
                 * this loop ensures that the tested perm does not imply an excluded perm; in which case it must be excluded
                 */
                Enumeration<Permission> e = excludedPermissions.elements();
                while (e.hasMoreElements()) {
                    Permission excludedPerm = (Permission) e.nextElement();
                    if (testedPermission.implies(excludedPerm)) {
                        isExcluded = true;
                        break;
                    }

                }
            } else {
                isExcluded = true;
            }

        }

        return isExcluded;
    }

    /**
     * @param protectionDomain
     * @param permission
     * @return integer -1 if excluded, 0 if not implied, 1 if implied.
     * @throws jakarta.security.jacc.PolicyContextException
     */
    private int doImplies(ProtectionDomain protectionDomain, Permission permission) throws PolicyContextException {
        pcrLock.lock();
        int rvalue = 0;
        try {
            assertStateIsInService();

            if (permissionIsExcluded(permission)) {
                rvalue = -1;

            } else if (getUncheckedPermissions().implies(permission)) {
                rvalue = 1;

            } else if (roleTable != null) {

                Principal principals[] = protectionDomain.getPrincipals();

                if (principals.length == 0) {
                    rvalue = 0;

                } else {

                    for (Role role : roleTable) {
                        if (role.arePrincipalsInRole(principals) && role.implies(permission)) {
                            rvalue = 1;
                            break;
                        }
                        // added as a per role debugging convenience
                        if (rvalue != 1) {
                            rvalue = 0;
                        }
                    }

                    // added as an all role debugging convenience
                    if (rvalue != 1) {
                        rvalue = 0;
                    }
                }
            }
            return rvalue;
        } catch (UnsupportedOperationException uso) {
            throw new PolicyContextException(uso);
        } finally {
            pcrLock.unlock();
        }
    }

    private boolean hasExcludedPermissions() {
        return excludedPermissions == null || !excludedPermissions.elements().hasMoreElements() ? false : true;
    }

    private PermissionCollection getPermissions(PermissionCollection basePerms, PermissionCollection domainPerms, Principal[] principals)
            throws PolicyContextException, UnsupportedOperationException {

        pcrLock.lock();
        try {
            assertStateIsInService();
            Permissions permissions = null;
            boolean hasExcludes = hasExcludedPermissions();

            if (basePerms != null) {
                for (Enumeration<Permission> e = basePerms.elements(); e.hasMoreElements();) {
                    Permission permission = e.nextElement();
                    if (!hasExcludes || !permissionIsExcluded(permission)) {
                        if (permissions == null) {
                            permissions = new Permissions();
                        }

                        permissions.add(permission);
                    }

                }
            }

            if (domainPerms != null) {
                for (Enumeration<Permission> e = domainPerms.elements(); e.hasMoreElements();) {
                    Permission permission = e.nextElement();
                    if (!hasExcludes || !permissionIsExcluded(permission)) {
                        if (permissions == null) {
                            permissions = new Permissions();
                        }

                        permissions.add(permission);
                    }

                }
            }

            for (Enumeration<Permission> e = getUncheckedPermissions().elements(); e.hasMoreElements();) {
                Permission permission = e.nextElement();
                if (!hasExcludes || !permissionIsExcluded(permission)) {
                    if (permissions == null) {
                        permissions = new Permissions();
                    }

                    permissions.add(permission);
                }

            }

            if (principals.length == 0 || roleTable == null) {
                return permissions;
            }

            for (Role role : roleTable) {
                if (role.arePrincipalsInRole(principals)) {
                    PermissionCollection permissionCollection = role.getPermissions();
                    for (Enumeration<Permission> e = permissionCollection.elements(); e.hasMoreElements();) {
                        Permission permission = e.nextElement();
                        if (!hasExcludes || !permissionIsExcluded(permission)) {
                            if (permissions == null) {
                                permissions = new Permissions();
                            }

                            permissions.add(permission);
                        }
                    }
                }
            }

            return permissions;
        } catch (UnsupportedOperationException uso) {
            throw new PolicyContextException(uso);
        } finally {
            pcrLock.unlock();
        }
    }

    /**
     * Refreshes/reloads all of the inservice policy configurations. The behavior of this method depends on the
     * implementation. For example, calling <code>refresh</code> on a file-based policy will cause the file to be re-read.
     */
    static void refresh() throws PolicyContextException {
    }

    static void doPrivilegedLog(final Level level, final String msg, final Object[] params) {
        final Logger logger = SharedState.getLogger();
        if (logger.isLoggable(level)) {
            if (System.getSecurityManager() == null) {
                logger.log(level, msg, params);
            } else {
                AccessController.doPrivileged(new PrivilegedAction() {

                    @Override
                    public Object run() {
                        logger.log(level, msg, params);
                        return null;
                    }
                });
            }
        }
    }

    static void doPrivilegedLog(final Level level, final String msg, final Throwable t) {
        final Logger logger = SharedState.getLogger();
        if (logger.isLoggable(level)) {
            if (System.getSecurityManager() == null) {
                logger.log(level, msg, t);
            } else {
                AccessController.doPrivileged(new PrivilegedAction() {

                    @Override
                    public Object run() {
                        logger.log(level, msg, t);
                        return null;
                    }
                });
            }
        }
    }

    // Internal logging interfaces start here
    static void logGetPermissionsFailure(final Object o, Throwable t) {

        String id = PolicyContext.getContextID();
        doPrivilegedLog(Level.INFO, "getPermissions.failure", new Object[] { id, o });
        doPrivilegedLog(Level.INFO, "getPermissions.failure", t);
    }

    private static boolean permissionShouldBeLogged(Permission p) {
        return
            !(p instanceof WebResourcePermission) &&
            !(p instanceof WebUserDataPermission) &&
            !(p instanceof MBeanPermission) &&
            !(p instanceof WebRoleRefPermission) &&
            !(p instanceof EJBRoleRefPermission);
    }

    static void logAccessFailure(ProtectionDomain d, Permission p) {
        if (permissionShouldBeLogged(p) || SharedState.getLogger().isLoggable(Level.FINE)) {
            String contextId = PolicyContext.getContextID();
            doPrivilegedLog(Level.FINE, "Domain.that.failed", new Object[] { contextId, p, d });
        }
    }

    static void logException(Level l, String msg, Throwable t) {
        String contextId = PolicyContext.getContextID();
        doPrivilegedLog(l, msg, new Object[] { contextId });
        doPrivilegedLog(l, msg, t);
    }
}
