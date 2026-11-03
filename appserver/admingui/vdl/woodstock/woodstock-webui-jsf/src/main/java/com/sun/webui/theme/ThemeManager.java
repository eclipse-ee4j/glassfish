/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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

package com.sun.webui.theme;

import com.sun.webui.jsf.util.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * The ThemeManager manages the locale specific versions of each Theme. The
 * ThemeManager is created by the ThemeConfigurationListener and placed in an
 * application parameter of the same name as the theme itself.
 * <p>
 * Components do not need to interact with the ThemeManager directly - use
 * {@code com.sun.webui.jsf.util.ThemeUtilities} instead.</p>
 * <p>
 * To specify a default theme, set it in a context init parameter in the
 * deployment descriptor. Use the parameter name
 * {@code com.sun.webui.DefaultTheme}.</p>
 *
 */
public final class ThemeManager {

    /**
     * The context attribute name used to place/retrieve the ThemeManager.
     */
    public static final String THEME_MANAGER = "com.sun.webui.ThemeManager";

    /**
     * Default theme.
     */
    private static String defaultTheme = null;

    /**
     * Default locale.
     */
    private Locale defaultLocale = null;

    /**
     * Loaded themes.
     */
    private final HashMap themes;

    /**
     * Default flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Constructor for the ThemeManager.
     */
    ThemeManager() {
        themes = new HashMap();
    }

    /**
     * This method adds a named Theme to the ThemeManager.
     *
     * @param name The name for which this Theme was created
     * @param themeMap The mapObject of themes
     */
    @SuppressWarnings("unchecked")
    void addThemeMap(final String name, final Map themeMap) {
        if (DEBUG) {
            log("\tNow adding theme map for " + name);
        }
        themes.put(name, themeMap);
    }

    /**
     * Retrieve a Theme instance for a Theme and for a specified locale.
     * <p>
     * If no Theme instances can be found for the specified name, the method
     * uses the name of the default theme instead. If no default theme has been
     * specified, any available theme name will be used. If none is found, a
     * ThemeConfigurationException is thrown.</p>
     * <p>
     * If no Theme instance can be found for the locale, the default locale from
     * the {@code faces-config.xml} file is used. If no default theme was
     * specified, any theme instance will be used. If no theme instances are
     * found, a ThemeConfigurationException is thrown.</p>
     *
     * @param name The for which this Theme was created
     * @param locale The locale for which the Theme instance is needed
     * @return The Theme for the locale
     */
    public JarTheme getTheme(final String name, final Locale locale) {

        // "themes" is a map which manages the individual theme maps.
        // First see if there is a value for the name specified in the
        // method parameter. (themes is not null, it is created on
        // startup).
        Object mapObject = themes.get(name);

        // If there is no value, try to find another theme map.
        if (mapObject == null) {

            // Check if there are no themes (we don't want to do perform this
            // test at the start of this method for performance reasons - it is
            // invoked a lot and this fallback mechanism is only used when there
            // is a misconfiguration.
            if (themes.isEmpty()) {
                throw new ThemeConfigurationException("CONFIGURATION ERROR:"
                        + " no theme resources library available");
            }

            //log("WARNING: theme " + name + " has not been initialized.");

            // If a default theme name was specified, try to get the theme map
            // for the default theme.
            if (defaultTheme != null) {
                mapObject = themes.get(defaultTheme);
                if (mapObject != null) {
                    log("Using the default theme " + defaultTheme);
                }
            } else {
                log("WARNING: no default theme name available either,"
                        + " using any theme!");
            }

            if (mapObject == null) {
                mapObject = themes.values().iterator().next();
            }
        }

        Map themeMap = (Map) mapObject;
        Object object = themeMap.get(locale);
        if (object == null) {
            log("No theme instance found for locale "
                    + locale.getDisplayName());

            if (defaultLocale != null) {
                log("Trying to use the default locale "
                        + defaultLocale.getDisplayName());
                object = themeMap.get(defaultLocale);
            }
            if (object == null && !themeMap.isEmpty()) {
                log("Trying to use any theme instance");
                object = themeMap.values().iterator().next();
            }
            if (object == null) {
                throw new ThemeConfigurationException("CONFIGURATION ERROR: "
                        + "no theme resources library available");
            }
        }
        return (JarTheme) object;
    }

    /**
     * Use this method to specify the default theme for the web application.
     *
     * @param name The name of the default Theme
     */
    void setDefaultThemeName(final String name) {
        if (name != null && name.length() > 0) {
            defaultTheme = name;
        }
        checkDefaultThemeName();
    }

    /**
     * Use this method to specify the default locale for the web application.
     *
     * @param newDefaultLocale The defaultLocale
     */
    private void setDefaultLocale(final Locale newDefaultLocale) {
        this.defaultLocale = newDefaultLocale;
    }

    /**
     * Use this method to retrieve the name of the default Theme for the locale.
     *
     * @return The default Theme's name
     */
    public String getDefaultThemeName() {
        return defaultTheme;
    }

    /**
     * String representation of this class.
     *
     * @return The string representation of this class
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder()
                .append("ThemeManager for Sun Web Componenents.")
                .append("\nDefault theme is ")
                .append(defaultTheme)
                .append(".\nAvailable themes: ");
        Iterator iterator = themes.keySet().iterator();
        while (iterator.hasNext()) {
            buffer.append(iterator.next());
            buffer.append(" ");
        }
        buffer.append(".\nAvailable locales: ");
        iterator = ((Map) (themes.get(defaultTheme))).keySet().iterator();
        while (iterator.hasNext()) {
            buffer.append(iterator.next());
            buffer.append(" ");
        }
        return buffer.toString();
    }

    /**
     * Use this method to check if name is a valid theme name.
     */
    private void checkDefaultThemeName() {
        boolean warn = false;
        if (defaultTheme != null) {
            if (themes.containsKey(defaultTheme)) {
                return;
            } else {
                warn = true;
                log("WARNING: default theme name " + defaultTheme
                        + " is invalid");
            }
        }

        Iterator keys = themes.keySet().iterator();
        if (keys.hasNext()) {
            defaultTheme = keys.next().toString();
        }

        if (warn) {
            log("Using defaultTheme " + defaultTheme + "instead");
        }
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(ThemeManager.class.getName() + "::" + msg);
    }
}
