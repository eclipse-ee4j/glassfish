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
import java.util.Properties;

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
import org.glassfish.security.services.config.AuthenticationService;
import org.glassfish.security.services.config.SecurityConfiguration;
import org.glassfish.security.services.config.SecurityConfigurations;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * General create security service command.
 */
@Service(name="_create-security-service")
@PerLookup
@ExecuteOn(RuntimeType.DAS)
@TargetType(CommandTarget.DAS)
public class CreateSecurityService implements AdminCommand, AdminCommandSecurity.Preauthorization {
    private static final String AUTHENTICATION = "authentication";

    @Param(optional = false)
    private String serviceType;

    @Param(defaultValue = "false", optional = true)
    private Boolean enableDefault;

    @Param(optional = true, separator = ':')
    private Properties configuration;

    @Param(primary = true)
    private String serviceName;

    @Inject
    private Domain domain;

    // Service configuration type and handler
    private Class<? extends SecurityConfiguration> clazzServiceType;
    private ServiceConfigHandler<? extends SecurityConfiguration> serviceConfigHandler;

    @AccessRequired.To("create")
    private SecurityConfigurations secConfigs;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        secConfigs = getSecurityConfigurations(report);
        return secConfigs != null;
    }

    /**
     * Execute the create-security-service admin command.
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        // Setup the service type and configuration handler
        if (AUTHENTICATION.equalsIgnoreCase(serviceType)) {
            clazzServiceType = AuthenticationService.class;
            serviceConfigHandler = new AuthenticationConfigHandler();
        }
        else {
            report.setMessage("Invalid security service type specified: " + serviceType);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // Add service configuration to the security configurations
        // TODO - Add validation logic required for base service configuration
        SecurityConfiguration config = null;
        try {
            config = (SecurityConfiguration) ConfigSupport.apply(new SingleConfigCode<SecurityConfigurations>() {
                @Override
                public Object run(SecurityConfigurations param) throws PropertyVetoException, TransactionFailure {
                    SecurityConfiguration svcConfig = param.createChild(clazzServiceType);
                    svcConfig.setName(serviceName);
                    svcConfig.setDefault(enableDefault);
                    param.getSecurityServices().add(svcConfig);
                    return svcConfig;
                }
            }, secConfigs);
        } catch (TransactionFailure transactionFailure) {
            report.setMessage("Unable to create security service: " + transactionFailure.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(transactionFailure);
            return;
        }

        // Configure the specific service type settings
        // TODO - Add validation logic required for specific service configuration
        if ((config != null) && (configuration != null) && (!configuration.isEmpty())) {
            serviceConfigHandler.setupConfiguration(report, config);
        }
    }

    /**
     * Base class for service type configuration handling
     */
    private abstract class ServiceConfigHandler<T extends SecurityConfiguration> {
        abstract T setupConfiguration(ActionReport report, SecurityConfiguration securityServiceConfig);
    }

    /**
     * Handle the authentication service configuration
     */
    private class AuthenticationConfigHandler extends ServiceConfigHandler<AuthenticationService> {
        @Override
        public AuthenticationService setupConfiguration(ActionReport report, SecurityConfiguration securityServiceConfig) {
            // TODO - Additional type checking needed?
            AuthenticationService config = (AuthenticationService) securityServiceConfig;
            try {
                config = (AuthenticationService) ConfigSupport.apply(new SingleConfigCode<AuthenticationService>() {
                    @Override
                    public Object run(AuthenticationService param) throws PropertyVetoException, TransactionFailure {
                        // Look at the use password credential setting
                        Boolean usePassCred = Boolean.valueOf(configuration.getProperty("use-password-credential"));
                        param.setUsePasswordCredential(usePassCred.booleanValue());
                        return param;
                    }
                }, config);
            } catch (TransactionFailure transactionFailure) {
                report.setMessage("Unable to configure authentication service: " + transactionFailure.getMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(transactionFailure);
                return null;
            }

            // Return the updated configuration object
            return config;
        }
    }

    private SecurityConfigurations getSecurityConfigurations(final ActionReport report) {
        // Lookup or Create the security configurations
        SecurityConfigurations result = domain.getExtensionByType(SecurityConfigurations.class);
        if (result == null) {
            try {
                result = (SecurityConfigurations) ConfigSupport.apply(new SingleConfigCode<Domain>() {
                    @Override
                    public Object run(Domain wDomain) throws PropertyVetoException, TransactionFailure {
                        SecurityConfigurations s = wDomain.createChild(SecurityConfigurations.class);
                        wDomain.getExtensions().add(s);
                        return s;
                    }
                }, domain);
            } catch (TransactionFailure transactionFailure)  {
                report.setMessage("Unable to create security configurations: " + transactionFailure.getMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(transactionFailure);
            }
        }
        return result;
    }
}
