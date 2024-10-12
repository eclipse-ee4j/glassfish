/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jdke.i18n;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.Control.FORMAT_PROPERTIES;

/**
 * This class makes getting localized strings super-simple.  This is the companion
 * class to Strings.  Use this class when performance may be an issue.  I.e. Strings
 * is all-static and creates a ResourceBundle on every call.  This class is instantiated
 * once and can be used over and over from the same package.
 * <p>Specifics:
 * <ul>
 *    <li>Your calling code should have a file named LocalStrings.properties in its
 * package directory.
 *    <li>If your localized string has no arguments call get(String) to get the localized
 *    String value.
 *    <li>If you have a parameterized string, call get(String, Object...)
 * </ul>
 * <p>Note: <b>You can not get an Exception out of calling this code!</b>  If the String
 * or the properties file does not exist, it will return the String that you gave
 * in the first place as the argument.
 * <p>Example:
 * <ul>
 * <li> LocalStringsImpl sh = new LocalStringsImpl();
 * <li>String s = sh.get("xyz");
 * <li>String s = sh.get("xyz", new Date(), 500, "something", 2.00003);
 * <li>String s = sh.get("xyz", "something", "foo", "whatever");
 * </ul>
 *
 * @author bnevins 2005
 */
public class LocalStringsImpl {

    private static final boolean LOG_ERRORS = Boolean.parseBoolean(System.getenv("AS_LOG_I18N_ERRORS"));
    private static final String LOG_TARGET_FILE = System.getenv("AS_LOG_I18N_LOG_FILE");
    private static final PrintStream LOG_TARGET;
    private static final String thisPackage = "com.elf.util";
    private static final ResourceBundle.Control rbcontrol = ResourceBundle.Control.getControl(FORMAT_PROPERTIES);
    static {
        if (LOG_ERRORS) {
            if (LOG_TARGET_FILE == null) {
                LOG_TARGET = System.err;
            } else {
                try {
                    LOG_TARGET = new PrintStream(LOG_TARGET_FILE);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> LOG_TARGET.close()));
                } catch (FileNotFoundException e) {
                    throw new Error(e);
                }
            }
        } else {
            LOG_TARGET = null;
        }
    }

    private final ResourceBundle bundle;

    /**
     * Create a LocalStringsImpl instance.
     * use the proffered class object to find LocalStrings.properties.
     * This is the constructor to use if you are concerned about getting
     * the fastest performance.
     */
    public LocalStringsImpl(Class clazz) {
        bundle = load(clazz);
    }


    private static ResourceBundle load(Class clazz) {
        try {
            String className = clazz.getName();
            String props = className.substring(0, className.lastIndexOf('.')) + "." + "LocalStrings";
            return ResourceBundle.getBundle(props, Locale.getDefault(), clazz.getModule());
        } catch (Exception e) {
            if (LOG_ERRORS) {
                e.printStackTrace(LOG_TARGET);
            }
            return null;
        }
    }


    /**
     * Get a String from the caller's package's LocalStrings.properties
     *
     * @param indexString The string index into the localized string file
     * @return the String from LocalStrings or the supplied String if it doesn't exist
     */
    public String get(String indexString) {
        try {
            return getBundle().getString(indexString);
        } catch (Exception e) {
            if (LOG_ERRORS) {
                e.printStackTrace(LOG_TARGET);
            }
            // it is not an error to have no key...
            return indexString;
        }
    }


    /**
     * Get and format a String from the caller's package's LocalStrings.properties
     *
     * @param indexString The string index into the localized string file
     * @param objects The arguments to give to MessageFormat
     * @return the String from LocalStrings or the supplied String if it doesn't exist --
     *         using the array of supplied Object arguments
     */
    public String get(String indexString, Object... objects) {
        indexString = get(indexString);
        try {
            MessageFormat mf = new MessageFormat(indexString);
            return mf.format(objects);
        } catch (Exception e) {
            if (LOG_ERRORS) {
                e.printStackTrace(LOG_TARGET);
            }
            return indexString;
        }
    }


    /**
     * Get a String from the caller's package's LocalStrings.properties
     *
     * @param indexString The string index into the localized string file
     * @return the String from LocalStrings or the supplied default value if it doesn't exist
     */
    public String getString(String indexString, String defaultValue) {
        try {
            return getBundle().getString(indexString);
        } catch (Exception e) {
            if (LOG_ERRORS) {
                e.printStackTrace(LOG_TARGET);
            }
            // it is not an error to have no key...
            return defaultValue;
        }
    }


    /**
     * Get an integer from the caller's package's LocalStrings.properties
     *
     * @param indexString The string index into the localized string file
     * @return the integer value from LocalStrings or the supplied default if
     *         it doesn't exist or is bad.
     */
    public int getInt(String indexString, int defaultValue) {
        try {
            String s = getBundle().getString(indexString);
            return Integer.parseInt(s);
        } catch (Exception e) {
            if (LOG_ERRORS) {
                e.printStackTrace(LOG_TARGET);
            }
            // it is not an error to have no key...
            return defaultValue;
        }
    }


    /**
     * Get a boolean from the caller's package's LocalStrings.properties
     *
     * @param indexString The string index into the localized string file
     * @return the integer value from LocalStrings or the supplied default if
     *         it doesn't exist or is bad.
     */
    public boolean getBoolean(String indexString, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(getBundle().getString(indexString));
        } catch (Exception e) {
            if (LOG_ERRORS) {
                e.printStackTrace(LOG_TARGET);
            }
            // it is not an error to have no key...
            return defaultValue;
        }
    }


    public ResourceBundle getBundle() {
        return bundle;
    }
}
