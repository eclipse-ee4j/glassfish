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

 /*
 * $Id: Theme.java,v 1.1.6.1 2009-12-29 05:05:17 jyeary Exp $
 */
package com.sun.webui.theme;

/**
 * <p>
 * The Sun Java Web UI Components rely on non-Java resources such a message
 * files, image files, CSS stylesheets and JS files to render correctly. These
 * resources are collectively known as a "Theme" and are bundled together in a
 * Jar file. Themes are swappable, so you can switch from one Theme to another
 * by placing a different Theme jar in the web application's classpath.<p>
 *
 * <p>
 * Themes consist of both of resources that are used directly by the Java
 * classes at runtime (for example property files) and resources that are
 * requested by the application users' browser (for example image files). In
 * order to make Theme resources available over HTTP, you must configure the
 * ThemeServlet in at least one of the SJWUIC applications on the server (@see
 * ThemeServlet).</p>
 *
 * <p>
 * For more information on how to configure a SJWUIC web application w.r.t.
 * Themes, see the documentation for @see ThemeServlet.</p>
 *
 * <p>
 * The Theme class is used to access the resources associated with a particular
 * Theme. The manifest of the Theme jar must contain a section named
 * {@code com/sun/webui/theme} which contains attributes whose values are
 * used to locale the Theme resources. When the SJWUIC application is loaded on
 * the server, the @see ThemeFactory class looks for manifest containing such
 * sections and creates instances of the Theme class based on the information.
 * For each Theme jar, one instance is created for each locale specified in the
 * JSF configuration file. </p>
 *
 * * <p>
 * Note that, since the JAR files for all installed themes are loaded into the
 * same class loader, the actual resource paths for the resources used by each
 * theme <strong>MUST</strong> be unique. This is true regardless of whether the
 * themes share a prefix or not.</p>
 *
 */
public interface Theme {

    /**
     * Attribute name used to store the user's theme name in the Session.
     */
    String THEME_ATTR = "com.sun.webui.jsf.Theme";

    /**
     * The context parameter name used to specify a console path, if one is
     * used.
     */
    String RESOURCE_PATH_ATTR  = "com.sun.web.console.resource_path";

    /**
     * Use this method to retrieve a String array of URIs to the JS files that
     * should be included with all pages of this application.
     *
     * @return String array of URIs to the JS files
     */
    String[] getGlobalJSFiles();

    /**
     * Use this method to retrieve a String array of URIs to the CSS style
     * sheets files that should be included with all pages of this application.
     *
     * @return String array of URIs to the style sheets
     */
    String[] getGlobalStylesheets();

    /**
     * Returns a String that represents a valid path to the JS file
     * corresponding to the key.
     *
     * @return Returns a String that represents a valid path to the JS file
     * corresponding to the key
     * @param key Key to retrieve the JS file
     */
    String getPathToJSFile(String key);

    /**
     * Retrieves a String from the JS ResourceBundle without the theme path
     * prefix.
     *
     * @param key The key used to retrieve the message
     * @return A localized message string
     */
    String getJSString(String key);

    /**
     * Get master style-sheets.
     * @return String[]
     */
    String[] getMasterStylesheets();

    /**
     * Get style-sheets.
     * @param key key
     * @return String[]
     */
    String[] getStylesheets(String key);

    /**
     * Returns a String that represents a valid path to the HTML template
     * corresponding to the key.
     *
     * @param clientName resource bundle key
     * @return A String that represents a valid path to the HTML template
     * corresponding to the key
     */
    String getPathToTemplate(String clientName);

    /**
     * Returns the name of a CSS style. If the Theme includes a class mapper,
     * the method checks it for the presence of a mapping for the CSS class name
     * passed in with the argument. If there is no mapping, the name is used as
     * is.
     *
     * up in the class mapper if there is one, a valid path to the CSS style
     * sheet corresponding to the key
     *
     * @param name The style class name to be used
     * @return the name of a CSS style.
     */
    String getStyleClass(String name);

    /**
     * Retrieves a message from the appropriate ResourceBundle. If the web
     * application specifies a bundle that overrides the standard bundle, that
     * one is tried first. If no override bundle is specified, or if the bundle
     * does not contain the key, the key is resolved from the Theme's default
     * bundle.
     *
     * @param key The key used to retrieve the message
     * @return A localized message string
     */
    String getMessage(String key);

    /**
     * Retrieves a message from the appropriate ResourceBundle. If the web
     * application specifies a bundle that overrides the standard bundle, that
     * one is tried first. If no override bundle is specified, or if the bundle
     * does not contain the key, the key is resolved from the Theme's default
     * bundle.
     *
     * @param key The key used to retrieve the message
     * @param params An object array specifying the parameters of the message
     * @return A localized message string
     */
    String getMessage(String key, Object[] params);

    /**
     * Return a translated image path, containing the theme servlet context.
     *
     * @param key The key used to retrieve the image path
     * @return a path with the theme servlet context, or null if the {@code key}
     * resolves to {@code null} or the empty string.
     * @throws RuntimeException if {@code key} cannot be found.
     */
    String getImagePath(String key);

    /**
     * Return a {@code ThemeImage} instance for an image identified by
     * {@code key} from the {@code ThemeResourceBundle.ThemeBundle.IMAGES>}
     * resource bundle.The {@code key} property defines the path of the image
     * resource.If {@code key} is not defined {@code null} is returned.<br>
     * The bundle should define additional properties where the each property is
     * defined as {@code key} with the following suffixes: (i.e. key ==
     * "SMALL_ALERT", SMALL_ALERT_ALT)
     * <ul>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.ALT_SUFFIX}</li>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.TITLE_SUFFIX}</li>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.HEIGHT_SUFFIX}</li>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.WIDTH_SUFFIX}</li>
     * <li>{@link com.sun.webui.jsf.theme.ThemeImage.UNITS_SUFFIX}</li>
     * </ul>
     * If {@code key} is not defined {@code key} is returned.
     *
     * @param key image key
     * @return ThemeImage
     */
    ThemeImage getImage(String key);

    /**
     * Retrieves a String from the images ResourceBundle without the theme path
     * prefix.
     *
     * @param key The key used to retrieve the message
     * @return A localized message string
     */
    String getImageString(String key);
}
