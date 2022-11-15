/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.persistence.common;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18NHelper {

    private static final String bundleSuffix = ".Bundle"; // NOI18N
    private static Hashtable<String, ResourceBundle> bundles = new Hashtable<>();
    private static Locale locale = Locale.getDefault();

    /**
     * Constructor
     */
    public I18NHelper() {
    }


    /**
     * Load ResourceBundle by bundle name
     */
    public static ResourceBundle loadBundle(String bundleName) {
        return loadBundle(bundleName, I18NHelper.class.getClassLoader());
    }


    /**
     * Load ResourceBundle by bundle name and class loader
     */
    public static ResourceBundle loadBundle(String bundleName, ClassLoader loader) {
        ResourceBundle messages = bundles.get(bundleName);
        if (messages == null) {
            // not found as loaded - add
            messages = ResourceBundle.getBundle(bundleName, locale, loader);
            bundles.put(bundleName, messages);
        }
        return messages;
    }


    /**
     * Load ResourceBundle by class object - figure out the bundle name
     * for the class object's package and use the class' class loader.
     */
    public static ResourceBundle loadBundle(Class classObject) {
        return loadBundle(getPackageName(classObject.getName()) + bundleSuffix, classObject.getClassLoader());
    }


    /**
     * Returns message as String
     */
    public static final String getMessage(ResourceBundle messages, String messageKey) {
        return messages.getString(messageKey);
    }


    /**
     * Formats message by adding Array of arguments
     */
    public static final String getMessage(ResourceBundle messages, String messageKey, Object... msgArgs) {
        MessageFormat formatter = new MessageFormat(messages.getString(messageKey));
        return formatter.format(msgArgs);
    }


    /**
     * Returns the package portion of the specified class
     *
     * @param className the name of the class from which to extract the
     *            package
     * @return package portion of the specified class
     */
    private static String getPackageName(final String className) {
        if (className != null) {
            final int index = className.lastIndexOf('.');

            return ((index != -1) ? className.substring(0, index) : ""); // NOI18N
        }

        return null;
    }

}
