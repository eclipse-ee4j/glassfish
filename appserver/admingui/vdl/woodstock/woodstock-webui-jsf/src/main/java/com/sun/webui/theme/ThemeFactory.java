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

package com.sun.webui.theme;

import java.util.Locale;

/**
 * Factory class responsible for setting up the Sun Web Component
 * application's ThemeManager.
 */
public interface ThemeFactory {

    /**
     * JAR manifest resource path.
     */
    String MANIFEST = "META-INF/MANIFEST.MF";

    /**
     * JAR manifest filename property key.
     */
    String FILENAME = "manifest-file";

    /**
     * Component resource path prefix.
     */
    String COMPONENTS_SECTION = "com/sun/webui/jsf/";

    /**
     * Theme resource path prefix.
     */
    String THEME_SECTION = "com/sun/webui/jsf/theme/";

    /**
     * Theme version required flag manifest key.
     */
    String THEME_VERSION_REQUIRED = "X-SJWUIC-Theme-Version-Required";

    /**
     * Theme version manifest key.
     */
    String THEME_VERSION = "X-SJWUIC-Theme-Version";

    /**
     * Theme name manifest key.
     */
    String NAME = "X-SJWUIC-Theme-Name";

    /**
     * Theme prefix manifest key.
     */
    String PREFIX = "X-SJWUIC-Theme-Prefix";

    /**
     * Theme default manifest key.
     */
    String DEFAULT = "X-SJWUIC-Theme-Default";

    /**
     * Theme CSS style-sheets manifest key.
     */
    String STYLESHEETS = "X-SJWUIC-Theme-Stylesheets";

    /**
     * Theme JS files manifest key.
     */
    String JSFILES = "X-SJWUIC-Theme-JavaScript";

    /**
     * Theme class mapper manifest key.
     */
    String CLASSMAPPER = "X-SJWUIC-Theme-ClassMapper";

    /**
     * Theme images manifest key.
     */
    String IMAGES = "X-SJWUIC-Theme-Images";

    /**
     * Theme messages manifest key.
     */
    String MESSAGES = "X-SJWUIC-Theme-Messages";

    /**
     * Theme templates manifest key.
     */
    String TEMPLATES = "X-SJWUIC-Theme-Templates";

    /**
     * Return the default {@code Theme} for {@code locale} within the theme
     * run-time environment of {@code themeContext}.
     *
     * @param locale the theme locale
     * @param themeContext the theme context
     * @return Theme
     */
    Theme getTheme(Locale locale, ThemeContext themeContext);

    /**
     * Return the {@code themeName} {@code Theme} for {@code locale} within the
     * theme run-time environment of {@code themeContext}.
     *
     * @param themeName the theme name
     * @param locale the theme locale
     * @param themeContext the theme context
     * @return Theme
     */
    Theme getTheme(String themeName, Locale locale, ThemeContext themeContext);

    /**
     * Hack - this will go away.
     *
     * @param themeContext theme context
     * @return String
     */
    String getDefaultThemeName(ThemeContext themeContext);
}
