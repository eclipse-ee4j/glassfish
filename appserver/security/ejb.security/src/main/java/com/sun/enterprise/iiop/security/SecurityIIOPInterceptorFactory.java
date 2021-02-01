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

package com.sun.enterprise.iiop.security;

import com.sun.logging.LogDomains;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;
import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import jakarta.inject.Inject;

/**
 *
 * @author Kumar
 */
@Service(name = "ServerSecurityInterceptorFactory")
@Singleton
public class SecurityIIOPInterceptorFactory implements IIOPInterceptorFactory {

    private static Logger _logger = null;
    final String interceptorFactory = System.getProperty(AlternateSecurityInterceptorFactory.SEC_INTEROP_INTFACTORY_PROP);

    static {
        _logger = LogDomains.getLogger(SecurityIIOPInterceptorFactory.class, LogDomains.SECURITY_LOGGER);
    }
    private ClientRequestInterceptor creq;
    private ServerRequestInterceptor sreq;
    private SecIORInterceptor sior;

    @Inject
    private ProcessEnvironment penv;

    private AlternateSecurityInterceptorFactory altSecFactory;

    // are we supposed to add the interceptor and then return or just return an instance ?.
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
            _logger.log(Level.SEVERE, null, ex);
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
            } else {
                _logger.log(Level.INFO,
                        "Not a valid factory class: " + interceptorFactory + ". Must implement " + AlternateSecurityInterceptorFactory.class.getName());
            }
        } catch (ClassNotFoundException ex) {
            _logger.log(Level.INFO, "Interceptor Factory class " + interceptorFactory + " not loaded: ", ex);
        } catch (InstantiationException ex) {
            _logger.log(Level.INFO, "Interceptor Factory class " + interceptorFactory + " not loaded: ", ex);
        } catch (IllegalAccessException ex) {
            _logger.log(Level.INFO, "Interceptor Factory class " + interceptorFactory + " not loaded: ", ex);
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
