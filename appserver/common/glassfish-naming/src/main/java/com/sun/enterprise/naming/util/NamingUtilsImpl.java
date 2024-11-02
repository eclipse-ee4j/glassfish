/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import jakarta.inject.Singleton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.naming.Context;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.naming.util.LogFacade.logger;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static org.glassfish.common.util.ObjectInputOutputStreamFactoryFactory.getFactory;

/**
 * This is a utils class for refactoring the following method.
 */

@Service
@Singleton
public class NamingUtilsImpl implements NamingUtils {

    @LogMessageInfo(
        message = "Exception in NamingUtilsImpl copyMutableObject(): {0}",
        cause = "Problem with serialising or deserialising of the object",
        action = "Check the class hierarchy to see if all the classes are Serializable.")
    public static final String EXCEPTION_COPY_MUTABLE = "AS-NAMING-00006";

    @Override
    public <T> NamingObjectFactory createSimpleNamingObjectFactory(SimpleJndiName name, T value) {
        return new SimpleNamingObjectFactory<>(name, value);
    }


    @Override
    public NamingObjectFactory createLazyNamingObjectFactory(SimpleJndiName name, SimpleJndiName jndiName, boolean cacheResult) {
        return new JndiNamingObjectFactory(name, jndiName, cacheResult);
    }


    @Override
    public NamingObjectFactory createLazyInitializationNamingObjectFactory(SimpleJndiName name, SimpleJndiName jndiName, boolean cacheResult) {
        return new JndiInitializationNamingObjectFactory(name, jndiName, cacheResult);
    }


    @Override
    public NamingObjectFactory createCloningNamingObjectFactory(SimpleJndiName name, NamingObjectFactory delegate) {
        return new CloningNamingObjectFactory<>(name, delegate);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T makeCopyOfObject(T obj) {
        if (obj instanceof Context || !(obj instanceof Serializable)) {
            // XXX no copy ?
            return obj;
        }

        logger.log(FINE, "makeCopyOfObject({0})", obj);

        try {
            // first serialize the object
            final byte[] data;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = getFactory().createObjectOutputStream(bos)) {
                oos.writeObject(obj);
                oos.flush();
                data = bos.toByteArray();
            }

            // Now deserialize it
            try (
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    ObjectInputStream ois = getFactory().createObjectInputStream(bis)) {

                return (T) ois.readObject();
            }
        } catch (Exception ex) {
            logger.log(SEVERE, EXCEPTION_COPY_MUTABLE, obj);
            throw new RuntimeException("Cant copy Serializable object " + obj, ex);
        }
    }
}
