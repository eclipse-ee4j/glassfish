/*
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

import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;
import javax.naming.NamingException;

@Service
public class CloningNamingObjectFactory
        implements NamingObjectFactory {

    private static NamingUtils namingUtils = new NamingUtilsImpl();

    private String name;

    private Object value;

    private NamingObjectFactory delegate;

    public CloningNamingObjectFactory(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public CloningNamingObjectFactory(String name, NamingObjectFactory delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    public boolean isCreateResultCacheable() {
        return false;
    }

    public String getName() {
        return name;
    }

    public Object create(Context ic)
            throws NamingException {
        return (delegate != null)
                ? namingUtils.makeCopyOfObject(delegate.create(ic))
                : namingUtils.makeCopyOfObject(value);
    }
}
