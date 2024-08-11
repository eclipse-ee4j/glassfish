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

package org.glassfish.loadbalancer.admin.cli.helper;

import com.sun.enterprise.config.serverbeans.Domain;

import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.loadbalancer.admin.cli.LbLogUtil;
import org.glassfish.loadbalancer.admin.cli.beans.Loadbalancer;
import org.glassfish.loadbalancer.admin.cli.reader.api.ClusterReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.InstanceReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.LoadbalancerReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.WebModuleReader;
import org.glassfish.loadbalancer.admin.cli.reader.impl.LoadbalancerReaderImpl;
import org.glassfish.loadbalancer.admin.cli.transform.LoadbalancerVisitor;
import org.glassfish.loadbalancer.config.LbConfig;
import org.glassfish.loadbalancer.config.LbConfigs;
import org.glassfish.loadbalancer.config.LoadBalancer;
import org.glassfish.loadbalancer.config.LoadBalancers;


/**
 * Export support class
 *
 * @author Kshitiz Saxena
 */
public class LbConfigHelper {

    /**
     * exports the loadbalancer.xml from the config to the outputstream provided
     * @param ctx ConfigContext
     * @param lbConfigName name of lb-config
     * @param out OutputStream into which the loadbalancer.xml is written
     */
    public static LoadbalancerReader getLbReader(Domain domain, ApplicationRegistry appRegistry,
            String lbConfigName) throws Exception {
        // reads the load balancer related data
        LbConfigs lbConfigs = domain.getExtensionByType(LbConfigs.class);
        if (lbConfigs == null) {
            throw new Exception(LbLogUtil.getStringManager().getString("UnableToGetLbConfig", lbConfigName));
        }
        LbConfig lbConfig = lbConfigs.getLbConfig(lbConfigName);
        if (lbConfig == null) {
            throw new Exception(LbLogUtil.getStringManager().getString("UnableToGetLbConfig", lbConfigName));
        }
        return new LoadbalancerReaderImpl(domain, appRegistry, lbConfig);
    }

    public static LoadBalancer getLoadBalancer(Domain domain, String lbName) throws Exception {
        LoadBalancers loadBalancers = domain.getExtensionByType(LoadBalancers.class);
        if (loadBalancers == null) {
            throw new Exception(LbLogUtil.getStringManager().getString("UnableToGetLoadbalancer", lbName));
        }
        LoadBalancer loadBalancer = loadBalancers.getLoadBalancer(lbName);
        if (loadBalancer == null) {
            throw new Exception(LbLogUtil.getStringManager().getString("UnableToGetLoadbalancer", lbName));
        }
        return loadBalancer;
    }

    /**
     * exports the loadbalancer.xml from the config to the outputstream provided
     * @param ctx ConfigContext
     * @param lbConfigName name of lb-config
     * @param out OutputStream into which the loadbalancer.xml is written
     */
    public static void exportXml(LoadbalancerReader lbRdr, OutputStream out)
            throws Exception {

        // tranform the data using visitor pattern
        Loadbalancer _lb = new Loadbalancer();

        LoadbalancerVisitor lbVstr = new LoadbalancerVisitor(_lb);
        lbRdr.accept(lbVstr);

        try {
            String footer = LbLogUtil.getStringManager().getString("GeneratedFileFooter",
                    new Date().toString());
            // write the header
            _lb.graphManager().setDoctype(PUBLICID, SYSTEMID);
            _lb.write(out);
            out.write(footer.getBytes());
        } finally {
            if (out != null) {
                out.close();
                out = null;
            }
        }
    }

