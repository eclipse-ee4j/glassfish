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

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a local string manager. Provides access to i18n messages
 * for classes that need them.
 *
 * <p> One StringManagerBase per resource bundle name can be created and accessed by the
 * getManager method call.
 *
 * <xmp>
 * Example:
 *
 * [LocalStrings.properties]
 * test=At {1,time} on {1,date}, there was {2} on planet {0,number,integer}
 *
 *
 *  StringManagerBase sm  = StringManagerBase.getStringManager("LocalStrings.properties");
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
public class StringManagerBase {

    /** logger used for this class */
    private static final Logger _logger = CULoggerInfo.getLogger();

    /** resource bundle to be used by this manager */
    private volatile ResourceBundle _resourceBundle;

    private final String _resourceBundleName;
    private final ClassLoader _classLoader;

    /** default value used for undefined local string */
    private static final String NO_DEFAULT = "No local string defined";

    /** cache for all the local string managers (per pkg) */
    private static Hashtable managers = new Hashtable();

    /**
     * Initializes the resource bundle.
     *
     * @param    resourceBundleName    name of the resource bundle
     */
    protected StringManagerBase(String resourceBundleName, ClassLoader classLoader) {
        this._resourceBundleName = resourceBundleName;
        this._classLoader = classLoader;
    }

    /**
     * Lazily load {@link ResourceBundle}.
     *
     * <p>
     * {@link ResourceBundle} loading is expensive, and since we don't typically look at strings
     * in start-up, doing this lazily improves overall performance.
     */
    private ResourceBundle getResourceBundle() {
        if(_resourceBundle==null) {
            // worst case we just end up loading this twice. No big deal.
            try {
                _resourceBundle = ResourceBundle.getBundle(_resourceBundleName, Locale.getDefault(), _classLoader);
            } catch (Exception e) {
                _logger.log(Level.SEVERE, CULoggerInfo.exceptionResourceBundle, e);
            }
        }
        return _resourceBundle;
    }

    /**
     * Returns a local string manager for the given resourceBundle name.
     *
     * @param    resourceBundleName    name of the resource bundle
     *
     * @return   a local string manager for the given package name
     */
    public synchronized static StringManagerBase getStringManager(String resourceBundleName, ClassLoader classLoader) {
        StringManagerBase mgr = (StringManagerBase) managers.get(resourceBundleName);
        if (mgr == null) {
            mgr = new StringManagerBase(resourceBundleName, classLoader);
            try {
                managers.put(resourceBundleName, mgr);
            } catch (Exception e) {
                _logger.log(Level.SEVERE, CULoggerInfo.exceptionCachingStringManager, e);
            }
        }
        return mgr;
    }

    /**
     * Returns a localized string.
     *
     * @param    key           the name of the resource to fetch
     *
     * @return   the localized string
     */
    public String getString(String key) {
        return getStringWithDefault(key, NO_DEFAULT);
    }

    /**
     * Returns a localized string. If the key is not found, it will
     * return the default given value.
     *
     * @param    key           the name of the resource to fetch
     * @param    defaultValue  the default return value if not found
     *
     * @return   the localized string
     */
    public String getStringWithDefault(String key, String defaultValue) {

        String value = null;

        try {
            value = getResourceBundle().getString(key);
        } catch (Exception e) {
            _logger.log(Level.FINE, "No local string for: " + key, e);
        }

        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns a local string for the caller and format the arguments
     * accordingly. If the key is not found, it will use the given
     * default format.
     *
     * @param   key            the key to the local format string
     * @param   defaultFormat  the default format if not found in the resources
     * @param   arguments      the set of arguments to provide to the formatter
     *
     * @return  a formatted localized string
     */
    public String getStringWithDefault(String key, String defaultFormat,
            Object arguments[]) {

        MessageFormat f =
            new MessageFormat( getStringWithDefault(key, defaultFormat) );

        for (int i=0; i<arguments.length; i++) {

            if ( arguments[i] == null ) {

                arguments[i] = "null";

            } else if  ( !(arguments[i] instanceof String) &&
                     !(arguments[i] instanceof Number) &&
                     !(arguments[i] instanceof java.util.Date)) {

                arguments[i] = arguments[i].toString();
            }
        }

        String fmtStr;
        try {
            fmtStr =  f.format(arguments);
        } catch (Exception e) {
            _logger.log(Level.WARNING, CULoggerInfo.exceptionWhileFormating, e);

            // returns default format
            fmtStr = defaultFormat;
        }
        return fmtStr;
    }

    /**
     * Returns a local string for the caller and format the arguments
     * accordingly.
     *
     * @param   key     the key to the local format string
     * @param   arg1    the one argument to be provided to the formatter
     *
     * @return  a formatted localized string
     */
    public String getString(String key, Object arg1) {

        return getStringWithDefault(key, NO_DEFAULT, new Object[] {arg1});
    }

    /**
     * Returns a local string for the caller and format the arguments
     * accordingly.
     *
     * @param   key     the key to the local format string
     * @param   arg1    first argument to be provided to the formatter
     * @param   arg2    second argument to be provided to the formatter
     *
     * @return  a formatted localized string
     */
    public String getString(String key, Object arg1, Object arg2) {

        return getStringWithDefault(key, NO_DEFAULT, new Object[] {arg1, arg2});
    }

    /**
     * Returns a local string for the caller and format the arguments
     * accordingly.
     *
     * @param   key     the key to the local format string
     * @param   arg1    first argument to be provided to the formatter
     * @param   arg2    second argument to be provided to the formatter
     * @param   arg3    third argument to be provided to the formatter
     *
     * @return  a formatted localized string
     */
    public String getString(String key, Object arg1, Object arg2,
            Object arg3) {

        return getStringWithDefault(key, NO_DEFAULT,
                                    new Object[] {arg1, arg2, arg3});
    }

    /**
     * Returns a local string for the caller and format the arguments
     * accordingly.
     *
     * @param   key     the key to the local format string
     * @param   arg1    first argument to be provided to the formatter
     * @param   arg2    second argument to be provided to the formatter
     * @param   arg3    third argument to be provided to the formatter
     * @param   arg4    fourth argument to be provided to the formatter
     *
     * @return  a formatted localized string
     */
    public String getString(String key, Object arg1, Object arg2,
            Object arg3, Object arg4) {

        return getStringWithDefault(key, NO_DEFAULT,
                                    new Object[] {arg1, arg2, arg3, arg4});
    }

    /**
     * Returns a local string for the caller and format the arguments
     * accordingly.
     *
     * @param   key     the key to the local format string
     * @param   args    the array of arguments to be provided to the formatter
     *
     * @return  a formatted localized string
     */
    public String getString(String key, Object[] args) {

        return getStringWithDefault(key, NO_DEFAULT, args);
    }
}
