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

package com.sun.enterprise.container.common.spi.util;

import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * A Factory class for creating EJBObject input/output Stream
 *
 * @author Mahesh Kannan
 */
// FIXME: Seems dead, unused.
@Service
public class JavaEEObjectStreamFactory {

    @Inject
    ServiceLocator habitat;

    public static final Logger _logger = LogDomains.getLogger(
            JavaEEObjectStreamFactory.class, LogDomains.UTIL_LOGGER);

    private static Collection<JavaEEObjectStreamHandler> _empty
            = new ArrayList<>();
    /**
     *
     * Obtain an ObjectOutputStream that allows serialization
     *  of a graph of objects. The objects can be plain Serializable objects
     *  or can be converted into Serializable objects using
     *  the handler
     *
     *@throws java.io.IOException when the serialziation fails
     *@return an ObjectOutputStream that can be used to serialize objects
     */
    public ObjectOutputStream createObjectOutputStream(
            final OutputStream os,
            final boolean replaceObject)
        throws IOException
    {
        // Need privileged block here because EJBObjectOutputStream
        // does enableReplaceObject
        ObjectOutputStream oos = null;

        final Collection<JavaEEObjectStreamHandler> handlers = replaceObject
                ? habitat.<JavaEEObjectStreamHandler>getAllServices(JavaEEObjectStreamHandler.class) : _empty;

        if(System.getSecurityManager() == null) {
            oos = new JavaEEObjectOutputStream(os, replaceObject, handlers);
        } else {
            try {
                oos = (ObjectOutputStream) AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                    @Override
                    public java.lang.Object run()
                    throws Exception {
                        return new JavaEEObjectOutputStream(os, replaceObject, handlers);
                    }
                });
            } catch ( PrivilegedActionException ex ) {
                throw (IOException) ex.getException();
            }
        }
        return oos;
    }

    /**
     *
     * Obtain an ObjectInputStream that allows de-serialization
     *  of a graph of objects.
     *
     *@throws java.io.IOException when the de-serialziation fails
     *@return an ObjectInputStream that can be used to deserialize objects
     */
    public ObjectInputStream createObjectInputStream(
            final InputStream is,
            final boolean resolveObject,
            final ClassLoader loader)
        throws Exception
    {
        ObjectInputStream ois = null;
        if ( loader != null ) {

            final Collection<JavaEEObjectStreamHandler> handlers = resolveObject
                ? habitat.<JavaEEObjectStreamHandler>getAllServices(JavaEEObjectStreamHandler.class) : _empty;

            // Need privileged block here because EJBObjectInputStream
            // does enableResolveObject
            if(System.getSecurityManager() == null) {
                ois = new JavaEEObjectInputStream(is, loader, resolveObject, handlers);
            } else {
                try {
                    ois = (ObjectInputStream)AccessController.doPrivileged(
                            new PrivilegedExceptionAction() {
                        @Override
                        public java.lang.Object run()
                        throws Exception {
                            return new JavaEEObjectInputStream(
                                    is, loader, resolveObject, handlers);
                        }
                    });
                } catch ( PrivilegedActionException ex ) {
                    throw (IOException) ex.getException();
                }
            }
        } else {
            ois = new ObjectInputStream(is);
        }

        return ois;
    }

    public final byte[] serializeObject(Object obj, boolean replaceObject)
            throws java.io.IOException
    {
        byte[] data = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = this.createObjectOutputStream(
                    bos, replaceObject);

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

    public final Object deserializeObject(byte[] data, boolean resolveObject,
                                          ClassLoader classLoader)
            throws Exception
    {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(data);
            ois = this.createObjectInputStream(bis, resolveObject,
                    classLoader);
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


}
