/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.jvnet.hk2.annotations.Contract;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;

import com.sun.enterprise.container.common.spi.util.GlassFishOutputStreamHandler;
import com.sun.enterprise.container.common.spi.util.GlassFishInputStreamHandler;
import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.logging.LogDomains;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A contract that defines a set of methods to serialize / deserialze Java EE
 * objects (even if they are not directly serializable).
 *
 * Some of the objects that are expected to be serialized / de-serialized are a)
 * Local EJB references b) EJB Handles c) JNDI (sub) contexts d) (Non
 * serializable) StatefulSessionBeans
 *
 * @author Mahesh Kannan
 *
 */
@Service
public class JavaEEIOUtilsImpl implements JavaEEIOUtils {

    private static Logger _logger = LogDomains.getLogger(
            JavaEEIOUtilsImpl.class, LogDomains.JNDI_LOGGER);

    @Inject
    ServiceLocator habitat;

    private Collection<GlassFishOutputStreamHandler> outputHandlers = new HashSet<GlassFishOutputStreamHandler>();

    private Collection<GlassFishInputStreamHandler> inputHandlers = new HashSet<GlassFishInputStreamHandler>();

    public ObjectInputStream createObjectInputStream(InputStream is,
            boolean resolveObject, ClassLoader loader) throws Exception {
        return new GlassFishObjectInputStream(inputHandlers, is, loader, resolveObject);
    }

    public ObjectOutputStream createObjectOutputStream(OutputStream os,
            boolean replaceObject) throws IOException {
        return new GlassFishObjectOutputStream(outputHandlers, os, replaceObject);
    }

    public byte[] serializeObject(Object obj, boolean replaceObject)
            throws java.io.IOException {

        byte[] data = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = createObjectOutputStream(bos, replaceObject);

            oos.writeObject(obj);
            oos.flush();
            data = bos.toByteArray();
        } catch (java.io.NotSerializableException notSerEx) {
            throw notSerEx;
        } catch (Exception th) {
            IOException ioEx = new IOException(th.toString());
            ioEx.initCause(th);
            throw ioEx;
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (Exception ex) {
                }
            }
            try {
                bos.close();
            } catch (Exception ex) {
            }
        }

        return data;
    }

    public Object deserializeObject(byte[] data, boolean resolveObject,
            ClassLoader appClassLoader) throws Exception {

        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(data);
            ois = createObjectInputStream(bis, resolveObject, appClassLoader);
            obj = ois.readObject();
        } catch (Exception ex) {
            _logger.log(Level.FINE, "Error during deserialization", ex);
            throw ex;
        } finally {
            try {
                ois.close();
            } catch (Exception ex) {
                _logger.log(Level.FINEST, "Error during ois.close()", ex);
            }
            try {
                bis.close();
            } catch (Exception ex) {
                _logger.log(Level.FINEST, "Error during bis.close()", ex);
            }
        }
        return obj;
    }

    public void addGlassFishOutputStreamHandler(GlassFishOutputStreamHandler handler) {
        outputHandlers.add(handler);

    }

    public void removeGlassFishOutputStreamHandler(GlassFishOutputStreamHandler handler) {
        outputHandlers.remove(handler);
    }

    public void addGlassFishInputStreamHandler(
            GlassFishInputStreamHandler handler) {
        inputHandlers.add(handler);
    }

    public void removeGlassFishInputStreamHandler(
            GlassFishInputStreamHandler handler) {
        inputHandlers.remove(handler);
    }

}
