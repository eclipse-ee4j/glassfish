/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.jms.util;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.connectors.jms.JMSLoggerInfo;
import com.sun.enterprise.connectors.jms.config.JmsService;
import com.sun.enterprise.connectors.jms.inflow.MdbContainerProps;
import com.sun.enterprise.connectors.jms.system.MQAddressList;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipFile;
import com.sun.enterprise.util.zip.ZipFileException;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.ejb.config.MdbContainer;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.RelativePathResolver;
import org.jvnet.hk2.config.types.Property;

import static com.sun.appserv.connectors.internal.api.ConnectorConstants.DEFAULT_JMS_ADAPTER;
import static com.sun.enterprise.util.SystemPropertyConstants.IMQ_LIB_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

public class JmsRaUtil {

    private final static String MQ_RAR = "imqjmsra.rar";

    /** lib/install/applications */
    private static final Path SYSTEM_APP_DIR = Path.of("lib", "install", "applications");

    /** jmsra/META-INF/MANIFEST.MF */
    private static final Path MQ_RAR_MANIFEST = Path.of("META-INF", "MANIFEST.MF");

    // Manifest version tag.
    private final static String MANIFEST_TAG = "Implementation-Version";

    private static final String propName_reconnect_delay_in_seconds =
        "reconnect-delay-in-seconds";
    private static final String propName_reconnect_max_retries =
        "reconnect-max-retries";
    private static final String propName_reconnect_enabled =
        "reconnect-enabled";
    private static final int DEFAULT_RECONNECT_DELAY = 60;
    private static final int DEFAULT_RECONNECT_RETRIES = 60;

    private static final String propName_cmt_max_runtime_exceptions
        = "cmt-max-runtime-exceptions";
    private static final int DEFAULT_CMT_MAX_RUNTIME_EXCEPTIONS = 1;

    private static final String ENABLE_AUTO_CLUSTERING = "com.sun.enterprise.connectors.system.enableAutoClustering";

    private int cmtMaxRuntimeExceptions = DEFAULT_CMT_MAX_RUNTIME_EXCEPTIONS;

    private int reconnectDelayInSeconds = DEFAULT_RECONNECT_DELAY;
    private int reconnectMaxRetries = DEFAULT_RECONNECT_RETRIES;
    private boolean reconnectEnabled;

    JmsService js;
    MQAddressList list;

    private static final Logger _logger = JMSLoggerInfo.getLogger();

    public JmsRaUtil() throws ConnectorRuntimeException {
        this(null);
    }


    public JmsRaUtil(JmsService js) throws ConnectorRuntimeException {
        try {
            if (js != null) {
                this.js = js;
            } else {
                this.js = Globals.get(JmsService.class);
            }
            list = new MQAddressList(this.js);
//            if (isClustered() && ! this.js.getType().equals(
//                ActiveJmsResourceAdapter.REMOTE)) {
//                list.setupForLocalCluster();
//            } else {
//                list.setup();
//            }
        } catch(Exception ce) {
            throw handleException(ce);
        }
    }


