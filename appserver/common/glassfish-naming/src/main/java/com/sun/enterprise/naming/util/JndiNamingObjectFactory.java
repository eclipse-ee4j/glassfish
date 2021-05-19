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
import org.glassfish.api.naming.GlassfishNamingManager;
import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class JndiNamingObjectFactory
    implements NamingObjectFactory {

    private String name;

    private String jndiName;

    private AtomicReference value;

    private boolean cacheResult;

    public JndiNamingObjectFactory(String name, String jndiName, boolean cacheResult) {
        this.name = name;
        this.jndiName = jndiName;
        this.cacheResult = cacheResult;
        this.value = new AtomicReference();
    }

    public boolean isCreateResultCacheable() {
        return cacheResult;
    }

    public Object create(Context ic)
            throws NamingException {
        Object result = null;
        try {
            ic.addToEnvironment(GlassfishNamingManager.LOGICAL_NAME, name);
            if (cacheResult) {
                result = value.get();
                if (result == null) {
                    Object tempResult = ic.lookup(jndiName);
                    if (value.compareAndSet(null, tempResult)) {
                        result = tempResult;
                    } else {
                        result = value.get();
                    }
                }
            } else {
                result = ic.lookup(jndiName);
            }
        } finally {
            ic.removeFromEnvironment(GlassfishNamingManager.LOGICAL_NAME);
        }

        return result;
    }

}
