/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.domain;

import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainException;
import com.sun.enterprise.config.modularity.CustomizationTokensProviderFactory;
import com.sun.enterprise.config.modularity.customization.ConfigCustomizationToken;
import com.sun.enterprise.config.modularity.customization.CustomizationTokensProvider;
import com.sun.enterprise.config.modularity.customization.FileTypeDetails;
import com.sun.enterprise.config.modularity.customization.PortTypeDetails;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.net.NetUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.text.MessageFormat.format;

/**
 * Client class to retrieve customize tokens.
 */
public class CustomTokenClient {

    /** Place holder for the PORT BASE value in domain.xml */
    public static final String PORTBASE_PLACE_HOLDER = "PORT_BASE";

    /** Place holder for the custom tokens in domain.xml */
    public static final String CUSTOM_TOKEN_PLACE_HOLDER = "TOKENS_HERE";
    public static final String DEFAULT_TOKEN_PLACE_HOLDER = "DEFAULT_TOKENS_HERE";

    private final DomainConfig _domainConfig;

    public CustomTokenClient(DomainConfig domainConfig) {
        _domainConfig = domainConfig;
    }

    /**
     * Get's the substitutable custom tokens.
     *
     * @return {@link Map} of substitutable tokens, or empty Map if no custom token found.
     * @throws DomainException If error occurred in retrieving the custom tokens.
     */
    public Map<String, String> getSubstitutableTokens() throws DomainException {
        CustomizationTokensProvider provider = CustomizationTokensProviderFactory.createCustomizationTokensProvider();
        Map<String, String> generatedTokens = new HashMap<>();
        String lineSeparator = System.lineSeparator();
        int noOfTokens = 0;
        try {
            List<ConfigCustomizationToken> customTokens = provider.getPresentConfigCustomizationTokens();
            if (!customTokens.isEmpty()) {
                StringBuffer generatedSysTags = new StringBuffer();

                // Check presence of token place-holder
                Set<Integer> usedPorts = new HashSet<>();
                Properties domainProps = _domainConfig.getDomainProperties();
                String portBase = (String) _domainConfig.get(DomainConfig.K_PORTBASE);

                Map<String, String> filePaths = new HashMap<>(3, 1);
                filePaths.put(SystemPropertyConstants.INSTALL_ROOT_PROPERTY,
                    System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
                filePaths.put(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY,
                    System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY));
                filePaths.put(SystemPropertyConstants.JAVA_ROOT_PROPERTY,
                    System.getProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY));
                noOfTokens = customTokens.size();
                for (ConfigCustomizationToken token : customTokens) {
                    String name = token.getName();
                    // Check for valid custom token parameters.
                    if (isNullOrEmpty(name) || isNullOrEmpty(token.getValue()) || isNullOrEmpty(token.getDescription())) {
                        throw new IllegalArgumentException(format(
                            "Invalid token, Empty/null values are not allowed for token name/value/description: {0}/{1}/{2}",
                            name, token.getValue(), token.getDescription()));
                    }
                    switch (token.getCustomizationType()) {
                    case PORT:
                        Integer port = null;
                        if (domainProps.containsKey(name)) {
                            port = Integer.valueOf(domainProps.getProperty(token.getName()));
                            if (!NetUtils.isPortFree(port)) {
                                throw new DomainException(format("Given port {0} is not free.", port));
                            }
                        } else {
                          Integer firstPortTried = port;
                            if (portBase != null && token.getTokenTypeDetails() instanceof PortTypeDetails) {
                                PortTypeDetails portTypeDetails = (PortTypeDetails) token.getTokenTypeDetails();
                                port = Integer.parseInt(portBase) + Integer.parseInt(portTypeDetails.getBaseOffset());
                                if (!generatedTokens.containsKey(PORTBASE_PLACE_HOLDER)) {
                                    // Adding a token to persist port base value as a system tag
                                    generatedTokens.put(PORTBASE_PLACE_HOLDER,
                                            SystemPropertyTagBuilder.buildSystemTag(PORTBASE_PLACE_HOLDER, portBase));
                                }
                            } else {
                                port = Integer.valueOf(token.getValue());
                            }
                            // Find next available unused port by incrementing the port value by 1
                            while (!NetUtils.isPortFree(port) && !usedPorts.contains(port)) {
                                if (port > NetUtils.MAX_PORT) {
                                    throw new DomainException(format("No free port available in range {0} - {1} or it is prohibited.", firstPortTried, port));
                                }
                                port++;
                            }
                        }
                        usedPorts.add(port);
                        generatedSysTags.append(SystemPropertyTagBuilder.buildSystemTag(token, port.toString()));
                        break;
                    case FILE:
                        String path = token.getValue();
                        for (Map.Entry<String, String> entry : filePaths.entrySet()) {
                            if (path.contains(entry.getKey())) {
                                path = path.replace(entry.getKey(), entry.getValue());
                                break;
                            }
                        }
                        if (token.getTokenTypeDetails() instanceof FileTypeDetails) {
                            FileTypeDetails details = (FileTypeDetails) token.getTokenTypeDetails();
                            File file = new File(path);
                            switch (details.getExistCondition()) {
                            case MUST_EXIST:
                                if (!file.exists()) {
                                    throw new DomainException(format("Missing file : {0}", file));
                                }
                                break;
                            case MUST_NOT_EXIST:
                                if (file.exists()) {
                                    throw new DomainException(format(
                                        "File {0} must not exist for the domain creation process to succeed", file));
                                }
                                break;
                            case NO_OP:
                                break;
                            }
                        }
                        generatedSysTags.append(SystemPropertyTagBuilder.buildSystemTag(token, path));
                        break;
                    case STRING:
                        generatedSysTags.append(SystemPropertyTagBuilder.buildSystemTag(token));
                        break;
                    }
                    if (--noOfTokens > 0) {
                        generatedSysTags.append(lineSeparator);
                    }
                }
                String tags = generatedSysTags.toString();
                if (!isNullOrEmpty(tags)) {
                    generatedTokens.put(CUSTOM_TOKEN_PLACE_HOLDER, tags);
                }
            }
            List<ConfigCustomizationToken> defaultTokens = provider.getPresentDefaultConfigCustomizationTokens();
            if (!defaultTokens.isEmpty()) {
                StringBuffer defaultSysTags = new StringBuffer();
                noOfTokens = defaultTokens.size();
                for (ConfigCustomizationToken token : defaultTokens) {
                    defaultSysTags.append(SystemPropertyTagBuilder.buildSystemTag(token));
                    if (--noOfTokens > 0) {
                        defaultSysTags.append(lineSeparator);
                    }
                }
                generatedTokens.put(DEFAULT_TOKEN_PLACE_HOLDER, defaultSysTags.toString());
            }
        } catch (DomainException de) {
            throw de;
        } catch (Exception ex) {
            throw new DomainException(ex);
        }
        return generatedTokens;
    }

    /**
     * Check for empty or null input string.
     *
     * @return true Only if given string string is null or empty.
     */
    private boolean isNullOrEmpty(String input) {
        return input == null || input.isEmpty();
    }

    /**
     * A builder class to build the custom tag.
     */
    private static class SystemPropertyTagBuilder {

        private static final String placeHolderTagWithDesc = "<system-property name=\"%%%NAME%%%\" value=\"%%%VALUE%%%\" description=\"%%%DESCRIPTION%%%\" />";
        private static final String placeHolderTagWithoutDesc = "<system-property name=\"%%%NAME%%%\" value=\"%%%VALUE%%%\" />";
        private static final String namePlaceHolder = "%%%NAME%%%";
        private static final String valuePlaceHolder = "%%%VALUE%%%";
        private static final String descriptionPlaceHolder = "%%%DESCRIPTION%%%";

        /**
         * Build the System tag for the given token & value.
         */
        private static String buildSystemTag(ConfigCustomizationToken token, String value) {
            String builtTag = placeHolderTagWithDesc.replace(valuePlaceHolder, value);
            builtTag = builtTag.replace(descriptionPlaceHolder, token.getDescription());
            builtTag = builtTag.replace(namePlaceHolder, token.getName());
            return builtTag;
        }

        private static String buildSystemTag(ConfigCustomizationToken token) {
            return buildSystemTag(token, token.getValue());
        }

        /**
         * Build the System tag for the given name & value.
         */
        private static String buildSystemTag(String name, String value) {
            String builtTag = placeHolderTagWithoutDesc.replace(valuePlaceHolder, value);
            builtTag = builtTag.replace(namePlaceHolder, name);
            return builtTag;
        }
    }
}
