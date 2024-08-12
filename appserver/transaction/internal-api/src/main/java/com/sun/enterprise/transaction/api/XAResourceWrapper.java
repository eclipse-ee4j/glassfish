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

package com.sun.enterprise.transaction.api;

import com.sun.enterprise.util.i18n.StringManager;

import jakarta.resource.spi.ManagedConnection;

import javax.security.auth.Subject;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Wrappers over XAResources extend from this class. This class simply implements the
 * the standard XAResource interface. In addition it holds the XAConnection which is
 * set by XARecoveryManager and is used by deriving classes to implement workarounds.
 * An example of class extending from this is OracleXARescource.
 *
 * @author <a href="mailto:bala.dutt@sun.com">Bala Dutt</a>
 * @version 1.0
 */
public abstract class XAResourceWrapper implements XAResource
{

    /// Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(XAResourceWrapper.class);

    protected ManagedConnection m_xacon;
    protected Subject subject;

    public void init(ManagedConnection xacon,Subject subject){
        m_xacon=xacon;
        this.subject = subject;
    }

    public void end(Xid xid, int i) throws XAException{
        throw new XAException(sm.getString("transaction.for_recovery_only"));
    }

    public void forget(Xid xid) throws XAException{
        throw new XAException(sm.getString("transaction.for_recovery_only"));
    }

    public int getTransactionTimeout() throws XAException{
        throw new XAException(sm.getString("transaction.for_recovery_only"));
    }

    public boolean isSameRM(XAResource xaresource) throws XAException
    {
        throw new XAException(sm.getString("transaction.for_recovery_only"));
    }

    public int prepare(Xid xid) throws XAException{
        throw new XAException(sm.getString("transaction.for_recovery_only"));
    }

    public boolean setTransactionTimeout(int i) throws XAException {
        throw new XAException(sm.getString("transaction.for_recovery_only"));
    }

    public void start(Xid xid, int i) throws XAException{
        throw new XAException(sm.getString("transaction.for_recovery_only"));
    }

    public abstract Xid[] recover(int flag) throws XAException;

    public abstract void commit(Xid xid, boolean flag) throws XAException;

    public abstract void rollback(Xid xid) throws XAException;

    /**
    public Xid[] recover(int flag) throws XAException {
        throw new XAException("This is to be implemented by sub classes");
    }
    public void commit(Xid xid, boolean flag) throws XAException{
        throw new XAException("This is to be implemented by sub classes");
    }
    public void rollback(Xid xid) throws XAException{
        throw new XAException("This is to be implemented by sub classes");
    }
    */

    public abstract XAResourceWrapper getInstance();
}
