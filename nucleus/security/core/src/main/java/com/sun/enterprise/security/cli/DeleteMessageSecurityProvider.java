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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.MessageSecurityConfig;
import com.sun.enterprise.config.serverbeans.ProviderConfig;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Delete Message Security Provider Command
 *
 * Usage: delete-message-security-provider --layer message_layer [--terse=false] [--echo=false] [--interactive=true] [--host
 * localhost] [--port 4848|4849] [--secure | -s] [--user admin_user] [--passwordfile file_name] [--target target(Defaultserver)]
 * provider_name
 *
 * @author Nandini Ektare
 */
@Service(name = "delete-message-security-provider")
@PerLookup
@I18n("delete.message.security.provider")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
public class DeleteMessageSecurityProvider implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteMessageSecurityProvider.class);

    @Param(name = "providername", primary = true)
    String providerId;

    // auth-layer can only be SOAP | HttpServlet
    @Param(name = "layer", defaultValue = "SOAP")
    String authLayer;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Domain domain;

    ProviderConfig thePC = null;

    @AccessRequired.To("delete")
    private MessageSecurityConfig msgSecCfg = null;

    private SecurityService secService;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.chooseConfig(domain, target, context.getActionReport());
        if (config == null) {
            return false;
        }
        secService = config.getSecurityService();
        msgSecCfg = CLIUtil.findMessageSecurityConfig(secService, authLayer);
        if (msgSecCfg == null) {
            final ActionReport report = context.getActionReport();
            report.setMessage(localStrings.getLocalString("delete.message.security.provider.confignotfound",
                "A Message security config does not exist for the layer {0}", authLayer));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        return true;
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

        List<ProviderConfig> pcs = msgSecCfg.getProviderConfig();
        for (ProviderConfig pc : pcs) {
            if (pc.getProviderId().equals(providerId)) {
                thePC = pc;
                try {
                    ConfigSupport.apply(new SingleConfigCode<MessageSecurityConfig>() {

                        @Override
                        public Object run(MessageSecurityConfig param) throws PropertyVetoException, TransactionFailure {

                            if ((param.getDefaultProvider() != null) && param.getDefaultProvider().equals(thePC.getProviderId())) {
                                param.setDefaultProvider(null);
                            }

                            if ((param.getDefaultClientProvider() != null)
                                && param.getDefaultClientProvider().equals(thePC.getProviderId())) {
                                param.setDefaultClientProvider(null);
                            }

                            param.getProviderConfig().remove(thePC);
                            return null;
                        }
                    }, msgSecCfg);
                } catch (TransactionFailure e) {
                    e.printStackTrace();
                    report.setMessage(localStrings.getLocalString("delete.message.security.provider.fail",
                        "Deletion of message security provider named {0} failed", providerId));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setFailureCause(e);
                    return;
                }
                /*report.setMessage(localStrings.getLocalString(
                    "delete.message.security.provider.success",
                    "Deletion of message security provider {0} completed " +
                    "successfully", providerId));*/
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                return;
            }
        }
    }
}
