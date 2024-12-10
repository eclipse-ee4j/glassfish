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

package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.naming.spi.NamingObjectFactory;

import javax.naming.Context;

import org.glassfish.api.invocation.InvocationManager;


public class FactoryForEntityManagerFactoryWrapper implements NamingObjectFactory {

    private final InvocationManager invocationManager;
    private final ComponentEnvManager componentEnvManager;
    private final String unitName;

    public FactoryForEntityManagerFactoryWrapper(String unitName, InvocationManager invMgr, ComponentEnvManager compEnvMgr) {
        this.unitName = unitName;
        this.invocationManager = invMgr;
        this.componentEnvManager = compEnvMgr;
    }


    @Override
    public boolean isCreateResultCacheable() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityManagerFactoryWrapper create(Context ic) {
        return new EntityManagerFactoryWrapper(unitName, invocationManager, componentEnvManager);
    }

}
