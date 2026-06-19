/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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
 *  ScopeHandlers.java
 *
 *  Created on December 2, 2004, 3:06 AM
 */
package com.sun.jsftemplating.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.el.PageSessionResolver;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.resource.ResourceBundleManager;
import com.sun.jsftemplating.util.Util;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.Serializable;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * <p>
 * This class contains {@link com.sun.jsftemplating.layout.descriptors.handler.Handler} methods that perform common
 * utility-type functions.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ScopeHandlers {

    /**
     * <p>
     * Default Constructor.
     * </p>
     */
    public ScopeHandlers() {
    }

    /**
     * <p>
     * This handler gets a request attribute. It requires "key" as an input value. It returns "value" as an output value.
     * Note this can also be done via #{requestScope["attributeName"]}.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "getAttribute", input = { @HandlerInput(name = "key", type = String.class, required = true) }, output = {
            @HandlerOutput(name = "value", type = Object.class) })
    public static void getAttribute(HandlerContext context) {
        String key = (String) context.getInputValue("key");
        Object value = context.getFacesContext().getExternalContext().getRequestMap().get(key);
        context.setOutputValue("value", value);
    }

    /**
     * <p>
     * This handler sets a request attribute. It requires "key" and "value" input values to be passed in.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "setAttribute", input = { @HandlerInput(name = "key", type = String.class, required = true),
            @HandlerInput(name = "value", required = true) })
    public static void setAttribute(HandlerContext context) {
        String key = (String) context.getInputValue("key");
        Object value = context.getInputValue("value");
        context.getFacesContext().getExternalContext().getRequestMap().put(key, value);
    }

    /**
     * <p>
     * This handler produces a String consisting of all the request attributes. It outputs this via the "value" output
     * value.
     * </p>
     */
    @Handler(id = "dumpAttributes", output = { @HandlerOutput(name = "value", type = String.class) })
    public static void dumpAttributes(HandlerContext context) {
        Map<String, Object> map = context.getFacesContext().getExternalContext().getRequestMap();
        context.setOutputValue("value", formatAttributes(map));
    }

    /**
     * <p>
     * This handler gets a "page" session attribute. It requires <code>key</code> as an input value. It returns
     * <code>value</code> as an output value. You may pass in the page (<code>UIViewRoot</code>) as an input value via the
     * <code>page</code> input value, if you don't the current <code>UIViewRoot</code> will be used.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "getPageSessionAttribute", input = { @HandlerInput(name = "key", type = String.class, required = true),
            @HandlerInput(name = "page", type = UIViewRoot.class, required = false) }, output = {
                    @HandlerOutput(name = "value", type = Serializable.class) })
    public static void getPageSessionAttribute(HandlerContext context) {
        // Get the Page Session Map
        Map<String, Serializable> map = PageSessionResolver.getPageSession(context.getFacesContext(),
                (UIViewRoot) context.getInputValue("page"));

        // Get the value to return
        Serializable value = null;
        if (map != null) {
            value = map.get(context.getInputValue("key"));
        }

        // Return the value
        context.setOutputValue("value", value);
    }

    /**
     * <p>
     * This handler sets a page session attribute. It requires <code>key</code> and <code>value</code> input values to be
     * passed in. <code>page</code> may optionally be passed in to specify the page (<code>UIViewRoot</code>) that should be
     * used -- otherwise the current <code>UIViewRoot</code> is used.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "setPageSessionAttribute", input = { @HandlerInput(name = "key", type = String.class, required = true),
            @HandlerInput(name = "value", type = Serializable.class, required = true),
            @HandlerInput(name = "page", type = UIViewRoot.class, required = false) })
    public static void setPageSessionAttribute(HandlerContext context) {
        UIViewRoot root = (UIViewRoot) context.getInputValue("page");
        FacesContext ctx = context.getFacesContext();

        // Get the Page Session Map
        Map<String, Serializable> map = PageSessionResolver.getPageSession(ctx, root);
        if (map == null) {
            map = PageSessionResolver.createPageSession(ctx, root);
        }

        // Set the page session value
        map.put((String) context.getInputValue("key"), (Serializable) context.getInputValue("value"));
    }

    /**
     * <p>
     * This handler produces a String consisting of all the page session attributes. It outputs this via the
     * <code>value</code> output value. This handler optionally accepts the <code>page</code> input value
     * (<code>UIViewRoot</code>) -- if not supplied the current <code>UIViewRoot</code> is used.
     * </p>
     */
    @Handler(id = "dumpPageSessionAttributes", input = {
            @HandlerInput(name = "page", type = UIViewRoot.class, required = false) }, output = {
                    @HandlerOutput(name = "value", type = String.class) })
    public static void dumpPageSessionAttributes(HandlerContext context) {
        // Get the Page Session Map
        Map<String, Serializable> map = PageSessionResolver.getPageSession(context.getFacesContext(),
                (UIViewRoot) context.getInputValue("page"));

        // Return the formatted result
        context.setOutputValue("value", formatAttributes(map));
    }

    /**
     * <p>
     * This handler gets a session attribute. It requires "key" as an input value. It returns "value" as an output value.
     * Note this can also be done via #{sessionScope["attributeName"]}.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "getSessionAttribute", input = { @HandlerInput(name = "key", type = String.class, required = true) }, output = {
            @HandlerOutput(name = "value", type = Object.class) })
    public static void getSessionAttribute(HandlerContext context) {
        String key = (String) context.getInputValue("key");
        Object value = context.getFacesContext().getExternalContext().getSessionMap().get(key);
        context.setOutputValue("value", value);
    }

    /**
     * <p>
     * This handler sets a session attribute. It requires "key" and "value" input values to be passed in.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "setSessionAttribute", input = { @HandlerInput(name = "key", type = String.class, required = true),
            @HandlerInput(name = "value", required = true) })
    public static void setSessionAttribute(HandlerContext context) {
        String key = (String) context.getInputValue("key");
        Object value = context.getInputValue("value");
        context.getFacesContext().getExternalContext().getSessionMap().put(key, value);
    }

    /**
     * <p>
     * This handler produces a String consisting of all the request attributes. It outputs this via the "value" output
     * value.
     * </p>
     */
    @Handler(id = "dumpSessionAttributes", output = { @HandlerOutput(name = "value", type = String.class) })
    public static void dumpSessionAttributes(HandlerContext context) {
        Map<String, Object> map = context.getFacesContext().getExternalContext().getSessionMap();
        context.setOutputValue("value", formatAttributes(map));
    }

    /**
     * <p>
     * This handler gets a application attribute. It requires "key" as an input value. It returns "value" as an output
     * value. Note this can also be done via #{applicationScope["attributeName"]}.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "getApplicationAttribute", input = { @HandlerInput(name = "key", type = String.class, required = true) }, output = {
            @HandlerOutput(name = "value", type = Object.class) })
    public static void getApplicationAttribute(HandlerContext context) {
        String key = (String) context.getInputValue("key");
        Object value = context.getFacesContext().getExternalContext().getApplicationMap().get(key);
        context.setOutputValue("value", value);
    }

    /**
     * <p>
     * This handler sets a application attribute. It requires "key" and "value" input values to be passed in.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "setApplicationAttribute", input = { @HandlerInput(name = "key", type = String.class, required = true),
            @HandlerInput(name = "value", required = true) })
    public static void setApplicationAttribute(HandlerContext context) {
        String key = (String) context.getInputValue("key");
        Object value = context.getInputValue("value");
        context.getFacesContext().getExternalContext().getApplicationMap().put(key, value);
    }

    /**
     * <p>
     * This handler produces a String consisting of all the request attributes. It outputs this via the "value" output
     * value.
     * </p>
     */
    @Handler(id = "dumpApplicationAttributes", output = { @HandlerOutput(name = "value", type = String.class) })
    public static void dumpApplicationAttributes(HandlerContext context) {
        Map<String, Object> map = context.getFacesContext().getExternalContext().getApplicationMap();
        context.setOutputValue("value", formatAttributes(map));
    }

    /**
     * <p>
     * This method formats attributes from a <code>Map</code>. This is used with the dump handlers.
     * </p>
     */
    private static <T> String formatAttributes(Map<String, T> map) {
        Formatter printf = new Formatter();
        if (map == null) {
            printf.format("Map null.");
        } else {
            Iterator<Map.Entry<String, T>> it = map.entrySet().iterator();
            Map.Entry<String, T> entry = null;
            while (it.hasNext()) {
                entry = it.next();
                printf.format("%-20s = %s\n", entry.getKey(), entry.getValue());
            }
        }
        return printf.toString();
    }

    /**
     * <p>
     * This handler sets a <code>ResourceBundle</code> in desired request attribute. It requires "key" as an input value,
     * which is the request attribute key used to store the bundle. "bundle" is also required as an input value, this is the
     * fully qualified name of the bundle. Optionally the "locale" can be specified, if not the web user's locale will be
     * used. It returns "bundle" as an output value.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "setResourceBundle", input = { @HandlerInput(name = "key", type = String.class, required = true),
            @HandlerInput(name = "bundle", type = String.class, required = true),
            @HandlerInput(name = "locale", type = Locale.class, required = false) }, output = {
                    @HandlerOutput(name = "result", type = ResourceBundle.class) })
    public static void setResourceBundle(HandlerContext context) {
        // Get the input
        String key = (String) context.getInputValue("key");
        String name = (String) context.getInputValue("bundle");
        Locale locale = (Locale) context.getInputValue("locale");
        FacesContext ctx = context.getFacesContext();
        if (locale == null) {
            locale = Util.getLocale(ctx);
        }

        // Get the ResourceBundle
        ResourceBundle bundle = ResourceBundleManager.getInstance(ctx).getBundle(name, locale);

        // Store it in the Request Map
        ctx.getExternalContext().getRequestMap().put(key, bundle);

        // Return it
        context.setOutputValue("result", bundle);
    }

    /**
     * <p>
     * This method provides access to the named cookie. If the cookie doesn't exist, the value will be set to ""
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "getCookie", input = { @HandlerInput(name = "key", type = String.class, required = true) }, output = {
            @HandlerOutput(name = "value", type = String.class) })
    public static void getCookie(HandlerContext context) {
        Map<String, Object> cookies = context.getFacesContext().getExternalContext().getRequestCookieMap();
        Cookie cookie = (Cookie) cookies.get(context.getInputValue("key"));
        context.setOutputValue("value", cookie == null ? "" : cookie.getValue());
    }

    /**
     * <p>
     * This method set the named cookie with the given value. Because cookies are set via a response header, this method
     * must be called before the content rendering has begun (before the rendering phase). This method is not valid for
     * Portlets and will do nothing if called in a Portlet environment.
     * </p>
     *
     * @param context The {@link HandlerContext}.
     */
    @Handler(id = "setCookie", input = { @HandlerInput(name = "key", type = String.class, required = true),
            @HandlerInput(name = "value", type = String.class, required = true),
            @HandlerInput(name = "maxAge", type = Integer.class, defaultValue = "-1", required = false),
            @HandlerInput(name = "domain", type = String.class, required = false),
            @HandlerInput(name = "path", type = String.class, required = false),
            @HandlerInput(name = "secure", type = Boolean.class, defaultValue = "false", required = false),
            @HandlerInput(name = "version", type = Integer.class, required = false) })
    public static void setCookie(HandlerContext context) {
        Object resp = context.getFacesContext().getExternalContext().getResponse();
        if (resp instanceof HttpServletResponse) {
            // Create a Cookie
            Cookie cookie = new Cookie((String) context.getInputValue("key"), (String) context.getInputValue("value"));

            // Set Max Age
            cookie.setMaxAge((Integer) context.getInputValue("maxAge"));

            // Set Domain
            String domain = (String) context.getInputValue("domain");
            if (domain != null) {
                cookie.setDomain(domain);
            }

            // Set Path
            String path = (String) context.getInputValue("path");
            if (path != null) {
                cookie.setPath(path);
            }

            // Set Secure Flag
            cookie.setSecure((Boolean) context.getInputValue("secure"));

            // Set version (0 = orig Netscape; 1= RFC 2109)
            Integer version = (Integer) context.getInputValue("version");
            if (version != null) {
                cookie.setVersion(version);
            }

            // Add the cookie
            ((HttpServletResponse) resp).addCookie(cookie);
        }
    }

    /**
     * <p>
     * This {@link Handler} allows you to set a preference via the Java Preferences API. The <code>root</code> specifies the
     * path in which the preference should be stored (e.g. '/org/company/foo'). The <code>key</code> is the name of the
     * preference, and the <code>value</code> is the value of the preference.
     * </p>
     */
    @Handler(id = "setPreference", input = { @HandlerInput(name = "root", type = String.class, required = true),
            @HandlerInput(name = "key", type = String.class, required = true),
            @HandlerInput(name = "value", type = String.class, required = true) })
    public static void setPreference(HandlerContext context) {
        String nodeKey = (String) context.getInputValue("root");
        String key = (String) context.getInputValue("key");
        String value = (String) context.getInputValue("value");
        Preferences prefs = Preferences.userRoot().node(nodeKey);
        prefs.put(key, value);
    }

    /**
     * <p>
     * This {@link Handler} allows you to get a preference via the Java Preferences API. The <code>root</code> specifies the
     * path in which the preference should be stored (e.g. '/org/company/foo'). The <code>key</code> is the name of the
     * preference, and the <code>default</code> is the value of the preference if it does not yet exist. The preference
     * value will be returned via the <code>value</code> output value.
     * </p>
     */
    @Handler(id = "getPreference", input = { @HandlerInput(name = "root", type = String.class, required = true),
            @HandlerInput(name = "key", type = String.class, required = true),
            @HandlerInput(name = "default", type = String.class) }, output = { @HandlerOutput(name = "value", type = String.class) })
    public static void getPreference(HandlerContext context) {
        String nodeKey = (String) context.getInputValue("root");
        String key = (String) context.getInputValue("key");
        String def = (String) context.getInputValue("default");
        Preferences prefs = Preferences.userRoot().node(nodeKey);
        String value = prefs.get(key, def);
        context.setOutputValue("value", value);
    }
}
