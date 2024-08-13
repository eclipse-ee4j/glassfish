/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors;


import jakarta.resource.ResourceException;
import jakarta.resource.spi.LazyEnlistableConnectionManager;

import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

/**
 * @author Aditya Gore
 */
public class LazyEnlistableConnectionManagerImpl extends ConnectionManagerImpl
        implements LazyEnlistableConnectionManager
{
    public LazyEnlistableConnectionManagerImpl( PoolInfo poolInfo, ResourceInfo resourceInfo ) {
        super( poolInfo, resourceInfo );
    }

    @Override
    public void lazyEnlist( jakarta.resource.spi.ManagedConnection mc ) throws ResourceException {
        ConnectorRuntime.getRuntime().getPoolManager().lazyEnlist( mc );
    }
}
