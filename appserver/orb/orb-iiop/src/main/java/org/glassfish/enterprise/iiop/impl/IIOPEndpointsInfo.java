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

package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.logging.LogDomains;

import java.util.List;
import java.util.logging.Logger;

import org.glassfish.enterprise.iiop.util.IIOPUtils;

/**
 * This class is responsible for reading the domain.xml via Config API
 * and producing a list of instances in the form of ClusterInstanceInfo
 * objects.
 * This class is designed for use by both FailoverIORInterceptor
 * and Java Web Start.
 * @author Sheetal Vartak
 * @date 1/12/05
 */
public class IIOPEndpointsInfo {

    private static final IIOPUtils iiopUtils = IIOPUtils.getInstance();

    private static Logger _logger = LogDomains.getLogger(IIOPEndpointsInfo.class, LogDomains.CORBA_LOGGER);

    private static final String baseMsg    = IIOPEndpointsInfo.class.getName();


    /**
     * TODO implement post V3 FCS
    public static Collection<ServerRef> getServersInCluster() {

        return iiopUtils.getServerRefs();
    }

    public static List<IiopListener> getListenersInCluster() {


        return iiopUtils.getIiopListeners();
    }

    **/

    /**
     * This method returns a list of SocketInfo objects for a particular
     * server. This method is the common code called by
     * getIIOPEndpoints() and getClusterInstanceInfo()
     */
    /*
    public static List<SocketInfo> getSocketInfoForServer(ServerRef serverRef,
                              IiopListener[] listen) {

        List<SocketInfo> listOfSocketInfo =
            new LinkedList<SocketInfo>();
    String serverName = serverRef.getRef();
    String hostName =
      getHostNameForServerInstance(serverName);
    if (hostName == null) {
        hostName = listen[0].getAddress();
    }
    for (int j = 0; j < listen.length; j++) {
        String id = listen[j].getId();
        String port =
          getResolvedPort(listen[j], serverName);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,
                baseMsg + ".getSocketInfoForServer:" +
                " adding address for "+
                serverName + "/" + id +
                "/" + hostName + "/" + port);
        }
        listOfSocketInfo.add(new SocketInfo(id, hostName, Integer.valueOf(port)));
    }
    return listOfSocketInfo;
    }
    */

    /**
     * This method returns the endpoints in host:port,host1:port1,host2:port2,...
     * format. This is called by Java Web Start
     */
    public static String getIIOPEndpoints() {
        // TODO FIXME
        String endpoints = null;
        return endpoints;
    }

    /**
     * This method returns a ClusterInstanceInfo list.
     */
    public static List<ClusterInstanceInfo> getClusterInstanceInfo() {
        // TODO FIXME
        return null;
    }

    /**
      * The following returns the IIOP listener(s) for all the
      * servers belonging to the current cluster.
      *
      * @author  satish.viswanatham@sun.com
      *
      */
    /*
    public static IiopListener[][] getIIOPEndPointsForCurrentCluster() {
    // For each server instance in a cluster, there are 3 iiop listeners:
    // one for non ssl, one for ssl and third for ssl mutual auth

        IiopListener[][] listeners = new IiopListener[serverRefs.length][3];  //SHEETAL can there be multiple SSL or
                                                                         //SSL_MUTH_AUTH ports? bug 6321813
    for (int i = 0; i < serverRefs.length; i++) {
        Server server =
        ServerHelper.getServerByName(configCtx, serverRefs[i].getRef());
        String configRef = server.getConfigRef();
        Config config =
        ConfigAPIHelper.getConfigByName(configCtx, configRef);
        IiopService iiopService = config.getIiopService();
        listeners[i] = iiopService.getIiopListener();
    }
    return listeners;
    }
    */
    /**
     * Returns ip address from node agent refered from instance
     * or null if Exception
     *
     * @author  sridatta.viswanath@sun.com
     */
    /*
    public static String getHostNameForServerInstance(String serverName)
    {
        try {
            JMXConnectorConfig info =
        ServerHelper.getJMXConnectorInfo(configCtx, serverName);
            _logger.log(Level.FINE,
            baseMsg + ".getHostNameForServerInstance: " +
            "found info: " + info.toString());
        String host = info.getHost();
            _logger.log(Level.FINE,
            baseMsg + ".getHostNameForServerInstance: " +
            "found host: " + host);
            return host;
        } catch (Throwable e){
            _logger.log(Level.FINE,
            baseMsg + ".getHostNameForServerInstance: " +
            "gotException: " + e + " " + e.getMessage() +
            "; returning null");
            return null;
        }
    }
    */
    /**
     * Gets the correct resolved value for the specific instance
     * Without this routine, config context resolves the value
     * to the current running instance
     *
     * @author  sridatta.viswanath@sun.com
     */
    /*
    public static String getResolvedPort(IiopListener l,
                      String server) {
    String rawPort = l.getRawAttributeValue("port");
    PropertyResolver pr = new PropertyResolver(configCtx, server);
    return pr.resolve(rawPort);
    }
    */
}
