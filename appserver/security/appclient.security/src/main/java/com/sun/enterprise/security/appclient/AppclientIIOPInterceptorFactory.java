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

package com.sun.enterprise.security.appclient;

import com.sun.enterprise.iiop.security.AlternateSecurityInterceptorFactory;
import com.sun.enterprise.iiop.security.SecClientRequestInterceptor;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import static java.lang.System.Logger.Level.ERROR;

import java.lang.System.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.jvnet.hk2.annotations.Service;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import static com.sun.enterprise.iiop.security.AlternateSecurityInterceptorFactory.SEC_INTEROP_INTFACTORY_PROP;

/**
 * @author Kumar
 */
@Service(name = "ClientSecurityInterceptorFactory")
@Singleton
public class AppclientIIOPInterceptorFactory implements IIOPInterceptorFactory {

    private static final Logger LOG = System.getLogger(AppclientIIOPInterceptorFactory.class.getName());
    private static final String FACTORY = System.getProperty(SEC_INTEROP_INTFACTORY_PROP);

    private ClientRequestInterceptor clientRequestInterceptor;

    @Inject
    private ProcessEnvironment processEnvironment;

    private AlternateSecurityInterceptorFactory altSecFactory;

    // Are we supposed to add the interceptor and then return or just return an instance?
    @Override
    public ClientRequestInterceptor createClientRequestInterceptor(ORBInitInfo info, Codec codec) {
        if (processEnvironment.getProcessType().isServer()) {
            return null;
        }

        if (altSecFactory != null || (FACTORY != null && createAlternateSecurityInterceptorFactory())) {
            return altSecFactory.getClientRequestInterceptor(codec);
        }

        return getClientInterceptorInstance(codec);
    }

    @Override
    public ServerRequestInterceptor createServerRequestInterceptor(ORBInitInfo info, Codec codec) {
        return null;
    }

    private synchronized boolean createAlternateSecurityInterceptorFactory() {
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(FACTORY);
            if (AlternateSecurityInterceptorFactory.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                altSecFactory = (AlternateSecurityInterceptorFactory) clazz.getDeclaredConstructor().newInstance();
                return true;
            }

            LOG.log(ERROR, "Not a valid factory class: {0}. Must implement {1}", FACTORY, AlternateSecurityInterceptorFactory.class);
        } catch (ReflectiveOperationException ex) {
            LOG.log(ERROR, "Interceptor Factory class " + FACTORY + " not loaded: ", ex);
        }

        return false;
    }

    private synchronized ClientRequestInterceptor getClientInterceptorInstance(Codec codec) {
        if (clientRequestInterceptor == null) {
            clientRequestInterceptor = new SecClientRequestInterceptor("SecClientRequestInterceptor", codec);
        }

        return clientRequestInterceptor;
    }

}
