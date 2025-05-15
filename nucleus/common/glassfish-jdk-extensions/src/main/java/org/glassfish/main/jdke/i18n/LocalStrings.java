/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

/**
 * This class makes getting localized strings super-simple.  All methods are static.
 * The reason is that that makes it much simpler to use -- you never need to create an
 * instance and store it.  You simply call one of the 2 methods directly.  However,
 * there is a performance penalty for this convenience.  This class has to figure out
 * what package your calling code is in (every time).  My reasoning is that the emitting
 * of log messages tends to be much less frequent than other normal processing steps.
 * If performance is an issue -- use an instance of LocalStringsImpl.
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
 * <p>Examples:
 * <ul>
 * <li>String s = LocalStrings.get("xyz");
 * <li>String s = LocalStrings.get("xyz", new Date(), 500, "something", 2.00003);
 * <li>String s = LocalStrings.get("xyz", "something", "foo", "whatever");
 * </ul>
 *
 *
 *
 * @author bnevins
 */
public class LocalStrings {

    private LocalStrings() {
    }

    /**
     * Get a String from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @return the String from LocalStrings or the supplied String if it doesn't exist
     */
    public static String get(String indexString) {
        return new LocalStringsImpl().get(indexString);
    }

    /**
     * Get and format a String from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @param objects The arguments to give to MessageFormat
     * @return the String from LocalStrings or the supplied String if it doesn't exist --
     * using the array of supplied Object arguments
     */
    public static String get(String indexString, Object... objects) {
        return new LocalStringsImpl().get(indexString, objects);
    }

    /**
     * Get a String from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @return the String from LocalStrings or the supplied default value if it doesn't exist
     */
    public String getString(String indexString, String defaultValue) {
        return new LocalStringsImpl().get(indexString, defaultValue);
    }

    /**
     * Get an integer from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @return the integer value from LocalStrings or the supplied default if
     * it doesn't exist or is bad.
     */
    public static int getInt(String indexString, int defaultValue) {
        return new LocalStringsImpl().getInt(indexString, defaultValue);
    }

    /**
     * Get a boolean from the caller's package's LocalStrings.properties
     * @param indexString The string index into the localized string file
     * @return the integer value from LocalStrings or the supplied default if
     * it doesn't exist or is bad.
     */
    public boolean getBoolean(String indexString, boolean defaultValue) {
        return new LocalStringsImpl().getBoolean(indexString, defaultValue);
    }
}
