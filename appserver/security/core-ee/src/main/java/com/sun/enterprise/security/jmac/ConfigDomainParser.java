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

package com.sun.enterprise.security.jmac;

import static java.util.regex.Matcher.quoteReplacement;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.config.types.Property;
import org.omnifaces.eleos.config.factory.ConfigParser;
import org.omnifaces.eleos.data.AuthModuleConfig;
import org.omnifaces.eleos.data.AuthModulesLayerConfig;

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

    private static Logger _logger = null;
    static {
        _logger = LogDomains.getLogger(ConfigDomainParser.class, LogDomains.SECURITY_LOGGER);
    }

    private static Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{\\{(.*?)}}|\\$\\{(.*?)}");

    // configuration info
    private Map<String, AuthModulesLayerConfig>  configMap = new HashMap<>();
    private Set<String> layersWithDefault = new HashSet<>();

    public ConfigDomainParser() {
    }

    @Override
    public void initialize(Object service) throws IOException {
        if (service == null && Globals.getDefaultHabitat() != null) {
            service = Globals.getDefaultHabitat().getService(SecurityService.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        }

        if (service instanceof SecurityService) {
            processServerConfig((SecurityService) service, configMap);
        }
    }

    @Override
    public Map<String, AuthModulesLayerConfig> getAuthModuleLayers() {
        return configMap;
    }

    @Override
    public Set<String> getLayersWithDefault() {
        return layersWithDefault;
    }

    private void processServerConfig(SecurityService service, Map newConfig) throws IOException {
        List<MessageSecurityConfig> configList = service.getMessageSecurityConfig();

        if (configList != null) {

            Iterator<MessageSecurityConfig> cit = configList.iterator();

            while (cit.hasNext()) {

                MessageSecurityConfig next = cit.next();

                // single message-security-config for each auth-layer
                // auth-layer is synonymous with intercept

                String authLayer = parseInterceptEntry(next, newConfig);

                List<ProviderConfig> providers = next.getProviderConfig();

                if (providers != null) {
                    for (ProviderConfig provider : providers) {
                        parseIDEntry(provider, newConfig, authLayer);
                    }
                }
            }
        }
    }



    private String parseInterceptEntry(MessageSecurityConfig msgConfig, Map<String, Object> newConfig) throws IOException {
        String authLayer = msgConfig.getAuthLayer();
        String defaultServerID = msgConfig.getDefaultProvider();
        String defaultClientID = msgConfig.getDefaultClientProvider();

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("Intercept Entry: " + "\n    intercept: " + authLayer + "\n    defaultServerID: " + defaultServerID
                    + "\n    defaultClientID:  " + defaultClientID);
        }

        if (defaultServerID != null || defaultClientID != null) {
            layersWithDefault.add(authLayer);
        }

        AuthModulesLayerConfig authModulesLayerConfig = (AuthModulesLayerConfig) newConfig.get(authLayer);
        if (authModulesLayerConfig != null) {
            throw new IOException("found multiple MessageSecurityConfig " + "entries with the same auth-layer");
        }

        // Create new intercept entry
        authModulesLayerConfig = new AuthModulesLayerConfig(defaultClientID, defaultServerID, null);
        newConfig.put(authLayer, authModulesLayerConfig);

        return authLayer;
    }

    private void parseIDEntry(ProviderConfig pConfig, Map newConfig, String intercept) throws IOException {

        String id = pConfig.getProviderId();
        String type = pConfig.getProviderType();
        String moduleClass = pConfig.getClassName();
        MessagePolicy requestPolicy = parsePolicy(pConfig.getRequestPolicy());
        MessagePolicy responsePolicy = parsePolicy(pConfig.getResponsePolicy());

        // get the module options

        Map options = new HashMap();
        String key;
        String value;

        List<Property> pList = pConfig.getProperty();

        if (pList != null) {

            Iterator<Property> pit = pList.iterator();

            while (pit.hasNext()) {

                Property property = pit.next();

                try {
                    options.put(property.getName(), expand(property.getValue()));
                } catch (IllegalStateException ee) {
                    // log warning and give the provider a chance to
                    // interpret value itself.
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, "jmac.unexpandedproperty");
                    }
                    options.put(property.getName(), property.getValue());
                }
            }
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("ID Entry: " + "\n    module class: " + moduleClass + "\n    id: " + id + "\n    type: " + type
                    + "\n    request policy: " + requestPolicy + "\n    response policy: " + responsePolicy + "\n    options: " + options);
        }

        // create ID entry
        AuthModuleConfig idEntry = new AuthModuleConfig(type, moduleClass, requestPolicy, responsePolicy,
                options);

        AuthModulesLayerConfig intEntry = (AuthModulesLayerConfig) newConfig.get(intercept);
        if (intEntry == null) {
            throw new IOException("intercept entry for " + intercept + " must be specified before ID entries");
        }

        if (intEntry.getAuthModules() == null) {
            intEntry.setIdMap(new HashMap());
        }

        // map id to Intercept
        intEntry.getAuthModules().put(id, idEntry);
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

        String authSource = policy.getAuthSource();
        String authRecipient = policy.getAuthRecipient();
        return org.omnifaces.eleos.config.helper.AuthMessagePolicy.getMessagePolicy(authSource, authRecipient);
    }

    private MessagePolicy parsePolicy(ResponsePolicy policy) {
        if (policy == null) {
            return null;
        }

        String authSource = policy.getAuthSource();
        String authRecipient = policy.getAuthRecipient();
        return org.omnifaces.eleos.config.helper.AuthMessagePolicy.getMessagePolicy(authSource, authRecipient);
    }

}
