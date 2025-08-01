/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Inject;

import java.util.logging.Level;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@Service(name = "recover-transactions")
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE})
@ExecuteOn(RuntimeType.DAS)
@PerLookup
@I18n("recover.transactions")
@RestEndpoints({
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="recover-transactions",
        description="Recover Transactions",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class RecoverTransactions extends RecoverTransactionsBase implements AdminCommand {

    @Param(name="target", alias = "destination", optional = true)
    String destinationServer;

    @Inject
    CommandRunner runner;

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("==> original target: " + destinationServer + " ... server: " + serverToRecover);
        }

        String error = validate(destinationServer, false);
        if (error != null) {
            LOG.log(Level.WARNING, MESSAGES.getString("recover.transactions.failed") + " " + error);
            report.setMessage(error);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        //here we are only if parameters consistent
        if(destinationServer==null) {
            destinationServer = serverToRecover;
        }

        try {
            boolean result;
            CommandInvocation inv = runner.getCommandInvocation(
                    "_recover-transactions-internal", report, context.getSubject());

            final ParameterMap parameters = new ParameterMap();
            parameters.add("target", destinationServer);
            parameters.add("DEFAULT", serverToRecover);
            parameters.add("transactionlogdir", transactionLogDir);

            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("==> calling _recover-transactions-internal with params: " + parameters);
            }

            inv.parameters(parameters).execute();

            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("==> _recover-transactions-internal returned with: " + report.getActionExitCode());
            }

            // Exit code is set by _recover-transactions-internal

        } catch (Exception e) {
            LOG.log(Level.WARNING, MESSAGES.getString("recover.transactions.failed"), e);
            report.setMessage(MESSAGES.getString("recover.transactions.failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
