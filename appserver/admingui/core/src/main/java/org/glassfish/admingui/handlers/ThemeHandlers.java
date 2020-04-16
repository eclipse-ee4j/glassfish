/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.handlers;


import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.webui.theme.ThemeContext;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import jakarta.faces.context.FacesContext;
import org.glassfish.admingui.common.handlers.PluginHandlers;
import org.glassfish.admingui.common.plugin.ConsoleClassLoader;
import org.glassfish.admingui.connector.IntegrationPoint;
import org.glassfish.admingui.theme.AdminguiThemeContext;

/**
 *
 * @author anilam
 */
public class ThemeHandlers {

        /**
     *	<p> This method initializes the theme using the given
     *	    <code>themeName</code> and <code>themeVersion</code>.  If these
     *	    values are not supplied, "suntheme" and "4.2" will be used
     *	    respectively.  This method should be invoked before the theme is
     *	    accessed (for example on the initPage or beforeCreate of the login
     *	    page).</p>
     *
     */
    @Handler(id = "getTheme", input = {
        @HandlerInput(name = "themeName", type = String.class),
        @HandlerInput(name = "themeVersion", type = String.class)
        },
        output = {
            @HandlerOutput(name = "themeContext", type = ThemeContext.class)
        })
    public static void getTheme(HandlerContext handlerCtx) {
        String themeName = (String) handlerCtx.getInputValue("themeName");
        String themeVersion = (String) handlerCtx.getInputValue("themeVersion");
        ThemeContext themeContext = AdminguiThemeContext.getInstance(
                handlerCtx.getFacesContext(), themeName, themeVersion);
        handlerCtx.setOutputValue("themeContext", themeContext);
    }

    /**
     *	<p> This method gets the <code>themeName</code> and <code>themeVersion</code>
     *	    via <code>Integration Point</code>.  If more than one is provided
     *	    the one with the lowest <code>priority</code> number will be used.
     *	    This method should be invoked before the theme is
     *	    accessed (for example on the initPage or beforeCreate of the login page).</p>
     */
    @Handler(id = "getThemeFromIntegrationPoints", output = {
        @HandlerOutput(name = "themeContext", type = ThemeContext.class)
    })
    public static void getThemeFromIntegrationPoints(HandlerContext handlerCtx) {
        FacesContext ctx = handlerCtx.getFacesContext();
        String type = "org.glassfish.admingui:customtheme";
        List<IntegrationPoint> ipList = PluginHandlers.getIntegrationPoints(ctx, type);
        if (ipList != null) {
            //if more than one integration point is provided then we
            //need to find the lowest priority number
            int lowest = getLowestPriorityNum(ipList);
            for (IntegrationPoint ip : ipList) {
                int priority = ip.getPriority();
                if (priority == lowest) {
                    String content = ip.getContent();
                    if (content == null || content.equals("")) {
                        throw new IllegalArgumentException("No Properties File Name Provided!");
                    }
                    ClassLoader pluginCL = ConsoleClassLoader.findModuleClassLoader(ip.getConsoleConfigId());
                    URL propertyFileURL = pluginCL.getResource("/" + content);
                    try {
                        Properties propertyMap = new Properties();
                        propertyMap.load(propertyFileURL.openStream());
                        ThemeContext themeContext =
			    AdminguiThemeContext.getInstance(ctx, propertyMap);
			themeContext.setDefaultClassLoader(pluginCL);
                        handlerCtx.setOutputValue("themeContext", themeContext);
                    } catch (Exception ex) {
                        throw new RuntimeException(
                                "Unable to access properties file '" + content + "'!", ex);
                    }
                }
            }
        }

    }

    private static int getLowestPriorityNum(List ipList) {
        Iterator iter = ipList.iterator();
            //assuming priority values can only be 1 to 100
            int lowest = 101;
            while (iter.hasNext()) {
                IntegrationPoint iP = (IntegrationPoint) iter.next();
                if (iP.getPriority() < lowest) {
                    lowest = iP.getPriority();
                }
            }

        return lowest;
    }

}
