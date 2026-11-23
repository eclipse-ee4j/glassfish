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

package com.sun.webui.theme;

import jakarta.servlet.ServletContext;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The Servlet implementation of {@code ThemeContext}.
 * This implementation uses the Servlet run-time environment to persist an
 * instance of a {@code ThemeContext} as a {@code ServletContext}
 * attribute.
 * <p>
 * A {@code ServletThemeContext} instance is created based on the
 * context-param values, defined as {@code ThemeContext} constants.
 * </p>
 *
 * <ul>
 * <li>{@code ThemeContext.DEFAULT_LOCALE} - defines the
 * default locale for this application.(optional)</li>
 * <li>{@code ThemeContext.DEFAULT_THEME} - define the theme name
 * for the theme that this application may depend on.(optional)</li>
 * <li>{@code ThemeContext.DEFAULT_THEME_VERSION} - define the theme
 * version this application may depend on.(optional)</li>
 * <li>{@code ThemeContext.THEME_RESOURECES} - a space separated list
 * of resources bundles containing theme resources that augment any theme
 * referenced by this application.</li>
 * <li>{@code ThemeContext.THEME_FACTORY_CLASS_NAME} - the name
 * of the class that implements {@code com.sun.webui.theme.ThemeFactory}
 * to instantiate to obtain theme instances for this application.</li>
 * <li>{@code ThemeContext.THEME_SERVLET_CONTEXT} - the value of
 * the url-pattern element of the theme servlet's servlet-mapping, less the
 * terminating "/*".</li>
 * </ul>
 *
 */
public class ServletThemeContext extends ThemeContext {

    /**
     * An object to synchronize with.
     */
    private static final Object LOCK = new Object();

    /**
     * Constructor controlled in {@code getInstance} and for subclasses.In non
     * servlet environments like JSF, which could be servlet or portlet, accept
     * a {@code Map} containing initialization parameters.
     *
     * @param initParamMap init map
     */
    protected ServletThemeContext(final Map initParamMap) {
        super();
        // deprecated
        String value = (String) initParamMap.get(THEME_MESSAGES);
        if (value != null) {
            setMessages(value);
        }
        // deprecated
        value = (String) initParamMap.get(SUPPORTED_LOCALES);
        if (value != null) {
            setSupportedLocales(getLocales(value));
        }
        value = (String) initParamMap.get(DEFAULT_THEME);
        if (value != null) {
            setDefaultTheme(value);
        }
        value = (String) initParamMap.get(DEFAULT_THEME_VERSION);
        if (value != null) {
            setDefaultThemeVersion(value);
        }
        value = (String) initParamMap.get(THEME_RESOURCES);
        if (value != null) {
            setThemeResources(value.trim().split(" "));
        }
        value = (String) initParamMap.get(DEFAULT_LOCALE);
        if (value != null) {
            setDefaultLocale(value);
        }
        value = (String) initParamMap.get(THEME_FACTORY_CLASS_NAME);
        if (value != null) {
            setThemeFactoryClassName(value);
        }
        // This must be the same as the ThemeServlet's context
        //
        value = (String) initParamMap.get(THEME_SERVLET_CONTEXT);
        if (value != null) {
            setThemeServletContext(value);
        }
    }

    /**
     * Constructor controlled in {@code getInstance}.
     * @param context theme servlet
     */
    protected ServletThemeContext(final ServletContext context) {
        super();
        // deprecated
        String value = (String) context.getInitParameter(THEME_MESSAGES);
        if (value != null) {
            setMessages(value);
        }
        // deprecated
        value = (String) context.getInitParameter(SUPPORTED_LOCALES);
        if (value != null) {
            setSupportedLocales(getLocales(value));
        }
        value = (String) context.getInitParameter(DEFAULT_THEME);
        if (value != null) {
            setDefaultTheme(value);
        }
        value = (String) context.getInitParameter(DEFAULT_THEME_VERSION);
        if (value != null) {
            setDefaultThemeVersion(value);
        }
        value = (String) context.getInitParameter(THEME_RESOURCES);
        if (value != null) {
            setThemeResources(value.trim().split(" "));
        }
        value = (String) context.getInitParameter(DEFAULT_LOCALE);
        if (value != null) {
            setDefaultLocale(value);
        }
        value = (String) context.getInitParameter(THEME_FACTORY_CLASS_NAME);
        if (value != null) {
            setThemeFactoryClassName(value);
        }
        // Not sure why this is needed.
        //
        value = (String) context.getInitParameter(THEME_SERVLET_CONTEXT);
        if (value != null) {
            setThemeServletContext(value);
        }
    }

    /**
     * Return an instance of {@code ThemeContext}.
     * @param context servlet context
     * @return ThemeContext
     */
    public static ThemeContext getInstance(final ServletContext context) {

        // The JSF ApplicationMap is the ServletContext and therefore
        // objects set in the JSF ApplicationMap use "getAttribute" and
        // "setAttribute" on ServletContext.
        //
        // Should there be a JSFThemeServlet to complement the
        // JSFThemeContext's use of "ApplicationMap" ?
        ThemeContext themeContext;
        synchronized (LOCK) {
            // Need to make sure another thread didn't just finish
            themeContext = (ThemeContext) context.getAttribute(THEME_CONTEXT);
            if (themeContext == null) {
                themeContext = new ServletThemeContext(context);
                context.setAttribute(THEME_CONTEXT, themeContext);
            }
        }
        return themeContext;
    }

    /**
     * Determines the set of supported locales.
     * @param locales locale names separated by commas
     * @return set containing the support locales.
     */
    private static Set getLocales(final String locales) {

        String[] localeArray = locales.split(LOCALE_SEPARATOR);
        Set<Locale> localeSet = new HashSet<Locale>();

        for (int counter = 0; counter < localeArray.length; ++counter) {

            String localeString = localeArray[counter].trim();
            if (localeString.length() == 0) {
                continue;
            }

            Locale locale = null;

            // The basename cannot have underscore in it.
            String[] strings = localeString.split("_");
            if (strings.length > 2) {
                locale = new Locale(strings[0], strings[1], strings[2]);
            } else if (strings.length > 1) {
                locale = new Locale(strings[0], strings[1]);
            } else if (strings.length > 0) {
                locale = new Locale(strings[0]);
            }
            localeSet.add(locale);
        }
        return localeSet;
    }
}