    public void setupAddressList() throws ConnectorRuntimeException {
        try {
            list.setup();
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    public String getUrl() {
        try {
            return list.toString();
        } catch (Exception e) {
            return null;
        }
    }


    public static boolean isClustered(List clusters, String instanceName) {
        return (enableClustering() && isServerClustered(clusters, instanceName));
    }


    /**
     * Return true if the given server instance is part of a cluster.
     */
    public static boolean isServerClustered(List clusters, String instanceName) {
        return (getClusterForServer(clusters, instanceName) != null);
    }


    public static Cluster getClusterForServer(List clusters, String instanceName) {
        //Return the server only if it is part of a cluster (i.e. only if a cluster
        //has a reference to it).
        for (Object cluster : clusters) {
            final List servers = ((Cluster)cluster).getInstances();
            for (Object server : servers) {
                if (((Server)server).getName().equals(instanceName)) {
                    // check to see if the server exists as a sanity check.
                    // NOTE: we are not checking for duplicate server instances here.
                    return (Cluster) cluster;
                }
            }
        }
        return null;
    }

    private static boolean enableClustering() {
        try {
            /* This flag disables the auto clustering functionality
               * No uMQ clusters will  be created with AS cluster if
               * this flag is set to false. Default is true.
               */
            String enablecluster = System.getProperty(ENABLE_AUTO_CLUSTERING);
            _logger.log(FINE, "Sun MQ Auto cluster system property " + enablecluster);
            if ((enablecluster != null) && (enablecluster.trim().equals("false"))){
                _logger.log(FINE, "Disabling Sun MQ Auto Clustering");
                return false;
            }
        } catch (Exception e) {

        }
        _logger.log(FINE, "Enabling Sun MQ Auto Clustering");
        return true;
    }

    public String getJMSServiceType(){
        return this.js.getType();
    }

    public MQAddressList getUrlList() {
        return list;
    }

    public boolean getReconnectEnabled() {
        return Boolean.parseBoolean(js.getReconnectEnabled());
    }

    public String getReconnectInterval() {
        return js.getReconnectIntervalInSeconds();
    }

    public String getReconnectAttempts() {
        return js.getReconnectAttempts();
    }

    public String getAddressListIterations() {
        return js.getAddresslistIterations();
    }

    public String getAddressListBehaviour() {
        return js.getAddresslistBehavior();
    }

    public void setMdbContainerProperties(){
        MdbContainer mdbc = null;
        try {
            mdbc = Globals.get(MdbContainer.class);
        } catch (Exception e) {
            _logger.log(WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION, new Object[] {e.getMessage()});
            if (_logger.isLoggable(FINE)) {
                _logger.log(FINE, e.getClass().getName(), e);
            }
        }

        if (mdbc != null) {
            List props = mdbc.getProperty();//        getElementProperty();
            if (props != null) {
                for (Object prop : props) {
                    Property p = (Property) prop;
                    if (p == null) {
                        continue;
                    }
                    String name = p.getName();
                    if (name == null) {
                        continue;
                    }
                    try {
                        if (name.equals(propName_reconnect_enabled)) {
                            if (p.getValue() == null) {
                                continue;
                            }
                            reconnectEnabled =
                                Boolean.valueOf(p.getValue()).booleanValue();
                        }
                        else if (name.equals
                                 (propName_reconnect_delay_in_seconds)) {
                            try {
                                reconnectDelayInSeconds =
                                    Integer.parseInt(p.getValue());
                            } catch (Exception e) {
                                _logger.log(WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION,
                                        new Object[]{e.getMessage()});
                            }
                        }
                        else if (name.equals(propName_reconnect_max_retries)) {
                            try {
                                reconnectMaxRetries =
                                    Integer.parseInt(p.getValue());
                            } catch (Exception e) {
                                _logger.log(WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION,
                                        new Object[]{e.getMessage()});
                            }
                        }
                        else if (name.equals
                                 (propName_cmt_max_runtime_exceptions)) {
                            try {
                                cmtMaxRuntimeExceptions =
                                    Integer.parseInt(p.getValue());
                            } catch (Exception e) {
                                _logger.log(WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION,
                                    new Object[]{e.getMessage()});
                            }
                        }
                    } catch (Exception e) {
                        _logger.log(WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION,
                                new Object[]{e.getMessage()});
                        if (_logger.isLoggable(FINE)) {
                            _logger.log(FINE, e.getClass().getName(), e);
                        }
                    }
                }
            }
        }
        if (reconnectDelayInSeconds < 0) {
            reconnectDelayInSeconds = DEFAULT_RECONNECT_DELAY;
        }
        if (reconnectMaxRetries < 0) {
            reconnectMaxRetries = DEFAULT_RECONNECT_RETRIES;
        }
        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE,
                propName_reconnect_delay_in_seconds+"="+
                reconnectDelayInSeconds +", "+
                propName_reconnect_max_retries+ "="+reconnectMaxRetries + ", "+
                propName_reconnect_enabled+"="+reconnectEnabled);
        }

        //Now set all these properties in the active resource adapter
        MdbContainerProps.setReconnectDelay(reconnectDelayInSeconds);
        MdbContainerProps.setReconnectMaxRetries(reconnectMaxRetries);
        MdbContainerProps.setReconnectEnabled(reconnectEnabled);
        MdbContainerProps.setMaxRuntimeExceptions(cmtMaxRuntimeExceptions);

    }

    public void configureDescriptor(ConnectorDescriptor cd) {
        Object[] envProps = cd.getConfigProperties().toArray();

        for (Object envProp2 : envProps) {
            ConnectorConfigProperty envProp = (ConnectorConfigProperty) envProp2;
            String name = envProp.getName();
            if (!name.equals("ConnectionURL")) {
                continue;
            }
            String userValue = getUrl();
            if (userValue != null) {
                cd.removeConfigProperty(envProp);
                cd.addConfigProperty(new ConnectorConfigProperty(name, userValue, userValue, envProp.getType()));
            }

        }

    }

