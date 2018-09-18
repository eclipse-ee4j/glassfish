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

package com.sun.jaspic.config.helper;

import com.sun.jaspic.config.delegate.MessagePolicyDelegate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;

/**
 *
 * @author Ron Monzillo
 */
public abstract class AuthConfigHelper {

    String loggerName;
    EpochCarrier providerEpoch;
    long epoch;
    MessagePolicyDelegate mpDelegate;
    String layer;
    String appContext;
    CallbackHandler cbh;
    private ReentrantReadWriteLock instanceReadWriteLock = new ReentrantReadWriteLock();
    private Lock instanceReadLock = instanceReadWriteLock.readLock();
    private Lock instanceWriteLock = instanceReadWriteLock.writeLock();

    public AuthConfigHelper(String loggerName, EpochCarrier providerEpoch,
            MessagePolicyDelegate mpDelegate, String layer, String appContext,
            CallbackHandler cbh) throws AuthException {

        this.loggerName = loggerName;
        this.providerEpoch = providerEpoch;
        this.mpDelegate = mpDelegate;
        this.layer = layer;
        this.appContext = appContext;
        this.cbh = cbh;
        initialize();
    }

    private void initialize() throws AuthException {
        instanceWriteLock.lock();
        try {
            this.epoch = providerEpoch.getEpoch();
            initializeContextMap();
        } finally {
            instanceWriteLock.unlock();
        }
    }

    private void doRefreshIfNeeded() {
        boolean hasChanged = false;
        instanceReadLock.lock();
        try {
            hasChanged = providerEpoch.hasChanged(epoch);
        } finally {
            instanceReadLock.unlock();
        }
        if (hasChanged) {
            refresh();
        }
    }

    private Integer getHashCode(Map properties) {
        if (properties == null) {
            return  Integer.valueOf("0");
        }
        return Integer.valueOf(properties.hashCode());
    }

    private <M> M getContextFromMap(HashMap<String, HashMap<Integer, M>> contextMap,
            String authContextID, Map properties) {
        M rvalue = null;
        HashMap<Integer, M> internalMap = contextMap.get(authContextID);
        if (internalMap != null) {
            rvalue = internalMap.get(getHashCode(properties));
        }
        if (rvalue != null) {
            if (isLoggable(Level.FINE)) {
                logIfLevel(Level.FINE, null, "AuthContextID found in Map: ", authContextID);
            }
        }
        return rvalue;
    }

    protected final <M> M getContext(
            HashMap<String, HashMap<Integer, M>> contextMap, String authContextID,
            Subject subject, Map properties) throws AuthException {

        M rvalue = null;

        doRefreshIfNeeded();
        instanceReadLock.lock();
        try {
            rvalue = getContextFromMap(contextMap, authContextID, properties);
            if (rvalue != null) {
                return rvalue;
            }
        } finally {
            instanceReadLock.unlock();
        }

        instanceWriteLock.lock();
        try {
            rvalue = getContextFromMap(contextMap, authContextID, properties);
            if (rvalue == null) {

                rvalue = (M) createAuthContext(authContextID, properties);

                HashMap<Integer, M> internalMap = contextMap.get(authContextID);
                if (internalMap == null) {
                    internalMap = new HashMap<Integer, M>();
                    contextMap.put(authContextID, internalMap);
                }

                internalMap.put(getHashCode(properties), rvalue);
            }
            return rvalue;
        } finally {
            instanceWriteLock.unlock();
        }
    }

    protected boolean isLoggable(Level level) {
        Logger logger = Logger.getLogger(loggerName);
        return logger.isLoggable(level);
    }

    protected void logIfLevel(Level level, Throwable t, String... msgParts) {
        Logger logger = Logger.getLogger(loggerName);
        if (logger.isLoggable(level)) {
          StringBuffer msgB = new StringBuffer("");
            for (String m : msgParts) {
                msgB.append(m);
            }
            String msg = msgB.toString();
            if ( !msg.isEmpty() && t != null) {
                logger.log(level, msg, t);
            } else if (!msg.isEmpty()) {
                logger.log(level, msg);
            }
        }
    }

    protected void checkMessageTypes(Class[] supportedMessageTypes) throws AuthException {
        Class[] requiredMessageTypes = mpDelegate.getMessageTypes();
        for (Class requiredType : requiredMessageTypes) {
            boolean supported = false;
            for (Class supportedType : supportedMessageTypes) {
                if (requiredType.isAssignableFrom(supportedType)) {
                    supported = true;
                }
            }
            if (!supported) {
                throw new AuthException("module does not support message type: " + requiredType.getName());
            }
        }
    }

    /**
     *  Only called from initialize (while lock is held).
     */
    protected abstract void initializeContextMap();

    protected abstract <M> M createAuthContext(final String authContextID,
            final Map properties) throws AuthException;

    public String getAppContext() {
        return appContext;
    }

    public String getAuthContextID(MessageInfo messageInfo) {
        return mpDelegate.getAuthContextID(messageInfo);
    }

    public String getMessageLayer() {
        return layer;
    }

    public void refresh() {
        try {
            initialize();
        } catch (AuthException ae) {
            throw new RuntimeException(ae);
        }
    }
}