    /**
     * exports the workser.properties from the config to the outputstream provided
     * @param ctx ConfigContext
     * @param lbConfigName name of lb-config
     * @param out OutputStream into which the loadbalancer.xml is written
     */
    public static void exportWorkerProperties(LoadbalancerReader lbRdr, OutputStream out)
            throws Exception {

        // tranform the data using visitor pattern
        Loadbalancer _lb = new Loadbalancer();

        Properties props = new Properties();

        String WORKER = "worker";
        String SEPARATOR = ".";
        String HOST = "host";
        String PORT = "port";
        String LIST = "list";
        String TYPE = "type";
        String TYPE_VALUE = "ajp13";
        String LBFACTOR = "lbfactor";
        String LBFACTOR_VALUE = "1";
        String SOCKET_KEEPALIVE = "socket_keepalive";
        String SOCKET_TIMEOUT = "socket_timeout";
        String SOCKET_KEEPALIVE_VALUE = "1";
        String SOCKET_TIMEOUT_VALUE = "300";
        String LOADBALANCER = "-lb";
        String BALANCER_WORKERS = "balance_workers";
        String LB = "lb";
        String CONTEXT_ROOT_MAPPING="CONTEXT_ROOT_MAPPING";
        String APP="APP";
        StringBuffer buffer = new StringBuffer();

        String workerList = "";

        LoadbalancerVisitor lbVstr = new LoadbalancerVisitor(_lb);
        lbRdr.accept(lbVstr);

        ClusterReader clusterReaders[] = lbRdr.getClusters();

        int c;
        buffer.append("worker.properties");

        for(int i=0;i<clusterReaders.length;i++) {
            String clusterWorkerList = "";
            ClusterReader clusterReader = clusterReaders[i];
            String clusterName = clusterReader.getName();
            WebModuleReader webmoduleReaders[] = clusterReader.getWebModules();
            InstanceReader instanceReaders[] = clusterReader.getInstances();

            for(int j =0; j<instanceReaders.length;j++) {
                InstanceReader instanceReader = instanceReaders[j];
                String listenerHost = "";
                String listenerPort = "";
                StringTokenizer st = new StringTokenizer(instanceReader.getListeners(), " ");
                while (st.hasMoreElements()) {
                    String listener = st.nextToken();
                    if (listener.contains("ajp://")) {
                        listenerHost = listener.substring(listener.lastIndexOf("/") + 1, listener.lastIndexOf(":"));
                        listenerPort = listener.substring(listener.lastIndexOf(":") + 1, listener.length());
                        break;
                    }
                }
                String listenterName = instanceReader.getName();

                props.setProperty(WORKER + SEPARATOR + listenterName + SEPARATOR + HOST, listenerHost);
                props.setProperty(WORKER + SEPARATOR + listenterName + SEPARATOR + PORT, listenerPort);
                props.setProperty(WORKER + SEPARATOR + listenterName + SEPARATOR + TYPE, TYPE_VALUE);
                props.setProperty(WORKER + SEPARATOR + listenterName + SEPARATOR + LBFACTOR, LBFACTOR_VALUE);
                props.setProperty(WORKER + SEPARATOR + listenterName + SEPARATOR + SOCKET_KEEPALIVE, SOCKET_KEEPALIVE_VALUE);
                props.setProperty(WORKER + SEPARATOR + listenterName + SEPARATOR + SOCKET_TIMEOUT, SOCKET_TIMEOUT_VALUE);
                workerList = workerList + listenterName + ",";
                clusterWorkerList = clusterWorkerList + listenterName + ",";
            }

            workerList = workerList + clusterName + LOADBALANCER + "," ;
            props.setProperty(WORKER+SEPARATOR+LIST,workerList.substring(0,workerList.length()-1));
            props.setProperty(WORKER+SEPARATOR+clusterName+LOADBALANCER + SEPARATOR+TYPE,LB);
            props.setProperty(WORKER+SEPARATOR+clusterName+LOADBALANCER + SEPARATOR+BALANCER_WORKERS,clusterWorkerList.substring(0,clusterWorkerList.length()-1));

            for (int m=0; m<webmoduleReaders.length;m++) {
               buffer.append("\n" + CONTEXT_ROOT_MAPPING+SEPARATOR+webmoduleReaders[m].getContextRoot()
                       +"="+clusterName+LOADBALANCER);
            }

        }

        try {

        props.store(out,buffer.toString());

        } finally {
            if (out != null) {
                out.close();
                out = null;
            }
        }
    }


   /**
     * exports the otd.properties from the config to the outputstream provided
     * @param ctx ConfigContext
     * @param lbConfigName name of lb-config
     */
    public static void exportOtdProperties(LoadbalancerReader lbRdr, OutputStream out)
            throws Exception {

        // tranform the data using visitor pattern
        Loadbalancer _lb = new Loadbalancer();

        Properties props = new Properties();

        String CLUSTER = "cluster";
        String LISTENER = "listeners";
        String WEB = "web-modules";
        String SEPARATOR = ".";
        StringBuffer buffer = new StringBuffer();


        LoadbalancerVisitor lbVstr = new LoadbalancerVisitor(_lb);
        lbRdr.accept(lbVstr);

        ClusterReader clusterReaders[] = lbRdr.getClusters();


        buffer.append("otd.properties");

        for(int i=0;i<clusterReaders.length;i++) {
            StringBuffer clusterHostList = new StringBuffer();
            String clusterWebList = "";
            ClusterReader clusterReader = clusterReaders[i];
            String clusterName = clusterReader.getName();
            WebModuleReader webmoduleReaders[] = clusterReader.getWebModules();
            InstanceReader instanceReaders[] = clusterReader.getInstances();

            for(int j =0; j<instanceReaders.length;j++) {
                InstanceReader instanceReader = instanceReaders[j];
                String listenerHost = "";
                String listenerPort = "";
                StringTokenizer st = new StringTokenizer(instanceReader.getListeners(), " ");
                while (st.hasMoreElements()) {
                    String listener = st.nextToken();
                    if (listener.contains("http://")) {
                        listenerHost = listener.substring(listener.lastIndexOf("/") + 1, listener.lastIndexOf(":"));
                        listenerPort = listener.substring(listener.lastIndexOf(":") + 1, listener.length());
                        break;
                    }
                }
                clusterHostList = clusterHostList.append(j > 0 ? "," : "").append(listenerHost).append(":").append(listenerPort);
            }

            props.setProperty(CLUSTER+SEPARATOR+clusterName+SEPARATOR+LISTENER,clusterHostList.toString());


            for (int m=0; m<webmoduleReaders.length;m++) {
               clusterWebList = clusterWebList + (m > 0 ? "," : "") + webmoduleReaders[m].getContextRoot();
            }

            props.setProperty(CLUSTER+SEPARATOR+clusterName+SEPARATOR+WEB,clusterWebList);
        }

        try {

        props.store(out,buffer.toString());

        } finally {
            if (out != null) {
                out.close();
                out = null;
            }
        }
    }

    private static final String PUBLICID =
            "-//Sun Microsystems Inc.//DTD Sun Java System Application Server 9.1//EN";
    private static final String SYSTEMID = "glassfish-loadbalancer_1_3.dtd";
}


