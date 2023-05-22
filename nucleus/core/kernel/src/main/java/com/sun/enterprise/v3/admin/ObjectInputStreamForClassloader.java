/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Collection;

/**
 * ObjectInputStream implementation with possibility to provide primary class loader.
 *
 * @author martinmares
 */
public class ObjectInputStreamForClassloader extends ObjectInputStream {

    private final Collection<ClassLoader> classLoaders;

    public ObjectInputStreamForClassloader(InputStream in, Collection<ClassLoader> classLoaders) throws IOException {
        super(in);
        this.classLoaders = classLoaders;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        for (ClassLoader cl : classLoaders) {
            try {
                Class<?> result = Class.forName(classDesc.getName(), false, cl);
                return result;
            } catch (ClassNotFoundException e) {
            }
        }
        return super.resolveClass(classDesc);
    }

}