    /**
     * Obtains the Implementation-Version from the MQ Client libraries
     * that are deployed in the application server and in MQ installation
     * directory.
     */
    public void upgradeIfNecessary() {
        String imqLibPath = System.getProperty(IMQ_LIB_PROPERTY);
        if (imqLibPath == null) {
            _logger.log(Level.SEVERE, "IMQ lib root system property not set: " + IMQ_LIB_PROPERTY);
            return;
        }

        String installRoot = System.getProperty(INSTALL_ROOT_PROPERTY);
        if (installRoot == null) {
            _logger.log(Level.SEVERE, "Install root system property not set: " + INSTALL_ROOT_PROPERTY);
            return;
        }

        final Path imqLib = new File(imqLibPath).toPath();
        final Path deployedDir = new File(installRoot).toPath().resolve(SYSTEM_APP_DIR).resolve(DEFAULT_JMS_ADAPTER);
        final File imqLibRar = imqLib.resolve(MQ_RAR).toFile();
        final String installedMqVersion;
        final String deployedMqVersion;
        try {
            installedMqVersion = getInstalledMqVersion(imqLibRar);
            _logger.log(FINE, "installedMQVersion: {0}", installedMqVersion);
            deployedMqVersion = getDeployedMqVersion(deployedDir);
            _logger.log(FINE, "deployedMQVersion: {0}", deployedMqVersion);
        } catch (Exception e) {
            throw new IllegalStateException("Upgrade failed - could not resolve deployed and installed version.", e);
        }

        if (installedMqVersion == null) {
            _logger.log(INFO, "No JMS RA installation RAR found on path {0}. Nothing for upgrade.", imqLibRar);
            return;
        }

        // If the Manifest entry has different versions, then attempt to
        // explode the MQ resource adapter.
        if (!Objects.equals(installedMqVersion, deployedMqVersion)) {
            try {
                _logger.log(INFO, JMSLoggerInfo.JMSRA_UPGRADE_STARTED,
                    new Object[] {deployedMqVersion, installedMqVersion});
                FileUtils.whack(deployedDir.toFile());
                ZipFile rarFile = new ZipFile(imqLibRar, deployedDir.toFile());
                rarFile.explode();
                _logger.log(INFO, JMSLoggerInfo.JMSRA_UPGRADE_COMPLETED);
            } catch (ZipFileException e) {
                _logger.log(Level.SEVERE, "Upgrading a MQ resource adapter failed", e);
            }
        }
    }


    /** Full path of installed Mq Client library */
    private String getInstalledMqVersion(final File imqLibRar) throws Exception {
        // This is for a file based install RAR has to be present in this location
        // AS_INSTALL/../imq/lib, but ca be set also to another place.
        if (!imqLibRar.exists()) {
            return null;
        }
        _logger.log(FINEST, "Found installation JMS RAR: {0}", imqLibRar);
        try (JarFile jFile = new JarFile(imqLibRar)) {
            Manifest mf = jFile.getManifest();
            return mf.getMainAttributes().getValue(MANIFEST_TAG);
        }
    }

    /** Full path of Mq client library that is deployed in appserver. */
    private String getDeployedMqVersion(Path deployedDir) throws Exception {
        File manifestFile = deployedDir.resolve(MQ_RAR_MANIFEST).toFile();
        if (!manifestFile.exists()) {
            return null;
        }
        _logger.log(FINEST, "Found deployed JMS RA Manifest file {0}", manifestFile);
        try (FileInputStream fis = new FileInputStream(manifestFile)) {
            Manifest mf = new Manifest(fis);
            return mf.getMainAttributes().getValue(MANIFEST_TAG);
        }
    }


    private static ConnectorRuntimeException handleException(Exception e) {
        ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
        cre.initCause(e);
        return cre;
    }


    public static String getJMSPropertyValue(Server as) {
        SystemProperty sp = as.getSystemProperty("JMS_PROVIDER_PORT");
        if (sp != null) {
            return sp.getValue();
        }

        Cluster cluster = as.getCluster();
        if (cluster != null) {
            sp = cluster.getSystemProperty("JMS_PROVIDER_PORT");
            if (sp != null) {
                return sp.getValue();
            }
        }

        Config config = as.getConfig();
        if (config != null) {
            sp = config.getSystemProperty("JMS_PROVIDER_PORT");
        }

        if (sp != null) {
            return sp.getValue();
        }

        return null;
    }


    public static String getUnAliasedPwd(String alias) {
        try {
            String unalisedPwd = RelativePathResolver.getRealPasswordFromAlias(alias);
            if (unalisedPwd != null && "".equals(unalisedPwd)) {
                return unalisedPwd;
            }

        } catch (Exception e) {

        }
        return alias;
    }
}
