/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util;

import com.sun.jsftemplating.resource.ResourceBundleManager;

import jakarta.faces.context.FacesContext;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>
 * This class gets ResourceBundle messages and formats them.
 * </p>
 *
 * @author Ken Paulsen
 */
public class MessageUtil extends Object {

    /**
     * <p>
     * This class should not be instantiated directly.
     * </p>
     */
    private MessageUtil() {
    }

    /**
     * <p>
     * Use this to get an instance of this class.
     * </p>
     */
    public static MessageUtil getInstance() {
        return _instance;
    }

    /**
     * <p>
     * This method returns a formatted String from the requested <code>ResourceBundle</code>.
     * </p>
     *
     * @param baseName The <code>ResourceBundle</code> name.
     * @param key The <code>ResourceBundle</code> key.
     */
    public String getMessage(String baseName, String key) {
        return getMessage(baseName, key, null);
    }

    /**
     * <p>
     * This method returns a formatted String from the requested <code>ResourceBundle</code>.
     * </p>
     *
     * @param baseName The <code>ResourceBundle</code> name.
     * @param key The <code>ResourceBundle</code> key.
     * @param args The substitution values (may be null).
     */
    public String getMessage(String baseName, String key, Object args[]) {
        return getMessage(null, baseName, key, args);
    }

    /**
     * <p>
     * This method returns a formatted String from the requested <code>ResourceBundle</code>.
     * </p>
     *
     * @param locale The desired <code>Locale</code> (may be null).
     * @param baseName The <code>ResourceBundle</code> name.
     * @param key The <code>ResourceBundle</code> key.
     * @param args The substitution values (may be null).
     */
    public String getMessage(Locale locale, String baseName, String key, Object args[]) {
        if (key == null) {
            return null;
        }
        if (baseName == null) {
            throw new RuntimeException("'baseName' is null for key '" + key + "'!");
        }
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (locale == null) {
            locale = Util.getLocale(ctx);
        }

        // Get the ResourceBundle
        ResourceBundle bundle = ResourceBundleManager.getInstance(ctx).getBundle(baseName, locale);
        if (bundle == null) {
            // FIXME: Log a warning
            return key;
        }

        String message = null;
        try {
            message = bundle.getString(key);
        } catch (MissingResourceException ex) {
            // Key not found!
            // FIXME: Log a warning
        }
        if (message == null) {
            // No message found?
            return key;
        }

        return getFormattedMessage(message, args);
    }

    /**
     * Format message using given arguments.
     *
     * @param message The string used as a pattern for inserting arguments.
     * @param args The arguments to be inserted into the string.
     */
    public static String getFormattedMessage(String message, Object args[]) {
        // Sanity Check
        if (message == null || args == null || args.length == 0) {
            return message;
        }

        String result = null;

        MessageFormat mf = new MessageFormat(message);
        result = mf.format(args);

        return result != null ? result : message;
    }

    /**
     * <p>
     * Singleton. This one is OK to share across VMs (no state).
     * </p>
     */
    private static final MessageUtil _instance = new MessageUtil();
}
