/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security;

import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.auth.login.DistinguishedPrincipalCredential;
import com.sun.enterprise.security.common.AbstractSecurityContext;
import com.sun.enterprise.security.common.AppservAccessController;
import com.sun.enterprise.security.integration.AppServSecurityContext;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.AuthPermission;
import javax.security.auth.Subject;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.UserNameAndPassword;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.SEVERE;

/**
 * This class that extends AbstractSecurityContext that gets stored in Thread Local Storage. If the current thread
 * creates child threads, the SecurityContext stored in the current thread is automatically propagated to the child
 * threads.
 *
 * This class is used on the server side to represent the security context.
 *
 * Class is a concept introduced in JDK1.0. Thread is a concept introduced in JDK1.0. Principal is a concept introduced
 * in JDK1.1. Thread Local Storage is a concept introduced in JDK1.2.
 *
 * @see java.lang.ThreadLocal
 * @see java.lang.InheritableThreadLocal
 *
 * @author Harish Prabandham
 * @author Harpreet Singh
 */
@Service
@PerLookup
public class SecurityContext extends AbstractSecurityContext {

    private static final long serialVersionUID = 1L;
    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    private static InheritableThreadLocal<SecurityContext> currentSecurityContext = new InheritableThreadLocal<>();
    private static SecurityContext defaultSecurityContext = generateDefaultSecurityContext();

    private static AuthPermission doAsPrivilegedPerm = new AuthPermission("doAsPrivileged");

    // Did the client log in as or did the server generate the context
    private boolean serverGeneratedSecurityContext;

    private final ThreadLocal<Principal> sessionPrincipal = new ThreadLocal<>();

    /*
     * This creates a new SecurityContext object. Note: that the docs for Subject state that the internal sets (eg. the
     * principal set) cannot be modified unless the caller has the modifyPrincipals AuthPermission. That said, there may be
     * some value to setting the Subject read only. Note: changing the principals in the embedded subject (after
     * construction will likely cause problem in the principal set keyed HashMaps of EJBSecurityManager.
     *
     * @param username The name of the user/caller principal.
     *
     * @param subject contains the authenticated principals and credential.
     */
    public SecurityContext(String userName, Subject subject) {
        Subject nonNullSubject = subject;
        if (nonNullSubject == null) {
            nonNullSubject = new Subject();
            _logger.warning(SecurityLoggerInfo.nullSubjectWarning);
        }

        this.initiator = new UserNameAndPassword(userName);
        final Subject finalSubject = nonNullSubject;
        PrivilegedAction<Subject> action = () -> {
            finalSubject.getPrincipals().add(initiator);
            return finalSubject;
        };

        this.subject = AppservAccessController.doPrivileged(action);
    }

    /**
     * Create a SecurityContext with given subject having DistinguishedPrincipalCredential. This is used for JMAC
     * environment.
     *
     * @param subject
     */
    public SecurityContext(Subject subject) {
        if (subject == null) {
            subject = new Subject();
            _logger.warning(SecurityLoggerInfo.nullSubjectWarning);
        }

        final Subject finalSubject = subject;
        this.subject = subject;
        this.initiator = AppservAccessController.doPrivileged(new PrivilegedAction<>() {
            @Override
            public Principal run() {
                Principal principal = null;

                for (Object publicCredential : finalSubject.getPublicCredentials()) {
                    if (publicCredential instanceof DistinguishedPrincipalCredential) {
                        DistinguishedPrincipalCredential distinguishedPrincipalCredential = (DistinguishedPrincipalCredential) publicCredential;
                        principal = distinguishedPrincipalCredential.getPrincipal();
                        break;
                    }
                }

                if (principal == null) {
                    for (Principal publicCredential : finalSubject.getPrincipals()) {
                        if (publicCredential instanceof DistinguishedPrincipalCredential) {
                            DistinguishedPrincipalCredential distinguishedPrincipalCredential = (DistinguishedPrincipalCredential) publicCredential;
                            principal = distinguishedPrincipalCredential.getPrincipal();
                            break;
                        }
                    }
                }

                // for old auth module
                if (principal == null) {
                    Iterator<Principal> prinIter = finalSubject.getPrincipals().iterator();
                    if (prinIter.hasNext()) {
                        principal = prinIter.next();
                    }
                }

                return principal;
            }
        });

        postConstruct();
    }

