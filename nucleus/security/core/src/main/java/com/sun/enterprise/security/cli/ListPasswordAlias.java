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

import java.util.Iterator;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * List Password Aliases Command
 *
 * Usage: list-password-aliases [--terse=false] [--echo=false] [--interactive=true] [--host localhost] [--port 4848|4849]
 * [--secure | -s] [--user admin_user] [--passwordfile file_name]
 *
 * Result of the command is that: <domain-dir>/<domain-name>/config/domain-passwords.p12 file gets appended with the entry of the
 * form: aliasname=<password encrypted with masterpassword>
 *
 * A user can use this aliased password now in setting passwords in domin.xml. Benefit is it is in NON-CLEAR-TEXT
 *
 * domain.xml example entry is:
 * <provider-config class-name="com.sun.xml.wss.provider.ClientSecurityAuthModule" provider-id="XWS_ClientProvider" provider-type
 * ="client"> <property name="password" value="${ALIAS=myalias}/> </provider-config>
 *
 * @author Nandini Ektare
 */

@Service(name = "list-password-aliases")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.password.alias")
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.DOMAIN })
@RestEndpoints({
    @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.GET, path = "list-password-aliases", description = "list-password-aliases") })
@AccessRequired(resource = "domain/passwordAliases", action = "read")
public class ListPasswordAlias implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListPasswordAlias.class);

    @Inject
    private DomainScopedPasswordAliasStore domainPasswordAliasStore;

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
            final Iterator<String> it = domainPasswordAliasStore.keys();

            if (!it.hasNext()) {
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                report.setMessage(localStrings.getLocalString("list.password.alias.nothingtolist", "Nothing to list"));
            }

            while (it.hasNext()) {
                ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(it.next());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            report.setMessage(localStrings.getLocalString("list.password.alias.fail", "Listing of Password Alias failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(ex);
            return;
        }
    }
}
