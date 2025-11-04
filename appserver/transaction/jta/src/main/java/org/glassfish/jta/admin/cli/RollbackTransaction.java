/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jta.admin.cli;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.ActionReport.ExitCode.FAILURE;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;
import static org.glassfish.api.admin.RuntimeType.INSTANCE;
import static org.glassfish.config.support.CommandTarget.CLUSTERED_INSTANCE;
import static org.glassfish.config.support.CommandTarget.DAS;
import static org.glassfish.config.support.CommandTarget.STANDALONE_INSTANCE;

@Service(name = "rollback-transaction")
@TargetType({ DAS, STANDALONE_INSTANCE, CLUSTERED_INSTANCE })
@ExecuteOn(INSTANCE)
@PerLookup
@I18n("rollback.transaction")
public class RollbackTransaction implements AdminCommand {
    private static StringManager localStrings = StringManager.getManager(RollbackTransaction.class);

    @Param(optional = true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(name = "transaction_id")
    String txnId;

    @Inject
    JavaEETransactionManager transactionManager;

    /**
     * Executes the command
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            transactionManager.forceRollback(txnId);
            report.setActionExitCode(SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getString("rollback.transaction.failed"));
            report.setActionExitCode(FAILURE);
            report.setFailureCause(e);
        }
    }
}
