/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Utility methods for localized messages for design time classes. This class
 * expects a resource bundle named {@code Bundle-DT} in the same package as the
 * class passed to its constructor. This class is also useful for design-time
 * behavior in component renderers and so exists in this run-time package.
 */
public class Bundle {

    /**
     * The {@code MessageFormat} instance we will use for messages that require
     * parameter replacement.
     */
    private final MessageFormat format = new MessageFormat("");

    /**
     * The {@code ResourceBundle} containing our messages.
     */
    private final ResourceBundle bundle;

    /**
     * Construct a {@code Bundle} instance for the specified class.
     *
     * @param clazz Class for which to construct a bundle instance
     */
    public Bundle(final Class clazz) {
        String name = clazz.getName();
        int period = name.lastIndexOf('.');
        if (period >= 0) {
            name = name.substring(0, period + 1);
        } else {
            name = "";
        }
        name += "Bundle-DT";
        bundle = ResourceBundle.getBundle(name, format.getLocale(),
                clazz.getClassLoader());
    }

    /**
     * Return the message for the specified key.
     *
     * @param key Message key to look up
     * @return String
     */
    public String message(final String key) {
        return bundle.getString(key);
    }

    /**
     * Return the message for the specified key, after substituting the
     * specified parameters.
     *
     * @param key Message key to look up
     * @param params Replacement parameters
     * @return String
     */
    public String message(final String key, final Object[] params) {
        String pattern = message(key);

        synchronized (format) {
            format.applyPattern(pattern);
            return format.format(params);
        }
    }
}
