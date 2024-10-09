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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

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
    private ResourceBundle bundle;
    private String propsName = "LocalStrings";
    private static final String thisPackage = "com.elf.util";
    private static final ResourceBundle.Control rbcontrol = ResourceBundle.Control
        .getControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    /**
     * Create a LocalStringsImpl instance.
     * Automatically discover the caller's LocalStrings.properties file
     */
    public LocalStringsImpl() {
        setBundle();
    }

    /**
     * Create a LocalStringsImpl instance.
     * use the proffered class object to find LocalStrings.properties.
     * This is the constructor to use if you are concerned about getting
     * the fastest performance.
     */
    public LocalStringsImpl(Class clazz) {
        setBundle(clazz);
    }

    /**
     * Create a LocalStringsImpl instance.
     * use the proffered String.  The String is the FQN of the properties file,
     * without the '.properties'.  E.g. 'com.elf.something.LogStrings'
     */
    public LocalStringsImpl(String packageName, String propsName) {
        this.propsName = propsName;
        int len = packageName.length();

        // side-effect -- make sure it ends in '.'
        if (packageName.charAt(len - 1) != '.') {
            packageName += '.';
        }

        setBundle(packageName);
    }

    /**
     * Get a String from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @return the String from LocalStrings or the supplied String if it doesn't exist
     */
    public String get(String indexString) {
        try {
            return getBundle().getString(indexString);
        }
        catch (Exception e) {
            // it is not an error to have no key...
            return indexString;
        }
    }

    /**
     * Get and format a String from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @param objects The arguments to give to MessageFormat
     * @return the String from LocalStrings or the supplied String if it doesn't exist --
     * using the array of supplied Object arguments
     */
    public String get(String indexString, Object... objects) {
        indexString = get(indexString);

        try {
            MessageFormat mf = new MessageFormat(indexString);
            return mf.format(objects);
        }
        catch (Exception e) {
            return indexString;
        }
    }

    /**
     * Get a String from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @return the String from LocalStrings or the supplied default value if it doesn't exist
     */
    public String getString(String indexString, String defaultValue) {
        try {
            return getBundle().getString(indexString);
        }
        catch (Exception e) {
            // it is not an error to have no key...
            return defaultValue;
        }
    }

    /**
     * Get an integer from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @return the integer value from LocalStrings or the supplied default if
     * it doesn't exist or is bad.
     */
    public int getInt(String indexString, int defaultValue) {
        try {
            String s = getBundle().getString(indexString);
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            // it is not an error to have no key...
            return defaultValue;
        }
    }

    /**
     * Get a boolean from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @return the integer value from LocalStrings or the supplied default if
     * it doesn't exist or is bad.
     */
    public boolean getBoolean(String indexString, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(getBundle().getString(indexString));
        }
        catch (Exception e) {
            // it is not an error to have no key...
            return defaultValue;
        }
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    ///////////////////////////////////////////////////////////////////////////
    private void setBundle() {
        // go through the stack, top to bottom.  The item that is below the LAST
        // method that is in the util framework is where the logfile is.
        // note that there may be more than one method from util in the stack
        // because they may be calling us indirectly from LoggerHelper.  Also
        // note that this class won't work from any class in the util hierarchy itself.

        try {
            StackTraceElement[] items = Thread.currentThread().getStackTrace();
            int lastMeOnStack = -1;

            for (int i = 0; i < items.length; i++) {
                StackTraceElement item = items[i];
                if (item.getClassName().startsWith(thisPackage)) {
                    lastMeOnStack = i;
                }
            }

            String className = items[lastMeOnStack + 1].getClassName();
            setBundle(className);
        }
        catch (Exception e) {
            bundle = null;
        }
    }

    private void setBundle(Class clazz) {

        try {
            String className = clazz.getName();
            setBundle(className);

            // April 25, 2009 -- if OSGi is in charge then we might not have got the
            // bundle!  Fix: send in the class's Classloader...
            if(bundle == null) {
                String props = className.substring(0, className.lastIndexOf('.')) + "." + propsName;
                bundle = ResourceBundle.getBundle(props, Locale.getDefault(), clazz.getClassLoader(),
                        rbcontrol);
            }
        }
        catch (Exception e) {
            bundle = null;
        }
    }

    private void setBundle(String className) {
        try {
            String props = className.substring(0, className.lastIndexOf('.')) + "." + propsName;
            bundle = ResourceBundle.getBundle(props, rbcontrol);
        }
        catch (Exception e) {
            bundle = null;
        }
    }
}
