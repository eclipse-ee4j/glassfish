/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService;
import com.sun.corba.ee.spi.copyobject.CopierManager;
import com.sun.corba.ee.spi.copyobject.CopyobjectDefaults ;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.presentation.rmi.InvocationInterceptor;
import com.sun.corba.ee.spi.threadpool.NoSuchWorkQueueException;
import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.TransportDefault;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.logging.LogDomains;

import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.enterprise.iiop.api.IIOPConstants;
import org.glassfish.enterprise.iiop.util.IIOPUtils;
import org.glassfish.enterprise.iiop.util.S1ASThreadPoolManager;
import org.glassfish.enterprise.iiop.util.ThreadPoolStats;
import org.glassfish.enterprise.iiop.util.ThreadPoolStatsImpl;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory ;

public class PEORBConfigurator implements ORBConfigurator {
    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(LogDomains.CORBA_LOGGER);

    private static final String SSL = "SSL";
    private static final String SSL_MUTUALAUTH = "SSL_MUTUALAUTH";
    private static final String IIOP_CLEAR_TEXT_CONNECTION =
            "IIOP_CLEAR_TEXT";
    private static final String DEFAULT_ORB_INIT_HOST = "localhost";

    // TODO private static TSIdentification tsIdent;
    private static ThreadPoolManager threadpoolMgr = null;

    static {
        // TODO tsIdent = new TSIdentificationImpl();
    }

    private GlassFishORBHelper getHelper() {
        IIOPUtils iiopUtils = IIOPUtils.getInstance();
        return iiopUtils.getHabitat().getService(GlassFishORBHelper.class);
    }

