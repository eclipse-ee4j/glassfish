/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1995-1997 IBM Corp. All rights reserved.
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

//----------------------------------------------------------------------------
//
// Module:      SenderReceiver.java
//
// Description: Transaction context propagation class.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        June, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.TSIdentification;
import org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified;
import org.omg.CORBA.TSIdentificationPackage.NotAvailable;
import org.omg.CORBA.WrongTransaction;
import org.omg.CosTSPortability.Receiver;
import org.omg.CosTSPortability.Sender;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHolder;

/**
 * The SenderRecevier class is our implemention of the OTS Sender and Receiver
 * classes.
 * <p>
 * Their method are implemented here as passthroughs to avoid dependency on the
 * CosTSPortability package in com.ibm.jts.implement. This is because
 * CosTSPortability is a deprecated interface in the OMG specification.
 *
 * @version 0.1
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see
 */

//----------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.1   SAJH   Initial implementation.
//----------------------------------------------------------------------------

class SenderReceiver implements Sender, Receiver {

    private static SenderReceiver sendRec = new SenderReceiver();
    /*
        Logger to log transaction messages
    */
    static Logger _logger = LogDomains.getLogger(SenderReceiver.class, LogDomains.TRANSACTION_LOGGER);

    /**
     * Default constructor.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    SenderReceiver() {}

    /**
     * Pass the operation through to the CurrentTransaction class.
     *
     * @param id      The request identifier.
     * @param holder  The completed context object.
     *
     * @return
     *
     * @exception TRANSACTION_ROLLEDBACK  The current transaction
     *   has been rolled back.  The message should not be sent and
     *   TRANSACTION_ROLLEDBACK should be returned to the caller.
     * @exception TRANSACTION_REQUIRED  There is no current transaction.
     *
     * @see
     */
    @Override
    public void sending_request(int id, PropagationContextHolder holder)
            throws TRANSACTION_ROLLEDBACK, TRANSACTION_REQUIRED {

        if (_logger.isLoggable(Level.FINE)) {
            if (holder.value != null) {
                _logger.log(Level.FINE, "In sending_request" + ":" + id + "," + holder.value.current.otid.formatID);
            } else {
                _logger.log(Level.FINE, "In sending_request" + ":" + id + "," + holder);
            }
        }

        CurrentTransaction.sendingRequest(id, holder);

        if (_logger.isLoggable(Level.FINE)) {
            if (holder.value != null) {
                _logger.log(Level.FINE, "Out sending_request" + ":" + id + "," + holder.value.current.otid.formatID);
            } else {
                _logger.log(Level.FINE, "Out sending_request" + ":" + id + "," + holder);
            }
        }
    }

    /**
     * Pass the operation through to the CurrentTransaction class.
     *
     * @param id       The request identifier.
     * @param context  The PropagationContext from the message.
     * @param ex       The exception on the message.
     *
     * @return
     *
     * @exception WrongTransaction  The context returned on the reply is for a
     *   different transaction from the current one on the thread.
     *
     * @see
     */
    @Override
    public void received_reply(int id, PropagationContext context,
            org.omg.CORBA.Environment ex)
            throws org.omg.CORBA.WrongTransaction {
        if (_logger.isLoggable(Level.FINE)) {
            if (context != null) {
                _logger.log(Level.FINE, "In received_reply" + ":" + id + "," + context.current.otid.formatID);
            } else {
                _logger.log(Level.FINE, "In received_reply" + ":" + id + ", null context");
            }
        }

        CurrentTransaction.receivedReply(id, context, ex);

        if (_logger.isLoggable(Level.FINE)) {
            if (context != null) {
                _logger.log(Level.FINE, "Out received_reply" + ":" + id + "," + context.current.otid.formatID);
            } else {
                _logger.log(Level.FINE, "Out received_reply" + ":" + id + ", null context");
            }
        }
    }


    /**
     * Pass the operation through to the CurrentTransaction class.
     *
     * @param id       The request identifier.
     * @param context  The PropagationContext from the message.
     *
     * @return
     *
     * @see
     */
    @Override
    public void received_request(int id, PropagationContext context) {

        if (_logger.isLoggable(Level.FINE)) {
            if (context != null) {
                _logger.log(Level.FINE, "In received_request" + ":" + id + "," + context.current.otid.formatID);
            } else {
                _logger.log(Level.FINE, "In received_request" + ":" + id + ", null context");
            }
        }

        CurrentTransaction.receivedRequest(id, context);

        if (_logger.isLoggable(Level.FINE)) {
            if (context != null) {
                _logger.log(Level.FINE, "Out received_request" + ":" + id + "," + context.current.otid.formatID);
            } else {
                _logger.log(Level.FINE, "Out received_request" + ":" + id + ", null context");
            }
        }
    }

    /**
     * Pass the operation through to the CurrentTransaction class.
     *
     * @param id      The request identifier.
     * @param holder  The context to be returned on the reply.
     *
     * @return
     *
     * @exception INVALID_TRANSACTION  The current transaction has
     *   outstanding work on this reply, and has been marked rollback-only,
     *   or the reply is returning when a different transaction is active
     *   from the one active when the request was imported.
     * @exception TRANSACTION_ROLLEDBACK  The current transaction has
     *   already been rolled back.
     *
     * @see
     */
    @Override
    public void sending_reply(int id, PropagationContextHolder holder)
        throws INVALID_TRANSACTION, TRANSACTION_ROLLEDBACK {
        if (_logger.isLoggable(Level.FINE)) {
            if (holder.value != null) {
                _logger.log(Level.FINE, "In sending_reply" + ":" + id + "," + holder.value.current.otid.formatID);
            } else {
                _logger.log(Level.FINE, "In sending_reply" + ":" + id + "," + holder);
            }
        }

        CurrentTransaction.sendingReply(id, holder);

        if (_logger.isLoggable(Level.FINE)) {
            if (holder.value != null) {
                _logger.log(Level.FINE, "Out sending_reply" + ":" + id + "," + holder.value.current.otid.formatID);
            } else {
                _logger.log(Level.FINE, "Out sending_reply" + ":" + id + "," + holder);
            }
        }
    }

    /**
     * Identifies an instance of this class to the TSIdentification object.
     *
     * @param ident  The TSIdentification object.
     *
     * @return
     *
     * @see
     */
    static void identify(TSIdentification ident) {
        try {
            ident.identify_sender(sendRec);
            ident.identify_receiver(sendRec);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Sender/Receiver " + sendRec + " successfully identified");
            }
        } catch (AlreadyIdentified exc) {
            _logger.log(Level.FINE, "jts.already_indetified_communication_manager");
        } catch (NotAvailable exc) {
            _logger.log(Level.WARNING, "jts.unable_to_indetify_communication_manager");
        }
    }


    private void debugMessage(String msg, int id, PropagationContext ctx) {
        // System.err.print is not removed as debug Message will no more be
        // used.
        _logger.log(Level.FINE, msg + ";" + id);
        if (ctx == null) {
            _logger.log(Level.FINE, "");
        } else {
            _logger.log(Level.FINE, "," + ctx.current.otid.formatID);
        }
    }
}
