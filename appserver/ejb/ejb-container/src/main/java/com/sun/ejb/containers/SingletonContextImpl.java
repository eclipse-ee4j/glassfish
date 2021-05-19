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


import jakarta.ejb.TimerService;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Status;
import javax.naming.InitialContext;
import java.util.logging.Level;

/**
 * Implementation of EJBContext for Singleton SessionBeans
 *
 * @author Mahesh Kannan
 */

public final class SingletonContextImpl
        extends AbstractSessionContextImpl {


    SingletonContextImpl(Object ejb, BaseContainer container) {
        super(ejb, container);
        try {
            initialContext = new InitialContext();
        } catch(Exception ex) {
            _logger.log(Level.FINE, "Exception in creating InitialContext",
                ex);
        }

    }

    @Override
    public TimerService getTimerService() throws IllegalStateException {

        // Instance key is first set after dependency injection but
        // before ejbCreate
        if ( instanceKey == null ) {
            throw new IllegalStateException("Operation not allowed");
        }

        EJBTimerService timerService = EJBTimerService.getValidEJBTimerService();
        return new EJBTimerServiceWrapper(timerService, this);

    }

    @Override
    public void setRollbackOnly()
        throws IllegalStateException
    {
        if (instanceKey == null) {
            throw new IllegalStateException("Singleton setRollbackOnly not allowed");
        }

        if ( container.isBeanManagedTran ) {
            throw new IllegalStateException(
                "Illegal operation for bean-managed transactions");
        }

        doGetSetRollbackTxAttrCheck();

        TransactionManager tm = EjbContainerUtilImpl.getInstance().getTransactionManager();

        try {
            if ( tm.getStatus() == Status.STATUS_NO_TRANSACTION ) {
                // EJB might be in a non-business method (for SessionBeans)
                // or afterCompletion.
                // OR this was a NotSupported/Never/Supports
                // EJB which was invoked without a global transaction.
                // In that case the JDBC connection would have autoCommit=true
                // so the container doesnt have to do anything.
                throw new IllegalStateException("No transaction context.");
            }

            tm.setRollbackOnly();

        } catch (Exception ex) {
            IllegalStateException illEx = new IllegalStateException(ex.toString());
            illEx.initCause(ex);
            throw illEx;
        }
    }

    @Override
    public boolean getRollbackOnly()
        throws IllegalStateException
    {
        if (instanceKey == null) {
            throw new IllegalStateException("Singleton getRollbackOnly not allowed");
        }


        if ( container.isBeanManagedTran ) {
            throw new IllegalStateException(
                "Illegal operation for bean-managed transactions");
        }

        doGetSetRollbackTxAttrCheck();

        TransactionManager tm = EjbContainerUtilImpl.getInstance().getTransactionManager();

        try {
            int status = tm.getStatus();
            if ( status == Status.STATUS_NO_TRANSACTION ) {
                // EJB which was invoked without a global transaction.
                throw new IllegalStateException("No transaction context.");
            }

            return ( status == Status.STATUS_MARKED_ROLLBACK ||
                     status == Status.STATUS_ROLLEDBACK      ||
                     status == Status.STATUS_ROLLING_BACK );

        } catch (Exception ex) {
            IllegalStateException illEx = new IllegalStateException(ex.toString());
            illEx.initCause(ex);
            throw illEx;
        }
    }

    @Override
    public void checkTimerServiceMethodAccess()
        throws IllegalStateException
    {
        if ( instanceKey == null ) {
            throw new IllegalStateException
            ("EJB Timer method calls cannot be called in this context");
        }
    }

    @Override
    public synchronized Object lookup(String name) {
        Object o = null;

        if( name == null ) {
            throw new IllegalArgumentException("Argument is null");
        }
        if( initialContext == null ) {
            throw new IllegalArgumentException("InitialContext is null");
        }
        try {
            // if name starts with java: use it as is.  Otherwise, treat it
            // as relative to the private component namespace.
            String lookupString = name.startsWith("java:") ?
                    name : "java:comp/env/" + name;

            o = initialContext.lookup(lookupString);
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
        return o;
    }

}
