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

package com.sun.enterprise.security.auth.realm;

import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Contract;

import static com.sun.enterprise.security.SecurityLoggerInfo.realmCreated;
import static com.sun.enterprise.security.auth.realm.RealmsManagerHolder.getNonNullRealmsManager;
import static com.sun.enterprise.security.auth.realm.RealmsManagerHolder.getRealmsManager;
import static java.util.logging.Level.INFO;

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
public abstract class Realm extends AbstractGlassFishRealmState implements GlassFishUserStore, GlassFishUserManagement {

    protected static final Logger _logger = SecurityLoggerInfo.getLogger();


    // Keep a mapping from "default" to default realm (if no such named
    // realm is present) for the sake of all the hardcoded accesses to it.
    // This needs to be removed as part of RI security service cleanup.
    final static String RI_DEFAULT = "default";

    private static RealmStatsProvider realmStatsProvier;


    /**
     * Instantiate a Realm with the given name and properties using the Class name given. This method is used by iAS and not
     * RI.
     *
     * @param name Name of the new realm.
     * @param className Java Class name of the realm to create.
     * @param props Properties containing values of the Property element from server.xml
     * @returns Reference to the new Realm. The Realm class keeps an internal list of all instantiated realms.
     * @throws BadRealmException If the requested realm cannot be instantiated.
     *
     */
    public static synchronized Realm instantiate(String name, String className, Properties props) throws BadRealmException {
        // Register the realm provider
        registerRealmStatsProvier();

        Realm realmClass = _getInstance(name);
        if (realmClass == null) {
            realmClass = doInstantiate(name, className, props);
            getRealmsManager().putIntoLoadedRealms(name, realmClass);
        }

        return realmClass;
    }

    /**
     * Instantiate a Realm with the given name and properties using the Class name given. This method is used by iAS and not
     * RI.
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
        // Register the realm provider
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
     * Returns the realm identified by the name which is passed as a parameter. This function knows about all the realms
     * which exist; it is not possible to store (or create) one which is not accessible through this routine.
     *
     * @param name identifies the realm
     * @return the requested realm
     * @exception NoSuchRealmException if the realm is invalid
     * @exception BadRealmException if realm data structures are bad
     */
    public static synchronized Realm getInstance(String name) throws NoSuchRealmException {
        Realm instance = _getInstance(name);

        if (instance == null) {
            throw new NoSuchRealmException(MessageFormat.format("Realm {0} does not exists.", name));
        }

        return instance;
    }

    /**
     * Returns the realm identified by the name which is passed as a parameter. This function knows about all the realms
     * which exist; it is not possible to store (or create) one which is not accessible through this routine.
     *
     * @param name identifies the realm
     * @return the requested realm
     * @exception NoSuchRealmException if the realm is invalid
     * @exception BadRealmException if realm data structures are bad
     */
    public static synchronized Realm getInstance(String configName, String name) throws NoSuchRealmException {
        Realm instance = _getInstance(configName, name);

        if (instance == null) {
            throw new NoSuchRealmException(MessageFormat.format("Realm {0} does not exists.", name));
        }

        return instance;
    }

    /**
     * Returns the name of the default realm.
     *
     * @return Default realm name.
     *
     */
    public static synchronized String getDefaultRealm() {
        return getNonNullRealmsManager().getDefaultRealmName();
    }

    /**
     * Sets the name of the default realm.
     *
     * @param realmName Name of realm to set as default.
     *
     */
    public static synchronized void setDefaultRealm(String realmName) {
        getNonNullRealmsManager().setDefaultRealmName(realmName);
    }

    /**
     * Checks if the given realm name is loaded/valid.
     *
     * @param String name of the realm to check.
     * @return true if realm present, false otherwise.
     */
    public static boolean isValidRealm(String name) {
        return getNonNullRealmsManager().isValidRealm(name);
    }

    /**
     * Checks if the given realm name is loaded/valid.
     *
     * @param String name of the realm to check.
     * @return true if realm present, false otherwise.
     */
    public static boolean isValidRealm(String configName, String name) {
        return getNonNullRealmsManager().isValidRealm(configName, name);
    }

    /**
     * Returns the names of accessible realms.
     *
     * @return set of realm names
     */
    public static synchronized Enumeration<String> getRealmNames() {
        return getNonNullRealmsManager().getRealmNames();
    }

