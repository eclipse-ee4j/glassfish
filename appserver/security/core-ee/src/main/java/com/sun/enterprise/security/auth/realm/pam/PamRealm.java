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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.libpam.PAM;
import org.jvnet.libpam.PAMException;

import com.sun.appserv.security.AppservRealm;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;

/**
 * Realm wrapper for supporting PAM based authentication for all Unix machines. The PAM realm uses the Operating
 * System's PAM login mechanism to authenticate the applications with their OS usernames and passwords.
 *
 * @author Nithya Subramanian
 */

@Service
public final class PamRealm extends AppservRealm {

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

    @Override
    public Enumeration getGroupNames(String username) throws NoSuchUserException {
        try {
            Set<String> groupsSet = new PAM(PAM_SERVICE).getGroupsOfUser(username);
            return Collections.enumeration(groupsSet);
        } catch (PAMException ex) {
            Logger.getLogger(PamRealm.class.getName()).log(Level.SEVERE, "pam_exception_getgroupsofuser", ex);
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
