/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.spi.stats;

/**
 * An interface that allows monitoring of an SFSBStoreManager implementation
 *
 * @author Mahesh Kannan
 */

public interface MonitorableSFSBStoreManager
    extends StatsProvider
{

    /**
     * Returns the number of passivated / checkpointed sessions in the store
     *    Note that this includes the sessions passivated / checkpointed by
     *    ohter store maangers in the cluster
     */
    public long getCurrentStoreSize();


    /**
     * Notification that the monitoringLevel is either turned off (false)
     *    OR tuened on (true).
     * @param monitoringOn true if monitoring is either HIGH / LOW
     *    false otherwise
     */
    public void monitoringLevelChanged(boolean monitoringOn);

}
