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

package com.sun.enterprise.web.session;

import java.net.URLEncoder;

/**
 * Representation of the session cookie configuration element for a web
 * application.
 *
 * This configuration is not specified as part of the standard deployment
 * descriptor but as part of the iAS 7.0's "extended" web application
 * deployment descriptor - ias-web.xml.
 */

public final class SessionCookieConfig {

    // ----------------------------------------------------- Manifest Constants

    /**
     * The value that allows the JSESSIONID cookie's secure attribute to
     * be configured based on the connection i.e. secure if HTTPS.
     */
    public static final String DYNAMIC_SECURE = "dynamic";

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new SessionCookieConfig with default properties.
     */
    public SessionCookieConfig() {
        super();
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The name of the cookie used for session tracking.
     *
     * Default value is JSESSIONID
     */
    private String _name = null;

    /**
     * The pathname that is set when the cookie is created.
     *
     * The default value is the context path at which the web application
     * is installed.  The browser will send the cookie if the pathname for the
     * request contains this pathname. If set to / (slash), the browser will
     * send the cookie to all URLs.
     */
    private String _path = null;

    /**
     * The expiration time in seconds after which the browser expires
     * the cookie.
     *
     * The default value is -1 (never expire) will be set in
     * org.apache.catalina.core.SessionCookieConfigImpl
     */
    private Integer _maxAge = null;

    /**
     * The domain for which the cookie is valid.
     */
    private String _domain = null;

    /**
     * The comment that identifies the session tracking cookie in the
     * browser's cookie file. Applications may choose to provide a more
     * specific name for this cookie.
     */
    private String _comment = null;

    /**
     * When set to "dynamic", the cookie is marked as secure only if the
     * connection on which the request was received is secure. To override this
     * behaviour, the value of this property can be set to "true" or "false".
     * If set to "true", user agents will use secure means to contact the
     * origin server when sending back the cookie regardless of whether the
     * connection on which the request was received is secure. If set to
     * "false", user agents do not have to use secure means to contact the
     * origin server when sending back the cookie regardless of whether the
     * connection on which the request was received is secure.
     */
    private String _secure = DYNAMIC_SECURE;

    /**
     * The Boolean (if set) indicates whether the session coookie will
     * be marked as httpOnly.
     *
     * The default value is true will be set in
     * org.apache.catalina.core.SessionCookieConfigImpl
     */
    private Boolean _httpOnly = null;

    /**
     * Construct a new SessionCookieConfig with the specified properties.
     *
     * @param name    The name of the cookie used for session tracking
     * @param path    The pathname that is set when the cookie is created
     * @param maxAge  The expiration time (in seconds) of the session cookie
     *                (-1 indicates 'never expire')
     * @param domain  The domain for which the cookie is valid
     * @param comment The comment that identifies the session tracking cookie
     *                in the cookie file.
     */
    public SessionCookieConfig(String name, String path, int maxAge,
                               String domain, String comment) {
        super();
        setName(name);
        setPath(path);
        setMaxAge(maxAge);
        setDomain(domain);
        setComment(comment);
    }

    // ------------------------------------------------------------- Properties

    /**
     * Set the name of the session tracking cookie (currently not supported).
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Return the name of the session tracking cookie.
     */
    public String getName() {
        return _name;
    }

    /**
     * Set the path to use when creating the session tracking cookie.
     */
    public void setPath(String path) {
        _path = path;
    }

    /**
     * Return the path that is set when the session tracking cookie is
     * created.
     */
    public String getPath() {
        return _path;
    }

    /**
     * Set the expiration time for the session cookie.
     */
    public void setMaxAge(Integer maxAge) {
        _maxAge = maxAge;
    }

    /**
     * Return the expiration time for the session cookie.
     */
    public Integer getMaxAge() {
        return _maxAge;
    }

    /**
     * Set the domain for which the cookie is valid.
     */
    public void setDomain(String domain) {
        _domain = domain;
    }

    /**
     * Return the domain for which the cookie is valid.
     */
    public String getDomain() {
        return _domain;
    }

    /**
     * Set the comment that identifies the session cookie.
     */
    public void setComment(String comment) {
        _comment = comment;
        if (comment != null)
            _comment = URLEncoder.encode(comment);
    }

    /**
     * Return the URLEncoded form of the comment that identifies the session
     * cookie.
     */
    public String getComment() {
        return _comment;
    }

    /**
     * Set whether the cookie is marked Secure or not.
     * @param secure Valid values are "dynamic", "true" or "false"
     */
    public void setSecure(String secure) throws IllegalArgumentException {
        if ((secure == null) || (!secure.equalsIgnoreCase("true") &&
                !secure.equalsIgnoreCase("false") &&
                !secure.equalsIgnoreCase(SessionCookieConfig.DYNAMIC_SECURE))) {
            throw new IllegalArgumentException();
        }
        _secure = secure;
    }

    /**
     * Return whether the cookie is to be marked Secure or not.
     * @return "dynamic", "true" or "false"
     */
    public String getSecure() {
        return _secure;
    }

    public void setHttpOnly(Boolean httpOnly) {
        _httpOnly = httpOnly;
    }

    public Boolean getHttpOnly() {
        return _httpOnly;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuilder sb = new StringBuilder("SessionCookieConfig[");
        if (_name != null) {
            sb.append("name=");
            sb.append(_name);
        }
        if (_path != null) {
            sb.append(", path=");
            sb.append(_path);
        }
        sb.append(", maxAge=");
        sb.append(_maxAge);
        if (_domain != null) {
            sb.append(", domain=");
            sb.append(_domain);
        }
        if (_comment != null) {
            sb.append(", comment=");
            sb.append(_comment);
        }
        sb.append(", secure=");
        sb.append(_secure);
        if (_httpOnly != null) {
            sb.append(", httpOnly=");
            sb.append(_httpOnly);
        }
        sb.append("]");
        return (sb.toString());

    }
}
