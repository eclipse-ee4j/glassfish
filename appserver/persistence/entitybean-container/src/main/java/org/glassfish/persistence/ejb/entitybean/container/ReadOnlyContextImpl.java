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

package org.glassfish.persistence.ejb.entitybean.container;

import com.sun.ejb.containers.BaseContainer;

import jakarta.ejb.EnterpriseBean;

/**
 * Implementation of EJBContext for ReadOnlyBeans. Contains extra
 * attributes that allows selective ejbLoad()
 *
 * @author Mahesh Kannan
 */

public final class ReadOnlyContextImpl
    extends EntityContextImpl
{
    private int pkLevelSequenceNum;
    private long lastRefreshedAt;
    private boolean removed = false;

    // only non-null when associated with a primary-key
    transient private ReadOnlyBeanInfo robInfo;

    ReadOnlyContextImpl(EnterpriseBean ejb, BaseContainer container) {
        super(ejb, container);
    }

    public int getPKLevelSequenceNum() {
        return pkLevelSequenceNum;
    }

    public void incrementPKLevelSequenceNum() {
        pkLevelSequenceNum++;
    }

    public void setPKLevelSequenceNum(int num) {
        pkLevelSequenceNum = num;
    }

    public long getLastRefreshedAt() {
        return lastRefreshedAt;
    }

    public void setLastRefreshedAt(long time) {
        lastRefreshedAt = time;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean value) {
        removed = value;
    }

    public void setReadOnlyBeanInfo(ReadOnlyBeanInfo info) {
        robInfo = info;

        // Whenever read-only bean info is set or nulled out, initialize
        // its derived fields.
        pkLevelSequenceNum = -1;
        lastRefreshedAt = 0;
    }

    public ReadOnlyBeanInfo getReadOnlyBeanInfo() {
        return robInfo;
    }
}
