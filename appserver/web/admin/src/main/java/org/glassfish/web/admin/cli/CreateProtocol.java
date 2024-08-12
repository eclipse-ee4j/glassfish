/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Command to create protocol element within network-config
 *
 * Sample Usage : create-protocol [--securityenabled true|false] protocol_name
 *
 * domain.xml element example
 *
 * <protocol name="http-listener-1"> <http max-connections="250" default-virtual-server="server" server-name="">
 * <file-cache enabled="false" /> </http> <ssl ssl3-enabled="false" cert-nickname="s1as" /> </protocol>
 *
 * @author Nandini Ektare
 */
@Service(name = "create-protocol")
@PerLookup
@I18n("create.protocol")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class CreateProtocol implements AdminCommand {
    @Param(name = "protocolname", primary = true)
    String protocolName;
    // TODO:
    // After v3 release, incorporate changes to CRUD <http/>, <port-unification/>
    // and <protocol-chain-instance-handler/>. As each has considerable number
    // of config options and no specific ids to co-relate, we may need to choose
    // the way create-ssl has been done. Grizzly team concurs on this proposal
    @Param(name = "securityenabled", alias="securityEnabled", optional = true, defaultValue = "false")
    Boolean securityEnabled = false;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    Domain domain;
    @Inject
    ServiceLocator services;

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and
     * the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            config = newConfig;
        }
        final ActionReport report = context.getActionReport();
        // check for duplicates
        NetworkConfig networkConfig = config.getNetworkConfig();
        Protocols protocols = networkConfig.getProtocols();
        for (Protocol protocol : protocols.getProtocol()) {
            if (protocolName != null &&
                protocolName.equalsIgnoreCase(protocol.getName())) {
                report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_PROTOCOL_FAIL_DUPLICATE), protocolName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
        // Add to the <network-config>
        try {
            create(protocols, protocolName, securityEnabled);
        } catch (TransactionFailure e) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_PROTOCOL_FAIL), protocolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        } catch (Exception e) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_PROTOCOL_FAIL), protocolName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

    }

    public static void create(final Protocols protocols, final String name, final Boolean securityEnabled)
        throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<Protocols>() {
            public Object run(Protocols param) throws TransactionFailure {
                Protocol newProtocol = param.createChild(Protocol.class);
                newProtocol.setName(name);
                newProtocol.setSecurityEnabled(securityEnabled == null ? null : securityEnabled.toString());
                param.getProtocol().add(newProtocol);
                return newProtocol;
            }
        }, protocols);
    }
}