    /**
     * Remove realm with given name from cache.
     *
     * @param realmName
     * @exception NoSuchRealmException
     */
    public static synchronized void unloadInstance(String realmName) throws NoSuchRealmException {
        // make sure instance exist
        getInstance(realmName);

        getNonNullRealmsManager().removeFromLoadedRealms(realmName);

        _logger.log(INFO, SecurityLoggerInfo.realmDeleted, realmName);
    }

    /**
     * Remove realm with given name from cache.
     *
     * @param realmName
     * @exception NoSuchRealmException
     */
    public static synchronized void unloadInstance(String configName, String realmName) throws NoSuchRealmException {
        getNonNullRealmsManager().removeFromLoadedRealms(configName, realmName);

        _logger.log(INFO, SecurityLoggerInfo.realmDeleted, realmName);
    }

    /**
     * Replace a Realm instance. Can be used by a Realm subclass to replace a previously initialized instance of itself.
     * Future getInstance requests will then obtain the new instance.
     *
     * <P>
     * Minimal error checking is done. The realm being replaced must already exist (instantiate() was previously called),
     * the new instance must be fully initialized properly and it must of course be of the same class as the previous
     * instance.
     *
     * @param realm The new realm instance.
     * @param name The (previously instantiated) name for this realm.
     *
     */
    protected static synchronized void updateInstance(Realm realm, String name) {
        RealmsManager realmsManager = getNonNullRealmsManager();

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
     * Replace a Realm instance. Can be used by a Realm subclass to replace a previously initialized instance of itself.
     * Future getInstance requests will then obtain the new instance.
     *
     * <P>
     * Minimal error checking is done. The realm being replaced must already exist (instantiate() was previously called),
     * the new instance must be fully initialized properly and it must of course be of the same class as the previous
     * instance.
     *
     * @param realm The new realm instance.
     * @param name The (previously instantiated) name for this realm.
     *
     */
    protected static synchronized void updateInstance(String configName, Realm realm, String name) {
        RealmsManager realmsManager = getNonNullRealmsManager();

        Realm oldRealm = realmsManager.getFromLoadedRealms(configName, name);
        if (!oldRealm.getClass().equals(realm.getClass())) {
            // would never happen unless bug in realm subclass
            throw new Error("Incompatible class " + realm.getClass() + " in replacement realm " + name);
        }

        realm.setName(oldRealm.getName());
        realmsManager.putIntoLoadedRealms(configName, name, realm);

        _logger.log(INFO, SecurityLoggerInfo.realmUpdated, new Object[] { realm.getName() });
    }

    public static synchronized void getRealmStatsProvier() {
        if (realmStatsProvier == null) {
            realmStatsProvier = new RealmStatsProvider();
        }
    }


    // ### Private static methods

    /**
     * Instantiates a Realm class of the given type and invokes its init()
     *
     */
    private static synchronized Realm doInstantiate(String name, String className, Properties props) throws BadRealmException {
        ServiceLocator serviceLocator = Globals.getDefaultBaseServiceLocator();

        RealmsManager realmsManager = null;
        try {
            realmsManager = getRealmsManager();

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

                } catch (IllegalArgumentException | ReflectiveOperationException |  SecurityException ex) {
                    realm = (Realm) Class.forName(className)
                                         .getDeclaredConstructor()
                                         .newInstance();
                }
            }

            realm.setName(name);
            realm.init(props);
            if (realmsManager == null) {
                throw new BadRealmException("Unable to locate RealmsManager Service");
            }

            _logger.log(INFO, realmCreated, new Object[] { name, className });

            return realm;

        } catch (NoSuchRealmException | ReflectiveOperationException ex) {
            throw new BadRealmException(ex);
        }
    }

    private static void registerRealmStatsProvier() {
        if (realmStatsProvier == null) {
            getRealmStatsProvier();
            StatsProviderManager.register("security", PluginPoint.SERVER, "security/realm", realmStatsProvier);
        }
    }

    /**
     * This is a private method for getting realm instance. If realm does not exist, then it will not return null rather
     * than throw exception.
     *
     * @param name identifies the realm
     * @return the requested realm
     */
    private static synchronized Realm _getInstance(String name) {
        return getNonNullRealmsManager()._getInstance(name);
    }

    /**
     * This is a private method for getting realm instance. If realm does not exist, then it will not return null rather
     * than throw exception.
     *
     * @param name identifies the realm
     * @return the requested realm
     */
    private static synchronized Realm _getInstance(String configName, String name) {
        return getNonNullRealmsManager()._getInstance(configName, name);
    }

}
