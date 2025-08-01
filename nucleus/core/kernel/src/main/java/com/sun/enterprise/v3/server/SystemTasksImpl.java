/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.server;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.io.ServerDirs;
import com.sun.enterprise.util.net.NetUtils;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.TranslatedConfigView;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.InitRunLevel;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.INFO;
import static org.glassfish.embeddable.GlassFishVariable.HOST_NAME;
import static org.glassfish.embeddable.GlassFishVariable.JAVA_HOME;
import static org.glassfish.embeddable.GlassFishVariable.JAVA_ROOT;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 * Init run level service to take care of vm related tasks.
 *
 * @author Jerome Dochez
 * @author Byron Nevins
 */
@Service
@RunLevel(value = InitRunLevel.VAL, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class SystemTasksImpl implements SystemTasks, PostConstruct {

    private static final Logger LOG = KernelLoggerInfo.getLogger();
    private final static LocalStringsImpl I18N = new LocalStringsImpl(SystemTasks.class);

    @Inject
    private ServerEnvironment env;

    // in embedded environment, JavaConfig is pointless, so make this optional
    @Inject
    @Optional
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private JavaConfig javaConfig;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Server server;

    @Inject
    private Domain domain;


    @Override
    public void postConstruct() {
        if (env.isEmbedded()) {
            initEmbeddedSystemOptions();
            LOG.log(INFO, "Loaded embedded server named: {0}", server.getName());
            return;
        }
        setVersion();
        setSystemPropertiesFromEnv();
        setSystemPropertiesFromDomainXml();
        resolveJavaConfig();
        LOG.log(INFO, "Loaded server named: {0}", server.getName());
    }


    @Override
    public void writePidFile() {
        File pidFile = null;
        try {
            ServerDirs serverDirs = new ServerDirs(env.getInstanceRoot());
            ProcessUtils.saveCurrentPid(serverDirs.getLastPidFile());
            pidFile = serverDirs.getPidFile();
            pidFile.deleteOnExit();
            ProcessUtils.saveCurrentPid(pidFile);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, I18N.get("internal_error", e), e);
        }
    }

    /**
     * In embedded environment, we don't have a domain.xml, but we still have defaults.
     * System properties should be set from the outside except few.
     */
    private void initEmbeddedSystemOptions() {
        // JMS may not be available.
        setProperty("org.glassfish.jms.InitializeOnDemand", "true", false);
    }

    private void setVersion() {
        setProperty("glassfish.version", Version.getProductIdInfo(), true);
    }

    private void setSystemPropertiesFromEnv() {
        // adding our version of some system properties.
        setProperty(JAVA_ROOT.getSystemPropertyName(), System.getProperty(JAVA_HOME.getSystemPropertyName()), true);
        String hostname = "localhost";
        try {
            // canonical name checks to make sure host is proper
            hostname = NetUtils.getCanonicalHostName();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, KernelLoggerInfo.exceptionHostname, ex);
        }
        if (hostname != null) {
            setProperty(HOST_NAME.getSystemPropertyName(), hostname, false);
        }
    }

    private void setSystemPropertiesFromDomainXml() {
        // precedence order from high to low
        // 0. server
        // 1. cluster
        // 2. <server>-config or <cluster>-config
        // 3. domain
        // so we need to add System Properties in *reverse order* to get the
        // right precedence.

        List<SystemProperty> domainSPList = domain.getSystemProperty();
        List<SystemProperty> configSPList = getConfigSystemProperties();
        Cluster cluster = server.getCluster();
        final List<SystemProperty> clusterSPList = cluster == null ? null : cluster.getSystemProperty();
        List<SystemProperty> serverSPList = server.getSystemProperty();

        setSystemProperties(domainSPList);
        setSystemProperties(configSPList);

        if (clusterSPList != null) {
            setSystemProperties(clusterSPList);

        }
        setSystemProperties(serverSPList);
    }

    private List<SystemProperty> getConfigSystemProperties() {
        try {
            String configName = server.getConfigRef();
            Configs configs = domain.getConfigs();
            List<Config> configsList = configs.getConfig();
            Config config = null;

            for (Config c : configsList) {
                if (c.getName().equals(configName)) {
                    config = c;
                    break;
                }
            }
            return config == null ? Collections.emptyList() : config.getSystemProperty();
        }
        catch (Exception e) {  //possible NPE if domain.xml has issues!
            return Collections.emptyList();
        }
    }

    private void resolveJavaConfig() {
        if (javaConfig != null) {
            Pattern p = Pattern.compile("-D([^=]*)=(.*)");
            for (String jvmOption : javaConfig.getJvmOptions()) {
                Matcher m = p.matcher(jvmOption);
                if (m.matches()) {
                    String name = m.group(1);
                    String value = TranslatedConfigView.expandValue(m.group(2));
                    setProperty(name, value, true);
                }
            }
        }
    }

    private void setSystemProperties(List<SystemProperty> spList) {
        for (SystemProperty sp : spList) {
            String name = sp.getName();
            String value = sp.getValue();

            if (ok(name)) {
                setProperty(name, value, true);
            }
        }
    }

    private static boolean ok(String s) {
        return s != null && !s.isEmpty();
    }
}
