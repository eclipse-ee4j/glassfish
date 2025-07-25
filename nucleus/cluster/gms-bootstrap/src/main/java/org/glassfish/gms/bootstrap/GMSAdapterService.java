/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.gms.bootstrap;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.util.i18n.StringManager;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.beans.PropertyChangeEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * This service is responsible for loading the group management
 * service. In the DAS, GMS will only be enabled if there is at least
 * one cluster present with the gms-enabled attribute set to true.
 * In an instance, GMS will be enabled if the cluster containing this
 * instance has gms-enabled set to true.
 *
 * Components can inject this service in order to obtain a reference
 * to a GMSAdapter object. From this, the appropriate GroupManagementService
 * object can be retrieved.
 */
@Service
@RunLevel(StartupRunLevel.VAL)
public class GMSAdapterService implements PostConstruct, ConfigListener {

    //final static Logger logger = LogDomains.getLogger(
    //    GMSAdapterService.class, LogDomains.CORE_LOGGER);

    @LoggerInfo(subsystem = "CLSTR", description="Group Management Service Logger")
    private static final String GMSBS_LOGGER_NAME = "jakarta.enterprise.cluster.gms.bootstrap";


    @LogMessagesResourceBundle
    private static final String LOG_MESSAGES_RB = "org.glassfish.cluster.gms.bootstrap.LogMessages";

    static final Logger GMSBS_LOGGER = Logger.getLogger(GMSBS_LOGGER_NAME, LOG_MESSAGES_RB);

    @LogMessageInfo(message = "Unable to load GMS classes. Group management service is not available.",
                    level="WARNING",
                    cause="GMS implementation classes are not present. See https://glassfish.dev.java.net/issues/show_bug.cgi?id=12850.",
                    action="Check that shoal-gms-impl.jar file is present.")
    private static final String GMSBS_GMSADAPTER_NOT_AVAILABLE="NCLS-CLSTR-10001";

    private static final StringManager strings = StringManager.getManager(GMSAdapterService.class);
    private static final Object LOCK = new Object();

    @Inject
    private Clusters clusters;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Server server;

    @Inject
    private ServerEnvironment env;

    @Inject
    private ServiceLocator habitat;

    @Inject
    private StartupContext startupContext;

    @Inject
    private Provider<GMSAdapter> gmsAdapterProvider;


    List<GMSAdapter> gmsAdapters = new LinkedList<>();

    final static private Level TRACE_LEVEL = Level.FINE;

