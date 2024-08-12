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
import com.sun.jts.CosTransactions.ControlImpl;
import com.sun.jts.CosTransactions.GlobalTID;
import com.sun.jts.codegen.jtsxa.OTSResource;
import com.sun.jts.jtsxa.OTSResourceImpl;
import com.sun.jts.jtsxa.Utility;
import com.sun.jts.jtsxa.XID;
import com.sun.logging.LogDomains;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.Unavailable;
/**
 * keep track of per-transaction state
 *
 * @author Tony Ng
 */
public class TransactionState {

    /**
     * various association states for an XAResource
     */
    private static final int NOT_EXIST = -1;
    private static final int ASSOCIATED = 0;
    private static final int NOT_ASSOCIATED = 1;
    private static final int ASSOCIATION_SUSPENDED = 2;
    private static final int FAILED = 3;
    private static final int ROLLING_BACK = 4;

    /**
     * a mapping of XAResource -> Integer (state)
     * possible states are listed above
     */
    private Map resourceStates;

    /**
     * Map: XAResource -> Xid
     */
    private Map resourceList;

    /**
     * a set of Xid branches on which xa_start() has been called
     */
    private Set seenXids;

    /**
     * a list of unique resource factory (represented by XAResource)
     * Vector of XAResource objects
     */
    private List factories;

    // the OTS synchronization object for this transaction
    private SynchronizationImpl syncImpl;

    // count of XAResources that are still in active state
    // private int activeResources = 0;

    private GlobalTID gtid;
    private TransactionImpl tran;
    /*
        Logger to log transaction messages
    */

    static Logger _logger = LogDomains.getLogger(TransactionState.class, LogDomains.TRANSACTION_LOGGER);

    // private static TransactionManagerImpl tm = TransactionManagerImpl.getTransactionManagerImpl();

    public TransactionState(GlobalTID gtid, TransactionImpl tran) {
        resourceStates = new HashMap();
        resourceList = new HashMap();
        seenXids = new HashSet();
        factories = new ArrayList();
        this.gtid = gtid;
        this.tran  = tran;
    }


    /**
     * this is called via callback of Synchronization
     * right before a transaction commit or rollback
     * to ensure that all XAResources are properly delisted
     */
    synchronized public void beforeCompletion() {

        boolean exceptionThrown = false;
        XAResource res = null;
        Iterator e = resourceStates.keySet().iterator();
        while (e.hasNext()) {
            try {
                res = (XAResource) e.next();
                int XAState = getXAState(res);
                switch (XAState) {
                    case NOT_ASSOCIATED:
                    case FAILED:
                        break;
                    case ASSOCIATION_SUSPENDED:
                    case ASSOCIATED:
                        Xid xid = (Xid) resourceList.get(res);
                        res.end(xid, XAResource.TMSUCCESS);
                        setXAState(res, NOT_ASSOCIATED);
                        break;
                    case ROLLING_BACK:
                    case NOT_EXIST:
                    default:
                        throw new IllegalStateException("Wrong XA State: " + XAState/*#Frozen*/);
                }
            } catch (Exception ex) {
                setXAState(res, FAILED);
                _logger.log(Level.WARNING,"jts.delist_exception",ex);
                exceptionThrown = true;
            }
        }
        if (exceptionThrown) {
            try {
                tran.setRollbackOnly();
            } catch (Exception ex) {}
        }
    }

    /**
     * This is called from OTS to rollback a particular XAResource
     */
    synchronized public void rollback(XAResource res)
        throws IllegalStateException, XAException {

        // Rollback the requested resource
        _rollback(res);

        // Now rollback all other resources known that are not
        // registered with the RegisteredResources during startAssociation() call
        Iterator e = resourceStates.keySet().iterator();
        while (e.hasNext()) {
            XAResource res0 = (XAResource) e.next();
            if (res0.isSameRM(res) && res0 != res) {
                _end(res0);
            }
        }
    }

