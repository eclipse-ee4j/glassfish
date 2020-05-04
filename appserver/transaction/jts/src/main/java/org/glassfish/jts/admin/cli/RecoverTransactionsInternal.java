/*
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

package org.glassfish.jts.admin.cli;

import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import org.glassfish.hk2.api.PerLookup;

import com.sun.enterprise.transaction.api.ResourceRecoveryManager;
import com.sun.jts.CosTransactions.Configuration;

import java.util.logging.Level;

@Service(name = "_recover-transactions-internal")
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE})
@ExecuteOn(RuntimeType.INSTANCE)
@PerLookup
public class RecoverTransactionsInternal extends RecoverTransactionsBase implements AdminCommand {

    @Param(name="target", optional = false)
    String destinationServer;

    @Inject
    ResourceRecoveryManager recoveryManager;

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (_logger.isLoggable(Level.INFO)) {
            _logger.info("==> internal target: " + destinationServer + " ... server: " + serverToRecover);
        }

        if (Configuration.isDBLoggingEnabled() && !serverToRecover.equals(destinationServer)) {
            // This is important: need to pass instance name to the recovery
            // process via log dir for delegated recovery
            transactionLogDir = serverToRecover;
        }

        String error = validate(destinationServer, true);
        if (error != null) {
            report.setMessage(error);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            boolean result;
            if (!(serverToRecover.equals(destinationServer))) {
                result = recoveryManager.recoverIncompleteTx(true, transactionLogDir);
            } else {
                result = recoveryManager.recoverIncompleteTx(false, null);
            }

            if (_logger.isLoggable(Level.INFO)) {
                _logger.info("==> recovery completed successfuly: " + result);
            }

            if (result)
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            else
                throw new IllegalStateException();
        } catch (Exception e) {
            _logger.log(Level.WARNING, localStrings.getString("recover.transactions.failed"), e);
            report.setMessage(localStrings.getString("recover.transactions.failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

}
