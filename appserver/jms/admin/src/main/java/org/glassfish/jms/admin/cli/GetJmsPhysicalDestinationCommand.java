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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.internal.api.ServerContext;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 *
 * @author jasonlee
 */
@Service(name = "__get-jmsdest")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.GET,
        path="__get-jmsdest",
        description="Get JMS Destinations",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.GET,
        path="__get-jmsdest",
        description="Get JMS Destinations",
        params={
            @RestParam(name="target", value="$parent")
        })
})
public class GetJmsPhysicalDestinationCommand extends JMSDestination implements AdminCommand {
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(GetJmsPhysicalDestinationCommand.class);

    @Param(name = "desttype", shortName = "t", optional = false)
    String destType;

    @Param(name = "dest_name", primary = true)
    String destName;

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
        logger.entering(getClass().getName(), "__getJmsPhysicalDestination",
                new Object[]{destName, destType});

        try {
            validateJMSDestName(destName);
            validateJMSDestType(destType);

            Map entity = getJMSDestination();
            Properties ep = new Properties();
            ep.put("entity", entity);
            report.setExtraProperties(ep);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
    }

    protected Map<String, Object> getJMSDestination()
            throws Exception {

        logger.log(Level.FINE, "__getJmsPhysicalDestination ...");
        MQJMXConnectorInfo mqInfo = getMQJMXConnectorInfo(target, config, serverContext, domain, connectorRuntime);
        Map<String, Object> destAttrs = new HashMap<String, Object>();

        try {
            MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();

            if (destType.equalsIgnoreCase("topic")) {
                destType = DESTINATION_TYPE_TOPIC;
            } else if (destType.equalsIgnoreCase("queue")) {
                destType = DESTINATION_TYPE_QUEUE;
            }
            ObjectName on = new ObjectName(MBEAN_DOMAIN_NAME + ":type=Destination,subtype=Config,desttype=" + destType +",name=\"" + destName + "\"");
            MBeanAttributeInfo[] attribs = mbsc.getMBeanInfo(on).getAttributes();
            for (MBeanAttributeInfo attrib: attribs){
                destAttrs.put(attrib.getName(), mbsc.getAttribute(on, attrib.getName()));
            }
            return destAttrs;
        } catch (Exception e) {
            //log JMX Exception trace as WARNING
            logAndHandleException(e, "admin.mbeans.rmb.error_getting_jms_dest");
        } finally {
            try {
                if (mqInfo != null) {
                    mqInfo.closeMQMBeanServerConnection();
                }
            } catch (Exception e) {
                handleException(e);
            }
        }
        return destAttrs;
    }
}
