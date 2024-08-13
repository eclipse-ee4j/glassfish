/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.NamingClusterInfo;
import org.glassfish.api.naming.NamingObjectsProvider;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ORBLocator;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;

import static org.glassfish.enterprise.iiop.impl.IIOPImplLogFacade.FAILED_TO_RESOLVE_GROUPINFOSERVICE;
import static org.glassfish.enterprise.iiop.impl.IIOPImplLogFacade.NO_ENDPOINT_SELECTED;

/**
 * This class is responsible for setting up naming load-balancing including RoundRobinPolicy.
 */
@Service
@Singleton
public class NamingClusterInfoImpl implements NamingClusterInfo {

    private static final Logger LOG = IIOPImplLogFacade.getLogger(NamingClusterInfoImpl.class);

    private RoundRobinPolicy rrPolicy;

    private GroupInfoServiceObserverImpl giso;

    @Override
    public void initGroupInfoService(Hashtable<?, ?> myEnv, String defaultHost, String defaultPort, ORB orb,
        ServiceLocator services) {
        // Always create one rrPolicy to be shared, if needed.
        final List<String> epList = getEndpointList(myEnv, defaultHost, defaultPort);
        rrPolicy = new RoundRobinPolicy(epList);

        final GroupInfoService gis;
        try {
            gis = (GroupInfoService) (orb.resolve_initial_references(ORBLocator.FOLB_CLIENT_GROUP_INFO_SERVICE));
        } catch (InvalidName ex) {
            LOG.log(Level.SEVERE, FAILED_TO_RESOLVE_GROUPINFOSERVICE, ORBLocator.FOLB_CLIENT_GROUP_INFO_SERVICE);
            throw new IllegalStateException("Could not resolve the group info service.", ex);
        }

        giso = new GroupInfoServiceObserverImpl(gis, rrPolicy);

        gis.addObserver(giso);

        // this should force the initialization of the resources providers
        if (services !=null) {
            for (ServiceHandle<?> provider : services.getAllServiceHandles(NamingObjectsProvider.class)) {
                provider.getService();
                // no - op. Do nothing with the provided object
            }
        }

        // Get the actual content, not just the configured endpoints.
        giso.forceMembershipChange();
        LOG.log(Level.FINE, "NamingClusterInfoImpl.initGroupInfoService RoundRobinPolicy {0}", rrPolicy);
    }


    @Override
    public void setClusterInstanceInfo(Hashtable<?, ?> myEnv, String defaultHost, String defaultPort,
        boolean membershipChangeForced) {
        final List<String> list = getEndpointList(myEnv, defaultHost, defaultPort) ;
        rrPolicy.setClusterInstanceInfoFromString(list);
        if (!membershipChangeForced) {
            giso.forceMembershipChange() ;
        }
    }

    @Override
    public List<String> getNextRotation() {
        return rrPolicy.getNextRotation();
    }

    private List<String> getEndpointList(Hashtable env, String defaultHost, String defaultPort) {
        final List<String> list = new ArrayList<>() ;
        final String lbpv = getEnvSysProperty( env, LOAD_BALANCING_PROPERTY);
        final List<String> lbList = splitOnComma(lbpv) ;
        if (!lbList.isEmpty()) {
            final String first = lbList.remove(0);
            if (first.equals(IC_BASED) || first.equals(IC_BASED_WEIGHTED)) {
                // XXX concurrency issue here:  possible race on global
                System.setProperty(LOAD_BALANCING_PROPERTY, first );
            }
        }
        list.addAll(lbList);

        if (list.isEmpty()) {
            final String iepv = getEnvSysProperty( env, IIOP_ENDPOINTS_PROPERTY);
            final List<String> epList = splitOnComma(iepv) ;
            list.addAll(epList);
        }

        if (list.isEmpty()) {
            final String urlValue = (String) env.get(ORBLocator.JNDI_PROVIDER_URL_PROPERTY);
            list.addAll(rrPolicy.getEndpointForProviderURL(urlValue));
        }

        if (list.isEmpty()) {
            String host = getEnvSysProperty(env, ORBLocator.OMG_ORB_INIT_HOST_PROPERTY);
            String port = getEnvSysProperty(env, ORBLocator.OMG_ORB_INIT_PORT_PROPERTY);
            if (host != null && port != null) {
                list.addAll(rrPolicy.getAddressPortList(host, port) ) ;
                LOG.log(Level.WARNING, NO_ENDPOINT_SELECTED, new Object[]{host, port});
            }
        }

        if (list.isEmpty()) {
            if (defaultHost != null && defaultPort != null) {
                list.add(defaultHost + ":" + defaultPort);
            }
        }

        if (list.isEmpty()) {
            throw new RuntimeException("Cannot Proceed. No Endpoints specified.");
        }

        return list;
    }

    private List<String> splitOnComma( String arg ) {
        final List<String> result = new ArrayList<>() ;
        if (arg != null) {
            final String[] splits = arg.split(",");
            if (splits != null) {
                for (String str : splits) {
                    result.add(str.trim());
                }
            }
        }

        return result;
    }

    private String getEnvSysProperty(Hashtable env, String pname) {
        String value = (String) env.get(pname);
        if (value == null) {
            value = System.getProperty(pname);
        }
        return value ;
    }

}
