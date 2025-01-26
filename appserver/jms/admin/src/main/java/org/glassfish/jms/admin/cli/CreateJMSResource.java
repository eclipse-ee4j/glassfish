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
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Create JMS Resource Command
 */
@Service(name="create-jms-resource")
@PerLookup
@I18n("create.jms.resource")
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.DOMAIN})
@RestEndpoints({
    @RestEndpoint(configBean=Resources.class,
        opType=RestEndpoint.OpType.POST,
        path="create-jms-resource",
        description="create-jms-resource")
})
public class CreateJMSResource implements AdminCommand {

    private static final String QUEUE = "jakarta.jms.Queue";
    private static final String TOPIC = "jakarta.jms.Topic";
    private static final String QUEUE_CF = "jakarta.jms.QueueConnectionFactory";
    private static final String TOPIC_CF = "jakarta.jms.TopicConnectionFactory";
    private static final String UNIFIED_CF = "jakarta.jms.ConnectionFactory";
    private static final String DEFAULT_JMS_ADAPTER = "jmsra";
    private static final String DEFAULT_OPERAND="DEFAULT";
    private static final String JNDINAME_APPENDER="-Connection-Pool";

    // JMS destination resource properties
    private static final String NAME = "Name";
    private static final String IMQ_DESTINATION_NAME = "imqDestinationName";

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(CreateJMSResource.class);

    @Param(name = "resType")
    private String resourceType;

    @Param(optional = true, defaultValue = "true")
    private Boolean enabled;

    @Param(name = "property", optional = true, separator = ':')
    private Properties props;

