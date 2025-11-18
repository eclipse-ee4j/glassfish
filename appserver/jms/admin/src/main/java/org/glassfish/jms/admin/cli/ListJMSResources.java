/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.glassfish.api.naming.DefaultResourceProxy;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * List Connector Resources command
 *
 */
@Service(name="list-jms-resources")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.jms.resources")
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.DOMAIN,CommandTarget.CLUSTERED_INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.GET,
        path="list-jms-resources",
        description="list-jms-resources")
})
public class ListJMSResources implements AdminCommand {

    private static final String JMSRA = "jmsra";
    private static final String QUEUE = "jakarta.jms.Queue";
    private static final String TOPIC = "jakarta.jms.Topic";
    private static final String QUEUE_CF = "jakarta.jms.QueueConnectionFactory";
    private static final String TOPIC_CF = "jakarta.jms.TopicConnectionFactory";
    private static final String UNIFIED_CF = "jakarta.jms.ConnectionFactory";
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListJMSResources.class);

    @Param(name="resType", optional=true)
    String resourceType;

    @Param(primary=true, optional=true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;


    @Inject
    Domain domain;

    @Inject
    ServiceLocator habitat;

    /**
        * Executes the command with the command parameters passed as Properties
        * where the keys are the paramter names and the values the parameter values
        *
        * @param context information
        */
    @Override
       public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        ArrayList<Map<String,String>> list = new ArrayList<>();
        Properties extraProperties = new Properties();

        Collection<AdminObjectResource> adminObjectResourceList = domain.getResources().getResources(AdminObjectResource.class);
        Collection<ConnectorResource> connectorResourcesList = domain.getResources().getResources(ConnectorResource.class);

        Object[] connectorResources = connectorResourcesList.toArray();
        Object[] adminObjectResources = adminObjectResourceList.toArray();

        if (resourceType == null){
          try {
            //list all JMS resources
            for (Object r :  adminObjectResources) {
                AdminObjectResource adminObject = (AdminObjectResource) r;
                if (JMSRA.equals(adminObject.getResAdapter())) {
                    Map<String,String> m = new HashMap<>();
                    m.put("name", adminObject.getJndiName());
                    list.add(m);
                }
            }

            for (Object c : connectorResources) {
                ConnectorResource cr = (ConnectorResource) c;
                ConnectorConnectionPool cp = domain.getResources().getResourceByName(ConnectorConnectionPool.class,
                    SimpleJndiName.of(cr.getPoolName()));

                if (cp  != null && JMSRA.equals(cp.getResourceAdapterName())){
                    Map<String,String> m = new HashMap<>();
                    m.put("name", cr.getJndiName());
                    list.add(m);
                }
            }
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.jms.resources.fail",
                    "Unable to list JMS Resources") + " " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
      } else {
            switch (resourceType) {
                case TOPIC_CF:
                case QUEUE_CF:
                case UNIFIED_CF:
                    for (Object c : connectorResources) {
                       ConnectorResource cr = (ConnectorResource)c;
                       ConnectorConnectionPool cp = domain.getResources()
                           .getResourceByName(ConnectorConnectionPool.class, SimpleJndiName.of(cr.getPoolName()));
                       if (cp != null && resourceType.equals(cp.getConnectionDefinitionName())
                           && JMSRA.equals(cp.getResourceAdapterName())) {
                           Map<String, String> m = new HashMap<>();
                           m.put("name", cr.getJndiName());
                           list.add(m);
                       }
                    }
                    break;
                case TOPIC:
                case QUEUE:
                    for (Object r : adminObjectResources) {
                        AdminObjectResource res = (AdminObjectResource)r;
                        if(resourceType.equals(res.getResType()) && JMSRA.equals(res.getResAdapter())) {
                            Map<String,String> m = new HashMap<>();
                            m.put("name", res.getJndiName());
                            list.add(m);
                        }
                    }
                    break;
            }
      }
        if (!list.isEmpty()) {
            List<Map<String,String>> resourceList =
                CommandTarget.DOMAIN.isValid(habitat, target) ? list : filterListForTarget(list);

            List<DefaultResourceProxy> drps = habitat.getAllServices(DefaultResourceProxy.class);

            for (Map<String,String> m : resourceList) {
                String jndiName = m.get("name");
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(jndiName);
                String logicalName = DefaultResourceProxy.Util.getLogicalName(drps, jndiName);
                if (logicalName != null) {
                    m.put("logical-jndi-name", logicalName);
                }
            }
            extraProperties.put("jmsResources", resourceList);
        }
        report.setExtraProperties(extraProperties);
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
  }

    private List filterListForTarget(List<Map<String,String>> list){
        List<Map<String,String>> resourceList = new ArrayList<>();
        if (target != null){
            List<ResourceRef> resourceRefs = null;
            Cluster cluster = domain.getClusterNamed(target);
            if (cluster != null) {
                resourceRefs=  cluster.getResourceRef();
            } else {
                Server server = domain.getServerNamed(target);
                if (server != null) {
                    resourceRefs = server.getResourceRef();
                }
            }
            if (resourceRefs != null && !resourceRefs.isEmpty()) {
                for (Map<String,String> m : list) {
                    String jndiName = m.get("name");
                    for (ResourceRef resource : resourceRefs) {
                        if (jndiName.equals(resource.getRef())) {
                            resourceList.add(m);
                        }
                    }
                }
            }
        }
        return resourceList;
    }
}
