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

package com.sun.enterprise.v3.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * This subclass of ObjectInputStream uses HK2 to lookup classes not resolved by default ClassLoader.
 *
 * @author Andriy Zhdanov
 */

@Service
public class ObjectInputStreamWithServiceLocator extends ObjectInputStream {

    private final ServiceLocator serviceLocator;

    /**
     * Loader must be non-null;
     *
     * @throws IOException on io error
     * @throws StreamCorruptedException on a corrupted stream
     */

    public ObjectInputStreamWithServiceLocator(InputStream in, ServiceLocator serviceLocator) throws IOException, StreamCorruptedException {

        super(in);
        if (serviceLocator == null) {
            throw new IllegalArgumentException("Illegal null argument to ObjectInputStreamWithLoader");
        }
        this.serviceLocator = serviceLocator;
    }

    /**
     * Use the given ClassLoader rather than using the system class
     *
     * @throws ClassNotFoundException if class can not be loaded
     */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        try {
            // Try superclass first
            return super.resolveClass(classDesc);
        } catch (ClassNotFoundException e) {
            String cname = classDesc.getName();
            if (cname.startsWith("[")) {
                // An array
                Class<?> component; // component class
                int dcount; // dimension
                for (dcount = 1; cname.charAt(dcount) == '['; dcount++) {
                    ;
                }
                if (cname.charAt(dcount) == 'L') {
                    component = loadClass(cname.substring(dcount + 1, cname.length() - 1));
                } else {
                    throw new ClassNotFoundException(cname);// malformed
                }
                int dim[] = new int[dcount];
                for (int i = 0; i < dcount; i++) {
                    dim[i] = 0;
                }
                return Array.newInstance(component, dim).getClass();
            } else {
                return loadClass(cname);
            }
        }
    }

    private Class<?> loadClass(final String cname) throws ClassNotFoundException {
        List<ActiveDescriptor<?>> descriptors;
        // non-services are not known by HK2
        if ("com.oracle.cloudlogic.accountmanager.cli.AccountAwareJobImpl".equals(cname)) {
            descriptors = getDescriptors("com.oracle.cloudlogic.accountmanager.cli.AccountAwareJobCreator");
        } else {
            descriptors = getDescriptors(cname);
        }
        if (descriptors.size() > 0) {
            try {
                return descriptors.get(0).getLoader().loadClass(cname);
            } catch (MultiException ex) {
                throw ex;
            }
        } else {
            throw new ClassNotFoundException(cname);
        }
    }

    private List<ActiveDescriptor<?>> getDescriptors(final String cname) throws ClassNotFoundException {
        return serviceLocator.getDescriptors(new Filter() {
            @Override
            public boolean matches(Descriptor d) {
                return d.getImplementation().equals(cname);
            }
        });
    }
}
