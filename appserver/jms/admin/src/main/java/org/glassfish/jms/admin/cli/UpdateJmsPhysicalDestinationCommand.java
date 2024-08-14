/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Properties;
import java.util.logging.Level;

import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author jasonlee
 */
@Service(name = "__update-jmsdest")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.POST,
        path="__update-jmsdest",
        description="Update JMS Destination",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="__update-jmsdest",
        description="Update JMS Destination",
        params={
            @RestParam(name="target", value="$parent")
        })
})
public class UpdateJmsPhysicalDestinationCommand extends JMSDestination implements AdminCommand {
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(UpdateJmsPhysicalDestinationCommand.class);

    @Param(name = "desttype", shortName = "t", optional = false)
    String destType;

    @Param(name = "dest_name", primary = true)
    String destName;

    @Param(name = "property", optional = true, separator = ':')
    Properties props;

    @Param(optional = true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Inject
    com.sun.appserv.connectors.internal.api.ConnectorRuntime connectorRuntime;

    @Inject
    Domain domain;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    ServerContext serverContext;

    // com.sun.messaging.jms.server:type=Destination,subtype=Config,desttype=destinationType,name=destinationName
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        logger.entering(getClass().getName(), "__updateJmsPhysicalDestination", new Object[]{destName, destType});

        try {
            validateJMSDestName(destName);
            validateJMSDestType(destType);

            updateJMSDestination();
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
    }

    protected void updateJMSDestination() throws Exception {
        logger.log(Level.FINE, "updateJMSDestination ...");
        try (MQJMXConnectorInfo mqInfo = createMQJMXConnectorInfo(target, config, serverContext, domain, connectorRuntime)) {
            final MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
            final AttributeList destAttrs = props == null ? null : convertProp2Attrs(props);
            if (destType.equalsIgnoreCase("topic")) {
                destType = DESTINATION_TYPE_TOPIC;
            } else if (destType.equalsIgnoreCase("queue")) {
                destType = DESTINATION_TYPE_QUEUE;
            }
            ObjectName on = new ObjectName(MBEAN_DOMAIN_NAME + ":type=Destination,subtype=Config,desttype=" + destType +",name=\"" + destName + "\"");
            mbsc.setAttributes(on, destAttrs);
        } catch (Exception e) {
            throw logAndHandleException(e, "admin.mbeans.rmb.error_updating_jms_dest");
        }
    }
}
