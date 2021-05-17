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

package com.sun.enterprise.container.common.impl.util;

import com.sun.logging.LogDomains;

import com.sun.enterprise.util.Utility;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.internal.api.Globals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.enterprise.container.common.spi.util.GlassFishOutputStreamHandler;
import com.sun.enterprise.container.common.spi.util.IndirectlySerializable;
import com.sun.enterprise.container.common.spi.util.SerializableObjectFactory;

import org.glassfish.common.util.ObjectInputOutputStreamFactory;
import org.glassfish.common.util.ObjectInputOutputStreamFactoryFactory;

/**
 * A class that is used to save conversational state
 *
 * @author Mahesh Kannan
 */
class GlassFishObjectOutputStream extends java.io.ObjectOutputStream {
    private static Logger _logger = LogDomains.getLogger(
            GlassFishObjectOutputStream.class, LogDomains.JNDI_LOGGER);

    static final int EJBID_OFFSET = 0;
    static final int INSTANCEKEYLEN_OFFSET = 8;
    static final int INSTANCEKEY_OFFSET = 12;

    private static final byte HOME_KEY = (byte) 0xff;

    private ObjectInputOutputStreamFactory outputStreamHelper;

    private Collection<GlassFishOutputStreamHandler> handlers;

    GlassFishObjectOutputStream(Collection<GlassFishOutputStreamHandler> handlers, OutputStream out, boolean replaceObject)
            throws IOException {
        super(out);
        this.handlers = handlers;

        if (replaceObject == true) {
            enableReplaceObject(replaceObject);
        }

        outputStreamHelper = ObjectInputOutputStreamFactoryFactory.getFactory();

    }

    /**
     * This code is needed to serialize non-Serializable objects that can be
     * part of a bean's state. See EJB2.0 section 7.4.1.
     */
    protected Object replaceObject(Object obj) throws IOException {
        Object result = obj;

        if (obj instanceof IndirectlySerializable) {
            result = ((IndirectlySerializable) obj).getSerializableObjectFactory();
        } else if (obj instanceof Context) {
            result = new SerializableJNDIContext((Context) obj);
        } else {
            for (GlassFishOutputStreamHandler handler : handlers) {
                Object r = handler.replaceObject(obj);
                if (r != null) {
                    result = r;
                    break;
                }
            }
        }

        return result;
    }

    @Override
    protected void annotateClass(Class<?> cl) throws IOException {
        outputStreamHelper.annotateClass(this, cl);
    }

}

final class SerializableJNDIContext implements SerializableObjectFactory {
    private String name;

    SerializableJNDIContext(Context ctx) throws IOException {
        try {
            // Serialize state for a jndi context. The spec only requires
            // support for serializing contexts pointing to java:comp/env
            // or one of its subcontexts. We also support serializing the
            // references to the the default no-arg InitialContext, as well
            // as references to the the contexts java: and java:comp. All
            // other contexts will either not serialize correctly or will
            // throw an exception during deserialization.
            this.name = ctx.getNameInNamespace();
        } catch (NamingException ex) {
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    public Object createObject() throws IOException {
        try {
            if ((name == null) || (name.length() == 0)) {
                return new InitialContext();
            } else {
                return Globals.getDefaultHabitat()
                        .<GlassfishNamingManager>getService(GlassfishNamingManager.class)
                        .restoreJavaCompEnvContext(name);
            }
        } catch (NamingException namEx) {
            IOException ioe = new IOException();
            ioe.initCause(namEx);
            throw ioe;
        }
    }

}
