/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.mbeanapi.common;

import com.sun.appserv.management.config.*;
import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.J2EECluster;
import com.sun.appserv.management.util.stringifier.SmartStringifier;

import java.io.IOException;
import java.util.Map;
import java.util.Iterator;

/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Sep 12, 2004
 * @version $Revision: 1.2 $
 */
public class ClusterManager {
    AMXConnector mAsapiConnector;
    ClusterConfig mClusterCfg;
    DomainConfig mDomainConfig;

    public ClusterManager(
                final String host,
                final int port,
                final String adminUser,
                final String adminPassword,
                final boolean useTLS )
                    throws IOException
    {
        mAsapiConnector = new AMXConnector(host, port, adminUser, adminPassword, useTLS);
        mDomainConfig = mAsapiConnector.getDomainRoot().getDomainConfig();
    }

    public ClusterManager( final AMXConnector conn){
        mAsapiConnector = conn;
        mDomainConfig = mAsapiConnector.getDomainRoot().getDomainConfig();
    }

    /**
     * creates a cluster
     * @param clusterName
     * @param configName
     * @param optional
     *
     * @return ClusterConfig
     */
    public ClusterConfig createCluster(final String clusterName,
                                               final String configName,
                                               final java.util.Map optional ) throws Exception
    {
        mClusterCfg = mDomainConfig.createClusterConfig(clusterName, configName, optional);
        return mClusterCfg;
    }

    /**
     * removes a cluster
     * @param name
     */
    public void deleteCluster(final String name) throws Exception{
        mDomainConfig.removeClusterConfig(name);
    }

    /**
     * Starts a cluster
     * @param name
     * @throws ObjectNotFoundException
     */
    public void startCluster(final String name) throws ObjectNotFoundException {
        final J2EECluster cluster = getCluster(name);
        if(cluster != null){
            cluster.start();
            return;
        }
        throw new ObjectNotFoundException("startInstance: instance "+
                                        name+" was not found by the DAS");
    }


    /**
     * stops a named standalone instance
     * @param name
     */
    public void stopCluster(final String name) throws ObjectNotFoundException {
        final J2EECluster cluster = getCluster(name);
        if(cluster != null){
            cluster.stop();
            return;
        }
        throw new ObjectNotFoundException("stopInstance: instance "+
                                        name + " was not found by the DAS");
    }

    private J2EECluster getCluster(final String name) {
        final Map clusters = mAsapiConnector.getDomainRoot().getJ2EEDomain().getClusterMap();
        final Iterator iter;
        J2EECluster cluster=null;
        String listName;
        for(iter=clusters.keySet().iterator();iter.hasNext();){
            if((listName = (String)iter.next()).equals(name)){
                cluster = (J2EECluster)clusters.get(listName);
                break;
            }
        }
        return cluster;
    }

    public void listClusters() {
        final Map clusters = mAsapiConnector.getDomainRoot().getJ2EEDomain().getClusterMap();
        final Iterator iter;
        for(iter=clusters.keySet().iterator();iter.hasNext();){
            println(((J2EECluster)iter.next()).getName());
        }
    }

    public boolean isCreated(final String name){
        final J2EECluster cluster = getCluster(name);
        if( cluster != null )
            return true;
        return false;
    }
    public static void   println( final Object o )
    {
        System.out.println( toString( o ) );
    }

    private static String  toString(final Object o )
    {
        return( SmartStringifier.toString( o ) );
    }
}