    synchronized private void _rollback(XAResource res)
        throws IllegalStateException, XAException {

        Xid xid = (Xid) resourceList.get(res);
        assert_prejdk14(xid != null);
        int XAState = getXAState(res);
        switch (XAState) {
        case NOT_ASSOCIATED:
        case FAILED:
            res.rollback(xid);
            break;
        case ASSOCIATION_SUSPENDED:
        case ASSOCIATED:
            try {
                res.end(xid, XAResource.TMSUCCESS);
            } catch (Exception ex) {
                _logger.log(Level.WARNING,"jts.delist_exception",ex);
            }
            setXAState(res, NOT_ASSOCIATED);
            res.rollback(xid);
            /** was in ASSOCIATED:
            // rollback is deferred until delistment
            setXAState(res, ROLLING_BACK);
            activeResources++;
            **/
            break;
        case ROLLING_BACK:
        case NOT_EXIST:
        default:
            throw new IllegalStateException("Wrong XAState: " +
                                            XAState/*#Frozen*/);
        }
    }

    synchronized private void _end(XAResource res)
            throws IllegalStateException, XAException {

        Xid xid = (Xid) resourceList.get(res);
        assert_prejdk14(xid != null);
        int XAState = getXAState(res);
        switch (XAState) {
        case NOT_ASSOCIATED:
        case FAILED:
            // do nothing
            break;
        case ASSOCIATION_SUSPENDED:
        case ASSOCIATED:
            try {
                res.end(xid, XAResource.TMSUCCESS);
            } catch (Exception ex) {
                _logger.log(Level.WARNING,"jts.delist_exception",ex);
            }
            setXAState(res, NOT_ASSOCIATED);
            break;
        case ROLLING_BACK:
        case NOT_EXIST:
        default:
            throw new IllegalStateException("Wrong XAState: " +
                                            XAState/*#Frozen*/);
        }
    }

    private Xid computeXid(XAResource res, Control control)
        throws Inactive, Unavailable, XAException {

        // one branch id per RM
        int size = factories.size();
        for (int i=0; i<size; i++) {
            XAResource fac = (XAResource) factories.get(i);
            if (res.isSameRM(fac)) {
                // use same branch
                Xid xid = (Xid) resourceList.get(fac);
                return xid;
            }
        }

        // use a different branch
        // XXX ideally should call JTS layer to get the branch id
        XID xid;

        if( Configuration.isLocalFactory()) {
            xid = Utility.getXID(((ControlImpl) control).get_localCoordinator());
        } else {
            xid = Utility.getXID(control.get_coordinator());
        }
        factories.add(res);

        byte[] branchid = parseSize(size);
        byte[] sname = Configuration.getServerNameByteArray();
        byte[] branch = new byte[sname.length + 1 + branchid.length];

        System.arraycopy(sname, 0, branch, 0, sname.length);
        branch[sname.length] = (byte) ',';
        System.arraycopy(branchid, 0, branch, sname.length+1, branchid.length);

        xid.setBranchQualifier(branch);

        return xid;
    }


