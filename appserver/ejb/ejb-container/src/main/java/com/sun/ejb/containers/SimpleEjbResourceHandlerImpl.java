/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.glassfish.api.invocation.ResourceHandler;

/*
//This class was originally an inner class of AbstractSingletonContainer. I have moved this to a top level
//  class as it is now used by EjbInvocation.clone() method also.
*/
public class SimpleEjbResourceHandlerImpl
        implements ResourceHandler, Synchronization {

    private static Map<Transaction, SimpleEjbResourceHandlerImpl> _resourceHandlers =
            new ConcurrentHashMap<Transaction, SimpleEjbResourceHandlerImpl>();

    private List l = null;
    private Transaction tx = null;
    private TransactionManager tm = null;

    private SimpleEjbResourceHandlerImpl(TransactionManager tm) {
        this.tm = tm;
        checkTransaction();
    }

    public static ResourceHandler createResourceHandler(TransactionManager tm) {
        return new SimpleEjbResourceHandlerImpl(tm);
    }

    public static ResourceHandler getResourceHandler(TransactionManager tm) {
        SimpleEjbResourceHandlerImpl rh = null;
        try {
            Transaction tx = tm.getTransaction();
            if (tx != null) {
                rh = _resourceHandlers.get(tx);
            }
        } catch (Exception e) {
            BaseContainer._logger.log(Level.WARNING, "Exception during Singleton ResourceHandler processing", e);
        }

        if (rh == null) {
            rh = new SimpleEjbResourceHandlerImpl(tm);
        }

        return rh;
    }

    public List getResourceList() {
        if (tx == null) {
            checkTransaction();
        }

        if( l == null ) {
            l = new ArrayList();
        }
        return l;
    }

    public void beforeCompletion() {
        // do nothing
    }

    public void afterCompletion(int status) {
        if (tx != null) {
            _resourceHandlers.remove(tx);
            tx = null;
        }
    }

    private void checkTransaction() {
        try {
            tx = tm.getTransaction();
            if (tx != null) {
                tx.registerSynchronization(this);
                _resourceHandlers.put(tx, this);
            }
        } catch (Exception e) {
            tx = null;
            BaseContainer._logger.log(Level.WARNING, "Exception during Singleton ResourceHandler processing", e);
        }
    }

}
