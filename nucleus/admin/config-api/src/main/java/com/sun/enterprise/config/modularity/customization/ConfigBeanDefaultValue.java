/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.modularity.customization;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * @author Masoud Kalali
 */

import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * Carries the default configuration values for a ConfigBeanProxy
 */
public class ConfigBeanDefaultValue {

    private String location;
    private String xmlConfiguration;
    private String configBeanClassName;
    private boolean replaceCurrentIfExists;
    private List<ConfigCustomizationToken> customizationTokens;

    public String getLocation() {
        return location;
    }

    public String getXmlConfiguration() {
        return xmlConfiguration;
    }

    public String getConfigBeanClassName() {
        return configBeanClassName;
    }

    public boolean replaceCurrentIfExists() {
        return replaceCurrentIfExists;
    }

    public List<ConfigCustomizationToken> getCustomizationTokens() {
        if (customizationTokens == null) {
            customizationTokens = Collections.emptyList();
        }

        return customizationTokens;
    }

    /**
     * @param location the location of the config bean which this configuration is intended to create
     * @param configBeanClassName what is the type of the config bean this configuration is intended for
     * @param xmlConfiguration the XML snippet that represent the mentioned configuration. The XML snippet should be a valid
     * config bean configuration
     * @param replaceCurrentIfExists should this config bean replace an already existing one or not. Note that, this
     * parameter will be processed only if the configuration is intended for a named configuration element. The other
     * condition for the replace to happen is that this configuration get the chance to be processed which means it should
     * be part of an array of config beans intended for a service that has no configuration present in the domain.xml
     * @param customizationTokens
     * @param <U> Type of the config bean which is an extension of ConfigBeanProxy
     */
    public <U extends ConfigBeanProxy> ConfigBeanDefaultValue(String location, String configBeanClassName, String xmlConfiguration,
            boolean replaceCurrentIfExists, List<ConfigCustomizationToken> customizationTokens) {
        this.location = location;
        this.xmlConfiguration = xmlConfiguration;
        this.configBeanClassName = configBeanClassName;
        this.replaceCurrentIfExists = replaceCurrentIfExists;
        this.customizationTokens = customizationTokens;
    }

    /**
     * @param location the location of the config bean which this configuration is intended to create
     * @param configBeanClassName what is the type of the config bean this configuration is intended for
     * @param xmlSnippetFileInputStream An InputStream for the actual configuration which might be a file or anything other
     * InputStream to read the configuration from.
     * @param replaceCurrentIfExists should this config bean replace an already existing one or not. Note that, this
     * parameter will be processed only if the configuration is intended for a named configuration element. The other
     * condition for the replace to happen is that this configuration get the chance to be processed which means it should
     * be part of an array of config beans intended for a service that has no configuration present in the domain.xml
     * @param customizationTokens
     * @param <U> Type of the config bean which is an extension of ConfigBeanProxy
     * @throws Exception If the stream is not readable or closing the stream throws exception constructor will fail with the
     * exception.
     */
    public <U extends ConfigBeanProxy> ConfigBeanDefaultValue(String location, String configBeanClassName,
            InputStream xmlSnippetFileInputStream, boolean replaceCurrentIfExists, List<ConfigCustomizationToken> customizationTokens)
            throws Exception {
        this.location = location;
        this.configBeanClassName = configBeanClassName;
        this.xmlConfiguration = streamToString(xmlSnippetFileInputStream, "utf-8");
        this.replaceCurrentIfExists = replaceCurrentIfExists;
        this.customizationTokens = customizationTokens;
    }

    public boolean addCustomizationToken(ConfigCustomizationToken e) {

        if (customizationTokens == null) {
            customizationTokens = new ArrayList<ConfigCustomizationToken>();
        }
        return customizationTokens.add(e);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setXmlConfiguration(String xmlConfiguration) {
        this.xmlConfiguration = xmlConfiguration;
    }

    public void setConfigBeanClassName(String configBeanClassName) {
        this.configBeanClassName = configBeanClassName;
    }

    public void setReplaceCurrentIfExists(boolean replaceCurrentIfExists) {
        this.replaceCurrentIfExists = replaceCurrentIfExists;
    }

    public void setCustomizationTokens(List<ConfigCustomizationToken> customizationTokens) {
        this.customizationTokens = customizationTokens;
    }

    public ConfigBeanDefaultValue() {

    }

    /**
     * @param ins the InputStream to read and turn it into String
     * @return String equivalent of the stream
     */
    private String streamToString(InputStream ins, String encoding) throws IOException {
        String s = new Scanner(ins, encoding).useDelimiter("\\A").next();
        ins.close();
        return s;
    }

    @Override
    public String toString() {
        return "ConfigBeanDefaultValue{" + "location='" + location + '\'' + ", configBeanClassName='" + configBeanClassName + '\''
                + ", replaceCurrentIfExists=" + replaceCurrentIfExists + '}';
    }
}
