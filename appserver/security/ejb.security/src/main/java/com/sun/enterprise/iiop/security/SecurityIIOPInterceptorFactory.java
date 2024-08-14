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

package com.sun.enterprise.iiop.security;

import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import static com.sun.logging.LogDomains.SECURITY_LOGGER;

/**
 * @author Kumar
 */
@Service(name = "ServerSecurityInterceptorFactory")
@Singleton
public class SecurityIIOPInterceptorFactory implements IIOPInterceptorFactory {

    private static final Logger LOG = LogDomains.getLogger(SecurityIIOPInterceptorFactory.class, SECURITY_LOGGER, false);
    final String interceptorFactory = System.getProperty(AlternateSecurityInterceptorFactory.SEC_INTEROP_INTFACTORY_PROP);
    private ClientRequestInterceptor creq;
    private ServerRequestInterceptor sreq;
    private SecIORInterceptor sior;

    @Inject
    private ProcessEnvironment penv;

    private AlternateSecurityInterceptorFactory altSecFactory;

    // are we supposed to add the interceptor and then return or just return an instance ?.
    @Override
    public ClientRequestInterceptor createClientRequestInterceptor(ORBInitInfo info, Codec codec) {
        if (!penv.getProcessType().isServer()) {
            return null;
        }
        if (altSecFactory != null || (interceptorFactory != null && createAlternateSecurityInterceptorFactory())) {
            return altSecFactory.getClientRequestInterceptor(codec);
        }
        ClientRequestInterceptor ret = getClientInterceptorInstance(codec);
        return ret;
    }

    @Override
    public ServerRequestInterceptor createServerRequestInterceptor(ORBInitInfo info, Codec codec) {
        ServerRequestInterceptor ret = null;
        try {
            if (!penv.getProcessType().isServer()) {
                return null;
            }
            if (altSecFactory != null || (interceptorFactory != null && createAlternateSecurityInterceptorFactory())) {
                ret = altSecFactory.getServerRequestInterceptor(codec);
            } else {
                ret = getServerInterceptorInstance(codec);
            }
            // also register the IOR Interceptor here
            if (info instanceof com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt) {
                com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt infoExt = (com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt) info;
                IORInterceptor secIOR = getSecIORInterceptorInstance(codec, infoExt.getORB());
                info.add_ior_interceptor(secIOR);
            }

        } catch (DuplicateName ex) {
            throw new RuntimeException(ex);
        }
        return ret;
    }

    private synchronized boolean createAlternateSecurityInterceptorFactory() {
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(interceptorFactory);
            if (AlternateSecurityInterceptorFactory.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                altSecFactory = (AlternateSecurityInterceptorFactory) clazz.newInstance();
                return true;
            }
            LOG.log(Level.INFO, "Not a valid factory class: {0}. Must implement {1}",
                new Object[] {interceptorFactory, AlternateSecurityInterceptorFactory.class});
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.INFO, "Interceptor Factory class " + interceptorFactory + " not loaded: ", ex);
        } catch (InstantiationException ex) {
            LOG.log(Level.INFO, "Interceptor Factory class " + interceptorFactory + " not loaded: ", ex);
        } catch (IllegalAccessException ex) {
            LOG.log(Level.INFO, "Interceptor Factory class " + interceptorFactory + " not loaded: ", ex);
        }
        return false;
    }

    private synchronized ClientRequestInterceptor getClientInterceptorInstance(Codec codec) {
        if (creq == null) {
            creq = new SecClientRequestInterceptor("SecClientRequestInterceptor", codec);
        }
        return creq;
    }

    private synchronized ServerRequestInterceptor getServerInterceptorInstance(Codec codec) {
        if (sreq == null) {
            sreq = new SecServerRequestInterceptor("SecServerRequestInterceptor", codec);
        }
        return sreq;
    }

    private synchronized IORInterceptor getSecIORInterceptorInstance(Codec codec, ORB orb) {
        if (sior == null) {
            sior = new SecIORInterceptor(codec, orb);
        }
        return sior;
    }
}
