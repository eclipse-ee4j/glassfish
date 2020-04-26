/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.theme;

import com.sun.webui.jsf.theme.JSFThemeContext;
import com.sun.webui.theme.ServletThemeContext;
import com.sun.webui.theme.ThemeContext;

import org.glassfish.admingui.common.util.GuiUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Enumeration;

import javax.faces.context.FacesContext;


/**
 *  <p>	This class allows us to use a <code>Map</code> to pass in parameters
 *	which alter the Woodstock theme instead of using context-params from
 *	the web.xml.  This allows us greater runtime control over the theme and
 *	helps allow the Woodstock theme properties to be overridden by
 *	plugins.</p>
 *
 *  @author ana
 *  @author Ken
 */
public class AdminguiThemeContext extends ServletThemeContext {

    /**
     *	<p> This constructor takes in the theme name and version to use by
     *	    default.</p>
     *
     *	@param	themeName	The theme name (i.e. suntheme).
     *	@param	themeVersion	The version number (i.e. 4.2).
     */
    protected AdminguiThemeContext(String themeName, String themeVersion) {
	super(setThemeParams(themeName, themeVersion));

	// The following ThemeContext is created to allow us to delegate our
	// logic to it.
	jsfThemeCtx = JSFThemeContext.getInstance(FacesContext.getCurrentInstance());
        jsfThemeCtx.setThemeServletContext("/theme");
    }

    
    /**
     *	<p> Return an instance of <code>ThemeContext</code> creating one
     *	    if necessary and persisting it in the <code>ApplicationMap</code>.
     */
    public synchronized static ThemeContext getInstance(FacesContext context, String themeName, String themeVersion) {
	Map map = context.getExternalContext().getApplicationMap();
	String themeKey = THEME_CONTEXT + themeName + themeVersion;
	ThemeContext themeContext = (ThemeContext) map.get(themeKey);
	if (themeContext == null) {
	    themeContext = new AdminguiThemeContext(themeName, themeVersion);
	    map.put(themeKey, themeContext);
	}
	return themeContext;
    }
    
/**
     *	<p> Return an instance of <code>ThemeContext</code> 
     *	    using properties provided via <code>Integration point</code>.
     */
    public synchronized static ThemeContext getInstance(FacesContext context,  Properties propMap) {
	Map map = context.getExternalContext().getApplicationMap();        
        String themeName = (String)propMap.get(THEME_NAME_KEY);
        String themeVersion = (String)propMap.get(THEME_VERSION_KEY);
	String themeKey = THEME_CONTEXT + themeName + themeVersion;
	ThemeContext themeContext = (ThemeContext) map.get(themeKey);
	if (themeContext == null) {
	    themeContext = new AdminguiThemeContext(themeName, themeVersion);
	    map.put(themeKey, themeContext);
	}
	return themeContext;
    }    

    
    /**
     *	<p> Creates a <code>Map</code> object with the theme name and
     *	    version.</p>
     */
    public static Map setThemeParams(String theme, String version) {
	Map map = new HashMap();
	if (theme == null) {
	    theme = "suntheme";
	}
	map.put(ThemeContext.DEFAULT_THEME, theme);
	if (version == null) {
	    version = "4.2";
	}
	map.put(ThemeContext.DEFAULT_THEME_VERSION, version);
	return map;
    }
    
    
    /**
     *	<p> This method delegates to <code>JSFThemeContext</code>.</p>
     */
    public ClassLoader getDefaultClassLoader() {
	return jsfThemeCtx.getDefaultClassLoader();
    }

    /**
     *	<p> This method delegates to <code>JSFThemeContext</code>.</p>
     */
    public void setDefaultClassLoader(ClassLoader classLoader) {
	jsfThemeCtx.setDefaultClassLoader(classLoader);
    }

    /**
     *	<p> This method delegates to <code>JSFThemeContext</code>.</p>
     */
    public String getRequestContextPath() {
	return jsfThemeCtx.getRequestContextPath();
    }

    /**
     *	<p> This method delegates to <code>JSFThemeContext</code>.</p>
     */
    public void setRequestContextPath(String path) {
	jsfThemeCtx.setRequestContextPath(path);
    }

    /**
     *	<p> This method delegates to <code>JSFThemeContext</code>.</p>
     */
    public String getResourcePath(String path) {
	return jsfThemeCtx.getResourcePath(path);
    }


    /**
     *	<p> This hold a reference to an instance of JSFThemeContext which
     *	    will help us implement the functionality of this class.</p>
     */
    private ThemeContext jsfThemeCtx = null;
    
    /**
     *	<p> These keys are used when getting the property values
     *	    provided in the custom theme plugin properties file.</p>
     */
    public static final String THEME_NAME_KEY = "com.sun.webui.theme.DEFAULT_THEME";
    public static final String THEME_VERSION_KEY = "com.sun.webui.theme.DEFAULT_THEME_VERSION";
}
