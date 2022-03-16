/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.apache.catalina.core;

import org.apache.catalina.LogFacade;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import jakarta.servlet.SessionCookieConfig;

/**
 * Class that may be used to configure various properties of cookies
 * used for session tracking purposes.
 */
public class SessionCookieConfigImpl implements SessionCookieConfig {

    private static final int DEFAULT_MAX_AGE = -1;
    private static final boolean DEFAULT_HTTP_ONLY = false;
    private static final boolean DEFAULT_SECURE = false;
    private static final String DEFAULT_NAME = "JSESSIONID";

    private String name = DEFAULT_NAME;

    private final Map<String,String> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final StandardContext ctx;

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();


    /**
     * Constructor
     */
    public SessionCookieConfigImpl(StandardContext ctx) {
        this.ctx = ctx;
    }


    /**
     * @param name the cookie name to use
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    @Override
    public void setName(String name) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SESSION_COOKIE_CONFIG_ALREADY_INIT),
                                              new Object[] {"name", ctx.getName()});
            throw new IllegalStateException(msg);
        }

        this.name = name;
        ctx.setSessionCookieName(name);
    }


    /**
     * @return the cookie name set via {@link #setName}, or
     * <tt>JSESSIONID</tt> if {@link #setName} was never called
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * @param domain the cookie domain to use
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    @Override
    public void setDomain(String domain) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SESSION_COOKIE_CONFIG_ALREADY_INIT),
                                              new Object[] {"dnmain", ctx.getName()});
            throw new IllegalStateException(msg);
        }
        setAttribute(Constants.COOKIE_DOMAIN_ATTR, domain);
    }


    /**
     * @return the cookie domain set via {@link #setDomain}, or
     * <tt>null</tt> if {@link #setDomain} was never called
     */
    @Override
    public String getDomain() {
        return getAttribute(Constants.COOKIE_DOMAIN_ATTR);
    }


    /**
     * @param path the cookie path to use
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    @Override
    public void setPath(String path) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SESSION_COOKIE_CONFIG_ALREADY_INIT),
                                              new Object[] {"path", ctx.getName()});
            throw new IllegalStateException(msg);
        }
        setAttribute(Constants.COOKIE_PATH_ATTR, path);
    }


    /**
     * @return the cookie path set via {@link #setPath}, or the context
     * path of the <tt>ServletContext</tt> from which this
     * <tt>SessionCookieConfig</tt> was acquired if {@link #setPath}
     * was never called
     */
    @Override
    public String getPath() {
        return getAttribute(Constants.COOKIE_PATH_ATTR);
    }


    /**
     * @param comment the cookie comment to use
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    @Override
    public void setComment(String comment) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SESSION_COOKIE_CONFIG_ALREADY_INIT),
                                              new Object[] {"comment", ctx.getName()});
            throw new IllegalStateException(msg);
        }
        setAttribute(Constants.COOKIE_COMMENT_ATTR, comment);
    }


    /**
     * @return the cookie comment set via {@link #setComment}, or
     * <tt>null</tt> if {@link #setComment} was never called
     */
    @Override
    public String getComment() {
        return getAttribute(Constants.COOKIE_COMMENT_ATTR);
    }


    /**
     * @param httpOnly true if the session tracking cookies created
     * on behalf of the <tt>ServletContext</tt> from which this
     * <tt>SessionCookieConfig</tt> was acquired shall be marked as
     * <i>HttpOnly</i>, false otherwise
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    @Override
    public void setHttpOnly(boolean httpOnly) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SESSION_COOKIE_CONFIG_ALREADY_INIT),
                                              new Object[] {"httpOnly", ctx.getName()});
            throw new IllegalStateException(msg);
        }
        setAttribute(Constants.COOKIE_HTTP_ONLY_ATTR, String.valueOf(httpOnly));
    }


    /**
     * @return true if the session tracking cookies created on behalf of the
     * <tt>ServletContext</tt> from which this <tt>SessionCookieConfig</tt>
     * was acquired will be marked as <i>HttpOnly</i>, false otherwise
     */
    @Override
    public boolean isHttpOnly() {
        String value = getAttribute(Constants.COOKIE_HTTP_ONLY_ATTR);
        return value == null ? DEFAULT_HTTP_ONLY : Boolean.parseBoolean(value);
    }


    /**
     * @param secure true if the session tracking cookies created on
     * behalf of the <tt>ServletContext</tt> from which this
     * <tt>SessionCookieConfig</tt> was acquired shall be marked as
     * <i>secure</i> even if the request that initiated the corresponding
     * session is using plain HTTP instead of HTTPS, and false if they
     * shall be marked as <i>secure</i> only if the request that initiated
     * the corresponding session was also secure
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    @Override
    public void setSecure(boolean secure) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SESSION_COOKIE_CONFIG_ALREADY_INIT),
                                              new Object[] {"secure", ctx.getName()});
            throw new IllegalStateException(msg);
        }
        setAttribute(Constants.COOKIE_SECURE_ATTR, String.valueOf(secure));
    }


    /**
     * @return true if the session tracking cookies created on behalf of the
     * <tt>ServletContext</tt> from which this <tt>SessionCookieConfig</tt>
     * was acquired will be marked as <i>secure</i> even if the request
     * that initiated the corresponding session is using plain HTTP
     * instead of HTTPS, and false if they will be marked as <i>secure</i>
     * only if the request that initiated the corresponding session was
     * also secure
     */
    @Override
    public boolean isSecure() {
        String value = getAttribute(Constants.COOKIE_SECURE_ATTR);
        return value == null ? DEFAULT_SECURE : Boolean.parseBoolean(value);
    }


    @Override
    public void setMaxAge(int maxAge) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SESSION_COOKIE_CONFIG_ALREADY_INIT),
                                              new Object[] {"maxAge", ctx.getName()});
            throw new IllegalStateException(msg);
        }
        setAttribute(Constants.COOKIE_MAX_AGE_ATTR, String.valueOf(maxAge));
    }


    @Override
    public int getMaxAge() {
        String value = getAttribute(Constants.COOKIE_MAX_AGE_ATTR);
        return value == null ? DEFAULT_MAX_AGE : Integer.parseInt(value);
    }

    @Override
    public void setAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    @Override
    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }
}
