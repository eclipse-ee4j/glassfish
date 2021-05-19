/*
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

import org.jvnet.hk2.annotations.Contract;

import com.sun.enterprise.container.common.spi.util.GlassFishOutputStreamHandler;

import java.io.*;

/**
 * A contract that defines a set of methods to serialize / deserialze Java EE
 *  objects (even if they are not directly serializable).
 *
 * Some of the objects that are expected to be serialized / de-serialized are
 *   a) Local EJB references
 *   b) EJB Handles
 *   c) JNDI (sub) contexts
 *   d) (Non serializable) StatefulSessionBeans
 *
 * @author Mahesh Kannan
 *
 */
@Contract
public interface JavaEEIOUtils {

    public ObjectInputStream createObjectInputStream(InputStream is, boolean resolveObject, ClassLoader loader)
        throws Exception;

    public ObjectOutputStream createObjectOutputStream(OutputStream os, boolean replaceObject)
        throws IOException;

    public byte[] serializeObject(Object obj, boolean replaceObject)
        throws java.io.IOException;

    public Object deserializeObject(byte[] data, boolean resolveObject, ClassLoader appClassLoader)
            throws Exception;

    public void addGlassFishOutputStreamHandler(GlassFishOutputStreamHandler handler);

    public void removeGlassFishOutputStreamHandler(GlassFishOutputStreamHandler handler);

    public void addGlassFishInputStreamHandler(GlassFishInputStreamHandler handler);

    public void removeGlassFishInputStreamHandler(GlassFishInputStreamHandler handler);

}
