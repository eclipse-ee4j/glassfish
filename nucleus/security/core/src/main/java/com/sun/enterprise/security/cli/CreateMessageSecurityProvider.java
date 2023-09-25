/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.cli;

import static org.glassfish.api.ActionReport.ExitCode.FAILURE;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;
import static org.glassfish.api.admin.ServerEnvironment.DEFAULT_INSTANCE_NAME;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.MessageSecurityConfig;
import com.sun.enterprise.config.serverbeans.ProviderConfig;
import com.sun.enterprise.config.serverbeans.RequestPolicy;
import com.sun.enterprise.config.serverbeans.ResponsePolicy;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Create Message Security Provider Command
 *
 * Usage: create-message-security-provider [--terse=false] [--echo=false] [--interactive=true] [--host localhost] [--port
 * 4848|4849] [--secure | -s] [--user admin_user] [--passwordfile file_name] [--target target(Default server)] [--layer
 * message_layer=SOAP] [--providertype provider_type] [--requestauthsource request_auth_source] [--requestauthrecipient
 * request_auth_recipient] [--responseauthsource response_auth_source] [--responseauthrecipient response_auth_recipient]
 * [--isdefaultprovider] [--property (name=value)[:name=value]*] --classname provider_class provider_name
 *
 * domain.xml element example
 *
 * <message-security-config auth-layer="SOAP"> <!-- turned off by default -->
 * <provider-config class-name="com.sun.wss.provider.ClientSecAuthModule" provider-id="XWS_ClientProvider" provider-type=
 * "client"> <request-policy auth-source="content"/> <response-policy auth-source="content"/>
 * <property name="encryption.key.alias" value="s1as"/> <property name="signature.key.alias" value="s1as"/>
 * <property name="dynamic.username.password" value="false"/> <property name="debug" value="false"/> </provider-config>
 * </message-security-config>
 *
 * @author Nandini Ektare
 */
