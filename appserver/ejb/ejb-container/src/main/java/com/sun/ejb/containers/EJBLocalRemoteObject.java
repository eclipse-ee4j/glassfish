/*
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

package com.sun.ejb.containers;


import com.sun.ejb.Container;

import java.util.logging.Logger;

/**
 * Implementation common to EJBObjects and EJBLocalObjects.
 * It is extended by EJBObjectImpl and EJBLocalObjectImpl.
 *
 */
public abstract class EJBLocalRemoteObject
{
    protected static final boolean debug = false;


    transient protected static final Logger _logger = EjbContainerUtilImpl.getLogger();

    transient protected BaseContainer container;
    transient protected Object primaryKey;
    transient private boolean removed=false;

    // Only used for stateful SessionBeans
    transient private SessionContextImpl context;

    //Used only for SFSBs.
    private long sfsbClientVersion;


    final void setContainer(Container container)
    {
        this.container = (BaseContainer)container;
    }

    /**
     * Container needs to be accessed from generated code as well
     * as from other classes in this package.  Rather than having one
     * public method, we have a protected one that is used from generated
     * code and a package-private one used within other container classes.
     *
     */

    protected final Container getContainer()
    {
        return container;
    }

    final Container _getContainerInternal()
    {
        return container;
    }

    final void setRemoved(boolean r)
    {
        removed = r;
    }

    final boolean isRemoved()
    {
        return removed;
    }

    final void setKey(Object key)
    {
        primaryKey = key;
    }

    final Object getKey()
    {
        return primaryKey;
    }

    // Only used for stateful SessionBeans
    final SessionContextImpl getContext()
    {
        return context;
    }

    // Only used for stateful SessionBeans
    final void setContext(SessionContextImpl ctx)
    {
        context = ctx;
    }

    // Only used for stateful SessionBeans
    final void clearContext()
    {
        context = null;
    }

    //This is called when a local ref is serialized
    public long getSfsbClientVersion() {
        return this.sfsbClientVersion;
    }

    //This is called when the assocaited SFSB context is
    //  checkpointed / passivated
    public void setSfsbClientVersion(long sfsbClientVersion) {
        this.sfsbClientVersion = sfsbClientVersion;
    }

}