    private void initDefaultCallerPrincipal() {
        if (this.initiator == null) {
            this.initiator = getDefaultCallerPrincipal();
        }
    }

    public SecurityContext(String userName, Subject subject, String realm) {
        Subject nonNullSubject = subject;
        if (nonNullSubject == null) {
            nonNullSubject = new Subject();
            _logger.warning(SecurityLoggerInfo.nullSubjectWarning);
        }

        PrincipalGroupFactory factory = Globals.get(PrincipalGroupFactory.class);
        if (factory != null) {
            this.initiator = factory.getPrincipalInstance(userName, realm);
        }

        final Subject finalSubject = nonNullSubject;
        this.subject = AppservAccessController.doPrivileged(new PrivilegedAction<>() {
            @Override
            public Subject run() {
                finalSubject.getPrincipals().add(initiator);
                return finalSubject;
            }
        });
    }

    public SecurityContext() {
        _logger.log(FINE, "Default CTOR of SecurityContext called");
        this.subject = new Subject();

        // Delay assignment of caller principal until it is requested
        this.initiator = null;
        this.setServerGeneratedCredentials();

        // read only is only done for guest logins.
        AppservAccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                subject.setReadOnly();
                return null;
            }
        });
    }

    /**
     * Initialize the SecurityContext and handle the unauthenticated principal case
     */
    public static SecurityContext init() {
        SecurityContext securityContext = currentSecurityContext.get();
        if (securityContext == null) {
            // There is no current security context.
            securityContext = defaultSecurityContext;
        }

        return securityContext;
    }

    public static SecurityContext getDefaultSecurityContext() {
        // Unauthenticated Security Context.
        return defaultSecurityContext;
    }

    public static Subject getDefaultSubject() {
        // Subject of unauthenticated Security Context.
        return defaultSecurityContext.subject;
    }

    // Get caller principal of unauthenticated Security Context
    public static Principal getDefaultCallerPrincipal() {
        synchronized (SecurityContext.class) {
            if (defaultSecurityContext.initiator == null) {
                String guestUser = null;
                try {
                    guestUser = AppservAccessController.doPrivileged(new PrivilegedExceptionAction<>() {
                        @Override
                        public String run() throws Exception {
                            SecurityService securityService = SecurityServicesUtil.getInstance().getHabitat()
                                    .getService(SecurityService.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
                            if (securityService == null) {
                                return null;
                            }
                            return securityService.getDefaultPrincipal();
                        }
                    });
                } catch (Exception e) {
                    _logger.log(SEVERE, SecurityLoggerInfo.defaultUserLoginError, e);
                } finally {
                    if (guestUser == null) {
                        guestUser = "ANONYMOUS";
                    }
                }
                defaultSecurityContext.initiator = new UserNameAndPassword(guestUser);
            }
        }

        return defaultSecurityContext.initiator;
    }

    private static SecurityContext generateDefaultSecurityContext() {
        synchronized (SecurityContext.class) {
            try {
                return AppservAccessController.doPrivileged(new PrivilegedExceptionAction<>() {
                    @Override
                    public SecurityContext run() throws Exception {
                        return new SecurityContext();
                    }
                });
            } catch (Exception e) {
                _logger.log(SEVERE, SecurityLoggerInfo.defaultSecurityContextError, e);
            }
        }

        return null;
    }

    /**
     * No need to unmarshall the unauthenticated principal....
     */
    public static void reset(SecurityContext securityContext) {
        setCurrent(securityContext);
    }

    /**
     * This method gets the SecurityContext stored in the Thread Local Store (TLS) of the current thread.
     *
     * @return The current Security Context stored in TLS. It returns null if SecurityContext could not be found in the
     * current thread.
     */
    public static SecurityContext getCurrent() {
        SecurityContext securityContext = currentSecurityContext.get();
        if (securityContext == null) {
            securityContext = defaultSecurityContext;
        }

        return securityContext;
    }

    /**
     * This method sets the SecurityContext stored in the TLS.
     *
     * @param securityContext The Security Context that should be stored in TLS. This public static method needs to be protected such
     * that it can only be called by container code. Otherwise it can be called by application code to set its subject
     * (which the EJB security manager will use to create a domain combiner, and then everything the ejb does will be run as
     * the corresponding subject.
     */
    public static void setCurrent(SecurityContext securityContext) {
        if (securityContext == null || securityContext == defaultSecurityContext) {
            currentSecurityContext.set(securityContext);
            return;
        }

        SecurityContext current = currentSecurityContext.get();
        if (securityContext == current) {
            return;
        }

        boolean permitted = false;
        try {
            java.lang.SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                _logger.fine("permission check done to set SecurityContext");
                sm.checkPermission(doAsPrivilegedPerm);
            }
            permitted = true;
        } catch (java.lang.SecurityException se) {
            _logger.log(SEVERE, SecurityLoggerInfo.securityContextPermissionError, se);
        } catch (Throwable t) {
            _logger.log(SEVERE, SecurityLoggerInfo.securityContextUnexpectedError, t);
        }

        if (permitted) {
            currentSecurityContext.set(securityContext);
        } else {
            _logger.severe(SecurityLoggerInfo.securityContextNotChangedError);
        }
    }

    public static void setUnauthenticatedContext() {
        _logger.entering(SecurityContext.class.getName(), "setCurrentSecurityContext");
        currentSecurityContext.set(defaultSecurityContext);
    }

    public boolean didServerGenerateCredentials() {
        return serverGeneratedSecurityContext;
    }

    private void setServerGeneratedCredentials() {
        serverGeneratedSecurityContext = true;
    }

    /**
     * This method returns the caller principal. This information may be redundant since the same information can be
     * inferred by inspecting the Credentials of the caller.
     *
     * @return The caller Principal.
     */
    @Override
    public Principal getCallerPrincipal() {
        return this == defaultSecurityContext ? getDefaultCallerPrincipal() : initiator;
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "SecurityContext[Initiator: " + initiator + ", Subject " + subject + "]";
    }

    public Principal getSessionPrincipal() {
        return sessionPrincipal.get();
    }

    public void setSessionPrincipal(Principal sessionPrincipal) {
        if (sessionPrincipal != null) {
            this.sessionPrincipal.set(sessionPrincipal);
        } else {
            this.sessionPrincipal.remove();
        }
    }

    public Set<Principal> getPrincipalSet() {
        return subject.getPrincipals();
    }

    public void postConstruct() {
        initDefaultCallerPrincipal();
    }

    @Override
    public AppServSecurityContext newInstance(String userName, Subject subject, String realm) {
        if (_logger.isLoggable(FINER)) {
            _logger.entering(getClass().getName(), "newInstance", new Object[] { userName, subject, realm });
        }

        return new SecurityContext(userName, subject, realm);
    }

    @Override
    public AppServSecurityContext newInstance(String userName, Subject subject) {
        if (_logger.isLoggable(FINER)) {
            _logger.entering(getClass().getName(), "newInstance", new Object[] { userName, subject });
        }

        return new SecurityContext(userName, subject);
    }

    @Override
    public void setCurrentSecurityContext(AppServSecurityContext context) {
        if (_logger.isLoggable(FINER)) {
            _logger.entering(getClass().getName(), "setCurrentSecurityContext", context);
        }

        if (context == null) {
            setCurrent(null);
            return;
        }

        if (context instanceof SecurityContext) {
            setCurrent((SecurityContext) context);
            return;
        }

        throw new IllegalArgumentException("Expected SecurityContext, found " + context);
    }

    @Override
    public AppServSecurityContext getCurrentSecurityContext() {
        return getCurrent();
    }

    @Override
    public void setUnauthenticatedSecurityContext() {
        setUnauthenticatedContext();
    }

    @Override
    public void setSecurityContextWithPrincipal(Principal principal) {
        SecurityContext ctx = getSecurityContextForPrincipal(principal);
        setCurrent(ctx);
    }

    // Moved from J2EEInstanceListener.java
    private SecurityContext getSecurityContextForPrincipal(final Principal p) {
        if (p == null) {
            return null;
        } else if (p instanceof SecurityContextProxy) {
            return ((SecurityContextProxy) p).getSecurityContext();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<SecurityContext>() {
                @Override
                public SecurityContext run() {
                    Subject s = new Subject();
                    s.getPrincipals().add(p);
                    return new SecurityContext(p.getName(), s);
                }
            });
        }
    }
}
