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

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.logging.LogDomains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.internal.api.Globals;

/**
 * This implements IORToSocketInfo for ORB. Part of logic is from previous version of IIOPSSLSocketFactory.
 *
 * @author Shing Wai Chan
 */
public class IORToSocketInfoImpl implements IORToSocketInfo {
    private static final Logger LOG = LogDomains.getLogger(IORToSocketInfoImpl.class, LogDomains.CORBA_LOGGER, false);

    // Maps primary address to list of alternate addresses.
    // Used to compare new IOR/primaries with ones already seen for
    // that primary. We expect them to be equal.
    private final Map primaryToAddresses = new HashMap();

    // Maps primary to randomized list of alternate addresses.
    private final Map primaryToRandomizedAddresses = new HashMap();

    // ----- implements com.sun.corba.ee.spi.transport.IORToSocketInfo -----
    private final SecurityMechanismSelector selector;

    public IORToSocketInfoImpl() {
        selector = Globals.get(SecurityMechanismSelector.class);
    }

    @Override
    public List getSocketInfo(IOR ior, List previous) {
        try {
            LOG.entering(getClass().getName(), "getSocketInfo");
            List result = new ArrayList();

            IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate) ior.getProfile().getTaggedProfileTemplate();
            IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress();
            Locale loc = Locale.getDefault();
            String host = primary.getHost().toLowerCase(loc);

            String type = null;
            int port = 0;
            ConnectionContext ctx = new ConnectionContext();
            SocketInfo socketInfo = selector.getSSLPort(ior, ctx);
            selector.setClientConnectionContext(ctx);
            if (socketInfo == null) {
                type = SocketInfo.IIOP_CLEAR_TEXT;
                port = primary.getPort();
                LOG.log(Level.FINE, "Did not find SSL SocketInfo");
            } else {
                type = socketInfo.getType();
                port = socketInfo.getPort();
                LOG.log(Level.FINE, "Found SSL socketInfo");
            }

            LOG.log(Level.FINE, "Connection Context: {0}", ctx);
            LOG.log(Level.FINE, "Endpoint: type={0}, host={1}, port={2}", new Object[] {type, host, port});

            // for SSL
            if (socketInfo != null) {
                result.add(socketInfo);
                LOG.log(Level.FINE, "Returning SSL socketInfo: type={0}, host={1}, port={2}.",
                    new Object[] {socketInfo.getType(), socketInfo.getHost(), socketInfo.getPort()});
                // REVISIT: should call IIOPPrimaryToContactInfo.reset
                // right here to invalidate sticky for this result.
                // However, SSL and IIOP-FO is not a supported feature.
                return result;
            }

            ////////////////////////////////////////////////////
            //
            // The remainder of this code is non-SSL.
            // Author: Harold Carr
            // Please contact author is changes needed.
            //

            // for non-SSL
            LOG.log(Level.FINE, "Returning non SSL socketInfo");

            if (!previous.isEmpty()) {
                LOG.log(Level.FINE, "Returning previous socketInfo: {0}", previous);
                return previous;
            }

            //
            // Save and add primary address
            //

            SocketInfo primarySocketInfo = createSocketInfo("primary", type, host, port);
            result.add(primarySocketInfo);

            //
            // List alternate addresses.
            //

            Iterator iterator = iiopProfileTemplate.iteratorById(org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value);

            while (iterator.hasNext()) {
                AlternateIIOPAddressComponent alternate = (AlternateIIOPAddressComponent) iterator.next();

                host = alternate.getAddress().getHost().toLowerCase(loc);
                port = alternate.getAddress().getPort();

                result.add(createSocketInfo("AlternateIIOPAddressComponent", SocketInfo.IIOP_CLEAR_TEXT, host, port));
            }

            synchronized (this) {
                List existing = (List) primaryToAddresses.get(primarySocketInfo);
                if (existing == null) {
                    // First time we've seen this primary.
                    // Save unrandomized list with primary at head.
                    primaryToAddresses.put(primarySocketInfo, result);
                    result.remove(0); // do not randomize primary
                    // Randomized the alternates.
                    Collections.shuffle(result);
                    result.add(0, primarySocketInfo); // put primary at head
                    // Save for subsequent use.
                    primaryToRandomizedAddresses.put(primarySocketInfo, result);
                    LOG.log(Level.FINE, "Initial randomized result: {0}", result);
                    return result;
                }
                if (result.equals(existing)) {
                    // The are the same so return the randomized version.
                    result = (List) primaryToRandomizedAddresses.get(primarySocketInfo);
                    LOG.log(Level.FINE, "Existing randomized result: {0}", result);
                    return result;
                }
                // The lists should be the same.
                // If not log a warning and return the
                // non-randomized current list since it is different.
                LOG.log(Level.FINE,
                    "Address lists do not match: primary: {0}; returning current: {1}; existing is: {2}",
                    new Object[] {primarySocketInfo, result, existing});
                return result;
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Exception getting SocketInfo", ex);
            throw new RuntimeException(ex);
        } finally {
            LOG.exiting(getClass().getName(), "getSocketInfo");
        }
    }

    // ----- END implements com.sun.corba.ee.spi.transport.IORToSocketInfo -----

    public static SocketInfo createSocketInfo(String msg, final String type, final String host, final int port) {
        LOG.log(Level.FINE, "Address from: {0}; type/address/port: {1}/{2}/{3}", new Object[] {msg, type, host, port});

        return new SocketInfo() {
            @Override
            public String getType() {
                return type;
            }

            @Override
            public String getHost() {
                return host;
            }

            @Override
            public int getPort() {
                return port;
            }

            @Override
            public boolean equals(Object o) {
                if (o == null) {
                    return false;
                }
                if (!(o instanceof SocketInfo)) {
                    return false;
                }
                SocketInfo other = (SocketInfo) o;
                if (other.getPort() != port) {
                    return false;
                }
                if (!other.getHost().equals(host)) {
                    return false;
                }
                if (!other.getType().equals(type)) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                return type.hashCode() ^ host.hashCode() ^ port;
            }

            @Override
            public String toString() {
                return "SocketInfo[" + type + " " + host + " " + port + "]";
            }
        };
    }
}

// End of file.
