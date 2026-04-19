/*
 * Copyright (c) 2023, 2026 Contributors to the Eclipse Foundation.
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
import com.sun.corba.ee.spi.copyobject.CopyobjectDefaults;
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
import com.sun.enterprise.util.net.NetUtils;

import java.lang.System.Logger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.enterprise.iiop.api.GlassFishORBLocator;
import org.glassfish.enterprise.iiop.api.IIOPConstants;
import org.glassfish.enterprise.iiop.util.IIOPUtils;
import org.glassfish.enterprise.iiop.util.S1ASThreadPoolManager;
import org.glassfish.enterprise.iiop.util.ThreadPoolStats;
import org.glassfish.enterprise.iiop.util.ThreadPoolStatsImpl;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.internal.api.Globals;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory ;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;

public class PEORBConfigurator implements ORBConfigurator {
    private static final Logger LOG = System.getLogger(PEORBConfigurator.class.getName());

    private static final String SSL = "SSL";
    private static final String SSL_MUTUALAUTH = "SSL_MUTUALAUTH";
    private static final String IIOP_CLEAR_TEXT_CONNECTION = "IIOP_CLEAR_TEXT";
    private static final String DEFAULT_ORB_INIT_HOST = NetUtils.getHostName();
    private static final Set<String> ANY_ADDRS = new HashSet<>(Arrays.asList("0.0.0.0", "::", "::ffff:0.0.0.0"));

    @Override
    public synchronized void configure(DataCollector dc, ORB orb) {
        LOG.log(TRACE, "configure(dc, orb)");
        // In the server-case, iiop acceptors need to be set up after the
        // initial part of the orb creation but before any
        // portable interceptor initialization
        final IIOPUtils iiopUtils = Globals.get(IIOPUtils.class);
        if (iiopUtils.getProcessType().isServer()) {
            orb.setThreadPoolManager(S1ASThreadPoolManager.getThreadPoolManager());
            try {
                configureStats(orb);
            } catch (NoSuchWorkQueueException ex) {
                LOG.log(ERROR, "Failed to configure ORB: " + orb, ex);
            }
        }

        configureCopiers(orb);
        orb.setInvocationInterceptor(new PEORBConfiguratorNOOPInterceptor());

        if (iiopUtils.getProcessType().isServer()) {
            IiopListener[] iiopListeners = iiopUtils.getIiopService().getIiopListener().toArray(IiopListener[]::new);
            createORBListeners(iiopUtils, iiopListeners, orb);
            // Requires the POAFactory.registerRootPOA()
            new TransientNameService(orb);
            GlassFishORBLocator.setThreadLocal(orb);
        }
    }

    private void configureStats(ORB orb) throws NoSuchWorkQueueException {
        // Do the stats for the threadpool
        ThreadPoolManager tpool =  orb.getThreadPoolManager();
        // ORB creates its own threadpool if threadpoolMgr was null above
        ThreadPool thpool = tpool.getDefaultThreadPool();
        ThreadPoolStats tpStats = new ThreadPoolStatsImpl(thpool.getWorkQueue(0).getThreadPool());
        String subTreeRoot = "thread-pool/orb/threadpool/" + thpool.getName();
        StatsProviderManager.register("orb", PluginPoint.SERVER, subTreeRoot, tpStats);
    }

    private void createORBListeners(IIOPUtils iiopUtils, IiopListener[] iiopListenerBeans, org.omg.CORBA.ORB orb) {
        if (iiopListenerBeans == null) {
            return;
        }

        var lazyListeners = Stream.of(iiopListenerBeans)
            .filter(ilb -> Boolean.parseBoolean(ilb.getLazyInit())).collect(Collectors.toList());

        if (lazyListeners.size() > 1) {
            throw new IllegalStateException("Only one iiop-listener can be configured with lazy-init=true. "
                + lazyListeners.stream().map(IiopListener::getId).collect(Collectors.toList()));
        }

        var lazySslListeners = lazyListeners.stream()
            .filter(ilb -> Boolean.parseBoolean(ilb.getSecurityEnabled()) && ilb.getSsl() != null)
            .collect(Collectors.toList());

        if (!lazySslListeners.isEmpty()) {
            throw new IllegalStateException("Lazy-init not supported for SSL iiop-listeners. "
                + lazySslListeners.stream().map(IiopListener::getId).collect(Collectors.toList()));
        }

        for (IiopListener iiopListener : iiopListenerBeans) {
            if (!Boolean.parseBoolean(iiopListener.getEnabled())) {
                continue;
            }

            boolean isLazy = Boolean.parseBoolean(iiopListener.getLazyInit());
            String host = handleAddrAny(iiopListener.getAddress());
            int port = Integer.parseInt(iiopListener.getPort());
            boolean isSslListener = Boolean.parseBoolean(iiopListener.getSecurityEnabled()) && iiopListener.getSsl() != null;
            if (isSslListener) {
                Ssl sslBean = iiopListener.getSsl();
                boolean clientAuth = Boolean.parseBoolean(sslBean.getClientAuthEnabled());
                String type = clientAuth ? SSL_MUTUALAUTH : SSL;
                addAcceptor(orb, isLazy, host, type, port);
            } else {
                Acceptor acceptor = addAcceptor(orb, isLazy, host, IIOP_CLEAR_TEXT_CONNECTION, port);
                if (isLazy) {
                    GlassFishORBLocator.setThreadLocal(new AcceptorDelegate(acceptor));
                }
            }
        }
    }

    private String handleAddrAny(String hostAddr) {
        if (!ANY_ADDRS.contains(hostAddr)) {
            return hostAddr;
        }
        return DEFAULT_ORB_INIT_HOST;
    }

    private Acceptor addAcceptor(org.omg.CORBA.ORB orb, boolean isLazy, String host, String type, int port) {
        LOG.log(DEBUG, "addAcceptor(orb, isLazy={0}, host={1}, type={2}, port={3})", isLazy, host, type, port);
        ORB theOrb = (ORB) orb;
        TransportManager transportManager = theOrb.getTransportManager();
        Acceptor acceptor;
        if (isLazy) {
            acceptor = TransportDefault.makeLazyCorbaAcceptor(theOrb, port, host, type);
        } else {
            acceptor = TransportDefault.makeStandardCorbaAcceptor(theOrb, port, host, type);
        }
        transportManager.registerAcceptor(acceptor);
        return acceptor;
    }

    private static void configureCopiers(ORB orb) {
        CopierManager cpm = orb.getCopierManager();
        ObjectCopierFactory stream = CopyobjectDefaults.makeORBStreamObjectCopierFactory(orb);
        ObjectCopierFactory reflect = CopyobjectDefaults.makeReflectObjectCopierFactory(orb);
        ObjectCopierFactory fallback = CopyobjectDefaults.makeFallbackObjectCopierFactory(reflect, stream);
        ObjectCopierFactory reference = CopyobjectDefaults.getReferenceObjectCopierFactory();
        cpm.registerObjectCopierFactory(fallback, IIOPConstants.PASS_BY_VALUE_ID);
        cpm.registerObjectCopierFactory(reference, IIOPConstants.PASS_BY_REFERENCE_ID);
        cpm.setDefaultId(IIOPConstants.PASS_BY_VALUE_ID);
    }

    private static final class PEORBConfiguratorNOOPInterceptor implements InvocationInterceptor {
        @Override
        public void preInvoke() {
        }


        @Override
        public void postInvoke() {
        }
    }

}
