/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming.util;

import com.sun.enterprise.naming.spi.NamingObjectFactory;
import com.sun.enterprise.naming.spi.NamingUtils;
import com.sun.enterprise.naming.util.JndiInitializationNamingObjectFactory;

import org.glassfish.logging.annotation.LogMessageInfo;

import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Singleton;

import javax.naming.Context;
import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Level;

import static com.sun.enterprise.naming.util.LogFacade.logger;
import static org.glassfish.common.util.ObjectInputOutputStreamFactoryFactory.getFactory;;

/**
 * This is a utils class for refactoring the following method.
 */

@Service
@Singleton
public class NamingUtilsImpl implements NamingUtils {
    @LogMessageInfo(message = "Exception in NamingManagerImpl copyMutableObject(): {0}",
    cause = "Problem with serialising or deserialising of the object",
    action = "Check the class hierarchy to see if all the classes are Serializable.")
    public static final String EXCEPTION_COPY_MUTABLE = "AS-NAMING-00006";

    public NamingObjectFactory createSimpleNamingObjectFactory(String name,
        Object value) {
        return new SimpleNamingObjectFactory(name, value);
    }

    public NamingObjectFactory createLazyNamingObjectFactory(String name,
        String jndiName, boolean cacheResult) {
        return new JndiNamingObjectFactory(name, jndiName, cacheResult);
    }

    public NamingObjectFactory createLazyInitializationNamingObjectFactory(String name, String jndiName,
            boolean cacheResult){
        return new JndiInitializationNamingObjectFactory(name, jndiName, cacheResult);
    }

    public NamingObjectFactory createCloningNamingObjectFactory(String name,
        Object value) {
        return new CloningNamingObjectFactory(name, value);
    }

    public NamingObjectFactory createCloningNamingObjectFactory(String name,
        NamingObjectFactory delegate) {
        return new CloningNamingObjectFactory(name, delegate);
    }

    public NamingObjectFactory createDelegatingNamingObjectFactory(String name,
        NamingObjectFactory delegate, boolean cacheResult) {
        return new DelegatingNamingObjectFactory(name, delegate, cacheResult);
    }

    public Object makeCopyOfObject(Object obj) {
        if ( !(obj instanceof Context) && (obj instanceof Serializable) ) {
            if(logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "** makeCopyOfObject:: " + obj);
            }

            try {
                // first serialize the object
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = getFactory().createObjectOutputStream(bos);
                oos.writeObject(obj);
                oos.flush();
                byte[] data = bos.toByteArray();
                oos.close();
                bos.close();

                // now deserialize it
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                final ObjectInputStream ois = getFactory().createObjectInputStream(bis);
                obj = AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws IOException, ClassNotFoundException {
                        return ois.readObject();
                    }
                });
                return obj;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, EXCEPTION_COPY_MUTABLE, ex);
                RuntimeException re = new RuntimeException("Cant copy Serializable object:", ex);
                throw re;
            }
        } else {
            // XXX no copy ?
            return obj;
        }
    }
}
