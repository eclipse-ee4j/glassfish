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

package com.sun.jts.CosTransactions;

import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Synchronization;

/**
 * This used for mimicking a superior TopCoordinator behavior.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
class TxInflowCoordinator extends TopCoordinator {

    /**
     * Simply returns a recovery coordinator.
     *
     * @return a RecoveryCoordinator object that could be used by a subordinate
     * TopCoordinator to replay completion.
     */
    public RecoveryCoordinator register_resource(Resource res) {
        return new TxInflowRecoveryCoordinator();
    }

    /**
     * Ignores synchronization registrations from the subordinate. The
     * synchronization flows will be initiated when the superior initiates
     * a transaction completion.
     *
     * @see XATerminatorImpl
     */
    public void register_synchronization(Synchronization sync) {}
}