    @Override
    public void configure(DataCollector dc, ORB orb) {
        try {
            //begin temp fix for bug 6320008
            // this is needed only because we are using transient Name Service
            //this should be removed once we have the persistent Name Service in place
            /*TODO
            orb.setBadServerIdHandler(
            new BadServerIdHandler() {
            public void handle(ObjectKey objectkey) {
            // NO-OP
            }
            }
            );
             */
            //end temp fix for bug 6320008
            if (threadpoolMgr != null) {
                // This will be the case for the Server Side ORB created
                // For client side threadpoolMgr will be null, so we will
                // never come here
              orb.setThreadPoolManager(threadpoolMgr);
            }

            // Do the stats for the threadpool

            ThreadPoolManager tpool =  orb.getThreadPoolManager();
            // ORB creates its own threadpool if threadpoolMgr was null above
            ThreadPool thpool=tpool.getDefaultThreadPool();
            String ThreadPoolName = thpool.getName();
            ThreadPoolStats tpStats = new ThreadPoolStatsImpl(
                thpool.getWorkQueue(0).getThreadPool());
            StatsProviderManager.register("orb", PluginPoint.SERVER,
                "thread-pool/orb/threadpool/"+ThreadPoolName, tpStats);

            configureCopiers(orb);
            configureCallflowInvocationInterceptor(orb);

            // In the server-case, iiop acceptors need to be set up after the
            // initial part of the orb creation but before any
            // portable interceptor initialization
            IIOPUtils iiopUtils = IIOPUtils.getInstance();
            if (iiopUtils.getProcessType().isServer()) {
                List<IiopListener> iiop_listener_list = IIOPUtils.getInstance()
                        .getIiopService().getIiopListener() ;
                IiopListener[] iiopListenerBeans =  iiop_listener_list
                        .toArray(new IiopListener [iiop_listener_list.size()]) ;
                this.createORBListeners(iiopUtils, iiopListenerBeans, orb);
            }
            if (orb.getORBData().environmentIsGFServer()) {
                // Start the transient name service, which publishes NameService
                // in the ORB's local resolver.
                new TransientNameService(orb);
            }
            // Publish the ORB reference back to GlassFishORBHelper, so that
            // subsequent calls from interceptor ORBInitializers can call
            // GlassFishORBHelper.getORB() without problems.  This is
            // especially important for code running in the service initializer
            // thread.
            getHelper().setORB(orb);
        } catch (NoSuchWorkQueueException ex) {
            Logger.getLogger(PEORBConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
        }

    private static void configureCopiers(ORB orb) {
        CopierManager cpm = orb.getCopierManager();

        ObjectCopierFactory stream =
            CopyobjectDefaults.makeORBStreamObjectCopierFactory(orb) ;
        ObjectCopierFactory reflect =
            CopyobjectDefaults.makeReflectObjectCopierFactory(orb) ;
        ObjectCopierFactory fallback =
            CopyobjectDefaults.makeFallbackObjectCopierFactory( reflect, stream ) ;
        ObjectCopierFactory reference =
            CopyobjectDefaults.getReferenceObjectCopierFactory() ;

        cpm.registerObjectCopierFactory( fallback, IIOPConstants.PASS_BY_VALUE_ID ) ;
        cpm.registerObjectCopierFactory( reference, IIOPConstants.PASS_BY_REFERENCE_ID ) ;
        cpm.setDefaultId( IIOPConstants.PASS_BY_VALUE_ID ) ;
    }

    // Called from GlassFishORBManager only when the ORB is running on server side
    public static void setThreadPoolManager() {
        threadpoolMgr = S1ASThreadPoolManager.getThreadPoolManager();
    }

    private static void configureCallflowInvocationInterceptor(ORB orb) {
        orb.setInvocationInterceptor(
                new InvocationInterceptor() {
            @Override
                    public void preInvoke() {
                        /*    TODO
                  Agent agent = Switch.getSwitch().getCallFlowAgent();
                  if (agent != null) {
                      agent.startTime(
                          ContainerTypeOrApplicationType.ORB_CONTAINER);
                  }
                  */
                    }

            @Override
                    public void postInvoke() {
                        /*   TODO
                  Agent agent = Switch.getSwitch().getCallFlowAgent();
                  if (agent != null) {
                      agent.endTime();
                  }
                  */
                    }
                }
        );
    }

    private Acceptor addAcceptor( org.omg.CORBA.ORB orb, boolean isLazy,
        String host, String type, int port ) {

        com.sun.corba.ee.spi.orb.ORB theOrb = (com.sun.corba.ee.spi.orb.ORB) orb;
        TransportManager ctm = theOrb.getTransportManager() ;
        Acceptor acceptor ;
        if (isLazy) {
            acceptor = TransportDefault.makeLazyCorbaAcceptor(
                theOrb, port, host, type );
        } else {
            acceptor = TransportDefault.makeStandardCorbaAcceptor(
                theOrb, port, host, type ) ;
        }
        ctm.registerAcceptor( acceptor ) ;
        return acceptor;
    }

    private static final Set<String> ANY_ADDRS = new HashSet<>(
        Arrays.asList( "0.0.0.0", "::", "::ffff:0.0.0.0" ) ) ;

    private String handleAddrAny( String hostAddr )  {
        if (ANY_ADDRS.contains( hostAddr )) {
            try {
                return java.net.InetAddress.getLocalHost().getHostAddress() ;
            } catch (java.net.UnknownHostException exc) {
                logger.log( Level.WARNING,
                    "Unknown host exception : Setting host to localhost" ) ;
                return DEFAULT_ORB_INIT_HOST ;
            }
        } else {
            return hostAddr ;
        }
    }

    private void createORBListeners(IIOPUtils iiopUtils,
            IiopListener[] iiopListenerBeans, org.omg.CORBA.ORB orb) {

        if (iiopListenerBeans == null) {
            return;
        }

        var lazyListeners = Stream.of(iiopListenerBeans)
                .filter(ilb -> Boolean.parseBoolean(ilb.getLazyInit())).collect(Collectors.toList());

        if (lazyListeners.size() > 1) {
            throw new IllegalStateException(
                    "Only one iiop-listener can be configured with lazy-init=true. "
                            + lazyListeners.stream().map(IiopListener::getId).collect(Collectors.toList()));
        }

        var lazySslListeners = lazyListeners.stream()
                .filter(ilb -> Boolean.valueOf(ilb.getSecurityEnabled()) && ilb.getSsl() != null)
                .collect(Collectors.toList());

        if (lazySslListeners.size() > 0) {
            throw new IllegalStateException(
                    "Lazy-init not supported for SSL iiop-listeners. "
                            + lazySslListeners.stream().map(IiopListener::getId).collect(Collectors.toList()));
        }

        for (IiopListener ilb : iiopListenerBeans) {
            if (!Boolean.parseBoolean(ilb.getEnabled())) {
                continue;
            }

            boolean isLazy = Boolean.parseBoolean(ilb.getLazyInit());
            int port = Integer.parseInt(ilb.getPort());
            String host = handleAddrAny(ilb.getAddress());

            boolean isSslListener = Boolean.valueOf(ilb.getSecurityEnabled()) && ilb.getSsl() != null;
            if (isSslListener) {
                Ssl sslBean = ilb.getSsl();
                boolean clientAuth = Boolean.parseBoolean(
                        sslBean.getClientAuthEnabled());
                String type = clientAuth ? SSL_MUTUALAUTH : SSL;
                addAcceptor(orb, isLazy, host, type, port);
            } else {
                Acceptor acceptor = addAcceptor(orb, isLazy, host,
                        IIOP_CLEAR_TEXT_CONNECTION, port);
                if (isLazy) {
                    getHelper().setSelectableChannelDelegate(new AcceptorDelegateImpl(
                            acceptor));
                }
            }
        }
    }

    private static class AcceptorDelegateImpl
        implements GlassFishORBHelper.SelectableChannelDelegate {

        private final Acceptor acceptor;

        AcceptorDelegateImpl(Acceptor lazyAcceptor) {
            acceptor = lazyAcceptor;
        }

        @Override
        public void handleRequest(SelectableChannel channel) {
            SocketChannel sch = (SocketChannel)channel ;
            Socket socket = sch.socket() ;
            acceptor.processSocket( socket ) ;
        }
    }
}