    @Param(optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Param(name = "description", optional = true)
    private String description;

    @Param(optional = true, defaultValue = "false")
    private Boolean force;

    @Param(name = "jndi_name", primary = true)
    private String jndiName;

    @Inject
    private CommandRunner commandRunner;

    @Inject
    private Domain domain;


    /**
     * As per new requirement all resources should have unique name so appending 'JNDINAME_APPENDER'
     * to jndiName for creating jndiNameForConnectionPool.
     */
    private String jndiNameForConnectionPool;
    private Hashtable<String, String> mapping;


    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        //Collection connPools = domain.getResources().getResources(ConnectorConnectionPool.class);
        if (resourceType == null) {
            report.setMessage(I18N.getLocalString("create.jms.resource.noResourceType",
                            "No Resoruce Type specified for JMS Resource."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (jndiName == null) {
            report.setMessage(I18N.getLocalString("create.jms.resource.noJndiName",
                            "No JNDI name specified for JMS Resource."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (!(resourceType.equals(TOPIC_CF) || resourceType.equals(QUEUE_CF) || resourceType.equals(UNIFIED_CF)
            || resourceType.equals(TOPIC) || resourceType.equals(QUEUE))) {
             report.setMessage(I18N.getLocalString("create.jms.resource.InvalidResourceType",
                            "Invalid Resource Type specified for JMS Resource."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;

        }

        jndiNameForConnectionPool = jndiName + JNDINAME_APPENDER;
        SimpleJndiName simpleJndiName = new SimpleJndiName(jndiName);
        if (force) {
            final Resource res;
            if (resourceType.equals(TOPIC) || resourceType.equals(QUEUE)) {
                res = domain.getResources().getResourceByName(AdminObjectResource.class, simpleJndiName);
            } else {
                res = domain.getResources().getResourceByName(ConnectorResource.class, simpleJndiName);
            }

            if (res != null) {
                ActionReport deleteReport = report.addSubActionsReport();
                ParameterMap parameters = new ParameterMap();
                parameters.set(DEFAULT_OPERAND, jndiName);
                parameters.set("target", target);
                commandRunner.getCommandInvocation("delete-jms-resource", deleteReport, context.getSubject()).parameters(parameters).execute();
                if (ActionReport.ExitCode.FAILURE.equals(deleteReport.getActionExitCode())) {
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }

        //Populate the JMS RA map
        populateJmsRAMap();


        /* Map MQ properties to Resource adapter properties */
        if (props != null) {
            Enumeration en = props.keys();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String raKey = getMappedName(key);
                if (raKey == null) {
                    raKey = key;
                }
                props.put(raKey, props.get(key));
                if (!raKey.equals(key)) {
                    props.remove(key);
                }
            }
        }

        ActionReport subReport = report.addSubActionsReport();

        if (resourceType.equals(TOPIC_CF) || resourceType.equals(QUEUE_CF) || resourceType.equals(UNIFIED_CF)) {
            ConnectorConnectionPool cpool = domain.getResources().getResourceByName(ConnectorConnectionPool.class,
                SimpleJndiName.of(jndiNameForConnectionPool));

            boolean createdPool = false;
            // If pool is already existing, do not try to create it again
            if (cpool == null || !filterForTarget(jndiNameForConnectionPool)) {
                // Add connector-connection-pool.
                ParameterMap parameters = populateConnectionPoolParameters();
                commandRunner.getCommandInvocation("create-connector-connection-pool", subReport, context.getSubject()).parameters(parameters).execute();
                createdPool= true;
                if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
                    report.setMessage(I18N.getLocalString("create.jms.resource.cannotCreateConnectionPool",
                        "Unable to create connection pool."));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
            ParameterMap params = populateConnectionResourceParameters();
            commandRunner.getCommandInvocation("create-connector-resource", subReport, context.getSubject()).parameters(params).execute();

            if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())) {
                report.setMessage(I18N.getLocalString("create.jms.resource.cannotCreateConnectorResource",
                    "Unable to create connection resource."));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);

                //rollback the connection pool ONLY if we created it...
                if (createdPool) {
                    ParameterMap paramsForRollback = new ParameterMap();
                    paramsForRollback.set(DEFAULT_OPERAND, jndiNameForConnectionPool);
                    commandRunner.getCommandInvocation("delete-connector-connection-pool", subReport, context.getSubject())
                    .parameters(paramsForRollback)
                    .execute();
                }
                return;
            }
        } else if (resourceType.equals(TOPIC) || resourceType.equals(QUEUE)) {
            ParameterMap aoAttrList = new ParameterMap();
            try {
                //validate the provided properties and modify it if required.
                Properties properties =  validateDestinationResourceProps(props, jndiName);
                //aoAttrList.put("property", properties);
                StringBuilder builder = new StringBuilder();
                for (java.util.Map.Entry<Object, Object>prop : properties.entrySet()) {
                    builder.append(prop.getKey()).append("=").append(prop.getValue()).append(":");
                }
                String propString = builder.toString();
                int lastColonIndex = propString.lastIndexOf(":");
                if (lastColonIndex >= 0) {
                    propString = propString.substring(0, lastColonIndex);
                }
                aoAttrList.set("property", propString);
            } catch (Exception e) {
                report.setMessage(I18N.getLocalString("create.jms.resource.cannotCreateAdminObjectWithRootCause",
                    "Unable to create admin object. Reason: " + e.getMessage(), e.getMessage()));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            // create admin object
            aoAttrList.set(DEFAULT_OPERAND,  jndiName);
            aoAttrList.set("restype",  resourceType);
            aoAttrList.set("raname",  DEFAULT_JMS_ADAPTER);
            aoAttrList.set("target",  target);
            if (enabled != null) {
                aoAttrList.set("enabled", Boolean.toString(enabled));
            }

            commandRunner.getCommandInvocation("create-admin-object", subReport, context.getSubject()).parameters(aoAttrList).execute();

            if (ActionReport.ExitCode.FAILURE.equals(subReport.getActionExitCode())){
                report.setMessage(I18N.getLocalString("create.jms.resource.cannotCreateAdminObject",
                    "Unable to create admin object."));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
        report.setActionExitCode(ec);
    }

    private boolean filterForTarget(String jndiName){
        if (target != null) {
            List<ResourceRef> resourceRefs = null;
            Cluster cluster = domain.getClusterNamed(target);
            if (cluster != null) {
                resourceRefs = cluster.getResourceRef();
            } else {
                Server server = domain.getServerNamed(target);
                if (server != null) {
                    resourceRefs = server.getResourceRef();
                }
            }
            if (resourceRefs != null && !resourceRefs.isEmpty()) {
                for (ResourceRef resource : resourceRefs) {
                    if (jndiName.equalsIgnoreCase(resource.getRef())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void populateJmsRAMap() {
        mapping = new Hashtable<>();
        mapping.put("imqDestinationName","Name");
        mapping.put("imqDestinationDescription","Description");
        mapping.put("imqConnectionURL","ConnectionURL");
        mapping.put("imqDefaultUsername","UserName");
        mapping.put("imqDefaultPassword","Password");
        mapping.put("imqConfiguredClientID","ClientId");
        mapping.put("imqAddressList","AddressList");
        mapping.put("MessageServiceAddressList","AddressList");
    }

    public String getMappedName(String key){
        return mapping.get(key);
    }

    private ParameterMap populateConnectionPoolParameters(){

        String steadyPoolSize = null;
        String maxPoolSize = null;
        String poolResizeQuantity = null;
        String idleTimeoutInSecs = null;
        String maxWaitTimeInMillis = null;
        String failAllConnections = null;
        String transactionSupport = null;
        ParameterMap parameters = new ParameterMap();

        if (props != null) {
            Enumeration keys =  props.keys();
            Properties tmpProps = new Properties();

            while (keys.hasMoreElements()) {
                String propKey = (String) keys.nextElement();

                if ("steady-pool-size".equals(propKey)) {
                    steadyPoolSize = props.getProperty(propKey);
                } else if ("max-pool-size".equals(propKey)) {
                    maxPoolSize = props.getProperty(propKey);
                } else if ("pool-resize-quantity".equals(propKey)) {
                    poolResizeQuantity = props.getProperty(propKey);
                } else if ("idle-timeout-in-seconds".equals(propKey)) {
                    idleTimeoutInSecs = props.getProperty(propKey);
                } else if ("max-wait-time-in-millis".equals(propKey)) {
                    maxWaitTimeInMillis = props.getProperty(propKey);
                } else if ("transaction-support".equals(propKey)) {
                    transactionSupport = props.getProperty(propKey);
                } else if("fail-all-connections".equals(propKey)) {
                    failAllConnections = props.getProperty(propKey);
                } else{
                    if ("AddressList".equals(propKey)) {
                        String addressListProp = props.getProperty(propKey);
                        props.setProperty(propKey, "\"" + addressListProp + "\"");
                    } else if ("Password".equals(propKey)){
                        String password = props.getProperty(propKey);
                        if (isPasswordAlias(password)) {
                            //If the string is a password alias, it needs to be escapted with another pair of quotes...
                            props.setProperty(propKey, "\"" + password + "\"");
                        }
                    }

                    tmpProps.setProperty(propKey, props.getProperty(propKey));
                }
            }
            if (tmpProps.size() > 0) {
                StringBuilder builder = new StringBuilder();
                for (java.util.Map.Entry<Object, Object>prop : tmpProps.entrySet()) {
                    builder.append(prop.getKey()).append("=").append(prop.getValue()).append(":");
                }
                String propString = builder.toString();
                int lastColonIndex = propString.lastIndexOf(":");
                if (lastColonIndex >= 0) {
                    propString = propString.substring(0, lastColonIndex);
                }
                parameters.set("property", propString);
            }
        }
        //parameters.set("restype", resourceType);
        parameters.set(DEFAULT_OPERAND, jndiNameForConnectionPool);
        parameters.set("poolname", jndiName);

        if (description != null) {
            parameters.set("description", description);
        }

        // Get the default res adapter name from Connector-runtime
        String raName = DEFAULT_JMS_ADAPTER;
        parameters.set("raname", raName);

        parameters.set("connectiondefinition", resourceType);
        parameters.set("maxpoolsize",  (maxPoolSize == null) ? "250" : maxPoolSize);
        parameters.set("steadypoolsize", (steadyPoolSize == null) ? "1" : steadyPoolSize);
        if (poolResizeQuantity != null) {
            parameters.set("poolresize", poolResizeQuantity);
        }
        if (idleTimeoutInSecs != null) {
            parameters.set("idletimeout", idleTimeoutInSecs);
        }

        if (maxWaitTimeInMillis != null) {
            parameters.set("maxwait", maxWaitTimeInMillis);
        }

        if (failAllConnections != null) {
            parameters.set("failconnection",failAllConnections);
        }
        if (transactionSupport != null) {
            parameters.set("transactionsupport", transactionSupport);
        }

        return parameters;
    }

    private boolean isPasswordAlias(String password){
        if (password != null && password.startsWith("${ALIAS=")) {
            return true;
        }

        return false;
    }

    private ParameterMap populateConnectionResourceParameters() {
        ParameterMap parameters = new ParameterMap();
        parameters.set("jndi_name", jndiName);
        parameters.set(DEFAULT_OPERAND, jndiName);
        parameters.set("enabled", Boolean.toString(enabled));
        parameters.set("poolname", jndiNameForConnectionPool);
        parameters.set("target", target);
        if (description != null) {
            parameters.set("description", description);
        }
        return parameters;
    }

   /**
     * Validates the properties specified for a Destination Resource
     * and returns a validated Properties list.
     *
     * NOTE: When "Name" property has not been specified by the user,
     * the properties object is updated with a computed Name.
     */
   private Properties validateDestinationResourceProps(Properties props, String jndiName) throws Exception {
        final String providedDestinationName;
        if (props == null) {
            props = new Properties();
            providedDestinationName = null;
        } else {
            providedDestinationName = getProvidedDestinationName(props);
        }
        if (providedDestinationName == null) {
            String newDestName = computeDestinationName(jndiName);
            props.put(NAME, newDestName);
        } else {
            if (!isSyntaxValid(providedDestinationName)) {
                throw new Exception(I18N.getLocalString(
                      "admin.mbeans.rmb.destination_name_invalid",
                      "Destination Resource " + jndiName +
                      " has an invalid destination name " + providedDestinationName,
                      jndiName, providedDestinationName));
            }
        }
        return props;
    }

    /**
     * Get the physical destination name provided by the user. The "Name"
     * and "imqDestinationName" properties are used to link a JMS destination
     * resource to its physical destination in SJSMQ.
     */
    private String getProvidedDestinationName(Properties props) {
        for (String key : props.stringPropertyNames()) {
            String value = (String)props.get(key);
            if (NAME.equals(key) || IMQ_DESTINATION_NAME.equals(key)) {
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    // Modified this method to support wildcards in MQ destinations...
    private boolean isSyntaxValid(String name) {
        if (name.startsWith("mq.")) {
            return false;
        }

        try {
            CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
            if (!asciiEncoder.canEncode(name)) {
                return false;
            }
        } catch (Exception e) {
            // skip detecting non ASCII charactors if error occurs
        }

        char[] namechars = name.toCharArray();
        if (Character.isJavaIdentifierStart(namechars[0]) || namechars[0] == '*' || namechars[0] == '>') {
            for (int i = 1; i < namechars.length; i++) {
                if (!Character.isJavaIdentifierPart(namechars[i]) && ! (namechars[i] == '.' || namechars[i] == '*' || namechars[i] == '>')) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Derive a destination name, valid as per MQ destination naming rules,
     * from the JNDI name provided for the JMS destination resource.
     *
     * Scheme: merely replace all invalid identifiers in the JNDI name with
     * an 'underscore'.
     */
    private String computeDestinationName(String providedJndiName) {
        char[] jndiName = providedJndiName.toCharArray();
        char[] finalName = new char[jndiName.length];
        finalName[0] = Character.isJavaIdentifierStart(jndiName[0]) ? jndiName[0] : '_';
        for (int i = 1; i < jndiName.length; i++) {
            finalName[i] = Character.isJavaIdentifierPart(jndiName[i])? jndiName[i] : '_';
        }
        return new String(finalName);
    }
}