    synchronized public void startAssociation(XAResource res, Control control,
                                              int status)
        throws XAException, SystemException, IllegalStateException,
        RollbackException
    {
        OTSResource ref;

        if (_logger.isLoggable(Level.FINE)) {
             _logger.log(Level.FINE, "startAssociation for " + res);
        }

        try {
            // XXX should avoid using XID in JTA layer
            Xid xid = null;
            boolean newResource = false;
            boolean seenXid = false;
            if (resourceList.get(res) == null) {
                if (_logger.isLoggable(Level.FINE)) {
                     _logger.log(Level.FINE, "startAssociation for unknown resource");
                }

                // throw RollbackException if try to register
                // a new resource when a transaction is marked rollback
                if (status !=
                    jakarta.transaction.Status.STATUS_ACTIVE) {
                    throw new RollbackException();
                }

                newResource = true;
                xid = computeXid(res, control);
                seenXid = seenXids.contains(xid);

                // register with OTS
                if (!seenXid) {
                    // new branch
                    // COMMENT(Ram J) no need to activate OTSResource object since its local.
                    ref = new OTSResourceImpl(xid, res, this);
                    if( Configuration.isLocalFactory()) {
                      ((ControlImpl) control).get_localCoordinator().register_resource(ref);
                    }
                    else {
                      control.get_coordinator().register_resource(ref);
                    }
                }
                resourceList.put(res, xid);
            } else {
                if (_logger.isLoggable(Level.FINE)) {
                     _logger.log(Level.FINE, "startAssociation for known resource");
                }

                // use the previously computed branch id
                xid = (Xid) resourceList.get(res);
                seenXid = seenXids.contains(xid);
            }

            int XAState = getXAState(res);
            if (_logger.isLoggable(Level.FINE)) {
                 _logger.log(Level.FINE, "startAssociation in state: " + XAState);
            }

            if (!seenXid) {
                // first time this branch is enlisted
                seenXids.add(xid);
                res.start(xid, XAResource.TMNOFLAGS);
                setXAState(res, ASSOCIATED);
            } else {
                // have seen this branch before
                switch (XAState) {
                case NOT_ASSOCIATED:
                case NOT_EXIST:
                    res.start(xid, XAResource.TMJOIN);
                    setXAState(res, ASSOCIATED);
                    break;
                case ASSOCIATION_SUSPENDED:
                    res.start(xid, XAResource.TMRESUME);
                    setXAState(res, ASSOCIATED);
                    break;
                case ASSOCIATED:
                case FAILED:
                case ROLLING_BACK:
                default:
                    throw new IllegalStateException("Wrong XAState: " +
                                                    XAState/*#Frozen*/);
                }
            }

           /**
            // need to do connection enlistment for NativeXAResource
            if (res instanceof NativeXAResource) {
                if (newResource) {
                    ((NativeXAResource) res).enlistConnectionInXA();
                }
            }
           **/
        } catch (XAException ex) {
            setXAState(res, FAILED);
            throw ex;
        } catch (Inactive ex) {
            _logger.log(Level.WARNING,"jts.transaction_inactive",ex);
            throw new SystemException();
        } catch (Unavailable ex) {
            _logger.log(Level.WARNING,"jts.object_unavailable",ex);
            throw new SystemException();
        }
    }

    synchronized
    public void endAssociation(XAResource xares, int flags)
        throws XAException, IllegalStateException
    {
        if (_logger.isLoggable(Level.FINE)) {
             _logger.log(Level.FINE, "endAssociation for " + xares);
        }

        try {
            Xid xid = (Xid) resourceList.get(xares);
            assert_prejdk14(xid != null);
            int XAState = getXAState(xares);
            if (_logger.isLoggable(Level.FINE)) {
                 _logger.log(Level.FINE, "endAssociation in state: " + XAState);
            }

            switch (XAState) {
            case ASSOCIATED:
                if ((flags & XAResource.TMSUCCESS) != 0) {
                    xares.end(xid, XAResource.TMSUCCESS);
                    setXAState(xares, NOT_ASSOCIATED);
                } else if ((flags & XAResource.TMSUSPEND) != 0) {
                    xares.end(xid, XAResource.TMSUSPEND);
                    setXAState(xares, ASSOCIATION_SUSPENDED);
                } else {
                    xares.end(xid, XAResource.TMFAIL);
                    setXAState(xares, FAILED);
                }
                break;
            case ROLLING_BACK:
                // rollback deferred XAResources
                // activeResources--;
                // cleanupTransactionStateMapping();
                xares.end(xid, XAResource.TMSUCCESS);
                setXAState(xares, NOT_ASSOCIATED);
                xares.rollback(xid);

                break;
            case ASSOCIATION_SUSPENDED:
                if ((flags & XAResource.TMSUCCESS) != 0) {
                    xares.end(xid, XAResource.TMSUCCESS);
                    setXAState(xares, NOT_ASSOCIATED);
                } else if ((flags & XAResource.TMSUSPEND) != 0) {
                    throw new IllegalStateException
                        ("Wrong XAState: " + XAState/*#Frozen*/);
                } else {
                    xares.end(xid, XAResource.TMFAIL);
                    setXAState(xares, FAILED);
                }
                break;
            case NOT_ASSOCIATED:
            case NOT_EXIST:
            case FAILED:
            default:
                throw new IllegalStateException("Wrong XAState: " + XAState/*#Frozen*/);
            }
        } catch (XAException ex) {
            setXAState(xares, FAILED);
            throw ex;
        }
    }

