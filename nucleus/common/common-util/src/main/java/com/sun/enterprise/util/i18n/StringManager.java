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

package com.sun.enterprise.util.i18n;

import com.sun.enterprise.util.CULoggerInfo;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a local string manager. Provides access to i18n messages
 * for classes that need them.
 *
 * <p> One StringManager per package can be created and accessed by the
 * getManager method call. The ResourceBundle name is constructed from
 * the given package name in the constructor plus the suffix of "LocalStrings".
 * Thie means that localized information will be contained in a
 * LocalStrings.properties file located in the package directory of the
 * classpath.
 *
 * <xmp>
 * Example:
 *
 * [LocalStrings.properties]
 * test=At {1,time} on {1,date}, there was {2} on planet {0,number,integer}
 *
 *
 *  StringManager sm  = StringManager.getManager(this);
 *  .....
 *
 *
 *  try {
 *      ....
 *  } catch (Exception e) {
 *      String localizedMsg = sm.getString("test",
 *          new Integer(7), new java.util.Date(System.currentTimeMillis()),
 *          "a disturbance in the Force");
 *
 *      throw new MyException(localizedMsg, e);
 *  }
 *
 * Localized message:
 *   At 2:27:41 PM on Jul 8, 2002, there was a disturbance in the Force
 *   on planet 7
 *
 * </xmp>
 *
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public class StringManager extends StringManagerBase {

    /** logger used for this class */
    private static final Logger _logger = CULoggerInfo.getLogger();

    /** name of the resource bundle property file name */
    private static final String RES_BUNDLE_NM = ".LocalStrings";


    /** cache for all the local string managers (per pkg) */
    private static Hashtable managers = new Hashtable();

    /**
     * Initializes the resource bundle.
     *
     * @param    packageName    name of the package
     */
    private StringManager(String packageName, ClassLoader classLoader) {
        super(packageName + RES_BUNDLE_NM, classLoader);
    }

    /**
     * Returns a local string manager for the given package name.
     *
     * @param    packageName    name of the package of the src
     *
     * @return   a local string manager for the given package name
     */
    public synchronized static StringManager getManager(String packageName, ClassLoader classLoader) {

        StringManager mgr = (StringManager) managers.get(packageName);

        if (mgr == null) {
            mgr = new StringManager(packageName, classLoader);
            try {
                managers.put(packageName, mgr);
            } catch (Exception e) {
                _logger.log(Level.SEVERE, CULoggerInfo.exceptionCachingStringManager, e);
            }
        }
        return mgr;
    }

    /**
     *
     * Returns a local string manager for the given package name.
     *
     * @param    callerClass    the object making the call
     *
     * @return   a local string manager for the given package name
     */
    public synchronized static StringManager getManager(Class callerClass) {

        try {
            Package pkg = callerClass.getPackage();
            if (pkg != null) {
                String pkgName = pkg.getName();
                return getManager(pkgName, callerClass.getClassLoader());
            } else {
                // class does not belong to any pkg
                String pkgName = callerClass.getName();
                return getManager(pkgName, callerClass.getClassLoader());
            }
        } catch (Exception e) {
            _logger.log(Level.SEVERE, CULoggerInfo.exceptionConstructingStringManager, e);

            // dummy string manager
            return getManager("", callerClass.getClassLoader());
        }
    }
}
