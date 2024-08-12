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

import com.sun.enterprise.transaction.spi.TransactionalResource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transaction;

import java.util.Set;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface JavaEETransaction
    extends Transaction {

    public  SimpleResource getExtendedEntityManagerResource(EntityManagerFactory factory);

    public SimpleResource getTxEntityManagerResource(EntityManagerFactory factory);

    public void addTxEntityManagerMapping(EntityManagerFactory factory, SimpleResource em);

    public void addExtendedEntityManagerMapping(EntityManagerFactory factory, SimpleResource em);

    public void removeExtendedEntityManagerMapping(EntityManagerFactory factory);

    public <T> void setContainerData(T data);

    public <T> T getContainerData();

    public Set getAllParticipatingPools();

    public Set getResources(Object poolInfo);

    public TransactionalResource getLAOResource();

    public void setLAOResource(TransactionalResource h);

    public TransactionalResource getNonXAResource();

    public void setResources(Set resources, Object poolInfo);

    public boolean isLocalTx();

    public boolean isTimedOut();
}
