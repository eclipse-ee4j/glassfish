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
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
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
 * Create JMS Destination
 */
@Service(name="list-jmsdest")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.jms.dests")
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.GET,
        path="list-jmsdest",
        description="List JMS Destinations",
        params={
            @RestParam(name="id", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.GET,
        path="list-jmsdest",
        description="List JMS Destinations",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class ListJMSDestinations extends JMSDestination implements AdminCommand {

    private static final Logger logger = Logger.getLogger(LogUtils.JMS_ADMIN_LOGGER);
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJMSDestination.class);

    @Param(name="destType", optional=true)
    String destType;

    @Param(name="property", optional=true, separator=':')
    Properties props;

    @Param(primary=true, optional=true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

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

        if (destType != null && !destType.equals(JMS_DEST_TYPE_QUEUE)
                && !destType.equals(JMS_DEST_TYPE_TOPIC)) {
            report.setMessage(localStrings.getLocalString("admin.mbeans.rmb.invalid_jms_desttype", destType));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        report.setExtraProperties(new Properties());
        List<Map> jmsDestList = new ArrayList<>();

        try {
            List<JMSDestinationInfo> list = listJMSDestinations(target, destType);

            for (JMSDestinationInfo destInfo : list) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(destInfo.getDestinationName());
                Map<String, String> destMap = new HashMap<>();
                destMap.put("name", destInfo.getDestinationName());
                destMap.put("type", destInfo.getDestinationType());
                jmsDestList.add(destMap);
            }

            report.getExtraProperties().put("destinations", jmsDestList);

        } catch (Exception e) {
            logger.throwing(getClass().getName(), "ListJMSDestination", e);
            report.setMessage(localStrings.getLocalString("list.jms.dest.fail",
                    "Unable to list JMS Destinations. Please ensure that the Message Queue Brokers are running"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    public List<JMSDestinationInfo> listJMSDestinations(String tgtName, String destType) throws Exception {
        logger.log(Level.FINE, "listJMSDestinations ...");
        try (MQJMXConnectorInfo mqInfo = createMQJMXConnectorInfo(target, config, serverContext, domain, connectorRuntime)) {
            MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
            ObjectName on = new ObjectName(DESTINATION_MANAGER_CONFIG_MBEAN_NAME);
            ObjectName[] dests = (ObjectName[]) mbsc.invoke(on, "getDestinations", null, null);
            if (dests == null || dests.length <= 0) {
                return null;
            }
            final List<JMSDestinationInfo> jmsdi = new ArrayList<>();
            for (ObjectName dest : dests) {
                on = dest;
                String jdiType = toStringLabel(on.getKeyProperty("desttype"));
                String jdiName = on.getKeyProperty("name");

                // check if the destination name has double quotes at the beginning
                // and end, if yes strip them
                if ((jdiName != null) && (jdiName.length() > 1)) {
                    if (jdiName.indexOf('"') == 0) {
                        jdiName = jdiName.substring(1);
                    }
                    if (jdiName.lastIndexOf('"') == (jdiName.length() - 1)) {
                        jdiName = jdiName.substring(0, jdiName.lastIndexOf('"'));
                    }
                }

                JMSDestinationInfo jdi = new JMSDestinationInfo(jdiName, jdiType);

                if (destType == null) {
                    jmsdi.add(jdi);
                } else if (destType.equals(JMS_DEST_TYPE_TOPIC)
                        || destType.equals(JMS_DEST_TYPE_QUEUE)) {
                    //Physical Destination Type specific listing
                    if (jdiType.equalsIgnoreCase(destType)) {
                        jmsdi.add(jdi);
                    }
                }
            }
            return jmsdi;
        } catch (Exception e) {
            throw logAndHandleException(e, "admin.mbeans.rmb.error_listing_jms_dest");
        }
    }


    private String toStringLabel(String type) {
        if (type.equals(DESTINATION_TYPE_QUEUE)) {
            return "queue";
        } else if (type.equals(DESTINATION_TYPE_TOPIC)) {
            return "topic";
        } else {
            return "unknown";
        }
    }

}