@Service(name = "create-message-security-provider")
@PerLookup
@I18n("create.message.security.provider")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
public class CreateMessageSecurityProvider implements AdminCommand, AdminCommandSecurity.Preauthorization {

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateMessageSecurityProvider.class);

    private static final String SERVER = "server";
    private static final String CLIENT = "client";
    private static final String CLIENT_SERVER = "client-server";

    // auth-layer can only be SOAP | HttpServlet
    @Param(name = "layer", acceptableValues = "SOAP,HttpServlet", defaultValue = "SOAP")
    String authLayer;

    // provider-type can only be - client | server | 'client-server'
    @Param(name = "providertype", acceptableValues = "client,server,client-server", optional = true, defaultValue = "client-server")
    String providerType;

    // auth-source can only be - sender | content
    @Param(name = "requestauthsource", optional = true)
    String requestAuthSource;

    // auth-recipient can only be - before-content | after-content
    @Param(name = "requestauthrecipient", optional = true)
    String requestAuthRecipient;

    // auth-source can only be - sender | content
    @Param(name = "responseauthsource", optional = true)
    String responseAuthSource;

    // auth-recipient can only be - before-content | after-content
    @Param(name = "responseauthrecipient", optional = true)
    String responseAuthRecipient;

    // isdefaultprovider can only be - client | server | 'client-server'
    @Param(name = "isdefaultprovider", optional = true, defaultValue = "false")
    Boolean isDefaultProvider;

    @Param(optional = true, name = "property", separator = ':')
    Properties properties;

    @Param(name = "classname")
    String providerClass;

    @Param(name = "providername", primary = true)
    String providerId;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;

    @Inject
    @Named(DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Domain domain;

    @AccessRequired.NewChild(type = MessageSecurityConfig.class)
    private SecurityService securityService;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.chooseConfig(domain, target, context.getActionReport());
        if (config == null) {
            return false;
        }

        securityService = config.getSecurityService();
        return true;
    }

    /**
     * Executes the command with the command parameters passed as Properties where the keys are parameter names and the
     * values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        List<MessageSecurityConfig> messageSecurityConfigs = securityService.getMessageSecurityConfig();

        // Let's find the correct MessageSecurityConfig. As of now, there can be only two of them - one for SOAP and one for
        // HttpServlet
        MessageSecurityConfig messageSecurityConfig = null;
        for (MessageSecurityConfig candidateMessageSecurityConfig : messageSecurityConfigs) {
            if (candidateMessageSecurityConfig.getAuthLayer().equals(authLayer)) {
                messageSecurityConfig = candidateMessageSecurityConfig;
            }
        }

        // If there is message security config for this type of layer
        // then, add a new provider config under it provided it is not duplicate
        if (messageSecurityConfig != null) {
            // Check if there exists a provider config by the specified provider name; if so return failure.
            for (ProviderConfig providerConfig : messageSecurityConfig.getProviderConfig()) {
                if (providerConfig.getProviderId().equals(providerId)) {
                    report.setMessage(localStrings.getLocalString("create.message.security.provider.duplicatefound",
                            "Message security provider named {0} exists. " + "Cannot add duplicate.", providerId));
                    report.setActionExitCode(FAILURE);
                    return;
                }
            }

            // No duplicate message security providers found. So add one.
            try {
                ConfigSupport.apply(new SingleConfigCode<MessageSecurityConfig>() {
                    @Override
                    public Object run(MessageSecurityConfig param) throws PropertyVetoException, TransactionFailure {
                        ProviderConfig newProviderConfig = param.createChild(ProviderConfig.class);
                        populateProviderConfigElement(newProviderConfig);
                        param.getProviderConfig().add(newProviderConfig);

                        // Depending on the providerType of the new provider the isDefaultProvider=true results in creation of
                        // either default-provider attribute or default-client-provider or BOTH in the message
                        // security config object
                        if (isDefaultProvider) {
                            if (providerType.equals(SERVER) || providerType.equals(CLIENT_SERVER)) {
                                param.setDefaultProvider(providerId);
                            }

                            if (providerType.equals(CLIENT) || providerType.equals(CLIENT_SERVER)) {
                                param.setDefaultClientProvider(providerId);
                            }
                        }

                        return newProviderConfig;
                    }
                }, messageSecurityConfig);
            } catch (TransactionFailure e) {
                report.setMessage(localStrings.getLocalString("create.message.security.provider.fail",
                        "Creation of message security provider named {0} failed", providerId));
                report.setActionExitCode(FAILURE);
                report.setFailureCause(e);
                return;
            }
            report.setActionExitCode(SUCCESS);
            report.setMessage(localStrings.getLocalString("create.message.security.provider.success",
                    "Creation of message security provider named {0} completed " + "successfully", providerId));
        } else {

            // Now if there is NO message security config for this type of layer then, first add a message security config
            // for the layer and then add a provider config under this message security config

            try {
                ConfigSupport.apply(new SingleConfigCode<SecurityService>() {
                    @Override
                    public Object run(SecurityService param) throws PropertyVetoException, TransactionFailure {
                        MessageSecurityConfig newMessageSecurityConfig = param.createChild(MessageSecurityConfig.class);
                        newMessageSecurityConfig.setAuthLayer(authLayer);
                        param.getMessageSecurityConfig().add(newMessageSecurityConfig);

                        ProviderConfig newProviderConfig = newMessageSecurityConfig.createChild(ProviderConfig.class);

                        populateProviderConfigElement(newProviderConfig);
                        newMessageSecurityConfig.getProviderConfig().add(newProviderConfig);

                        // Depending on the providerType of the new provider the isDefaultProvider=true results in creation of
                        // either default-provider attribute or default-client-provider or BOTH in the message
                        // security config object
                        if (isDefaultProvider) {
                            if (providerType.equals(SERVER) || providerType.equals(CLIENT_SERVER)) {
                                newMessageSecurityConfig.setDefaultProvider(providerId);
                            }

                            if (providerType.equals(CLIENT) || providerType.equals(CLIENT_SERVER)) {
                                newMessageSecurityConfig.setDefaultClientProvider(providerId);
                            }
                        }

                        return newMessageSecurityConfig;
                    }
                }, securityService);
            } catch (TransactionFailure e) {
                report.setMessage(localStrings.getLocalString("create.message.security.provider.fail",
                        "Creation of message security provider named {0} failed", providerId));
                report.setActionExitCode(FAILURE);
                report.setFailureCause(e);
                return;
            }

            report.setActionExitCode(SUCCESS);
        }
    }

    private void populateProviderConfigElement(ProviderConfig newProviderConfig) throws PropertyVetoException, TransactionFailure {
        newProviderConfig.setClassName(providerClass);
        newProviderConfig.setProviderId(providerId);
        newProviderConfig.setProviderType(providerType);

        // Create a new RequestPolicy config and add as child of this new Provider Config
        RequestPolicy newRequestPolicy = newProviderConfig.createChild(RequestPolicy.class);
        newRequestPolicy.setAuthSource(requestAuthSource);
        newRequestPolicy.setAuthRecipient(requestAuthRecipient);
        newProviderConfig.setRequestPolicy(newRequestPolicy);

        // Create a new ResponsePolicy config and add as child of this new Provider Config
        ResponsePolicy newResponsePolicy = newProviderConfig.createChild(ResponsePolicy.class);
        newResponsePolicy.setAuthSource(responseAuthSource);
        newResponsePolicy.setAuthRecipient(responseAuthRecipient);
        newProviderConfig.setResponsePolicy(newResponsePolicy);

        // Add properties
        if (properties != null) {
            for (Object propname : properties.keySet()) {
                Property newProperty = newProviderConfig.createChild(Property.class);
                newProperty.setName((String) propname);
                newProperty.setValue(properties.getProperty((String) propname));
                newProviderConfig.getProperty().add(newProperty);
            }
        }
    }
}
