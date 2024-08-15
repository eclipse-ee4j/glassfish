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

package org.glassfish.jms.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.connectors.jms.config.JmsHost;
import com.sun.enterprise.connectors.jms.config.JmsService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.ArrayList;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
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
 * List JMS Hosts command
 *
 */
@Service(name="list-jms-hosts")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.jms.hosts")
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=JmsService.class,
        opType=RestEndpoint.OpType.GET,
        path="list-jms-hosts",
        description="list-jms-hosts")
})
public class ListJMSHosts implements AdminCommand {
        final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListJMSHosts.class);

    @Param(name="target", optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    //@Inject(name = ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    Domain domain;
    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        Config targetConfig = domain.getConfigNamed(target);
                if (targetConfig != null)
                    config = targetConfig;

        Server targetServer = domain.getServerNamed(target);
        //String configRef = targetServer.getConfigRef();
        if (targetServer!=null) {
            config = domain.getConfigNamed(targetServer.getConfigRef());
        }
        com.sun.enterprise.config.serverbeans.Cluster cluster =domain.getClusterNamed(target);
        if (cluster!=null) {
            config = domain.getConfigNamed(cluster.getConfigRef());
        }

        JmsService jmsService = config.getExtensionByType(JmsService.class);
            /*for (Config c : configs.getConfig()) {
                if(configRef.equals(c.getName()))
                     jmsService = c.getJmsService();
            } */

            if (jmsService == null) {
            report.setMessage(localStrings.getLocalString("list.jms.host.invalidTarget",
                            "Invalid Target specified."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
          }
           try {
            ArrayList<String> list = new ArrayList();
            for (JmsHost r : jmsService.getJmsHost()) {
                list.add(r.getName());
            }

            for (String jmsName : list) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(jmsName);
            }
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.jms.host.fail",
                    "Unable to list JMS Hosts") + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
