/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.appclient;

import static java.lang.System.Logger.Level.ERROR;
import static java.util.regex.Matcher.quoteReplacement;

import com.sun.enterprise.security.common.Util;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Property;
import org.glassfish.appclient.client.acc.config.ProviderConfig;
import org.glassfish.appclient.client.acc.config.RequestPolicy;
import org.glassfish.appclient.client.acc.config.ResponsePolicy;
import org.glassfish.epicyro.config.factory.ConfigParser;
import org.glassfish.epicyro.config.helper.AuthMessagePolicy;
import org.glassfish.epicyro.data.AuthModuleConfig;
import org.glassfish.epicyro.data.AuthModulesLayerConfig;
import org.glassfish.internal.api.Globals;

/**
 * Parser for message-security-config in glassfish-acc.xml
 */
public class ConfigXMLParser implements ConfigParser {
    private static final Logger LOG = System.getLogger(ConfigXMLParser.class.getName());

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{\\{(.*?)}}|\\$\\{(.*?)}");

    // configuration info
    private final Map<String, AuthModulesLayerConfig> authModuleLayers = new HashMap<>();
    private final Set<String> layersWithDefault = new HashSet<>();
    private List<MessageSecurityConfig> msgSecConfigs;
    private static final String ACC_XML = "glassfish-acc.xml.url";

    public ConfigXMLParser() {
    }

    public void initialize(List<MessageSecurityConfig> msgConfigs) throws IOException {
        this.msgSecConfigs = msgConfigs;
        if (this.msgSecConfigs != null) {
            processClientConfigContext(authModuleLayers);
        }
    }

    private void processClientConfigContext(Map<String, AuthModulesLayerConfig> newConfig) throws IOException {
        // auth-layer
        String authLayer = null;

        List<MessageSecurityConfig> msgConfigs = this.msgSecConfigs;

        for (MessageSecurityConfig messageSecurityConfig : msgConfigs) {
            authLayer = parseInterceptEntry(messageSecurityConfig, newConfig);
            List<ProviderConfig> providerConfigurations = messageSecurityConfig.getProviderConfig();
            for (ProviderConfig providerConfiguration : providerConfigurations) {
                parseIDEntry(providerConfiguration, newConfig, authLayer);
            }
        }

    }

    @Override
    public Map<String, AuthModulesLayerConfig> getAuthModuleLayers() {
        return authModuleLayers;
    }

    @Override
    public Set<String> getLayersWithDefault() {
        return layersWithDefault;
    }

    private String parseInterceptEntry(MessageSecurityConfig msgConfig, Map<String, AuthModulesLayerConfig> newConfig) throws IOException {
        MessageSecurityConfig clientMsgSecConfig = msgConfig;
        String authLayer = clientMsgSecConfig.getAuthLayer();
        String defaultServerID = clientMsgSecConfig.getDefaultProvider();
        String defaultClientID = clientMsgSecConfig.getDefaultClientProvider();

        LOG.log(Level.DEBUG, "AuthLayer Entry:\n AuthLayer: {0}\n defaultServerID: {1}\n defaultClientID: {2}", authLayer, defaultServerID,
                defaultClientID);

        if (defaultServerID != null || defaultClientID != null) {
            layersWithDefault.add(authLayer);
        }

        AuthModulesLayerConfig authModulesLayerConfig = newConfig.get(authLayer);
        if (authModulesLayerConfig != null) {
            throw new IOException("found multiple MessageSecurityConfig " + "entries with the same auth-layer");
        }

        // Create new AuthLayer entry
        authModulesLayerConfig = new AuthModulesLayerConfig(defaultClientID, defaultServerID, null);
        newConfig.put(authLayer, authModulesLayerConfig);

        return authLayer;
    }

