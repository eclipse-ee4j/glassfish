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

import java.util.logging.Logger;

import org.omg.CORBA.LocalObject;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.IORInterceptor;

public class IORAddrAnyInterceptor extends LocalObject implements IORInterceptor {

    public static final String baseMsg = IORAddrAnyInterceptor.class.getName();
    private static final Logger _logger = LogDomains.getLogger(IORAddrAnyInterceptor.class, LogDomains.CORBA_LOGGER);

    private final Codec codec;


    /** Creates a new instance of IORAddrAnyInterceptor
     * @param c The codec
     */
    public IORAddrAnyInterceptor(Codec c) {
        codec = c;
    }

    /**
     * Provides an opportunity to destroy this interceptor.
     * The destroy method is called during <code>ORB.destroy</code>. When an
     * application calls <code>ORB.destroy</code>, the ORB:
     * <ol>
     *  <li>waits for all requests in progress to complete</li>
     *  <li>calls the <code>Interceptor.destroy</code> operation for each
     *      interceptor</li>
     *  <li>completes destruction of the ORB</li>
     * </ol>
     * Method invocations from within <code>Interceptor.destroy</code> on
     * object references for objects implemented on the ORB being destroyed
     * result in undefined behavior. However, method invocations on objects
     * implemented on an ORB other than the one being destroyed are
     * permitted. (This means that the ORB being destroyed is still capable
     * of acting as a client, but not as a server.)
     */
    @Override
    public void destroy() {
    }

    /**
     * A server side ORB calls the <code>establish_components</code>
     * operation on all registered <code>IORInterceptor</code> instances
     * when it is assembling the list of components that will be included
     * in the profile or profiles of an object reference. This operation
     * is not necessarily called for each individual object reference.
     * For example, the POA specifies policies at POA granularity and
     * therefore, this operation might be called once per POA rather than
     * once per object. In any case, <code>establish_components</code> is
     * guaranteed to be called at least once for each distinct set of
     * server policies.
     * <p>
     * An implementation of <code>establish_components</code> must not
     * throw exceptions. If it does, the ORB shall ignore the exception
     * and proceed to call the next IOR Interceptor's
     * <code>establish_components</code> operation.
     *
     * @param iorInfo The <code>IORInfo</code> instance used by the ORB
     *    service to query applicable policies and add components to be
     *    included in the generated IORs.
     */
    @Override
    public void establish_components(org.omg.PortableInterceptor.IORInfo iorInfo) {
        /*
        try {
            IORInfoExt iorInfoExt = (IORInfoExt) iorInfo;
            int port = iorInfoExt.getServerPort(ORBSocketFactory.IIOP_CLEAR_TEXT);

            ArrayList allInetAddress = getAllInetAddresses();
            addAddressComponents(iorInfo, allInetAddress, port);
            ORB orb = (ORB)((IORInfoImpl)iorInfo).getORB();
            Object[] userPorts = orb.getUserSpecifiedListenPorts().toArray();
            if (userPorts.length > 0) {
                for (int i = 0; i < userPorts.length; i++) {
                    com.sun.corba.ee.internal.corba.ORB.UserSpecifiedListenPort p =
                        ((com.sun.corba.ee.internal.corba.ORB.UserSpecifiedListenPort)userPorts[i]);
            //        if (p.getType().equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
                        addAddressComponents(iorInfo, allInetAddress, p.getPort());
            //        }
                }
            }
        } catch (Exception e) {
            _logger.log(Level.WARNING,"Exception in " + baseMsg, e);
        }
        */
    }

    /**
     * Returns the name of the interceptor.
     * <p>
     * Each Interceptor may have a name that may be used administratively
     * to order the lists of Interceptors. Only one Interceptor of a given
     * name can be registered with the ORB for each Interceptor type. An
     * Interceptor may be anonymous, i.e., have an empty string as the name
     * attribute. Any number of anonymous Interceptors may be registered with
     * the ORB.
     *
     * @return the name of the interceptor.
     */
    @Override
    public String name() {
        return baseMsg;
    }


    protected short intToShort(int value) {
        if (value > 32767) {
            return (short) (value - 65536);
        }
        return (short) value;
    }

    /*
    private void addAddressComponents(org.omg.PortableInterceptor.IORInfo iorInfo,
                    ArrayList allInetAddress, int port) {
        try {
            for (int i = 0; i < allInetAddress.size(); i++) {
                String address = ((InetAddress)allInetAddress.get(i)).getHostAddress();
                AlternateIIOPAddressComponent iiopAddress =
                    new AlternateIIOPAddressComponent(address, intToShort(port));
                Any any = ORB.init().create_any();
                AlternateIIOPAddressComponentHelper.insert(any, iiopAddress);
                byte[] data = codec.encode_value(any);
                TaggedComponent taggedComponent =
                    new TaggedComponent( org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value,
                    //AlternateIIOPAddressComponent.TAG_ALTERNATE_IIOP_ADDRESS_ID,
                            data);
                iorInfo.add_ior_component(taggedComponent);
            }
        } catch (Exception e) {
            _logger.log(Level.WARNING,"Exception in " + baseMsg, e);
        }
    }
    */

}
