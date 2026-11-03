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

import com.sun.webui.jsf.theme.ThemeJavascript;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ClientSniffer;
import com.sun.webui.jsf.util.ClientType;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;

import jakarta.faces.context.FacesContext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * The Sun Java Web UI Components rely on non-Java resources such a message
 * files, image files, CSS style sheets and JavaScript files to render
 * correctly. These resources are collectively known as a "Theme" and are
 * bundled together in a Jar file. Themes are swap-able, so you can switch from
 * one Theme to another by placing a different Theme jar in the web
 * application's class-path.
 *
 * Themes consist of both of resources that are used directly by the Java
 * classes at run-time (for example property files) and resources that are
 * requested by the application user's browser (for example image files). In
 * order to make Theme resources available over HTTP, you must configure the
 * ThemeServlet in at least one of the SJWUIC applications on the server (@see
 * ThemeServlet).
 *
 * For more information on how to configure a SJWUIC web application w.r.t.
 * Themes, see the documentation for @see ThemeServlet.
 *
 * The Theme class is used to access the resources associated with a particular
 * Theme. The manifest of the Theme jar must contain a section named
 * {@code com/sun/webui/theme} which contains attributes whose values are used
 * to locale the Theme resources. When the SJWUIC application is loaded on the
 * server, the @see ThemeFactory class looks for manifest containing such
 * sections and creates instances of the Theme class based on the information.
 * For each Theme jar, one instance is created for each locale specified in the
 * JSF configuration file.
 *
 * * Note that, since the JAR files for all installed themes are loaded into
 * the same class loader, the actual resource paths for the resources used by
 * each theme <strong>MUST</strong> be unique. This is true regardless of
 * whether the themes share a prefix or not.
 *
 */
public final class JarTheme implements Theme {

    /**
     * Height suffix.
     */
    private static final String HEIGHT_SUFFIX = "_HEIGHT";

    /**
     * Width suffix.
     */
    private static final String WIDTH_SUFFIX = "_WIDTH";

    /**
     * Alt suffix.
     */
    private static final String ALT_SUFFIX = "_ALT";

    /**
     * Global JS files.
     */
    private static final String GLOBAL_JSFILES = ThemeJavascript.GLOBAL;

    /**
     * Global CSS style-sheets.
     */
    private static final String GLOBAL_STYLESHEETS = ThemeStyles.GLOBAL;

    /**
     * Master CSS style-sheet.
     */
    private static final String MASTER_STYLESHEET = ThemeStyles.MASTER;

    /**
     * Theme context.
     */
    private  static final ThreadLocal<ThemeContext> THEME_CONTEXT =
            new ThreadLocal<ThemeContext>();

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Default resource bundle.
     */
    private ResourceBundle bundle = null;

    /**
     * Fallback resource bundle.
     */
    private ResourceBundle fallbackBundle = null;

    /**
     * Class mapper resource bundle.
     */
    private ResourceBundle classMapper = null;

    /**
     * Image resources bundle.
     */
    private ResourceBundle imageResources = null;

    /**
     * JS files bundle.
     */
    private ResourceBundle jsFiles = null;

    /**
     * CSS style-sheets bundle.
     */
    private ResourceBundle stylesheets = null;

    /**
     * Templates bundle.
     */
    private ResourceBundle templates = null;

    /**
     * Global JS files.
     */
    private String[] globalJSFiles = null;

    /**
     * Global CSS style-sheets.
     */
    private String[] globalStylesheets = null;

    /**
     * Theme prefix.
     */
    private String prefix;

    /**
     * Theme locale.
     */
    private Locale locale = null;

    /**
     * Set the theme context.
     * @param themeContext new theme context
     */
    void setThemeContext(final ThemeContext themeContext) {
        JarTheme.THEME_CONTEXT.set(themeContext);
    }

    /**
     * Get the theme context.
     * @return ThemeContext
     */
    ThemeContext getThemeContext() {
        return (ThemeContext) THEME_CONTEXT.get();
    }

