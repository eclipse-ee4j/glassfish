/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.ts.tests.ejb30.common.helper.ServiceLocator;

import jakarta.ejb.EJBContext;

import javax.naming.Context;

import static com.sun.ts.tests.ejb30.lite.basic.common.GlobalJNDITest.getAppJNDIName;
import static com.sun.ts.tests.ejb30.lite.basic.common.GlobalJNDITest.getGlobalJNDIName;
import static com.sun.ts.tests.ejb30.lite.basic.common.GlobalJNDITest.getModuleJNDIName;

public class BasicBeanHelper {
    private static int x = -2;

    private static int y = -3;

    private static int expected = x + y;

    private BasicBeanHelper() {
    }

    private static Object lookupWithEJBContext(String name, EJBContext... ec) {
        EJBContext ejbContext = (ec.length > 0) ? ec[0] : null;
        if (ejbContext != null) {
            Helper.getLogger().fine("Use EJBContext for lookup:" + ejbContext);
            return ejbContext.lookup(name);
        }

        return ServiceLocator.lookupNoTry(name);
    }

    private static Object lookupWithEJBContextOrNamingContext(String name, EJBContext ec, Context nc) {
        if (ec != null) {
            Helper.getLogger().fine("Use EJBContext for lookup:" + ec);
            return ec.lookup(name);
        }

        if (nc != null) {
            Helper.getLogger().fine("Use supplied naming Context for lookup:" + nc);
            return ServiceLocator.lookupNoTry(name, nc);
        }

        return ServiceLocator.lookupNoTry(name);
    }

    // When invoked from web components, EJBContext is not passed as param. Fall
    // back to ServiceLocator for lookup.
    // When invoked from ejbembed vehicle, EJBContainer.getContext() is passed in
    // for
    // lookup.

    public static void globalJNDI(String appName, String modName, Class<?> businessInterface, StringBuilder reason, EJBContext ec,
        Context nc) {

        String lookupName = getGlobalJNDIName(appName, modName, "BasicBean");
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b0 = (Basic1IF) lookupWithEJBContextOrNamingContext(lookupName, ec, nc);
        Helper.assertEquals("BasicBean from lookup with global jndi name " + lookupName, expected, b0.add(x, y), reason);
        b0 = null;

        lookupName = getGlobalJNDIName(appName, modName, "BasicBean", businessInterface);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b00 = (Basic1IF) lookupWithEJBContextOrNamingContext(lookupName, ec, nc);
        Helper.assertEquals("BasicBean from lookup with global jndi name " + lookupName, expected, b00.add(x, y), reason);
        b00 = null;

        lookupName = getGlobalJNDIName(appName, modName, "OneInterfaceBasicBean");
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b1 = (Basic1IF) lookupWithEJBContextOrNamingContext(lookupName, ec, nc);
        Helper.assertEquals("OneInterfaceBasicBean from lookup with global jndi name " + lookupName, expected, b1.add(x, y), reason);
        b1 = null;

        lookupName = getGlobalJNDIName(appName, modName, "OneInterfaceBasicBean", Basic1IF.class);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b11 = (Basic1IF) lookupWithEJBContextOrNamingContext(lookupName, ec, nc);
        Helper.assertEquals("OneInterfaceBasicBean from lookup with global jndi name " + lookupName, expected, b11.add(x, y), reason);
        b11 = null;

        lookupName = getGlobalJNDIName(appName, modName, "TwoInterfacesBasicBean", Basic1IF.class);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b21 = (Basic1IF) lookupWithEJBContextOrNamingContext(lookupName, ec, nc);
        Helper.assertEquals("TwoInterfacesBasicBean from lookup with global jndi name " + lookupName, expected, b21.add(x, y), reason);
        b21 = null;

        lookupName = getGlobalJNDIName(appName, modName, "TwoInterfacesBasicBean", Basic2IF.class);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b22 = (Basic1IF) lookupWithEJBContextOrNamingContext(lookupName, ec, nc);
        Helper.assertEquals("TwoInterfacesBasicBean from lookup with global jndi name " + lookupName, expected, b22.add(x, y), reason);
        b22 = null;
    }

