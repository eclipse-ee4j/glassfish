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

package com.sun.ejb;

import jakarta.transaction.Transaction;

import java.util.List;

import org.glassfish.api.invocation.ResourceHandler;

/**
 * The ComponentContext contains context information about an EJB instance.
 * EJBContextImpl implements ComponentContext in addition to EJBContext.
 *
 */

public interface ComponentContext
    extends ResourceHandler {

    /**
     * Get the EJB instance associated with this context.
     */
    Object getEJB();

    /**
     * Get the Container instance which created this Context.
     */
    Container getContainer();

    /**
     * Get the Transaction object associated with this Context.
     */
    Transaction getTransaction();

    /**
     * The EJB spec makes a distinction between access to the TimerService
     * object itself (via EJBContext.getTimerService) and access to the
     * methods on TimerService, Timer, and TimerHandle.  The latter case
     * is covered by this check.
     */
    void checkTimerServiceMethodAccess() throws IllegalStateException;

    /**
     * Get the resources associated with this Context.
     */
    List getResourceList();

}

