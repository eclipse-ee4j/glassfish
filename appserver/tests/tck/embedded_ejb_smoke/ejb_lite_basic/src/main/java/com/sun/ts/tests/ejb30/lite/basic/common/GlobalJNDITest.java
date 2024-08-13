/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */

package com.sun.ts.tests.ejb30.lite.basic.common;

import com.sun.ts.tests.ejb30.common.helper.Helper;

import java.util.logging.Level;

public class GlobalJNDITest {

    private GlobalJNDITest() {
    }

    // no appName or interfaceName in global jndi name. Only moduleName and
    // beanName
    public static String getGlobalJNDIName(String moduleName, String beanName) {
        return getGlobalJNDIName((String) null, moduleName, beanName, (Class<?>) null);
    }

    // no appName in global jndi name. Only moduleName, beanName and
    // interfaceClass
    public static String getGlobalJNDIName(String moduleName, String beanName, Class<?> interfaceClass) {
        return getGlobalJNDIName((String) null, moduleName, beanName, interfaceClass);
    }

    // no interface name in global jndi name
    public static String getGlobalJNDIName(String appName, String moduleName, String beanName) {
        return getGlobalJNDIName(appName, moduleName, beanName, (Class<?>) null);
    }

    public static String getGlobalJNDIName(String appName, String moduleName, String beanName, Class<?> interfaceClass) {
        Helper.getLogger().logp(Level.FINE, "GlobalJNDITest", "getGlobalJNDIName",
            "appName=" + appName + ", moduleName=" + moduleName + ", beanName=" + beanName + ", interfaceClass=" + interfaceClass);
        if (moduleName == null || moduleName.length() == 0) {
            throw new IllegalArgumentException("invalid moduleName:" + moduleName);
        }
        if (beanName == null || beanName.length() == 0) {
            throw new IllegalArgumentException("invalid beanName:" + beanName);
        }

        String result = "java:global";
        if (appName != null && appName.length() > 0) {
            result += "/" + appName;
        }

        result += "/" + moduleName + "/" + beanName;
        if (interfaceClass != null) {
            result += "!" + interfaceClass.getName();
        }

        return result;
    }

    public static String getAppJNDIName(String moduleName, String beanName) {
        return getAppJNDIName(moduleName, beanName, (Class<?>) null);
    }

    /**
     * For intra-app use.
     *
     * If the app is packaged in EAR, then call this method with (moduleName, beanName, and optional interfaceClass).
     *
     * If the app is packaged in WAR or JAR, then call this method with (moduleName, beanName, and optional interfaceClass). Note
     * that module-name is still required for standalone modules.
     */
    public static String getAppJNDIName(String moduleName, String beanName, Class<?> interfaceClass) {
        String result = "java:app";
        if (moduleName != null && moduleName.length() > 0) {
            result += "/" + moduleName;
        }
        if (beanName == null || beanName.length() == 0) {
            throw new RuntimeException("Invalid beanName " + beanName);
        }

        result += "/" + beanName;
        if (interfaceClass != null) {
            result += "!" + interfaceClass.getName();
        }

        return result;
    }

    public static String getModuleJNDIName(String beanName) {
        return getModuleJNDIName(beanName, (Class<?>) null);
    }

    /**
     * For intra-module use
     *
     * Call this method with (beanName, and optional interfaceClass)
     */
    public static String getModuleJNDIName(String beanName, Class<?> interfaceClass) {
        if (beanName == null || beanName.length() == 0) {
            throw new RuntimeException("Invalid beanName " + beanName);
        }

        String result = "java:module/" + beanName;
        if (interfaceClass != null) {
            result += "!" + interfaceClass.getName();
        }

        return result;
    }
}
