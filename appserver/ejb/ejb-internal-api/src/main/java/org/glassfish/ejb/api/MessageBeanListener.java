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

package org.glassfish.ejb.api;

import com.sun.appserv.connectors.internal.api.ResourceHandle;

import java.lang.reflect.Method;

/**
 * Lifecycle contract for a single MessageBeanListener. Implemented
 * by the MessageBeanContainer and called by the MessageBeanClient for
 * each message delivery.  Each message delivery MUST call each of the
 * three methods exactly once, in the same thread, and in the following
 * order :
 *
 * 1. beforeMessageDelivery
 * 2. deliverMessage
 * 3. afterMessageDelivery
 *
 *
 * @author Kenneth Saks
 */
public interface MessageBeanListener {

    /**
     * Pre-delivery notification to the container.  Any transaction
     * initialization is peformed here.  In addition, when this method
     * returns, the current thread's context class loader will be set
     * the message-bean's application class loader.
     *
     * @param method is the method that will be invoked during deliverMessage.
     * It is used the container during transaction setup to lookup the
     * appropriate transaction attribute.
     * @param txImported whether a transaction is being imported
     *
     */
    void beforeMessageDelivery(Method method, boolean txImported);

    /**
     * Deliver a message to a message bean instance.
     *
     * @param params to use of the method invocation.  Can be null or
     * an 0-length array if there are 0 arguments.
     *
     * @throws Throwable  This is either an application exception as thrown
     * from the message bean instance or a jakarta.ejb.EJBException in the case
     * that the bean throws a system exception.  Note that exceptions are
     * *always* propagated, regardless of transaction type.
     */
    Object deliverMessage(Object[] params) throws Throwable;

    /**
     * Post-delivery notification to the container.  Container will perform
     * any work required to commit/rollback a container-managed transaction.
     * When this method returns, the thread's context class loader will be
     * restored to the value it had when beforeMessageDelivery was called.
     */
    void afterMessageDelivery();

    ResourceHandle getResourceHandle();

    void setResourceHandle(ResourceHandle handle);

}
