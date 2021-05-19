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

import com.sun.enterprise.container.common.spi.util.SerializableObjectFactory;

import com.sun.enterprise.container.common.spi.util.GlassFishInputStreamHandler;
import com.sun.logging.LogDomains;

import java.io.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.common.util.ObjectInputOutputStreamFactory;
import org.glassfish.common.util.ObjectInputOutputStreamFactoryFactory;

/**
 * A class that is used to restore conversational state
 *
 * @author Mahesh Kannan
 */
class GlassFishObjectInputStream extends ObjectInputStream
{
    private ClassLoader appLoader;

    private static Logger _logger = LogDomains.getLogger(GlassFishObjectInputStream.class, LogDomains.JNDI_LOGGER);

    private ObjectInputOutputStreamFactory inputStreamHelper;

    private Collection<GlassFishInputStreamHandler> handlers;

    GlassFishObjectInputStream(Collection<GlassFishInputStreamHandler> handlers,  InputStream in, ClassLoader appCl, boolean resolve)
        throws IOException, StreamCorruptedException
    {
        super(in);
        appLoader = appCl;
        this.handlers = handlers;

        if (resolve) {
            enableResolveObject(resolve);

        }

        inputStreamHelper = ObjectInputOutputStreamFactoryFactory.getFactory();
    }

    @Override
    protected Object resolveObject(Object obj)
        throws IOException
    {
        Object result = obj;
        try {
            if (obj instanceof SerializableObjectFactory) {
                return ((SerializableObjectFactory) obj).createObject();
            } else {
                for (GlassFishInputStreamHandler handler : handlers) {
                    Object r = handler.resolveObject(obj);
                    if (r != null) {
                        result = r == GlassFishInputStreamHandler.NULL_OBJECT ? null : r;
                        break;
                    }
                }

                return result;
            }
        } catch (IOException ioEx ) {
            _logger.log(Level.SEVERE, "ejb.resolve_object_exception", ioEx);
            throw ioEx;
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "ejb.resolve_object_exception", ex);
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    @Override
    protected Class<?> resolveProxyClass(String[] interfaces)
        throws IOException, ClassNotFoundException
    {
        Class<?>[] classObjs = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> cl = Class.forName(interfaces[i], false, appLoader);
            // If any non-public interfaces, delegate to JDK's
            // implementation of resolveProxyClass.
            if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
                return super.resolveProxyClass(interfaces);
            } else {
                classObjs[i] = cl;
            }
        }
        try {
            return Proxy.getProxyClass(appLoader, classObjs);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
                throws IOException, ClassNotFoundException
    {
        Class<?> clazz = inputStreamHelper.resolveClass(this, desc);
        if( clazz == null ) {
            try {
                // First try app class loader
                clazz = appLoader.loadClass(desc.getName());
            }  catch (ClassNotFoundException e) {

                clazz = super.resolveClass(desc);
            }

        }

        return clazz;
    }


}
