/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.appclient.integration;

import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.jvnet.hk2.annotations.Contract;

/**
 * The Interface usable by AppClient Container for configuring the Security Runtime.
 *
 * @author Kumar Jayanti
 */
@Contract
public interface AppClientSecurityInfo {

    public enum CredentialType {
        USERNAME_PASSWORD, CERTIFICATE, ALL
    }

    /**
     * Initialize Security Runtime for the AppContainer (Stores, SecurityManager, Jakarta Authentication etc)
     *
     * @param tServers the Appclient Configuration Object
     * @param handler the CallbackHandler
     * @param username the static username if any was configured
     * @param password the static password if any was configured
     * @Param isJWS set to true if it is Java WebStart client
     * @Param useGUIAuth flag when set to true indicates the use of GUI Authentication
     */
    void initializeSecurity(List<TargetServer> tServers, List<MessageSecurityConfig> msgSecConfigs, CallbackHandler handler, String username, char[] password, boolean isJWS, boolean useGUIAuth);

    /**
     * @param type the credential type
     * @return the integer encoding for this type
     */
    int getCredentialEncoding(CredentialType type);

    /**
     * Do a client login using the CredentialType
     *
     * @param credType
     * @return {@link Subject}
     */
    Subject doClientLogin(CredentialType credType);

    /**
     * Clears the Client's current Security Context.
     */
    void clearClientSecurityContext();

    /**
     * Check if the Login attempt was cancelled.
     *
     * @return boolean indicating whether the login attempt was cancelled.
     */
    boolean isLoginCancelled();
}
