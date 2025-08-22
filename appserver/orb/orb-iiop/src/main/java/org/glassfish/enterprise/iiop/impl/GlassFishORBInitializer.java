/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import com.sun.logging.LogDomains;

import java.util.Collection;
import java.util.logging.Logger;

import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.glassfish.enterprise.iiop.util.IIOPUtils;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import static java.util.logging.Level.FINE;

/**
 * This file implements an initializer class for all portable interceptors
 * used in the J2EE RI (currently security and transactions).
 * It registers the IOR, client and server request interceptors.
 *
 * @author Vivek Nagar
 * @author Mahesh Kannan
 *
 */
public class GlassFishORBInitializer extends org.omg.CORBA.LocalObject implements ORBInitializer {

    private static final Logger LOG = LogDomains.getLogger(GlassFishORBInitializer.class, LogDomains.CORBA_LOGGER);

    /**
     * This method is called during ORB initialization.
     *
     * @param info object that provides initialization attributes and operations by which
     *            interceptors are registered.
     */
    @Override
    public void pre_init(ORBInitInfo info) {
    }


    /**
     * This method is called during ORB initialization.
     *
     * @param info object that provides initialization attributes and operations by which
     *            interceptors are registered.
     */
    @Override
    public void post_init(ORBInitInfo info) {
        LOG.log(FINE, "J2EE Initializer post_init");
        LOG.log(FINE, "Creating Codec for CDR encoding");

        CodecFactory cf = info.codec_factory();

        byte major_version = 1;
        byte minor_version = 2;
        Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value,
                major_version, minor_version);
        try {
            Codec codec = cf.create_codec(encoding);
            IIOPUtils iiopUtils = IIOPUtils.getInstance();
            Collection<IIOPInterceptorFactory> interceptorFactories = iiopUtils.getAllIIOPInterceptrFactories();

            for (IIOPInterceptorFactory factory : interceptorFactories) {
                LOG.log(FINE, "Processing interceptor factory: {0}", factory);

                ClientRequestInterceptor clientReq = factory.createClientRequestInterceptor(info, codec);
                ServerRequestInterceptor serverReq = factory.createServerRequestInterceptor(info, codec);

                if (clientReq != null) {
                    LOG.log(FINE, "Registering client interceptor: {0}", clientReq);
                    info.add_client_request_interceptor(clientReq);
                }
                if (serverReq != null) {
                    LOG.log(FINE, "Registering server interceptor: {0}", serverReq);
                    info.add_server_request_interceptor(serverReq);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception registering interceptors", e);
        }
    }
}
