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

import com.sun.enterprise.config.serverbeans.AuditModule;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.SecurityConfigListener;
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
import org.jvnet.hk2.config.types.Property;

/**
 * Create Audit Module Command
 *
 * Usage: create-audit-module --classname classnme [--terse=false] [--echo=false] [--interactive=true] [--host localhost] [--port
 * 4848|4849] [--secure | -s] [--user admin_user] [--passwordfile file_name] [--property (name=value) [:name=value]*] [--target
 * target(Default server)] audit_module_name
 *
 * domain.xml element example <audit-module classname="com.foo.security.Audit" name="AM">
 * <property name="auditOn" value="false"/> </audit-module>
 *
 * @author Nandini Ektare
 */

@Service(name = "create-audit-module")
@PerLookup
@I18n("create.audit.module")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
public class CreateAuditModule implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateAuditModule.class);

    @Param(name = "classname")
    String className;

    @Param(name = "auditmodulename", primary = true)
    String auditModuleName;

    @Param(optional = true, name = "property", separator = ':')
    java.util.Properties properties;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Domain domain;

    @Inject
    SecurityConfigListener securityConfigListener;

    @AccessRequired.NewChild(type = AuditModule.class)
    private SecurityService securityService = null;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        securityService = chooseSecurityService(context.getActionReport());
        return (securityService != null);
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

        // check if there exists an audit module by the specified name
        // if so return failure.
        List<AuditModule> ams = securityService.getAuditModule();
        for (AuditModule am : ams) {
            if (am.getName().equals(auditModuleName)) {
                report.setMessage(localStrings.getLocalString("create.audit.module.duplicatefound",
                    "AuditModule named {0} exists. " + "Cannot add duplicate AuditModule.", auditModuleName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        // No duplicate audit modules found. So add one.
        try {
            ConfigSupport.apply(new SingleConfigCode<SecurityService>() {

                @Override
                public Object run(SecurityService param) throws PropertyVetoException, TransactionFailure {
                    AuditModule newAuditModule = param.createChild(AuditModule.class);
                    populateAuditModuleElement(newAuditModule);
                    param.getAuditModule().add(newAuditModule);
                    return newAuditModule;
                }
            }, securityService);

        } catch (TransactionFailure e) {
            report
                .setMessage(localStrings.getLocalString("create.audit.module.fail", "Creation of AuditModule {0} failed", auditModuleName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        //report.setMessage(localStrings.getLocalString("create.audit.module.success",
        //    "Creation of AuditModule {0} completed successfully", auditModuleName));
    }

    private SecurityService chooseSecurityService(final ActionReport report) {
        config = CLIUtil.chooseConfig(domain, target, report);
        if (config == null) {
            return null;
        }
        return config.getSecurityService();
    }

    private void populateAuditModuleElement(AuditModule newAuditModule) throws PropertyVetoException, TransactionFailure {
        newAuditModule.setName(auditModuleName);
        newAuditModule.setClassname(className);
        if (properties != null) {
            for (Object propname : properties.keySet()) {
                Property newprop = newAuditModule.createChild(Property.class);
                newprop.setName((String) propname);
                newprop.setValue(properties.getProperty((String) propname));
                newAuditModule.getProperty().add(newprop);
            }
        }
    }
}
