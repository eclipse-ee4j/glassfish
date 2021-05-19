/*
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

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.connectors.jms.JMSLoggerInfo;
import com.sun.enterprise.connectors.jms.config.JmsService;
import com.sun.enterprise.connectors.jms.inflow.MdbContainerProps;
import com.sun.enterprise.connectors.jms.system.MQAddressList;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.zip.ZipFile;
import com.sun.enterprise.util.zip.ZipFileException;
import org.glassfish.ejb.config.MdbContainer;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.RelativePathResolver;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author
 */
public class JmsRaUtil {

    private final static String MQ_RAR = "imqjmsra.rar";

    private final String SYSTEM_APP_DIR =
        "lib" + File.separator + "install" + File.separator + "applications";

    private final String MQ_RAR_MANIFEST =
        ConnectorConstants.DEFAULT_JMS_ADAPTER + File.separator + "META-INF"
        + File.separator + "MANIFEST.MF";

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
    private boolean reconnectEnabled = false;

    JmsService js = null;
    MQAddressList list = null;

    private static final Logger _logger = JMSLoggerInfo.getLogger();

    public JmsRaUtil() throws ConnectorRuntimeException {
        this(null);
    }

    public JmsRaUtil(JmsService js) throws ConnectorRuntimeException {
        try {
            if (js != null) {
            this.js = js;
            } else {
                  this.js = (JmsService) Globals.get(JmsService.class);
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

    public void setupAddressList() throws ConnectorRuntimeException{
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
              return (enableClustering() && isServerClustered(clusters,
                instanceName));
     }
      /**
     * Return true if the given server instance is part of a cluster.
     */
    public static boolean isServerClustered(List clusters, String instanceName)
    {
        return (getClusterForServer(clusters, instanceName) != null);
    }
    public static Cluster getClusterForServer(List clusters, String instanceName){
        //Return the server only if it is part of a cluster (i.e. only if a cluster
        //has a reference to it).
        for (int i = 0; i < clusters.size(); i++) {
            final List servers = ((Cluster)clusters.get(i)).getInstances();
            for (int j = 0; j < servers.size(); j++) {
                if (((Server)servers.get(j)).getName().equals(instanceName)) {
                    // check to see if the server exists as a sanity check.
                    // NOTE: we are not checking for duplicate server instances here.
                    return (Cluster) clusters.get(i);
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
            _logger.log(Level.FINE, "Sun MQ Auto cluster system property " + enablecluster);
            if ((enablecluster != null) && (enablecluster.trim().equals("false"))){
                _logger.log(Level.FINE, "Disabling Sun MQ Auto Clustering");
                return false;
            }
        } catch (Exception e) {
            ;
        }
        _logger.log(Level.FINE, "Enabling Sun MQ Auto Clustering");
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

        }
        catch (Exception e) {
            _logger.log(Level.WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION, new Object[]{e.getMessage()});
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, e.getClass().getName(), e);
            }
        }

        if (mdbc != null) {
            List props = mdbc.getProperty();//        getElementProperty();
            if (props != null) {
                for (int i = 0; i < props.size(); i++) {
                    Property p = (Property) props.get(i);
                    if (p == null) continue;
                    String name = p.getName();
                    if (name == null) continue;
                    try {
                        if (name.equals(propName_reconnect_enabled)) {
                            if (p.getValue() == null) continue;
                            reconnectEnabled =
                                Boolean.valueOf(p.getValue()).booleanValue();
                        }
                        else if (name.equals
                                 (propName_reconnect_delay_in_seconds)) {
                            try {
                                reconnectDelayInSeconds =
                                    Integer.parseInt(p.getValue());
                            } catch (Exception e) {
                                _logger.log(Level.WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION,
                                        new Object[]{e.getMessage()});
                            }
                        }
                        else if (name.equals(propName_reconnect_max_retries)) {
                            try {
                                reconnectMaxRetries =
                                    Integer.parseInt(p.getValue());
                            } catch (Exception e) {
                                _logger.log(Level.WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION,
                                        new Object[]{e.getMessage()});
                            }
                        }
                        else if (name.equals
                                 (propName_cmt_max_runtime_exceptions)) {
                            try {
                                cmtMaxRuntimeExceptions =
                                    Integer.parseInt(p.getValue());
                            } catch (Exception e) {
                                _logger.log(Level.WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION,
                                    new Object[]{e.getMessage()});
                            }
                        }
                    } catch (Exception e) {
                        _logger.log(Level.WARNING, JMSLoggerInfo.MDB_CONFIG_EXCEPTION,
                                new Object[]{e.getMessage()});
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.log(Level.FINE, e.getClass().getName(), e);
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
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,
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

        for (int i = 0; i < envProps.length; i++) {
            ConnectorConfigProperty  envProp = (ConnectorConfigProperty ) envProps[i];
            String name = envProp.getName();
        if (!name.equals("ConnectionURL")) {
            continue;
        }
            String userValue = getUrl();
            if (userValue != null) {
                cd.removeConfigProperty(envProp);
                cd.addConfigProperty(new ConnectorConfigProperty (
                              name, userValue, userValue, envProp.getType()));
            }

        }

    }

    /**
     * Obtains the Implementation-Version from the MQ Client libraries
     * that are deployed in the application server and in MQ installation
     * directory.
     */
    public void upgradeIfNecessary() {

        String installedMqVersion = null;
        String deployedMqVersion = null;

        try {
           installedMqVersion = getInstalledMqVersion();
           _logger.log(Level.FINE,"installedMQVersion :: " + installedMqVersion);
           deployedMqVersion =  getDeployedMqVersion();
           _logger.log(Level.FINE,"deployedMQVersion :: " + deployedMqVersion);
        }catch (Exception e) {
        return;
        }

        String deployed_dir =
           java.lang. System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY)
           + File.separator + SYSTEM_APP_DIR + File.separator
           + ConnectorConstants.DEFAULT_JMS_ADAPTER;

        // If the Manifest entry has different versions, then attempt to
        // explode the MQ resource adapter.
        if (!installedMqVersion.equals(deployedMqVersion)) {
           try {
               _logger.log(Level.INFO, JMSLoggerInfo.JMSRA_UPGRADE_STARTED);
           ZipFile rarFile = new ZipFile(System.getProperty
                                 (SystemPropertyConstants.IMQ_LIB_PROPERTY) +
                                 File.separator + MQ_RAR, deployed_dir);
               rarFile.explode();
               _logger.log(Level.INFO, JMSLoggerInfo.JMSRA_UPGRADE_COMPLETED);
       } catch(ZipFileException ze) {
               _logger.log(Level.SEVERE, JMSLoggerInfo.JMSRA_UPGRADE_FAILED,
                       new Object[]{ze.getMessage()});
           }
        }

    }

    private String getInstalledMqVersion() throws Exception {
       String ver = null;
       // Full path of installed Mq Client library
       String installed_dir =
           System.getProperty(SystemPropertyConstants.IMQ_LIB_PROPERTY);
       String jarFile = installed_dir + File.separator + MQ_RAR;
       _logger.log(Level.FINE,"Installed MQ JAR " + jarFile);
    JarFile jFile = null;
       try {
       if ((new File(jarFile)).exists()) {
        /* This is for a file based install
           * RAR has to be present in this location
           * ASHOME/imq/lib
           */
        jFile = new JarFile(jarFile);
       } else {
        /* This is for a package based install
           * RAR has to be present in this location
           * /usr/lib
           */
        jFile = new JarFile(installed_dir + File.separator + ".." + File.separator + MQ_RAR);
       }
           Manifest mf = jFile.getManifest();
           ver = mf.getMainAttributes().getValue(MANIFEST_TAG);
           return ver;
       } catch (Exception e) {
           _logger.log(Level.WARNING, JMSLoggerInfo.JMSRA_UPGRADE_CHECK_FAILED,
                   new Object[]{e.getMessage() + ":" + jarFile});
           throw e;
       } finally {
           if (jFile != null) {
               jFile.close();
           }
       }
    }

    private String getDeployedMqVersion() throws Exception {
       String ver = null;
        // Full path of Mq client library that is deployed in appserver.
       String deployed_dir =
           System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY)
           + File.separator + SYSTEM_APP_DIR;
       String manifestFile = deployed_dir + File.separator +
                             MQ_RAR_MANIFEST;
       _logger.log(Level.FINE,"Deployed MQ version Manifest file" + manifestFile);
       try (FileInputStream fis = new FileInputStream(manifestFile)) {
           Manifest mf = new Manifest(fis);
           ver = mf.getMainAttributes().getValue(MANIFEST_TAG);
           return ver;
       } catch (Exception e) {
           _logger.log(Level.WARNING, JMSLoggerInfo.JMSRA_UPGRADE_CHECK_FAILED,
                   new Object[]{e.getMessage() + ":" + manifestFile});
           throw e;
       }
   }

   private static ConnectorRuntimeException handleException(Exception e) {
        ConnectorRuntimeException cre =
             new ConnectorRuntimeException(e.getMessage());
        cre.initCause(e);
        return cre;
    }

    public static String getJMSPropertyValue(Server as){

        SystemProperty sp = as.getSystemProperty("JMS_PROVIDER_PORT");
        if (sp != null) return sp.getValue();

        Cluster cluster = as.getCluster();
        if (cluster != null) {
            sp = cluster.getSystemProperty("JMS_PROVIDER_PORT");
            if (sp != null) return sp.getValue();
        }

        Config config = as.getConfig();
        if (config != null)
            sp = config.getSystemProperty("JMS_PROVIDER_PORT");

        if (sp != null) return sp.getValue();

        return null;
    }

    public static String getUnAliasedPwd(String alias){
        try{
            String unalisedPwd = RelativePathResolver.getRealPasswordFromAlias(alias);
            if (unalisedPwd != null && "".equals(unalisedPwd))
               return unalisedPwd;

        }catch(Exception e){

        }
         return alias;
    }
}
