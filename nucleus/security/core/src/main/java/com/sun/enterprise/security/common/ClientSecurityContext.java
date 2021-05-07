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

package com.sun.enterprise.security.common;

import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.integration.AppServSecurityContext;
import java.security.Principal;
import javax.security.auth.Subject;

import org.glassfish.security.common.PrincipalImpl;
//V3:Comment import com.sun.enterprise.ServerConfiguration;

import java.util.logging.*;
import com.sun.logging.*;


/**
 * This class represents the security context on the client side.
 * For usage of the IIOP_CLIENT_PER_THREAD_FLAG flag, see
 * UsernamePasswordStore. When set to false, the volatile
 * field sharedCsc is used to store the context.
 *
 * @see UsernamePasswordStore
 * @author Harpreet Singh
 *
 */
public final class ClientSecurityContext extends AbstractSecurityContext {

    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    public static final String IIOP_CLIENT_PER_THREAD_FLAG =
        "com.sun.appserv.iiopclient.perthreadauth";

    // Bug Id: 4787940
    private static final boolean isPerThreadAuth =
            Boolean.getBoolean(IIOP_CLIENT_PER_THREAD_FLAG);

    // either the thread local or shared version will be used
    private static ThreadLocal localCsc =
        isPerThreadAuth ? new ThreadLocal() : null;
    private static volatile ClientSecurityContext sharedCsc;

    /**
     * This creates a new ClientSecurityContext object.
     * @param The name of the user.
     * @param The Credentials of the user.
     */
    public ClientSecurityContext(String userName,
                                 Subject s) {

        this.initiator = new PrincipalImpl(userName);
        this.subject = s ;
    }

    /**
     * Initialize the SecurityContext & handle the unauthenticated
     * principal case

    public static ClientSecurityContext init() {
        ClientSecurityContext sc = getCurrent();
        if (sc == null) { // there is no current security context
            // create a default one if
            sc = generateDefaultSecurityContext();
        }
        return sc;
    }*/

   /*
    private static ClientSecurityContext generateDefaultSecurityContext() {
        final String PRINCIPAL_NAME = "auth.default.principal.name";
        final String PRINCIPAL_PASS = "auth.default.principal.password";


        //ServerConfiguration config = ServerConfiguration.getConfiguration();
        //String username = config.getProperty(PRINCIPAL_NAME, "guest");
        //String password = config.getProperty(PRINCIPAL_PASS, "guest123");

        //Temporary hardcoding to make V3 code for WebProfile compile
        String username ="guest";
        char[] password = new char[]{'g','e','t','s','t','1','2','3'};
        synchronized (ClientSecurityContext.class) {
            // login & all that stuff..
            try {
                final Subject subject = new Subject();
                final PasswordCredential pc = new PasswordCredential(username,
                        password, "default");
                AppservAccessController.doPrivileged(new PrivilegedAction() {
                    public java.lang.Object run() {
                        subject.getPrivateCredentials().add(pc);
                        return null;
                    }
                });
                // we do not need to generate any credential as authorization
                // decisions are not being done on the appclient side.
                ClientSecurityContext defaultCSC =
                    new ClientSecurityContext(username, subject);
                setCurrent(defaultCSC);
                return defaultCSC;
            } catch(Exception e) {
                _logger.log(Level.SEVERE,
                            "java_security.gen_security_context", e);
                return null;
            }
        }
    }
    */

    /**
     * This method gets the SecurityContext stored here.  If using a
     * per-thread authentication model, it gets the context from
     * Thread Local Store (TLS) of the current thread. If not using a
     * per-thread authentication model, it gets the singleton context.
     *
     * @return The current Security Context stored here. It returns
     *      null if SecurityContext could not be found.
     */
    public static ClientSecurityContext getCurrent() {
        if (isPerThreadAuth) {
            return (ClientSecurityContext) localCsc.get();
        } else {
            return sharedCsc;
        }
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
     * This method returns the caller principal.
     * This information may be redundant since the same information
     * can be inferred by inspecting the Credentials of the caller.
     *
     * @return The caller Principal.
     */
    public Principal getCallerPrincipal() {
        return initiator;
    }


    public Subject getSubject() {
        return subject;
    }

    public String toString() {
        return "ClientSecurityContext[ " + "Initiator: " + initiator +
            "Subject " + subject + " ]";
    }

    //added for CR:6620388
    public static boolean hasEmtpyCredentials(ClientSecurityContext sc) {
        if (sc == null) {
            return true;
        }
        Subject s = sc.getSubject();
        if (s == null) {
            return true;
        }
        if (s.getPrincipals().isEmpty()) {
            return true;
        }
        return false;
    }

    public AppServSecurityContext newInstance(String userName, Subject subject, String realm) {
        //TODO:V3 ignoring realm in this case
        return new ClientSecurityContext(userName, subject);
    }

    public AppServSecurityContext newInstance(String userName, Subject subject) {
        return new ClientSecurityContext(userName, subject);
    }

    public void setCurrentSecurityContext(AppServSecurityContext context) {
        if (context instanceof ClientSecurityContext) {
            setCurrent((ClientSecurityContext)context);
            return;
        }
        throw new IllegalArgumentException("Expected ClientSecurityContext, found " + context);
    }

    public AppServSecurityContext getCurrentSecurityContext() {
         return getCurrent();
    }

    public void setUnauthenticatedSecurityContext() {
        throw new UnsupportedOperationException("Not supported yet in V3.");
    }

    public void setSecurityContextWithPrincipal(Principal principal) {
        throw new UnsupportedOperationException("Not supported yet in V3.");
    }


}







