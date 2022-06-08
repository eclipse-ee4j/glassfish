/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.jvnet.hk2.annotations.Contract;

/**
 * A contract that defines a set of methods to serialize / deserialize Java EE
 * objects (even if they are not directly serializable).
 * Some of the objects that are expected to be serialized / deserialized are
 * <ol>
 * <li>Local EJB references
 * <li>EJB Handles
 * <li>JNDI (sub) contexts
 * <li>(Non serializable) StatefulSessionBeans
 * </ol>
 *
 * @author Mahesh Kannan
 */
@Contract
public interface JavaEEIOUtils {

    ObjectInputStream createObjectInputStream(InputStream is, boolean resolveObject, ClassLoader loader)
        throws Exception;

    ObjectOutputStream createObjectOutputStream(OutputStream os, boolean replaceObject) throws IOException;

    byte[] serializeObject(Object obj, boolean replaceObject) throws java.io.IOException;

    Object deserializeObject(byte[] data, boolean resolveObject, ClassLoader appClassLoader) throws Exception;

    void addGlassFishOutputStreamHandler(GlassFishOutputStreamHandler handler);

    void removeGlassFishOutputStreamHandler(GlassFishOutputStreamHandler handler);

    void addGlassFishInputStreamHandler(GlassFishInputStreamHandler handler);

    void removeGlassFishInputStreamHandler(GlassFishInputStreamHandler handler);

}
