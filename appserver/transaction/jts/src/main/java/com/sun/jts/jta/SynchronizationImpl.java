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

import com.sun.jts.CosTransactions.Configuration;
import com.sun.logging.LogDomains;

import jakarta.transaction.Synchronization;

import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Request;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.SynchronizationHelper;
import org.omg.CosTransactions.SynchronizationPOA;
import org.omg.PortableServer.POA;

/**
 * An implementation of org.omg.CosTransactions.Synchronization
 * this object is activated at creation time and de-activated
 * when after_completion is called
 *
 * @author Tony Ng
 */
public class SynchronizationImpl extends SynchronizationPOA implements org.omg.CosTransactions.Synchronization {

    private Vector syncs;
    private Vector interposedSyncs;
    private POA poa;
    private org.omg.CosTransactions.Synchronization corbaRef = null;
    private TransactionState state = null;
    /**
     * Logger to log transaction messages
     */
    static Logger _logger = LogDomains.getLogger(SynchronizationImpl.class, LogDomains.TRANSACTION_LOGGER);

    public SynchronizationImpl() {
        syncs = new Vector();
        interposedSyncs = new Vector();
        poa = Configuration.getPOA("transient"/*#Frozen*/);
    }

    public SynchronizationImpl(TransactionState state) {
        this();
        this.state = state;
    }


    public void addSynchronization(Synchronization sync, boolean interposed) {
        if (!interposed) {
            syncs.addElement(sync);
        } else {
            interposedSyncs.addElement(sync);
        }
    }


    @Override
    public void before_completion() {
        // Regular syncs first then the interposed syncs
        Enumeration e = syncs.elements();
        while (e.hasMoreElements()) {
            Synchronization sync = (Synchronization) e.nextElement();
            try {
                sync.beforeCompletion();
            } catch (RuntimeException rex) {
                try {
                    state.setRollbackOnly();
                } catch (Exception ex1) {
                    _logger.log(Level.WARNING, "jts.unexpected_error_occurred_in_after_completion", ex1);
                }
                _logger.log(Level.WARNING, "jts.unexpected_error_occurred_in_after_completion", rex);
                throw rex;
            } catch (Exception ex) {
                _logger.log(Level.WARNING, "jts.unexpected_error_occurred_in_after_completion", ex);
            }
        }
        Enumeration e1 = interposedSyncs.elements();
        while (e1.hasMoreElements()) {
            Synchronization sync = (Synchronization) e1.nextElement();
            try {
                sync.beforeCompletion();
            } catch (RuntimeException rex) {
                try {
                    state.setRollbackOnly();
                } catch (Exception ex1) {
                    _logger.log(Level.WARNING, "jts.unexpected_error_occurred_in_after_completion", ex1);
                }
                _logger.log(Level.WARNING, "jts.unexpected_error_occurred_in_after_completion", rex);
                throw rex;
            } catch (Exception ex) {
                _logger.log(Level.WARNING, "jts.unexpected_error_occurred_in_after_completion", ex);
            }
        }
        state.beforeCompletion();
    }


    @Override
    public void after_completion(Status status) {
        try {
            int result = TransactionManagerImpl.mapStatus(status);
            // Interposed Syncs First and then the regular syncs
            Enumeration e1 = interposedSyncs.elements();
            while (e1.hasMoreElements()) {
                Synchronization sync = (Synchronization) e1.nextElement();
                try {
                    sync.afterCompletion(result);
                } catch (Exception ex) {
                    _logger.log(Level.WARNING, "jts.unexpected_error_occurred_in_after_completion", ex);
                }
            }
            Enumeration e = syncs.elements();
            while (e.hasMoreElements()) {
                Synchronization sync = (Synchronization) e.nextElement();
                try {
                    sync.afterCompletion(result);
                } catch (Exception ex) {
                    _logger.log(Level.WARNING, "jts.unexpected_error_occurred_in_after_completion", ex);
                }
            }
        } finally {
            try {
                // deactivate object
                if (corbaRef != null) {
                    if (poa == null) {
                        poa = Configuration.getPOA("transient"/* #Frozen */);
                    }
                    poa.deactivate_object(poa.reference_to_id(corbaRef));
                }
            } catch (Exception ex) {
                _logger.log(Level.WARNING, "jts.unexpected_error_occurred_in_after_completion", ex);
            }
        }
    }


    public org.omg.CosTransactions.Synchronization getCORBAReference() {
        if (poa == null) {
            poa = Configuration.getPOA("transient"/* #Frozen */);
        }

        if (corbaRef == null) {
            try {
                poa.activate_object(this);
                corbaRef = SynchronizationHelper.narrow(poa.servant_to_reference(this));
                // corbaRef = (org.omg.CosTransactions.Synchronization) this;
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, "jts.unexpected_error_in_getcorbareference", ex);
            }
        }

        return corbaRef;
    }

    /*
     * These methods are there to satisy the compiler. At some point
     * when we move towards a tie based model, the org.omg.Corba.Object
     * interface method implementation below shall be discarded.
     */

    @Override
    public org.omg.CORBA.Object _duplicate() {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public void _release() {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public boolean _is_a(String repository_id) {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public boolean _is_equivalent(org.omg.CORBA.Object that) {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public boolean _non_existent() {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public int _hash(int maximum) {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public Request _request(String operation) {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public Request _create_request(Context ctx,
                   String operation,
                   NVList arg_list,
                   NamedValue result) {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public Request _create_request(Context ctx,
                   String operation,
                   NVList arg_list,
                   NamedValue result,
                   ExceptionList exceptions,
                   ContextList contexts) {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public org.omg.CORBA.Object _get_interface_def() {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public org.omg.CORBA.Policy _get_policy(int policy_type) {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public org.omg.CORBA.DomainManager[] _get_domain_managers() {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }

    @Override
    public org.omg.CORBA.Object _set_policy_override(
            org.omg.CORBA.Policy[] policies,
            org.omg.CORBA.SetOverrideType set_add) {
        throw new org.omg.CORBA.NO_IMPLEMENT("This is a locally constrained object.");
    }
}
