/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018-2021 Oracle and/or its affiliates. All rights reserved.
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

/*
 * RepositoryConfig.java
 *
 * Created on August 19, 2003, 1:59 PM
 */
package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;

/**
 * This class represents a repository configuration. A repository can be either a domain, a node
 * agent, or a server
 * instance. Configuration specific to each (DomainConfig, AgentConfig, InstanceConfig) is derived
 * from this class.
 *
 * A repository config consists of the following attributes:
 * <ol>
 * <li>repositoryName -- domain or node agent name (e.g. domain1 or agent1)
 * <li>repositoryRoot -- the parent directory of the repository (e.g. $installDir/domains or
 * $installDir/agents)
 * <li>instanceName -- the optional server instance name (e.g. server1)
 * <li>configurationName -- the optional configuration name of the server instance (e.g.
 * default-config).
 * </ol>
 *
 * Using (repositoryName, repositoryRoot, instanceName, configurationName) syntax. Here are the
 * following permutations:
 * <ol>
 * <li>For a domain: (domainRootDirectory, domainName, null, null) e.g. ("/sun/appserver/domains",
 * "domain1", null, null)
 * <li>For a node agent: (agentRootDirectory, agentName, "agent", null) e.g
 * ("/sun/appserver/agents", "agent1", "agent",
 * null). Note that the instance name of a node agent is always the literal string "agent".
 * <li>For a server instance (agentRootDirectory, agentName, instanceName, configName) e.g.
 * ("/sun/appserver/agents",
 * "agent1", "server1", "default-config")
 * </ol>
 *
 * The RepositoryConfig class is an extensible HashMap that can contain any attributes, but also
 * relies on two system
 * properties being set:
 * <ol>
 * <li>com.sun.aas.installRoot -- installation root directory stored under the K_INSTALL_ROOT key.
 * <li>com.sun.aas.configRoot -- configuration root (for locating asenv.conf) stored under the
 * K_CONFIG_ROOT key.
 * </ol>
 *
 * @author kebbs
 */
public class RepositoryConfig extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public static final String K_INSTALL_ROOT = "install.root";
    public static final String K_CONFIG_ROOT = "config.root";
    public static final String K_REFRESH_CONFIG_CONTEXT = "refresh.cc";
    //Name of the domain or node agent. Cannot be null.
    private final String _repositoryName;
    //Root directory where the domain or node agent resides. Cannot be null
    private String _repositoryRoot;
    //Name of the server instance. May be null
    private String _instanceName;
    //Name of the configuration. May be null
    private String _configurationName;

    /**
     * Creates a new instance of RepositoryConfig The K_INSTALL_ROOT and K_CONFIG_ROOT attributes are implicitly set
     */
    public RepositoryConfig(String repositoryName, String repositoryRoot, String instanceName, String configName) {
        _instanceName = instanceName;
        _repositoryName = repositoryName;
        _repositoryRoot = repositoryRoot;
        _configurationName = configName;
        final Map<String, String> envProperties = getEnvProps();

        // Since the changes for the startup, we have the problem of refreshing
        // config context. So, by default, I am making a change to refresh the
        // config context. If some processes (e.g. start-domain) have already
        // created a config context, then they should explicitly say so.
        put(K_REFRESH_CONFIG_CONTEXT, true);
        if (envProperties != null) {
            put(K_INSTALL_ROOT, getFilePath(envProperties.get(INSTALL_ROOT.getPropertyName())));
            put(K_CONFIG_ROOT, getFilePath(envProperties.get(INSTALL_ROOT.getPropertyName())));
        }
    }

    public RepositoryConfig(String repositoryName, String repositoryRoot, String instanceName) {
        this(repositoryName, repositoryRoot, instanceName, null);
    }

    public RepositoryConfig(String repositoryName, String repositoryRoot) {
        this(repositoryName, repositoryRoot, null);
    }

    public RepositoryConfig() {
        this(System.getProperty(INSTANCE_ROOT.getSystemPropertyName()));
    }

    /**
     * Creates a new instance of RepositoryConfig defined using the system property com.sun.aas.instanceRoot. It is assumed
     * that this system property is a directory of the form: <repositoryRootDirectory>/<repositoryName>/<instanceName>
     */
    public RepositoryConfig(String instanceRootString) {
        final File instanceRoot = new File(instanceRootString);
        final File repositoryDir = instanceRoot.getParentFile();
        _instanceName = instanceRoot.getName();
        _repositoryName = repositoryDir.getName();
        _repositoryRoot = FileUtils.makeForwardSlashes(repositoryDir.getParentFile().getAbsolutePath());
        _configurationName = null;
        final Map<String, String> envProperties = getEnvProps();
        if (envProperties != null) {
            put(K_INSTALL_ROOT, envProperties.get(INSTALL_ROOT.getPropertyName()));
            put(K_CONFIG_ROOT, getFilePath(envProperties.get(SystemPropertyConstants.CONFIG_ROOT_PROPERTY)));
        }
    }

    @Override
    public String toString() {
        return ("repositoryRoot " + _repositoryRoot + " repositoryName " + _repositoryName + " instanceName " + _instanceName
                + " configurationName " + _configurationName);
    }

    protected String getFilePath(String propertyName) {
        File f = new File(propertyName);
        return FileUtils.makeForwardSlashes(f.getAbsolutePath());
    }

    public void setConfigurationName(String configurationName) {
        _configurationName = configurationName;
    }

    public String getConfigurationName() {
        return _configurationName;
    }

    public String getDisplayName() {
        return getRepositoryName();
    }

    public void setInstanceName(String instanceName) {
        _instanceName = instanceName;
    }

    public String getInstanceName() {
        return _instanceName;
    }

    public String getRepositoryName() {
        return _repositoryName;
    }

    protected void setRepositoryRoot(String repositoryRoot) {
        _repositoryRoot = repositoryRoot;
    }

    public String getRepositoryRoot() {
        return _repositoryRoot;
    }

    public String getInstallRoot() {
        return (String) get(K_INSTALL_ROOT);
    }

    public String getConfigRoot() {
        return (String) get(K_CONFIG_ROOT);
    }

    public Boolean getRefreshConfigContext() {
        return ((Boolean) get(K_REFRESH_CONFIG_CONTEXT));
        //this will never be null, because constructor initializes it to false
    }

    public void setRefreshConfingContext(final boolean refresh) {
        this.put(K_REFRESH_CONFIG_CONTEXT, refresh);
    }

    private Map<String, String> getEnvProps() {
        ASenvPropertyReader pr = new ASenvPropertyReader();
        return pr.getProps();
    }
}
