/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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
package com.sun.enterprise.security.auth.realm;

import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Abstract class that stores the common state for all Realms.
 *
 * <p>
 * This basically includes the realm name, a number of properties, and specifically a number of (extra) groups which are always
 * assigned to an authenticated caller.
 *
 */
public abstract class AbstractGlassFishRealmState implements Comparable<Realm>  {

    public static final String JAAS_CONTEXT_PARAM = "jaas-context";
    public static final String PARAM_GROUP_MAPPING = "group-mapping";

    private static final String PARAM_GROUPS = "assign-groups";
    private static final String GROUPS_SEP = ",";
    private static final String DEFAULT_DEF_DIG_ALGO_VAL = "SHA-256";

    private String realmName;
    private Properties contextProperties;
    private List<String> assignGroups;
    private String defaultDigestAlgorithm;

    protected GroupMapper groupMapper;


    /**
     * The default the constructor creates a realm which will later be initialized,
     * either from properties or by deserializing.
     */
    protected AbstractGlassFishRealmState() {
        contextProperties = new Properties();
    }

    /**
     * Initialize a realm with some properties. This can be used when instantiating realms from their
     * descriptions. This method may only be called a single time.
     *
     * @param properties initialization parameters used by this realm.
     * @exception BadRealmException if the configuration parameters identify a corrupt realm
     * @exception NoSuchRealmException if the configuration parameters specify a realm which doesn't
     * exist
     */
    protected void init(Properties properties) throws BadRealmException, NoSuchRealmException {
        String groupList = properties.getProperty(PARAM_GROUPS);

        if (groupList != null && groupList.length() > 0) {
            setProperty(PARAM_GROUPS, groupList);

            assignGroups = new ArrayList<String>();
            for (String group :  groupList.split(GROUPS_SEP)) {
                if (!assignGroups.contains(group)) {
                    assignGroups.add(group);
                }
            }
        }

        String groupMapping = properties.getProperty(PARAM_GROUP_MAPPING);
        if (groupMapping != null) {
            groupMapper = new GroupMapper();
            groupMapper.parse(groupMapping);
        }

        String defaultDigestAlgo = null;
        if (RealmsManagerHolder._getRealmsManager() != null) {
            defaultDigestAlgo = RealmsManagerHolder._getRealmsManager().getDefaultDigestAlgorithm();
        }

        defaultDigestAlgorithm = defaultDigestAlgo == null ? DEFAULT_DEF_DIG_ALGO_VAL : defaultDigestAlgo;
    }

    /**
     * Add assign groups to given array of groups. To be used by getGroupNames.
     *
     * @param groups
     * @return
     */
    protected String[] addAssignGroups(String[] groups) {
        String[] resultGroups = groups;

        if (assignGroups != null && assignGroups.size() > 0) {
            List<String> groupList = new ArrayList<>();
            if (groups != null && groups.length > 0) {
                for (String group : groups) {
                    groupList.add(group);
                }
            }

            for (String assignGroup : assignGroups) {
                if (!groupList.contains(assignGroup)) {
                    groupList.add(assignGroup);
                }
            }
            resultGroups = groupList.toArray(new String[groupList.size()]);
        }

        return resultGroups;
    }

    protected ArrayList<String> getMappedGroupNames(String group) {
        if (groupMapper == null) {
            return null;
        }

        ArrayList<String> result = new ArrayList<String>();
        groupMapper.getMappedGroups(group, result);

        return result;
    }

    /**
     * Refreshes the realm data so that new users/groups are visible.
     *
     * @param configName
     * @exception BadRealmException if realm data structures are bad
     */
    public void refresh(String configName) throws BadRealmException {
        // do nothing
    }

    /**
     * Returns the name of this realm.
     *
     * @return realm name.
     */
    public final String getName() {
        return realmName;
    }

    /**
     * Assigns the name of this realm, and stores it in the cache of realms. Used when initializing a
     * newly created in-memory realm object; if the realm already has a name, there is no effect.
     *
     * @param name name to be assigned to this realm.
     */
    protected final void setName(String name) {
        if (realmName != null) {
            return;
        }

        realmName = name;
    }

    protected String getDefaultDigestAlgorithm() {
        return defaultDigestAlgorithm;
    }

    /**
     * Get a realm property.
     *
     * @param name property name.
     * @return
     * @returns value.
     *
     */
    public synchronized String getProperty(String name) {
        return contextProperties.getProperty(name);
    }

    /**
     * Set a realm property.
     *
     * @param name property name.
     * @param value property value.
     *
     */
    public synchronized void setProperty(String name, String value) {
        contextProperties.setProperty(name, value);
    }

    /**
     * Return properties of the realm.
     *
     * @return
     */
    protected synchronized Properties getProperties() {
        return contextProperties;
    }

    /**
     * Returns a short (preferably less than fifteen characters) description of the kind of
     * authentication which is supported by this realm.
     *
     * @return description of the kind of authentication that is directly supported by this realm.
     */
    public abstract String getAuthType();

    /**
     * Returns name of JAAS context used by this realm.
     *
     * <P>
     * The JAAS context is defined in server.xml auth-realm element associated with this realm.
     *
     * @return String containing JAAS context name.
     *
     */
    public synchronized String getJAASContext() {
        return contextProperties.getProperty(JAAS_CONTEXT_PARAM);
    }

    /**
     * Returns the name of this realm.
     *
     * @return name of realm.
     */
    @Override
    public String toString() {
        return realmName;
    }

    /**
     * Compares a realm to another.
     *
     * <p>
     * The comparison first considers the authentication type, so that realms supporting the same
     * kind of user authentication are grouped together.
     * Then it compares realm realm names. Realms compare "before" other kinds of objects
     * (i.e. there's only a partial order defined, in the case that those other objects compare themselves
     * "before" a realm object).
     */
    @Override
    public int compareTo(Realm otherRealm) {
        String str = otherRealm.getAuthType();
        int temp;

        if ((temp = getAuthType().compareTo(str)) != 0) {
            return temp;
        }

        return getName().compareTo(otherRealm.getName());
    }

}
