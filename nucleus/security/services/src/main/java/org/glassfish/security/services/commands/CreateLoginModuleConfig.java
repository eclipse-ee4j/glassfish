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
import org.glassfish.security.services.config.LoginModuleConfig;
import org.glassfish.security.services.config.SecurityProvider;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * General create LoginModule config command.
 */
@Service(name="_create-login-module-config")
@PerLookup
@ExecuteOn(RuntimeType.DAS)
@TargetType(CommandTarget.DAS)
public class CreateLoginModuleConfig implements AdminCommand, AdminCommandSecurity.Preauthorization {

    @Param(optional = false)
    private String serviceName;

    @Param(optional = false)
    private String providerName;

    @Param(optional = false)
    private String moduleClass;

    @Param(optional = false)
    private String controlFlag;

    @Param(optional = true, separator = ':')
    private Properties configuration;

    @Param(primary = true)
    private String name;

    @Inject
    private Domain domain;

    @AccessRequired.NewChild(type=LoginModuleConfig.class)
    private SecurityProvider provider;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        SecurityProvider provider = CLIUtil.findSecurityProvider(domain, serviceName, providerName, context.getActionReport());
        return (provider != null);
    }


    /**
     * Execute the create-login-module-config admin command.
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        // Add LoginModule configuration to the security provider setup
        // TODO - Add validation logic of the LoginModule config attributes
        LoginModuleConfig config = null;
        try {
            config = (LoginModuleConfig) ConfigSupport.apply(new SingleConfigCode<SecurityProvider>() {
                @Override
                public Object run(SecurityProvider param) throws PropertyVetoException, TransactionFailure {
                    LoginModuleConfig lmConfig = param.createChild(LoginModuleConfig.class);
                    lmConfig.setName(name);
                    lmConfig.setModuleClass(moduleClass);
                    lmConfig.setControlFlag(controlFlag);
                    // TODO - Should prevent multiple security provider config entries
                    param.getSecurityProviderConfig().add(lmConfig);
                    return lmConfig;
                }
            }, provider);
        } catch (TransactionFailure transactionFailure) {
            report.setMessage("Unable to create login module config: " + transactionFailure.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(transactionFailure);
            return;
        }

        // Setup LoginModule configuration options
        if ((config != null) && (configuration != null) && (!configuration.isEmpty())) {
            try {
                ConfigSupport.apply(new SingleConfigCode<LoginModuleConfig>() {
                    @Override
                    public Object run(LoginModuleConfig param) throws PropertyVetoException, TransactionFailure {
                        for (Object configPropName: configuration.keySet()) {
                            Property prop = param.createChild(Property.class);
                            String propName = (String) configPropName;
                            prop.setName(propName);
                            prop.setValue(configuration.getProperty(propName));
                            param.getProperty().add(prop);
                        }
                        return param;
                    }
                }, config);
            } catch (TransactionFailure transactionFailure) {
                report.setMessage("Unable to create login module options: " + transactionFailure.getMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(transactionFailure);
            }
        }
    }
}
