/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.auth.realm.pam;

import static java.util.Collections.enumeration;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.libpam.PAM;
import org.jvnet.libpam.PAMException;
import org.jvnet.libpam.UnixUser;

/**
 * Realm wrapper for supporting PAM based authentication for all Unix machines. The PAM realm uses the Operating
 * System's PAM login mechanism to authenticate the applications with their OS usernames and passwords.
 *
 * @author Nithya Subramanian
 */

@Service
public final class PamRealm extends Realm {

    // Descriptive string of the authentication type of this realm.
    public static final String AUTH_TYPE = "pam";

    // Default PAM stack service set to sshd - since it is present in all
    // OSx by default
    private static final String PAM_SERVICE = "sshd";

    /**
     * Initialize a realm with some properties. This can be used when instantiating realms from their descriptions. This
     * method may only be called a single time.
     *
     * @param props Initialization parameters used by this realm.
     * @exception BadRealmException If the configuration parameters identify a corrupt realm.
     * @exception NoSuchRealmException If the configuration parameters specify a realm which doesn't exist.
     *
     */
    @Override
    public synchronized void init(Properties props) throws BadRealmException, NoSuchRealmException {
        super.init(props);
        String jaasCtx = props.getProperty(JAAS_CONTEXT_PARAM);
        if (jaasCtx == null) {
            throw new BadRealmException("No jaas-context defined");
        }
        this.setProperty(JAAS_CONTEXT_PARAM, jaasCtx);
    }

    /**
     * @return Description of the kind of authentication that is directly supported by this realm.
     */
    @Override
    public String getAuthType() {
        return AUTH_TYPE;
    }

    public String[] authenticate(String username, String password) {
        UnixUser user = null;
        try {
            user = new PAM(getPamService()).authenticate(username, password);
        } catch (PAMException e) {
            _logger.log(SEVERE, "pam_exception_authenticate", e);
        }

        if (user == null) { // JAAS behavior
            return null;
        }

        _logger.log(FINE, () -> "PAM login succeeded for: " + username);

        // Get the groups from the libpam4j UnixUser class that has been returned after a successful authentication.

        String[] groups = null;
        Set<String> groupSet = user.getGroups();

        if (groupSet != null) {
            groups = new String[groupSet.size()];
            user.getGroups().toArray(groups);
        } else {
            // Empty group list, create a zero-length group list
            groups = new String[0];
        }

        return groups;
    }

    @Override
    public Enumeration<String> getGroupNames(String username) throws NoSuchUserException {
        try {
            return enumeration(new UnixUser(username).getGroups());
        } catch (PAMException ex) {
            Logger.getLogger(PamRealm.class.getName()).log(SEVERE, "pam_exception_getgroupsofuser", ex);
            return null;
        }
    }

    /**
     * This method retreives the PAM service stack to be used by the Realm class and Login Module uniformly
     *
     * @return String = Pam Service
     */
    public String getPamService() {
        return PAM_SERVICE;
    }
}
