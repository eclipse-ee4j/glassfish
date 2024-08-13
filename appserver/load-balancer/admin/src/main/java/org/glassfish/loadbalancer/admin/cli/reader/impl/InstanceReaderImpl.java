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

package org.glassfish.loadbalancer.admin.cli.reader.impl;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.ServerTags;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.config.support.PropertyResolver;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.loadbalancer.admin.cli.LbLogUtil;
import org.glassfish.loadbalancer.admin.cli.reader.api.InstanceReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.LbReaderException;
import org.glassfish.loadbalancer.admin.cli.reader.api.LoadbalancerReader;
import org.glassfish.loadbalancer.admin.cli.transform.InstanceVisitor;
import org.glassfish.loadbalancer.admin.cli.transform.Visitor;

/**
 * Provides instance information relavant to Load balancer tier.
 *
 * @author Kshitiz Saxena
 */
public class InstanceReaderImpl implements InstanceReader {

    /**
     * Constructor
     */
    public InstanceReaderImpl(Domain domain, ServerRef ref) {
        _domain = domain;
        _serverRef = ref;
        _server = domain.getServerNamed(ref.getRef());
    }

    public InstanceReaderImpl(Domain domain, Server server) {
        _domain = domain;
        _server = server;
    }

    /**
     * Return server instance's name.
     *
     * @return String           instance' name
     */
    @Override
    public String getName() throws LbReaderException {
        return _server.getName();
    }

    /**
     * Returns if the server is enabled in the load balancer or not.
     *
     * @return boolean          true if enabled in LB; false if disabled
     */
    @Override
    public boolean getLbEnabled() throws LbReaderException {
        if(_serverRef != null){
            return Boolean.valueOf(_serverRef.getLbEnabled()).booleanValue();
        }
        return LoadbalancerReader.LBENABLED_VALUE;
    }

    /**
     * This is used in quicescing. Timeouts after this interval and disables the
     * instance in the load balancer.
     *
     * @return String           Disable time out in minutes
     */
    @Override
    public String getDisableTimeoutInMinutes() throws LbReaderException {
        if(_serverRef != null) {
            return _serverRef.getDisableTimeoutInMinutes();
        }
        return LoadbalancerReader.DISABLE_TIMEOUT_IN_MINUTES_VALUE;
    }

    /**
     * This is used in weighted round robin. returns the weight of the instance
     *
     * @return String           Weight of the instance
     */
    @Override
    public String getWeight() throws LbReaderException {
        return _server.getLbWeight();
    }

    /**
     * Enlists both http and https listeners of this server instance
     * It will be form "http:<hostname>:<port> https:<hostname>:<port>"
     *
     * @return String   Listener(s) info.
     */
    @Override
    public String getListeners() throws LbReaderException {
        StringBuffer listenerStr = new StringBuffer();

        Config config = _domain.getConfigNamed(_server.getConfigRef());
        NetworkConfig networkConfig = config.getNetworkConfig();
        Protocols protocols = networkConfig.getProtocols();
        NetworkListeners nls = networkConfig.getNetworkListeners();
        Iterator<NetworkListener> listenerIter = nls.getNetworkListener().iterator();

        int i = 0;
        PropertyResolver resolver = new PropertyResolver(_domain, _server.getName());
        while (listenerIter.hasNext()) {
            NetworkListener listener = listenerIter.next();
            NetworkListener rawListener = GlassFishConfigBean.getRawView(listener);
            if (rawListener.getName().equals(ADMIN_LISTENER)) {
                continue;
            }

            String prot = rawListener.getProtocol();
            Protocol protocol = protocols.findProtocol(prot);

            if (i > 0) {
                listenerStr.append(' '); // space between listener names
            }
            i++;

            if (Boolean.valueOf(protocol.getHttp().getJkEnabled())){
                listenerStr.append(AJP_PROTO);
            } else {
            if (Boolean.valueOf(protocol.getSecurityEnabled()).booleanValue()) {
                listenerStr.append(HTTPS_PROTO);
            } else {
                listenerStr.append(HTTP_PROTO);
            }
            }
            String hostName = getResolvedHostName(rawListener.getAddress());
            listenerStr.append(hostName);
            listenerStr.append(':');
            // resolve the port name
            String port = rawListener.getPort();

            // If it is system variable, resolve it
            if ((port != null) && (port.length() > 1) && (port.charAt(0) == '$')
                    && (port.charAt(1) == '{') && (port.charAt(port.length() - 1) == '}')) {
                String portVar = port.substring(2, port.length() - 1);
                port = resolver.getPropertyValue(portVar);
                if (port == null) {
                    throw new LbReaderException(LbLogUtil.getStringManager().getString("UnableToResolveSystemProperty", portVar, _server.getName()));
                }
            }
            listenerStr.append(port);
        }
        return listenerStr.toString();
    }

    // --- VISITOR IMPLEMENTATION ---
    @Override
    public void accept(Visitor v) throws Exception {
        if (v instanceof InstanceVisitor) {
            InstanceVisitor pv = (InstanceVisitor) v;
            pv.visit(this);
        }
    }

    private String getResolvedHostName(String address) throws LbReaderException {
        InetAddress addr = null;
        if (!address.equals(BIND_TO_ANY)) {
            try {
                addr = InetAddress.getByName(address);
            } catch (UnknownHostException ex) {
                String msg = LbLogUtil.getStringManager().getString("CannotResolveHostName", address);
                throw new LbReaderException(msg, ex);
            }
            if (!addr.isLoopbackAddress()) {
                return address;
            }
        }
        String nodeName = _server.getNodeRef();
        Node node = _domain.getNodes().getNode(nodeName);
        if (node == null) {
            String msg = LbLogUtil.getStringManager().getString("UnableToGetNode", _server.getName());
            throw new LbReaderException(msg);
        }
        if (node.getNodeHost() != null && !node.getNodeHost().equals(LOCALHOST)) {
            return node.getNodeHost();
        }
        return System.getProperty("com.sun.aas.hostName");
    }
    // --- PRIVATE VARS -------
    private Domain _domain = null;
    private ServerRef _serverRef = null;
    private Server _server = null;
    private static final String HTTP_PROTO = "http://";
    private static final String HTTPS_PROTO = "https://";
    private static final String AJP_PROTO = "ajp://";
    private static final String ADMIN_LISTENER = ServerTags.ADMIN_LISTENER_ID;
    private static final String BIND_TO_ANY = "0.0.0.0";
    private static final String LOCALHOST = "localhost";
}
