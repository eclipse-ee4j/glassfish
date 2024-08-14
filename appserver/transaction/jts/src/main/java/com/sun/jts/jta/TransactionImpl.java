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

package com.sun.jts.jta;

import com.sun.enterprise.transaction.spi.TransactionInternal;
import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.CosTransactions.ControlImpl;
import com.sun.jts.CosTransactions.GlobalTID;
import com.sun.logging.LogDomains;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.Unavailable;

/**
 * An implementation of jakarta.transaction.Transaction using JTS
 * XXX TODO should catch all org.omg.CORBA.SystemException
 * and throw jakarta.transaction.SystemException
 *
 * @author Tony Ng
 */
public class TransactionImpl implements TransactionInternal {

    /**
     * OTS Control object for this transaction
     */
    private Control control;

    private GlobalTID gtid;

    private TransactionState tranState = null;

    private static TransactionManagerImpl tm = TransactionManagerImpl.getTransactionManagerImpl();
    /*
        Logger to log transaction messages
    */
    static Logger _logger = LogDomains.getLogger(TransactionImpl.class, LogDomains.TRANSACTION_LOGGER);

    // START 4662745
    private long startTime;
    // END 4662745

    public TransactionImpl(Control control, GlobalTID gtid)
        throws SystemException {

        this.control = control;
        this.gtid = gtid;
        startTime=System.currentTimeMillis();
    }

    /**
     * return the OTS Control object for this transaction.
     */
    Control getControl() {
        return control;
    }

    //-----------------------------------------------------------------
    // The following implements jakarta.transaction.Trasaction interface
    //-----------------------------------------------------------------

    /**
     * Complete the transaction represented by this Transaction object
     */
    public void commit() throws HeuristicMixedException,
        RollbackException, HeuristicRollbackException, IllegalStateException,
        SecurityException, SystemException
    {
        try {
            if (Configuration.isLocalFactory()) {
              ((ControlImpl) control).get_localTerminator().commit(true);
            } else {
                control.get_terminator().commit(true);
            }
        } catch (TRANSACTION_ROLLEDBACK ex) {
            RollbackException rbe = new RollbackException();
            Throwable cause = ex.getCause();
            if (cause != null) {
                rbe.initCause(cause);
            }
            throw rbe;
        } catch (INVALID_TRANSACTION ex) {
            throw new IllegalStateException();
        } catch (HeuristicMixed ex) {
            throw new HeuristicMixedException();
        } catch (HeuristicHazard ex) {
            throw new HeuristicMixedException();
        } catch (NO_PERMISSION ex) {
            throw new SecurityException();
        } catch (Unavailable ex) {
            SystemException sException = new SystemException();
            sException.initCause(ex);
            throw sException;
        } catch (Exception ex) {
            SystemException sException = new SystemException();
            sException.initCause(ex);
            throw sException;
        }

    }


    /**
     * Rollback the transaction represented by this Transaction object.
     */
    public void rollback()
        throws IllegalStateException, SystemException {

        try {
            if (Configuration.isLocalFactory()) {
              ((ControlImpl) control).get_localTerminator().rollback();
            } else {
              control.get_terminator().rollback();
            }
        } catch (INVALID_TRANSACTION ex) {
            throw new IllegalStateException();
        } catch (TRANSACTION_ROLLEDBACK ex) {
            throw new IllegalStateException();
        } catch (Unavailable ex) {
            SystemException sException = new SystemException();
            sException.initCause(ex);
            throw sException;
        } catch (Exception ex) {
            SystemException sException = new SystemException();
            sException.initCause(ex);
            throw sException;
        }
    }


    /**
     * enlist a resource with the current transaction
     * If a transaction is marked as rollback, enlistment will
     * succeed if the resource has been enlisted before. Otherwise,
     * enlistment will fail. In both cases, a RollbackException will
     * be thrown.
     */
    public boolean enlistResource(XAResource res)
        throws RollbackException, IllegalStateException,
            SystemException {

        int status = getStatus();
        if (status != jakarta.transaction.Status.STATUS_ACTIVE &&
            status != jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            throw new IllegalStateException();
        }
        //START IASRI 4706150
        try {
            if (TransactionManagerImpl.getXAResourceTimeOut() > 0) {
                res.setTransactionTimeout(TransactionManagerImpl.getXAResourceTimeOut());
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "jts.error_while_setting_xares_txn_timeout", ex);
        }
        // END IASRI 4706150
        try {
            if (tranState == null) {
                tranState = new TransactionState(gtid, this);
                // Synchronization sync = new SynchronizationListener(tranState);
                // registerSynchronization(sync);
            }
            tranState.startAssociation(res, control, status);
            if (status == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
                throw new RollbackException();
            }
            return true;
        } catch (XAException ex) {
            _logger.log(Level.WARNING,"jts.resource_outside_transaction",ex);
            if (ex.errorCode == XAException.XAER_OUTSIDE) {
                throw new IllegalStateException();
            }
            // XXX FIXME should throw rollback exception on XARB_*
            // for now just throw SystemException
            throw new SystemException();
        }

    }


