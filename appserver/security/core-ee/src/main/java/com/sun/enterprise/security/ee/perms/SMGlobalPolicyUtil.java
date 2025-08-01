/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.perms;

import com.sun.logging.LogDomains;

import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to load the EE permissions, EE restrictions, and check restrictions for a given permission set
 */
public class SMGlobalPolicyUtil {

    static Logger logger = Logger.getLogger(LogDomains.SECURITY_LOGGER);

    /**
     * Jakarta EE Component type supporting the use of declared permissions
     */
    public enum CommponentType {
        ear, ejb, war, rar, car
    }

    private enum PolicyType {
        /**
         * Configured EE permissions in the domain
         */
        EEGranted,

        /**
         * Configured EE restriction list in the domain
         */
        EERestricted,

        /**
         * Configured domain allowed list
         */
        ServerAllowed
    }

    /**
     * This is the file storing the default permissions granted to each component type
     */
    public static final String EE_GRANT_FILE = "javaee.server.policy";

    /**
     * This is the file storing the restricted permissions for each component type; Any permissions declared in this list
     * can not be used by the application
     */
    public static final String EE_RESTRICTED_FILE = "restrict.server.policy";

    /**
     * This is the file storing the allowed permissions for each component type A permission listed in this file may not be
     * used but the application, but any application declared permission must exist in this list;
     */
    public static final String SERVER_ALLOWED_FILE = "restrict.server.policy";

    /**
     * Code source URL representing Ejb type
     */
    public static final String EJB_TYPE_CODESOURCE = "file:/module/Ejb";
    /**
     * Code source URL representing Web type
     */
    public static final String WEB_TYPE_CODESOURCE = "file:/module/Web";
    /**
     * Code source URL representing Rar type
     */
    public static final String RAR_TYPE_CODESOURCE = "file:/module/Rar";
    /**
     * Code source URL representing App client type
     */
    public static final String CLIENT_TYPE_CODESOURCE = "file:/module/Car";

    /**
     * Code source URL representing Ear type
     */
    public static final String EAR_TYPE_CODESOURCE = "file:/module/Ear";

    public static final String EAR_CLASS_LOADER = "org.glassfish.javaee.full.deployment.EarClassLoader";

    // map recording the 'Jakarta EE component type' to its EE granted permissions
    private static final Map<CommponentType, PermissionCollection> compTypeToEEGarntsMap = new HashMap<>();

    // map recording the 'Jakarta EE component type' to its EE restricted permissions
    private static final Map<CommponentType, PermissionCollection> compTypeToEERestrictedMap = new HashMap<>();

    // map recording the 'Jakarta EE component type' to its allowed permissions
    private static final Map<CommponentType, PermissionCollection> compTypeToServAllowedMap = new HashMap<>();

    private static boolean eeGrantedPolicyInitDone = false;

    private static final AllPermission ALL_PERM = new AllPermission();

    // JDK-8173082: JDK required permissions needed by applications using java.desktop module
    private static final List<String> JDK_REQUIRED_PERMISSIONS = Stream.of("accessClassInPackage.com.sun.beans",
            "accessClassInPackage.com.sun.beans.*", "accessClassInPackage.com.sun.java.swing.plaf.*", "accessClassInPackage.com.apple.*")
            .collect(Collectors.toList());

    // convert a string type to the CommponentType
    public static CommponentType convertComponentType(String type) {

        return Enum.valueOf(CommponentType.class, type);
    }

    /**
     * Get the default granted permissions of a specified component type
     *
     * @param type Jakarta EE component type
     * @return the permission set granted to the specified component
     */
    public static PermissionCollection getEECompGrantededPerms(CommponentType type) {
        initDefPolicy();
        return compTypeToEEGarntsMap.get(type);
    }

    /**
     * Get the default granted permissions of a specified component type
     *
     * @param type Jakarta EE component type such as ejb, war, rar, car, ear
     * @return
     */
    public static PermissionCollection getEECompGrantededPerms(String type) {
        CommponentType compType = convertComponentType(type);
        return getEECompGrantededPerms(compType);
    }

    /**
     * Get the restricted permission set of a specified component type on the server
     *
     * @param type Jakarta EE component type
     * @return the restricted permission set of the specified component type on the server
     */
    public static PermissionCollection getCompRestrictedPerms(CommponentType type) {
        initDefPolicy();
        return compTypeToEERestrictedMap.get(type);
    }

    public static PermissionCollection getCompRestrictedPerms(String type) {
        CommponentType compType = convertComponentType(type);
        return getCompRestrictedPerms(compType);
    }

    private synchronized static void initDefPolicy() {
        System.out.println("Policy no longer supported");
    }

    private static void checkEETypePermsAgainstServerRestiction(CommponentType type) throws SecurityException {

        checkRestriction(compTypeToEEGarntsMap.get(type), compTypeToEERestrictedMap.get(type));
    }

    public static void checkRestriction(CommponentType type, PermissionCollection declaredPC) throws SecurityException {

        checkRestriction(declaredPC, getCompRestrictedPerms(type));
    }

    /**
     * Checks a permissions set against a restriction set
     *
     * @param declaredPC
     * @param restrictedPC
     * @return true for passed
     * @throws SecurityException is thrown if violation detected
     */
    public static void checkRestriction(PermissionCollection declaredPC, PermissionCollection restrictedPC) throws SecurityException {

        if (restrictedPC == null || declaredPC == null) {
            return;
        }

        // check declared does not contain restricted
        checkContains(declaredPC, restrictedPC);

        // check restricted does not contain declared
        checkContains(restrictedPC, declaredPC);

    }

    // check if permissionCollection toBeCheckedPC is contained/implied by containPC
    private static void checkContains(PermissionCollection containPC, PermissionCollection toBeCheckedPC) throws SecurityException {

        if (containPC == null || toBeCheckedPC == null) {
            return;
        }

        Enumeration<Permission> checkEnum = toBeCheckedPC.elements();
        while (checkEnum.hasMoreElements()) {
            Permission p = checkEnum.nextElement();
            if (!JDK_REQUIRED_PERMISSIONS.contains(p.getName()) && containPC.implies(p)) {
                throw new SecurityException("Restricted permission " + p + " is declared or implied in the " + containPC);
            }
        }
    }

    /**
     * Check a permission set against a restriction of a component type
     *
     * @param declaredPC
     * @param type
     * @return
     * @throws SecurityException
     */
    public static void checkRestrictionOfComponentType(PermissionCollection declaredPC, CommponentType type) throws SecurityException {

        if (CommponentType.ear == type) {
            checkRestrictionOfEar(declaredPC);
        }

        PermissionCollection restrictedPC = compTypeToEERestrictedMap.get(type);

        checkRestriction(declaredPC, restrictedPC);
    }

    // for ear type, check evrything
    public static void checkRestrictionOfEar(PermissionCollection declaredPC) throws SecurityException {

        PermissionCollection pc = compTypeToEERestrictedMap.get(CommponentType.ejb);
        if (pc != null) {
            SMGlobalPolicyUtil.checkRestriction(declaredPC, pc);
        }

        pc = compTypeToEERestrictedMap.get(CommponentType.war);
        if (pc != null) {
            SMGlobalPolicyUtil.checkRestriction(declaredPC, pc);
        }

        pc = compTypeToEERestrictedMap.get(CommponentType.rar);
        if (pc != null) {
            SMGlobalPolicyUtil.checkRestriction(declaredPC, pc);
        }

        pc = compTypeToEERestrictedMap.get(CommponentType.car);
        if (pc != null) {
            SMGlobalPolicyUtil.checkRestriction(declaredPC, pc);
        }

    }

}
