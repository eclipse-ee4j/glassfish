/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.jts.admin.cli;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.Param;

import static com.sun.logging.LogDomains.TRANSACTION_LOGGER;

public class RecoverTransactionsBase {

    protected static final StringManager MESSAGES = StringManager.getManager(RecoverTransactionsBase.class);
    protected static final Logger LOG = LogDomains.getLogger(RecoverTransactionsBase.class, TRANSACTION_LOGGER, false);

    @Inject
    Servers servers;

    @Param(name = "transactionlogdir", optional = true)
    String transactionLogDir;

    @Param(name = "server_name", primary = true)
    String serverToRecover;

    String validate(String destinationServer, boolean validateAllParams) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("==> validating target: " + destinationServer + " ... server: " + serverToRecover);
        }

        if (servers.getServer(serverToRecover) == null) {
            return MESSAGES.getString("recover.transactions.serverBeRecoveredIsNotKnown",
                    serverToRecover);
        }

        if (isServerRunning(serverToRecover)) {
            if (destinationServer != null && !serverToRecover.equals(destinationServer)) {
                return MESSAGES.getString(
                        "recover.transactions.runningServerBeRecoveredFromAnotherServer",
                        serverToRecover, destinationServer);
            }
            if (transactionLogDir != null) {
                return MESSAGES.getString(
                        "recover.transactions.logDirShouldNotBeSpecifiedForSelfRecovery");
            }
        } else if (destinationServer == null) {
            return MESSAGES.getString("recover.transactions.noDestinationServer");

        } else if (servers.getServer(destinationServer) == null) {
            return MESSAGES.getString("recover.transactions.DestinationServerIsNotKnown");

        } else if (!isServerRunning(destinationServer)) {
            return MESSAGES.getString("recover.transactions.destinationServerIsNotAlive",
                    destinationServer);

        } else if (validateAllParams && transactionLogDir == null) {
             return MESSAGES.getString("recover.transactions.logDirNotSpecifiedForDelegatedRecovery");
        }

        return null;
    }

    private boolean isServerRunning(String serverName) {
        for(Server server : servers.getServer()) {
            if(serverName.equals(server.getName())) {
                return server.isListeningOnAdminPort();
            }
        }
        return false;
    }

}
