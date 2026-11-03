/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;

import java.beans.Beans;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Factory class responsible for setting up the Sun Web Component
 * application's ThemeManager.
 */
public final class JarThemeFactory implements ThemeFactory {

    /**
     * Warning message for not being able to load any themes.
     */
    private static final String WARNING_LOAD =
            "WARNING: the Sun Web Components could not load any themes.";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Theme manager.
     */
    private ThemeManager themeManager;

    /**
     * Create a new instance.
     */
    public JarThemeFactory() {
    }

    /**
     * Get the theme attributes.
     * @param classLoader class-loader to use
     * @return theme attributes iterator.
     */
    private Iterator getThemeAttributes(final ClassLoader classLoader) {

        if (DEBUG) {
            log("getThemeAttributes()");
        }
        Collection<URL> manifests = getManifests(classLoader);
        if (manifests.isEmpty()) {
            String msg = "No Themes in the classpath!";
            throw new ThemeConfigurationException(msg);
        }

        URL url;
        URLConnection conn;
        InputStream in = null;
        Manifest manifest;
        Attributes themeAttributes;
        ArrayList<Attributes> themeProps = new ArrayList<Attributes>();
        Iterator<URL> it = manifests.iterator();
        while (it.hasNext()) {
            url = it.next();
            try {
                if (DEBUG) {
                    log("\tExamine " + url.toString());
                }
                conn = url.openConnection();
                in = conn.getInputStream();
                manifest = new Manifest(in);
                themeAttributes = manifest.getAttributes(THEME_SECTION);
                if (themeAttributes != null) {
                    if (DEBUG) {
                        log("\tFound a theme section");
                    }
                    themeAttributes.putValue(FILENAME, url.toString());
                    themeProps.add(themeAttributes);
                }
            } catch (IOException ioex) {
                // do nothing
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException t) {
                }
            }
        }
        return themeProps.iterator();
    }

    /**
     * Read an attribute.
     * @param themeAttributes attributes to read from
     * @param propName name of the attribute to get
     * @return String
     */
    private String readAttribute(final Attributes themeAttributes,
            final String propName) {

        String name = themeAttributes.getValue(propName);
        if (name == null || name.length() == 0) {
            String propFile = themeAttributes.getValue(FILENAME);
            StringBuilder msgBuffer = new StringBuilder();
            msgBuffer.append("ThemeConfiguration file ");
            if (propFile != null) {
                msgBuffer.append(propFile);
                msgBuffer.append(" ");
            }
            msgBuffer.append("does not contain required property \"");
            msgBuffer.append(propName);
            msgBuffer.append("\".");
            throw new ThemeConfigurationException(msgBuffer.toString());
        }
        return name;
    }

    /**
     * Throw a version exception.
     * @param name theme name
     * @param version theme version
     * @param requiredThemeVersion required theme version
     */
    private void throwVersionException(final String name, final String version,
            final String requiredThemeVersion) {

        StringBuilder msgBuffer = new StringBuilder();
        msgBuffer.append("\n\nTheme \"");
        msgBuffer.append(name);
        msgBuffer.append("\" is not up to date with the component library.\n");
        msgBuffer.append("Its version is ");
        msgBuffer.append(version);
        msgBuffer.append(". Version ");
        msgBuffer.append(requiredThemeVersion);
        msgBuffer.append(" or higher required.\n");
        throw new ThemeConfigurationException(msgBuffer.toString());
    }

    /**
     * Get application locales.
     * @param application application to get the locales of
     * @return {@code Set<Locale>}
     */
    private static Set<Locale> getLocales(final Application application) {

        if (DEBUG) {
            log("getLocales()");
        }

        HashSet<Locale> localeSet = new HashSet<Locale>();

        Locale locale = application.getDefaultLocale();
        // According to the JSF spec, getDefaultLocale never returns
        // null, but in the Creator simulated environment it does,
        // so we add belt and braces.
        if (locale != null) {
            if (DEBUG) {
                log("\tDefault locale is " + locale.toString());
            }
            localeSet.add(locale);
        } else if (DEBUG) {
            log("\tNo default locale!");
        }

        Iterator<Locale> localeIterator = application.getSupportedLocales();
        // Again, this should not be null, but we need to account for
        // Creator...
        if (localeIterator != null) {
            while (localeIterator.hasNext()) {
                Locale localeObject = localeIterator.next();
                if (DEBUG) {
                    log("\tAdding locale " + localeObject.toString());
                }
                localeSet.add(localeObject);
            }
        } else if (DEBUG) {
            log("\tNo supported locales!");
        }

        // If things went wrong (= we're in Creator), add the default
        // locale now
        if (localeSet.isEmpty()) {
            if (DEBUG) {
                log("\tAdding default locale which is "
                        + Locale.getDefault().toString());
            }
            localeSet.add(Locale.getDefault());
        }
        return localeSet;
    }

    /**
     * Get the required theme version.
     * @param classLoader class-loader to use
     * @return String
     */
    private static String getRequiredThemeVersion(
            final ClassLoader classLoader) {

        if (DEBUG) {
            log("getRequiredThemeVersion()");
        }

        Collection<URL> manifests = getManifests(classLoader);
        if (manifests.isEmpty()) {
            if (DEBUG) {
                log("\tNo manifests in the classpath!");
            }
            return null;
        }

        URL url;
        Manifest manifest;
        InputStream in = null;
        String themeVersion = null;

        Iterator<URL> it = manifests.iterator();
        while (themeVersion == null && it.hasNext()) {
            url = it.next();
            if (!url.toString().contains("webui")) {
                continue;
            }
            if (DEBUG) {
                log("\tNow processing " + url.toString());
            }
            try {
                in = url.openConnection().getInputStream();
                manifest = new Manifest(in);
                Attributes attr = manifest.getAttributes(COMPONENTS_SECTION);
                if (attr != null) {
                    themeVersion = attr.getValue(THEME_VERSION_REQUIRED);
                    if (DEBUG) {
                        log("\tFound attribute " + themeVersion);
                    }
                }
            } catch (IOException ex) {
                LogUtil.severe(null, ex);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException t) {
                }
            }
        }
        return themeVersion;
    }

    /**
     * Get the manifest resource of a class-loader.
     *
     * @param classLoader classLoader to use
     * @return {@code Collection<URL>}
     */
    private static Collection<URL> getManifests(final ClassLoader classLoader) {

        if (classLoader == null) {
            return Collections.emptyList();
        }
        Enumeration<URL> manifests = null;
        if (Beans.isDesignTime() && classLoader instanceof URLClassLoader) {
            // This is a temporary hack to limit our manifest search for themes
            // to the URLs in the ClassLoader, if it's a URLClassLoader.
            // This is necessary to get theme switching to work when there
            // are multiple simultaneous open projects in Creator.
            Set<URL> v = new HashSet<URL>();
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            for (URL url : urls) {
                try {
                    URL manifest = new URL(url, MANIFEST);
                    // See if the manifest file exists
                    InputStream is = manifest.openStream();
                    v.add(manifest);
                    is.close();
                } catch (IOException ioe) {
                    // No such manifest, so don't add one to the vector
                }
            }
            return v;
        }

        try {
            manifests = classLoader.getResources(MANIFEST);
        } catch (IOException ioex) {
            if (DEBUG) {
                log("\tIOException using the Context ClassLoader");
            }
        }
        if (manifests == null) {
            return Collections.emptyList();
        }
        if (!manifests.hasMoreElements()) {
            try {
                manifests = classLoader.getResources(MANIFEST);
            } catch (IOException ioex) {
                if (DEBUG) {
                    log("\tIOException using JarThemeFactory's ClassLoader");
                }
            }
            if (manifests == null) {
                return Collections.emptyList();
            }
        }
        return Collections.list(manifests);
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(JarThemeFactory.class.getName() + "::" + msg);
    }

    /**
     * Return the default {@code Theme} for
     * {@code locale} within the theme run-time environment of
     * {@code themeContext}.
     */
    @Override
    public Theme getTheme(final Locale locale,
            final ThemeContext themeContext) {

        return getTheme(null, locale, themeContext);
    }

    /**
     * Return the {@code themeName} {@code Theme} for
     * {@code locale} within the theme run-time environment of
     * {@code themeContext}.
     */
    @Override
    public Theme getTheme(final String themeName, final Locale locale,
            final ThemeContext themeContext) {

        // First, get the ThemeManager
        if (themeManager == null) {
            themeManager = createThemeManager(themeContext);
        }
        JarTheme theme = themeManager.getTheme(themeName, locale);
        theme.setThemeContext(themeContext);
        return theme;
    }

    @Override
    public String getDefaultThemeName(final ThemeContext themeContext) {
        if (themeManager == null) {
            themeManager = createThemeManager(themeContext);
        }
        return themeManager.getDefaultThemeName();
    }

    /**
     * Create a theme manager.
     * @param themeContext theme context
     * @return ThemeManager
     */
    @SuppressWarnings("unchecked")
    private ThemeManager createThemeManager(final ThemeContext themeContext) {

        // If the context doesn't have a default theme name
        // let the first theme jar that says default win.
        //
        String defaultThemeName = themeContext.getDefaultTheme();

        // The name value found in the current manifest attributes.
        String name = null;

        ClassLoader classLoader = themeContext.getDefaultClassLoader();
        // From the components jar
        //
        String requiredThemeVersion = getRequiredThemeVersion(classLoader);
        // Get all attributes from all theme jars.
        //
        Iterator themeAttributesIterator = getThemeAttributes(classLoader);

        if (!themeAttributesIterator.hasNext()) {
            throw new ThemeConfigurationException(WARNING_LOAD);
        }

        ThemeManager manager = new ThemeManager();
        while (themeAttributesIterator.hasNext()) {

            Attributes themeAttributes = (Attributes)
                    themeAttributesIterator.next();

            // Better not see the same theme name twice
            //
            name = readAttribute(themeAttributes, NAME);

            // Probably should see if the there are more themes.
            //
            String version = readAttribute(themeAttributes, THEME_VERSION);
            if (requiredThemeVersion != null
                    && requiredThemeVersion.compareTo(version) > 0) {
                throwVersionException(name, version, requiredThemeVersion);
            }

            Map map = new HashMap();
            Set localeSet = themeContext.getSupportedLocales();
            // No explicitly supported themes ?
            // just add the default theme, assume it is one
            // of the supported themes if localeSet is not null
            //
            if (localeSet == null) {
                FacesContext ctx = FacesContext.getCurrentInstance();
                if (ctx != null) {
                    // We're in JSF, use JSF's locales...
                    localeSet = getLocales(ctx.getApplication());
                }
            }
            if (localeSet == null) {
                // Use the default locale...
                localeSet = new HashSet();
                localeSet.add(themeContext.getDefaultLocale());
            }
            Iterator locales = localeSet.iterator();
            while (locales.hasNext()) {
                Locale locale = (Locale) (locales.next());
                // createTheme throws a ThemeConfigurationException if
                // it fails, in which case we abort
                map.put(locale, createTheme(themeAttributes, locale,
                        themeContext));
            }
            manager.addThemeMap(name, map);

            // If the theme context does not define a default theme name
            // make the first default theme jar the default theme.
            if (defaultThemeName == null) {
                String isDefault = themeAttributes.getValue(DEFAULT);
                if (isDefault != null
                        && isDefault.toLowerCase().equals("true")) {
                    defaultThemeName = name;
                }
            }
        }
        manager.setDefaultThemeName(defaultThemeName);
        return manager;
    }

    /**
     * Create a theme.
     * @param themeAttributes theme attributes
     * @param locale locale to use
     * @param themeContext theme context
     * @return Theme
     * @throws ThemeConfigurationException if an error occurs
     */
    private Theme createTheme(final Attributes themeAttributes,
            final Locale locale, final ThemeContext themeContext)
            throws ThemeConfigurationException {

        if (themeContext.getThemeServletContext() == null) {
            String prefix = readAttribute(themeAttributes, PREFIX);
            if (!prefix.startsWith("/")) {
                prefix = "/".concat(prefix);
            }
            themeContext.setThemeServletContext(prefix);
        }

        // Need to use themeContext for "translateURI" semantics.
        JarTheme theme = new JarTheme(locale);

        ResourceBundle override = null;
        ClassLoader classLoader = themeContext.getDefaultClassLoader();
        ResourceBundle bundle =
                createResourceBundle(themeAttributes, MESSAGES, locale,
                classLoader);
        String messageOverride = themeContext.getMessages();
        if (messageOverride != null) {
            try {
                override = ResourceBundle.getBundle(messageOverride, locale);
            } catch (MissingResourceException mre) {
                /* Don't do anything just use the theme's bundle
                StringBuffer errorMessage =
                new StringBuffer("The message resource file ");
                errorMessage.append(messageOverride);
                errorMessage.append(" specified by context parameter ");
                errorMessage.append(MESSAGES_PARAM);
                errorMessage.append(" does not exist.");
                throw new ThemeConfigurationException(errorMessage.toString());
                 */
            }
        }
        theme.configureMessages(bundle, override);

        // Configure the images
        bundle = createResourceBundle(themeAttributes, IMAGES, locale,
                classLoader);
        theme.configureImages(bundle);

        // Configure the javascript files
        bundle = createResourceBundle(themeAttributes, JSFILES, locale,
                classLoader);
        String jsFiles = readAttribute(themeAttributes, JSFILES);
        theme.configureJSFiles(bundle);

        // Configure the style sheets
        bundle = createResourceBundle(themeAttributes, STYLESHEETS, locale,
                classLoader);
        theme.configureStylesheets(bundle);

        // Configure the template files
        bundle = createResourceBundle(themeAttributes, TEMPLATES, locale,
                classLoader);
        theme.configureTemplates(bundle);

        // Configure the classmapper
        String classMapper = themeAttributes.getValue(CLASSMAPPER);
        if (classMapper != null && classMapper.length() > 0) {
            bundle = createResourceBundle(themeAttributes, CLASSMAPPER, locale,
                    classLoader);
            theme.configureClassMapper(bundle);
        }
        return theme;
    }

    /**
     * Create a resource bundle.
     * @param themeAttributes theme attributes
     * @param propName bundle name property
     * @param locale locale to use
     * @param classLoader class-loader to use
     * @return ResourceBundle
     */
    private ResourceBundle createResourceBundle(
            final Attributes themeAttributes, final String propName,
            final Locale locale, final ClassLoader classLoader) {

        String bundleName = readAttribute(themeAttributes, propName);
        try {
            return ResourceBundle.getBundle(bundleName, locale, classLoader);
        } catch (MissingResourceException mre) {
            StringBuilder msgBuffer = new StringBuilder();
            msgBuffer.append("Invalid theme configuration file for theme ");
            msgBuffer.append(themeAttributes.getValue(NAME));
            msgBuffer.append(
                    ".\nJarThemeFactory could not locate resource bundle at ");
            msgBuffer.append(bundleName);
            msgBuffer.append(".");
            throw new ThemeConfigurationException(msgBuffer.toString());
        }
    }
}
