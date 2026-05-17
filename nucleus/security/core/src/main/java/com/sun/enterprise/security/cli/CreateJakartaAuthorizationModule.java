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
import com.sun.enterprise.config.serverbeans.JakartaAuthorizationModule;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

import static com.sun.enterprise.util.SystemPropertyConstants.DAS_SERVER_NAME;
import static org.glassfish.api.ActionReport.ExitCode.FAILURE;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;
import static org.glassfish.api.admin.RuntimeType.DAS;
import static org.glassfish.api.admin.RuntimeType.INSTANCE;
import static org.glassfish.api.admin.ServerEnvironment.DEFAULT_INSTANCE_NAME;
import static org.glassfish.config.support.CommandTarget.CLUSTER;
import static org.glassfish.config.support.CommandTarget.CONFIG;
import static org.glassfish.config.support.CommandTarget.STANDALONE_INSTANCE;

/**
 * Create Jakarta Authorization Module Command
 *
 * Usage: create-jacc-provider --policyconfigfactoryclass pc_factory_class --policyproviderclass pol_provider_class [--help]
 * [--user admin_user] [--passwordfile file_name] [ --property (name=value)[:name=value]*] [ --target target_name]
 * jacc_provider_name
 *
 *
 * domain.xml element example
 * <jacc-provider policy-provider="org.glassfish.exousia.modules.locked.SimplePolicyProvider" name="default"
 * policy-configuration-factory-provider="org.glassfish.exousia.modules.locked.SimplePolicyConfigurationFactory">
 * <property name="repository" value="${com.sun.aas.instanceRoot}/generated/policy" /> </jacc-provider>
 *
 */
@Service(name = "create-jacc-provider")
@PerLookup
@I18n("create.jacc.provider")
@ExecuteOn({ DAS, INSTANCE })
@TargetType({ CommandTarget.DAS, STANDALONE_INSTANCE, CLUSTER, CONFIG })
public class CreateJakartaAuthorizationModule implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJakartaAuthorizationModule.class);

    @Param(name = "policyconfigfactoryclass", alias = "policyConfigurationFactoryProvider")
    private String policyConfigurationFactoryClass;

    @Param(name = "policyproviderclass", alias = "policyProvider")
    private String policyClass;

    @Param(name = "jaccprovidername", primary = true)
    private String jakartaAuthorizationModuleName;

    @Param(optional = true, name = "property", separator = ':')
    private Properties properties;

    @Param(name = "target", optional = true, defaultValue = DAS_SERVER_NAME)
    private String target;

    @Inject
    @Named(DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Domain domain;

    @AccessRequired.NewChild(type = JakartaAuthorizationModule.class)
    private SecurityService securityService;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.chooseConfig(domain, target, context.getActionReport());
        if (config == null) {
            return false;
        }

        securityService = config.getSecurityService();

        JakartaAuthorizationModule jakartaAuthorizationProvider = CLIUtil.findJakartaAuthorizationProvider(securityService, jakartaAuthorizationModuleName);
        if (jakartaAuthorizationProvider != null) {
            ActionReport report = context.getActionReport();
            report.setMessage(
                localStrings.getLocalString("create.jacc.provider.duplicatefound",
                "JakartaAuthorizationModule named {0} exists. Cannot add duplicate JakartaAuthorizationModule.",
                jakartaAuthorizationModuleName));
            report.setActionExitCode(FAILURE);

            return false;
        }

        return true;
    }

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        // No duplicate authorization provider found. So add one.
        try {
            ConfigSupport.apply(param -> newAuthorizationProvider(param), securityService);

        } catch (TransactionFailure e) {
            report.setMessage(localStrings.getLocalString("create.auth.realm.fail", "Creation of Authrealm {0} failed", jakartaAuthorizationModuleName)
                + "  " + e.getLocalizedMessage());
            report.setActionExitCode(FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(SUCCESS);
    }

    JakartaAuthorizationModule newAuthorizationProvider(SecurityService securityService) throws PropertyVetoException, TransactionFailure {
        JakartaAuthorizationModule authorizationProvider = securityService.createChild(JakartaAuthorizationModule.class);
        authorizationProvider.setName(jakartaAuthorizationModuleName);
        authorizationProvider.setPolicyConfigurationFactoryClass(policyConfigurationFactoryClass);
        authorizationProvider.setPolicyClass(policyClass);
        securityService.getJakartaAuthorizationModule().add(authorizationProvider);

        return authorizationProvider;
    }

}
