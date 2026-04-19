/*
 * Copyright (c) 2025, 2026 Contributors to the Eclipse Foundation.
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

import java.lang.System.Logger;
import java.util.List;

import org.glassfish.enterprise.iiop.api.IIOPInterceptorFactory;
import org.glassfish.enterprise.iiop.util.IIOPUtils;
import org.glassfish.internal.api.Globals;
import org.omg.IOP.Codec;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * This file implements an initializer class for all portable interceptors
 * used in the J2EE RI (currently security and transactions).
 * It registers the IOR, client and server request interceptors.
 *
 * @author Vivek Nagar
 * @author Mahesh Kannan
 */
public class GlassFishORBInitializer extends org.omg.CORBA.LocalObject implements ORBInitializer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = System.getLogger(GlassFishORBInitializer.class.getName());

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
        LOG.log(DEBUG, "post_init(info={0})", info);
        LOG.log(DEBUG, "Creating Codec for CDR encoding");
        byte major_version = 1;
        byte minor_version = 2;
        Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, major_version, minor_version);
        try {
            Codec codec = info.codec_factory().create_codec(encoding);
            IIOPUtils iiopUtils = Globals.get(IIOPUtils.class);
            List<IIOPInterceptorFactory> interceptorFactories = iiopUtils.getAllIIOPInterceptrFactories();
            for (IIOPInterceptorFactory factory : interceptorFactories) {
                LOG.log(DEBUG, "Processing interceptor factory: {0}", factory);
                ClientRequestInterceptor clientReq = factory.createClientRequestInterceptor(info, codec);
                if (clientReq != null) {
                    LOG.log(DEBUG, "Registering client interceptor: {0}", clientReq);
                    info.add_client_request_interceptor(clientReq);
                }
                ServerRequestInterceptor serverReq = factory.createServerRequestInterceptor(info, codec);
                if (serverReq != null) {
                    LOG.log(DEBUG, "Registering server interceptor: {0}", serverReq);
                    info.add_server_request_interceptor(serverReq);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception registering interceptors", e);
        }
    }
}
