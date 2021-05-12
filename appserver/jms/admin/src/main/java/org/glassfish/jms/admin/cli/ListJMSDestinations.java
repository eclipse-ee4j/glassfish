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

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.internal.api.ServerContext;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.*;

import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.api.admin.*;

/**
 * Create JMS Destination
 *
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
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Inject
    com.sun.appserv.connectors.internal.api.ConnectorRuntime connectorRuntime;

    @Inject
    Domain domain;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    ServerContext serverContext;


    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        if (destType != null && !destType.equals(JMS_DEST_TYPE_QUEUE)
                && !destType.equals(JMS_DEST_TYPE_TOPIC)) {
            report.setMessage(localStrings.getLocalString("admin.mbeans.rmb.invalid_jms_desttype", destType));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        report.setExtraProperties(new Properties());
        List<Map> jmsDestList = new ArrayList<Map>();

        try {
            List<JMSDestinationInfo> list = listJMSDestinations(target, destType);

            for (JMSDestinationInfo destInfo : list) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(destInfo.getDestinationName());
                Map<String, String> destMap = new HashMap<String, String>();
                destMap.put("name", destInfo.getDestinationName());
                destMap.put("type", destInfo.getDestinationType());
                jmsDestList.add(destMap);
            }

            report.getExtraProperties().put("destinations", jmsDestList);

        } catch (Exception e) {
            logger.throwing(getClass().getName(), "ListJMSDestination", e);
            e.printStackTrace();//handleException(e);
            report.setMessage(localStrings.getLocalString("list.jms.dest.fail",
                    "Unable to list JMS Destinations. Please ensure that the Message Queue Brokers are running"));// + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
    }

// list-jmsdest
    public List listJMSDestinations(String tgtName, String destType)
        throws Exception {

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "listJMSDestination ...");
        }
        MQJMXConnectorInfo mqInfo = getMQJMXConnectorInfo(target, config, serverContext, domain, connectorRuntime);

        //MBeanServerConnection  mbsc = getMBeanServerConnection(tgtName);
        try {
            MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
            ObjectName on = new ObjectName(DESTINATION_MANAGER_CONFIG_MBEAN_NAME);

            ObjectName [] dests = (ObjectName [])mbsc.invoke(on, "getDestinations", null, null);
            if ((dests != null) && (dests.length > 0)) {
                List<JMSDestinationInfo> jmsdi = new ArrayList<JMSDestinationInfo>();
                for (int i = 0; i < dests.length; i++) {
                    on = dests[i];

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
                //(JMSDestinationInfo[]) jmsdi.toArray(new JMSDestinationInfo[]{});
            }
        } catch (Exception e) {
            // log JMX Exception trace as WARNING
            logAndHandleException(e, "admin.mbeans.rmb.error_listing_jms_dest");
        } finally {
            try {
                if (mqInfo != null) {
                    mqInfo.closeMQMBeanServerConnection();
                }
            } catch (Exception e) {
                handleException(e);
            }
        }

        return null;
    }


    private String toStringLabel(String type) {
        if (type.equals(DESTINATION_TYPE_QUEUE)) {
            return ("queue");
        } else if (type.equals(DESTINATION_TYPE_TOPIC)) {
            return ("topic");
        } else {
            return ("unknown");
        }
    }

}
