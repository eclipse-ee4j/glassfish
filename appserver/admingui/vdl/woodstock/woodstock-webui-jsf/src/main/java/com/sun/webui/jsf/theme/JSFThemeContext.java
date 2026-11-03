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

package com.sun.webui.jsf.theme;

import com.sun.webui.jsf.util.ClassLoaderFinder;
import com.sun.webui.theme.ServletThemeContext;
import com.sun.webui.theme.ThemeContext;

import jakarta.faces.context.FacesContext;

import java.beans.Beans;
import java.net.URL;
import java.util.Map;

/**
 * {@code JSFThemeContext} encapsulates the theme JSF run-time environment.
 * It is different from other potential run-time environments
 * in that JSF encapsulates run-time contexts in a {@code FacesContext}.
 * This context could encapsulate a servlet context or a portlet context.
 * As such application information affecting a theme may be obtained
 * differently. This class encapsulates that behavior.
 */
public final class JSFThemeContext extends ServletThemeContext {

    /**
     * An object to synchronize with.
     */
    private static final Object SYNC_OBJECT = new Object();

    /**
     * Construction is controlled by {@code getInstance}.
     * @param context faces context
     */
    private JSFThemeContext(final FacesContext context) {
        super(context.getExternalContext().getInitParameterMap());
    }

    /**
     * Return an instance of {@code ThemeContext} creating one if necessary
     * and persisting it in the {@code ApplicationMap}.
     *
     * Note that since a ThemeServlet <b>MUST</b> be defined then getInstance
     * should never have to create a ThemeContext instance.
     *
     * @param context faces context
     * @return ThemeContext
     */
    @SuppressWarnings("unchecked")
    public static ThemeContext getInstance(final FacesContext context) {

        // Does it make sense call a "super.getInstance()" ?
        //
        // In a servlet environment the JSF ApplicationMap is the
        // ServletContext and entires in the map are references by
        // ServletContext.{get/set}Attribute().
        // But a PortletContext is not a direct descendant of
        // ServletContext and there is not "interface compatible".
        //
        // We need synchronization here because there is one
        // ThemeContext per application servlet.

        Map map = context.getExternalContext().getApplicationMap();
        ThemeContext themeContext;
        synchronized (SYNC_OBJECT) {
            // try again in case another thread created it.
            //
            themeContext = (ThemeContext) map.get(THEME_CONTEXT);
            if (themeContext == null) {
                themeContext = new JSFThemeContext(context);
                map.put(THEME_CONTEXT, themeContext);
            }
        }
        return themeContext;
    }

    @Override
    public ClassLoader getDefaultClassLoader() {
        return ClassLoaderFinder.getCurrentLoader(JSFThemeContext.class);
    }

    @Override
    public void setDefaultClassLoader(final ClassLoader classLoader) {
    }

    @Override
    public String getRequestContextPath() {
        return FacesContext.getCurrentInstance().getExternalContext().
                getRequestContextPath();
    }

    @Override
    public void setRequestContextPath(final String path) {
    }

    @Override
    public String getResourcePath(final String path) {
        String resourcePath = path;
        if (Beans.isDesignTime()) {
            ClassLoader cl = getDefaultClassLoader();
            URL url = cl.getResource(path);
            if (url != null) {
                resourcePath = url.toExternalForm();
            }
        } else if (path != null) {
            FacesContext context = FacesContext.getCurrentInstance();
            String servletContext = getThemeServletContext();
            StringBuilder sb = new StringBuilder();
            // Just to make sure
            //
            if (!servletContext.startsWith("/")) {
                sb.append("/");
            }
            sb.append(servletContext);
            if (!path.startsWith("/") && !servletContext.endsWith("/")) {
                sb.append("/");
            }
            sb.append(path);
            resourcePath = context.getApplication().getViewHandler().
                    getResourceURL(context, sb.toString());
        }
        return resourcePath;
    }
}