    // duplicate implementation for clientbeans config
    private void parseIDEntry(ProviderConfig providerConfig, Map<String, AuthModulesLayerConfig> newConfig, String authLayer) throws IOException {
        String id = providerConfig.getProviderId();
        String type = providerConfig.getProviderType();
        String moduleClass = providerConfig.getClassName();
        MessagePolicy requestPolicy = parsePolicy(providerConfig.getRequestPolicy());
        MessagePolicy responsePolicy = parsePolicy(providerConfig.getResponsePolicy());

        // get the module options

        Map<String, Object> options = new HashMap<>();
        List<Property> properties = providerConfig.getProperty();
        for (Property property : properties) {
            try {
                options.put(property.getName(), expand(property.getValue()));
            } catch (IllegalStateException ee) {
                // log warning and give the provider a chance to interpret value itself.
                LOG.log(Level.WARNING, "SEC1200: Unable to expand provider property value, unexpanded value passed to provider.");
                options.put(property.getName(), property.getValue());
            }
        }

        LOG.log(Level.DEBUG,
                "ID Entry: \n module class: {0}\n id: {1}\n type: {2}\n" + " request policy: {3}\n response policy: {4}\n options: {5}",
                moduleClass, id, type, requestPolicy, responsePolicy, options);

        // create ID entry

        AuthModuleConfig authModuleConfig = new AuthModuleConfig(type, moduleClass, requestPolicy, responsePolicy, options);

        AuthModulesLayerConfig authModulesLayerConfig = newConfig.get(authLayer);
        if (authModulesLayerConfig == null) {
            throw new IOException("authLayer entry for " + authLayer + " must be specified before ID entries");
        }

        if (authModulesLayerConfig.getAuthModules() == null) {
            authModulesLayerConfig.setIdMap(new HashMap<>());
        }

        // Map id to authLayer
        authModulesLayerConfig.getAuthModules().put(id, authModuleConfig);
    }

    private String expand(String rawProperty) {
        Matcher propertyMatcher = PROPERTY_PATTERN.matcher(rawProperty);
        StringBuilder propertyBuilder = new StringBuilder();
        while (propertyMatcher.find()) {
            // Check if the ignore pattern matched
            if (propertyMatcher.group(1) != null) {
                // Ignore ${{...}} matched, so just append everything
                propertyMatcher.appendReplacement(propertyBuilder, quoteReplacement(propertyMatcher.group()));
            } else {

                String replacement = System.getProperty(propertyMatcher.group(2));
                if (replacement == null) {
                    throw new IllegalStateException("No system property for " + propertyMatcher.group(2));
                }

                // The replacement pattern matched
                propertyMatcher.appendReplacement(propertyBuilder, quoteReplacement(replacement));
            }
        }
        propertyMatcher.appendTail(propertyBuilder);

        return propertyBuilder.toString();
    }

    private MessagePolicy parsePolicy(Object policy) {
        if (policy == null) {
            return null;
        }

        String authSource = null;
        String authRecipient = null;

        if (policy instanceof RequestPolicy) {
            RequestPolicy clientRequestPolicy = (RequestPolicy) policy;
            authSource = clientRequestPolicy.getAuthSource();
            authRecipient = clientRequestPolicy.getAuthRecipient();
        } else if (policy instanceof ResponsePolicy) {
            ResponsePolicy clientResponsePolicy = (ResponsePolicy) policy;
            authSource = clientResponsePolicy.getAuthSource();
            authRecipient = clientResponsePolicy.getAuthRecipient();
        }

        return AuthMessagePolicy.getMessagePolicy(authSource, authRecipient);
    }

    @Override
    public void initialize(Object config) throws IOException {
        String glassFishAccXml = System.getProperty(ACC_XML, "glassfish-acc.xml");

        List<MessageSecurityConfig> msgconfigs = null;
        if (Globals.getDefaultHabitat() == null && glassFishAccXml != null && new File(glassFishAccXml).exists()) {
            try (InputStream is = new FileInputStream(glassFishAccXml)) {
                ClientContainer clientContainer = (ClientContainer)
                        JAXBContext.newInstance(ClientContainer.class)
                                   .createUnmarshaller()
                                   .unmarshal(is);

                msgconfigs = clientContainer.getMessageSecurityConfig();
            } catch (JAXBException ex) {
                LOG.log(ERROR, "Failed to parse glassfish-acc.xml", ex);
            }
        } else {
            msgconfigs = Util.getInstance().getAppClientMsgSecConfigs();
        }

        this.initialize(msgconfigs);
    }
}
