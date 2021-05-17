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

import javax.security.auth.Subject;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.internal.api.ServerContext;
import java.util.*;
import java.util.logging.Logger;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.config.serverbeans.*;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.AttributeList;

import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;

import org.glassfish.api.admin.*;

/**
 * Create JMS Destination
 *
 */
@Service(name = "create-jmsdest")
@PerLookup
@I18n("create.jms.dest")
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.POST,
        path="create-jmsdest",
        description="Create JMS Destination",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="create-jmsdest",
        description="Create JMS Destination",
        params={
            @RestParam(name="target", value="$parent")
        })
})
public class CreateJMSDestination extends JMSDestination implements AdminCommand {
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateJMSDestination.class);

    @Param(name = "destType", shortName = "T", optional = false)
    String destType;

    @Param(name = "property", optional = true, separator = ':')
    Properties props;

    @Param(optional=true, defaultValue="false")
    Boolean force;

    @Param(name = "dest_name", primary = true)
    String destName;

    @Param(optional = true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Inject
    com.sun.appserv.connectors.internal.api.ConnectorRuntime connectorRuntime;

    @Inject
    CommandRunner commandRunner;

    @Inject
    Domain domain;

    //@Inject
    //Configs configs;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    ServerContext serverContext;

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        try {
            validateJMSDestName(destName);
            validateJMSDestType(destType);
        } catch (IllegalArgumentException e) {
            report.setMessage(e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (destType.equals(JMS_DEST_TYPE_QUEUE)) {
            if (props == null) {
                props = new Properties();
            }
            if (!props.containsKey(MAX_ACTIVE_CONSUMERS_PROPERTY)
                    && !props.containsKey(MAX_ACTIVE_CONSUMERS_ATTRIBUTE)) {
                props.put(MAX_ACTIVE_CONSUMERS_ATTRIBUTE, DEFAULT_MAX_ACTIVE_CONSUMERS);
            }
        }
        try {
            createJMSDestination(report, context.getSubject());
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("create.jms.destination.CannotCreateJMSDest",
                    "Unable to create JMS Destination."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
    }

    // create-jmsdest
    private void createJMSDestination(ActionReport report, final Subject subject) throws Exception {

        MQJMXConnectorInfo mqInfo = getMQJMXConnectorInfo(target, config, serverContext, domain, connectorRuntime);

        //MBeanServerConnection  mbsc = getMBeanServerConnection(tgtName);
        try {
            MBeanServerConnection mbsc = mqInfo.getMQMBeanServerConnection();
            ObjectName on = new ObjectName(
                    DESTINATION_MANAGER_CONFIG_MBEAN_NAME);
            String[] signature = null;
            AttributeList destAttrs = null;
            Object[] params = null;

            if (force) {
                signature = new String[] {};
                params = new Object[] {};
                ObjectName[] dests = (ObjectName[]) mbsc.invoke(on, "getDestinations", params, signature);
                boolean destExists = false;
                if (dests != null) {
                    String type = destType.equalsIgnoreCase(JMS_DEST_TYPE_TOPIC) ? "t" : "q";
                    for (ObjectName dest : dests) {
                        if (dest.toString().indexOf("desttype=" + type + ",name=" + ObjectName.quote(destName)) != -1) {
                            destExists = true;
                            break;
                        }
                    }
                }
                if (destExists) {
                    ActionReport deleteReport = report.addSubActionsReport();
                    ParameterMap parameters = new ParameterMap();
                    parameters.set("DEFAULT", destName);
                    parameters.set("destType", destType);
                    parameters.set("target", target);
                    commandRunner.getCommandInvocation("delete-jmsdest", deleteReport, subject).parameters(parameters).execute();
                    if (ActionReport.ExitCode.FAILURE.equals(deleteReport.getActionExitCode())) {
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        return;
                    }
                }
            }

            if (props != null) {
                destAttrs = convertProp2Attrs(props);
            }

            // setAppserverDefaults(destAttrs, mqInfo);

            if (destType.equalsIgnoreCase(JMS_DEST_TYPE_TOPIC)) {
                destType = DESTINATION_TYPE_TOPIC;
            } else if (destType.equalsIgnoreCase(JMS_DEST_TYPE_QUEUE)) {
                destType = DESTINATION_TYPE_QUEUE;
            }
            if ((destAttrs == null) || (destAttrs.size() == 0)) {
                signature = new String[]{
                            "java.lang.String",
                            "java.lang.String"};
                params = new Object[]{destType, destName};
            } else {
                signature = new String[]{
                            "java.lang.String",
                            "java.lang.String",
                            "javax.management.AttributeList"};
                params = new Object[]{destType, destName, destAttrs};
            }

            mbsc.invoke(on, "create", params, signature);
            report.setMessage(localStrings.getLocalString("create.jms.destination.success", "JMS Desctination {0} created.", destName));
        } catch (Exception e) {
            logAndHandleException(e, "admin.mbeans.rmb.error_creating_jms_dest");
        } finally {
            try {
                if (mqInfo != null) {
                    mqInfo.closeMQMBeanServerConnection();
                }
            } catch (Exception e) {
                handleException(e);
            }
        }
    }
}
