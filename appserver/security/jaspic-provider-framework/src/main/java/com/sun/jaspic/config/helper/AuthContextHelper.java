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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;

/**
 *
 * @author Ron Monzillo
 */
public abstract class AuthContextHelper {

    String loggerName;
    private boolean returnNullContexts = false;

    // include this to force subclasses to call constructor with LoggerName
    private AuthContextHelper() {

    }

    protected AuthContextHelper(String loggerName, boolean returnNullContexts) {
        this.loggerName = loggerName;
        this.returnNullContexts = returnNullContexts;
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

    /**
     *
     * @param level
     * @return
     */
    protected Logger getLogger(Level level) {
        Logger rvalue = Logger.getLogger(loggerName);
        if (rvalue.isLoggable(level)) {
            return rvalue;
        }
        return null;
    }

    protected abstract void refresh();

    public boolean returnsNullContexts() {
        return returnNullContexts;
    }

    public <M> boolean isProtected(M[] template, String authContextID) throws AuthException {
        try {
            if (returnNullContexts) {
                return hasModules(template, authContextID);
            } else {
                return true;
            }
        } catch (AuthException ae) {
            throw new RuntimeException(ae);
        }
    }

    /**
     *
     * @param <M>
     * @param template
     * @param authContextID
     * @return
     * @throws AuthException
     */
    public abstract <M> boolean hasModules(M[] template,String authContextID) throws AuthException;

    /**
     *
     * @param <M>
     * @param template
     * @param authContextID
     * @return
     * @throws AuthException
     */
    public abstract <M> M[] getModules(M[] template,String authContextID) throws AuthException;

    /**
     *
     * @param i
     * @param properties
     * @return
     */
    public abstract Map<String, ?> getInitProperties(int i, Map<String, ?> properties);

    /**
     *
     * @param successValue
     * @param i
     * @param moduleStatus
     * @return
     */
    public abstract boolean exitContext(AuthStatus[] successValue,
            int i, AuthStatus moduleStatus);

    /**
     *
     * @param successValue
     * @param defaultFailStatus
     * @param status
     * @param position
     * @return
     */
    public abstract AuthStatus getReturnStatus(AuthStatus[] successValue,
            AuthStatus defaultFailStatus, AuthStatus[] status, int position);
}