    /**
     * Create a new instance.
     * @param zLocale locale to use
     */
    public JarTheme(final Locale zLocale) {
        this.locale = zLocale;
    }

    /**
     * Use this method to retrieve a String array of URIs
     * to the JavaScript files that should be included
     * with all pages of this application.
     * @return String array of URIs to the JavaScript files
     */
    @Override
    public String[] getGlobalJSFiles() {

        if (DEBUG) {
            log("getGlobalJSFiles()");
        }

        if (globalJSFiles == null) {

            try {
                String files = jsFiles.getString(GLOBAL_JSFILES);
                StringTokenizer tokenizer = new StringTokenizer(files, " ");
                String pathKey;
                String path;
                ArrayList<String> fileNames = new ArrayList<String>();

                while (tokenizer.hasMoreTokens()) {
                    pathKey = tokenizer.nextToken();
                    path = jsFiles.getString(pathKey);
                    fileNames.add(translateURI(path));
                }
                int numFiles = fileNames.size();
                globalJSFiles = new String[numFiles];
                for (int i = 0; i < numFiles; ++i) {
                    Object fileName = fileNames.get(i);
                    if (fileName != null) {
                        globalJSFiles[i] = fileName.toString();
                    }
                }
            } catch (MissingResourceException npe) {
                // Do nothing - there are no global javascript files
                //globalJSFiles = new String[0];
                return null;
            }
        }
        return globalJSFiles;
    }

    /**
     * Use this method to retrieve a String array of URIs
     * to the CSS style-sheets files that should be included
     * with all pages of this application.
     * @return String array of URIs to the style-sheets
     */
    @Override
    public String[] getGlobalStylesheets() {
        if (globalStylesheets == null) {

            try {
                String files = stylesheets.getString(GLOBAL_STYLESHEETS);
                StringTokenizer tokenizer = new StringTokenizer(files, " ");
                String pathKey;
                String path;
                ArrayList<String> fileNames = new ArrayList<String>();

                while (tokenizer.hasMoreTokens()) {
                    pathKey = tokenizer.nextToken();
                    path = stylesheets.getString(pathKey);
                    fileNames.add(translateURI(path));
                }
                int numFiles = fileNames.size();
                globalStylesheets = new String[numFiles];
                for (int i = 0; i < numFiles; ++i) {
                    globalStylesheets[i] = fileNames.get(i);
                }
            } catch (MissingResourceException npe) {
                // There was no "global" key
                // Do nothing
                //globalStylesheets = new String[0];
                return null;
            }
        }
        return globalStylesheets;
    }

    /**
     * Returns a String that represents a valid path to the JavaScript
     * file corresponding to the key.
     * @return Returns a String that represents a valid path to the JavaScript
     * file corresponding to the key
     * @param key Key to retrieve the JavaScript file
     */
    @Override
    public String getPathToJSFile(final String key) {
        if (DEBUG) {
            log("getPathToJSFile()");
        }
        String path = jsFiles.getString(key);
        if (DEBUG) {
            log("path is " + translateURI(path));
        }
        return translateURI(path);
    }

    /**
     * Retrieves a String from the JavaScript ResourceBundle without the theme
     * path prefix.
     *
     * @param key The key used to retrieve the message
     * @return A localized message string
     */
    @Override
    public String getJSString(final String key) {
        return jsFiles.getString(key);
    }

