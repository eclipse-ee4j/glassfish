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

package org.glassfish.enterprise.api.enabler;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.v3.services.impl.DummyNetworkListener;
import com.sun.enterprise.v3.services.impl.GrizzlyService;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.jvnet.hk2.annotations.Service;

/**
 */
@Service
@RunLevel(StartupRunLevel.VAL)
public class ORBConnectorStartup implements PostConstruct {

    // RMI-IIOP delegate constants
    private static final String ORB_UTIL_CLASS_PROPERTY =
               "javax.rmi.CORBA.UtilClass";
    private static final String RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY =
               "javax.rmi.CORBA.StubClass";
    private static final String RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY =
               "javax.rmi.CORBA.PortableRemoteObjectClass";

       // ORB constants: OMG standard
    private static final String OMG_ORB_CLASS_PROPERTY =
               "org.omg.CORBA.ORBClass";
    private static final String OMG_ORB_SINGLETON_CLASS_PROPERTY =
               "org.omg.CORBA.ORBSingletonClass";

    private static final String ORB_CLASS =
               "com.sun.corba.ee.impl.orb.ORBImpl";
    private static final String ORB_SINGLETON_CLASS =
               "com.sun.corba.ee.impl.orb.ORBSingleton";

    private static final String ORB_SE_CLASS =
               "com.sun.corba.se.impl.orb.ORBImpl";
    private static final String ORB_SE_SINGLETON_CLASS =
               "com.sun.corba.se.impl.orb.ORBSingleton";

    private static final String RMI_UTIL_CLASS =
               "com.sun.corba.ee.impl.javax.rmi.CORBA.Util";
    private static final String RMI_STUB_CLASS =
               "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl";
    private static final String RMI_PRO_CLASS =
               "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject";

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    private GrizzlyService grizzlyService;

    public void postConstruct()
    {
        setORBSystemProperties();
        initializeLazyListener();
    }

/**
     * Set ORB-related system properties that are required in case
     * user code in the app server or app client container creates a
     * new ORB instance.  The default result of calling
     * ORB.init( String[], Properties ) must be a fully usuable, consistent
     * ORB.  This avoids difficulties with having the ORB class set
     * to a different ORB than the RMI-IIOP delegates.
     */
    private void setORBSystemProperties() {

        java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public java.lang.Object run() {
                        if (System.getProperty(OMG_ORB_CLASS_PROPERTY) == null) {
                            // Assume Sun ee ORB at all times.
                            // set ORB based on JVM vendor
                            //
                            // if (System.getProperty("java.vendor").equals("Sun Microsystems Inc.")) {
                                // System.setProperty(OMG_ORB_CLASS_PROPERTY, ORB_SE_CLASS);
                            // } else {
                                // if not Sun, then set to EE class
                                System.setProperty(OMG_ORB_CLASS_PROPERTY, ORB_CLASS);
                            // }
                        }

                        if (System.getProperty(OMG_ORB_SINGLETON_CLASS_PROPERTY) == null) {
                            // Assume Sun ee ORB at all times.
                            //
                            // set ORBSingleton based on JVM vendor
                            // if (System.getProperty("java.vendor").equals("Sun Microsystems Inc.")) {
                                // System.setProperty(OMG_ORB_SINGLETON_CLASS_PROPERTY, ORB_SE_SINGLETON_CLASS);
                            // } else {
                                // if not Sun, then set to EE class
                                System.setProperty(OMG_ORB_SINGLETON_CLASS_PROPERTY, ORB_SINGLETON_CLASS);
                            // }
                        }

                        System.setProperty(ORB_UTIL_CLASS_PROPERTY,
                                RMI_UTIL_CLASS);

                        System.setProperty(RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY,
                                RMI_STUB_CLASS);

                        System.setProperty(RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY,
                                RMI_PRO_CLASS);

                        return null;
                    }
                }
        );
    }

    /**
     * Start Grizzly based ORB lazy listener, which is going to initialize
     * ORB container on first request.
     */
    private void initializeLazyListener() {
        final IiopService iiopService = config.getExtensionByType(IiopService.class);
        if (iiopService != null) {
            List<IiopListener> iiopListenerList = iiopService.getIiopListener();
            for (IiopListener oneListener : iiopListenerList) {
                if (Boolean.valueOf(oneListener.getEnabled()) && Boolean.valueOf(oneListener.getLazyInit())) {
                    NetworkListener dummy = new DummyNetworkListener();
                    dummy.setPort(oneListener.getPort());
                    dummy.setAddress(oneListener.getAddress());
                    dummy.setProtocol("light-weight-listener");
                    dummy.setTransport("tcp");
                    dummy.setName("iiop-service");
                    grizzlyService.createNetworkProxy(dummy);
                }
            }
        }
    }
}
