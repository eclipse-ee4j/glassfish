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

package com.sun.ejb.base.io;

import com.sun.enterprise.container.common.spi.util.GlassFishInputStreamHandler;
import com.sun.enterprise.container.common.spi.util.SerializableObjectFactory;
import com.sun.logging.LogDomains;

import java.io.IOException;
import java.rmi.Remote;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.enterprise.iiop.api.ProtocolManager;
import org.glassfish.internal.api.Globals;

/**
 * A class that is used to restore SFSB conversational state
 *
 * @author Mahesh Kannan
 */
public class EJBObjectInputStreamHandler
    implements GlassFishInputStreamHandler
{
    private static final Logger _ejbLogger =
       LogDomains.getLogger(EJBObjectInputStreamHandler.class, LogDomains.EJB_LOGGER);

    @Override
    public Object resolveObject(Object obj)
        throws IOException
    {

    // Until we've identified a remote object, we can't assume the orb is
    // available in the container.  If the orb is not present, this will be null.
        ProtocolManager protocolMgr = getProtocolManager();

        try {
            if ( (protocolMgr != null) && protocolMgr.isStub(obj) ) {
                protocolMgr.connectObject((Remote)obj);
                return obj;
            } else if (obj instanceof SerializableObjectFactory) {
                return ((SerializableObjectFactory) obj).createObject();
            } else {
                return obj;
            }
        } catch (IOException ioEx ) {
            _ejbLogger.log(Level.SEVERE, "ejb.resolve_object_exception", ioEx);
            throw ioEx;
        } catch (Exception ex) {
            _ejbLogger.log(Level.SEVERE, "ejb.resolve_object_exception", ex);
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    /**
     * Do all ProtocolManager access lazily and only request orb if it has already been
     * initialized so that code doesn't make the assumption that an orb is available in
     * this runtime.
     */
    private ProtocolManager getProtocolManager() {
    GlassFishORBHelper orbHelper = Globals.getDefaultHabitat().getService(GlassFishORBHelper.class);
    return orbHelper.isORBInitialized() ? orbHelper.getProtocolManager() : null;
    }

}
