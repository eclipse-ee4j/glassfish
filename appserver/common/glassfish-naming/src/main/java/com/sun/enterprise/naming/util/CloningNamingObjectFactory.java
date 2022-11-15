/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.api.naming.SimpleJndiName;
import org.jvnet.hk2.annotations.Service;

@Service
public class CloningNamingObjectFactory<T> implements NamingObjectFactory {

    private static final NamingUtils UTILS = new NamingUtilsImpl();

    private final SimpleJndiName name;
    private final T value;
    private final NamingObjectFactory delegate;

    public CloningNamingObjectFactory(SimpleJndiName name, T value) {
        this.name = name;
        this.value = value;
        this.delegate = null;
    }


    public CloningNamingObjectFactory(SimpleJndiName name, NamingObjectFactory delegate) {
        this.name = name;
        this.value = null;
        this.delegate = delegate;
    }


    @Override
    public boolean isCreateResultCacheable() {
        return false;
    }


    public SimpleJndiName getName() {
        return name;
    }


    @Override
    public T create(Context ic) throws NamingException {
        return delegate == null ? UTILS.makeCopyOfObject(value) : UTILS.makeCopyOfObject(delegate.create(ic));
    }
}
