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

package com.sun.enterprise.web.connector.extension;

import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import com.sun.enterprise.web.WebContainer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.j2ee.statistics.Stats;
import org.glassfish.web.LogFacade;

/**
 * This class track monitoring or Grizzly, using JMX to invoke Grizzly main
 * classes.
 *
 * @author Jeanfrancois Arcand
 */
public class GrizzlyConfig implements MonitoringLevelListener{

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    /**
     * Is monitoring already started.
     */
    private boolean isMonitoringEnabled = false;

    /**
     * The JMX domain
     */
    private String domain;


    /**
     * The port used to lookup Grizzly's Selector
     */
    private int port;


    /**
     * The list of instance created. This list is not thread-safe.
     */
    private static ArrayList<GrizzlyConfig>
            grizzlyConfigList = new ArrayList<GrizzlyConfig>();


    /**
     * This server context's default services.
     */
    private ServiceLocator services = null;


    // --------------------------------------------------------------- //


    /**
     * Creates the monitoring helper.
     */
    public GrizzlyConfig(WebContainer webContainer, String domain, int port) {

        this.domain = domain;
        this.port = port;

        this.services = webContainer.getServerContext().getDefaultServices();

        grizzlyConfigList.add(this);
    }

    public void destroy() {
        unregisterMonitoringLevelEvents();
        grizzlyConfigList.remove(this);
    }

    public void initConfig(){
        initMonitoringLevel();
    }


    private void initMonitoringLevel() {
        try{
            Config cfg = services.getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);

            MonitoringLevel monitoringLevel = MonitoringLevel.OFF; // default per DTD

            if (cfg.getMonitoringService() != null) {
                ModuleMonitoringLevels levels =
                    cfg.getMonitoringService().getModuleMonitoringLevels();
                if (levels != null) {
                    monitoringLevel = MonitoringLevel.instance(
                                                    levels.getHttpService());
                }
            }

            if(MonitoringLevel.OFF.equals(monitoringLevel)) {
                isMonitoringEnabled = false;
            } else {
                isMonitoringEnabled = true;
            }

            String methodToInvoke = isMonitoringEnabled ? "enableMonitoring" :
                "disableMonitoring";
            invokeGrizzly(methodToInvoke);
        } catch (Exception ex) {
            String msg = rb.getString(LogFacade.INIT_MONITORING_EXCEPTION);
            msg = MessageFormat.format(msg, Integer.valueOf(port));
            logger.log(Level.WARNING, msg, ex);
        }
    }


    public void registerMonitoringLevelEvents() {
        MonitoringRegistry monitoringRegistry = services.getService(MonitoringRegistry.class);
        if (monitoringRegistry!=null) {
            monitoringRegistry.registerMonitoringLevelListener(
                this, MonitoredObjectType.HTTP_LISTENER);
        }
    }


    private void unregisterMonitoringLevelEvents() {
        MonitoringRegistry monitoringRegistry = services.getService(MonitoringRegistry.class);
        if (monitoringRegistry!=null) {
            monitoringRegistry.unregisterMonitoringLevelListener(this);
        }
    }


    public void setLevel(MonitoringLevel level) {
        // deprecated, ignore
    }


    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
                            MonitoredObjectType type) {
        if (MonitoredObjectType.HTTP_LISTENER.equals(type)) {
            if(MonitoringLevel.OFF.equals(to)) {
                isMonitoringEnabled = false;
            } else {
                isMonitoringEnabled = true;
            }
        }
        String methodToInvoke = isMonitoringEnabled ? "enableMonitoring" :
            "disabledMonitoring";
        invokeGrizzly(methodToInvoke);
    }


    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
                Stats handback) {
        // deprecated, ignore
    }


    protected final void invokeGrizzly(String methodToInvoke) {
        invokeGrizzly(methodToInvoke,null,null);
    }


    protected final void invokeGrizzly(String methodToInvoke,
                                       Object[] objects, String[] signature) {
        try{
            String onStr = domain + ":type=Selector,name=http" + port;
            ObjectName objectName = new ObjectName(onStr);
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, LogFacade.INVOKE_GRIZZLY,
                        new Object[] {methodToInvoke, objectName});
            }

        } catch ( Exception ex ){
            String msg = rb.getString(LogFacade.INVOKE_MBEAN_EXCEPTION);
            msg = MessageFormat.format(msg, methodToInvoke);
            logger.log(Level.SEVERE, msg, ex);
            //throw new RuntimeException(ex);
        }
    }


    /**
     * Enable CallFlow gathering mechanism.
     */
    public final void setEnableCallFlow(boolean enableCallFlow){
        String methodToInvoke = enableCallFlow ? "enableMonitoring" :
            "disabledMonitoring";
        invokeGrizzly(methodToInvoke);
    }


    /**
     * Return the list of all instance of this class.
     */
    public static ArrayList<GrizzlyConfig> getGrizzlyConfigInstances(){
        return grizzlyConfigList;
    }


    /**
     * Return the port this configuration belongs.
     */
    public int getPort(){
        return port;
    }
}