    /**
     * Starts the application loader service.
     */
    @Override
    public void postConstruct() {
        if (startupContext != null) {
            Properties args = startupContext.getArguments();
            if (args != null && Boolean.valueOf(args.getProperty("-upgrade"))) {
                return;
            }
        }
        if (clusters != null) {
            if (env.isDas()) {
                checkAllClusters(clusters);
            } else {
                Cluster cluster = server.getCluster();
                if (cluster != null) {
                    checkCluster(cluster);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "GMS Loader";
    }

    /*
     */
    public GMSAdapter getGMSAdapter() {
        synchronized(LOCK) {
            if (gmsAdapters.size() > 1) {
                throw new IllegalStateException(
                    strings.getString("use.getByName"));
            } else if (gmsAdapters.size() == 1) {
                return gmsAdapters.get(0);
            } else {
                return null;
            }
        }
    }

    public boolean isGmsEnabled() {
        return gmsAdapters.size() > 0;
    }

    public GMSAdapter getGMSAdapterByName(String clusterName) {
        synchronized(LOCK) {
            return habitat.getService(GMSAdapter.class, clusterName);
        }
    }

    /**
     * Create a GMSAdapter for each cluster that has gms enabled.
     */
    private void checkAllClusters(Clusters clusters) {
        if (GMSBS_LOGGER.isLoggable(TRACE_LEVEL)) {
            GMSBS_LOGGER.log(TRACE_LEVEL, "In DAS. Checking all clusters.");
        }
        for (Cluster cluster : clusters.getCluster()) {
            checkCluster(cluster);
        }
    }

    private GMSAdapter checkCluster(Cluster cluster) {
        GMSAdapter result = null;
        String gmsEnString = cluster.getGmsEnabled();
        if (GMSBS_LOGGER.isLoggable(TRACE_LEVEL)) {
            GMSBS_LOGGER.log(TRACE_LEVEL, String.format("cluster %s found with gms-enabled='%s'",
                        cluster.getName(), gmsEnString));
        }
        if (gmsEnString != null && Boolean.parseBoolean(gmsEnString)) {
            result = loadModule(cluster);
        }
        return result;
    }

    /*
     * initial support for multiple clusters in DAS. a clustered instance can only belong to one cluster.
     */
    private GMSAdapter loadModule(Cluster cluster) {
        GMSAdapter result = null;
        synchronized(LOCK) {
            result = getGMSAdapterByName(cluster.getName());
            if (GMSBS_LOGGER.isLoggable(TRACE_LEVEL)) {
                GMSBS_LOGGER.log(TRACE_LEVEL, "lookup GMSAdapter by clusterName=" + cluster.getName() + " returned " + result);
            }
            if (result == null) {
                if (GMSBS_LOGGER.isLoggable(TRACE_LEVEL)) {
                    GMSBS_LOGGER.log(TRACE_LEVEL, "creating gms-adapter for clustername " + cluster.getName() + " since no gms adapter found for clustername " + cluster.getName());
                }
                result = gmsAdapterProvider.get();

                // see https://glassfish.dev.java.net/issues/show_bug.cgi?id=12850
                if (result == null) {
                    GMSBS_LOGGER.log(Level.WARNING, GMSBS_GMSADAPTER_NOT_AVAILABLE);
                    return null;
                }
                boolean initResult = result.initialize(cluster.getName());
                if (initResult == false) {
                    return null;
                }
                ServiceLocatorUtilities.addOneConstant(habitat, result, cluster.getName(), GMSAdapter.class);

                if (GMSBS_LOGGER.isLoggable(TRACE_LEVEL)) {
                    GMSBS_LOGGER.log(TRACE_LEVEL, "loadModule: registered created gmsadapter for cluster " + cluster.getName() + " initialized result=" + initResult);
                }
                gmsAdapters.add(result);
            }
        }
        return result;
    }

    /*
     * On create-cluster event, DAS joins a gms-enabled cluster.
     * On delete-cluster event, DAS leaves a gms-enabled cluster.
     */
    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        if (env.isDas()) {
            return ConfigSupport.sortAndDispatch(events, new Changed() {
                @Override
                public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
                    if (changedType == Cluster.class && type == TYPE.ADD) {  //create-cluster
                        Cluster cluster = (Cluster) changedInstance;
                        if (GMSBS_LOGGER.isLoggable(TRACE_LEVEL)) {
                            GMSBS_LOGGER.log(TRACE_LEVEL, "ClusterChangeEvent add cluster " + cluster.getName());
                        }
                        GMSAdapter localGmsAdapter = checkCluster(cluster);
                        if (localGmsAdapter != null) {
                            GroupManagementService gms = localGmsAdapter.getModule();
                            if (gms != null) {
                                gms.reportJoinedAndReadyState();
                            }
                        }

                        // todo:  when supporting multiple clusters, ensure that newly added cluster has a different gms-multicast-address than all existing clusters.
                        //        currently, generating a unique multicast address depending on random so this check is necessary.
                    }
                    if (changedType == Cluster.class && type == TYPE.REMOVE) {  //remove-cluster
                        Cluster cluster = (Cluster) changedInstance;
                        if (GMSBS_LOGGER.isLoggable(TRACE_LEVEL)) {
                            GMSBS_LOGGER.log(TRACE_LEVEL, "ClusterChangeEvent remove cluster " + cluster.getName());
                        }
                        synchronized(LOCK) {
                            GMSAdapter localGmsAdapter = getGMSAdapterByName(cluster.getName());
                            if (localGmsAdapter != null) {
                                gmsAdapters.remove(localGmsAdapter);
                                localGmsAdapter.getModule().shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                                ServiceLocatorUtilities.removeFilter(habitat, BuilderHelper.createNameAndContractFilter(
                                        GMSAdapter.class.getName(), cluster.getName()));

                                // remove GMS module for deleted cluster.  Must do this or will fail if the cluster is recreated before DAS is stopped.
                                localGmsAdapter.complete();
                            }
                        }
                    }
                    return null;
                }
            }, GMSBS_LOGGER);
        }
        return null;
    }
}
