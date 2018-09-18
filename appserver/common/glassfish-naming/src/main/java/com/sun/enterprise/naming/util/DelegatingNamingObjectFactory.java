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

import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;
import javax.naming.NamingException;

@Service
public class DelegatingNamingObjectFactory
    implements NamingObjectFactory {

    private String name;

    private transient volatile Object value;

    private boolean cacheResult;

    private NamingObjectFactory delegate;


    public DelegatingNamingObjectFactory(String name, NamingObjectFactory delegate, boolean cacheResult) {
        this.name = name;
        this.delegate = delegate;
        this.cacheResult = cacheResult;
    }

    public boolean isCreateResultCacheable() {
        return cacheResult;
    }

    public Object create(Context ic)
        throws NamingException {
        Object result = value;
        if (cacheResult) {
            if (value == null) {
                synchronized (this) {
                    if (value == null) {
                        result = value = delegate.create(ic);
                    }
                }
            } else {
                result = value;
            }
        } else {
            result = delegate.create(ic);
        }

        return result;
    }

    public String getName() {
        return name;
    }

}
