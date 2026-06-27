/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.web.security.realmadapter;

import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.auth.login.DistinguishedPrincipalCredential;
import com.sun.enterprise.security.auth.realm.certificate.CertificateRealm;
import com.sun.enterprise.security.ee.web.integration.WebPrincipal;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;

import org.glassfish.epicyro.config.helper.Caller;
import org.glassfish.epicyro.config.helper.CallerPrincipal;
import org.glassfish.security.common.UserNameAndPassword;

public class SubjectUtils {

    public static Caller getCaller(Subject subject) {
        Set<Caller> callers = subject.getPrincipals(Caller.class);
        if (callers.isEmpty()) {
            return null;
        }

        return callers.iterator().next();
    }

    public static Principal getGlassFishCallerPrincipal(Caller caller, String realmName) {
        Principal callerPrincipal = caller.getCallerPrincipal();

        // Check custom principal
        if (callerPrincipal instanceof CallerPrincipal == false) {
            return callerPrincipal;
        }

        // Check anonymous principal
        if (callerPrincipal.getName() == null) {
            return SecurityContext.getDefaultCallerPrincipal();
        }

        // Check certificate / X500 principal (this is oddly specific)
        if (CertificateRealm.AUTH_TYPE.equals(realmName)) {
            return new X500Principal(callerPrincipal.getName());
        }

        return new UserNameAndPassword(callerPrincipal.getName());
    }

    public static void copySubject(Subject target, Subject source) {
        target.getPrincipals().addAll(source.getPrincipals());
        target.getPublicCredentials().addAll(source.getPublicCredentials());
        target.getPrivateCredentials().addAll(source.getPrivateCredentials());
    }

    public static void toSubject(Subject subject, Principal principal) {
        subject.getPrincipals().add(principal);
    }

    public static void toSubjectCredential(Subject subject, Object credential) {
        subject.getPublicCredentials().add(credential);
    }

    public static Subject reuseSessionSubject(Caller caller) {
        Principal returnedPrincipal = findPrincipalWrapper(caller.getCallerPrincipal());

        if (returnedPrincipal instanceof WebPrincipal) {
            return reuseWebPrincipal((WebPrincipal) returnedPrincipal);
        }

        return null;
    }

    /**
     * See if we need to wrap back the principal.
     *
     * <p>
     * This situation occurs when according to the Jakarta Authentication "special move" the Principal
     * from the request is passed into the callback handler. This signals that a SAM wants to re-use
     * a previously saved authenticated identity.
     *
     *  <p>
     *  However, in GlassFish getting a Principal from the request will automatically unwrap it if a
     *  custom principal was used. Here we try to find the original wrapping principal, if any.
     *
     * @param principal
     * @return
     */
    private static Principal findPrincipalWrapper(Principal principal) {
        if (principal != null && !(principal instanceof WebPrincipal)) {

            // Get the top level session principal
            Principal sessionPrincipal = SecurityContext.getCurrent().getSessionPrincipal();

            // If it's the wrapper we're looking for, it must be of type WebPrincipal
            if (sessionPrincipal instanceof WebPrincipal webPrincipalFromSession) {

                // Check if the top level session principal is indeed wrapping our current principal
                if (webPrincipalFromSession.getCustomPrincipal() == principal) {

                    // Custom principal from wrapper is the same as our current principal, so
                    // this is the wrapper we're looking for.
                    return webPrincipalFromSession;
                }
            }
        }

        // Not wrapped, or wrapper could not be found
        return principal;
    }

    /**
     * This method will distinguish the initiator principal (of the SecurityContext obtained from the WebPrincipal) as the
     * caller principal, and copy all the other principals into the subject....
     *
     * It is assumed that the input WebPrincipal is coming from a SAM, and that it was created either by the SAM (as
     * described below) or by calls to the LoginContextDriver made by an Authenticator.
     *
     * A WebPrincipal constructed by the RealmAdapter will include a DistinguishedPrincipalCredential; other constructions may not; this method
     * interprets the absence of a DPC as evidence that the resulting WebPrincipal was not constructed by the RealmAdapter
     * as described below. Note that presence of a DistinguishedPrincipalCredential does not necessarily mean that the resulting WebPrincipal was
     * constructed by the RealmAdapter... since some authenticators also add the credential).
     *
     * A. handling of CPCB by CBH:
     *
     * 1. handling of CPC by CBH modifies subject a. constructs principalImpl if called by name b. uses LoginContextDriver
     * to add group principals for name c. puts principal in principal set, and DPC in public credentials
     *
     * B. construction of WebPrincipal by RealmAdapter (occurs after SAM uses CBH to set other than an unauthenticated
     * result in the subject:
     *
     * a. SecurityContext construction done with subject (returned by SAM). Construction sets initiator/caller principal
     * within SC from DistinguishedPrincipalCredential set by CBH in public credentials of subject
     *
     * b WebPrincipal is constructed with initiator principal and SecurityContext
     *
     * @param webPrincipal WebPrincipal
     *
     * @return true when Security Context has been obtained from webPrincipal, and CB is finished. returns false when more
     * CB processing is required.
     */
    private static Subject reuseWebPrincipal(final WebPrincipal webPrincipal) {

        SecurityContext securityContext = webPrincipal.getSecurityContext();
        final Subject securityContextSubject = securityContext != null ? securityContext.getSubject() : null;
        final Principal callerPrincipal = securityContext != null ? securityContext.getCallerPrincipal() : null;
        final Principal defaultPrincipal = SecurityContext.getDefaultCallerPrincipal();

        // This method uses 4 (numbered) criteria to determine if the argument WebPrincipal can be reused

        /**
         * 1. WebPrincipal must contain a SecurityContext and SC must have a non-null, non-default callerPrincipal and a Subject
         */
        if (callerPrincipal == null || callerPrincipal.equals(defaultPrincipal) || securityContextSubject == null) {
            return null;
        }

        boolean hasObject = false;
        Set<DistinguishedPrincipalCredential> distinguishedCreds = securityContextSubject.getPublicCredentials(DistinguishedPrincipalCredential.class);
        if (distinguishedCreds.size() == 1) {
            for (DistinguishedPrincipalCredential cred : distinguishedCreds) {
                if (cred.getPrincipal().equals(callerPrincipal)) {
                    hasObject = true;
                }

            }
        }

        if (!hasObject) {
            Set<DistinguishedPrincipalCredential> distinguishedPrincipals = securityContextSubject.getPrincipals(DistinguishedPrincipalCredential.class);
            if (distinguishedPrincipals.size() == 1) {
                for (DistinguishedPrincipalCredential cred : distinguishedPrincipals) {
                    if (cred.getPrincipal().equals(callerPrincipal)) {
                        hasObject = true;
                    }
                }
            }
        }

        /**
         * 2. Subject within SecurityContext must contain a single DistinguishedPrincipalCredential that identifies the Caller Principal
         */
        if (!hasObject) {
            return null;
        }

        hasObject = securityContextSubject.getPrincipals().contains(callerPrincipal);

        /**
         * 3. Subject within SecurityContext must contain the caller principal
         */
        if (!hasObject) {
            return null;
        }

        /**
         * 4. The webPrincipal must have a non null name that equals the name of the callerPrincipal.
         */
        if (webPrincipal.getName() == null || !webPrincipal.getName().equals(callerPrincipal.getName())) {
            return null;
        }

        return securityContextSubject;
    }

}
