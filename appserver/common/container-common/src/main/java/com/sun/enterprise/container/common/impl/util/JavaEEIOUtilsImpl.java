/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.container.common.spi.util.GlassFishInputStreamHandler;
import com.sun.enterprise.container.common.spi.util.GlassFishOutputStreamHandler;
import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINEST;

/**
 * A contract that defines a set of methods to serialize / deserialze Java EE
 * objects (even if they are not directly serializable).
 *
 * Some of the objects that are expected to be serialized / de-serialized are a)
 * Local EJB references b) EJB Handles c) JNDI (sub) contexts d) (Non
 * serializable) StatefulSessionBeans
 *
 * @author Mahesh Kannan
 */
@Service
public class JavaEEIOUtilsImpl implements JavaEEIOUtils {

    private static final Logger LOG = LogDomains.getLogger(JavaEEIOUtilsImpl.class, LogDomains.JNDI_LOGGER);

    private final Collection<GlassFishOutputStreamHandler> outputHandlers = new HashSet<>();
    private final Collection<GlassFishInputStreamHandler> inputHandlers = new HashSet<>();

    @Inject
    private ServiceLocator habitat;

    @Override
    public ObjectInputStream createObjectInputStream(InputStream is, boolean resolveObject, ClassLoader loader)
        throws Exception {
        return new GlassFishObjectInputStream(inputHandlers, is, loader, resolveObject);
    }


    @Override
    public ObjectOutputStream createObjectOutputStream(OutputStream os, boolean replaceObject) throws IOException {
        return new GlassFishObjectOutputStream(outputHandlers, os, replaceObject);
    }


    @Override
    public byte[] serializeObject(Object obj, boolean replaceObject) throws IOException {
        if (LOG.isLoggable(FINEST)) {
            LOG.log(FINEST, "serializeObject(object={0}, replaceObject={1})", new Object[] {obj, replaceObject});
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = createObjectOutputStream(bos, replaceObject)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        } catch (NotSerializableException notSerEx) {
            throw notSerEx;
        } catch (Exception th) {
            throw new IOException("Serialization failed.", th);
        }
    }


    @Override
    public Object deserializeObject(byte[] data, boolean resolveObject, ClassLoader appClassLoader) throws Exception {
        if (LOG.isLoggable(FINEST)) {
            LOG.log(FINEST, "deserializeObject(data, resolveObject={1}, classLoader)", resolveObject);
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = createObjectInputStream(bis, resolveObject, appClassLoader)) {
            return ois.readObject();
        } catch (Exception ex) {
            throw ex;
        }
    }


    @Override
    public void addGlassFishOutputStreamHandler(GlassFishOutputStreamHandler handler) {
        outputHandlers.add(handler);

    }


    @Override
    public void removeGlassFishOutputStreamHandler(GlassFishOutputStreamHandler handler) {
        outputHandlers.remove(handler);
    }


    @Override
    public void addGlassFishInputStreamHandler(GlassFishInputStreamHandler handler) {
        inputHandlers.add(handler);
    }


    @Override
    public void removeGlassFishInputStreamHandler(GlassFishInputStreamHandler handler) {
        inputHandlers.remove(handler);
    }
}
