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

package org.glassfish.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;

import org.jvnet.hk2.annotations.Service;

/**
 * This subclass of ObjectInputStream delegates loading of classes to
 * an existing ClassLoader.
 */

@Service
public class ObjectInputStreamWithLoader extends ObjectInputStream {
    protected ClassLoader loader;

    /**
     * Loader must be non-null;
     *
     * @throws IOException              on io error
     * @throws StreamCorruptedException on a corrupted stream
     */

    public ObjectInputStreamWithLoader(InputStream in, ClassLoader loader)
            throws IOException, StreamCorruptedException {

        super(in);
        if (loader == null) {
            throw new IllegalArgumentException("Illegal null argument to ObjectInputStreamWithLoader");
        }
        this.loader = loader;
    }

    /**
     * Make a primitive array class
     */

    private Class primitiveType(char type) {
        switch (type) {
            case 'B':
                return byte.class;
            case 'C':
                return char.class;
            case 'D':
                return double.class;
            case 'F':
                return float.class;
            case 'I':
                return int.class;
            case 'J':
                return long.class;
            case 'S':
                return short.class;
            case 'Z':
                return boolean.class;
            default:
                return null;
        }
    }

    /**
     * Use the given ClassLoader rather than using the system class
     *
     * @throws ClassNotFoundException if class can not be loaded
     */
    protected Class resolveClass(ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException {

        try {
            String cname = classDesc.getName();
            if (cname.startsWith("[")) {
                // An array
                Class component;        // component class
                int dcount;            // dimension
                for (dcount = 1; cname.charAt(dcount) == '['; dcount++) ;
                if (cname.charAt(dcount) == 'L') {
                    component = loader.loadClass(cname.substring(dcount + 1,
                            cname.length() - 1));
                } else {
                    if (cname.length() != dcount + 1) {
                        throw new ClassNotFoundException(cname);// malformed
                    }
                    component = primitiveType(cname.charAt(dcount));
                }
                int dim[] = new int[dcount];
                for (int i = 0; i < dcount; i++) {
                    dim[i] = 0;
                }
                return Array.newInstance(component, dim).getClass();
            } else {
                return loader.loadClass(cname);
            }
        } catch (ClassNotFoundException e) {
            // Try also the superclass because of primitive types
            return super.resolveClass(classDesc);
        }
    }
}
