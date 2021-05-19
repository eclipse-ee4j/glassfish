/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming.impl;

import org.glassfish.api.naming.NamingClusterInfo;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ORBLocator;
import org.glassfish.hk2.api.ServiceLocator;
import org.omg.CORBA.ORB;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import static com.sun.enterprise.naming.util.LogFacade.logger;
import static org.glassfish.api.naming.NamingClusterInfo.*;

public class SerialInitContextFactory implements InitialContextFactory {
    private static volatile boolean initialized = false ;

    private static String defaultHost = null ;

    private static String defaultPort = null ;

    private static ServiceLocator defaultServices = null ;

    static void setDefaultHost(String host) {
        defaultHost = host;
    }

    static void setDefaultPort(String port) {
        defaultPort = port;
    }

    static void setDefaultServices(ServiceLocator h) {
        defaultServices = h;

    }

    static ServiceLocator getDefaultServices() {
        return defaultServices;
    }

    private final ServiceLocator services;


    private boolean propertyIsSet( Hashtable env, String pname ) {
        String value = getEnvSysProperty( env, pname ) ;
        return value != null && !value.isEmpty() ;
    }

    private String getEnvSysProperty( Hashtable env, String pname ) {
        String value = (String)env.get( pname ) ;
        if (value == null) {
            value = System.getProperty( pname ) ;
        }
        return value ;
    }

    private String getCorbalocURL( final List<String> list) {
        final StringBuilder sb = new StringBuilder() ;
        boolean first = true ;
        for (String str : list) {
        if (first) {
                first = false ;
                sb.append( CORBALOC ) ;
        } else {
                sb.append( ',' ) ;
        }

            sb.append( IIOP_URL ) ;
            sb.append( str.trim() ) ;
    }

    // fineLog( "corbaloc url ==> {0}", sb.toString() );

    return sb.toString() ;
    }

    public SerialInitContextFactory() {
        // Issue 14396
        ServiceLocator temp = defaultServices;
        if (temp == null) {
            temp = Globals.getDefaultHabitat() ;
        }
        if (temp == null) {
            // May need to initialize hk2 component model in standalone client
            temp = Globals.getStaticHabitat() ;
        }
        services = temp ;
    }

    private ORB getORB() {
        if (services != null) {
            ORBLocator orbLocator = services.getService(ORBLocator.class) ;
            if (orbLocator != null) {
                return orbLocator.getORB() ;
            }
        }

        throw new RuntimeException( "Could not get ORB" ) ;
    }

    /**
     * Create the InitialContext object.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context getInitialContext(Hashtable env) throws NamingException {
        final Hashtable myEnv = env == null ? new Hashtable() : env ;


        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "getInitialContext: env={0}", env);
        }
        boolean useLB = propertyIsSet(myEnv, IIOP_ENDPOINTS_PROPERTY)
            || propertyIsSet(myEnv, LOAD_BALANCING_PROPERTY) ;
        NamingClusterInfo namingClusterInfo = null;


        if (useLB)  {
             if (!initialized) {
                 synchronized( SerialInitContextFactory.class ) {
                     if (!initialized) {
                         namingClusterInfo = services.getService(NamingClusterInfo.class);
                         namingClusterInfo.initGroupInfoService(myEnv, defaultHost, defaultPort, getORB(), services);
                         initialized = true ;
                     }
                 }
             }
            // If myEnv already contains the IIOP_URL, don't get a new one:
            // this getInitialContext call came from an internal
            // new InitialContext call.
            if (!myEnv.containsKey(IIOP_URL_PROPERTY)) {
                Context ctx = SerialContext.getStickyContext() ;
                if (ctx != null) {
                    return ctx ;
                }

                if(namingClusterInfo == null) {
                    namingClusterInfo = services.getService(NamingClusterInfo.class);
                }

                List<String> rrList = namingClusterInfo.getNextRotation();
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "getInitialContext: RoundRobinPolicy list = {0}", rrList);
                }
                myEnv.put(IIOP_URL_PROPERTY, getCorbalocURL(rrList));
            }

            myEnv.put(ORBLocator.JNDI_CORBA_ORB_PROPERTY, getORB());
        } else {
            if (defaultHost != null) {
                myEnv.put( ORBLocator.OMG_ORB_INIT_HOST_PROPERTY, defaultHost ) ;
            }

            if (defaultPort != null) {
                myEnv.put( ORBLocator.OMG_ORB_INIT_PORT_PROPERTY, defaultPort ) ;
            }
        }

        return createInitialContext(myEnv);
    }

    private Context createInitialContext(Hashtable env) throws NamingException
    {
        SerialContext serialContext = new SerialContext(env, services);
        if (NamingManager.hasInitialContextFactoryBuilder()) {
            // When builder is used, JNDI does not go through
            // URL Context discovery anymore. To address that
            // we install a wrapper that first goes through
            // URL context discovery and then falls back to
            // serialContext.
            return new WrappedSerialContext(env, serialContext);
        } else {
            return serialContext ;
        }
    }
}
