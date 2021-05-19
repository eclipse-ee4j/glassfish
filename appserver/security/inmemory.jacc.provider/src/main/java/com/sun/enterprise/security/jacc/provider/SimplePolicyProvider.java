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

import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.logging.Level;

import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;

/**
 *
 * @author monzillo
 */
public class SimplePolicyProvider extends Policy {

    private static final String REUSE = "java.security.Policy.supportsReuse";
    private Policy basePolicy;

    /**
     * ThreadLocal object to keep track of the reentrancy status of each thread. It contains a byte[] object whose single
     * element is either 0 (initial value or no reentrancy), or 1 (current thread is reentrant). When a thread exists the
     * implies method, byte[0] is alwasy reset to 0.
     */
    private static ThreadLocal<Object> reentrancyStatus = new ThreadLocal<>() {
        @Override
        protected synchronized Object initialValue() {
            return new byte[] { 0 };
        }
    };


    /**
     * Create a new instance of SimplePolicyProvider delegates to existing policy provider unless one is not defined, in
     * which case it trys to load default sun provider
     */
    public SimplePolicyProvider() {
        basePolicy = Policy.getPolicy();
        if (basePolicy == null) {
            try {
                basePolicy = Policy.getInstance("JavaPolicy", null);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace(); // Too bad, no policy then
            }
        }
    }

    /**
     * Evaluates the global policy and returns a PermissionCollection object specifying the set of permissions allowed for
     * code from the specified code source.
     *
     * @param codesource the CodeSource associated with the caller. This encapsulates the original location of the code
     * (where the code came from) and the public key(s) of its signer.
     *
     * @return the set of permissions allowed for code from <i>codesource</i> according to the policy.The returned set of
     * permissions must be a new mutable instance and it must support heterogeneous Permission types.
     *
     */
    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        PermissionCollection permissionCollection = basePolicy.getPermissions(codesource);
        try {
            permissionCollection = SimplePolicyConfiguration.getPermissions(permissionCollection, codesource);
        } catch (PolicyContextException pce) {
            SimplePolicyConfiguration.logGetPermissionsFailure(codesource, pce);
        }
        return permissionCollection;
    }

    /**
     * Evaluates the global policy and returns a PermissionCollection object specifying the set of permissions allowed given
     * the characteristics of the protection domain.
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
    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        PermissionCollection permissionCollection = basePolicy.getPermissions(domain);
        try {
            permissionCollection = SimplePolicyConfiguration.getPermissions(permissionCollection, domain);
        } catch (PolicyContextException pce) {
            SimplePolicyConfiguration.logGetPermissionsFailure(domain, pce);
        }
        return permissionCollection;
    }

    /**
     * Evaluates the global policy for the permissions granted to the ProtectionDomain and tests whether the permission is
     * granted.
     *
     * @param domain the ProtectionDomain to test
     * @param permission the Permission object to be tested for implication.
     *
     * @return true if "permission" is a proper subset of a permission granted to this ProtectionDomain.
     *
     * @see java.security.ProtectionDomain
     * @since 1.4
     */
    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {
        byte[] alreadyCalled = (byte[]) reentrancyStatus.get();
        if (alreadyCalled[0] == 1) {
            return true;
        }
        alreadyCalled[0] = 1;
        try {
            return doImplies(domain, permission);
        } finally {
            alreadyCalled[0] = 0;
        }
    }

    private boolean doImplies(ProtectionDomain domain, Permission permission) {
        int result = -1;
        try {
            result = SimplePolicyConfiguration.implies(domain, permission);
            if (result > 0) {
                return true;
            }
        } catch (PolicyContextException pce) {
            // the following block is included as a debugging convenience
            if (result != 0) {
                result = 1;
            }
        }
        boolean rvalue = false;
        if (result == 0) {
            rvalue = basePolicy.implies(domain, permission);
        }
        if (!rvalue) {
            SimplePolicyConfiguration.logAccessFailure(domain, permission);
        }
        return rvalue;
    }

    /**
     * Refreshes/reloads the policy configuration. The behavior of this method depends on the implementation. For example,
     * calling <code>refresh</code> on a file-based policy will cause the file to be re-read.
     *
     */
    @Override
    public void refresh() {
        basePolicy.refresh();
        try {
            // will enable permission caching of container, unless REUSE
            // property is set, and its value is not "true".
            String propValue = System.getProperty(REUSE);
            boolean supportsReuse = (propValue == null ? true : Boolean.valueOf(propValue));
            if (supportsReuse) {
                if (PolicyContext.getHandlerKeys().contains(REUSE)) {
                    PolicyContext.getContext(REUSE);
                }
            }
            SimplePolicyConfiguration.refresh();
        } catch (PolicyContextException pce) {
            SimplePolicyConfiguration.logException(Level.SEVERE, "refresh.failure", pce);
            throw new IllegalStateException(pce);
        }
    }

    /*
     * NB: Excluded perms should be removed from the collections returned by getPermissions. Permissions that imply excluded
     * permissions should also be excluded. There is a potential semantic integrity issue if the exluded perms have been
     * assigned to the protection domain. The calls to getPermissions and implies of SimplePolicyConfiguration remove
     * excluded permissions from the returned results.
     */
}
