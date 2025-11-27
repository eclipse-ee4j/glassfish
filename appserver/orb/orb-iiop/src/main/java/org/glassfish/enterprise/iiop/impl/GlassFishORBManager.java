/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager;
import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.util.Utility;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.enterprise.iiop.util.IIOPUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ORBLocator;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.glassfish.orb.admin.config.Orb;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;

import static com.sun.corba.ee.spi.misc.ORBConstants.REFERENCE_FACTORY_MANAGER;

/**
 * This class initializes the ORB with a list of (standard) properties
 * and provides a few convenience methods to get the ORB etc.
 */
public final class GlassFishORBManager {

    private static final Logger LOG = IIOPImplLogFacade.getLogger(GlassFishORBManager.class);

    private static final String ORB_SSL_STANDALONE_CLIENT_REQUIRED = "com.sun.CSIV2.ssl.standalone.client.required";

    // Set in constructor
    private final ServiceLocator services;
    private final IIOPUtils iiopUtils;

    private final int orbInitialPort;

    private final List<IiopListener> iiopListeners;
    private final IiopService iiopService;

    private final Properties csiv2Props = new Properties();

    private final ProcessType processType;

    private final IiopFolbGmsClient gmsClient;

    private final Orb orbConfig;

    // The ReferenceFactoryManager from the orb.
    private ReferenceFactoryManager rfm;


    /**
     * Keep this class private to the package.
     * Eventually we need to move all public statics or change them to package private.
     * All external orb/iiop access should go through orb-connector module.
     */
    GlassFishORBManager(ServiceLocator serviceLocator) {
        LOG.log(Level.CONFIG, "GlassFishORBManager({0})", serviceLocator);
        services = serviceLocator;
        iiopUtils = services.getService(IIOPUtils.class);
        ProcessEnvironment processEnv = services.getService(ProcessEnvironment.class);
        processType = processEnv.getProcessType();

        LOG.log(Level.FINEST, "processType: {0}", processType);
        gmsClient = processType.isServer() ? new IiopFolbGmsClient(serviceLocator) : null;

        if (processType != ProcessType.ACC) {
            String sslClientRequired = System.getProperty(ORB_SSL_STANDALONE_CLIENT_REQUIRED);
            if ("true".equals(sslClientRequired)) {
                csiv2Props.put(ORBLocator.ORB_SSL_CLIENT_REQUIRED, "true");
            }
        }

        if (!processType.isServer()) {
            // No access to domain.xml. Just init properties.
            // In this case iiopListener beans will be null.
            orbInitialPort = OrbCreator.evaluateInitialPort(null, List.of());
            orbConfig = null;
            iiopService = null;
            iiopListeners = List.of();
            return;
        }
        iiopService = iiopUtils.getIiopService();
        iiopListeners = iiopService.getIiopListener();

        // checkORBInitialPort looks at iiopListenerBeans, if present
        orbInitialPort = OrbCreator.evaluateInitialPort(null, iiopListeners);
        orbConfig = iiopService.getOrb();

        // Initialize IOR security config for non-EJB CORBA objects
        //iiopServiceBean.isClientAuthenticationRequired()));
        csiv2Props.put(ORBLocator.ORB_CLIENT_AUTH_REQUIRED,
            String.valueOf(iiopService.getClientAuthenticationRequired()));

        // If there is at least one non-SSL listener, then it means
        // SSL is not required for CORBA objects.
        boolean corbaSSLRequired = true;
        for (IiopListener bean : iiopListeners) {
            if (bean.getSsl() == null) {
                corbaSSLRequired = false;
                break;
            }
        }

        csiv2Props.put(ORBLocator.ORB_SSL_SERVER_REQUIRED, String.valueOf(corbaSSLRequired));
    }

    /**
     * Returns whether an adapterName (from ServerRequestInfo.adapter_name)
     * represents an EJB or not.
     * @param adapterName The adapter name
     * @return whether this adapter is an EJB or not
     */
    public boolean isEjbAdapterName(String[] adapterName) {
        if (rfm == null) {
            return false;
        }
        return rfm.isRfmName(adapterName);
    }

    Properties getCSIv2Props() {
        // Return a copy of the CSIv2Props
        return new Properties(csiv2Props);
    }

    void setCSIv2Prop(String name, String value) {
        csiv2Props.setProperty(name, value);
    }

    int getORBInitialPort() {
        return orbInitialPort;
    }

    String getIIOPEndpoints() {
        return gmsClient.getIIOPEndpoints() ;
    }

    ORB createOrb(Properties props) {
        final HK2Module orbOsgiModule = resolveCorbaOrbOsgiModule();
        final GroupInfoService clusterGroupInfo;
        if (processType.isServer() && gmsClient.isGMSAvailable()) {
            clusterGroupInfo = gmsClient.getGroupInfoService();
        } else {
            clusterGroupInfo = null;
        }
        final OrbCreator creator = new OrbCreator(orbConfig, processType, clusterGroupInfo, iiopListeners, orbOsgiModule);
        com.sun.corba.ee.spi.orb.ORB orb = creator.createOrb(props);
        try {
            this.rfm = (ReferenceFactoryManager) orb.resolve_initial_references(REFERENCE_FACTORY_MANAGER);
        } catch (InvalidName e) {
            throw new IllegalStateException("ReferenceFactoryManager not found in ORB", e);
        }
        return orb;
    }

    private HK2Module resolveCorbaOrbOsgiModule() throws ResolveError {
        if (!processType.isServer()) {
            return null;
        }
        final ClassLoader originalClassLoader = Utility.getClassLoader();
        try {
            Utility.setContextClassLoader(OrbCreator.class.getClassLoader());
            ModulesRegistry modulesRegistry = services.getService(ModulesRegistry.class);
            for (HK2Module module : modulesRegistry.getModules()) {
                if ("glassfish-corba-orb".equals(module.getName())) {
                    return module;
                }
            }
            return null;
        } finally {
            Utility.setContextClassLoader(originalClassLoader);
        }
    }
}