    /**
     * Returns a String that represents a valid path to the CSS style-sheet
     * corresponding to the key.
     * @param context FacesContext of the request
     * @return  A String that represents a valid path to the CSS style-sheet
     * corresponding to the key
     */
    private String getPathToStylesheet(final FacesContext context) {

        if (DEBUG) {
            log("getPathToStyleSheet()");
        }

        ClientType clientType = ClientSniffer.getClientType(context);
        if (DEBUG) {
            log("Client type is " + clientType.toString());
        }
        try {
            String path = stylesheets.getString(clientType.toString());
            if (DEBUG) {
                log(path);
                log(translateURI(path));
            }
            if (path == null || path.length() == 0) {
                return null;
            } else {
                return translateURI(path);
            }
        } catch (MissingResourceException mre) {
            StringBuilder msgBuffer =
                    new StringBuilder("Could not find property ");
            msgBuffer.append(clientType.toString());
            msgBuffer.append(" in ResourceBundle ");
            msgBuffer.append(stylesheets.toString());
            throw new RuntimeException(msgBuffer.toString());
        }
    }

    /**
     * Returns a String that represents a valid path to the CSS style-sheet
     * corresponding to the key.
     * @return  A String that represents a valid path to the CSS style-sheet
     * corresponding to the key
     */
    private String getPathToMasterStylesheet() {

        try {
            String path = stylesheets.getString(MASTER_STYLESHEET);
            if (path == null || path.length() == 0) {
                return null;
            } else {
                return translateURI(path);
            }
        } catch (MissingResourceException mre) {
            StringBuilder msgBuffer =
                    new StringBuilder("Could not find master ");
            msgBuffer.append("stylesheet in ResourceBundle ");
            msgBuffer.append(stylesheets.toString());
            throw new RuntimeException(msgBuffer.toString());
        }
    }

    @Override
    public String[] getMasterStylesheets() {
        String css = getPathToMasterStylesheet();
        if (css == null) {
            return null;
        }
        return new String[]{css};
    }

    /**
     * Returns a String that represents a valid path to the CSS style-sheet
     * corresponding to the key.
     * @param clientName resource bundle key
     * @return  A String that represents a valid path to the CSS style-sheet
     * corresponding to the key
     */
    private String getPathToStylesheet(final String clientName) {

        if (DEBUG) {
            log("getPathToStyleSheet()");
        }

        try {
            String path = stylesheets.getString(clientName);
            if (path == null || path.length() == 0) {
                return null;
            } else {
                return translateURI(path);
            }
        } catch (MissingResourceException mre) {
            StringBuilder msgBuffer =
                    new StringBuilder("Could not find propery ");
            msgBuffer.append(clientName);
            msgBuffer.append(" in ResourceBundle ");
            msgBuffer.append(stylesheets.toString());
            throw new RuntimeException(msgBuffer.toString());
        }
    }

    @Override
    public String[] getStylesheets(final String key) {
        String css = getPathToStylesheet(key);
        if (css == null) {
            return null;
        }
        return new String[]{css};
    }

    /**
     * Returns a String that represents a valid path to the HTML template
     * corresponding to the key.
     * @return  A String that represents a valid path to the HTML template
     * corresponding to the key
     */
    @Override
    public String getPathToTemplate(final String clientName) {

        if (DEBUG) {
            log("getPathToTemplate()");
        }

        try {
            String path = templates.getString(clientName);
            if (path == null || path.length() == 0) {
                return null;
            } else {
                return translateURI(path);
            }
        } catch (MissingResourceException mre) {
            StringBuilder msgBuffer =
                    new StringBuilder("Could not find propery ");
            msgBuffer.append(clientName);
            msgBuffer.append(" in ResourceBundle ");
            msgBuffer.append(templates.toString());
            throw new RuntimeException(msgBuffer.toString());
        }
    }

    /**
     * Returns the name of a CSS style. If the Theme includes a class mapper,
     * the method checks it for the presence of a mapping for the CSS class name
     * passed in with the argument. If there is no mapping, the name is used as
     * is.
     *
     * up in the class mapper if there is one, a valid path to the CSS
     * style-sheet corresponding to the key
     *
     * @param name The style class name to be used
     * @return the name of a CSS style.
     */
    @Override
    public String getStyleClass(final String name) {
        if (classMapper == null) {
            return name;
        }
        String styleClass = classMapper.getString(name);
        if (styleClass == null) {
            return name;
        }
        return styleClass;
    }