    public static void appJNDI(String modName, Class<?> businessInterface, StringBuilder reason, EJBContext... ec) {

        String lookupName = getAppJNDIName(modName, "BasicBean");
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b0 = (Basic1IF) lookupWithEJBContext(lookupName, ec);
        Helper.assertEquals("BasicBean from lookup with app jndi name " + lookupName, expected, b0.add(x, y), reason);
        b0 = null;

        lookupName = getAppJNDIName(modName, "BasicBean", businessInterface);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b00 = (Basic1IF) ServiceLocator.lookupNoTry(lookupName);
        Helper.assertEquals("BasicBean from lookup with app jndi name " + lookupName, expected, b00.add(x, y), reason);
        b00 = null;

        lookupName = getAppJNDIName(modName, "OneInterfaceBasicBean");
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b1 = (Basic1IF) lookupWithEJBContext(lookupName, ec);
        Helper.assertEquals("OneInterfaceBasicBean from lookup with app jndi name " + lookupName, expected, b1.add(x, y), reason);
        b1 = null;

        lookupName = getAppJNDIName(modName, "OneInterfaceBasicBean", Basic1IF.class);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b11 = (Basic1IF) ServiceLocator.lookupNoTry(lookupName);
        Helper.assertEquals("OneInterfaceBasicBean from lookup with app jndi name " + lookupName, expected, b11.add(x, y), reason);
        b11 = null;

        lookupName = getAppJNDIName(modName, "TwoInterfacesBasicBean", Basic1IF.class);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b21 = (Basic1IF) lookupWithEJBContext(lookupName, ec);
        Helper.assertEquals("TwoInterfacesBasicBean from lookup with app jndi name " + lookupName, expected, b21.add(x, y), reason);
        b21 = null;

        lookupName = getAppJNDIName(modName, "TwoInterfacesBasicBean", Basic2IF.class);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b22 = (Basic1IF) ServiceLocator.lookupNoTry(lookupName);
        Helper.assertEquals("TwoInterfacesBasicBean from lookup with app jndi name " + lookupName, expected, b22.add(x, y), reason);
        b22 = null;
    }

    public static void moduleJNDI(Class<?> businessInterface, StringBuilder reason, EJBContext... ec) {

        String lookupName = getModuleJNDIName("BasicBean");
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b0 = (Basic1IF) lookupWithEJBContext(lookupName, ec);
        Helper.assertEquals("BasicBean from lookup with module jndi name " + lookupName, expected, b0.add(x, y), reason);
        b0 = null;

        lookupName = getModuleJNDIName("BasicBean", businessInterface);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b00 = (Basic1IF) lookupWithEJBContext(lookupName, ec);
        Helper.assertEquals("BasicBean from lookup with module jndi name " + lookupName, expected, b00.add(x, y), reason);
        b00 = null;

        lookupName = getModuleJNDIName("OneInterfaceBasicBean");
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b1 = (Basic1IF) lookupWithEJBContext(lookupName, ec);
        Helper.assertEquals("OneInterfaceBasicBean from lookup with module jndi name " + lookupName, expected, b1.add(x, y), reason);
        b1 = null;

        lookupName = getModuleJNDIName("OneInterfaceBasicBean", Basic1IF.class);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b11 = (Basic1IF) ServiceLocator.lookupNoTry(lookupName);
        Helper.assertEquals("OneInterfaceBasicBean from lookup with module jndi name " + lookupName, expected, b11.add(x, y), reason);
        b11 = null;

        lookupName = getModuleJNDIName("TwoInterfacesBasicBean", Basic1IF.class);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b21 = (Basic1IF) ServiceLocator.lookupNoTry(lookupName);
        Helper.assertEquals("TwoInterfacesBasicBean from lookup with module jndi name " + lookupName, expected, b21.add(x, y), reason);
        b21 = null;

        lookupName = getModuleJNDIName("TwoInterfacesBasicBean", Basic2IF.class);
        Helper.getLogger().fine("About to look up " + lookupName);
        Basic1IF b22 = (Basic1IF) ServiceLocator.lookupNoTry(lookupName);
        Helper.assertEquals("TwoInterfacesBasicBean from lookup with module jndi name " + lookupName, expected, b22.add(x, y), reason);
        b22 = null;
    }

}
