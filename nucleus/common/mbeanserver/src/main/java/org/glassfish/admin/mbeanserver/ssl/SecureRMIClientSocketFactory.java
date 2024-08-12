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

package org.glassfish.admin.mbeanserver.ssl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.glassfish.admin.mbeanserver.Util;
import org.glassfish.logging.annotation.LogMessageInfo;


    /**
     * Inner class for SSL support for JMX connection using RMI.
     */
    public final class SecureRMIClientSocketFactory
            extends SslRMIClientSocketFactory  {

        @LogMessageInfo(level="INFO", message="Creating a SecureRMIClientSocketFactory @ {0}with ssl config = {1}")
        private final static String creatingFactory = Util.LOG_PREFIX + "00022";

        @LogMessageInfo(level="INFO", message="Setting SSLParams @ {0}")
        private final static String settingSSLParams = Util.LOG_PREFIX + "00023";

        private InetAddress mAddress;
        private transient SSLParams sslParams;
        private transient Map socketMap = new HashMap<Integer, Socket>();
        private static final Logger  _logger = Util.JMX_LOGGER;

        public SecureRMIClientSocketFactory(final SSLParams sslParams,
                final InetAddress addr) {
            super();
            mAddress = addr;
            this.sslParams = sslParams;
            if(sslParams != null) {
                _logger.log(Level.INFO,
                        creatingFactory,
                        new Object[]{addr.getHostAddress(), sslParams.toString()});
            }
        }

        public SecureRMIClientSocketFactory() {
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SecureRMIClientSocketFactory) {
                 return(this.hashCode()==obj.hashCode());
            } else  {
                return false;
            }
        }

        @Override
        public int hashCode() {
             return mAddress.hashCode();
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            throw new IOException("Serialization not supported");
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            //debug( "MyRMIServerSocketFactory.createServerSocket(): " + mAddress + " : " + port );
            if(socketMap.containsKey(Integer.valueOf(port))) {
                return (Socket)socketMap.get(Integer.valueOf(port));
            }

            final int backlog = 5;

            SSLClientConfigurator sslCC = SSLClientConfigurator.getInstance();
            _logger.log(Level.INFO, settingSSLParams, sslParams);
            sslCC.setSSLParams(sslParams);
            SSLContext sslContext = sslCC.configure(sslParams);
            SSLSocket sslSocket =
                    (SSLSocket)sslContext.getSocketFactory().createSocket(mAddress, port);
            configureSSLSocket(sslSocket, sslCC);

            socketMap.put(Integer.valueOf(8686), sslSocket);
            return sslSocket;
        }

        /**********************************************************************
         *  Private Methods
         ********************************************************************** /

        /**
         * Configures the client socket with the enabled protocols and cipher suites.
         * @param sslSocket
         * @param sslCC
         */
        private void configureSSLSocket(SSLSocket sslSocket,
                SSLClientConfigurator sslCC) {
            String ecs[] = sslCC.getEnabledCipherSuites();
            if (ecs != null) {
                sslSocket.setEnabledCipherSuites(configureEnabledCiphers(sslSocket, ecs));
            }

            String ep[] = sslCC.getEnabledProtocols();
            if (ep != null) {
                sslSocket.setEnabledProtocols(configureEnabledProtocols(sslSocket, ep));
            }

            sslSocket.setUseClientMode(true);
        }

        /**
         * Return the list of allowed protocols.
         * @return String[] an array of supported protocols.
         */
        private static String[] configureEnabledProtocols(
                SSLSocket socket, String[] requestedProtocols) {

            String[] supportedProtocols = socket.getSupportedProtocols();
            String[] protocols = null;
            ArrayList<String> list = null;
            for (String supportedProtocol : supportedProtocols) {
                /*
                 * Check to see if the requested protocol is among the
                 * supported protocols, i.e., may be enabled
                 */
                for (String protocol : requestedProtocols) {
                    protocol = protocol.trim();
                    if (supportedProtocol.equals(protocol)) {
                        if (list == null) {
                            list = new ArrayList<String>();
                        }
                        list.add(protocol);
                        break;
                    }
                }
            }

            if (list != null) {
                protocols = list.toArray(new String[list.size()]);
            }

            return protocols;
        }

        /**
         * Determines the SSL cipher suites to be enabled.
         *
         * @return Array of SSL cipher suites to be enabled, or null if none of the
         * requested ciphers are supported
         */
        private static String[] configureEnabledCiphers(SSLSocket socket,
                String[] requestedCiphers) {

            String[] supportedCiphers = socket.getSupportedCipherSuites();
            String[] ciphers = null;
            ArrayList<String> list = null;
            for (String supportedCipher : supportedCiphers) {
                /*
                 * Check to see if the requested protocol is among the
                 * supported protocols, i.e., may be enabled
                 */
                for (String cipher : requestedCiphers) {
                    cipher = cipher.trim();
                    if (supportedCipher.equals(cipher)) {
                        if (list == null) {
                            list = new ArrayList<String>();
                        }
                        list.add(cipher);
                        break;
                    }
                }
            }

            if (list != null) {
                ciphers = list.toArray(new String[list.size()]);
            }

            return ciphers;
        }
    }
