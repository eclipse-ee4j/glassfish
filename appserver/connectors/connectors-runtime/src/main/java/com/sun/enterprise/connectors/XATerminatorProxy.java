/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors;

import jakarta.resource.spi.XATerminator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * Proxy for XATerminator.
 *
 * <p>
 * This implementation is Serializable(Externalizable) such that RAR implementation can use it safely in Serialization
 * mandated scenarios<br>
 *
 * @author Jagadish Ramu
 */
public class XATerminatorProxy implements XATerminator, Externalizable {

    private transient XATerminator xaTerminator;

    /**
     * Provides a proxy for XATerminator
     *
     * @param xat Actual XATerminator
     */
    public XATerminatorProxy(XATerminator xat) {
        this.xaTerminator = xat;
    }

    /**
     * Provides a proxy for XATerminator<br>
     * no-args constructor for de-serialization
     */
    public XATerminatorProxy() {
    }

    /**
     * @see jakarta.resource.spi.XATerminator
     */
    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        xaTerminator.commit(xid, onePhase);
    }

    /**
     * @see jakarta.resource.spi.XATerminator
     */
    @Override
    public void forget(Xid xid) throws XAException {
        xaTerminator.forget(xid);
    }

    /**
     * @see jakarta.resource.spi.XATerminator
     */
    @Override
    public int prepare(Xid xid) throws XAException {
        return xaTerminator.prepare(xid);
    }

    /**
     * @see jakarta.resource.spi.XATerminator
     */
    @Override
    public Xid[] recover(int flag) throws XAException {
        return xaTerminator.recover(flag);
    }

    /**
     * @see jakarta.resource.spi.XATerminator
     */
    @Override
    public void rollback(Xid xid) throws XAException {
        xaTerminator.rollback(xid);
    }

    /**
     * @see java.io.Externalizable
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // do nothing
    }

    /**
     * @see java.io.Externalizable
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        xaTerminator = ConnectorRuntime.getRuntime().getTransactionManager().getXATerminator();
    }
}
