/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.commands;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.security.services.config.SecurityConfiguration;
import org.glassfish.security.services.config.SecurityProvider;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * General create security provider command.
 */
@Service(name="_create-security-provider")
@PerLookup
@ExecuteOn(RuntimeType.DAS)
@TargetType(CommandTarget.DAS)
public class CreateSecurityProvider implements AdminCommand, AdminCommandSecurity.Preauthorization {

    @Param(optional = false)
    private String serviceName;

    @Param(optional = false)
    private String providerName;

    @Param(optional = false)
    private String providerType;

    @Param(primary = true)
    private String name;

    @Inject
    private Domain domain;

    @AccessRequired.NewChild(type=SecurityProvider.class)
    private SecurityConfiguration securityServiceConfiguration;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        securityServiceConfiguration = CLIUtil.findSecurityConfiguration(domain,
            serviceName, context.getActionReport());
        return (securityServiceConfiguration != null);
    }

    /**
     * Execute the create-security-provider admin command.
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        // Add security provider configuration to the service
        // TODO - Add validation logic required for security provider attributes
        try {
            ConfigSupport.apply(new SingleConfigCode<SecurityConfiguration>() {
                @Override
                public Object run(SecurityConfiguration param) throws PropertyVetoException, TransactionFailure {
                    SecurityProvider providerConfig = param.createChild(SecurityProvider.class);
                    providerConfig.setName(name);
                    providerConfig.setType(providerType);
                    providerConfig.setProviderName(providerName);
                    param.getSecurityProviders().add(providerConfig);
                    return providerConfig;
                }
            }, securityServiceConfiguration);
        } catch (TransactionFailure transactionFailure) {
            report.setMessage("Unable to create security provider: " + transactionFailure.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(transactionFailure);
        }
    }


}
