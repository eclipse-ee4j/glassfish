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

/*
 * ReplicationManager.java
 *
 * Created on December 20, 2005, 1:44 PM
 *
 */

package org.glassfish.web.ha.session.management;


import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreException;

/**
 *
 * @author Larry White
 * @author Rajiv Mordani
 */
public interface ReplicationManager {

    String getApplicationId();

    Object getReplicationSessionMonitor(String id);

//    public void processMessage(ReplicationState message);
//
//    public void processQueryMessage(ReplicationState message, String returnInstance);

    void repair(long repairStartTime);

    void repair(long repairStartTime, boolean checkForStopping);

    void respondToFailure(String instanceName, boolean checkForStopping);


//    public <V> V __load(String id, String version, JxtaBackingStoreImpl jxtaBackingStore)
//            throws BackingStoreException;

<V> V __load(String id, String version, BackingStore backingStore) throws BackingStoreException;

    int processExpiredSessions();

}
