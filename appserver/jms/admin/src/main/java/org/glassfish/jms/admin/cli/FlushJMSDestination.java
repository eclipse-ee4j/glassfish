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
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.connectors.jms.util.JmsRaUtil;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;


/**
 * Flush JMS Destination
 */
@Service(name="flush-jmsdest")
@PerLookup
@I18n("flush.jms.dest")
@org.glassfish.api.admin.ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.POST,
        path="flush-jmsdest",
        description="Flush",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="flush-jmsdest",
        description="Flush",
        params={
            @RestParam(name="target", value="$parent")
        })
})
public class FlushJMSDestination extends JMSDestination implements AdminCommand {

    private static final Logger logger = Logger.getLogger(LogUtils.JMS_ADMIN_LOGGER);
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(FlushJMSDestination.class);
    private static final String DESTINATION_CONFIG_DOMAIN_TYPE
        = MBEAN_DOMAIN_NAME
        + ":type=" + "Destination"
        + ",subtype=Config";

    @Param(name="destType", shortName="T", optional=false)
    String destType;

    @Param(name="dest_name", primary=true)
    String destName;

    @Param(optional=true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Inject
    com.sun.appserv.connectors.internal.api.ConnectorRuntime connectorRuntime;

    @Inject
    Domain domain;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    ServerContext serverContext;


    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        logger.entering(getClass().getName(), "flushJMSDestination", new Object[] {destName, destType});

        try{
            validateJMSDestName(destName);
            validateJMSDestType(destType);
        }catch (IllegalArgumentException e){
            report.setMessage(e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            flushJMSDestination(destName, destType, target);
        } catch (Exception e) {
            logger.throwing(getClass().getName(), "flushJMSDestination", e);
            report.setMessage(localStrings.getLocalString("flush.jms.dest.failed",
                "Flush JMS Destination failed", e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

    // delete-jmsdest
    private void flushJMSDestination(String destName, String destType, String tgtName)
        throws Exception {

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "FlushJMSDestination ...");
        }

        //MBeanServerConnection  mbsc = getMBeanServerConnection(tgtName);
        // check and use JMX
        try {
            CommandTarget ctarget = getTypeForTarget(target);
            if (ctarget == CommandTarget.CLUSTER || ctarget == CommandTarget.CLUSTERED_INSTANCE) {
                /* The MQ 4.1 JMX Apis do not clean up all
                 * the destintations in all the instances
                 * in a broker cluster, in other words, JMX
                 * operation purge is not cluster aware
                 * So we have to ensure that we purge each instance
                 * in the cluster one by one.
                 * If one of them fail just log and proceed, we will
                 * flag an error towards the end. Issue 6523135
                 * This works because we resolve the port numbers
                 * even for standalone instances in MQAddressList.
                 */
                Exception failure = new Exception("Purging failed.");
                final Cluster cluster;
                if (ctarget == CommandTarget.CLUSTER){
                    cluster = Globals.get(Domain.class).getClusterNamed(target);
                } else {
                    List<Cluster> clustersList = Globals.get(Domain.class).getClusters().getCluster();
                    cluster = JmsRaUtil.getClusterForServer(clustersList, target);
                }
                List<Server> servers = cluster.getInstances();
                for (Server server : servers) {
                    try {
                        purgeJMSDestination(destName, destType, server.getName());
                    } catch (Exception e) {
                        failure.addSuppressed(
                            new RuntimeException("Purging failed for server of this name: " + server.getName(), e));
                    }
                }
                if (failure.getSuppressed().length != 0) {
                    throw failure;
                }
            } else {
                purgeJMSDestination(destName, destType, tgtName);
            }

        } catch (Exception e) {
            throw handleException(e);
        }
    }


    public void purgeJMSDestination(String destName, String destType, String tgtName) throws Exception {
        logger.log(Level.FINE, "purgeJMSDestination ...");
        final MQJMXConnectorInfo mqInfo = createMQJMXConnectorInfo(target, config, serverContext, domain, connectorRuntime);
        if (mqInfo == null) {
            return;
        }
        try {
            MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
            if (destType.equalsIgnoreCase("topic")) {
                destType = DESTINATION_TYPE_TOPIC;
            } else if (destType.equalsIgnoreCase("queue")) {
                destType = DESTINATION_TYPE_QUEUE;
            }
            ObjectName on = createDestinationConfig(destType, destName);
            mbsc.invoke(on, "purge", null, null);
        } catch (Exception e) {
            throw logAndHandleException(e, "admin.mbeans.rmb.error_purging_jms_dest");
        } finally {
            try {
                mqInfo.close();
            } catch (Exception e) {
                throw handleException(e);
            }
        }
    }


    private ObjectName createDestinationConfig(String destinationType, String destinationName)
        throws MalformedObjectNameException, NullPointerException {
        String s = DESTINATION_CONFIG_DOMAIN_TYPE
            + ",desttype="
            + destinationType
            + ",name="
            + ObjectName.quote(destinationName);

        return new ObjectName(s);
    }
}
