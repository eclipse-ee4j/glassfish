/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.common.util.admin;

import jakarta.inject.Singleton;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.Subject;

import org.jvnet.hk2.annotations.Service;

/**
 * Manages Rest Sessions.
 * @author Mitesh Meswani
 */
@Service
@Singleton
public class RestSessionManager {
    private final SecureRandom randomGenerator = new SecureRandom();
    private Map<String, SessionData> activeSessions = new ConcurrentHashMap<String, SessionData>(); //To guard against parallel mutation corrupting the map

    // package/private to help minimize the chances of someone instantiating this directly
    RestSessionManager() {
    }

    //TODO createSession is public. this package should not be exported
    public String createSession(String remoteAddr, final Subject subject, final int sessionTimeoutInMins) {
        String sessionId;
        do {
            sessionId = new BigInteger(130, randomGenerator).toString(16);
        } while(isSessionExist(sessionId));

        saveSession(sessionId, remoteAddr, subject, sessionTimeoutInMins);
        return sessionId;
    }

    public Subject authenticate(final String sessionId, final String remoteAddress) {
        Subject result = null;
        boolean authenticated = false;
        purgeInactiveSessions();

        if(sessionId != null) {
            SessionData sessionData = activeSessions.get(sessionId);
            if(sessionData != null) {
                authenticated = sessionData.authenticate(remoteAddress);
                if(authenticated) {
                    // update last access time
                    sessionData.updateLastAccessTime();
                    result = sessionData.subject();
                } else {
                    activeSessions.remove(sessionId);
                }
            }
        }
        return result;
    }

    /**
     * Deletes Session corresponding to given <code> sessionId </code>
     * @param sessionId
     * @return
     */
    public boolean deleteSession(String sessionId) {
        boolean sessionDeleted = false;
        if(sessionId != null) {
            SessionData removedSession = activeSessions.remove(sessionId);
            sessionDeleted = (removedSession != null);
        }
        return sessionDeleted;
    }

    private void saveSession(String sessionId, String remoteAddr, final Subject subject, final int sessionTimeoutInMins) {
        purgeInactiveSessions();
        activeSessions.put(sessionId, new SessionData(sessionId, remoteAddr, subject, sessionTimeoutInMins) );
    }

    private void purgeInactiveSessions() {
        Set<Map.Entry<String, SessionData>> activeSessionsSet = activeSessions.entrySet();
        for (Map.Entry<String, SessionData> entry : activeSessionsSet) {
            if (!entry.getValue().isSessionActive()) {
                activeSessionsSet.remove(entry);
            }
        }
    }

    private boolean isSessionExist(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    private static class SessionData{
        /** IP address of client as obtained from Grizzly request */
        private String clientAddress;
        private long lastAccessedTime = System.currentTimeMillis();
        private final long inactiveSessionLifeTime;
        private final static String DISABLE_REMOTE_ADDRESS_VALIDATION_PROPERTY_NAME = "org.glassfish.admin.rest.disable.remote.address.validation";
        private final boolean disableRemoteAddressValidation = Boolean.getBoolean(DISABLE_REMOTE_ADDRESS_VALIDATION_PROPERTY_NAME);
        private final Subject subject;

        public SessionData(String sessionId, String remoteAddr, final Subject subject, final int sessionTimeoutInMins) {
            this.clientAddress = remoteAddr;
            this.subject = subject;
            inactiveSessionLifeTime = sessionTimeoutInMins * 60000L; // minutes * 60 seconds * 1000 millis
        }

        /**
         * @return true if the session has not timed out. false otherwise
         */
        public boolean isSessionActive() {
            return lastAccessedTime + inactiveSessionLifeTime > System.currentTimeMillis();
        }

        /**
         * Update the last accessed time to current time
         */
        public void updateLastAccessTime() {
            lastAccessedTime = System.currentTimeMillis();
        }

        /**
         * @return true if session is still active and the request is from same remote address as the one session was
         * initiated from
         */
        public boolean authenticate(final String remoteAddress) {
            return isSessionActive() && (clientAddress.equals(remoteAddress) || disableRemoteAddressValidation );
        }

        /**
         * Returns the Subject associated with the session.
         * @return the Subject
         */
        public Subject subject() {
            return subject;
        }
    }
}