    /**
     * Retrieves a message from the appropriate ResourceBundle.
     * If the web application specifies a bundle that overrides
     * the standard bundle, that one is tried first. If no override
     * bundle is specified, or if the bundle does not contain the
     * key, the key is resolved from the Theme's default bundle.
     * @param key The key used to retrieve the message
     * @return A localized message string
     */
    @Override
    public String getMessage(final String key) {
        String message = null;
        try {
            message = bundle.getString(key);
        } catch (MissingResourceException mre) {
            try {
                message = fallbackBundle.getString(key);
            } catch (NullPointerException npe) {
                throw mre;
            }
        }
        return message;
    }

    /**
     * Retrieves a message from the appropriate ResourceBundle.
     * If the web application specifies a bundle that overrides
     * the standard bundle, that one is tried first. If no override
     * bundle is specified, or if the bundle does not contain the
     * key, the key is resolved from the Theme's default bundle.
     * @param key The key used to retrieve the message
     * @param params An object array specifying the parameters of
     * the message
     * @return A localized message string
     */
    @Override
    public String getMessage(final String key, final Object[] params) {
        String message = getMessage(key);
        MessageFormat mf = new MessageFormat(message, locale);
        return mf.format(params);
    }

    // Sets the prefix to be unconditionally prepended for any URI given out
    // by theme.
    /**
     * Sets the prefix to be prepended to the path names of the resources.
     * @param p prefix for all URIs in the theme
     */
    protected void setPrefix(final String p) {
        prefix = p;
    }

    /**
     * Configures a resource bundle which overrides the standard keys for
     * retrieving style class names.
     * @param newClassMapper A ResourceBundle that overrides the standard style
     * class keys
     */
    protected void configureClassMapper(final ResourceBundle newClassMapper) {
        this.classMapper = newClassMapper;
    }

    /**
     * Configures the message bundles. All Themes must contain a default
     * ResourceBundle for messages, which is configured in the Theme
     * configuration file. This bundle is passed in as the first parameter
     * (base).
     * Optionally, the web application developer can override
     * the messages from all themes by specifying a resource bundle
     * in a context init parameter (if they haven't done so, the second
     * parameter will be null). If the second parameter is non-null,
     * Theme.getMessage tries to get the message from the override bundle first.
     * If that fails (or if there is no override bundle), getMessage() tries
     * the base bundle.
     * @param base The message bundle specified by the Theme
     * configuration file.
     * @param override A message bundle configured by the user
     * in a context parameter, to override messages from the base bundle.
     */
    protected void configureMessages(final ResourceBundle base,
            final ResourceBundle override) {
        if (DEBUG) {
            log("configureMessages()");
        }
        if (override == null) {
            if (DEBUG) {
                log("override is null, bundle is " + base.toString());
            }
            bundle = base;
        } else {
            bundle = override;
            fallbackBundle = base;
        }
    }

    /**
     * Configures the image resource bundle.
     *
     * @param newImageResources A ResourceBundle whose keys specify
     * the available images.
     */
    protected void configureImages(final ResourceBundle newImageResources) {
        this.imageResources = newImageResources;
    }

    /**
     * Configures the JS resource files.
     *
     * @param newJsFiles A ResourceBundle whose keys specify the available
     * JavaScript files
     */
    protected void configureJSFiles(final ResourceBundle newJsFiles) {
        this.jsFiles = newJsFiles;
    }

    /**
     * Configures the style-sheets.
     *
     * @param newStyleSheets A resource bundle specifying the style-sheet for
     * each @link ClientType
     */
    protected void configureStylesheets(final ResourceBundle newStyleSheets) {
        this.stylesheets = newStyleSheets;
    }

    /**
     * Configures the HTML templates.
     *
     * @param newTemplates A ResourceBundle whose keys specify the available
     * HTML template files
     */
    protected void configureTemplates(final ResourceBundle newTemplates) {
        this.templates = newTemplates;
    }