    public boolean delistResource(XAResource res, int flags) throws IllegalStateException, SystemException {
        /*
        int status = getStatus();
        if (status != jakarta.transaction.Status.STATUS_ACTIVE &&
            status != jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            throw new IllegalStateException();
        }
        */

        try {
            // TransactionState tranState = tm.getTransactionState(gtid, this);
            if (tranState == null) {
                // transaction has completed
                throw new IllegalStateException();
            }
            if (tranState.containsXAResource(res) == false) {
                throw new IllegalStateException();
            }
            tranState.endAssociation(res, flags);
            if ((flags & XAResource.TMFAIL) != 0) {
                // set transaction to rollback only if TMFAIL used
                setRollbackOnly();
            }
            return true;
        } catch (XAException ex) {
            setRollbackOnly();
            SystemException se = new SystemException();
            se.initCause(ex);
            throw se;
        }
    }

    public int getStatus() throws SystemException {
        // XXX what should getStatus return on exception?
        Status  status;
        try {
            if (Configuration.isLocalFactory()) {
              status = ((ControlImpl) control).get_localCoordinator().get_status();
            } else {
              status = control.get_coordinator().get_status();
            }
            return TransactionManagerImpl.mapStatus(status);
        } catch (TRANSACTION_ROLLEDBACK ex) {
            return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
        } catch (INVALID_TRANSACTION ex) {
            return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
        } catch (Unavailable ex) {
            return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"jts.unexpected_error_in_getstatus",ex);
            throw new SystemException();
        }
    }

    public boolean equals(Object object) {
        if ((object instanceof TransactionImpl) == false) {
            return false;
        } else if (object == this) {
            return true;
        } else {
            return gtid.equals(((TransactionImpl) object).gtid);
        }
    }

    public int hashCode() {
        return gtid.hashCode();
    }

    public void registerSynchronization(Synchronization sync)
        throws RollbackException, IllegalStateException,
        SystemException {

        int status = getStatus();
        if (status == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            throw new RollbackException();
        }
        if (status != jakarta.transaction.Status.STATUS_ACTIVE) {
            throw new IllegalStateException();
        }
        if (tranState == null) {
            tranState = new TransactionState(gtid, this);
        }
        tranState.registerSynchronization(sync, control, false);
    }

    public void registerInterposedSynchronization(Synchronization sync)
        throws RollbackException, IllegalStateException,
        SystemException {

        int status = getStatus();
        if (status == jakarta.transaction.Status.STATUS_MARKED_ROLLBACK) {
            throw new RollbackException();
        }
        if (status != jakarta.transaction.Status.STATUS_ACTIVE) {
            throw new IllegalStateException();
        }
        if (tranState == null) {
            tranState = new TransactionState(gtid, this);
        }
        tranState.registerSynchronization(sync, control, true);
    }

    public void setRollbackOnly()
        throws IllegalStateException, SystemException {

        int status = getStatus();
        if (status != jakarta.transaction.Status.STATUS_MARKED_ROLLBACK &&
            status != jakarta.transaction.Status.STATUS_ACTIVE) {
            throw new IllegalStateException();
        }
        try {
            if (Configuration.isLocalFactory()) {
              ((ControlImpl) control).get_localCoordinator().rollback_only();
            } else {
              control.get_coordinator().rollback_only();
            }
        } catch (Inactive ex) {
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        } catch (Exception ex) {
            SystemException se = new SystemException();
            se.initCause(ex);
            throw se;
        }
    }


    // START IASRI 4662745
    /**
     * This method is used for the Admin Framework displaying
     * of Transactions Ids
     */
    public String getTransactionId(){
        return gtid.toString();
    }

    /**
     * This method returns the time this transaction was started
     */
    public long getStartTime(){
        return startTime;
    }
    // END IASRI 4662745


/**
class SynchronizationListener implements Synchronization {

    private GlobalTID gtid;
    private TransactionState tranState;

    SynchronizationListener(TransactionState tranState) {
        this.tranState = tranState;
    }

    public void afterCompletion(int status) {
        // tranState.cleanupTransactionStateMapping();
    }

    public void beforeCompletion() {
        try {
        tranState.beforeCompletion();
    }catch(XAException xaex){
        _logger.log(Level.WARNING,"jts.unexpected_xa_error_in_beforecompletion", new java.lang.Object[] {xaex.errorCode, xaex.getMessage()});
        _logger.log(Level.WARNING,"",xaex);
        } catch (Exception ex) {
        _logger.log(Level.WARNING,"jts.unexpected_error_in_beforecompletion",ex);
        }
    }
}
**/

}
