/*
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

package com.sun.enterprise.security.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.security.store.DomainScopedPasswordAliasStore;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Delete Password Alias Command
 *
 * Usage: delete-password-alias [--terse=false] [--echo=false] [--interactive=true] [--host localhost] [--port 4848|4849]
 * [--secure | -s] [--user admin_user] [--passwordfile file_name] aliasname
 *
 * Result of the command is that: The entry of the form: aliasname=<password-encrypted-with-masterpassword> in
 * <domain-dir>/<domain-name>/config/domain-passwords file is removed
 *
 * domain.xml example entry is:
 * <provider-config class-name="com.sun.xml.wss.provider.ClientSecurityAuthModule" provider-id="XWS_ClientProvider" provider-type
 * ="client"> <property name="password" value="${ALIAS=myalias}/> </provider-config>
 *
 * @author Nandini Ektare
 */

@Service(name = "delete-password-alias")
@PerLookup
@I18n("delete.password.alias")
@ExecuteOn(RuntimeType.ALL)
@TargetType({ CommandTarget.DAS, CommandTarget.DOMAIN })
@RestEndpoints({ @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.POST, // TODO: Should be DELETE
    path = "delete-password-alias", description = "delete-password-alias") })

@AccessRequired(resource = "domain/passwordAliases/passwordAlias/$aliasName", action = "delete")
public class DeletePasswordAlias implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeletePasswordAlias.class);

    @Param(name = "aliasname", primary = true)
    private String aliasName;

    @Inject
    private DomainScopedPasswordAliasStore domainPasswordAliasStore;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        try {
            if (!domainPasswordAliasStore.containsKey(aliasName)) {
                report.setMessage(localStrings.getLocalString("delete.password.alias.notfound",
                    "Password alias for the alias {0} does not exist.", aliasName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        } catch (Exception ex) {
            reportFailure(context.getActionReport(), ex);
            return false;
        }
        return true;
    }

    /**
     * Executes the command with the command parameters passed as Properties where the keys are paramter names and the values the
     * parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            domainPasswordAliasStore.remove(aliasName);
        } catch (Exception ex) {
            ex.printStackTrace();
            reportFailure(report, ex);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        /*report.setMessage(localStrings.getLocalString(
            "delete.password.alias.success",
            "Password alias for the alias {0} deleted successfully",
            aliasName));*/
    }

    private void reportFailure(final ActionReport report, final Exception ex) {
        report.setMessage(localStrings.getLocalString("delete.password.alias.fail", "Deletion of Password Alias {0} failed", aliasName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        report.setFailureCause(ex);
    }
}
