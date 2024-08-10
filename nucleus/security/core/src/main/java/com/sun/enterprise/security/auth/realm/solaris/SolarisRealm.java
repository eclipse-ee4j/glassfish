/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.auth.realm.solaris;

import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

/**
 * Realm wrapper for supporting Solaris authentication.
 *
 * <P>
 * The Solaris realm needs the following properties in its configuration:
 * <ul>
 * <li>jaas-ctx - JAAS context name used to access LoginModule for authentication.
 * </ul>
 *
 * @see com.sun.enterprise.security.auth.login.SolarisLoginModule
 *
 */
@Service
public final class SolarisRealm extends Realm {

    // Descriptive string of the authentication type of this realm.
    public static final String AUTH_TYPE = "solaris";
    public static final String OS_ARCH = "os.arch";
    public static final String SOL_SPARC_OS_ARCH = "sparc";
    public static final String SOL_X86_OS_ARCH = "x86";

    private HashMap groupCache;
    private Vector emptyVector;
    private static String osArchType;

    // Library for native methods
    static {
        osArchType = System.getProperty(OS_ARCH);
        if (SOL_SPARC_OS_ARCH.equals(osArchType)) {
            System.loadLibrary("solsparcauth");
        } else if (SOL_X86_OS_ARCH.equals(osArchType)) {
            System.loadLibrary("solx86auth");
        }
    }

    /**
     * Initialize a realm with some properties. This can be used when instantiating realms from their descriptions. This method may
     * only be called a single time.
     *
     * @param props Initialization parameters used by this realm.
     * @exception BadRealmException If the configuration parameters identify a corrupt realm.
     * @exception NoSuchRealmException If the configuration parameters specify a realm which doesn't exist.
     *
     */
    @Override
    public synchronized void init(Properties props) throws BadRealmException, NoSuchRealmException {
        super.init(props);
        String jaasCtx = props.getProperty(JAAS_CONTEXT_PARAM);
        if (jaasCtx == null) {
            if (_logger.isLoggable(WARNING)) {
                _logger.warning("realmconfig.noctx");
            }
            throw new BadRealmException("Solaris realm jaas-context not set.");
        }

        setProperty(JAAS_CONTEXT_PARAM, jaasCtx);

        if (_logger.isLoggable(FINE)) {
            _logger.fine("SolarisRealm : " + JAAS_CONTEXT_PARAM + "=" + jaasCtx);
        }

        groupCache = new HashMap();
        emptyVector = new Vector();
    }

    /**
     * Returns a short (preferably less than fifteen characters) description of the kind of authentication which is supported by this
     * realm.
     *
     * @return Description of the kind of authentication that is directly supported by this realm.
     */
    @Override
    public String getAuthType() {
        return AUTH_TYPE;
    }

    /**
     * Returns the name of all the groups that this user belongs to. This is called from web path role verification, though it should
     * not be.
     *
     * @param username Name of the user in this realm whose group listing is needed.
     * @return Enumeration of group names (strings).
     * @exception InvalidOperationException thrown if the realm does not support this operation - e.g. Certificate realm does not
     * support this operation.
     */
    @Override
    public Enumeration getGroupNames(String username) throws InvalidOperationException, NoSuchUserException {
        Vector v = (Vector) groupCache.get(username);
        if (v == null) {
            v = loadGroupNames(username);
        }

        return v.elements();
    }

    /**
     * Set group membership info for a user.
     *
     * <P>
     * See bugs 4646133,4646270 on why this is here.
     *
     */
    private void setGroupNames(String username, String[] groups) {
        Vector v = null;

        if (groups == null) {
            v = emptyVector;

        } else {
            v = new Vector(groups.length + 1);
            for (int i = 0; i < groups.length; i++) {
                v.add(groups[i]);
            }
        }

        synchronized (this) {
            groupCache.put(username, v);
        }
    }

    /**
     * Invoke the native authentication call.
     *
     * @param username User to authenticate.
     * @param password Given password.
     * @returns true of false, indicating authentication status.
     *
     */
    public String[] authenticate(String username, char[] password) {
        String[] grps = nativeAuthenticate(username, new String(password));
        if (grps != null) {
            grps = addAssignGroups(grps);
        }
        setGroupNames(username, grps);
        return grps;
    }

    /**
     * Loads groups names for the given user by calling native method.
     *
     * <P>
     * Group info is loaded when user authenticates, however in some cases (such as run-as) the group membership info is needed
     * without an authentication event.
     *
     */
    private Vector loadGroupNames(String username) {
        String[] grps = nativeGetGroups(username);
        if (grps == null) {
            _logger.fine("No groups returned for user: " + username);
        }

        grps = addAssignGroups(grps);
        setGroupNames(username, grps);
        return (Vector) groupCache.get(username);
    }

    /**
     * Native method. Authenticate using PAM.
     *
     */
    private static native String[] nativeAuthenticate(String user, String password);

    /**
     * Native method. Retrieve Solaris groups for user.
     *
     */
    private static native String[] nativeGetGroups(String user);

}
