/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.common;

import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.UsernamePasswordStore;
import com.sun.enterprise.security.integration.AppServSecurityContext;

import java.security.Principal;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.security.common.UserNameAndPassword;

/**
 * This class represents the security context on the client side. For usage of the IIOP_CLIENT_PER_THREAD_FLAG flag, see
 * UsernamePasswordStore. When set to false, the volatile field sharedCsc is used to store the context.
 *
 * @see UsernamePasswordStore
 * @author Harpreet Singh
 *
 */
public final class ClientSecurityContext extends AbstractSecurityContext {

    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    public static final String IIOP_CLIENT_PER_THREAD_FLAG = "com.sun.appserv.iiopclient.perthreadauth";

    // Bug Id: 4787940
    private static final boolean isPerThreadAuth = Boolean.getBoolean(IIOP_CLIENT_PER_THREAD_FLAG);

    // either the thread local or shared version will be used
    private static ThreadLocal localCsc = isPerThreadAuth ? new ThreadLocal() : null;
    private static volatile ClientSecurityContext sharedCsc;

    /**
     * This creates a new ClientSecurityContext object.
     *
     * @param The name of the user.
     * @param The Credentials of the user.
     */
    public ClientSecurityContext(String userName, Subject s) {
        this.initiator = new UserNameAndPassword(userName);
        this.subject = s;
    }

    /**
     * This method gets the SecurityContext stored here. If using a per-thread authentication model, it gets the context from Thread
     * Local Store (TLS) of the current thread. If not using a per-thread authentication model, it gets the singleton context.
     *
     * @return The current Security Context stored here. It returns null if SecurityContext could not be found.
     */
    public static ClientSecurityContext getCurrent() {
        if (isPerThreadAuth) {
            return (ClientSecurityContext) localCsc.get();
        }

        return sharedCsc;
    }

    /**
     * This method sets the SecurityContext to be stored here.
     *
     * @param The Security Context that should be stored.
     */
    public static void setCurrent(ClientSecurityContext sc) {
        if (isPerThreadAuth) {
            localCsc.set(sc);
        } else {
            sharedCsc = sc;
        }
    }

    /**
     * This method returns the caller principal. This information may be redundant since the same information can be inferred by
     * inspecting the Credentials of the caller.
     *
     * @return The caller Principal.
     */
    @Override
    public Principal getCallerPrincipal() {
        return initiator;
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "ClientSecurityContext[ " + "Initiator: " + initiator + "Subject " + subject + " ]";
    }

    //added for CR:6620388
    public static boolean hasEmtpyCredentials(ClientSecurityContext clientSecurityContext) {
        if (clientSecurityContext == null) {
            return true;
        }

        Subject subject = clientSecurityContext.getSubject();
        if (subject == null) {
            return true;
        }

        if (subject.getPrincipals().isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public AppServSecurityContext newInstance(String userName, Subject subject, String realm) {
        //TODO:V3 ignoring realm in this case
        return new ClientSecurityContext(userName, subject);
    }

    @Override
    public AppServSecurityContext newInstance(String userName, Subject subject) {
        return new ClientSecurityContext(userName, subject);
    }

    @Override
    public void setCurrentSecurityContext(AppServSecurityContext context) {
        if (context instanceof ClientSecurityContext) {
            setCurrent((ClientSecurityContext) context);
            return;
        }
        throw new IllegalArgumentException("Expected ClientSecurityContext, found " + context);
    }

    @Override
    public AppServSecurityContext getCurrentSecurityContext() {
        return getCurrent();
    }

    @Override
    public void setUnauthenticatedSecurityContext() {
        throw new UnsupportedOperationException("Not supported yet in V3.");
    }

    @Override
    public void setSecurityContextWithPrincipal(Principal principal) {
        throw new UnsupportedOperationException("Not supported yet in V3.");
    }

}
