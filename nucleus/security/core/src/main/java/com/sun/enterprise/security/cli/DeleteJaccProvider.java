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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JaccProvider;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.util.List;

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
 * Usage: delete-jacc-provider [--help] [--user admin_user] [--passwordfile file_name] [ --target target_name] jacc_provider_name
 *
 */
@Service(name = "delete-jacc-provider")
@PerLookup
@I18n("delete.jacc.provider")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
public class DeleteJaccProvider implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteJaccProvider.class);

    @Param(name = "jaccprovidername", primary = true)
    private String jaccprovider;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Domain domain;

    private SecurityService securityService;

    @AccessRequired.To("delete")
    private JaccProvider jprov;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        config = CLIUtil.chooseConfig(domain, target, report);
        if (config == null) {
            return false;
        }
        securityService = config.getSecurityService();
        jprov = CLIUtil.findJaccProvider(securityService, jaccprovider);
        if (jprov == null) {
            report
                .setMessage(localStrings.getLocalString("delete.jacc.provider.notfound", "JaccProvider named {0} not found", jaccprovider));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        if ("default".equals(jprov.getName()) || "simple".equals(jprov.getName())) {
            report.setMessage(localStrings.getLocalString("delete.jacc.provider.notallowed",
                "JaccProvider named {0} is a system provider and cannot be deleted", jaccprovider));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }

        return true;
    }

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        try {
            List<JaccProvider> jaccProviders = securityService.getJaccProvider();
            JaccProvider jprov = null;
            for (JaccProvider jaccProv : jaccProviders) {
                if (jaccProv.getName().equals(jaccprovider)) {
                    jprov = jaccProv;
                    break;
                }
            }

            final JaccProvider jaccprov = jprov;
            ConfigSupport.apply(new SingleConfigCode<SecurityService>() {
                @Override
                public Object run(SecurityService param) throws PropertyVetoException, TransactionFailure {
                    param.getJaccProvider().remove(jaccprov);
                    return null;
                }
            }, securityService);
        } catch (TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("delete.jacc.provider.fail", "Deletion of JaccProvider {0} failed", jaccprovider)
                + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

    }

}
