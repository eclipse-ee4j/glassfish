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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.AuthRealmConfig;
import static com.sun.appserv.management.config.AuthRealmConfig.*;
import com.sun.appserv.management.config.SecurityServiceConfig;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 */
public final class AuthRealmConfigTest
        extends ConfigMgrTestBase {
    public AuthRealmConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getConfigConfig().getSecurityServiceConfig());
        }
    }

    private static boolean WARNED_TESTABLE = false;

    private static final String TEMPLATE_PREFIX = "${";

    private synchronized Map<String, AuthRealmConfig>
    getTestableAuthRealms() {
        final Map<String, AuthRealmConfig> m =
                getConfigConfig().getSecurityServiceConfig().getAuthRealmConfigMap();

        final Map<String, AuthRealmConfig> std = new HashMap<String, AuthRealmConfig>();
        final List<String> warnings = new ArrayList<String>();
        for (final String name : m.keySet()) {
            final AuthRealmConfig c = m.get(name);

            if (AuthRealmConfig.DEFAULT_REALM_CLASSNAME.equals(c.getClassname())) {
                try {
                    final String file = c.getPropertyConfigMap().get("file").getValue();
                    if (file == null) {
                        warnings.add("Realm " + name +
                                " does not have a 'file' property (test skipped)");
                    } else if (file.indexOf(TEMPLATE_PREFIX) >= 0) {
                        warnings.add("Realm " + name +
                                " uses a ${...} name, not yet supported (test skipped)");
                    } else {
                        std.put(c.getName(), c);
                    }
                }
                catch (Exception e) {
                }
            }
        }

        if (!WARNED_TESTABLE) {
            WARNED_TESTABLE = true;
            warning(NEWLINE + CollectionUtil.toString(warnings, NEWLINE) + NEWLINE +
                    "Realms which WILL be tested: {" + CollectionUtil.toString(m.keySet()) + "}");
        }

        return std;
    }

    public synchronized void
    testGetters() {
        final Map<String, AuthRealmConfig> arcMap = getTestableAuthRealms();

        for (final AuthRealmConfig ar : arcMap.values()) {
            ar.getName();
            final String classname = ar.getClassname();
            if (classname != null) {
                ar.setClassname(classname);
            }
        }
    }

    private boolean
    userExists(
            final AuthRealmConfig config,
            final String user) {
        warning( "testAddRemoveUpdateUser.userExists(): NO API exists to getUserNames()" );
        //return GSetUtil.newStringSet((String[]) config.getUserNames()).contains(user);
        return false;
    }

    public synchronized void
    testAddRemoveUpdateUser()
            throws Exception {

        warning( "testAddRemoveUpdateUser.AuthRealmConfigTest(): NO API exists to add/remove users" );
        /*
        final Map<String, AuthRealmConfig> arcMap = getTestableAuthRealms();

        final String USER = "test";

        final Set<AuthRealmConfig> failures = new HashSet<AuthRealmConfig>();

        for (final AuthRealmConfig ar : arcMap.values()) {
            //printVerbose( "TESTING: " + ar.getName() );

            try {
                ar.getUserNames();

                if (userExists(ar, USER)) {
                    ar.removeUser(USER);
                }

                ar.addUser(USER, "foo-pwd", null);
                assert (userExists(ar, USER));
                ar.updateUser(USER, "foo-pwd2", null);
                assert (userExists(ar, USER));
                ar.removeUser(USER);
                assert (!userExists(ar, USER));
                //printVerbose( "SUCCESS testing: " + ar.getName() );
            }
            catch (Exception e) {
                trace("");
                trace("");
                trace("FAILURE FOR: " + ar.getName());
                //e.printStackTrace();
                failures.add(ar);
            }
        }

        if (failures.size() != 0) {
            final Set<String> names = Util.getNames(failures);

            warning("testAddRemoveUpdateUser failed on the following realms: " +
                    CollectionUtil.toString(names));
            assert (false);
        }
        */
    }

    public synchronized void
    testGetGroupNames() {
        warning( "testAddRemoveUpdateUser.testGetGroupNames(): NO API exists for getGroupNames()" );
    /*
        final Map<String, AuthRealmConfig> arcMap = getTestableAuthRealms();

        for (final AuthRealmConfig ar : arcMap.values()) {
            ar.getGroupNames();
        }
    */
    }

    public synchronized void
    testGetUserGroupNames() {
        warning( "testAddRemoveUpdateUser.testGetUserGroupNames(): NO API exists for getUserNames(), getUserGroupNames()" );
        /*
        final Map<String, AuthRealmConfig> arcMap = getTestableAuthRealms();

        for (final AuthRealmConfig ar : arcMap.values()) {
            final String[] users = ar.getUserNames();
            for (final String user : users) {
                ar.getUserGroupNames(user);
            }
        }
        */
    }


    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("AuthRealmConfig");
    }

    public static AuthRealmConfig
    ensureDefaultInstance(final SecurityServiceConfig securityServiceConfig) {
        AuthRealmConfig result =
                securityServiceConfig.getAuthRealmConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(securityServiceConfig,
                                    getDefaultInstanceName(),
                                    DEFAULT_REALM_CLASSNAME,
                                    KEY_FILE_PREFIX + "default-instance-test");
        }

        return result;
    }

    public static AuthRealmConfig
    createInstance(
            final SecurityServiceConfig securityServiceConfig,
            final String name,
            final String classname,
            final String keyFile) {
        final Map<String, String> options = new HashMap<String, String>();

        options.put(KEY_FILE_PROPERTY_KEY, keyFile);
        options.put(JAAS_CONTEXT_PROPERTY_KEY, "dummy-jaas-context-value");

        return securityServiceConfig.createAuthRealmConfig(name, classname, options);
    }


    protected Container
    getProgenyContainer() {
        return getConfigConfig().getSecurityServiceConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.AUTH_REALM_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getConfigConfig().getSecurityServiceConfig().removeAuthRealmConfig(name);
    }


    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        if (name.indexOf("Illegal") >= 0) {
            // MBean doesn't detect illegal parameters; anything is allowed
            // for the realm.
            throw new IllegalArgumentException();
        }

        return createInstance(getConfigConfig().getSecurityServiceConfig(),
                              name, DEFAULT_REALM_CLASSNAME, name + "-keyfile");
    }
}


