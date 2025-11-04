/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.SecurityConfigListener;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Auth Realm Command
 *
 * Usage: delete-auth-realm [--terse=false] [--echo=false] [--interactive=true] [--host localhost] [--port 4848|4849] [--secure |
 * -s] [--user admin_user] [--passwordfile file_name] [--target target(Default server)] auth_realm_name
 *
 * @author Nandini Ektare
 */
@Service(name = "delete-auth-realm")
@PerLookup
@I18n("delete.auth.realm")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
public class DeleteAuthRealm implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteAuthRealm.class);

    @Param(name = "authrealmname", primary = true)
    private String authRealmName;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Domain domain;
    // @Inject
    // private SecurityConfigListener securityListener;

    @AccessRequired.To("delete")
    AuthRealm authRealm = null;

    private SecurityService securityService;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.chooseConfig(domain, target, context.getActionReport());
        if (config == null) {
            return false;
        }
        securityService = config.getSecurityService();
        authRealm = findRealm();
        if (authRealm == null) {
            final ActionReport report = context.getActionReport();
            report.setMessage(localStrings.getLocalString("delete.auth.realm.notfound", "Authrealm named {0} not found", authRealmName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
        return (authRealm != null);
    }

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and the values the
     * parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        try {
            ConfigSupport.apply(new SingleConfigCode<SecurityService>() {
                @Override
                public Object run(SecurityService param) throws PropertyVetoException, TransactionFailure {
                    param.getAuthRealm().remove(authRealm);
                    //temporary fix - since the SecurityConfigListener is  not being called on an realm delete.
                    SecurityConfigListener.authRealmDeleted(authRealm);
                    return null;
                }
            }, securityService);
        } catch (TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("delete.auth.realm.fail", "Deletion of Authrealm {0} failed", authRealmName)
                + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private AuthRealm findRealm() {
        for (AuthRealm realm : securityService.getAuthRealm()) {
            if (realm.getName().equals(authRealmName)) {
                return realm;
            }
        }
        return null;
    }
}
