/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.connectors.jms.config.JmsHost;
import com.sun.enterprise.connectors.jms.config.JmsService;
import com.sun.enterprise.connectors.jms.util.JmsRaUtil;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;


/**
 * Change JMS Master Broker Command
 */
@Service(name="change-master-broker")
@PerLookup
@I18n("change.master.broker")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.POST,
        path="change-master-broker",
        description="change-master-broker")
})
public class ChangeMasterBrokerCommand extends JMSDestination implements AdminCommand {
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ChangeMasterBrokerCommand.class);
    // [usemasterbroker] [availability-enabled] [dbvendor] [dbuser] [dbpassword admin] [jdbcurl] [properties props] clusterName

    private enum BrokerStatusCode {
        BAD_REQUEST(400), NOT_ALLOWED(405), UNAVAILABLE(503), PRECONDITION_FAILED(412);

        private final int code;

        BrokerStatusCode(int c) {
            code = c;
        }

        public int getCode() {
            return code;
        }
    }

    @Param (primary=true)//(name="newmasterbroker", alias="nmb", optional=false)
    String newMasterBroker;

    //@Param(primary=true)
    //String clusterName;

    @Inject
    CommandRunner commandRunner;


    @Inject
    Domain domain;

    @Inject
    com.sun.appserv.connectors.internal.api.ConnectorRuntime connectorRuntime;

    @Inject
    ServerContext serverContext;
    Config config;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final String newMB = newMasterBroker;
        Server newMBServer = domain.getServerNamed(newMasterBroker);
        if (newMBServer == null) {
            report.setMessage(localStrings.getLocalString("change.master.broker.invalidServerName",
                            "Invalid server name specified. There is no server by this name"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        Cluster cluster = newMBServer.getCluster();//domain.getClusterNamed(clusterName);

        if (cluster == null) {
            report.setMessage(localStrings.getLocalString("change.master.broker.invalidClusterName",
                            "The server specified is not associated with a cluster. The server assocaited with the master broker has to be a part of the cluster"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }


        /*if(!cluster.getName().equals(newMBServer.getCluster().getName()))
        {
            report.setMessage(localStrings.getLocalString("configure.jms.cluster.invalidClusterName",
                            "{0} does not belong to the specified cluster", newMasterBroker));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        } */

        Nodes nodes = domain.getNodes();
        config = domain.getConfigNamed(cluster.getConfigRef());
        JmsService jmsservice = config.getExtensionByType(JmsService.class);
        Server oldMBServer = null;
        //If Master broker has been set previously using this command, use that master broker as the old MB instance
        //Else use the first configured instance in the cluster list
        if (jmsservice.getMasterBroker() != null) {
            oldMBServer = domain.getServerNamed(jmsservice.getMasterBroker());
        } else {
            List<Server> serverList = cluster.getInstances();
            //if(serverList == null || serverList.size() == 0){
            //report.setMessage(localStrings.getLocalString("change.master.broker.invalidCluster",
            //             "No servers configured in cluster {0}", clusterName));
            //report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            //return;
            //}
            oldMBServer = serverList.get(0);
        }

        String oldMasterBrokerPort = JmsRaUtil.getJMSPropertyValue(oldMBServer);
        if (oldMasterBrokerPort == null) {
            SystemProperty sp = config.getSystemProperty("JMS_PROVIDER_PORT");
            if (sp != null) {
                oldMasterBrokerPort = sp.getValue();
            }
        }
        if (oldMasterBrokerPort == null) {
            oldMasterBrokerPort = getDefaultJmsHost(jmsservice).getPort();
        }
        String oldMasterBrokerHost = nodes.getNode(oldMBServer.getNodeRef()).getNodeHost();

        String newMasterBrokerPort = JmsRaUtil.getJMSPropertyValue(newMBServer);
        if (newMasterBrokerPort == null) {
            newMasterBrokerPort = getDefaultJmsHost(jmsservice).getPort();
        }
        String newMasterBrokerHost = nodes.getNode(newMBServer.getNodeRef()).getNodeHost();

        String oldMasterBroker = oldMasterBrokerHost + ":" + oldMasterBrokerPort;
        String newMasterBroker = newMasterBrokerHost + ":" + newMasterBrokerPort;
        // System.out.println("1: IN deleteinstanceCheck supplimental oldMasterBroker = " + oldMasterBroker + " newmasterBroker " + newMasterBroker);
        try {
            CompositeData result = updateMasterBroker(oldMBServer.getName(), oldMasterBroker, newMasterBroker);
            boolean success = ((Boolean) result.get("Success")).booleanValue();
            if (!success) {
                int statusCode = ((Integer) result.get("StatusCode")).intValue();
                String detailMessage = (String) result.get("DetailMessage");
                String msg = " " + detailMessage;
                if (BrokerStatusCode.BAD_REQUEST.getCode() == statusCode || BrokerStatusCode.NOT_ALLOWED.getCode() == statusCode ||
                    BrokerStatusCode.UNAVAILABLE.getCode() == statusCode || BrokerStatusCode.PRECONDITION_FAILED.getCode() == statusCode) {
                    msg = localStrings.getLocalString("change.master.broker.errorMsg",
                        "{0}. But it didn't affect current master broker configuration.", msg);
                } else {
                    msg = msg + ". " + localStrings.getLocalString("change.master.broker.otherErrorMsg",
                        "The cluster should be shutdown and configured with the new master broker then restarts.");
                }

                report.setMessage(localStrings.getLocalString("change.master.broker.CannotChangeMB",
                    "Unable to change master broker.{0}", msg));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        } catch(Exception e){
            report.setMessage(localStrings.getLocalString("change.master.broker.CannotChangeMB",
                "Unable to change master broker because {0}", e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            /*String setCommandStr = cluster.getConfigRef() + "." + "jms-service" + "." +"master-Broker";
            ParameterMap parameters = new ParameterMap();
            parameters.set(setCommandStr, newMB );

            ActionReport subReport = report.addSubActionsReport();
            commandRunner.getCommandInvocation("set", subReport, context.getSubject()).parameters(parameters).execute();

              if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())){
                    report.setMessage(localStrings.getLocalString("create.jms.resource.cannotCreateConnectionPool",
                            "Unable to create connection pool."));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
              }*/

            ConfigSupport.apply(new SingleConfigCode<JmsService>() {
                @Override
                public Object run(JmsService param) throws PropertyVetoException, TransactionFailure {

                    param.setMasterBroker(newMB);
                    return param;
                }
            }, jmsservice);
        } catch(Exception tfe) {
            report.setMessage(localStrings.getLocalString("change.master.broker.fail",
                "Unable to update the domain.xml with the new master broker") +
                " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }
        report.setMessage(localStrings.getLocalString("change.master.broker.success",
            "Master broker change has executed successfully for Cluster {0}.", cluster.getName()));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private JmsHost getDefaultJmsHost(JmsService jmsService){
        JmsHost jmsHost = null;
        String defaultJmsHostName = jmsService.getDefaultJmsHost();
        List jmsHostsList = jmsService.getJmsHost();
        for (Object element : jmsHostsList) {
            JmsHost tmpJmsHost = (JmsHost) element;
            if (tmpJmsHost != null && tmpJmsHost.getName().equals(defaultJmsHostName)) {
                jmsHost = tmpJmsHost;
            }
        }
        return jmsHost;
    }

    private CompositeData updateMasterBroker(String serverName, String oldMasterBroker, String newMasterBroker) throws Exception {
        try (MQJMXConnectorInfo mqInfo = createMQJMXConnectorInfo(serverName, config,serverContext, domain, connectorRuntime)) {
            MBeanServerConnection mbsc = null;
            try {
                mbsc = mqInfo.getMQMBeanServerConnection();
            } catch (Exception e) {
                String emsg = localStrings.getLocalString(
                    "change.master.broker.cannotConnectOldMasterBroker",
                    "Unable to connect to the current master broker {0}. Likely reasons:"
                    + " the cluster might not be running, the server instance {0} associated with"
                    + " the current master broker or the current master broker might not be running."
                    + " Please check server logs.",
                    mqInfo.getASInstanceName()
                );
                throw handleException(new Exception(emsg, e));
            }
            ObjectName on = new ObjectName(CLUSTER_CONFIG_MBEAN_NAME);
            String[] signature = new String[] {"java.lang.String", "java.lang.String"};
            Object[] params = new Object [] {oldMasterBroker, newMasterBroker};
            return mbsc == null ? null : (CompositeData) mbsc.invoke(on, "changeMasterBroker", params, signature);
        } catch (Exception e) {
            throw logAndHandleException(e, e.getMessage());
        }
    }
}
