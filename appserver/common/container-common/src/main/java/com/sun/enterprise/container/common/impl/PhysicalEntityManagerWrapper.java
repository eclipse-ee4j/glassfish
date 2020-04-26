/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.transaction.api.SimpleResource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.SynchronizationType;

/**
 * Wraps a physical entity manager so that we can carry synchronization type of it.
 * @author  Mitesh Meswani
 */
public class PhysicalEntityManagerWrapper implements SimpleResource {

    private EntityManager em;

    private SynchronizationType synchronizationType;

    public PhysicalEntityManagerWrapper(EntityManager em, SynchronizationType synchronizationType) {
        this.em = em;
        this.synchronizationType = synchronizationType;
    }


    /** The physical entity manager */
    public EntityManager getEM() {
        return em;
    }

    /**
     * SynchronizationType of the physical EM
     */
    public SynchronizationType getSynchronizationType() {
        return synchronizationType;
    }

    @Override
    public boolean isOpen() {
        return em.isOpen();
    }

    @Override
    public void close() {
        em.close();
    }

}
