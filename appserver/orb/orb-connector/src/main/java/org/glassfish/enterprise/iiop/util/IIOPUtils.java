/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.enterprise.iiop.util;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerRef;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.enterprise.iiop.api.GlassFishORBLifeCycleListener;
import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.ORB;

/**
 * @author Mahesh Kannan
 *         Date: Jan 15, 2009
 */
@Service
public class IIOPUtils implements PostConstruct {

    private static IIOPUtils _me;

    @Inject
    ServiceLocator services;

    @Inject
    private ClassLoaderHierarchy clHierarchy;

    @Inject
    private ProcessEnvironment processEnv;

    private ProcessType processType;

    // The following info is only available for ProcessType.Server
    private Collection<ThreadPool> threadPools;
    private IiopService iiopService;
    private Collection<ServerRef> serverRefs;

    // Set during init
    private ORB defaultORB;

    //private GlassFishORBManager gfORBMgr;

    public void postConstruct() {

        processType = processEnv.getProcessType();

        if( processEnv.getProcessType().isServer()) {

            Config c = services.getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
            iiopService =c.getExtensionByType(IiopService.class);

            final Collection<ThreadPool> threadPool = c.getThreadPools().getThreadPool();
            final Collection<NetworkListener> listeners = allByContract(NetworkListener.class);
            final Set<String> names = new TreeSet<String>();
            threadPools = new ArrayList<ThreadPool>();
            for (NetworkListener listener : listeners) {
                names.add(listener.getThreadPool());
            }
            for (ThreadPool pool : threadPool) {
                if(!names.contains(pool.getName())) {
                    threadPools.add(pool);
                }
            }
            serverRefs  = allByContract(ServerRef.class);
        }

        IIOPUtils.initMe(this);

    }

    private static void initMe(IIOPUtils utils) {
        _me = utils;
    }

    public static IIOPUtils getInstance() {
        return _me;
    }


    public static void setInstance(IIOPUtils utils) {
        _me = utils;
    }

    /*
    void setGlassFishORBManager(GlassFishORBManager orbMgr) {
        gfORBMgr = orbMgr;
    }

    GlassFishORBManager getGlassFishORBManager() {
        return gfORBMgr;
    }
    */

    public ClassLoader getCommonClassLoader() {
        return clHierarchy.getCommonClassLoader();
    }

    private void assertServer() {
        if ( !processType.isServer() ) {
            throw new IllegalStateException("Only available in Server mode");
        }
    }

    public IiopService getIiopService() {
        assertServer();
        return iiopService;
    }

    public Collection<ThreadPool> getAllThreadPools() {
        assertServer();
        return threadPools;
    }

    public Collection<ServerRef> getServerRefs() {
        assertServer();
        return serverRefs;
    }

    public List<IiopListener> getIiopListeners() {
        assertServer();
        return iiopService.getIiopListener();
    }

    public Collection<IIOPInterceptorFactory> getAllIIOPInterceptrFactories() {
        return allByContract(IIOPInterceptorFactory.class);
    }

    public Collection<GlassFishORBLifeCycleListener> getGlassFishORBLifeCycleListeners() {
        return allByContract(GlassFishORBLifeCycleListener.class);
    }

    public ProcessType getProcessType() {
        return processType;
    }

    public ServiceLocator getHabitat() {
        return services;
    }

    public void setORB(ORB orb) {
        defaultORB = orb;
    }

    // For internal use only.  All other modules should use orb-connector
    // GlassFishORBHelper to acquire default ORB.
    public ORB getORB() {
        return defaultORB;
    }

    private <T> Collection<T> allByContract(Class<T> contractClass) {
        return services.getAllServices(contractClass);
    }

}
