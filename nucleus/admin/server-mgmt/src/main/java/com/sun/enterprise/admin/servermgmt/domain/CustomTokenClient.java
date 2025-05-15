/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

    /** Place holder for the custom tokens in domain.xml */
    private static final String CUSTOM_TOKEN_PLACE_HOLDER = "TOKENS_HERE";
    private static final String DEFAULT_TOKEN_PLACE_HOLDER = "DEFAULT_TOKENS_HERE";

    private final DomainConfig domainConfig;

    public CustomTokenClient(DomainConfig domainConfig) {
        this.domainConfig = domainConfig;
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
        int noOfTokens = 0;
        try {
            List<ConfigCustomizationToken> customTokens = provider.getPresentConfigCustomizationTokens();
            if (!customTokens.isEmpty()) {
                StringBuilder generatedSysTags = new StringBuilder();
                Set<Integer> usedPorts = new HashSet<>();
                Properties domainProps = domainConfig.getDomainProperties();
                Integer portBase = getPortBase(domainConfig);

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
                        throw new DomainException(format(
                            "Invalid token, Empty/null values are not allowed for token name, value and description: {0}",
                            token));
                    }
                    switch (token.getCustomizationType()) {
                        case PORT:
                            Integer port = resolvePort(token, portBase, generatedTokens, usedPorts, domainProps);
                            generatedSysTags.append(SystemPropertyTagBuilder.buildSystemTag(token, port.toString()));
                            break;
                        case FILE:
                            String path = resolvePath(token, filePaths);
                            generatedSysTags.append(SystemPropertyTagBuilder.buildSystemTag(token, path));
                            break;
                        case STRING:
                            generatedSysTags.append(SystemPropertyTagBuilder.buildSystemTag(token));
                            break;
                        default:
                            throw new DomainException(format("Unknown token type: {0}", token.getCustomizationType()));
                    }
                    if (--noOfTokens > 0) {
                        generatedSysTags.append(System.lineSeparator());
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
                        defaultSysTags.append(System.lineSeparator());
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

    private String resolvePath(ConfigCustomizationToken token, Map<String, String> filePaths) throws DomainException {
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
                        throw new DomainException(format("Missing file: {0}, token: {1}", file, token));
                    }
                    break;
                case MUST_NOT_EXIST:
                    if (file.exists()) {
                        throw new DomainException(format(
                            "File {0} must not exist for the domain creation process to succeed, token: {1}",
                            file, token));
                    }
                    break;
                case NO_OP:
                    break;
                default:
                    throw new DomainException(format("Unknown file existence condition: {0} for token: {1}",
                        details.getExistCondition(), token));
            }
        }
        return path;
    }


    private Integer getPortBase(DomainConfig domainConfig2) {
        String portBase = (String) domainConfig.get(DomainConfig.K_PORTBASE);
        return portBase == null ? null : Integer.valueOf(portBase);
    }


    /**
     * Limitations of this implementation:
     * <ul>
     * <li>When using the portBase (not null), all ports have to be generated. Otherwise, preset
     * port values would have to be put to usedPorts before generating the ports.
     * </ul>
     * @param checkPorts
     */
    private Integer resolvePort(ConfigCustomizationToken token, Integer portBase, Map<String, String> generatedTokens,
        Set<Integer> usedPorts, Properties domainProps) throws DomainException {
        Integer port = null;
        String name = token.getName();
        if (domainProps.containsKey(name)) {
            port = Integer.valueOf(domainProps.getProperty(name));
        } else {
            if (portBase != null && token.getTokenTypeDetails() instanceof PortTypeDetails) {
                PortTypeDetails portTypeDetails = (PortTypeDetails) token.getTokenTypeDetails();
                int firstPortTried = portBase + Integer.parseInt(portTypeDetails.getBaseOffset());
                generatedTokens.computeIfAbsent(name, k -> SystemPropertyTagBuilder.buildSystemTag(name, portBase));
                port = generateFreePort(usedPorts, token, firstPortTried);
            } else {
                port = Integer.valueOf(token.getValue());
            }
        }
        usedPorts.add(port);
        return port;
    }

    /** Find next available unused port by incrementing the port value by 1 */
    private int generateFreePort(Set<Integer> usedPorts, ConfigCustomizationToken token, Integer firstPortTried)
        throws DomainException {
        int port = firstPortTried;
        while (usedPorts.contains(port) || !NetUtils.isPortFree(port)) {
            if (port > NetUtils.MAX_PORT) {
                throw new DomainException(format(
                    "No free port is available in range {0} - {1} or it is prohibited. Token: {2}",
                    firstPortTried, NetUtils.MAX_PORT, token));
            }
            port++;
        }
        return port;
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
        private static String buildSystemTag(String name, Integer value) {
            String builtTag = placeHolderTagWithoutDesc.replace(valuePlaceHolder, value.toString());
            builtTag = builtTag.replace(namePlaceHolder, name);
            return builtTag;
        }
    }
}
