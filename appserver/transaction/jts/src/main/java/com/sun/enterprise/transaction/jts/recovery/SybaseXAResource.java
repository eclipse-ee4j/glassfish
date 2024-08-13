/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

//Source File Name:   SybaseXAResource.java

package com.sun.enterprise.transaction.jts.recovery;

import com.sun.enterprise.transaction.api.XAResourceWrapper;
import com.sun.enterprise.util.i18n.StringManager;

import jakarta.resource.ResourceException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * XA Resource wrapper class for sybase XA Resource with jConnect 5.2.
 *
 * @author <a href="mailto:bala.dutt@sun.com">Bala Dutt</a>
 * @version 1.0
 */
public class SybaseXAResource extends XAResourceWrapper {

    // Use superclass Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(XAResourceWrapper.class);

    @Override
    public XAResourceWrapper getInstance() {
        return new SybaseXAResource();
    }

    /**
     * Returns xids list for recovery depending on flags. Sybase XA Resource ignores the flags for XAResource recover call.
     * This method takes care for the fault. Allows the recover call only for TMSTARTRSCAN, for other values of flags just
     * returns null.
     *
     * @param flag an <code>int</code> value
     * @return a <code>Xid[]</code> value
     * @exception XAException if an error occurs
     */
    @Override
    public Xid[] recover(int flag) throws XAException {
        try {
            if (flag == XAResource.TMSTARTRSCAN) {
                return m_xacon.getXAResource().recover(flag);
            }
        } catch (ResourceException e) {
            throw new XAException(sm.getString("transaction.sybase_xa_wrapper_connection_failed", e));
        }

        return null;
    }

    @Override
    public void commit(Xid xid, boolean flag) throws XAException {
        try {
            m_xacon.getXAResource().commit(xid, flag);
        } catch (ResourceException e) {
            throw new XAException(sm.getString("transaction.sybase_xa_wrapper_connection_failed", e));
        }
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        try {
            m_xacon.getXAResource().rollback(xid);
        } catch (ResourceException e) {
            throw new XAException(sm.getString("transaction.sybase_xa_wrapper_connection_failed", e));
        }
    }
}
