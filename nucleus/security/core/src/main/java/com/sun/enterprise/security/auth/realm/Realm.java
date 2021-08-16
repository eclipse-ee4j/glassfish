/*
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

import static com.sun.enterprise.security.SecurityLoggerInfo.realmCreated;
import static com.sun.enterprise.security.SecurityLoggerInfo.realmDeleted;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Contract;

import com.sun.enterprise.security.BaseRealm;
import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.util.IASSecurityException;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * javadoc
 *
 * @see java.security.Principal
 *
 * @author Harish Prabandham
 * @author Harpreet Singh
 * @author Jyri Virkki
 * @author Shing Wai Chan
 *
 */
@Contract
public abstract class Realm implements Comparable {

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Realm.class);

    private String myName;


    // Keep a mapping from "default" to default realm (if no such named
    // realm is present) for the sake of all the hardcoded accesses to it.
    // This needs to be removed as part of RI security service cleanup.
    final static String RI_DEFAULT = "default";

    // All realms have a set of properties from config file, consolidate.
    private Properties ctxProps;

    // for assign-groups
    private static final String PARAM_GROUPS = "assign-groups";
    private static final String GROUPS_SEP = ",";
    private List<String> assignGroups = null;
    public static final String PARAM_GROUP_MAPPING = "group-mapping";
    protected GroupMapper groupMapper = null;
    private static RealmStatsProvider realmStatsProvier = null;
    private static final String DEFAULT_DEF_DIG_ALGO_VAL = "SHA-256";

    private static WeakReference<RealmsManager> realmsManager = new WeakReference<>(null);
    private String defaultDigestAlgorithm = null;

    protected static final Logger _logger = SecurityLoggerInfo.getLogger();


    // ### Static methods


    /**
     * Instantiate a Realm with the given name and properties using the Class name given. This method is used by iAS and not RI.
     *
     * @param name Name of the new realm.
     * @param className Java Class name of the realm to create.
     * @param props Properties containing values of the Property element from server.xml
     * @returns Reference to the new Realm. The Realm class keeps an internal list of all instantiated realms.
     * @throws BadRealmException If the requested realm cannot be instantiated.
     *
     */
    public static synchronized Realm instantiate(String name, String className, Properties props) throws BadRealmException {
        //Register the realm provider
        registerRealmStatsProvier();

        Realm realmClass = _getInstance(name);
        if (realmClass == null) {
            realmClass = doInstantiate(name, className, props);
            getRealmsManager().putIntoLoadedRealms(name, realmClass);
        }

        return realmClass;
    }

    /**
     * Instantiate a Realm with the given name and properties using the Class name given. This method is used by iAS and not RI.
     *
     * @param name Name of the new realm.
     * @param className Java Class name of the realm to create.
     * @param props Properties containing values of the Property element from server.xml
     * @param configName the config to which this realm belongs
     * @returns Reference to the new Realm. The Realm class keeps an internal list of all instantiated realms.
     * @throws BadRealmException If the requested realm cannot be instantiated.
     *
     */
    public static synchronized Realm instantiate(String name, String className, Properties props, String configName) throws BadRealmException {
        //Register the realm provider
        registerRealmStatsProvier();

        Realm realmClass = _getInstance(configName, name);
        if (realmClass == null) {
            realmClass = doInstantiate(name, className, props);
            getRealmsManager().putIntoLoadedRealms(configName, name, realmClass);
        }

        return realmClass;

    }

    /**
     * Convenience method which returns the Realm object representing the current default realm. Equivalent to
     * getInstance(getDefaultRealm()).
     *
     * @return Realm representing default realm.
     * @exception NoSuchRealmException if default realm does not exist
     */
    public static synchronized Realm getDefaultInstance() throws NoSuchRealmException {
        return getInstance(getDefaultRealm());
    }

    /**
     * Returns the realm identified by the name which is passed as a parameter. This function knows about all the realms which exist;
     * it is not possible to store (or create) one which is not accessible through this routine.
     *
     * @param name identifies the realm
     * @return the requested realm
     * @exception NoSuchRealmException if the realm is invalid
     * @exception BadRealmException if realm data structures are bad
     */
    public static synchronized Realm getInstance(String name) throws NoSuchRealmException {
        Realm retval = _getInstance(name);

        if (retval == null) {
            throw new NoSuchRealmException(
                localStrings.getLocalString("realm.no_such_realm", name + " realm does not exist.", new Object[] { name }));
        }

        return retval;
    }

    /**
     * Returns the realm identified by the name which is passed as a parameter. This function knows about all the realms which exist;
     * it is not possible to store (or create) one which is not accessible through this routine.
     *
     * @param name identifies the realm
     * @return the requested realm
     * @exception NoSuchRealmException if the realm is invalid
     * @exception BadRealmException if realm data structures are bad
     */
    public static synchronized Realm getInstance(String configName, String name) throws NoSuchRealmException {
        Realm retval = _getInstance(configName, name);

        if (retval == null) {
            throw new NoSuchRealmException(
                localStrings.getLocalString("realm.no_such_realm", name + " realm does not exist.", new Object[] { name }));
        }

        return retval;
    }

    /**
     * Returns the name of the default realm.
     *
     * @return Default realm name.
     *
     */
    public static synchronized String getDefaultRealm() {
        RealmsManager realmsManager = getRealmsManager();
        if (realmsManager != null) {
            return realmsManager.getDefaultRealmName();
        }

        throw new RuntimeException("Unable to locate RealmsManager Service");
    }

    /**
     * Sets the name of the default realm.
     *
     * @param realmName Name of realm to set as default.
     *
     */
    public static synchronized void setDefaultRealm(String realmName) {
        //defaultRealmName = realmName;
        RealmsManager realmsManager = getRealmsManager();
        if (realmsManager != null) {
            realmsManager.setDefaultRealmName(realmName);
        }

        throw new RuntimeException("Unable to locate RealmsManager Service");
    }

    /**
     * Remove realm with given name from cache.
     *
     * @param realmName
     * @exception NoSuchRealmException
     */
    public static synchronized void unloadInstance(String realmName) throws NoSuchRealmException {
        //make sure instance exist
        getInstance(realmName);

        RealmsManager realmsManager = getRealmsManager();
        if (realmsManager == null) {
            throw new RuntimeException("Unable to locate RealmsManager Service");
        }

        realmsManager.removeFromLoadedRealms(realmName);
        _logger.log(INFO, realmDeleted, realmName);
    }

    /**
     * Remove realm with given name from cache.
     *
     * @param realmName
     * @exception NoSuchRealmException
     */
    public static synchronized void unloadInstance(String configName, String realmName) throws NoSuchRealmException {
        RealmsManager realmsManager = getRealmsManager();
        if (realmsManager == null) {
            throw new RuntimeException("Unable to locate RealmsManager Service");
        }

        realmsManager.removeFromLoadedRealms(configName, realmName);
        _logger.log(INFO, realmDeleted, realmName);
    }

    public static synchronized void getRealmStatsProvier() {
        if (realmStatsProvier == null) {
            realmStatsProvier = new RealmStatsProvider();
        }
    }

    /**
     * Checks if the given realm name is loaded/valid.
     *
     * @param String name of the realm to check.
     * @return true if realm present, false otherwise.
     */
    public static boolean isValidRealm(String name) {
        RealmsManager mgr = getRealmsManager();
        if (mgr != null) {
            return mgr.isValidRealm(name);
        }

        throw new RuntimeException("Unable to locate RealmsManager Service");
    }

    /**
     * Checks if the given realm name is loaded/valid.
     *
     * @param String name of the realm to check.
     * @return true if realm present, false otherwise.
     */
    public static boolean isValidRealm(String configName, String name) {
        RealmsManager mgr = getRealmsManager();
        if (mgr != null) {
            return mgr.isValidRealm(configName, name);
        }

        throw new RuntimeException("Unable to locate RealmsManager Service");
    }

    /**
     * Replace a Realm instance. Can be used by a Realm subclass to replace a previously initialized instance of itself. Future
     * getInstance requests will then obtain the new instance.
     *
     * <P>
     * Minimal error checking is done. The realm being replaced must already exist (instantiate() was previously called), the new
     * instance must be fully initialized properly and it must of course be of the same class as the previous instance.
     *
     * @param realm The new realm instance.
     * @param name The (previously instantiated) name for this realm.
     *
     */
    protected static synchronized void updateInstance(Realm realm, String name) {
        RealmsManager realmsManager = getRealmsManager();
        if (realmsManager == null) {
            throw new RuntimeException("Unable to locate RealmsManager Service");
        }

        Realm oldRealm = realmsManager.getFromLoadedRealms(name);
        if (!oldRealm.getClass().equals(realm.getClass())) {
            // would never happen unless bug in realm subclass
            throw new Error("Incompatible class " + realm.getClass() + " in replacement realm " + name);
        }

        realm.setName(oldRealm.getName());
        realmsManager.putIntoLoadedRealms(name, realm);
        _logger.log(INFO, SecurityLoggerInfo.realmUpdated, new Object[] { realm.getName() });
    }

    /**
     * Replace a Realm instance. Can be used by a Realm subclass to replace a previously initialized instance of itself. Future
     * getInstance requests will then obtain the new instance.
     *
     * <P>
     * Minimal error checking is done. The realm being replaced must already exist (instantiate() was previously called), the new
     * instance must be fully initialized properly and it must of course be of the same class as the previous instance.
     *
     * @param realm The new realm instance.
     * @param name The (previously instantiated) name for this realm.
     *
     */
    protected static synchronized void updateInstance(String configName, Realm realm, String name) {
        RealmsManager realmsManager = getRealmsManager();
        if (realmsManager == null) {
            throw new RuntimeException("Unable to locate RealmsManager Service");
        }

        Realm oldRealm = realmsManager.getFromLoadedRealms(configName, name);
        if (!oldRealm.getClass().equals(realm.getClass())) {
            // would never happen unless bug in realm subclass
            throw new Error("Incompatible class " + realm.getClass() + " in replacement realm " + name);
        }

        realm.setName(oldRealm.getName());
        realmsManager.putIntoLoadedRealms(configName, name, realm);
        _logger.log(INFO, SecurityLoggerInfo.realmUpdated, new Object[] { realm.getName() });
    }


    private static void registerRealmStatsProvier() {
        if (realmStatsProvier == null) {
            getRealmStatsProvier();
            StatsProviderManager.register("security", PluginPoint.SERVER, "security/realm", realmStatsProvier);
        }
    }

    /**
     * Instantiates a Realm class of the given type and invokes its init()
     *
     */
    private static synchronized Realm doInstantiate(String name, String className, Properties props) throws BadRealmException {

        ServiceLocator serviceLocator = Globals.getDefaultHabitat();
        try {
            RealmsManager realmsManager = getRealmsManager();

            // Try a HK2 route first
            Realm realm = serviceLocator.getService(Realm.class, name);
            if (realm == null) {
                try {
                    // TODO: workaround here. Once fixed in V3 we should be able to use
                    // Context ClassLoader instead.
                    realm = (Realm) serviceLocator.getService(ClassLoaderHierarchy.class)
                                                  .getCommonClassLoader()
                                                  .loadClass(className)
                                                  .getDeclaredConstructor()
                                                  .newInstance();
                } catch (ClassNotFoundException ex) {
                    realm = (Realm) Class.forName(className).getDeclaredConstructor().newInstance();
                }
            }

            realm.setName(name);
            realm.init(props);

            if (realmsManager == null) {
                throw new BadRealmException("Unable to locate RealmsManager Service");
            }

            _logger.log(FINER, realmCreated, new Object[] { name, className });
            return realm;

        } catch (NoSuchRealmException | ReflectiveOperationException ex) {
            throw new BadRealmException(ex);
        }
    }

    /**
     * This is a private method for getting realm instance. If realm does not exist, then it will not return null rather than throw
     * exception.
     *
     * @param name identifies the realm
     * @return the requested realm
     */
    private static synchronized Realm _getInstance(String name) {
        RealmsManager mgr = getRealmsManager();
        if (mgr != null) {
            return mgr._getInstance(name);
        } else {
            throw new RuntimeException("Unable to locate RealmsManager Service");
        }
    }

    /**
     * This is a private method for getting realm instance. If realm does not exist, then it will not return null rather than throw
     * exception.
     *
     * @param name identifies the realm
     * @return the requested realm
     */
    private static synchronized Realm _getInstance(String configName, String name) {
        RealmsManager mgr = getRealmsManager();
        if (mgr != null) {
            return mgr._getInstance(configName, name);
        } else {
            throw new RuntimeException("Unable to locate RealmsManager Service");
        }
    }

    /**
     * Returns the names of accessible realms.
     *
     * @return set of realm names
     */
    public static synchronized Enumeration<String> getRealmNames() {
        RealmsManager realmsManager = getRealmsManager();
        if (realmsManager == null) {
            throw new RuntimeException("Unable to locate RealmsManager Service");
        }

        return realmsManager.getRealmNames();
    }

    private static synchronized RealmsManager _getRealmsManager() {
        if (realmsManager.get() == null) {
            if (Globals.getDefaultHabitat() != null) {
                realmsManager = new WeakReference<>(Globals.get(RealmsManager.class));
            } else {
                return null;
            }
        }
        return realmsManager.get();
    }

    private static RealmsManager getRealmsManager() {
        if (realmsManager.get() != null) {
            return realmsManager.get();
        }
        return _getRealmsManager();
    }









    // ### Instance methods


    /**
     * Returns the name of this realm.
     *
     * @return realm name.
     */
    public final String getName() {
        return myName;
    }

    /**
     * Returns the name of this realm.
     *
     * @return name of realm.
     */
    @Override
    public String toString() {
        return myName;
    }

    /**
     * Compares a realm to another. The comparison first considers the authentication type, so that realms supporting the same kind
     * of user authentication are grouped together. Then it compares realm realm names. Realms compare "before" other kinds of
     * objects (i.e. there's only a partial order defined, in the case that those other objects compare themselves "before" a realm
     * object).
     */
    @Override
    public int compareTo(Object realm) {
        if (!(realm instanceof Realm)) {
            return 1;
        }

        Realm r = (Realm) realm;
        String str = r.getAuthType();
        int temp;

        if ((temp = getAuthType().compareTo(str)) != 0) {
            return temp;
        }

        str = r.getName();
        return getName().compareTo(str);
    }

    /**
     * Set a realm property.
     *
     * @param name property name.
     * @param value property value.
     *
     */
    public synchronized void setProperty(String name, String value) {
        ctxProps.setProperty(name, value);
    }

    /**
     * Get a realm property.
     *
     * @param name property name.
     * @returns value.
     *
     */
    public synchronized String getProperty(String name) {
        return ctxProps.getProperty(name);
    }

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
        return ctxProps.getProperty(BaseRealm.JAAS_CONTEXT_PARAM);
    }

    /**
     * The default constructor creates a realm which will later be initialized, either from properties or by deserializing.
     */
    protected Realm() {
        ctxProps = new Properties();
    }

    /**
     * Return properties of the realm.
     */
    protected synchronized Properties getProperties() {
        return ctxProps;
    }

    protected String getDefaultDigestAlgorithm() {
        return defaultDigestAlgorithm;
    }

    /**
     * Assigns the name of this realm, and stores it in the cache of realms. Used when initializing a newly created in-memory realm
     * object; if the realm already has a name, there is no effect.
     *
     * @param name name to be assigned to this realm.
     */
    protected final void setName(String name) {
        if (myName != null) {
            return;
        }
        myName = name;
    }

    /**
     * Initialize a realm with some properties. This can be used when instantiating realms from their descriptions. This method may
     * only be called a single time.
     *
     * @param props initialization parameters used by this realm.
     * @exception BadRealmException if the configuration parameters identify a corrupt realm
     * @exception NoSuchRealmException if the configuration parameters specify a realm which doesn't exist
     */
    protected void init(Properties props) throws BadRealmException, NoSuchRealmException {
        String groupList = props.getProperty(PARAM_GROUPS);
        if (groupList != null && groupList.length() > 0) {
            this.setProperty(PARAM_GROUPS, groupList);
            assignGroups = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(groupList, GROUPS_SEP);
            while (st.hasMoreTokens()) {
                String grp = st.nextToken();
                if (!assignGroups.contains(grp)) {
                    assignGroups.add(grp);
                }
            }
        }
        String groupMapping = props.getProperty(PARAM_GROUP_MAPPING);
        if (groupMapping != null) {
            groupMapper = new GroupMapper();
            groupMapper.parse(groupMapping);
        }
        String defaultDigestAlgo = null;
        if (_getRealmsManager() != null) {
            defaultDigestAlgo = _getRealmsManager().getDefaultDigestAlgorithm();
        }
        this.defaultDigestAlgorithm = (defaultDigestAlgo == null) ? DEFAULT_DEF_DIG_ALGO_VAL : defaultDigestAlgo;
    }

    /**
     * Add assign groups to given Vector of groups. To be used by getGroupNames.
     *
     * @param grps
     */
    protected String[] addAssignGroups(String[] grps) {
        String[] resultGroups = grps;
        if (assignGroups != null && assignGroups.size() > 0) {
            List<String> groupList = new ArrayList<>();
            if (grps != null && grps.length > 0) {
                for (String grp : grps) {
                    groupList.add(grp);
                }
            }

            for (String agrp : assignGroups) {
                if (!groupList.contains(agrp)) {
                    groupList.add(agrp);
                }
            }
            resultGroups = groupList.toArray(new String[groupList.size()]);
        }
        return resultGroups;
    }

    protected ArrayList<String> getMappedGroupNames(String group) {
        if (groupMapper != null) {
            ArrayList<String> result = new ArrayList<>();
            groupMapper.getMappedGroups(group, result);
            return result;
        }
        return null;
    }




    //---[ Abstract methods ]------------------------------------------------

    /**
     * Returns a short (preferably less than fifteen characters) description of the kind of authentication which is supported by this
     * realm.
     *
     * @return description of the kind of authentication that is directly supported by this realm.
     */
    public abstract String getAuthType();

    /**
     * Returns names of all the users in this particular realm.
     *
     * @return enumeration of user names (strings)
     * @exception BadRealmException if realm data structures are bad
     */
    public abstract Enumeration<String> getUserNames() throws BadRealmException;

    /**
     * Returns the information recorded about a particular named user.
     *
     * @param name name of the user whose information is desired
     * @return the user object
     * @exception NoSuchUserException if the user doesn't exist
     * @exception BadRealmException if realm data structures are bad
     */
    public abstract User getUser(String name) throws NoSuchUserException, BadRealmException;

    /**
     * Returns names of all the groups in this particular realm.
     *
     * @return enumeration of group names (strings)
     * @exception BadRealmException if realm data structures are bad
     */
    public abstract Enumeration<String> getGroupNames() throws BadRealmException;

    /**
     * Returns the name of all the groups that this user belongs to
     *
     * @param username name of the user in this realm whose group listing is needed.
     * @return enumeration of group names (strings)
     * @exception InvalidOperationException thrown if the realm does not support this operation - e.g. Certificate realm does not
     * support this operation
     */
    public abstract Enumeration<String> getGroupNames(String username) throws InvalidOperationException, NoSuchUserException;

    /**
     * Refreshes the realm data so that new users/groups are visible.
     *
     * @exception BadRealmException if realm data structures are bad
     */
    public abstract void refresh() throws BadRealmException;

    /**
     * Refreshes the realm data so that new users/groups are visible.
     *
     * @exception BadRealmException if realm data structures are bad
     */
    public void refresh(String configName) throws BadRealmException {
        //do nothing
    }

    /**
     * Adds new user to file realm. User cannot exist already.
     *
     * @param name User name.
     * @param password Cleartext password for the user.
     * @param groupList List of groups to which user belongs.
     * @throws BadRealmException If there are problems adding user.
     *
     */
    public abstract void addUser(String name, char[] password, String[] groupList) throws BadRealmException, IASSecurityException;

    /**
     * Update data for an existing user. User must exist.
     *
     * @param name Current name of the user to update.
     * @param newName New name to give this user. It can be the same as the original name. Otherwise it must be a new user name which
     * does not already exist as a user.
     * @param password Cleartext password for the user. If non-null the user password is changed to this value. If null, the original
     * password is retained.
     * @param groupList List of groups to which user belongs.
     * @throws BadRealmException If there are problems adding user.
     * @throws NoSuchUserException If user does not exist.
     *
     */
    public abstract void updateUser(String name, String newName, char[] password, String[] groups)
        throws NoSuchUserException, BadRealmException, IASSecurityException;

    /**
     * Remove user from file realm. User must exist.
     *
     * @param name User name.
     * @throws NoSuchUserException If user does not exist.
     *
     */
    public abstract void removeUser(String name) throws NoSuchUserException, BadRealmException;

    /**
     * @return true if the realm implementation support User Management (add,remove,update user)
     */
    public abstract boolean supportsUserManagement();

    /**
     * Persist the realm data to permanent storage
     *
     * @throws com.sun.enterprise.security.auth.realm.BadRealmException
     */
    public abstract void persist() throws BadRealmException;
}
