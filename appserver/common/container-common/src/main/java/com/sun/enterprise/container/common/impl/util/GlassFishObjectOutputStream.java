/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.container.common.spi.util.GlassFishOutputStreamHandler;
import com.sun.enterprise.container.common.spi.util.IndirectlySerializable;
import com.sun.enterprise.container.common.spi.util.SerializableObjectFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.common.util.ObjectInputOutputStreamFactory;
import org.glassfish.common.util.ObjectInputOutputStreamFactoryFactory;
import org.glassfish.internal.api.Globals;

/**
 * A class that is used to save conversational state
 *
 * @author Mahesh Kannan
 */
class GlassFishObjectOutputStream extends ObjectOutputStream {

    private final ObjectInputOutputStreamFactory outputStreamHelper;
    private final Collection<GlassFishOutputStreamHandler> handlers;

    GlassFishObjectOutputStream(Collection<GlassFishOutputStreamHandler> handlers, OutputStream out,
        boolean replaceObject) throws IOException {
        super(out);
        this.handlers = handlers;

        if (replaceObject) {
            enableReplaceObject(replaceObject);
        }
        outputStreamHelper = ObjectInputOutputStreamFactoryFactory.getFactory();
    }

    /**
     * This code is needed to serialize non-Serializable objects that can be
     * part of a bean's state. See EJB2.0 section 7.4.1.
     */
    @Override
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
    private static final long serialVersionUID = 1L;
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
            throw new IOException("Unable to get a name in the namespace", ex);
        }
    }

    @Override
    public Object createObject() throws IOException {
        try {
            if (name == null || name.isEmpty()) {
                return new InitialContext();
            }
            return Globals.getDefaultHabitat()
                    .<GlassfishNamingManager>getService(GlassfishNamingManager.class)
                    .restoreJavaCompEnvContext(new SimpleJndiName(name));
        } catch (NamingException namEx) {
            throw new IOException("Unable to create a context named " + name, namEx);
        }
    }

}
