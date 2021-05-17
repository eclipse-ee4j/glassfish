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

package org.glassfish.jts.admin.cli;

import jakarta.inject.Inject;

import org.glassfish.api.Param;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;
import java.util.logging.Level;

public class RecoverTransactionsBase {

    static StringManager localStrings =
            StringManager.getManager(RecoverTransactionsBase.class);

    static Logger _logger = LogDomains.getLogger(RecoverTransactionsBase.class,
            LogDomains.TRANSACTION_LOGGER);

    @Inject
    Servers servers;

    @Param(name = "transactionlogdir", optional = true)
    String transactionLogDir;

    @Param(name = "server_name", primary = true)
    String serverToRecover;

    String validate(String destinationServer, boolean validateAllParams) {
        if (_logger.isLoggable(Level.INFO)) {
            _logger.info("==> validating target: " + destinationServer + " ... server: " + serverToRecover);
        }

        if (servers.getServer(serverToRecover) == null) {
            return localStrings.getString("recover.transactions.serverBeRecoveredIsNotKnown",
                    serverToRecover);
        }

        if (isServerRunning(serverToRecover)) {
            if (destinationServer != null && !serverToRecover.equals(destinationServer)) {
                return localStrings.getString(
                        "recover.transactions.runningServerBeRecoveredFromAnotherServer",
                        serverToRecover, destinationServer);
            }
            if (transactionLogDir != null) {
                return localStrings.getString(
                        "recover.transactions.logDirShouldNotBeSpecifiedForSelfRecovery");
            }
        } else if (destinationServer == null) {
            return localStrings.getString("recover.transactions.noDestinationServer");

        } else if (servers.getServer(destinationServer) == null) {
            return localStrings.getString("recover.transactions.DestinationServerIsNotKnown");

        } else if (!isServerRunning(destinationServer)) {
            return localStrings.getString("recover.transactions.destinationServerIsNotAlive",
                    destinationServer);

        } else if (validateAllParams && transactionLogDir == null) {
             return localStrings.getString("recover.transactions.logDirNotSpecifiedForDelegatedRecovery");
        }

        return null;
    }

    private boolean isServerRunning(String serverName) {
        boolean rs = false;
        for(Server server : servers.getServer()) {
            if(serverName.equals(server.getName())) {
                rs = server.isRunning();
                break;
            }
        }

        return rs;
    }

}
