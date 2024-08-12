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
import org.omg.CosTransactions.Status;

/**
 * This used for mimicking a RecoveryCoordinator behavior.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
class TxInflowRecoveryCoordinator extends org.omg.CORBA.LocalObject
        implements RecoveryCoordinator {

    /**
     * Simply returns unknown status. This forces the subordinate to wait
     * until the superior site completes the pending in-doubt transaction.
     *
     * @param res The Resource to be recovered. This is ignored.
     *
     * @return unknown status
     */
    public Status replay_completion(Resource res) {
        return Status.StatusUnknown;
    }
}
