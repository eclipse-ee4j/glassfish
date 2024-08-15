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

import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * List Delete Jms Hosts command
 *
 */
@Service(name="delete-jms-host")
@PerLookup
@I18n("delete.jms.host")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=JmsHost.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-jms-host",
        description="Delete JMS Host",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class DeleteJMSHost implements AdminCommand {
        final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteJMSHost.class);

    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param(name="jms_host_name", primary=true)
    String jmsHostName;

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
        if (targetServer!=null) {
            config = domain.getConfigNamed(targetServer.getConfigRef());
        }
        com.sun.enterprise.config.serverbeans.Cluster cluster =domain.getClusterNamed(target);
        if (cluster!=null) {
            config = domain.getConfigNamed(cluster.getConfigRef());
        }

         if (jmsHostName == null) {
            report.setMessage(localStrings.getLocalString("delete.jms.host.noHostName",
                            "No JMS Host Name specified for JMS Host."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

            JmsService jmsService = config.getExtensionByType(JmsService.class);
           /* for (Config c : configs.getConfig()) {

               if(configRef.equals(c.getName()))
                     jmsService = c.getJmsService();
            }*/

            if (jmsService == null) {
            report.setMessage(localStrings.getLocalString("list.jms.host.invalidTarget",
                            "Invalid Target specified."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
            JmsHost jmsHost = null;
            for (JmsHost r : jmsService.getJmsHost()) {
                if(jmsHostName.equals(r.getName())){
                    jmsHost = r;
                    break;
                }
            }
           if (jmsHost == null) {
            report.setMessage(localStrings.getLocalString("list.jms.host.noJmsHostFound",
                            "JMS Host {0} does not exist.", jmsHostName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        final JmsHost jHost = jmsHost;
         try {
            ConfigSupport.apply(new SingleConfigCode<JmsService>() {
                public Object run(JmsService param) throws PropertyVetoException, TransactionFailure {
                    return param.getJmsHost().remove(jHost);
                }
            }, jmsService);
        } catch(TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("delete.jms.host.fail",
                            "Unable to delete jms host {0}.", jmsHostName) +
                            " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
