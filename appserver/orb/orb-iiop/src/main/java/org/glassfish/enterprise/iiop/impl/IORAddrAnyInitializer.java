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

package org.glassfish.enterprise.iiop.impl;

import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * This class is used to add IOR interceptors for supporting IN_ADDR_ANY
 * functionality in the ORB
 */
public class IORAddrAnyInitializer extends org.omg.CORBA.LocalObject implements ORBInitializer {

    private static final Logger _logger = LogDomains.getLogger(IORAddrAnyInitializer.class, LogDomains.CORBA_LOGGER);

    public static final String baseMsg = IORAddrAnyInitializer.class.getName();

    /** Creates a new instance of IORAddrAnyInitializer */
    public IORAddrAnyInitializer() {
    }

    /**
     * Called during ORB initialization.  If it is expected that initial
     * services registered by an interceptor will be used by other
     * interceptors, then those initial services shall be registered at
     * this point via calls to
     * <code>ORBInitInfo.register_initial_reference</code>.
     *
     * @param info provides initialization attributes and operations by
     *    which Interceptors can be registered.
     */
    @Override
    public void pre_init(org.omg.PortableInterceptor.ORBInitInfo info) {
    }

    /**
     * Called during ORB initialization. If a service must resolve initial
     * references as part of its initialization, it can assume that all
     * initial references will be available at this point.
     * <p>
     * Calling the <code>post_init</code> operations is not the final
     * task of ORB initialization. The final task, following the
     * <code>post_init</code> calls, is attaching the lists of registered
     * interceptors to the ORB. Therefore, the ORB does not contain the
     * interceptors during calls to <code>post_init</code>. If an
     * ORB-mediated call is made from within <code>post_init</code>, no
     * request interceptors will be invoked on that call.
     * Likewise, if an operation is performed which causes an IOR to be
     * created, no IOR interceptors will be invoked.
     *
     * @param info provides initialization attributes and
     *    operations by which Interceptors can be registered.
     */
    @Override
    public void post_init(org.omg.PortableInterceptor.ORBInitInfo info) {
        Codec codec = null;
        CodecFactory cf = info.codec_factory();

        byte major_version = 1;
        byte minor_version = 2;
        Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value,
                                         major_version, minor_version);
        try {
            codec = cf.create_codec(encoding);
        } catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding e) {
            _logger.log(Level.WARNING,"UnknownEncoding from " + baseMsg,e);
        }
        try {
            info.add_ior_interceptor(new IORAddrAnyInterceptor(codec));
        } catch (DuplicateName ex) {
            _logger.log(Level.WARNING,"DuplicateName from " + baseMsg,ex);
        }
    }

}
