/*
 * Copyright (c) 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.mock;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.SimpleResource;
import com.sun.enterprise.transaction.spi.TransactionalResource;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import java.util.Set;
import javax.transaction.xa.XAResource;

/**
 * Mock class without any implementation
 */
public class JavaEETransactionMock implements JavaEETransaction {

    @Override
    public void commit()
            throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
        return false;
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
        return false;
    }

    @Override
    public int getStatus() throws SystemException {
        return 0;
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
    }

    @Override
    public SimpleResource getExtendedEntityManagerResource(EntityManagerFactory factory) {
        return null;
    }

    @Override
    public SimpleResource getTxEntityManagerResource(EntityManagerFactory factory) {
        return null;
    }

    @Override
    public void addTxEntityManagerMapping(EntityManagerFactory factory, SimpleResource em) {
    }

    @Override
    public void addExtendedEntityManagerMapping(EntityManagerFactory factory, SimpleResource em) {
    }

    @Override
    public void removeExtendedEntityManagerMapping(EntityManagerFactory factory) {
    }

    @Override
    public <T> void setContainerData(T data) {
    }

    @Override
    public <T> T getContainerData() {
        return null;
    }

    @Override
    public Set getAllParticipatingPools() {
        return null;
    }

    @Override
    public Set getResources(Object poolInfo) {
        return null;
    }

    @Override
    public TransactionalResource getLAOResource() {
        return null;
    }

    @Override
    public void setLAOResource(TransactionalResource h) {
    }

    @Override
    public TransactionalResource getNonXAResource() {
        return null;
    }

    @Override
    public void setResources(Set resources, Object poolInfo) {
    }

    @Override
    public boolean isLocalTx() {
        return false;
    }

    @Override
    public boolean isTimedOut() {
        return false;
    }
}
