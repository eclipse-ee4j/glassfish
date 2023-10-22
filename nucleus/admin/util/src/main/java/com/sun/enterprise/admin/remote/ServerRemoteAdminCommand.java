/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.remote;

import com.sun.enterprise.admin.util.AuthenticationInfo;
import com.sun.enterprise.admin.util.HttpConnectorAddress;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.config.serverbeans.SecureAdminInternalUser;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.security.store.DomainScopedPasswordAliasStore;

import java.net.URLConnection;
import java.util.logging.Logger;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * RemoteAdminCommand which is sent from a server (DAS or instance).
 * <p>
 * This class identifies the origin as a server (as opposed to a true admin client) for server-to-server authentication.
 *
 * @author Tim Quinn
 */
//TODO: Remove it
public class ServerRemoteAdminCommand extends RemoteAdminCommand {

    private final static String SSL_SOCKET_PROTOCOL = "TLS";

    private ServiceLocator habitat;

    private SecureAdmin secureAdmin;

    private ServerEnvironment serverEnv;

    private SSLUtils _sslUtils = null;

    private DomainScopedPasswordAliasStore domainPasswordAliasStore = null;

    public ServerRemoteAdminCommand(ServiceLocator habitat, String name, String host, int port, boolean secure, String user,
            String password, Logger logger) throws CommandException {
        super(name, host, port, secure, "admin", "".toCharArray(), logger);
        super.setOmitCache(true); //todo: [mmar] Remove after implementation CLI->ReST done
        completeInit(habitat);
    }

    private synchronized void completeInit(final ServiceLocator habitat) {
        this.habitat = habitat;
        final Domain domain = habitat.getService(Domain.class);
        secureAdmin = domain.getSecureAdmin();
        serverEnv = habitat.getService(ServerEnvironment.class);
        this.secure = SecureAdmin.isEnabled(secureAdmin);
        domainPasswordAliasStore = habitat.getService(DomainScopedPasswordAliasStore.class);
        setInteractive(false);
    }

    @Override
    protected synchronized HttpConnectorAddress getHttpConnectorAddress(String host, int port, boolean shouldUseSecure) {
        /*
         * Always use secure communication to another server process.
         * Return a connector address that uses a cert to authenticate this
         * process as a client only if a cert, rather than an admin username
         * and password, is used for process-to-process authentication.
         */
        try {
            final String certAlias = SecureAdmin.isUsingUsernamePasswordAuth(secureAdmin) ? null : getCertAlias();
            return new HttpConnectorAddress(host, port,
                    certAlias == null ? null : sslUtils().getAdminSocketFactory(certAlias, SSL_SOCKET_PROTOCOL));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected synchronized AuthenticationInfo authenticationInfo() {
        AuthenticationInfo result = null;
        if (SecureAdmin.isUsingUsernamePasswordAuth(secureAdmin)) {
            final SecureAdminInternalUser secureAdminInternalUser = SecureAdmin.secureAdminInternalUser(secureAdmin);
            if (secureAdminInternalUser != null) {
                try {
                    result = new AuthenticationInfo(secureAdminInternalUser.getUsername(),
                            domainPasswordAliasStore.get(secureAdminInternalUser.getPasswordAlias()));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return result;
    }

    /**
     * Adds the admin indicator header to the request so. Do this whether secure admin is enabled or not, because the
     * indicator is unique among domains to help make sure only processes in the same domain talk to each other.
     *
     * @param urlConnection
     */
    @Override
    protected synchronized void addAdditionalHeaders(final URLConnection urlConnection) {
        final String indicatorValue = SecureAdmin.configuredAdminIndicator(secureAdmin);
        if (indicatorValue != null) {
            urlConnection.setRequestProperty(SecureAdmin.ADMIN_INDICATOR_HEADER_NAME, indicatorValue);
        }
    }

    private synchronized String getCertAlias() {
        return (serverEnv.isDas() ? SecureAdmin.DASAlias(secureAdmin) : SecureAdmin.instanceAlias(secureAdmin));
    }

    private synchronized SSLUtils sslUtils() {
        if (_sslUtils == null) {
            _sslUtils = habitat.getService(SSLUtils.class);
        }
        return _sslUtils;
    }
}
