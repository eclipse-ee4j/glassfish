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

package org.apache.catalina.session;

import org.apache.catalina.LogFacade;
import org.apache.catalina.Session;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import jakarta.servlet.http.*;

/**
 * Session manager for cookie-based persistence, where cookies carry session state.
 *
 * With cookie-based persistence, only session attribute values of type String are supported.
 */

public class CookiePersistentManager extends StandardManager {

    private static final String LAST_ACCESSED_TIME = "lastAccessedTime=";
    private static final String MAX_INACTIVE_INTERVAL = "maxInactiveInterval=";
    private static final String IS_VALID = "isValid=";

    private final Set<String> sessionIds = new HashSet<String>();

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    // The name of the cookies that carry session state
    private String cookieName;

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    @Override
    public void add(Session session) {
        synchronized (sessionIds) {
            if (!sessionIds.add(session.getIdInternal())) {
                throw new IllegalArgumentException("Session with id " + session.getIdInternal() +
                        " already present");
            }
            int size = sessionIds.size();
            if (size > maxActive) {
                maxActive = size;
            }
        }
    }

    @Override
    public Session findSession(String id, HttpServletRequest request) throws IOException {
        if (cookieName == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String value = null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return parseSession(cookie.getValue(), request.getRequestedSessionId());
            }
        }
        return null;
    }

    @Override
    public void clearSessions() {
        synchronized (sessionIds) {
            sessionIds.clear();
        }
    }

    @Override
    public Session[] findSessions() {
        return null;
    }

    @Override
    public void remove(Session session) {
        synchronized (sessionIds) {
            sessionIds.remove(session.getIdInternal());
        }
    }

    @Override
    public Cookie toCookie(Session session) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(IS_VALID + session.isValid() + ';');
        sb.append(LAST_ACCESSED_TIME + session.getLastAccessedTime() + ';');
        sb.append(MAX_INACTIVE_INTERVAL + session.getMaxInactiveInterval() + ';');
        synchronized (session.getAttributes()) {
            Set<Map.Entry<String, Object>> entries = session.getAttributes().entrySet();
            int numElements = entries.size();
            int i = 0;
            for (Map.Entry<String,Object> entry : entries) {
                sb.append(entry.getKey() + "=" + entry.getValue());
                if (i++ < numElements-1) {
                    sb.append(',');
                }
            }
        }

        remove(session);

        return new Cookie(cookieName, sb.toString());
    }

    @Override
    public void checkSessionAttribute(String name, Object value) {
        if (!(value instanceof String)) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SET_SESSION_ATTRIBUTE_EXCEPTION), name);
            throw new IllegalArgumentException(msg);
        }
    }

    /*
     * Parses the given string into a session, and returns it.
     *
     * The given string is supposed to contain a session encoded using toCookie().
     */
    private Session parseSession(String value, String sessionId) throws IOException {
        String[] components = value.split(";");
        if (components.length != 4) {
            throw new IllegalArgumentException("Invalid session encoding");
        }

        StandardSession session = (StandardSession) createSession(sessionId);

        // 1st component: isValid
        int index = components[0].indexOf('=');
        if (index < 0) {
            throw new IllegalArgumentException("Missing separator for isValid");
        }
        session.setValid(Boolean.parseBoolean(components[0].substring(index+1, components[0].length())));

        // 2nd component: lastAccessedTime
        index = components[1].indexOf('=');
        if (index < 0) {
            throw new IllegalArgumentException("Missing separator for lastAccessedTime");
        }
        session.setLastAccessedTime(Long.parseLong(components[1].substring(index+1, components[1].length())));

        // 3rd component: maxInactiveInterval
        index = components[2].indexOf('=');
        if (index < 0) {
            throw new IllegalArgumentException("Missing separator for maxInactiveInterval");
        }
        session.setMaxInactiveInterval(Integer.parseInt(
                components[2].substring(index+1, components[2].length())));

        // 4th component: comma-separated sequence of name-value pairs
        String[] entries = components[3].split(",");
        for (String entry : entries) {
            index = entry.indexOf('=');
            if (index < 0) {
                throw new IllegalArgumentException("Missing session attribute key-value separator");
            }
            session.setAttribute(entry.substring(0, index), entry.substring(index+1, entry.length()));
        }

        return session;
    }
}
