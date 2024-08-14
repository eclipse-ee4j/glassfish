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

/*
 * @(#)MQRMIClientSocketFactory.java    1.5 06/29/07
 */

/*
 *  IMPORTANT NOTE: Please do not modify this file directly. This source code is owned and shipped as a part of MQ but has only been included here
 *  since it is required for certain JMX operations especially when MQ is running in the HA mode. Please refer to GF issue 13602 for more details.
 */

package com.sun.messaging.jmq.management;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.rmi.ssl.SslRMIClientSocketFactory;

public class MQRMIClientSocketFactory extends SslRMIClientSocketFactory {
    boolean debug = false;
    boolean isBrokerHostTrusted = true;
    boolean useSSL = false;
    String hostname = null;

    public MQRMIClientSocketFactory(String hostname, boolean isBrokerHostTrusted, boolean useSSL) {
        this.isBrokerHostTrusted = isBrokerHostTrusted;
        this.hostname = hostname;
        this.useSSL = useSSL;
    }


    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket s = null;
        String socketHost = hostname;

        /*
         * If the factory is not configured for any specific host, use whatever
         * is passed in to createSocket.
         *
         * The wildcard "*" here is something that could be set on the server
         * side. It is the constant jmsserver.Globals.HOSTNAME_ALL but we don't
         * want to introduce any server side compile time dependencies here.
         * Remember that this factory is created by the server.
         */
        if ((socketHost == null) || (socketHost.equals("*")))  {
            socketHost = host;
        }

        try  {
            if (useSSL)  {
                s = makeSSLSocket(socketHost, port);
            } else  {
                s = RMISocketFactory.getDefaultSocketFactory().
                createSocket(socketHost, port);
            }
        } catch (Exception e)  {
            throw new IOException(e.toString());
        }

        return (s);
    }

    @Override
    public String toString()  {
        return ("hostname="
        + hostname
        + ",isBrokerHostTrusted="
        + isBrokerHostTrusted
        + ",useSSL="
        + useSSL);
    }

    @Override
    public boolean equals(Object obj)  {
        if (!(obj instanceof MQRMIClientSocketFactory))  {
            return (false);
        }

        MQRMIClientSocketFactory that = (MQRMIClientSocketFactory)obj;

        if (this.hostname != null)  {
            if ((that.hostname == null) || !that.hostname.equals(this.hostname))  {
                return (false);
            }
        } else  {
            if (that.hostname != null)  {
                return (false);
            }
        }

        if (this.isBrokerHostTrusted != that.isBrokerHostTrusted)  {
            return (false);
        }

        if (this.useSSL != that.useSSL)  {
            return (false);
        }

        return (true);
    }


    @Override
    public int hashCode()  {
        return toString().hashCode();
    }


    private SSLSocket makeSSLSocket(String host, int port) throws Exception {
        SSLSocketFactory sslFactory;

        if (isBrokerHostTrusted) {
            sslFactory = getTrustSocketFactory();

            if ( debug ) {
                System.err.println("Broker is trusted ...");
            }
        } else {
            sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        }

        //This is here for QA to verify that SSL is used ...
        if ( debug ) {
            System.err.println ("Create connection using SSL protocol ...");
            System.err.println ("Broker Host: " + host);
            System.err.println ("Broker Port: " + port);
        }

        Object socket = sslFactory.createSocket (host, port);
        SSLSocket sslSocket = null;
        if (socket instanceof SSLSocket) {
            sslSocket = (SSLSocket) socket;

            //tcp no delay flag
            boolean tcpNoDelay = true;
            String prop = System.getProperty("imqTcpNoDelay", "true");
            if ( prop.equals("false") ) {
                tcpNoDelay = false;
            } else {
                sslSocket.setTcpNoDelay(tcpNoDelay);
            }
        }
        return sslSocket;
    }


    private SSLSocketFactory getTrustSocketFactory() throws Exception {
        SSLSocketFactory factory = null;

        SSLContext ctx;
        ctx = SSLContext.getInstance("TLS");
        TrustManager[] tm = new TrustManager [1];
        tm[0] = new DefaultTrustManager();

        ctx.init(null, tm, null);
        factory = ctx.getSocketFactory();

        return factory;
    }

}
