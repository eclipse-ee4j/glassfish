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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.util.io.FileUtils;
import java.io.File;
import java.util.Collections;
import java.util.List;

import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.InitRunLevel;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.net.NetUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.appserv.server.util.Version;
import com.sun.enterprise.universal.io.SmartFile;

import org.jvnet.hk2.annotations.Optional;
import org.glassfish.hk2.api.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.config.support.TranslatedConfigView;
import org.glassfish.api.admin.ServerEnvironment;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.glassfish.kernel.KernelLoggerInfo;

/**
 * Init run level service to take care of vm related tasks.
 *
 * @author Jerome Dochez
 * @author Byron Nevins
 */
// TODO: eventually use CageBuilder so that this gets triggered when JavaConfig enters Habitat.
@Service
@RunLevel( value=InitRunLevel.VAL, mode=RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class SystemTasksImpl implements SystemTasks, PostConstruct {

    // in embedded environment, JavaConfig is pointless, so make this optional
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME) @Optional

    JavaConfig javaConfig;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    Domain domain;

    Logger _logger = KernelLoggerInfo.getLogger();

    private final static LocalStringsImpl strings = new LocalStringsImpl(SystemTasks.class);

    @Override
    public void postConstruct() {
        setVersion();
        setSystemPropertiesFromEnv();
        setSystemPropertiesFromDomainXml();
        resolveJavaConfig();
        _logger.fine("SystemTasks: loaded server named: " + server.getName());
    }

    @Override
    public void writePidFile() {
        File pidFile = null;

        try {
            pidFile = SmartFile.sanitize(getPidFile());
            File pidFileCopy = new File(pidFile.getPath() + ".prev");
            String pidString = getPidString();
            FileUtils.writeStringToFile(pidString, pidFile);
            FileUtils.writeStringToFile(pidString, pidFileCopy);
        }
        catch (PidException pe) {
            _logger.warning(pe.getMessage());
        }
        catch (Exception e) {
            _logger.warning(strings.get("internal_error", e));
        }
        finally {
            if (pidFile != null) {
                pidFile.deleteOnExit();
            }
        }
    }

    private void setVersion() {
        System.setProperty("glassfish.version", Version.getFullVersion());
    }

    /*
     * Here is where we make the change Post-TP2 to *not* use JVM System Properties
     */
    private void setSystemProperty(String name, String value) {
        System.setProperty(name, value);
    }

    private void setSystemPropertiesFromEnv() {
        // adding our version of some system properties.
        setSystemProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY, System.getProperty("java.home"));

        String hostname = "localhost";


        try {
            // canonical name checks to make sure host is proper
            hostname = NetUtils.getCanonicalHostName();


        }
        catch (Exception ex) {
            if (_logger != null) {
                _logger.log(Level.SEVERE, KernelLoggerInfo.exceptionHostname, ex);
            }
        }
        if (hostname != null) {
            setSystemProperty(SystemPropertyConstants.HOST_NAME_PROPERTY, hostname);
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
        List<SystemProperty> clusterSPList = null;


        if (cluster != null) {
            clusterSPList = cluster.getSystemProperty();


        }
        List<SystemProperty> serverSPList = server.getSystemProperty();

        setSystemProperties(
                domainSPList);
        setSystemProperties(
                configSPList);


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
            return (List<SystemProperty>) (config != null ? config.getSystemProperty() : Collections.emptyList());
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
                    setSystemProperty(m.group(1), TranslatedConfigView.getTranslatedValue(m.group(2)).toString());

                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("Setting " + m.group(1) + " = " + TranslatedConfigView.getTranslatedValue(m.group(2)));
                    }
                }
            }
        }
    }

    private void setSystemProperties(List<SystemProperty> spList) {
        for (SystemProperty sp : spList) {
            String name = sp.getName();
            String value = sp.getValue();

            if (ok(name)) {
                setSystemProperty(name, value);
            }
        }
    }

    private String getPidString() {
        return "" + ProcessUtils.getPid();
    }

    private File getPidFile() throws PidException {
        try {
            String configDirString = System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);

            if (!ok(configDirString))
                throw new PidException(strings.get("internal_error",
                        "Null or empty value for the System Property: "
                        + SystemPropertyConstants.INSTANCE_ROOT_PROPERTY));

            File configDir = new File(new File(configDirString), "config");

            if (!configDir.isDirectory())
                throw new PidException(strings.get("bad_config_dir", configDir));

            File pidFile = new File(configDir, "pid");

            if (pidFile.exists()) {
                if (!pidFile.delete() || pidFile.exists()) {
                    throw new PidException(strings.get("cant_delete_pid_file", pidFile));
                }
            }
            return pidFile;
        }
        catch (PidException pe) {
            throw pe;
        }
        catch (Exception e) {
            throw new PidException(e.getMessage());
        }
    }

    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private static class PidException extends Exception {

        public PidException(String s) {
            super(s);
        }
    }
}
