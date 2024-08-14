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
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Update Password Alias Command
 *
 * Usage: update-password-alias [--terse=false] [--echo=false] [--interactive=true] [--host localhost] [--port 4848|4849]
 * [--secure | -s] [--user admin_user] [--passwordfile file_name] aliasname
 *
 * Result of the command is that: the entry of the form: aliasname=<password-encrypted-with-masterpassword> in
 * <domain-dir>/<domain-name>/config/domain-passwords file gets updated with the new alias password
 *
 * domain.xml example entry is:
 * <provider-config class-name="com.sun.xml.wss.provider.ClientSecurityAuthModule" provider-id="XWS_ClientProvider" provider-type
 * ="client"> <property name="password" value="${ALIAS=myalias}/> </provider-config>
 *
 * @author Nandini Ektare
 */

@Service(name = "update-password-alias")
@PerLookup
@I18n("update.password.alias")
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.DOMAIN })
@RestEndpoints({
    @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.POST, path = "update-password-alias", description = "update-password-alias") })
@AccessRequired(resource = "domain/passwordAliases/passwordAlias/$aliasName", action = "update")
public class UpdatePasswordAlias implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreatePasswordAlias.class);

    @Param(name = "aliasname", primary = true)
    private String aliasName;

    @Param(name = "aliaspassword", password = true)
    private String aliasPassword;

    @Inject
    private DomainScopedPasswordAliasStore domainPasswordAliasStore;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are parameter names and the values the
     * parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            if (!domainPasswordAliasStore.containsKey(aliasName)) {
                report.setMessage(localStrings.getLocalString("update.password.alias.notfound",
                    "Password alias for the alias {0} does not exist.", aliasName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            domainPasswordAliasStore.put(aliasName, aliasPassword.toCharArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            report.setMessage(localStrings.getLocalString("update.password.alias.fail", "Update of Password Alias {0} failed", aliasName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(ex);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        report.setMessage(localStrings.getLocalString("update.password.alias.success",
            "Encrypted password for the alias {0} updated successfully", aliasName));
    }
}
