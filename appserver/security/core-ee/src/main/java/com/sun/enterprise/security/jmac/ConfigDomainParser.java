/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.jmac;

import static java.util.logging.Level.FINE;
import static java.util.regex.Matcher.quoteReplacement;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.epicyro.config.factory.ConfigParser;
import org.glassfish.epicyro.config.helper.AuthMessagePolicy;
import org.glassfish.epicyro.data.AuthModuleConfig;
import org.glassfish.epicyro.data.AuthModulesLayerConfig;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.config.types.Property;

import com.sun.enterprise.config.serverbeans.MessageSecurityConfig;
import com.sun.enterprise.config.serverbeans.ProviderConfig;
import com.sun.enterprise.config.serverbeans.RequestPolicy;
import com.sun.enterprise.config.serverbeans.ResponsePolicy;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.logging.LogDomains;

import jakarta.security.auth.message.MessagePolicy;

/**
 * Parser for message-security-config in domain.xml
 */
public class ConfigDomainParser implements ConfigParser {

    private static final Logger _logger = LogDomains.getLogger(ConfigDomainParser.class, LogDomains.SECURITY_LOGGER);
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{\\{(.*?)}}|\\$\\{(.*?)}");

    // The authentication modules per layer (SOAP or Servlet)
    private final Map<String, AuthModulesLayerConfig>  authModuleLayers = new HashMap<>();
    private final Set<String> layersWithDefault = new HashSet<>();

    public ConfigDomainParser() {
    }

    @Override
    public void initialize(Object service) throws IOException {
        if (service == null && Globals.getDefaultHabitat() != null) {
            service = Globals.getDefaultHabitat().getService(SecurityService.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        }

        if (service instanceof SecurityService) {
            processServerConfig((SecurityService) service, authModuleLayers);
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

    private void processServerConfig(SecurityService service, Map<String, AuthModulesLayerConfig> newAuthModuleLayers) throws IOException {
        List<MessageSecurityConfig> messageSecurityConfigs = service.getMessageSecurityConfig();

        if (messageSecurityConfigs != null) {

            for (MessageSecurityConfig messageSecurityConfig : messageSecurityConfigs) {

                // single message-security-config for each auth-layer
                // auth-layer is synonymous with intercept

                String authLayer = parseInterceptEntry(messageSecurityConfig, newAuthModuleLayers);

                List<ProviderConfig> providers = messageSecurityConfig.getProviderConfig();

                if (providers != null) {
                    for (ProviderConfig provider : providers) {
                        parseIDEntry(provider, newAuthModuleLayers, authLayer);
                    }
                }
            }
        }
    }

    private String parseInterceptEntry(MessageSecurityConfig msgConfig, Map<String, AuthModulesLayerConfig> newConfig) throws IOException {
        String authLayer = msgConfig.getAuthLayer();
        String defaultServerID = msgConfig.getDefaultProvider();
        String defaultClientID = msgConfig.getDefaultClientProvider();

        if (_logger.isLoggable(FINE)) {
            _logger.fine("Intercept Entry: " + "\n    intercept: " + authLayer + "\n    defaultServerID: " + defaultServerID
                    + "\n    defaultClientID:  " + defaultClientID);
        }

        if (defaultServerID != null || defaultClientID != null) {
            layersWithDefault.add(authLayer);
        }

        AuthModulesLayerConfig authModulesLayerConfig = newConfig.get(authLayer);
        if (authModulesLayerConfig != null) {
            throw new IOException("found multiple MessageSecurityConfig " + "entries with the same auth-layer");
        }

        // Create new intercept entry
        authModulesLayerConfig = new AuthModulesLayerConfig(defaultClientID, defaultServerID, null);
        newConfig.put(authLayer, authModulesLayerConfig);

        return authLayer;
    }

    private void parseIDEntry(ProviderConfig providerConfig, Map<String, AuthModulesLayerConfig> newConfig, String intercept) throws IOException {
        String providerId = providerConfig.getProviderId();
        String providerType = providerConfig.getProviderType();
        String moduleClass = providerConfig.getClassName();

        MessagePolicy requestPolicy = parsePolicy(providerConfig.getRequestPolicy());
        MessagePolicy responsePolicy = parsePolicy(providerConfig.getResponsePolicy());

        // Expand the module options
        Map<String, Object> options = getModuleOptions(providerConfig);

        if (_logger.isLoggable(FINE)) {
            _logger.fine("ID Entry: " + "\n    module class: " + moduleClass + "\n    id: " + providerId + "\n    type: " + providerType
                    + "\n    request policy: " + requestPolicy + "\n    response policy: " + responsePolicy + "\n    options: " + options);
        }

        AuthModuleConfig AuthModuleConfig = new AuthModuleConfig(providerType, moduleClass, requestPolicy, responsePolicy, options);

        AuthModulesLayerConfig AuthModulesLayerConfig = newConfig.get(intercept);
        if (AuthModulesLayerConfig == null) {
            throw new IOException("intercept entry for " + intercept + " must be specified before ID entries");
        }

        if (AuthModulesLayerConfig.getAuthModules() == null) {
            AuthModulesLayerConfig.setIdMap(new HashMap<>());
        }

        // Map id to Intercept
        AuthModulesLayerConfig.getAuthModules().put(providerId, AuthModuleConfig);
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

    private MessagePolicy parsePolicy(RequestPolicy policy) {
        if (policy == null) {
            return null;
        }

        return AuthMessagePolicy.getMessagePolicy(policy.getAuthSource(), policy.getAuthRecipient());
    }

    private MessagePolicy parsePolicy(ResponsePolicy policy) {
        if (policy == null) {
            return null;
        }

        return AuthMessagePolicy.getMessagePolicy(policy.getAuthSource(), policy.getAuthRecipient());
    }

    private Map<String, Object> getModuleOptions(ProviderConfig providerConfig) {
        Map<String, Object> options = new HashMap<>();

        List<Property> properties = providerConfig.getProperty();
        if (properties != null) {
            for (Property property : properties) {
                try {
                    options.put(property.getName(), expand(property.getValue()));
                } catch (IllegalStateException ee) {
                    // log warning and give the provider a chance to
                    // interpret value itself.
                    _logger.log(FINE, "jmac.unexpandedproperty");
                    options.put(property.getName(), property.getValue());
                }
            }
        }

        return options;
    }
}