    /**
     * This method needs to be refactored. The information about what
     * type of path to generate is available when the Theme is configured,
     * and it does not vary from request to request. So it should be
     * fixed on startup.
     * @param uri URI to be translated
     * @return translated URI String
     */
    private String translateURI(final String uri) {
        if (uri == null || uri.length() == 0) {
            return null;
        }
        ThemeContext tc = getThemeContext();
        return tc.getResourcePath(uri);
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private void log(final String msg) {
        LogUtil.finest(getClass().getName() + "::" + msg);
    }

    /**
     * Return a translated image path, containing the theme servlet context.
     *
     * @param key The key used to retrieve the image path
     * @return a path with the theme servlet context, or null if the
     * {@code key} resolves to {@code null} or the empty string.
     * @throws RuntimeException if {@code key} cannot be found.
     */
    @Override
    public String getImagePath(final String key) {
        String path = null;
        try {
            path = imageResources.getString(key);
            if (path == null || path.trim().length() == 0) {
                return null;
            }
            path = translateURI(path);

        /*
        path = (String)getProperty(
        themeReferences,
        ThemeResourceBundle.ThemeBundle.IMAGES,
        key, locale, classLoader);
         */
        } catch (MissingResourceException mre) {
            Object[] params = {key};
            String message = MessageUtil
                    .getMessage("com.sun.webui.jsf.resources.LogMessages",
                    "Theme.noIcon", params);
            throw new RuntimeException(message, mre);
        }
        return path;
    }

    /**
     * Return a {@code ThemeImage} instance for an image identified
     * by {@code key} from the
     * {@code ThemeResourceBundle.ThemeBundle.IMAGES>}
     * resource bundle.
     * The {@code key} property defines the path of the image resource.
     * If {@code key} is not defined {@code null} is returned.<br/>
     * The bundle should define additional properties
     * where the each property is defined as {@code key} with the
     * following suffixes: (i.e. key == "SMALL_ALERT", SMALL_ALERT_ALT)
     * <ul>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.ALT_SUFFIX}</li>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.TITLE_SUFFIX}</li>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.HEIGHT_SUFFIX}</li>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.WIDTH_SUFFIX}</li>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.UNITS_SUFFIX}</li>
     * </ul>
     * If {@code key} is not defined {@code key} is returned.
     */
    @Override
    public ThemeImage getImage(final String key) {
        // make sure to setIcon on parent and not the icon itself which
        // now does the theme stuff in the component

        String path = null;
        try {
            path = translateURI(imageResources.getString(key));
        } catch (MissingResourceException mre) {
            Object[] params = {key};
            String message = MessageUtil
                    .getMessage("com.sun.webui.jsf.resources.LogMessages",
                    "Theme.noIcon", params);
            throw new RuntimeException(message, mre);
        }

        String alt = null;
        try {
            alt = getMessage(imageResources.getString(key.concat(ALT_SUFFIX)));
        } catch (MissingResourceException mre) {
        }

        String title = null;

        int ht = Integer.MIN_VALUE;
        try {
            String height = imageResources.getString(key.concat(HEIGHT_SUFFIX));
            ht = Integer.parseInt(height);
        } catch (MissingResourceException mre) {
        } catch (NumberFormatException nfe) {
        }

        int wt = Integer.MIN_VALUE;
        try {
            String width = imageResources.getString(key.concat(WIDTH_SUFFIX));
            wt = Integer.parseInt(width);
        } catch (MissingResourceException mre) {
        } catch (NumberFormatException nfe) {
        }

        return new ThemeImage(wt, ht, null, alt, title, path);
    }

    /**
     * Retrieves a String from the images ResourceBundle without the theme path
     * prefix.
     *
     * @param key The key used to retrieve the message
     * @return A localized message string
     */
    @Override
    public String getImageString(final String key) {
        return imageResources.getString(key);
    }
}