   // To be called by SynchronizationImpl, if there are any exception
   // in beforeCompletion() call backs
   void setRollbackOnly()
        throws IllegalStateException, SystemException {
       tran.setRollbackOnly();
   }

   /**
    synchronized void cleanupTransactionStateMapping() {
        if (activeResources == 0) {
            TransactionManagerImpl tm =
                TransactionManagerImpl.getTransactionManagerImpl();
        }
    }
  **/

    synchronized
    public void registerSynchronization(Synchronization sync,
                                        Control control,
                                        boolean interposed)
        throws RollbackException, IllegalStateException,
        SystemException {

        try {
            // One OTS Synchronization object per transaction
            if (syncImpl == null) {
                // syncImpl = new SynchronizationImpl();
                syncImpl = new SynchronizationImpl(this);

                // COMMENT(Ram J) syncImpl is a local object. No need to
                // activate it.
                if (Configuration.isLocalFactory()) {
                  ((ControlImpl) control).get_localCoordinator().register_synchronization(syncImpl);
                } else {
                  control.get_coordinator().register_synchronization(syncImpl);
                }
            }
            syncImpl.addSynchronization(sync, interposed);
        } catch (TRANSACTION_ROLLEDBACK ex) {
            throw new RollbackException();
        } catch (Unavailable ex) {
            _logger.log(Level.WARNING,"jts.object_unavailable",ex);
            throw new SystemException();
        } catch (Inactive ex) {
            _logger.log(Level.WARNING,"jts.transaction_inactive",ex);
            throw new IllegalStateException();
        } catch (Exception ex) {
            _logger.log(Level.WARNING,"jts.exception_in_register_synchronization",ex);
            throw new SystemException();
        }
    }

    private void setXAState(XAResource res, Integer state) {
        if (_logger.isLoggable(Level.FINE)) {
            int oldValue = getXAState(res);
            _logger.log(Level.FINE,"transaction id : " + gtid);
            _logger.log(Level.FINE,"res: " + res + ", old state: " + oldValue + ", new state: " + state);
        }
        resourceStates.put(res, state);
    }

    private int getXAState(XAResource res) {
        Integer result = (Integer) resourceStates.get(res);
        if (result == null) return NOT_EXIST;
        return result.intValue();
    }

    /**
     * list all the XAResources that have been enlisted in this
     * transaction.
     */
    public Enumeration listXAResources() {
        return Collections.enumeration(resourceList.keySet());
        // return resourceList.keys();
    }

    /**
     * return true if res has been enlisted in this transaction;
     * false otherwise.
     */
    public boolean containsXAResource(XAResource res) {
        return resourceList.containsKey(res);
    }

    static private void assert_prejdk14(boolean value) {
        if (!value) {
            Exception e = new Exception();
            _logger.log(Level.WARNING,"jts.assert",e);
        }
    }

    private static byte[] parseSize(int size) {

          switch(size) {
            case 0:
               return new byte[]{0};
            case 1:
               return new byte[]{1};
            case 2:
               return new byte[]{2};
            case 3:
               return new byte[]{3};
            case 4:
               return new byte[]{4};
            case 5:
               return new byte[]{5};
            case 6:
               return new byte[]{6};
            case 7:
               return new byte[]{7};
            case 8:
               return new byte[]{8};
            case 9:
               return new byte[]{9};
        }
        int j = 9;
        byte [] res = new byte[10];
        while (size > 0) {
            res[j--] = (byte) (size % 10);
            size = size / 10;
        }
        int len = 9-j;
        byte [] result = new byte[len];
        System.arraycopy(res, j+1, result, 0, len);
        return result;
    }

}
