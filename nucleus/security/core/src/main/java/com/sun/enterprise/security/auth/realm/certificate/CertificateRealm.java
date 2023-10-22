/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.auth.realm.certificate;

import static com.sun.enterprise.util.Utility.isEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;

import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.auth.login.DistinguishedPrincipalCredential;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.x500.X500Principal;
import org.glassfish.security.common.Group;
import org.jvnet.hk2.annotations.Service;

/**
 * Realm wrapper for supporting certificate authentication.
 *
 * <P>
 * The certificate realm provides the security-service functionality needed to process a client-cert authentication.
 * Since the SSL processing, and client certificate verification is done by NSS, no authentication is actually done by
 * this realm. It only serves the purpose of being registered as the certificate handler realm and to service group
 * membership requests during web container role checks.
 *
 * <P>
 * There is no JAAS LoginModule corresponding to the certificate realm, therefore this realm does not require the
 * jaas-context configuration parameter to be set. The purpose of a JAAS LoginModule is to implement the actual
 * authentication processing, which for the case of this certificate realm is already done by the time execution gets to
 * Java.
 *
 * <P>
 * The certificate realm needs the following properties in its configuration: None.
 *
 * <P>
 * The following optional attributes can also be specified:
 * <ul>
 * <li>assign-groups - A comma-separated list of group names which will be assigned to all users who present a
 * cryptographically valid certificate. Since groups are otherwise not supported by the cert realm, this allows grouping
 * cert users for convenience.
 * </ul>
 *
 */
@Service
public final class CertificateRealm extends Realm {

    // Descriptive string of the authentication type of this realm.
    public static final String AUTH_TYPE = "certificate";
    private List<String> defaultGroups = new LinkedList<>();

    /**
     * Returns a short (preferably less than fifteen characters) description of the kind of authentication which is
     * supported by this realm.
     *
     * @return Description of the kind of authentication that is directly supported by this realm.
     */
    @Override
    public String getAuthType() {
        return AUTH_TYPE;
    }

    /**
     * Initialize a realm with some properties. This can be used when instantiating realms from their descriptions. This
     * method is invoked from Realm during initialization.
     *
     * @param props Initialization parameters used by this realm.
     * @exception BadRealmException If the configuration parameters identify a corrupt realm.
     * @exception NoSuchRealmException If the configuration parameters specify a realm which doesn't exist.
     *
     */
    @Override
    protected void init(Properties props) throws BadRealmException, NoSuchRealmException {
        super.init(props);

        String[] groups = addAssignGroups(null);
        if (!isEmpty(groups)) {
            defaultGroups.addAll(asList(groups));
        }

        String jaasCtx = props.getProperty(JAAS_CONTEXT_PARAM);
        if (jaasCtx != null) {
            setProperty(JAAS_CONTEXT_PARAM, jaasCtx);
        }
    }

    /**
     * Complete authentication of certificate user.
     *
     * <P>
     * As noted, the certificate realm does not do the actual authentication (signature and cert chain validation) for the
     * user certificate, this is done earlier in NSS. This method simply sets up the security context for the user in order
     * to properly complete the authentication processing.
     *
     * <P>
     * If any groups have been assigned to cert-authenticated users through the assign-groups property these groups are
     * added to the security context for the current user.
     *
     * @param subject The Subject object for the authentication request.
     * @param principal The X500Principal object from the user certificate.
     *
     */
    public void authenticate(Subject subject, X500Principal principal) {
        String name = principal.getName();

        _logger.log(Level.FINE, () -> "Certificate realm setting up security context for: " + name);

        if (defaultGroups != null) {
            Set<Principal> principalSet = subject.getPrincipals();
            for (String groupName : defaultGroups) {
                principalSet.add(new Group(groupName));
            }
        }

        if (!subject.getPrincipals().isEmpty()) {
            subject.getPublicCredentials().add(new DistinguishedPrincipalCredential(principal));
        }

        SecurityContext.setCurrent(new SecurityContext(name, subject));
    }

    /**
     * Returns the name of all the groups that this user belongs to.
     *
     * @param username Name of the user in this realm whose group listing is needed.
     * @return Enumeration of group names (strings).
     * @exception InvalidOperationException thrown if the realm does not support this operation - e.g. Certificate realm
     * does not support this operation.
     *
     */
    @Override
    public Enumeration<String> getGroupNames(String username) throws NoSuchUserException, InvalidOperationException {
        // This is called during web container role check, not during
        // EJB container role checks... fix RI for consistency.

        // Groups for cert users is empty by default unless some assign-groups
        // property has been specified (see init()).
        return enumeration(defaultGroups);
    }

    /**
     * A <code>LoginModule</code> for <code>CertificateRealm</code> can instantiate and pass a
     * <code>AppContextCallback</code> to <code>handle</code> method of the passed <code>CallbackHandler</code> to retrieve
     * the application name information.
     */
    public final static class AppContextCallback implements Callback {
        private String moduleID;

        /**
         * Get the fully qualified module name. The module name consists of the application name (if not a singleton) followed
         * by a '#' and the name of the module.
         *
         * <p>
         *
         * @return the application name.
         */
        public String getModuleID() {
            return moduleID;
        }

        /**
         * Set the fully qualified module name. The module name consists of the application name (if not a singleton) followed
         * by a '#' and the name of the module.
         *
         */
        public void setModuleID(String moduleID) {
            this.moduleID = moduleID;
        }
    }

}
