/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.theme.JSFThemeContext;
import com.sun.webui.theme.Theme;
import com.sun.webui.theme.ThemeContext;
import com.sun.webui.theme.ThemeFactory;
import com.sun.webui.theme.ThemeImage;

import jakarta.faces.context.FacesContext;

import java.util.Locale;
import java.util.Map;

/**
 * Utilities needed by Sun Web Components to retrieve an appropriate Theme.
 */
public final class ThemeUtilities {

    /**
     * Cannot be instanciated.
     */
    private ThemeUtilities() {
    }

    /**
     * Defines the attribute in the {@code RequestMap} for caching the
     * theme.
     */
    private static final String JSFTHEME = "com.sun.webui.jsf.theme.THEME";

    /**
     * Return the default {@code Theme} instance.{@code getTheme} first looks in
     * the request map for an instance.If an instance does not exist in the
     * request map, obtain an instance from the {@code ThemeFactory}.If the
     * theme is obtained from the {@code ThemeFactory} place it in the request
     * map.
     *
     * @param context faces context
     * @return Theme
     */
    public static Theme getTheme(final FacesContext context) {

        // optimization
        // Assumptions include that the locale is not changing
        // within a request for the default theme.
        // It is also assumed that there is a single thread of
        // execution during a request especially code that
        // might be calling "getTheme".
        Theme theme = (Theme) context
                .getExternalContext()
                .getRequestMap()
                .get(JSFTHEME);
        if (theme != null) {
            return theme;
        }
        Locale locale = context.getViewRoot().getLocale();

        String themeName = null;
        Map<String, Object> sessionAttributes
                = context.getExternalContext().getSessionMap();
        Object themeObject = sessionAttributes.get(Theme.THEME_ATTR);
        if (themeObject != null) {
            themeName = themeObject.toString().trim();
        }

        ThemeContext themeContext = JSFThemeContext.getInstance(context);

        // We must ensure that a theme instance is always returned.
        ThemeFactory themeFactory = themeContext.getThemeFactory();
        theme = themeFactory.getTheme(themeName, locale, themeContext);

        // Now see if this call to getTheme, set a default theme
        // as a result of not theme set in ThemeContext and the
        // decision was left to ThemeFactory.
        // if THEME_ATTR was null
        if (themeName == null) {
            // Hack - this will go away.
            themeName = themeFactory.getDefaultThemeName(themeContext);
            if (themeName != null) {
                sessionAttributes.put(Theme.THEME_ATTR, themeName);
            }
        }

        context.getExternalContext().getRequestMap().put(JSFTHEME, theme);
        return theme;
    }

    /**
     * Return an {@code Icon} component for the {@code iconKey}.
     * @param theme theme to use
     * @param iconKey icon key
     * @return Icon
     */
    public static Icon getIcon(final Theme theme, final String iconKey) {
        Icon icon = new Icon();
        icon.setIcon(iconKey);
        if (iconKey == null) {
            return icon;
        }

        ThemeImage themeImage;
        themeImage = theme.getImage(iconKey);
        icon.setUrl(themeImage.getPath());
        icon.setAlt(themeImage.getAlt());
        icon.setHeight(themeImage.getHeight());
        icon.setWidth(themeImage.getWidth());
        return icon;
    }
}
