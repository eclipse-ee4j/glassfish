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

package org.glassfish.persistence.common;

import java.util.*;
import java.text.MessageFormat;

public class I18NHelper {
    private static final String bundleSuffix = ".Bundle";    // NOI18N
    private static Hashtable    bundles = new Hashtable();
      private static Locale         locale = Locale.getDefault();

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
              ResourceBundle messages = (ResourceBundle)bundles.get(bundleName);

            if (messages == null) //not found as loaded - add
        {
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
        return loadBundle(
                getPackageName(classObject.getName()) + bundleSuffix,
                    classObject.getClassLoader());
    }


    /**
     * Returns message as String
     */
    final public static String getMessage(ResourceBundle messages, String messageKey)
    {
            return messages.getString(messageKey);
      }

      /**
     * Formats message by adding Array of arguments
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, Object msgArgs[])
    {
            for (int i=0; i<msgArgs.length; i++) {
                if (msgArgs[i] == null) msgArgs[i] = ""; // NOI18N
            }
            MessageFormat formatter = new MessageFormat(messages.getString(messageKey));
            return formatter.format(msgArgs);
      }
      /**
     * Formats message by adding a String argument
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, String arg)
    {
            Object []args = {arg};
            return getMessage(messages, messageKey, args);
      }
      /**
     * Formats message by adding two String arguments
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, String arg1,
                       String arg2)
    {
            Object []args = {arg1, arg2};
            return getMessage(messages, messageKey, args);
      }
      /**
     * Formats message by adding three String arguments
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, String arg1,
                       String arg2, String arg3)
    {
            Object []args = {arg1, arg2, arg3};
            return getMessage(messages, messageKey, args);
      }
      /**
     *
     * Formats message by adding an Object as an argument
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, Object arg)
    {
            Object []args = {arg};
            return getMessage(messages, messageKey, args);
      }
      /**
     * Formats message by adding an int as an argument
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, int arg)
    {
            Object []args = {arg};
            return getMessage(messages, messageKey, args);
      }
      /**
     * Formats message by adding a boolean as an argument
     */
    final public static String getMessage(ResourceBundle messages, String messageKey, boolean arg)
    {
            Object []args = {String.valueOf(arg)};
            return getMessage(messages, messageKey, args);
      }

        /**
         * Returns the package portion of the specified class
         * @param className the name of the class from which to extract the
         * package
         * @return package portion of the specified class
         */
        private static String getPackageName (final String className)
        {
                if (className != null)
                {
                        final int index = className.lastIndexOf('.');

                        return ((index != -1) ?
                                className.substring(0, index) : ""); // NOI18N
                }

                return null;
        }

}
